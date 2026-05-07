# Networking Strategy - KMP Migration

Dokumen ini menjelaskan strategi networking untuk Auction App dalam konteks Kotlin Multiplatform.

## 📊 Current State

### Android Implementation ✅

**Location:** `app/src/main/java/com/polytron/auctionapp/data/remote/`

**Stack:**
- Ktor Client (Android engine)
- Kotlinx Serialization
- Coroutines for async operations

**Files:**
- `repository/ItemsRepositoryImpl.kt` - Repository implementation
- `model/` - Response models

**Example:**
```kotlin
class ItemsRepositoryImpl(
    private val sessionManager: SessionManager
) : ItemsRepository {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    
    override suspend fun getItems(page: Int, perPage: Int): List<ItemResponse> {
        return client.get("$BASE_URL/items") {
            parameter("page", page)
            parameter("perPage", perPage)
        }.body()
    }
}
```

### Desktop Implementation ✅

**Location:** `desktopApp/src/main/kotlin/com/polytron/auctionapp/desktop/data/`

**Stack:**
- In-memory repository (no networking yet)
- Sample data for testing

**Current:**
```kotlin
class DesktopItemsRepository : ItemsRepository {
    private val itemsState = MutableStateFlow<List<SharedItem>>(sampleData)
    
    override suspend fun refreshItems(): List<SharedItem> {
        // No API call, just return sample data
        return itemsState.value
    }
}
```

---

## 🎯 Migration Options

### Option 1: Keep Android-Only (Current) ⭐ RECOMMENDED

**Pros:**
- ✅ Already working
- ✅ No migration needed
- ✅ Android-specific optimizations possible
- ✅ Faster to implement
- ✅ Less risk

**Cons:**
- ❌ Desktop needs separate implementation
- ❌ Code duplication for networking
- ❌ Different behavior per platform

**Implementation:**
```
Android:
  ItemsRepositoryImpl (Ktor) → API
  
Desktop:
  DesktopItemsRepository (Ktor) → API
  (Separate implementation, same interface)
```

**When to use:**
- Quick MVP/prototype
- Platform-specific requirements
- Different API endpoints per platform

### Option 2: Move to Shared (Ktor KMP) 🚀

**Pros:**
- ✅ Single networking code
- ✅ Consistent behavior
- ✅ Easier to maintain
- ✅ Testable in shared module

**Cons:**
- ❌ Requires migration effort
- ❌ Platform-specific features harder
- ❌ More complex setup

**Implementation:**
```
Shared:
  ApiClient (Ktor KMP) → API
  
Android:
  AndroidItemsRepository → ApiClient
  
Desktop:
  DesktopItemsRepository → ApiClient
```

**When to use:**
- Long-term project
- Multiple platforms planned
- Consistent API behavior needed

### Option 3: Hybrid Approach 🔄

**Pros:**
- ✅ Flexibility per feature
- ✅ Gradual migration
- ✅ Best of both worlds

**Cons:**
- ❌ More complex architecture
- ❌ Mixed patterns

**Implementation:**
```
Shared:
  ApiClient (core networking)
  
Android:
  AndroidItemsRepository → ApiClient + Android-specific
  
Desktop:
  DesktopItemsRepository → ApiClient + Desktop-specific
```

---

## 📋 Recommended Approach: Option 1 (Keep Android-Only)

### Rationale

1. **Current State Works**
   - Android networking already implemented
   - No bugs or issues
   - Production-ready

2. **Desktop Needs Different Approach**
   - Desktop might use different API
   - Desktop might have different auth
   - Desktop might need different caching

3. **Minimal Risk**
   - No breaking changes
   - No migration bugs
   - Faster delivery

4. **Future Flexibility**
   - Can migrate later if needed
   - Can evaluate after desktop is stable
   - Can decide based on real usage

### Implementation Plan

#### Phase 1: Desktop Basic Networking ✅ (Current)

**Status:** In-memory repository with sample data

```kotlin
class DesktopItemsRepository : ItemsRepository {
    private val itemsState = MutableStateFlow(sampleData)
    
    override suspend fun refreshItems(): List<SharedItem> {
        return itemsState.value
    }
}
```

#### Phase 2: Desktop Real API (Next)

**Add Ktor client to Desktop:**

```kotlin
// desktopApp/build.gradle.kts
dependencies {
    implementation("io.ktor:ktor-client-cio:3.2.2")
    implementation("io.ktor:ktor-client-content-negotiation:3.2.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.2.2")
}
```

**Implement API calls:**

```kotlin
class DesktopItemsRepository : ItemsRepository {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    
    private val itemsState = MutableStateFlow<List<SharedItem>>(emptyList())
    
    override suspend fun refreshItems(page: Int, perPage: Int): List<SharedItem> {
        val response = client.get("$BASE_URL/items") {
            parameter("page", page)
            parameter("perPage", perPage)
        }.body<List<ItemResponseDto>>()
        
        val items = response.map { it.toSharedItem() }
        itemsState.value = items
        return items
    }
}
```

#### Phase 3: Add Authentication

**Desktop auth:**

```kotlin
class DesktopItemsRepository(
    private val authManager: DesktopAuthManager
) : ItemsRepository {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) { json() }
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(
                        accessToken = authManager.getToken(),
                        refreshToken = ""
                    )
                }
            }
        }
    }
}
```

#### Phase 4: Add Caching & Offline

**Desktop caching:**

```kotlin
class DesktopItemsRepository(
    private val cache: DesktopCache
) : ItemsRepository {
    override suspend fun refreshItems(): List<SharedItem> {
        return try {
            val items = fetchFromApi()
            cache.save(items)
            items
        } catch (e: Exception) {
            cache.load() // Fallback to cache
        }
    }
}
```

---

## 🔄 Alternative: Migrate to Shared (If Needed Later)

### Step 1: Create Shared API Client

```kotlin
// shared/src/commonMain/kotlin/.../network/ApiClient.kt
class ApiClient {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    
    suspend fun getItems(page: Int, perPage: Int): List<ItemDto> {
        return client.get("$BASE_URL/items") {
            parameter("page", page)
            parameter("perPage", perPage)
        }.body()
    }
}
```

### Step 2: Create Platform-Specific Auth

```kotlin
// shared/src/commonMain/kotlin/.../network/AuthProvider.kt
expect class AuthProvider {
    fun getToken(): String?
}

// Android implementation
actual class AuthProvider(private val context: Context) {
    actual fun getToken(): String? {
        return SessionManager(context).getToken()
    }
}

// Desktop implementation
actual class AuthProvider {
    actual fun getToken(): String? {
        return DesktopAuthManager.getToken()
    }
}
```

### Step 3: Update Repositories

```kotlin
// shared/src/commonMain/kotlin/.../repository/SharedItemsRepository.kt
class SharedItemsRepository(
    private val apiClient: ApiClient,
    private val authProvider: AuthProvider
) : ItemsRepository {
    override suspend fun refreshItems(): List<SharedItem> {
        val token = authProvider.getToken()
        return apiClient.getItems(token).map { it.toSharedItem() }
    }
}
```

---

## 🧪 Testing Strategy

### Android Testing

```kotlin
@Test
fun `repository fetches items from API`() = runTest {
    val mockClient = MockHttpClient()
    val repository = ItemsRepositoryImpl(mockClient)
    
    val items = repository.getItems(1, 10)
    
    assertEquals(10, items.size)
}
```

### Desktop Testing

```kotlin
@Test
fun `desktop repository fetches items`() = runTest {
    val repository = DesktopItemsRepository()
    
    val items = repository.refreshItems()
    
    assertTrue(items.isNotEmpty())
}
```

### Shared Testing (If migrated)

```kotlin
@Test
fun `shared api client fetches items`() = runTest {
    val mockEngine = MockEngine { request ->
        respond(
            content = """[{"id":"1","name":"Item 1"}]""",
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }
    
    val client = ApiClient(mockEngine)
    val items = client.getItems(1, 10)
    
    assertEquals(1, items.size)
}
```

---

## 📊 Decision Matrix

| Criteria | Keep Android-Only | Move to Shared | Hybrid |
|----------|-------------------|----------------|--------|
| **Implementation Time** | ⭐⭐⭐ Fast | ⭐ Slow | ⭐⭐ Medium |
| **Maintenance** | ⭐⭐ Medium | ⭐⭐⭐ Easy | ⭐ Complex |
| **Code Reuse** | ⭐ Low | ⭐⭐⭐ High | ⭐⭐ Medium |
| **Flexibility** | ⭐⭐⭐ High | ⭐ Low | ⭐⭐⭐ High |
| **Risk** | ⭐⭐⭐ Low | ⭐ High | ⭐⭐ Medium |
| **Testing** | ⭐⭐ Medium | ⭐⭐⭐ Easy | ⭐⭐ Medium |

**Recommendation:** Start with **Keep Android-Only**, migrate to **Shared** only if:
- Multiple platforms need same API
- API behavior must be identical
- Team has KMP networking experience

---

## 🎯 Current Decision: Keep Android-Only ✅

### Rationale

1. **Android networking works** - no need to fix what's not broken
2. **Desktop can implement separately** - more flexibility
3. **Lower risk** - no migration bugs
4. **Faster delivery** - focus on features, not refactoring
5. **Future flexibility** - can migrate later if needed

### Next Steps

1. ✅ Keep Android `ItemsRepositoryImpl` as-is
2. 🔄 Implement Desktop `DesktopItemsRepository` with Ktor
3. 🔄 Add authentication to Desktop
4. 🔄 Add caching to Desktop
5. 📋 Evaluate migration to shared after 3-6 months

---

## 📚 References

- [Ktor Client - Multiplatform](https://ktor.io/docs/getting-started-ktor-client-multiplatform-mobile.html)
- [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
- [KMP Networking Best Practices](https://kotlinlang.org/docs/multiplatform-mobile-ktor-sqldelight.html)

---

**Last Updated:** May 7, 2026  
**Decision:** Keep Android-Only (Option 1)  
**Review Date:** Q4 2026
