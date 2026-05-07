# Quick Start Guide - Auction App KMP

Panduan cepat untuk memulai development Auction App setelah migrasi KMP.

## 🚀 Getting Started

### Prerequisites

- **JDK 21** or higher
- **Android Studio** Hedgehog or newer
- **Gradle 8.x**
- **Kotlin 2.3.10**

### Clone & Setup

```bash
git clone <repository-url>
cd AuctionApp
./gradlew build
```

---

## 📱 Running Android App

### From Android Studio

1. Open project in Android Studio
2. Select `app` configuration
3. Choose device/emulator
4. Click Run ▶️

### From Command Line

```bash
# Debug build
./gradlew :app:assembleDebug

# Install to device
./gradlew :app:installDebug

# Run
adb shell am start -n com.polytron.auctionapp/.MainActivity
```

---

## 🖥️ Running Desktop App

### From IntelliJ IDEA

1. Open project in IntelliJ IDEA
2. Navigate to `desktopApp/src/main/kotlin/.../Main.kt`
3. Click green run button ▶️

### From Command Line

```bash
# Run desktop app
./gradlew :desktopApp:run

# Build distributable
./gradlew :desktopApp:packageDistributionForCurrentOS

# Output: desktopApp/build/compose/binaries/main/
```

---

## 🏗️ Project Structure

```
AuctionApp/
├── app/                    # Android application
│   ├── src/main/java/
│   │   └── com/polytron/auctionapp/
│   │       ├── data/       # Android-specific data layer
│   │       ├── di/         # Dependency injection
│   │       ├── ui/         # Android ViewModels
│   │       └── view/       # Compose UI screens
│   └── build.gradle.kts
│
├── shared/                 # Shared KMP module
│   ├── src/commonMain/kotlin/
│   │   └── com/polytron/auctionapp/shared/
│   │       ├── model/      # Domain models
│   │       ├── repository/ # Repository interface
│   │       ├── usecase/    # Business logic
│   │       ├── viewmodel/  # Shared ViewModel
│   │       └── util/       # Utilities
│   └── build.gradle.kts
│
├── desktopApp/            # Desktop application
│   ├── src/main/kotlin/
│   │   └── com/polytron/auctionapp/desktop/
│   │       ├── data/       # Desktop repository
│   │       ├── di/         # Dependency injection
│   │       ├── AuctionDesktopApp.kt
│   │       └── Main.kt
│   └── build.gradle.kts
│
└── Documentation/
    ├── KMP_MIGRATION_CHECKLIST.md
    ├── KMP_MIGRATION_SUMMARY.md
    ├── PLATFORM_FEATURES.md
    ├── NETWORKING_STRATEGY.md
    └── MIGRATION_COMPLETE.md
```

---

## 🔧 Common Tasks

### Adding a New Feature

1. **Define in Shared Module**
   ```kotlin
   // shared/src/commonMain/kotlin/.../usecase/
   class MyNewUseCase {
       operator fun invoke(data: Data): Result {
           // Business logic here
       }
   }
   ```

2. **Add to ViewModel**
   ```kotlin
   // shared/src/commonMain/kotlin/.../viewmodel/
   class ItemsSharedViewModel(...) {
       fun myNewFeature() {
           viewModelScope.launch {
               val result = MyNewUseCase()(data)
               _state.update { it.copy(result = result) }
           }
       }
   }
   ```

3. **Use in UI (Android)**
   ```kotlin
   // app/src/main/java/.../view/screens/
   @Composable
   fun MyScreen() {
       val sharedVm: ItemsSharedViewModel = koinInject()
       val state by sharedVm.state.collectAsState()
       
       Button(onClick = { sharedVm.myNewFeature() }) {
           Text("New Feature")
       }
   }
   ```

4. **Use in UI (Desktop)**
   ```kotlin
   // desktopApp/src/main/kotlin/.../
   @Composable
   fun MyDesktopScreen() {
       val sharedVm: ItemsSharedViewModel = koinInject()
       val state by sharedVm.state.collectAsState()
       
       Button(onClick = { sharedVm.myNewFeature() }) {
           Text("New Feature")
       }
   }
   ```

### Adding a New Model

```kotlin
// shared/src/commonMain/kotlin/.../model/
data class MyNewModel(
    val id: String,
    val name: String,
    val value: Int
)
```

### Adding a New Use Case

```kotlin
// shared/src/commonMain/kotlin/.../usecase/
class ProcessDataUseCase {
    operator fun invoke(input: List<Data>): List<Result> {
        return input.map { data ->
            // Process data
            Result(data.id, data.value * 2)
        }
    }
}
```

---

## 🧪 Testing

### Run All Tests

```bash
./gradlew test
```

### Run Specific Module Tests

```bash
# Shared module
./gradlew :shared:test

# Android module
./gradlew :app:testDebugUnitTest

# Desktop module
./gradlew :desktopApp:test
```

### Writing Tests

**Shared Module Test:**
```kotlin
// shared/src/commonTest/kotlin/
class MyUseCaseTest {
    @Test
    fun `test use case logic`() {
        val useCase = MyUseCase()
        val result = useCase(input)
        assertEquals(expected, result)
    }
}
```

**Android Test:**
```kotlin
// app/src/test/java/
class MyViewModelTest {
    @Test
    fun `test viewmodel behavior`() = runTest {
        val viewModel = MyViewModel(mockRepository)
        viewModel.doSomething()
        assertEquals(expected, viewModel.state.value)
    }
}
```

---

## 🔍 Debugging

### Android Debugging

1. Set breakpoint in code
2. Run in Debug mode (🐛)
3. Use Android Studio debugger

### Desktop Debugging

1. Set breakpoint in code
2. Run in Debug mode from IntelliJ
3. Use IntelliJ debugger

### Shared Module Debugging

- Can debug from either Android or Desktop
- Breakpoints work in shared code
- Use platform-specific debugger

---

## 📚 Key Concepts

### 1. Shared ViewModel (Singleton)

```kotlin
// Injected via Koin as singleton
val sharedVm: ItemsSharedViewModel = koinInject()

// NOT created with remember
// val sharedVm = remember { ItemsSharedViewModel() } ❌
```

### 2. State Management

```kotlin
// State is immutable
data class ItemsSharedState(
    val items: List<SharedItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

// Update with copy
_state.update { it.copy(isLoading = true) }
```

### 3. Use Cases

```kotlin
// Use cases are operators
class FilterItemsUseCase {
    operator fun invoke(items: List<Item>, query: String): List<Item> {
        return items.filter { it.name.contains(query) }
    }
}

// Usage
val filtered = FilterItemsUseCase()(items, query)
```

### 4. Repository Pattern

```kotlin
// Interface in shared
interface ItemsRepository {
    suspend fun getItems(): List<SharedItem>
}

// Implementation per platform
class AndroidItemsRepository : ItemsRepository {
    override suspend fun getItems() = api.fetchItems()
}

class DesktopItemsRepository : ItemsRepository {
    override suspend fun getItems() = localDb.getItems()
}
```

---

## 🐛 Troubleshooting

### Build Errors

**Problem:** Gradle sync fails  
**Solution:**
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

**Problem:** Kotlin version mismatch  
**Solution:** Check `gradle/libs.versions.toml` and ensure all Kotlin versions match

### Runtime Errors

**Problem:** Koin dependency not found  
**Solution:** Check DI module registration in `AppModule.kt` or `DesktopModule.kt`

**Problem:** State not updating  
**Solution:** Ensure using `_state.update { }` not direct assignment

### Desktop-Specific Issues

**Problem:** Window doesn't open  
**Solution:** Check Main.kt has correct `application { }` block

**Problem:** Koin not initialized  
**Solution:** Ensure `startKoin { }` is called before Window creation

---

## 📖 Documentation

### Essential Reading

1. **[KMP_MIGRATION_CHECKLIST.md](./KMP_MIGRATION_CHECKLIST.md)** - Migration steps
2. **[PLATFORM_FEATURES.md](./PLATFORM_FEATURES.md)** - Platform-specific features
3. **[NETWORKING_STRATEGY.md](./NETWORKING_STRATEGY.md)** - Networking approach
4. **[shared/README.md](./shared/README.md)** - Shared module docs
5. **[desktopApp/README.md](./desktopApp/README.md)** - Desktop app docs

### Quick References

- **Shared Models:** `shared/src/commonMain/kotlin/.../model/`
- **Use Cases:** `shared/src/commonMain/kotlin/.../usecase/`
- **ViewModel:** `shared/src/commonMain/kotlin/.../viewmodel/`
- **Android Screens:** `app/src/main/java/.../view/screens/`
- **Desktop UI:** `desktopApp/src/main/kotlin/.../`

---

## 🎯 Best Practices

### DO ✅

- Use shared ViewModel via Koin injection
- Put business logic in use cases
- Keep models in shared module
- Write tests for use cases
- Document platform-specific features
- Use immutable state
- Update state with `copy()`

### DON'T ❌

- Create ViewModel with `remember { }`
- Put business logic in UI
- Use platform-specific code in shared
- Mutate state directly
- Skip documentation
- Duplicate logic across platforms

---

## 🚀 Next Steps

### For New Developers

1. Read [MIGRATION_COMPLETE.md](./MIGRATION_COMPLETE.md)
2. Explore shared module structure
3. Run Android and Desktop apps
4. Try adding a simple feature
5. Write a test

### For Feature Development

1. Define feature in shared module
2. Add use case if needed
3. Update ViewModel
4. Implement UI per platform
5. Write tests
6. Update documentation

### For Bug Fixes

1. Identify if bug is in shared or platform code
2. Write failing test
3. Fix the bug
4. Verify test passes
5. Test on all platforms

---

## 💡 Tips & Tricks

### Tip 1: Use Computed Properties

```kotlin
data class State(
    val items: List<Item>,
    val query: String
) {
    val filteredItems: List<Item>
        get() = items.filter { it.name.contains(query) }
}
```

### Tip 2: Use Extension Functions

```kotlin
fun String?.toPrice(): Long = 
    this?.replace(Regex("\\D"), "")?.toLongOrNull() ?: 0L

// Usage
val price = "Rp 100.000".toPrice() // 100000
```

### Tip 3: Use Sealed Classes for Events

```kotlin
sealed class UiEvent {
    data class ShowError(val message: String) : UiEvent()
    data class Navigate(val route: String) : UiEvent()
    object Success : UiEvent()
}
```

### Tip 4: Use Flow for Reactive Data

```kotlin
val items: Flow<List<Item>> = repository.observeItems()
    .map { it.filter { item -> item.isActive } }
    .flowOn(Dispatchers.IO)
```

---

## 📞 Getting Help

### Resources

- **Project Documentation:** See `/docs` folder
- **Shared Module:** See `shared/README.md`
- **Desktop App:** See `desktopApp/README.md`
- **Kotlin Multiplatform:** https://kotlinlang.org/docs/multiplatform.html
- **Compose Multiplatform:** https://www.jetbrains.com/lp/compose-multiplatform/

### Common Questions

**Q: How do I add a new screen?**  
A: Create Composable in platform module, use shared ViewModel via Koin

**Q: How do I share code between platforms?**  
A: Put code in shared module, use expect/actual for platform-specific

**Q: How do I test shared code?**  
A: Write tests in `shared/src/commonTest/kotlin/`

**Q: How do I debug shared code?**  
A: Set breakpoints, debug from either Android or Desktop

---

## ✅ Checklist for New Features

- [ ] Define models in shared (if needed)
- [ ] Create use case in shared (if needed)
- [ ] Update ViewModel in shared
- [ ] Implement UI in Android
- [ ] Implement UI in Desktop
- [ ] Write tests
- [ ] Update documentation
- [ ] Test on both platforms
- [ ] Code review
- [ ] Merge

---

**Happy Coding!** 🎉

For more information, see [MIGRATION_COMPLETE.md](./MIGRATION_COMPLETE.md)
