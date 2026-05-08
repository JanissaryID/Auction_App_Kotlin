# AuctionApp KMP Migration Guide

Dokumen ini adalah source of truth untuk migrasi AuctionApp dari Android-only menjadi Kotlin Multiplatform (KMP) dengan target Android dan Desktop Windows.

Tujuan utama:

- Android app saat ini dianggap final dan tidak boleh regresi.
- Business logic harus sama persis antara Android dan Desktop.
- Desktop Windows tidak membutuhkan camera dan bluetooth.
- Desktop UI harus desktop-friendly: side navigation di kiri, content di kanan, dan detail/action kompleks memakai custom dialog, bukan pindah halaman.
- Banyak AI/model boleh bekerja paralel, tetapi harus mengikuti aturan dokumen ini agar visi tetap sama dan perubahan AI lain tidak rusak.

Wajib dibaca sebelum AI manapun mengubah kode:

- Bagian "Kondisi Project Saat Ini".
- Bagian "Arsitektur Target".
- Bagian "Aturan Multi-AI".
- Bagian phase yang sesuai dengan scope task.
- Bagian "Definition of Done".

---

## 1. Kondisi Project Saat Ini

Berdasarkan inspeksi project:

- Root project: `AuctionApp`.
- Module aktif di `settings.gradle.kts` saat ini hanya `:app`.
- Folder `shared` dan `desktopApp` sudah ada, tetapi belum di-include sebagai module aktif.
- Source utama masih berada di:
  - `app/src/main/java/com/polytron/auctionapp`
- Android app memakai:
  - Jetpack Compose Android.
  - Koin Android.
  - AndroidX Lifecycle ViewModel.
  - DataStore Android untuk session.
  - Ktor.
  - PocketBase Kotlin library.
  - CameraX + MLKit barcode.
  - Bluetooth Android.
  - Room ada, tetapi belum terlihat dipakai oleh flow utama.
- Business logic penting saat ini berada di:
  - `data/remote/repository/ItemsRepository.kt`
  - `data/remote/repository/ItemsRepositoryImpl.kt`
  - `data/session/SessionManager.kt`
  - `ui/viewmodel/AuthViewModel.kt`
  - `ui/viewmodel/ItemsViewModel.kt`
  - `ui/viewmodel/AuctionViewModel.kt`
- Android-only yang harus dipisahkan:
  - `MainActivity.kt`
  - package `bluetooth`
  - package `view/components/camera`
  - Android `Toast`
  - `android.util.Log`
  - `LocalContext`
  - Android `BackHandler`
  - Android Navigation Compose
  - Android DataStore implementation
  - Android MediaStore export Excel

Kesimpulan:

- Migrasi tidak boleh dilakukan dengan memindahkan semua file mentah ke `shared`.
- Kode harus dipilah menjadi common business logic, Android adapter, Desktop adapter, Android UI, dan Desktop UI.
- Android harus tetap bisa build setelah setiap fase besar.

---

## 2. Prinsip Utama Migrasi

1. Android adalah baseline final.
   Jangan mengubah behavior Android kecuali memang diperlukan untuk memisahkan business logic dan setelah itu wajib diverifikasi.

2. Business logic harus sama.
   Login, restore token, CRUD item, SSE, status item, order ID, payment, take item, transaction grouping, dan format data API harus tetap sama.

3. Desktop bukan copy 1:1 UI mobile.
   Desktop harus memakai layout kerja desktop: side navigation, table/list yang lebih lebar, toolbar, panel detail, dan custom dialog.

4. Camera dan bluetooth tidak dibawa ke desktop.
   Desktop flow barcode diganti input manual atau scanner keyboard-wedge. Printer Bluetooth tidak ditampilkan di desktop.

5. Common code tidak boleh bergantung pada Android.
   `shared/commonMain` dilarang import `android.*`, `androidx.activity.*`, `androidx.navigation.*`, `LocalContext`, `Toast`, `Bluetooth`, `Camera`, dan API Android lain.

6. Platform-specific logic harus jelas.
   Gunakan interface dependency injection atau `expect/actual` hanya untuk hal yang benar-benar platform-specific.

7. Migrasi bertahap.
   Setiap fase harus buildable dan punya checkpoint.

---

## 3. Arsitektur Target

Struktur target:

```text
AuctionApp
├─ app
│  ├─ Android application
│  ├─ Android UI lama atau UI Android hasil rewire
│  ├─ Android-only camera
│  ├─ Android-only bluetooth
│  └─ dependsOn shared
│
├─ shared
│  ├─ KMP module
│  ├─ commonMain
│  │  ├─ pure models
│  │  ├─ repository contracts
│  │  ├─ session manager
│  │  ├─ use cases
│  │  ├─ shared state/viewmodel/presenter
│  │  ├─ validation
│  │  ├─ formatting utils
│  │  └─ platform abstraction interfaces
│  │
│  ├─ androidMain
│  │  ├─ Android preference implementation
│  │  ├─ Android logger implementation
│  │  ├─ Android Excel exporter if needed
│  │  └─ Android repository implementation only if common implementation is impossible
│  │
│  └─ desktopMain
│     ├─ Desktop preference implementation
│     ├─ Desktop logger implementation
│     ├─ Desktop Excel exporter
│     └─ Desktop repository implementation only if common implementation is impossible
│
└─ desktopApp
   ├─ Compose Desktop app
   ├─ Windows entry point
   ├─ Desktop navigation state
   ├─ Side navigation layout
   ├─ Desktop screens
   ├─ Custom dialogs
   └─ dependsOn shared
```

Recommended dependency direction:

```text
desktopApp -> shared
app        -> shared
shared/commonMain -> pure Kotlin/KMP libraries only
shared/androidMain -> Android APIs allowed
shared/desktopMain -> JVM/Desktop APIs allowed
```

Forbidden dependency direction:

```text
shared -> app
shared -> desktopApp
app -> desktopApp
desktopApp -> app
```

---

## 4. Module Setup Target

### 4.1 settings.gradle.kts Target

Target akhir:

```kotlin
include(":app")
include(":shared")
include(":desktopApp")
```

Jangan menghapus `:app`.

### 4.2 Root build.gradle.kts Target

Root harus menyediakan plugin tanpa langsung apply, misalnya:

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.compose.multiplatform) apply false
}
```

Catatan:

- Nama alias bisa disesuaikan dengan `libs.versions.toml`.
- Jangan update versi besar-besaran bersamaan dengan migrasi kecuali compile gagal karena compatibility.

### 4.3 shared build.gradle.kts Target

Target konseptual:

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget()
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.cio)
            implementation(libs.datastore.preferences)
        }

        desktopMain.dependencies {
            implementation(libs.ktor.client.cio)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "com.polytron.auctionapp.shared"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }
}
```

Catatan:

- Jika PocketBase Kotlin library tidak compile di `commonMain`, pindahkan implementasi repository ke platform source set, tetapi contract tetap di common.
- Jangan masukkan Compose UI ke `shared` dulu kecuali memang ingin shared UI. Untuk migration ini, prioritasnya shared business logic.

### 4.4 desktopApp build.gradle.kts Target

Target konseptual:

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(libs.koin.core)
}

compose.desktop {
    application {
        mainClass = "com.polytron.auctionapp.desktop.MainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi
            )
            packageName = "AuctionApp"
            packageVersion = "1.0.0"
        }
    }
}
```

Catatan:

- Desktop target adalah JVM desktop, bukan Kotlin/Native Windows.
- Windows installer dibuat lewat Compose Desktop native distribution.

---

## 5. Data Model dan Business Semantics

### 5.1 Item Status

Jangan ubah arti status tanpa approval.

Status saat ini:

```text
0 = item tersedia untuk lelang
1 = item sudah dilelang, menunggu pembayaran
2 = item sudah dibayar, menunggu pengambilan
3 = item sudah diambil / selesai
```

Flow utama:

```text
Create item -> status default 0
Auction submit -> status 1, buyer dan price terisi
Payment submit -> status 2, orderID dan typePayment terisi
Take item submit -> status 3
```

### 5.2 Field Item Yang Harus Dipertahankan

Field API item harus sama:

```kotlin
data class Item(
    val id: String? = null,
    val orderID: String? = null,
    val admin: String? = null,
    val nameItem: String? = null,
    val buyer: String? = null,
    val price: String? = null,
    val maxPrice: String? = null,
    val codeItem: String? = null,
    val basePrice: String? = null,
    val user: String? = null,
    val status: Int? = null,
    val typePayment: String? = null,
    val created: String? = null,
    val updated: String? = null
)
```

Catatan:

- Nama field `orderID` harus tetap persis, bukan `orderId`, karena API kemungkinan memakai field ini.
- `price`, `basePrice`, dan `maxPrice` saat ini berupa `String`; jangan ubah ke number dalam migration awal.
- Field `id` berasal dari PocketBase record. Model common sebaiknya murni, tidak extend `Record`.

### 5.3 Auth Semantics

Login:

```text
email + password -> token + userId + name + avatar
save login data
loginWithToken(token)
sessionManager.onLoginSuccess(token)
emit success event
```

Restore session:

```text
read saved token once
if token exists:
    loginWithToken(token)
    sessionManager.onLoginSuccess(token)
if failed:
    sessionManager.onSessionExpired()
```

Important:

- Restore token hanya boleh terjadi sekali saat app start.
- Jangan membuat multiple SSE connection karena restore token emit berkali-kali.

Logout:

```text
sessionManager.onLogout()
clear profile state
clear saved login
stop SSE
clear item list in Items state
```

Session expired:

```text
repository detects HTTP 401/403 or token expired message
sessionManager.onSessionExpired()
clear login
emit event "Sesi telah habis. Silakan login kembali."
UI shows snackbar/dialog/toast equivalent
```

### 5.4 Realtime SSE Semantics

Current behavior:

```text
ItemsViewModel observes isLoggedIn
if logged in:
    fetchItems()
    startRealtimeItems()
if logged out:
    clear items
    cancel SSE job

SSE connects to /api/realtime
first event usually contains client id
subscribe to collection "Items"
on event create/update/delete:
    fetchItems()
```

Rules:

- Only one active SSE job per Items state owner.
- Cancel SSE on logout.
- Retry connection after failure.
- Do not subscribe repeatedly with same client ID.
- Do not update local list manually in migration awal unless tested. Current logic refetches after create/update/delete event.

---

## 6. Platform Abstractions

Common code should depend on interfaces. Platform code provides implementations.

### 6.1 Logger

Common:

```kotlin
interface AppLogger {
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun warn(tag: String, message: String)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}
```

Android:

```kotlin
class AndroidLogger : AppLogger {
    override fun debug(tag: String, message: String) = Log.d(tag, message)
    override fun info(tag: String, message: String) = Log.i(tag, message)
    override fun warn(tag: String, message: String) = Log.w(tag, message)
    override fun error(tag: String, message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
    }
}
```

Desktop:

```kotlin
class DesktopLogger : AppLogger {
    override fun debug(tag: String, message: String) = println("D/$tag: $message")
    override fun info(tag: String, message: String) = println("I/$tag: $message")
    override fun warn(tag: String, message: String) = println("W/$tag: $message")
    override fun error(tag: String, message: String, throwable: Throwable?) {
        println("E/$tag: $message")
        throwable?.printStackTrace()
    }
}
```

### 6.2 User Preferences

Common contract:

```kotlin
interface UserPreferencesRepository {
    suspend fun saveLogin(
        email: String,
        password: String,
        token: String,
        idUser: String,
        name: String,
        avatar: String? = null
    )

    suspend fun clearLogin()

    val userEmail: Flow<String?>
    val userPassword: Flow<String?>
    val userToken: Flow<String?>
    val userIdUser: Flow<String?>
    val userName: Flow<String?>
    val userAvatar: Flow<String?>
    val isLoggedIn: Flow<Boolean>
}
```

Android implementation:

- Use existing Android DataStore.
- Keep same keys:
  - `email`
  - `password`
  - `token`
  - `id_user`
  - `user_name`
  - `user_avatar`
  - `is_logged_in`

Desktop implementation:

- Use file storage under user app data directory.
- Recommended Windows path:
  - `%APPDATA%/AuctionApp/session.json`
- Alternative cross-platform JVM path:
  - `${user.home}/.auctionapp/session.json`
- Keep storage simple for migration awal.
- Do not block UI thread while reading/writing file.

### 6.3 Excel Exporter

Common contract:

```kotlin
interface ExcelExporter {
    suspend fun exportTransactions(items: List<Item>): ExportResult
}

sealed interface ExportResult {
    data class Success(val path: String) : ExportResult
    data class Failure(val message: String, val cause: Throwable? = null) : ExportResult
}
```

Android:

- Use existing MediaStore implementation.
- UI shows Toast/Snackbar based on result.

Desktop:

- Save to Documents or ask user via file chooser.
- Recommended default:
  - `${user.home}/Documents/AuctionApp/transactions-yyyyMMdd-HHmmss.xlsx`
- Use Apache POI if already accepted.

Important:

- Export must not live in common if it imports Android `Context`.
- Desktop export should return file path so UI can show dialog/snackbar.

### 6.4 Barcode Input

Android:

- Camera scan remains Android-only.
- Existing `ScreenScanBarcode` can remain in `app`.

Desktop:

- No camera.
- Use custom dialog:
  - `BarcodeEntryDialog`
  - text input focused automatically
  - user types/scans barcode
  - Enter triggers lookup
  - matched item added to selected list
- Many USB barcode scanners act as keyboard. This approach supports them.

### 6.5 Printer

Android:

- Keep Bluetooth printer logic as Android-only.

Desktop:

- No Bluetooth printer in migration awal.
- Do not show printer buttons on Desktop unless later there is a Windows printer requirement.
- If a shared print action is needed, expose interface:

```kotlin
interface ReceiptPrinter {
    val isAvailable: Boolean
    suspend fun printAuctionLabel(...)
    suspend fun printReceipt(...)
}
```

Desktop implementation can be `NoOpReceiptPrinter`.

---

## 7. Phase-by-Phase Migration Plan

Each phase must have:

- Scope.
- Files allowed.
- Tasks.
- Validation commands.
- Exit criteria.
- Handoff note.

Do not skip phases unless lead developer explicitly approves.

---

## Phase 0 - Baseline Lock and Safety Net

Goal:

- Prove Android baseline builds before KMP migration.
- Create documentation and task boundaries.

Allowed files:

- `KMP_MIGRATION_GUIDE.md`
- Optional `MIGRATION_STATUS.md`
- Optional CI/build script docs only

Tasks:

1. Create migration branch:

```powershell
git checkout -b kmp-desktop-migration
```

2. Check current build:

```powershell
.\gradlew.bat :app:assembleDebug
```

3. Record baseline:

```text
Android build: pass/fail
Commit hash:
Known warnings:
Known manual behavior:
```

4. Do not modify Android behavior in this phase.

Exit criteria:

- Android current app build result is known.
- Migration docs exist.
- Team knows Android is final baseline.

Handoff note format:

```text
Phase 0 done.
Android assembleDebug: pass/fail
Docs created/updated:
- KMP_MIGRATION_GUIDE.md
Risks:
- ...
```

---

## Phase 1 - Gradle Module Activation

Goal:

- Make `shared` and `desktopApp` real modules.
- Keep `app` building.

Allowed files:

- `settings.gradle.kts`
- root `build.gradle.kts`
- `gradle/libs.versions.toml`
- `shared/build.gradle.kts`
- `desktopApp/build.gradle.kts`
- minimal placeholder source in `shared`
- minimal placeholder source in `desktopApp`

Forbidden:

- Moving Android source files.
- Editing business logic.
- Editing UI behavior.

Tasks:

1. Add module includes:

```kotlin
include(":app")
include(":shared")
include(":desktopApp")
```

2. Add version catalog aliases if missing:

Required conceptual plugins:

```toml
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
android-library = { id = "com.android.library", version.ref = "agp" }
compose-multiplatform = { id = "org.jetbrains.compose", version = "..." }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
```

Important:

- Pick Compose Multiplatform version compatible with current Kotlin.
- If unsure, use official compatibility docs.
- Do not change Kotlin/AGP versions unless necessary.

3. Create `shared/build.gradle.kts`.

4. Create minimal source:

```text
shared/src/commonMain/kotlin/com/polytron/auctionapp/shared/Platform.kt
```

Example:

```kotlin
package com.polytron.auctionapp.shared

object SharedMarker
```

5. Create `desktopApp/build.gradle.kts`.

6. Create minimal desktop entry:

```text
desktopApp/src/main/kotlin/com/polytron/auctionapp/desktop/Main.kt
```

or if KMP layout:

```text
desktopApp/src/jvmMain/kotlin/com/polytron/auctionapp/desktop/Main.kt
```

7. Desktop placeholder:

```kotlin
package com.polytron.auctionapp.desktop

import androidx.compose.material3.Text
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Auction App") {
        Text("Auction App Desktop")
    }
}
```

Validation commands:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :shared:compileKotlinDesktop
.\gradlew.bat :desktopApp:run
```

Exit criteria:

- Android still builds.
- Shared desktop target compiles.
- Desktop app opens placeholder window.

---

## Phase 2 - Move Pure Models to shared/commonMain

Goal:

- Put API/domain models in common code.
- Remove direct dependency on Android for models.

Allowed files:

- `shared/src/commonMain/kotlin/.../model/*`
- `shared/src/commonMain/kotlin/.../data/remote/model/*`
- Android imports only if needed to point to shared models
- Tests for serialization if added

Forbidden:

- Moving repository implementation yet.
- Changing API field names.
- Changing status behavior.
- Adding Android imports to commonMain.

Models to migrate or recreate:

- `PaymentMethod`
- `TypeScreenBarcode`
- `RealtimeSse`
- `RealtimeEvent`
- `AuthResult`
- `User`
- `ItemResponse` or renamed common `Item`

Recommended model strategy:

1. Create pure common model:

```kotlin
@Serializable
data class Item(
    val id: String? = null,
    val orderID: String? = null,
    val admin: String? = null,
    val nameItem: String? = null,
    val buyer: String? = null,
    val price: String? = null,
    val maxPrice: String? = null,
    val codeItem: String? = null,
    val basePrice: String? = null,
    val user: String? = null,
    val status: Int? = null,
    val typePayment: String? = null,
    val created: String? = null,
    val updated: String? = null
)
```

2. Decide naming:

- Safer short term: keep name `ItemResponse` to reduce Android changes.
- Cleaner long term: rename to `Item`, but more files must change.

Recommendation for migration awal:

- Keep `ItemResponse` in shared common.
- Remove inheritance from PocketBase `Record`.
- Include `id` directly as nullable field.

3. User model:

```kotlin
@Serializable
data class User(
    val id: String? = null,
    val email: String,
    val name: String,
    val avatar: String? = null
)
```

Important:

- Existing `User : BaseModel()` gets `id` from parent. Common model should declare `id` explicitly.
- Repository mapping must preserve `id`.

Validation:

```powershell
.\gradlew.bat :shared:compileKotlinDesktop
.\gradlew.bat :app:assembleDebug
```

Exit criteria:

- Common models compile in shared.
- Android either still uses old models or has been carefully redirected to shared.
- No commonMain import from PocketBase model base classes.

---

## Phase 3 - Move Pure Utilities to shared/commonMain

Goal:

- Share formatting and pure helpers.

Allowed utilities:

- `formatRupiah`
- `formatCurrencyInput`
- `generateRandomAlphanumeric`

Forbidden utilities:

- `exportItemsToExcel` as-is, because it imports Android APIs.

Tasks:

1. Move or copy pure utility functions to:

```text
shared/src/commonMain/kotlin/com/polytron/auctionapp/utils/
```

2. Check JVM/Android compatibility:

- `NumberFormat`
- `Locale`
- random generator

For Android + Desktop JVM, Java `NumberFormat` and `Locale` are acceptable if common target only includes Android and JVM Desktop. If future iOS is planned, use expect/actual or kotlinx alternatives.

3. Update Android imports if Android should use shared utils.

Validation:

```powershell
.\gradlew.bat :shared:compileKotlinDesktop
.\gradlew.bat :app:assembleDebug
```

Exit criteria:

- Utilities compile in shared.
- Android behavior formatting remains the same.

---

## Phase 4 - Repository Contract in shared/commonMain

Goal:

- Move repository interface to common.
- Prepare shared business logic to depend on interface.

Allowed files:

- `shared/src/commonMain/kotlin/.../data/remote/repository/ItemsRepository.kt`
- model imports
- temporary Android typealiases or adapter if needed

Forbidden:

- Changing endpoint semantics.
- Changing function names unless all call sites are updated in same phase.

Target interface:

```kotlin
interface ItemsRepository {
    suspend fun loginWithEmailPassword(email: String, password: String): AuthResult
    suspend fun loginWithToken(token: String)
    suspend fun getUser(id: String): User

    suspend fun getItems(page: Int = 1, perPage: Int = 500): List<ItemResponse>
    suspend fun createItem(item: ItemResponse): ItemResponse
    suspend fun updateItem(id: String, item: ItemResponse): ItemResponse
    suspend fun deleteItem(id: String)

    suspend fun withRealtimeEvents(onEvent: suspend (RealtimeSse) -> Unit)
    suspend fun subscribeRealtime(clientId: String, collections: List<String>): Boolean
}
```

Rules:

- Return types must be common models.
- Contract must not mention Android.
- Contract must not mention UI state.

Validation:

```powershell
.\gradlew.bat :shared:compileKotlinDesktop
```

Exit criteria:

- Repository contract is in common.
- No Android dependency in contract.

---

## Phase 5 - Repository Implementation Strategy

Goal:

- Provide repository implementation usable by Android and Desktop.

There are two acceptable strategies.

### Strategy A - Common Ktor Implementation

Preferred if PocketBase dependency blocks KMP or feels risky.

Build repository using Ktor directly:

- POST `/api/collections/users/auth-with-password`
- GET `/api/collections/users/records/{id}`
- GET `/api/collections/Items/records?page=1&perPage=500`
- POST `/api/collections/Items/records`
- PATCH `/api/collections/Items/records/{id}`
- DELETE `/api/collections/Items/records/{id}`
- GET/SSE `/api/realtime`
- POST `/api/realtime` with clientId and subscriptions

Pros:

- Pure common implementation.
- No dependency on PocketBase model base classes.
- Full control over serialization.

Cons:

- More code to write.
- Must verify PocketBase response envelopes exactly.

### Strategy B - Platform Repository Implementations

Use common interface, but implement per platform:

```text
shared/androidMain/.../ItemsRepositoryImpl.kt
shared/desktopMain/.../ItemsRepositoryImpl.kt
```

Pros:

- Easier if current PocketBase library works on both Android and Desktop JVM.
- Less API reimplementation.

Cons:

- Possible duplicate code.
- Desktop may hit library incompatibility.

Recommendation:

- Try Strategy A for long-term stability.
- If time is short, Strategy B is acceptable only if both Android and Desktop compile and behavior is verified.

Mandatory behavior in either strategy:

1. Base host remains:

```text
pb.janissaryid.com
```

2. Protocol remains:

```text
https
```

3. Item collection remains:

```text
Items
```

4. 401/403 must trigger:

```kotlin
sessionManager.onSessionExpired()
```

5. Login token must be stored in repository/client so future calls are authenticated.

6. SSE must keep connection until coroutine is cancelled.

Validation:

```powershell
.\gradlew.bat :shared:compileKotlinDesktop
.\gradlew.bat :app:assembleDebug
```

Manual test:

- Login valid user.
- Login invalid user.
- Fetch items.
- Create item.
- Update item.
- Delete item.
- Start SSE and verify refresh on another client/device.
- Token expiry clears session.

Exit criteria:

- Repository works on Android and Desktop target.
- No duplicate session-expired storms.

---

## Phase 6 - Preferences and Session in shared/commonMain

Goal:

- Move session lifecycle to shared.
- Keep persistence platform-specific.

Allowed files:

- `shared/commonMain/.../UserPreferencesRepository.kt`
- `shared/commonMain/.../SessionManager.kt`
- `shared/androidMain/.../UserPreferencesRepositoryImpl.kt`
- `shared/desktopMain/.../DesktopUserPreferencesRepository.kt`
- DI modules

Tasks:

1. Move `UserPreferencesRepository` interface to common.

2. Move `SessionManager` to common.

3. Replace `android.util.Log` in `SessionManager` with:

- injected `AppLogger`, or
- no-op logging, or
- simple common logger interface.

4. Keep `AtomicBoolean` only if compile target supports it.

Since Android + JVM Desktop both support `java.util.concurrent.atomic.AtomicBoolean`, this is acceptable for now. If future non-JVM targets are added, replace with common atomic solution or Mutex.

5. Android implementation:

- Reuse current DataStore implementation.
- Ensure Android DataStore extension remains in android source set.

6. Desktop implementation:

- Use simple JSON/properties backed repository.
- Expose Flows using `MutableStateFlow`.
- On init, load file and emit values.
- On save, write file and update flows.
- On clear, delete/empty file and update flows.

Desktop repository behavior:

```text
init:
    read saved file if exists
    set flows

saveLogin:
    write file
    update all flows
    isLoggedIn = true

clearLogin:
    remove file content
    set all user fields null/empty
    isLoggedIn = false
```

Validation:

```powershell
.\gradlew.bat :shared:compileKotlinDesktop
.\gradlew.bat :app:assembleDebug
```

Manual test:

- Android login persists after restart.
- Desktop login persists after restart.
- Logout clears persisted state.
- Session expired clears persisted state.

Exit criteria:

- Session logic shared.
- Persistence works on both platforms.

---

## Phase 7 - Shared State Holders / ViewModels

Goal:

- Share Auth, Items, and Auction state logic.

Current Android classes:

- `AuthViewModel`
- `ItemsViewModel`
- `AuctionViewModel`
- `PrinterViewModel`

Migration decision:

### Option A - KMP ViewModel

Use KMP-compatible ViewModel support and Koin ViewModel.

Pros:

- Similar to current Android architecture.
- Works well with Compose Multiplatform.

Cons:

- Requires correct dependencies and lifecycle setup.

### Option B - Plain State Controller

Use plain class with injected `CoroutineScope`:

```kotlin
class AuthController(
    private val scope: CoroutineScope,
    ...
)
```

Pros:

- Very portable.
- No lifecycle dependency.

Cons:

- UI must own/cancel scope correctly.

Recommendation:

- Use KMP ViewModel if dependencies are stable.
- If compile issues appear, use plain controllers first for Desktop and keep Android ViewModels as thin wrappers.

Mandatory behavior to preserve:

Auth:

- email and password fields are StateFlow.
- profile fields are StateFlow.
- isLoggedIn delegates to SessionManager.
- restore token only once.
- login validates non-blank email/password.
- login saves email/password/token/id/name/avatar.
- logout clears session and local profile.
- session expired clears local profile and emits event.

Items:

- items list is StateFlow.
- loading state is StateFlow.
- fetchItems loads page 1 perPage 500.
- create/update/delete call repository.
- isLoggedIn true triggers fetch + SSE.
- isLoggedIn false clears data + cancels SSE.
- SSE event create/update/delete triggers fetchItems.
- only one SSE job active.

Auction:

- selected items StateFlow.
- editing buyers map.
- editing prices map.
- add avoids duplicate id.
- remove clears editing state for item.
- clear clears selection and editing state.
- update price keeps digits only.

Printer:

- Android-only unless a platform abstraction is added.
- Desktop should not depend on Bluetooth.

Validation:

```powershell
.\gradlew.bat :shared:compileKotlinDesktop
.\gradlew.bat :app:assembleDebug
```

Manual test:

- Login.
- Restore token.
- Logout.
- Fetch items.
- SSE reconnect.
- Select item for auction.
- Edit buyer/price.
- Clear selected item.

Exit criteria:

- Shared state logic works.
- Android can consume shared state logic.
- Desktop can consume shared state logic.

---

## Phase 8 - Android Rewire to shared

Goal:

- Android app uses shared business logic.
- Android UI behavior remains final and unchanged.

Allowed files:

- Android DI module
- Android imports to shared models/viewmodels/controllers
- Android UI call sites only as required by type changes
- Android preference adapter
- Android repository adapter

Forbidden:

- Desktop UI work in this phase.
- Redesign Android screens.
- Removing camera or bluetooth from Android.

Tasks:

1. Add dependency in `app/build.gradle.kts`:

```kotlin
implementation(project(":shared"))
```

2. Update DI:

- Android module provides:
  - `UserPreferencesRepository` implementation from shared androidMain or app adapter
  - `SessionManager`
  - `ItemsRepository`
  - shared ViewModels/controllers
  - Android `BluetoothHelper` remains in app

3. Remove duplicate business classes from app only after Android compiles with shared.

Safer sequence:

- Point imports to shared.
- Compile.
- Delete old duplicate files one group at a time.
- Compile after each group.

4. Keep Android-only packages:

```text
app/.../bluetooth
app/.../view/components/camera
app/.../MainActivity.kt
```

5. Fix UI imports:

- `ItemResponse` package may change.
- `PaymentMethod` package may change.
- `TypeScreenBarcode` package may change.
- ViewModel package may change.

Validation:

```powershell
.\gradlew.bat :app:assembleDebug
```

Manual Android regression checklist:

- App starts.
- Login dialog works.
- Saved token restores.
- Home profile displays.
- Item list loads.
- Add item.
- Edit item.
- Delete item.
- Scan barcode with camera.
- Auction flow.
- Payment flow.
- Bluetooth print still works.
- List payment.
- Take items.
- Transactions.
- Export Excel.
- Logout.
- Session expired.

Exit criteria:

- Android still behaves like final app.
- Shared business logic is source of truth.

---

## Phase 9 - Desktop App Foundation

Goal:

- Create real Compose Desktop app shell.
- Start shared DI and display app based on shared auth/items state.

Allowed files:

- `desktopApp/src/...`
- desktop DI modules
- desktop theme
- desktop navigation state
- desktop platform implementations

Forbidden:

- Android UI changes.
- Bluetooth/camera dependency.

Desktop entry requirements:

```kotlin
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Auction App"
    ) {
        AuctionDesktopApp()
    }
}
```

Window target:

- Initial size: around `1280x800`.
- Minimum size: around `1100x700`.
- Support resize.
- No mobile-only bottom nav.

Desktop app structure:

```text
desktopApp
└─ src/main/kotlin/com/polytron/auctionapp/desktop
   ├─ Main.kt
   ├─ di/DesktopModule.kt
   ├─ theme/DesktopTheme.kt
   ├─ app/AuctionDesktopApp.kt
   ├─ navigation/DesktopDestination.kt
   ├─ navigation/DesktopNavigator.kt
   ├─ layout/DesktopShell.kt
   ├─ layout/SideNavigation.kt
   ├─ components/
   ├─ dialogs/
   └─ screens/
```

Navigation state:

```kotlin
enum class DesktopDestination {
    Dashboard,
    Items,
    Auction,
    Payment,
    Pickup,
    Transactions
}
```

Dialog state:

```kotlin
sealed interface DesktopDialog {
    data object Login : DesktopDialog
    data object Profile : DesktopDialog
    data object AddItem : DesktopDialog
    data class EditItem(val itemId: String) : DesktopDialog
    data class ItemDetail(val itemId: String) : DesktopDialog
    data object SelectAuctionItems : DesktopDialog
    data object SelectPaymentItems : DesktopDialog
    data object BarcodeEntry : DesktopDialog
    data object PaymentMethod : DesktopDialog
    data class TransactionDetail(val orderId: String) : DesktopDialog
    data class ConfirmDelete(val itemIds: List<String>) : DesktopDialog
    data class ConfirmTakeItems(val orderId: String) : DesktopDialog
}
```

Important:

- Dialog changes must not mutate destination unless explicitly needed.
- Closing dialog returns user to same content.

Validation:

```powershell
.\gradlew.bat :desktopApp:run
```

Exit criteria:

- Desktop window opens.
- Koin/shared dependencies start.
- Login state can be observed.
- Side navigation visible.
- No Android imports in desktop code.

---

## Phase 10 - Desktop Visual System

Goal:

- Establish consistent desktop design before building feature screens.

Principles:

- Desktop app is an operational tool, not landing page.
- Dense but readable.
- Side nav on left, content on right.
- Toolbars at top of content area.
- Tables/lists should use available width.
- Dialogs for detail and forms.
- Avoid mobile bottom sheets.
- Avoid floating action button as primary desktop action unless very intentional.

Recommended layout:

```text
┌─────────────────────────────────────────────────────────┐
│ Side Nav │ Content Header                               │
│          ├──────────────────────────────────────────────│
│          │ Toolbar / filters / actions                  │
│          ├──────────────────────────────────────────────│
│          │ Main content table/list/panels               │
│          │                                              │
└─────────────────────────────────────────────────────────┘
```

Side nav width:

```kotlin
260.dp
```

Content padding:

```kotlin
24.dp
```

Card/dialog corner:

```kotlin
8.dp
```

Recommended side nav items:

```text
Dashboard
Barang
Lelang
Pembayaran
Pengambilan
Transaksi
```

Top content header should show:

- Current destination title.
- Login/profile status.
- Refresh action if relevant.
- Search/filter controls in toolbar, not nav.

Theme:

- Use Material 3.
- Avoid one-color palette.
- Keep contrast high.
- Use typography suitable for desktop:
  - Title medium for screen title.
  - Body medium for table.
  - Label medium for metadata.

Exit criteria:

- All desktop screens use same shell.
- No screen has isolated full-page mobile Scaffold if it breaks desktop layout.

---

## Phase 11 - Desktop Auth and Profile

Goal:

- Implement login/profile/logout on Desktop.

UI:

- If not logged in:
  - show login button in side nav footer or top right.
  - clicking opens `LoginDialog`.
- If logged in:
  - show user name/avatar placeholder.
  - profile button opens `ProfileDialog`.
  - logout action available in profile dialog.

LoginDialog requirements:

- Email input.
- Password input.
- Submit button.
- Loading state disables submit.
- Enter key may submit when fields valid.
- Error shown inside dialog or snackbar.
- On success:
  - close dialog.
  - remain on current destination.
  - items auto-load through shared state.

ProfileDialog requirements:

- User name.
- Email if available.
- Avatar if easy; otherwise initials placeholder.
- Logout button.
- Logout confirmation optional but recommended.

Validation:

- Start desktop app logged out.
- Login valid.
- Close/reopen app, token restores.
- Logout clears state.
- Invalid login shows error.
- Session expired opens/indicates login needed.

Exit criteria:

- Desktop auth works without Android APIs.

---

## Phase 12 - Desktop Items CRUD

Goal:

- Implement desktop item management.

Destination:

```text
Barang
```

Data:

- Show all items.
- Use shared `items` StateFlow.
- Filter/search by:
  - nameItem
  - codeItem
  - buyer
  - orderID

Recommended content:

```text
Header: Barang
Toolbar:
  Search input
  Status filter
  Refresh button
  Add button
  Delete selected button when selected

Main:
  Table/list with columns:
    Select checkbox
    Code
    Name
    Base price
    Max price
    Status
    Buyer
    Auction price
    Order ID
    Actions
```

Item actions:

- Detail -> `ItemDetailDialog`
- Edit -> `AddEditItemDialog`
- Delete -> `ConfirmDeleteDialog`

AddEditItemDialog fields:

- Nama barang.
- Kode barang.
- Harga dasar.
- Harga maksimal.
- Jumlah.
- Auto max price toggle if existing behavior uses it.

Rules:

- Add multiple items must preserve existing behavior:
  - if jumlah > 1, suffix name/code as current Android does.
- Edit item must update:
  - `nameItem`
  - `codeItem`
  - `basePrice`
  - `maxPrice`
- Do not accidentally change:
  - `status`
  - `buyer`
  - `price`
  - `orderID`
  - `typePayment`

Delete:

- Single delete confirmation.
- Multi delete confirmation lists count and maybe names.
- Delete uses `itemsViewModel.deleteItem(id)`.

Validation:

- Add item status 0.
- Add multiple items.
- Edit item keeps status.
- Delete selected item.
- Search works.
- Status filter works.
- SSE refresh updates desktop when Android modifies item.

Exit criteria:

- CRUD parity with Android.

---

## Phase 13 - Desktop Auction Flow

Goal:

- Implement auction flow without moving pages for selection/detail.

Destination:

```text
Lelang
```

Business:

- Select items with `status == 0`.
- Set buyer and price per selected item.
- Submit updates each item to:

```kotlin
item.copy(
    buyer = finalBuyer,
    price = finalPrice,
    status = 1
)
```

UI:

```text
Header: Lelang
Toolbar:
  Add item button -> SelectAuctionItemsDialog
  Barcode/manual code button -> BarcodeEntryDialog
  Clear selected button
  Submit auction button

Main:
  Selected item table/editor:
    Code
    Name
    Base price
    Max price
    Buyer input
    Price input
    Remove button
```

Validation rules:

- Submit disabled if no selected items.
- Submit disabled if buyer empty for any selected item.
- Submit disabled if price empty or invalid for any selected item.
- Price input stores digits only.
- Existing buyer/price values can be shown if present.

SelectAuctionItemsDialog:

- Lists items where `status == 0`.
- Search by name/code.
- Multi select.
- Confirm adds to shared Auction state.
- Close returns to Lelang screen.

BarcodeEntryDialog:

- Input code.
- Match against `status == 0` item.
- If found, add item if not already selected.
- If not found, show inline message.
- Enter submits.

After submit success:

- Clear selected items.
- Refresh items or rely on SSE plus optional fetch.
- Show success snackbar/dialog.
- Stay on Lelang destination.

Desktop-specific:

- No print action.
- No Bluetooth dependency.

Validation:

- Select one item and submit auction.
- Select multiple items and submit.
- Duplicate selection prevented.
- Invalid price blocked.
- Item status becomes 1.
- Buyer and price saved.

Exit criteria:

- Auction business parity without camera/bluetooth.

---

## Phase 14 - Desktop Payment Flow

Goal:

- Implement payment flow.

Destination:

```text
Pembayaran
```

Business:

- Select items with `status == 1`.
- Submit payment updates each selected item to:

```kotlin
item.copy(
    status = 2,
    orderID = generatedOrderId,
    typePayment = paymentMethod.label
)
```

Order ID:

```text
Order-${generateRandomAlphanumeric()}
```

Payment methods:

- Cash
- QRIS
- Kredit

UI:

```text
Header: Pembayaran
Toolbar:
  Add item button -> SelectPaymentItemsDialog
  Barcode/manual code button -> BarcodeEntryDialog
  Payment history/detail shortcut optional
  Clear selected
  Pay button

Main:
  Receipt-like selected item table:
    Code
    Name
    Buyer
    Auction price
    Remove
  Total amount summary
```

PaymentMethodDialog:

- Shows total.
- Lets user select Cash/QRIS/Kredit.
- Confirm button.
- Loading state while submit.

After submit:

- Patch all selected items.
- Clear selected items.
- Show transaction detail dialog or success snackbar.
- Stay on Pembayaran destination.

Desktop-specific:

- Do not print Bluetooth receipt.
- Optional future: export receipt PDF or system print, but not in initial migration.

Validation:

- Select items status 1.
- Pay cash.
- Pay QRIS.
- Pay Kredit.
- All selected items get same orderID.
- Status becomes 2.
- typePayment saved.
- Total uses numeric price.

Exit criteria:

- Payment parity except printing.

---

## Phase 15 - Desktop Pickup / Take Items Flow

Goal:

- Implement take item flow.

Destination:

```text
Pengambilan
```

Business:

- Show items with `status == 2`.
- Group by `orderID`.
- Confirm pickup updates every item in order/group:

```kotlin
item.copy(status = 3)
```

UI:

```text
Header: Pengambilan
Toolbar:
  Search buyer/order/code
  Refresh

Main:
  Grouped orders table:
    Order ID
    Buyer(s)
    Item count
    Total
    Payment method
    Action: Detail
    Action: Confirm Pickup
```

ConfirmTakeItemsDialog:

- Show order ID.
- Show item list.
- Show warning that status will become completed.
- Confirm button.

After confirm:

- Patch all items to status 3.
- Show success.
- Stay on Pengambilan destination.

Validation:

- Paid transaction appears.
- Search works.
- Detail dialog works.
- Confirm pickup updates status 3.
- Completed transaction no longer appears in pickup list if filtering status 2.

Exit criteria:

- Take item parity.

---

## Phase 16 - Desktop Transactions

Goal:

- Implement transaction report/detail.

Destination:

```text
Transaksi
```

Business:

- Group items by `orderID`.
- Android currently groups transactions from all items with orderID.
- Preserve total calculations:
  - total base = sum basePrice numeric.
  - total price = sum price numeric.

UI:

```text
Header: Transaksi
Toolbar:
  Search
  Status filter
  Export button
  Refresh

Summary:
  Total base
  Total price
  Margin/profit optional if existing logic supports

Main:
  Transaction group table:
    Order ID
    Buyer(s)
    Item count
    Total base
    Total price
    Payment method
    Status summary
    Detail button
```

TransactionDetailDialog:

- Custom dialog, not page navigation.
- Shows:
  - Order ID.
  - Payment method.
  - Buyer(s).
  - Item rows.
  - Base price.
  - Auction price.
  - Status.
  - Total base.
  - Total payment.
- Scroll internal if list long.
- Close button.

Export:

- Calls shared `ExcelExporter`.
- Desktop writes file to Documents or selected path.
- Show result path.

Validation:

- Transactions grouped correctly.
- Detail opens dialog.
- Dialog closes without navigation change.
- Export creates readable Excel.
- Totals match Android.

Exit criteria:

- Transaction reporting parity.

---

## Phase 17 - Desktop Polish and Keyboard UX

Goal:

- Make app comfortable for desktop users.

Checklist:

- Window resize does not overlap UI.
- Dialogs have max width and max height.
- Tables scroll vertically.
- Long text ellipsizes or wraps intentionally.
- Buttons have clear labels/icons.
- Enter key submits primary dialog forms.
- Escape closes dialogs when safe.
- Search fields are focusable.
- Barcode input auto-focuses.
- Loading state visible during network calls.
- Error state visible and recoverable.
- Empty states are helpful but not marketing-like.

Desktop keyboard recommendations:

```text
Ctrl+F = focus search in current screen
Esc = close top dialog
Enter = submit focused dialog if valid
F5 = refresh items
```

Do not add shortcut text visibly everywhere. Tooltips are acceptable.

Validation:

- Test at 1280x800.
- Test at 1366x768.
- Test at 1920x1080.
- Test with long item names.
- Test with long order IDs.
- Test with many selected items.

Exit criteria:

- Desktop feels like an actual Windows operational app, not stretched mobile UI.

---

## Phase 18 - Packaging Windows

Goal:

- Produce Windows runnable package.

Tasks:

1. Verify desktop run:

```powershell
.\gradlew.bat :desktopApp:run
```

2. Package installer:

```powershell
.\gradlew.bat :desktopApp:packageMsi
```

3. Package executable:

```powershell
.\gradlew.bat :desktopApp:packageExe
```

4. Verify output path, usually:

```text
desktopApp/build/compose/binaries/main/msi/
desktopApp/build/compose/binaries/main/exe/
```

5. Install on clean Windows environment if possible.

Validation:

- App launches from installed shortcut.
- Login works.
- Session file writes to expected location.
- Network works.
- Export writes file.
- Uninstall works.

Exit criteria:

- Windows app package is usable.

---

## Phase 19 - Final Regression Matrix

Run this before considering migration complete.

Build:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :shared:compileKotlinDesktop
.\gradlew.bat :desktopApp:run
.\gradlew.bat :desktopApp:packageMsi
```

Android manual:

```text
[ ] App starts
[ ] Login works
[ ] Token restore works
[ ] Logout works
[ ] Session expired works
[ ] Item list loads
[ ] Create item
[ ] Create multiple item
[ ] Edit item
[ ] Delete item
[ ] Camera scan works
[ ] Bluetooth printer list works
[ ] Auction with print works
[ ] Payment with print works
[ ] List payment works
[ ] Take item works
[ ] Transaction detail works
[ ] Android export Excel works
```

Desktop manual:

```text
[ ] App starts
[ ] Side navigation works
[ ] Login dialog works
[ ] Token restore works after restart
[ ] Logout works
[ ] Session expired works
[ ] Items load
[ ] SSE refresh works
[ ] Create item
[ ] Create multiple item
[ ] Edit item
[ ] Delete item
[ ] Item detail dialog works
[ ] Auction select dialog works
[ ] Auction barcode/manual code works
[ ] Auction submit updates status 1
[ ] Payment select dialog works
[ ] Payment barcode/manual code works
[ ] Payment method dialog works
[ ] Payment submit updates status 2
[ ] Pickup detail dialog works
[ ] Pickup confirm updates status 3
[ ] Transactions grouping works
[ ] Transaction detail dialog works
[ ] Desktop export Excel works
[ ] Window resize works
[ ] Long text does not break layout
```

Cross-client manual:

```text
[ ] Android create item appears on Desktop through SSE/refetch
[ ] Desktop create item appears on Android through SSE/refetch
[ ] Android auction update appears on Desktop
[ ] Desktop payment update appears on Android
[ ] Logout on one app does not crash the other app
```

---

## 8. Desktop Screen Specification

This section is the UI contract for Desktop. AI working on desktop must follow this.

### 8.1 DesktopShell

Responsibility:

- Own selected destination.
- Display side nav.
- Display current content.
- Display global dialogs.
- Collect global session events.

Pseudo structure:

```kotlin
@Composable
fun DesktopShell(...) {
    Row(Modifier.fillMaxSize()) {
        SideNavigation(...)
        Box(Modifier.weight(1f).fillMaxHeight()) {
            when (destination) {
                Dashboard -> DashboardScreen(...)
                Items -> ItemsScreen(...)
                Auction -> AuctionScreen(...)
                Payment -> PaymentScreen(...)
                Pickup -> PickupScreen(...)
                Transactions -> TransactionsScreen(...)
            }
        }
    }

    DesktopDialogHost(dialogState = dialogState, ...)
}
```

Rules:

- Do not use Android `NavHost`.
- Do not push details as another page.
- Dialog host should be centralized or at least consistent.

### 8.2 SideNavigation

Content:

- App title: `Auction App`.
- Nav items:
  - Dashboard
  - Barang
  - Lelang
  - Pembayaran
  - Pengambilan
  - Transaksi
- Footer:
  - logged out: Login button
  - logged in: user/profile button

Behavior:

- Clicking nav changes destination.
- Does not clear selected auction/payment state unless explicitly confirmed.
- Active item visibly highlighted.

### 8.3 ContentHeader

Each screen should have:

- Title.
- Optional subtitle/meta.
- Main actions in toolbar.
- Refresh if data-driven.

Example:

```text
Barang
Kelola data barang lelang
[Search] [Status filter] [Refresh] [Tambah Barang]
```

Do not add long instructional paragraphs.

### 8.4 Dialog Design

All desktop dialogs should:

- Use custom composable dialog wrapper.
- Have max width.
- Have max height.
- Have title.
- Have content scroll if needed.
- Have footer buttons.
- Close on explicit close.
- Not navigate away from current destination.

Recommended sizes:

```text
Small dialog: 420-520 dp width
Medium dialog: 640-760 dp width
Large detail dialog: 860-1000 dp width
Max height: 80-90% window height
```

Dialog footer:

```text
[Cancel/Close] [Primary Action]
```

Danger action:

- Use red/destructive styling.
- Require confirmation.

---

## 9. Aturan Multi-AI

These rules are mandatory for all AI/model agents.

### 9.1 General Rules

1. Read this document before editing.
2. Run `git status --short` before editing.
3. Read the latest target files before editing.
4. Work only inside assigned scope.
5. Do not revert changes made by other AI/user.
6. Do not run `git reset --hard`.
7. Do not use destructive delete unless explicitly assigned.
8. Do not bulk format entire project.
9. Do not change package names globally without a dedicated phase.
10. Do not update dependency versions casually.
11. Do not edit generated/build folders.
12. Do not touch Android camera/bluetooth unless assigned Android phase.
13. Do not introduce Android APIs into `shared/commonMain`.
14. Do not make desktop depend on `app`.
15. Do not change API field names.
16. Do not change status semantics.
17. Always run relevant build/test after edits.
18. Handoff must include changed files and verification.

### 9.2 Ownership Rules

Only one AI should own a file at a time.

Suggested ownership:

```text
AI-Gradle:
  settings.gradle.kts
  root build.gradle.kts
  gradle/libs.versions.toml
  shared/build.gradle.kts
  desktopApp/build.gradle.kts

AI-Shared-Models:
  shared/src/commonMain/.../model
  shared/src/commonMain/.../data/remote/model

AI-Shared-Repository:
  shared/src/commonMain/.../data/remote/repository
  shared/src/androidMain/... repository implementation
  shared/src/desktopMain/... repository implementation

AI-Shared-Session:
  shared/src/commonMain/.../session
  shared/src/commonMain/.../preferences
  shared/src/androidMain/... preferences
  shared/src/desktopMain/... preferences

AI-Shared-State:
  shared/src/commonMain/.../viewmodel or controller

AI-Android-Rewire:
  app/build.gradle.kts
  app/src/main/.../di
  app/src/main/... Android import fixes

AI-Desktop-Shell:
  desktopApp/src/.../Main.kt
  desktopApp/src/.../layout
  desktopApp/src/.../navigation
  desktopApp/src/.../theme

AI-Desktop-Items:
  desktopApp/src/.../screens/items
  desktopApp/src/.../dialogs item-related

AI-Desktop-Auction:
  desktopApp/src/.../screens/auction
  desktopApp/src/.../dialogs auction/barcode selection

AI-Desktop-Payment:
  desktopApp/src/.../screens/payment
  desktopApp/src/.../dialogs payment

AI-Desktop-Pickup-Transactions:
  desktopApp/src/.../screens/pickup
  desktopApp/src/.../screens/transactions
  desktopApp/src/.../dialogs transaction/pickup detail

AI-QA:
  tests
  manual checklist docs
  bug reports
```

### 9.3 Conflict Rules

If an AI sees changes it did not make:

1. Assume user/another AI made them intentionally.
2. Do not revert.
3. Re-read file.
4. Adapt your change.
5. If impossible, stop and report:

```text
Blocked by concurrent change:
- file:
- expected:
- actual:
- needed decision:
```

### 9.4 Build Failure Rules

If build fails:

1. Identify whether failure is in your scope.
2. If in your scope, fix it.
3. If outside your scope, do not edit outside scope unless explicitly allowed.
4. Report exact task and error summary.
5. Do not hide failed validation.

### 9.5 Handoff Template

Every AI must end with:

```text
Task:
Scope:

Files changed:
- path/to/file.kt
- path/to/build.gradle.kts

What changed:
- ...

Validation run:
- .\gradlew.bat :shared:compileKotlinDesktop -> PASS/FAIL
- .\gradlew.bat :app:assembleDebug -> PASS/FAIL
- .\gradlew.bat :desktopApp:run -> PASS/FAIL/not run

Manual checks:
- ...

Risks / follow-up:
- ...
```

### 9.6 Prompt Template For Any AI

Use this when starting a new AI/model:

```text
You are working on AuctionApp KMP migration.

Read KMP_MIGRATION_GUIDE.md first.

Core rules:
- Android app is final baseline and must not regress.
- Business logic must remain identical between Android and Desktop.
- Desktop Windows does not need camera or bluetooth.
- Desktop UI uses side navigation and custom dialogs, not page navigation for detail.
- Do not add Android-only APIs into shared/commonMain.
- Do not edit outside assigned scope.
- Do not revert changes made by other AI/user.

Assigned phase:
[PHASE NAME]

Assigned scope:
[FILES/MODULES]

Task:
[SPECIFIC TASK]

Before editing:
- Run git status --short.
- Read latest target files.

After editing:
- Run relevant Gradle command.
- Report changed files, validation result, and risks.
```

---

## 10. Forbidden Changes

Never do these unless explicitly approved:

- Delete Android app module.
- Rewrite Android UI during KMP setup.
- Replace PocketBase API semantics without testing.
- Rename `orderID` to `orderId`.
- Change item status values.
- Convert price fields from `String` to numeric during migration awal.
- Add Android imports to `shared/commonMain`.
- Make Desktop import from `app`.
- Bring CameraX/MLKit to Desktop.
- Bring Android Bluetooth to Desktop.
- Replace SSE with polling unless explicitly approved.
- Store token in random temp folder.
- Run destructive git commands.
- Bulk reformat all files.
- Upgrade all dependencies at once.
- Add unrelated architecture framework.

---

## 11. Recommended Test Plan

### 11.1 Unit Tests in shared

Add tests for:

- `formatRupiah`.
- `formatCurrencyInput`.
- `generateRandomAlphanumeric` shape/length.
- Auction selection duplicate prevention.
- Price digit filtering.
- SessionManager:
  - login success.
  - logout.
  - session expired only emits once under duplicate calls.
- Realtime payload parser:
  - create triggers fetch.
  - update triggers fetch.
  - delete triggers fetch.
  - unknown action ignored.
  - invalid JSON ignored.

### 11.2 Fake Repository Tests

Use fake `ItemsRepository`:

- returns list.
- records create/update/delete calls.
- emits fake SSE events.

Test Items state holder:

- logged in triggers fetch.
- logged out clears.
- SSE event triggers fetch.
- create calls repository.
- update calls repository.
- delete calls repository.

### 11.3 Manual Desktop Tests

Desktop must be manually tested because UI workflows are critical.

Minimum manual data:

```text
Item A status 0
Item B status 0
Item C status 1 with buyer/price
Item D status 2 with orderID/typePayment
Item E status 3 completed
```

Use this dataset to verify filters and flows.

---

## 12. Known Risks and Mitigations

### Risk 1 - PocketBase model inheritance blocks KMP

Current:

- `ItemResponse : Record()`
- `User : BaseModel()`

Mitigation:

- Use pure common models.
- Map PocketBase response manually.
- Prefer Ktor direct implementation if library is not KMP-friendly.

### Risk 2 - Duplicate SSE connections

Current code already has guard for token restore once.

Mitigation:

- Keep restore guard.
- Keep only one `sseJob`.
- Cancel on logout.
- Do not collect token DataStore repeatedly to start SSE.

### Risk 3 - Android regression during rewire

Mitigation:

- Rewire one layer at a time.
- Build Android after each group.
- Keep Android-only code in app.
- Do not redesign Android.

### Risk 4 - Desktop UI becomes stretched mobile

Mitigation:

- Build desktop shell first.
- Use table/list and dialogs.
- Avoid bottom sheets/FAB as main pattern.
- Review at 1280x800 and 1920x1080.

### Risk 5 - Multiple AI edit same files

Mitigation:

- Assign ownership.
- Require `git status`.
- Require handoff.
- Avoid broad refactors.

---

## 13. Suggested File Map Target

This is a suggested final map. Exact package names may vary, but dependency direction must stay.

```text
shared/src/commonMain/kotlin/com/polytron/auctionapp/
├─ model/
│  ├─ ItemResponse.kt
│  ├─ PaymentMethod.kt
│  ├─ RealtimeEvent.kt
│  └─ TypeScreenBarcode.kt
├─ data/
│  ├─ preferences/
│  │  └─ UserPreferencesRepository.kt
│  ├─ remote/
│  │  ├─ model/
│  │  │  ├─ AuthResult.kt
│  │  │  ├─ RealtimeSse.kt
│  │  │  └─ User.kt
│  │  └─ repository/
│  │     ├─ ItemsRepository.kt
│  │     └─ ItemsRepositoryImpl.kt
│  └─ export/
│     └─ ExcelExporter.kt
├─ session/
│  └─ SessionManager.kt
├─ logging/
│  └─ AppLogger.kt
├─ ui/
│  └─ state/
│     ├─ AuthViewModel.kt
│     ├─ ItemsViewModel.kt
│     └─ AuctionViewModel.kt
├─ utils/
│  ├─ formatCurrencyInput.kt
│  ├─ formatRupiah.kt
│  └─ generateRandomAlphanumeric.kt
└─ di/
   └─ SharedModule.kt
```

Android platform:

```text
shared/src/androidMain/kotlin/com/polytron/auctionapp/
├─ data/preferences/AndroidUserPreferencesRepository.kt
├─ logging/AndroidLogger.kt
└─ data/export/AndroidExcelExporter.kt
```

Desktop platform:

```text
shared/src/desktopMain/kotlin/com/polytron/auctionapp/
├─ data/preferences/DesktopUserPreferencesRepository.kt
├─ logging/DesktopLogger.kt
└─ data/export/DesktopExcelExporter.kt
```

Desktop app:

```text
desktopApp/src/main/kotlin/com/polytron/auctionapp/desktop/
├─ Main.kt
├─ app/AuctionDesktopApp.kt
├─ di/DesktopModule.kt
├─ layout/DesktopShell.kt
├─ layout/SideNavigation.kt
├─ navigation/DesktopDestination.kt
├─ navigation/DesktopDialog.kt
├─ dialogs/LoginDialog.kt
├─ dialogs/ProfileDialog.kt
├─ dialogs/AddEditItemDialog.kt
├─ dialogs/ItemDetailDialog.kt
├─ dialogs/SelectItemsDialog.kt
├─ dialogs/BarcodeEntryDialog.kt
├─ dialogs/PaymentMethodDialog.kt
├─ dialogs/TransactionDetailDialog.kt
├─ screens/DashboardScreen.kt
├─ screens/ItemsScreen.kt
├─ screens/AuctionScreen.kt
├─ screens/PaymentScreen.kt
├─ screens/PickupScreen.kt
└─ screens/TransactionsScreen.kt
```

---

## 14. Definition of Done

Migration is complete only when all are true:

- `:app:assembleDebug` passes.
- `:shared:compileKotlinDesktop` passes.
- `:desktopApp:run` works.
- `:desktopApp:packageMsi` works.
- Android manual regression passes.
- Desktop manual regression passes.
- Login works on both platforms.
- Token restore works on both platforms.
- CRUD works on both platforms.
- SSE works on both platforms.
- Auction flow works on both platforms, except Desktop intentionally has no camera/bluetooth.
- Payment flow works on both platforms, except Desktop intentionally has no Bluetooth print.
- Take item flow works on both platforms.
- Transactions and detail dialogs work on Desktop.
- Desktop UI uses side navigation.
- Desktop details/forms use custom dialogs.
- No Android-only import exists in `shared/commonMain`.
- Desktop does not depend on `app`.
- Multi-AI handoff notes are complete.

---

## 15. Official References

Use official references when an AI needs to verify current Gradle/API usage:

- Kotlin Multiplatform Gradle/source sets:
  - https://kotlinlang.org/docs/multiplatform/multiplatform-configure-compilations.html
- Kotlin expect/actual:
  - https://kotlinlang.org/docs/multiplatform/multiplatform-expect-actual.html
- Compose Desktop native distributions:
  - https://kotlinlang.org/docs/multiplatform/compose-native-distribution.html
- Ktor client engines:
  - https://ktor.io/docs/client-engines.html
- Koin ViewModel:
  - https://insert-koin.io/docs/reference/koin-core/viewmodel/
- Room KMP if local cache becomes necessary:
  - https://developer.android.com/kotlin/multiplatform/room

---

## 16. Quick Start For The Next AI

If a new AI starts after this document exists, give this instruction:

```text
Start by reading KMP_MIGRATION_GUIDE.md.
Then run:
  git status --short
  .\gradlew.bat :app:assembleDebug

Do not edit Android behavior unless assigned.
Do not put Android APIs in shared/commonMain.
Follow the phase assigned to you.
End with changed files and validation results.
```

