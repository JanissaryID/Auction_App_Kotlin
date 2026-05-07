# Shared Module - Auction App KMP

Shared business logic module untuk Auction App yang dapat digunakan oleh Android dan Desktop platform.

## 📦 Structure

```
shared/
├── model/
│   └── SharedItem.kt              # Domain model untuk item
├── repository/
│   └── ItemsRepository.kt         # Repository interface
├── usecase/
│   ├── FilterItemsUseCase.kt      # Filter items by search query
│   ├── GroupItemsByNameUseCase.kt # Group items by name
│   ├── GroupItemsByOrderUseCase.kt# Group items by order ID
│   └── CalculateTotalsUseCase.kt  # Calculate totals
└── viewmodel/
    └── ItemsSharedViewModel.kt    # Shared ViewModel with state
```

## 🎯 Core Components

### 1. SharedItem (Model)

Domain model yang merepresentasikan item auction:

```kotlin
data class SharedItem(
    val id: String,
    val nameItem: String? = null,
    val codeItem: String? = null,
    val buyer: String? = null,
    val price: String? = null,
    val orderId: String? = null,
    val status: Int? = null
)
```

**Status Values:**
- `1` - Item tersedia untuk lelang
- `2` - Item sudah dibayar (ready untuk diambil)
- `3` - Item sudah diambil

### 2. ItemsRepository (Interface)

Contract untuk data operations:

```kotlin
interface ItemsRepository {
    fun observeItems(): Flow<List<SharedItem>>
    suspend fun refreshItems(page: Int = 1, perPage: Int = 500): List<SharedItem>
    suspend fun createItem(item: SharedItem): SharedItem
    suspend fun updateItem(id: String, item: SharedItem): SharedItem
    suspend fun deleteItem(id: String)
    suspend fun updateItemStatus(id: String, status: Int)
}
```

**Platform Implementations:**
- **Android:** `AndroidSharedItemsRepository` - adapter dari Android repository
- **Desktop:** `DesktopItemsRepository` - in-memory implementation

### 3. Use Cases

#### FilterItemsUseCase
Filter items berdasarkan search query (nama atau kode):

```kotlin
operator fun invoke(items: List<SharedItem>, query: String): List<SharedItem>
```

#### GroupItemsByNameUseCase
Group items berdasarkan nama (extract base name):

```kotlin
operator fun invoke(items: List<SharedItem>): Map<String, List<SharedItem>>
```

**Example:**
- "Sanco - 1", "Sanco - 2" → grouped as "Sanco"
- "Kursi - 1", "Kursi - 2" → grouped as "Kursi"

#### GroupItemsByOrderUseCase
Group items berdasarkan order ID:

```kotlin
operator fun invoke(items: List<SharedItem>): Map<String, List<SharedItem>>
```

#### CalculateTotalsUseCase
Calculate total price dari list items:

```kotlin
operator fun invoke(items: List<SharedItem>): Long
```

### 4. ItemsSharedViewModel

Shared ViewModel dengan state management:

```kotlin
class ItemsSharedViewModel(
    private val repository: ItemsRepository
) {
    val state: StateFlow<ItemsSharedState>
    
    fun refreshItems(page: Int = 1, perPage: Int = 500)
    fun updateSearchQuery(value: String)
    fun updateItemStatus(id: String, status: Int, onComplete: () -> Unit = {})
    fun createItem(item: SharedItem, onComplete: (SharedItem) -> Unit = {})
    fun updateItem(id: String, item: SharedItem, onComplete: () -> Unit = {})
    fun deleteItem(id: String, onComplete: () -> Unit = {})
    fun clearError()
}
```

**State:**
```kotlin
data class ItemsSharedState(
    val searchQuery: String = "",
    val items: List<SharedItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val filteredItems: List<SharedItem>        // Computed
    val groupedByName: Map<String, List<SharedItem>>  // Computed
    val groupedByOrderId: Map<String, List<SharedItem>> // Computed
}
```

## 🔧 Usage

### Android

**1. Setup DI (Koin):**

```kotlin
// AppModule.kt
single<ItemsRepository> { 
    AndroidSharedItemsRepository(remoteRepository = get()) 
}
single { 
    ItemsSharedViewModel(repository = get()) 
}
```

**2. Inject in Composable:**

```kotlin
@Composable
fun MyScreen() {
    val sharedVm: ItemsSharedViewModel = koinInject()
    val state by sharedVm.state.collectAsState()
    
    LaunchedEffect(Unit) {
        sharedVm.refreshItems()
    }
    
    // Use state.filteredItems, state.groupedByName, etc.
}
```

### Desktop

**1. Setup DI (Koin):**

```kotlin
// DesktopModule.kt
single<ItemsRepository> { 
    DesktopItemsRepository() 
}
single { 
    ItemsSharedViewModel(repository = get()) 
}
```

**2. Initialize Koin:**

```kotlin
// Main.kt
fun main() = application {
    startKoin {
        modules(desktopModule)
    }
    
    Window(onCloseRequest = ::exitApplication) {
        AuctionDesktopApp()
    }
}
```

**3. Inject in Composable:**

```kotlin
@Composable
fun AuctionDesktopApp() {
    val sharedVm: ItemsSharedViewModel = koinInject()
    val state by sharedVm.state.collectAsState()
    
    LaunchedEffect(Unit) {
        sharedVm.refreshItems()
    }
    
    // Use state.filteredItems, state.groupedByName, etc.
}
```

## 🎨 State Management Pattern

### Computed Properties

State menggunakan computed properties untuk derived data:

```kotlin
val filteredItems: List<SharedItem>
    get() = FilterItemsUseCase()(items, searchQuery)

val groupedByName: Map<String, List<SharedItem>>
    get() = GroupItemsByNameUseCase()(filteredItems)

val groupedByOrderId: Map<String, List<SharedItem>>
    get() = GroupItemsByOrderUseCase()(filteredItems)
```

**Benefits:**
- Always up-to-date (recomputed when dependencies change)
- No manual synchronization needed
- Immutable state pattern

### Reactive Updates

ViewModel observes repository changes automatically:

```kotlin
init {
    repository.observeItems()
        .onEach { items ->
            _state.update { it.copy(items = items, isLoading = false) }
        }
        .launchIn(viewModelScope)
}
```

**Flow:**
1. Repository emits new data
2. ViewModel receives update
3. State updates automatically
4. UI recomposes with new data

## 🧪 Testing

### Unit Test Example

```kotlin
class ItemsSharedViewModelTest {
    private lateinit var repository: FakeItemsRepository
    private lateinit var viewModel: ItemsSharedViewModel
    
    @Before
    fun setup() {
        repository = FakeItemsRepository()
        viewModel = ItemsSharedViewModel(repository)
    }
    
    @Test
    fun `updateSearchQuery filters items correctly`() = runTest {
        // Given
        repository.setItems(listOf(
            SharedItem(id = "1", nameItem = "Sanco - 1"),
            SharedItem(id = "2", nameItem = "Kursi - 1")
        ))
        
        // When
        viewModel.updateSearchQuery("Sanco")
        
        // Then
        val state = viewModel.state.value
        assertEquals(1, state.filteredItems.size)
        assertEquals("Sanco - 1", state.filteredItems[0].nameItem)
    }
}
```

## 📚 Dependencies

```kotlin
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}
```

## 🔄 Migration Notes

### From Android-Only to Shared

**Before:**
```kotlin
// Android ViewModel
class ItemsViewModel(private val repository: ItemsRepository) {
    private val _items = MutableStateFlow<List<ItemResponse>>(emptyList())
    val items = _items.asStateFlow()
    
    fun filterItems(query: String) {
        // Filtering logic in ViewModel
    }
}
```

**After:**
```kotlin
// Shared ViewModel
class ItemsSharedViewModel(private val repository: ItemsRepository) {
    private val _state = MutableStateFlow(ItemsSharedState())
    val state = _state.asStateFlow()
    
    // Filtering logic in use case
    // State has computed property: filteredItems
}
```

**Benefits:**
- Business logic moved to use cases
- State is immutable
- Computed properties for derived data
- Reusable across platforms

## 🚀 Future Enhancements

### Planned Features

1. **Pagination Support**
   - Add pagination state to ItemsSharedState
   - Implement load more functionality

2. **Offline Support**
   - Cache items locally
   - Sync when online

3. **Real-time Updates**
   - WebSocket support for live updates
   - Optimistic updates

4. **Advanced Filtering**
   - Filter by status
   - Filter by price range
   - Filter by buyer

5. **Sorting**
   - Sort by name
   - Sort by price
   - Sort by date

## 📖 References

- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Koin Documentation](https://insert-koin.io/)

---

**Version:** 1.0.0  
**Last Updated:** May 7, 2026
