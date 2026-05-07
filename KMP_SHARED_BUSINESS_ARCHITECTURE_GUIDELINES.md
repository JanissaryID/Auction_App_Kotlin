# KMP Shared Business Architecture Guidelines

Tanggal: 2026-05-07

Dokumen ini menjelaskan pendekatan arsitektur KMP agar Android dan Desktop tidak menulis business logic dua kali. Target utama: business logic satu kali di shared module, UI berbeda per platform, platform adapter berbeda per platform, tetapi model, use case, validation, state, dan workflow tetap sama.

Dokumen terkait:

- `KMP_MODULAR_CLEAN_MVVM_PHASES.md`
- `KMP_MIGRATION_CHECKLIST.md`

## Target Arsitektur

Target akhir:

```text
Android UI
  -> shared presentation ViewModel/state/action
  -> shared domain use case
  -> shared repository interface
  -> Android data adapter/platform services

Desktop UI
  -> shared presentation ViewModel/state/action
  -> shared domain use case
  -> shared repository interface
  -> Desktop data adapter/platform services
```

Business logic berada di `shared/commonMain`.

Platform-specific logic berada di:

- `app/src/main/...` untuk Android.
- `desktopApp/src/main/...` untuk Desktop.

Yang boleh berbeda:

- UI layout.
- Navigation implementation.
- Camera/barcode input.
- Printer implementation.
- Local storage implementation.
- File save/export mechanism.
- Platform permission handling.

Yang tidak boleh berbeda:

- Model bisnis.
- Status lifecycle.
- Validation.
- Payload create/update/delete.
- Selection behavior.
- Auction workflow.
- Payment workflow.
- Pickup workflow.
- Transaction/report calculations.
- SSE/realtime behavior.
- Session expiry behavior.

## Prinsip Utama

1. Shared business first.
   - Business rule masuk `shared/commonMain`.
   - UI hanya mengirim event/action.

2. Platform UI is thin.
   - Android Compose UI dan Desktop Compose UI tidak membuat payload server langsung.
   - UI tidak menentukan status transition langsung.
   - UI tidak generate order id sendiri.

3. Domain layer tidak tahu platform.
   - Domain tidak import Android.
   - Domain tidak import Compose Desktop.
   - Domain tidak import Ktor implementation langsung jika tidak perlu.

4. Data layer mengimplementasikan kontrak domain.
   - Repository interface ada di shared domain.
   - PocketBase implementation bisa di shared data jika multiplatform aman.
   - Jika ada API/library tidak KMP-compatible, buat platform adapter.

5. Presentation shared memakai StateFlow.
   - UI Android dan Desktop collect state yang sama.
   - Intent/action function sama.

6. DI menjadi composition root per platform.
   - Shared menyediakan module common.
   - Android menambahkan module Android.
   - Desktop menambahkan module Desktop.

7. Clean Architecture boundaries wajib dijaga.
   - UI tidak akses network client langsung.
   - Use case tidak akses UI.
   - Repository tidak tahu screen mana yang memanggil.

## Layer Clean Architecture

Gunakan 4 layer logis:

```text
presentation
  ViewModel/Controller, UI state, UI action, effect

domain
  entity/model bisnis, repository interface, use case, validation

data
  repository implementation, remote DTO, mapper, realtime source

platform
  storage, printer, camera/barcode, file system, permission, logging
```

Di KMP, layer ini bisa dimulai sebagai package di `:shared`. Setelah stabil, bisa dipecah menjadi Gradle modules.

## Struktur Package Awal di Module `:shared`

Mulai dari package-based modularization:

```text
shared/src/commonMain/kotlin/com/polytron/auctionapp/shared/
  core/
    common/
      Result.kt
      AppError.kt
      CoroutineDispatchers.kt
    util/
      CurrencyFormatter.kt
      StringNormalizer.kt
      RandomIdGenerator.kt
  domain/
    model/
      AuctionItem.kt
      ItemStatus.kt
      PaymentMethod.kt
      UserSession.kt
      AuthUser.kt
      RealtimeEvent.kt
    repository/
      AuthRepository.kt
      ItemsRepository.kt
      SessionRepository.kt
      RealtimeRepository.kt
      PrinterRepository.kt
      ReportRepository.kt
    usecase/
      auth/
      items/
      auction/
      payment/
      pickup/
      transactions/
    validation/
      ItemValidation.kt
      AuctionValidation.kt
      PaymentValidation.kt
  data/
    remote/
      dto/
      mapper/
      PocketBaseItemsRepository.kt
      PocketBaseAuthRepository.kt
      PocketBaseRealtimeRepository.kt
    session/
      SessionRepositoryImpl.kt
  presentation/
    auth/
      AuthState.kt
      AuthAction.kt
      AuthViewModel.kt
    items/
      ItemsState.kt
      ItemsAction.kt
      ItemsViewModel.kt
    selection/
      SelectionState.kt
      SelectionViewModel.kt
    auction/
      AuctionState.kt
      AuctionAction.kt
      AuctionViewModel.kt
    payment/
      PaymentState.kt
      PaymentAction.kt
      PaymentViewModel.kt
    pickup/
      PickupState.kt
      PickupAction.kt
      PickupViewModel.kt
    transactions/
      TransactionsState.kt
      TransactionsAction.kt
      TransactionsViewModel.kt
  di/
    SharedModule.kt
```

Platform packages:

```text
app/src/main/java/com/polytron/auctionapp/
  platform/
    AndroidSessionStorage.kt
    AndroidPrinterRepository.kt
    AndroidBarcodeScanner.kt
    AndroidReportSaver.kt
  di/
    AndroidModule.kt
  ui/
    screens/
    components/

desktopApp/src/main/kotlin/com/polytron/auctionapp/desktop/
  platform/
    DesktopSessionStorage.kt
    DesktopPrinterRepository.kt
    DesktopBarcodeInput.kt
    DesktopReportSaver.kt
  di/
    DesktopModule.kt
  screens/
  components/
```

## Future Multi-Module Struktur

Jika package modular sudah stabil, bisa dinaikkan menjadi multi-module:

```text
:shared:core
:shared:domain
:shared:data
:shared:presentation
:shared:di
:app
:desktopApp
```

Aturan dependency:

```text
presentation -> domain
data -> domain
di -> presentation + data + domain
app -> shared modules
desktopApp -> shared modules
```

Domain tidak boleh depend ke data atau presentation.

## Domain Layer Guidelines

Domain layer berisi:

- Model bisnis.
- Repository interface.
- Use case.
- Validation.
- Business result/error.

Domain tidak boleh berisi:

- Compose UI.
- Android Context.
- Java Preferences.
- Room DAO.
- Ktor client detail.
- PocketBase SDK detail.
- println/log Android.

### Domain Model

Gunakan model lengkap:

```kotlin
data class AuctionItem(
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
    val typePayment: String? = null
)
```

Catatan:

- Nama field tetap sama dengan server untuk mengurangi mapper error.
- Jika ingin Kotlin naming lebih rapi, boleh pakai `orderId`, tetapi mapper harus sangat ketat dan test wajib lengkap.

### Status

```kotlin
enum class ItemStatus(val value: Int, val label: String) {
    NOT_AUCTIONED(0, "Belum Ditawar"),
    AUCTIONED(1, "Ditawar"),
    PAID(2, "Dibayar"),
    TAKEN(3, "Diambil")
}
```

Status transition:

```text
create -> status default server, expected 0
auction -> 1
payment -> 2
pickup -> 3
```

### Repository Interfaces

Repository interface ada di domain.

Contoh:

```kotlin
interface ItemsRepository {
    suspend fun getItems(page: Int = 1, perPage: Int = 500): List<AuctionItem>
    suspend fun createItem(item: AuctionItem): AuctionItem
    suspend fun updateItem(id: String, item: AuctionItem): AuctionItem
    suspend fun deleteItem(id: String)
}
```

Realtime bisa terpisah:

```kotlin
interface RealtimeRepository {
    suspend fun withRealtimeEvents(onEvent: suspend (RealtimeEvent) -> Unit)
    suspend fun subscribe(clientId: String, collections: List<String>): Boolean
}
```

Session:

```kotlin
interface SessionRepository {
    suspend fun save(session: UserSession)
    suspend fun clear()
    suspend fun getSession(): UserSession?
}
```

Printer:

```kotlin
interface PrinterRepository {
    suspend fun isReady(): Boolean
    suspend fun printItemLabel(item: AuctionItem)
    suspend fun printAuctionSlip(item: AuctionItem)
    suspend fun printPaymentReceipt(orderID: String, payment: String, items: List<AuctionItem>)
}
```

Jika printer belum tersedia di desktop, implementation boleh return not ready. Business flow tetap sama: user bisa save only.

## Use Case Guidelines

Use case adalah satu aksi bisnis yang bermakna.

Penamaan:

```text
CreateItemsUseCase
EditItemUseCase
DeleteItemsUseCase
SaveAuctionUseCase
ProcessPaymentUseCase
GetPaidOrdersUseCase
CompletePickupOrderUseCase
BuildTransactionReportUseCase
StartRealtimeItemsUseCase
```

Aturan:

- Use case menerima input model/value object.
- Use case melakukan validation.
- Use case memanggil repository.
- Use case preserve field yang tidak diubah.
- Use case tidak tahu UI Android/Desktop.

Contoh input:

```kotlin
data class CreateItemsInput(
    val name: String,
    val code: String,
    val basePrice: String,
    val maxPrice: String,
    val quantity: Int,
    val userId: String
)
```

Contoh output:

```kotlin
sealed class UseCaseResult<out T> {
    data class Success<T>(val data: T) : UseCaseResult<T>()
    data class Failure(val error: AppError) : UseCaseResult<Nothing>()
}
```

Atau gunakan Kotlin `Result<T>` jika error mapping sederhana.

## Presentation Layer Guidelines

Presentation shared berisi ViewModel/Controller multiplatform.

State pattern:

```kotlin
data class ItemsState(
    val items: List<AuctionItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val realtimeStatus: RealtimeStatus = RealtimeStatus.Disconnected
)
```

Action pattern:

```kotlin
sealed interface ItemsAction {
    data object Refresh : ItemsAction
    data class SearchChanged(val query: String) : ItemsAction
    data class DeleteSelected(val ids: Set<String>) : ItemsAction
}
```

ViewModel rule:

- Expose `StateFlow<State>`.
- UI calls function/action.
- ViewModel calls use case.
- ViewModel maps domain result into UI state/effect.
- ViewModel does not create server DTO directly.

Effect/event:

```kotlin
sealed interface UiEffect {
    data class ShowMessage(val message: String) : UiEffect
    data object SessionExpired : UiEffect
}
```

## MVVM in KMP

MVVM roles:

```text
View
  Android Compose screen
  Desktop Compose screen

ViewModel
  Shared KMP state holder
  StateFlow
  Calls use cases

Model
  Domain model
  Repository
  Use cases
```

Android can either:

1. Use shared ViewModel directly with Koin singleton/factory.
2. Wrap shared ViewModel inside AndroidX ViewModel if lifecycle integration is needed.

Desktop can use shared ViewModel directly.

Guideline:

- Prefer shared ViewModel for business screens.
- Use platform ViewModel only for platform-specific UI concerns.

## DI Guidelines

Gunakan Koin Multiplatform jika tetap mengikuti pola repo saat ini.

Layer DI:

```text
sharedModule
  use cases
  shared viewmodels/controllers
  repository interfaces bound to implementations that are common-safe

androidModule
  Android session storage
  Android printer
  Android report saver
  Android camera/barcode

desktopModule
  Desktop session storage
  Desktop printer
  Desktop report saver
  Desktop barcode input
```

Shared module contoh:

```kotlin
val sharedModule = module {
    single { CreateItemsUseCase(itemsRepository = get()) }
    single { SaveAuctionUseCase(itemsRepository = get(), printerRepository = get()) }
    single { ProcessPaymentUseCase(itemsRepository = get(), printerRepository = get(), idGenerator = get()) }
    single { ItemsViewModel(refreshItemsUseCase = get(), createItemsUseCase = get(), ...) }
}
```

Android module contoh:

```kotlin
val androidModule = module {
    single<SessionStorage> { AndroidSessionStorage(androidContext()) }
    single<PrinterRepository> { AndroidBluetoothPrinterRepository(...) }
    single<ReportSaver> { AndroidMediaStoreReportSaver(androidContext()) }
}
```

Desktop module contoh:

```kotlin
val desktopModule = module {
    single<SessionStorage> { DesktopPreferencesSessionStorage() }
    single<PrinterRepository> { DesktopPrinterRepository() }
    single<ReportSaver> { DesktopFileReportSaver() }
}
```

DI rules:

- UI boleh inject ViewModel/controller.
- UI jangan inject low-level repository langsung.
- Use case inject repository interface.
- Repository implementation inject API/storage/client.

## Data Layer Guidelines

Data layer bertugas:

- Remote API.
- DTO.
- Mapper.
- Local storage implementation.
- Error mapping.
- Realtime transport.

DTO vs Domain:

- DTO boleh mengikuti PocketBase serialization.
- Domain model dipakai use case.
- Mapper harus test full field.

Jika PocketBase SDK aman untuk KMP:

- Implement remote repository di `shared/data/remote`.

Jika ada dependency platform-specific:

- Buat `expect/actual`.
- Atau buat interface di shared, implementation di Android/Desktop.

Error handling:

- Tangkap 401/403.
- Convert ke `AppError.AuthExpired`.
- Trigger session expiry lewat session controller/use case.
- Jangan biarkan UI parsing string exception.

## Platform Service Guidelines

Platform services adalah hal yang memang berbeda.

Contoh:

- Session storage:
  - Android DataStore.
  - Desktop Preferences/file.
- Printer:
  - Android Bluetooth.
  - Desktop printer/file/no-op sementara.
- Barcode:
  - Android camera.
  - Desktop USB scanner text input.
- Report:
  - Android MediaStore.
  - Desktop file chooser/path.

Semua service ini harus punya interface shared.

## Boundary Rules

UI layer boleh:

- Render state.
- Mengirim action.
- Menampilkan dialog.
- Menampilkan loading/error.

UI layer tidak boleh:

- Generate `orderID`.
- Set status langsung tanpa use case.
- Membuat `ItemResponse` server payload.
- Memanggil PocketBase client.
- Menentukan retry SSE.
- Membersihkan session karena string error.

Use case boleh:

- Validate input.
- Generate payload domain.
- Call repository.
- Preserve field.
- Generate order id melalui injected generator.

Use case tidak boleh:

- Menampilkan snackbar.
- Membuka dialog.
- Memanggil Android Context.
- Memakai Compose.

Repository boleh:

- Serialize/deserialize.
- Call network.
- Map errors.

Repository tidak boleh:

- Memutuskan UI flow.
- Membuat validation UI.

## Naming Guidelines

Gunakan nama yang konsisten:

- `AuctionItem` untuk item domain lengkap.
- `ItemsRepository` untuk CRUD item.
- `AuthRepository` untuk login/token/user.
- `SessionRepository` untuk local session.
- `RealtimeRepository` untuk SSE.
- `PrinterRepository` untuk print actions.
- `ReportRepository` atau `ReportSaver` untuk export.

Use case:

- Verb + noun + `UseCase`.
- Contoh: `ProcessPaymentUseCase`, bukan `PaymentManager`.

ViewModel:

- Screen/feature + `ViewModel`.
- Contoh: `PaymentViewModel`, `AuctionViewModel`.

State:

- Feature + `State`.
- Contoh: `PaymentState`.

Action:

- Feature + `Action`.
- Contoh: `PaymentAction`.

## Testing Guidelines

Test wajib untuk mencegah kerja 2x rusak lagi:

Domain tests:

- status lifecycle.
- create item payload.
- edit item preserve field.
- delete selected ids.
- auction validation.
- payment orderID/typePayment.
- pickup order.
- transaction totals.
- mapper full field.

Presentation tests:

- action updates state.
- loading/error state.
- selection duplicate prevention.
- SSE event refresh trigger.

Repository tests:

- fake server or fake repository.
- safeApiCall maps 401/403 to session expired.

Manual E2E:

- Android and Desktop login together.
- Create on Android appears on Desktop.
- Create on Desktop appears on Android.
- Auction on Android appears on Desktop.
- Payment on Desktop appears on Android.
- Pickup on Android appears on Desktop.

## Migration Strategy

Do not big-bang refactor everything.

Recommended order:

1. Shared model.
2. Shared utilities.
3. Repository contracts.
4. Shared use cases.
5. Shared state/ViewModel.
6. Android migration to shared business.
7. Desktop migration to shared business.
8. UI desktop adaptation.
9. Cleanup old duplicated logic.

Kenapa Android dulu?

- Android adalah source of truth.
- Jika Android tetap jalan setelah memakai shared layer, shared layer benar.
- Setelah itu Desktop tinggal consume shared layer.

## Anti-Patterns Yang Harus Dihindari

- Menyalin `ScreenPayment` Android logic ke `PaymentScreen` Desktop.
- Membuat payload server di composable.
- Membuat enum status Desktop sendiri.
- Membuat `PaymentMethod` Desktop sendiri.
- Memakai model parsial untuk update server.
- Memakai `updateItemStatus` yang membuat payload baru dan membuang field.
- Menangani 401/403 hanya dengan toast tanpa clear session.
- SSE hanya di Android.
- Desktop refresh manual saja tanpa realtime.
- Repository Android dan Desktop punya behavior berbeda.

## Definition of Architecture Done

Arsitektur dianggap sesuai jika:

- Satu model domain lengkap dipakai semua platform.
- Satu set use case dipakai Android dan Desktop.
- Android dan Desktop hanya punya UI/platform adapter masing-masing.
- Repository contract sama.
- Status lifecycle sama.
- Session behavior sama.
- SSE behavior sama.
- Tidak ada business logic besar di UI.
- Test domain membuktikan payload dan workflow sama.
