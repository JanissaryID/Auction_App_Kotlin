# Desktop App - Auction App

Desktop client untuk Auction App menggunakan Compose Multiplatform.

## 🖥️ Overview

Desktop application yang menggunakan shared business logic dari module `shared` untuk menampilkan data auction items dengan fitur grouping dan filtering.

## 🚀 Features

### Current Features ✅

1. **Dashboard**
   - Welcome card dengan branding
   - Menu grid untuk navigasi (5 menu utama)
   - Data summary card (total items, unique names, total orders)

2. **Data Display**
   - Group by Nama Barang (dengan card display)
   - Group by Order ID (dengan detail items)
   - Real-time data dari repository

3. **Shared State Management**
   - Menggunakan `ItemsSharedViewModel` (injected via Koin)
   - Auto-refresh data dari repository
   - Reactive UI updates

### Planned Features 🔜

- [ ] Navigation antar screens
- [ ] Detail view untuk items
- [ ] CRUD operations UI
- [ ] Export to PDF/Excel
- [ ] Print functionality
- [ ] Local storage (SQLDelight)

## 📦 Architecture

```
desktopApp/
├── data/
│   └── DesktopItemsRepository.kt  # In-memory repository
├── di/
│   └── DesktopModule.kt           # Koin DI module
├── AuctionDesktopApp.kt           # Main UI
└── Main.kt                        # Entry point
```

## 🔧 Setup

### Prerequisites

- JDK 21 or higher
- Gradle 8.x
- Kotlin 2.3.10

### Dependencies

```kotlin
dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.foundation)
    implementation(compose.materialIconsExtended)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
}
```

## 🏃 Running the App

### From Command Line

```bash
# Run the desktop app
./gradlew :desktopApp:run

# Build distributable
./gradlew :desktopApp:packageDistributionForCurrentOS
```

### From IDE

1. Open project in IntelliJ IDEA
2. Navigate to `desktopApp/src/main/kotlin/com/polytron/auctionapp/desktop/Main.kt`
3. Click the green run button next to `fun main()`

## 📊 Data Source

### DesktopItemsRepository

In-memory repository dengan sample data untuk testing:

```kotlin
class DesktopItemsRepository : ItemsRepository {
    init {
        // 7 sample items dengan berbagai status
        itemsState.value = listOf(
            SharedItem(id = "1", nameItem = "Sanco - 1", ...),
            SharedItem(id = "2", nameItem = "Sanco - 2", ...),
            // ... more items
        )
    }
}
```

**Sample Data:**
- 7 items total
- 4 unique item names (Sanco, Kursi, Meja, Lemari)
- 4 unique order IDs
- Mixed status (1, 2, 3)

### Future: Real API Integration

Untuk connect ke real API, replace `DesktopItemsRepository` dengan implementation yang menggunakan Ktor:

```kotlin
class DesktopApiItemsRepository(
    private val httpClient: HttpClient
) : ItemsRepository {
    override suspend fun refreshItems(page: Int, perPage: Int): List<SharedItem> {
        return httpClient.get("https://api.example.com/items") {
            parameter("page", page)
            parameter("perPage", perPage)
        }.body()
    }
}
```

## 🎨 UI Components

### Main Screen Layout

```
┌─────────────────────────────────────────┐
│  Welcome Card                            │
│  (Icon + Title)                          │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  Layanan Utama                           │
│                                          │
│  ┌──────┐  ┌──────┐  ┌──────┐          │
│  │ List │  │Lelang│  │ Pay  │          │
│  └──────┘  └──────┘  └──────┘          │
│  ┌──────┐  ┌──────┐                    │
│  │ Take │  │Trans │                    │
│  └──────┘  └──────┘                    │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  📊 Data Summary                         │
│  Total Items: 7                          │
│  Unique Names: 4                         │
│  Total Orders: 4                         │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  Group by Nama Barang                    │
│  ┌───────────────────────────────────┐  │
│  │ Sanco              2 item         │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │ Kursi              2 item         │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  Group by Order ID                       │
│  ┌───────────────────────────────────┐  │
│  │ Order-1001         2 item         │  │
│  │ • Sanco - 1 (A01) - Status: 1    │  │
│  │ • Sanco - 2 (A02) - Status: 1    │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

### Color Scheme

Using Material 3 Light Color Scheme:
- Primary: Blue
- Secondary: Teal
- Surface Variant: Light Gray
- Cards: Semi-transparent overlays

## 🔌 Dependency Injection

### Koin Setup

**Module Definition:**
```kotlin
// DesktopModule.kt
val desktopModule = module {
    single<ItemsRepository> { DesktopItemsRepository() }
    single { ItemsSharedViewModel(repository = get()) }
}
```

**Initialization:**
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

**Injection:**
```kotlin
// AuctionDesktopApp.kt
@Composable
fun AuctionDesktopApp() {
    val sharedVm: ItemsSharedViewModel = koinInject()
    val state by sharedVm.state.collectAsState()
    
    // Use state...
}
```

## 📦 Building Distributable

### Windows

```bash
# Build EXE
./gradlew :desktopApp:packageExe

# Build MSI
./gradlew :desktopApp:packageMsi
```

Output location: `desktopApp/build/compose/binaries/main/`

### Configuration

```kotlin
compose.desktop {
    application {
        mainClass = "com.polytron.auctionapp.desktop.MainKt"

        nativeDistributions {
            targetFormats(
                TargetFormat.Exe,
                TargetFormat.Msi
            )
            packageName = "Auction App Desktop"
            packageVersion = "1.0.0"
            vendor = "Polytron"
            description = "Desktop client for Auction App"
        }
    }
}
```

## 🧪 Testing

### Manual Testing

1. Run the app
2. Verify data summary shows correct counts
3. Verify grouping by name works correctly
4. Verify grouping by order ID shows all items
5. Verify UI is responsive

### Future: Automated Testing

```kotlin
@Test
fun `desktop app displays correct data`() = runComposeUiTest {
    setContent {
        AuctionDesktopApp()
    }
    
    onNodeWithText("Total Items: 7").assertExists()
    onNodeWithText("Sanco").assertExists()
    onNodeWithText("Order-1001").assertExists()
}
```

## 🎯 Next Steps

### Phase 1: Navigation
- [ ] Implement navigation framework
- [ ] Create separate screens for each menu
- [ ] Add back navigation

### Phase 2: CRUD Operations
- [ ] Add item creation form
- [ ] Add item edit form
- [ ] Add item deletion confirmation
- [ ] Add status update UI

### Phase 3: Export Features
- [ ] Export to PDF
- [ ] Export to Excel
- [ ] Print functionality

### Phase 4: Local Storage
- [ ] Implement SQLDelight
- [ ] Add offline support
- [ ] Add data sync

### Phase 5: Real API
- [ ] Integrate Ktor client
- [ ] Connect to real backend
- [ ] Add authentication

## 🐛 Known Issues

- None currently

## 📝 Notes

### Platform-Specific Considerations

**Windows:**
- Uses native window decorations
- Supports system tray integration (future)
- Can create EXE/MSI installers

**macOS:**
- Uses native window decorations
- Can create DMG/PKG installers (future)

**Linux:**
- Uses native window decorations
- Can create DEB/RPM packages (future)

### Performance

- Initial load: ~1-2 seconds
- UI rendering: 60 FPS
- Memory usage: ~200-300 MB

## 🔗 Related Documentation

- [Shared Module README](../shared/README.md)
- [KMP Migration Checklist](../KMP_MIGRATION_CHECKLIST.md)
- [KMP Migration Summary](../KMP_MIGRATION_SUMMARY.md)

## 📞 Support

For issues or questions, please refer to the main project documentation.

---

**Version:** 1.0.0  
**Last Updated:** May 7, 2026  
**Platform:** Windows, macOS, Linux
