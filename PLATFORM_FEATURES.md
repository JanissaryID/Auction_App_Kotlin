# Platform-Specific Features Strategy

Dokumen ini menjelaskan strategi untuk fitur-fitur yang spesifik per platform dan bagaimana menangani parity antar platform.

## 📱 Android-Only Features

### 1. Bluetooth Printer ✅

**Status:** Implemented  
**Location:** `app/src/main/java/com/polytron/auctionapp/bluetooth/`

**Files:**
- `BluetoothHelper.kt` - Helper untuk Bluetooth operations
- `BluetoothPrinter.kt` - Printer implementation
- `EscPosCommands.kt` - ESC/POS commands untuk thermal printer

**Usage:**
```kotlin
val bluetoothHelper = BluetoothHelper(activity)
val printer = BluetoothPrinter()

bluetoothHelper.requestBluetooth(
    onReady = {
        printer.printBarcodeLabel(device, itemName, itemCode)
    }
)
```

**Desktop Alternative:** ⏳ Planned
- Export to PDF with barcode
- Use system print dialog
- Save as image file

### 2. Camera/Barcode Scanner 🔄

**Status:** Partially Implemented  
**Location:** `app/src/main/java/com/polytron/auctionapp/`

**Dependencies:**
- CameraX for camera access
- ML Kit for barcode scanning

**Usage:**
```kotlin
// Camera permission required
val scanner = BarcodeScanner()
scanner.scan { barcode ->
    // Handle scanned barcode
}
```

**Desktop Alternative:** ⏳ Planned
- Manual input field
- Webcam integration (future)
- Import from file

### 3. Room Local Storage ✅

**Status:** Implemented  
**Location:** `app/src/main/java/com/polytron/auctionapp/data/local/room/`

**Files:**
- `AppDatabase.kt` - Room database
- `ItemDao.kt` - Data access object
- `ItemModel.kt` - Room entity

**Usage:**
```kotlin
val dao = database.itemDao()
dao.insertItem(item)
val items = dao.getAllItems()
```

**Desktop Alternative:** ⏳ Planned
- SQLDelight (KMP-compatible)
- File-based storage (JSON/XML)
- In-memory with persistence

### 4. Android-Specific UI Components

**Status:** In Use  
**Components:**
- Material 3 Android components
- Android-specific navigation
- Android lifecycle integration

**Desktop Alternative:** ✅ Implemented
- Compose Multiplatform components
- Desktop-specific navigation
- Desktop window management

---

## 🖥️ Desktop-Only Features

### 1. Window Management ✅

**Status:** Implemented  
**Location:** `desktopApp/src/main/kotlin/com/polytron/auctionapp/desktop/Main.kt`

**Features:**
- Window title
- Window size
- Close request handling

**Usage:**
```kotlin
Window(
    onCloseRequest = ::exitApplication,
    title = "Auction App Desktop"
) {
    AuctionDesktopApp()
}
```

**Android Alternative:** N/A (Activity-based)

### 2. File System Access 🔄

**Status:** Planned  
**Use Cases:**
- Export data to files
- Import data from files
- Save reports

**Desktop Implementation:**
```kotlin
// File picker
val fileChooser = JFileChooser()
fileChooser.showSaveDialog(null)

// Save file
File("export.xlsx").writeBytes(data)
```

**Android Alternative:**
- Use Storage Access Framework
- Request storage permissions
- Use MediaStore API

### 3. System Tray Integration 🔄

**Status:** Planned  
**Use Cases:**
- Background operation
- Quick access menu
- Notifications

**Desktop Implementation:**
```kotlin
val tray = SystemTray.getSystemTray()
val trayIcon = TrayIcon(image, "Auction App")
tray.add(trayIcon)
```

**Android Alternative:**
- Foreground service
- Notification with actions

---

## 🔄 Cross-Platform Features (Shared)

### 1. Business Logic ✅

**Status:** Implemented  
**Location:** `shared/src/commonMain/kotlin/`

**Features:**
- Item filtering
- Item grouping
- Calculations
- State management

**Implementation:**
- Use cases in shared module
- Platform-agnostic logic
- Testable in isolation

### 2. Data Models ✅

**Status:** Implemented  
**Location:** `shared/src/commonMain/kotlin/.../model/`

**Models:**
- `SharedItem` - Item domain model
- `ItemStatus` - Status enum
- `OrderGroup` - Order grouping
- `ItemGroup` - Item grouping

### 3. Repository Interface ✅

**Status:** Implemented  
**Location:** `shared/src/commonMain/kotlin/.../repository/`

**Interface:**
```kotlin
interface ItemsRepository {
    fun observeItems(): Flow<List<SharedItem>>
    suspend fun refreshItems(): List<SharedItem>
    suspend fun createItem(item: SharedItem): SharedItem
    suspend fun updateItem(id: String, item: SharedItem): SharedItem
    suspend fun deleteItem(id: String)
    suspend fun updateItemStatus(id: String, status: Int)
}
```

**Implementations:**
- Android: `AndroidSharedItemsRepository`
- Desktop: `DesktopItemsRepository`

---

## 🎯 Feature Parity Strategy

### Priority 1: Core Features (Must Have)

| Feature | Android | Desktop | Status |
|---------|---------|---------|--------|
| View Items | ✅ | ✅ | Complete |
| Search/Filter | ✅ | ✅ | Complete |
| Grouping | ✅ | ✅ | Complete |
| CRUD Operations | ✅ | 🔄 | Android done, Desktop UI pending |
| State Management | ✅ | ✅ | Complete |

### Priority 2: Enhanced Features (Should Have)

| Feature | Android | Desktop | Status |
|---------|---------|---------|--------|
| Print Labels | ✅ Bluetooth | 🔄 PDF Export | Android done |
| Barcode Scan | ✅ Camera | 🔄 Manual Input | Android done |
| Local Storage | ✅ Room | 🔄 SQLDelight | Android done |
| Export Data | 🔄 Excel | 🔄 Excel/PDF | Planned |
| Navigation | ✅ | 🔄 | Android done |

### Priority 3: Platform-Specific (Nice to Have)

| Feature | Android | Desktop | Status |
|---------|---------|---------|--------|
| Offline Mode | 🔄 | 🔄 | Planned |
| Real-time Sync | 🔄 | 🔄 | Planned |
| System Tray | N/A | 🔄 | Planned |
| Widgets | 🔄 | N/A | Planned |

**Legend:**
- ✅ Implemented
- 🔄 Planned/In Progress
- ❌ Not Planned
- N/A Not Applicable

---

## 🛠️ Implementation Guidelines

### For Android-Only Features

1. **Keep in Android module**
   - Don't try to force into shared
   - Use platform-specific APIs freely

2. **Provide fallback in Desktop**
   - Document the alternative approach
   - Implement basic version if critical

3. **Example: Bluetooth Printer**
   ```kotlin
   // Android: Use BluetoothPrinter
   BluetoothPrinter().printLabel(device, data)
   
   // Desktop: Export to PDF
   PdfExporter().exportLabel(file, data)
   ```

### For Desktop-Only Features

1. **Keep in Desktop module**
   - Use JVM-specific APIs
   - Leverage desktop capabilities

2. **Provide fallback in Android**
   - Use Android alternatives
   - Adapt to mobile constraints

3. **Example: File System**
   ```kotlin
   // Desktop: Direct file access
   File("export.xlsx").writeBytes(data)
   
   // Android: Use SAF
   contentResolver.openOutputStream(uri).use { 
       it?.write(data) 
   }
   ```

### For Shared Features

1. **Define in shared module**
   - Business logic
   - Data models
   - Use cases

2. **Implement per platform**
   - Repository implementations
   - Platform-specific adapters

3. **Example: Repository**
   ```kotlin
   // Shared: Interface
   interface ItemsRepository {
       suspend fun getItems(): List<SharedItem>
   }
   
   // Android: Implementation
   class AndroidItemsRepository : ItemsRepository {
       override suspend fun getItems() = api.fetchItems()
   }
   
   // Desktop: Implementation
   class DesktopItemsRepository : ItemsRepository {
       override suspend fun getItems() = localDb.getItems()
   }
   ```

---

## 📋 Fallback Strategy

### When Feature Not Available

1. **Show Clear Message**
   ```kotlin
   if (isFeatureAvailable) {
       performAction()
   } else {
       showMessage("Feature not available on this platform")
   }
   ```

2. **Provide Alternative**
   ```kotlin
   if (canUseBluetooth) {
       printViaBluetooth()
   } else {
       exportToPdf()
   }
   ```

3. **Graceful Degradation**
   ```kotlin
   try {
       usePlatformFeature()
   } catch (e: Exception) {
       useBasicFallback()
   }
   ```

---

## 🔮 Future Enhancements

### Phase 1: Desktop Parity (Q2 2026)
- [ ] Desktop CRUD UI
- [ ] PDF export for printing
- [ ] SQLDelight for local storage
- [ ] Basic navigation

### Phase 2: Enhanced Features (Q3 2026)
- [ ] Real-time sync (WebSocket)
- [ ] Offline mode with sync
- [ ] Advanced export (Excel, CSV)
- [ ] Desktop system tray

### Phase 3: iOS Support (Q4 2026)
- [ ] iOS app using shared module
- [ ] iOS-specific features
- [ ] App Store deployment

### Phase 4: Web Support (2027)
- [ ] Web app using shared module
- [ ] Browser-based features
- [ ] PWA support

---

## 📚 References

- [Kotlin Multiplatform - expect/actual](https://kotlinlang.org/docs/multiplatform-connect-to-apis.html)
- [Compose Multiplatform - Platform-specific code](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-platform-specific-code.html)
- [SQLDelight - Multiplatform Database](https://cashapp.github.io/sqldelight/)

---

**Last Updated:** May 7, 2026  
**Version:** 1.0.0
