# KMP Modular Clean MVVM Refactor Phases

Tanggal: 2026-05-07

Dokumen ini adalah phase plan detail untuk refactor menuju KMP modular, DI, Clean Architecture, dan MVVM. Fokusnya adalah menghindari kerja 2x di business logic: Android dan Desktop memakai domain/use case/presentation shared yang sama, sedangkan UI dan platform adapter tetap masing-masing platform.

Dokumen referensi:

- `KMP_SHARED_BUSINESS_ARCHITECTURE_GUIDELINES.md`
- `KMP_MIGRATION_CHECKLIST.md`

## Phase 0: Architecture Baseline

Tujuan:

- Menetapkan aturan arsitektur sebelum memindahkan kode.
- Menentukan batas mana yang shared dan mana yang platform-specific.

Output:

- Daftar package target.
- Daftar interface repository/platform.
- Daftar use case.
- Daftar ViewModel shared.

Langkah:

1. Baca semua dokumen parity yang sudah ada.
2. Tandai business logic yang sekarang berada di Android UI:
   - create item.
   - edit item.
   - delete item.
   - auction save.
   - payment process.
   - pickup order.
   - transaction report.
3. Tandai business logic yang sekarang berada di Android ViewModel:
   - item CRUD.
   - SSE.
   - selection.
   - auth restore.
4. Tandai platform-specific logic:
   - Bluetooth printer.
   - camera barcode.
   - DataStore.
   - MediaStore export.
5. Buat target package di `shared`.
6. Tentukan DI boundaries.
7. Tentukan migration order.

Validasi:

- Tidak ada area abu-abu antara UI, domain, data, platform.

Definition of Done:

- Team sepakat bahwa business logic masuk shared.
- Platform code hanya adapter.

## Phase 1: Prepare Shared Modular Package Structure

Tujuan:

- Menyiapkan struktur modular di `:shared` tanpa langsung memecah Gradle module.

Kenapa package dulu:

- Lebih aman untuk repo yang sudah berjalan.
- Menghindari build refactor terlalu besar di awal.
- Nanti bisa dinaikkan ke multi-module setelah dependency stabil.

Langkah:

1. Buat package:
   - `core/common`
   - `core/util`
   - `domain/model`
   - `domain/repository`
   - `domain/usecase`
   - `domain/validation`
   - `data/remote`
   - `data/mapper`
   - `presentation`
   - `di`
2. Pindahkan utility shared yang sudah ada ke lokasi yang sesuai.
3. Jangan ubah behavior dulu.
4. Pastikan import compile.

Validasi:

- `:shared:compileKotlinJvm` sukses.
- Android compile.
- Desktop compile.

Definition of Done:

- Struktur folder siap untuk domain/data/presentation.

## Phase 2: Domain Model Foundation

Tujuan:

- Mengganti model parsial dengan domain model lengkap.

Langkah:

1. Tambah `AuctionItem`.
2. Tambah `ItemStatus`.
3. Tambah `PaymentMethod`.
4. Tambah `TypeScreenBarcode` jika masih dibutuhkan.
5. Tambah `AuthUser`.
6. Tambah `UserSession`.
7. Tambah `RealtimeEvent`.
8. Tambah value object optional:
   - `OrderId`
   - `Barcode`
   - `MoneyString`
9. Tambah helper extension:
   - parse price.
   - status label.
   - status predicate.
10. Tambah tests mapper/status/helper.

Validasi:

- Semua field server tersedia di `AuctionItem`.
- `status = 0` valid.
- No field loss.

Definition of Done:

- Domain model lengkap menjadi model bisnis utama.

## Phase 3: Shared Utility and Validation Foundation

Tujuan:

- Semua rule input dipakai bersama Android dan Desktop.

Langkah:

1. Tambah `CurrencyFormatter`.
2. Tambah `StringNormalizer`.
3. Tambah `RandomIdGenerator`.
4. Tambah `ItemValidation`.
5. Tambah `AuctionValidation`.
6. Tambah `PaymentValidation`.
7. Tambah `PickupValidation`.
8. Tambah `ReportCalculator`.

Rules wajib:

- name normalize sama dengan Android.
- code normalize sama dengan Android.
- price hanya digit.
- max price auto x3.
- order id format `Order-XXXXXX`.
- payment method label sama.

Validasi:

- Unit test untuk semua validation.

Definition of Done:

- Tidak ada screen Android/Desktop yang perlu punya validation bisnis sendiri.

## Phase 4: Repository Interfaces in Domain

Tujuan:

- Membuat domain tidak bergantung ke implementation platform.

Interfaces:

1. `AuthRepository`
   - login email/password.
   - login token.
   - get user.
2. `ItemsRepository`
   - get items.
   - create.
   - update.
   - delete.
3. `RealtimeRepository`
   - connect events.
   - subscribe.
4. `SessionRepository`
   - save/get/clear session.
5. `PrinterRepository`
   - is ready.
   - print label.
   - print auction slip.
   - print receipt.
6. `ReportSaver`
   - save transaction workbook/report.
7. `BarcodeInputRepository` optional.

Langkah:

1. Buat interfaces di `domain/repository`.
2. Pakai `AuctionItem` domain model.
3. Buat `AppError` dan result type.
4. Pastikan interface tidak import platform.

Validasi:

- Domain compile tanpa Android imports.

Definition of Done:

- Semua use case bisa depend ke interface domain.

## Phase 5: Data Layer Adapters

Tujuan:

- Mengadaptasi repository Android/Desktop ke contract shared.

Langkah Android:

1. Buat mapper `ItemResponse <-> AuctionItem`.
2. Wrap existing `ItemsRepositoryImpl` ke `ItemsRepository`.
3. Wrap auth methods ke `AuthRepository`.
4. Wrap SSE ke `RealtimeRepository`.
5. Wrap DataStore ke `SessionRepository`.
6. Wrap Bluetooth printer ke `PrinterRepository`.
7. Wrap MediaStore export ke `ReportSaver`.

Langkah Desktop:

1. Buat mapper desktop remote `ItemResponse <-> AuctionItem`.
2. Implement PocketBase repository sesuai contract.
3. Tambah safe API error mapping.
4. Implement Preferences session repository.
5. Implement Desktop printer repository minimal:
   - if not ready, return false.
   - save-only tetap bisa jalan.
6. Implement Desktop report saver.
7. Implement realtime repository.

Validasi:

- Mapper full field tests.
- 401/403 maps to auth expired.
- getItems reversed sama.

Definition of Done:

- Android/Desktop data adapter memenuhi contract yang sama.

## Phase 6: Use Case Layer

Tujuan:

- Memindahkan seluruh business action ke shared domain use cases.

Use cases wajib:

Auth:

- `LoginUseCase`
- `RestoreSessionUseCase`
- `LogoutUseCase`
- `HandleSessionExpiredUseCase`

Items:

- `RefreshItemsUseCase`
- `CreateItemsUseCase`
- `EditItemUseCase`
- `DeleteItemsUseCase`

Selection:

- `AddSelectedItemUseCase` optional jika selection di controller.
- `RemoveSelectedItemUseCase` optional.

Auction:

- `ValidateAuctionUseCase`
- `SaveAuctionUseCase`

Payment:

- `ValidatePaymentUseCase`
- `ProcessPaymentUseCase`

Pickup:

- `GetPickupOrdersUseCase`
- `CompletePickupOrderUseCase`

Transactions:

- `GetTransactionGroupsUseCase`
- `BuildTransactionReportUseCase`
- `ExportTransactionReportUseCase`

Realtime:

- `StartItemsRealtimeUseCase`
- `StopItemsRealtimeUseCase` or managed in ViewModel.

Langkah:

1. Implement use case satu per satu.
2. Setiap use case punya unit test.
3. Gunakan fake repository.
4. Jangan akses UI/platform langsung.
5. Inject dependencies lewat constructor.

Validasi:

- Semua use case tests hijau.

Definition of Done:

- Business workflow Android sudah tersedia sebagai shared use cases.

## Phase 7: Shared Presentation MVVM

Tujuan:

- Membuat ViewModel shared agar Android dan Desktop tidak menulis state/action dua kali.

Shared ViewModels:

1. `AuthViewModel`
2. `ItemsViewModel`
3. `SelectionViewModel`
4. `AuctionViewModel`
5. `PaymentViewModel`
6. `PaidOrdersViewModel`
7. `PickupViewModel`
8. `TransactionsViewModel`

State pattern:

- Satu immutable state data class per feature.
- Expose `StateFlow<State>`.
- Expose event/effect flow untuk one-time message.

Action pattern:

- Function direct atau sealed action.
- Contoh:
  - `onSearchChanged(query)`
  - `onRefresh()`
  - `onCreateSubmit(input)`
  - `onPaymentMethodSelected(method)`

Langkah:

1. Buat state class.
2. Buat effect class.
3. Buat ViewModel/controller shared.
4. Inject use cases.
5. Handle loading/error.
6. Handle refresh/SSE state.
7. Handle selection preservation.

Validasi:

- Unit test state transitions.
- Fake use case tests.

Definition of Done:

- UI Android/Desktop cukup observe state dan call action.

## Phase 8: DI with Koin Multiplatform

Tujuan:

- Menyatukan dependency graph agar shared business mudah dipakai Android/Desktop.

Langkah:

1. Buat `sharedModule`.
2. Register:
   - use cases.
   - shared ViewModels/controllers.
   - common utilities.
3. Buat `androidPlatformModule`.
4. Register:
   - Android session repository.
   - Android printer repository.
   - Android report saver.
   - Android barcode/camera service jika perlu.
5. Buat `desktopPlatformModule`.
6. Register:
   - Desktop session repository.
   - Desktop printer repository.
   - Desktop report saver.
   - Desktop barcode input service jika perlu.
7. Android app start:
   - load `sharedModule + androidPlatformModule`.
8. Desktop app start:
   - load `sharedModule + desktopPlatformModule`.
9. Remove duplicate singleton definitions old repository if no longer needed.

Validasi:

- Android Koin starts.
- Desktop Koin starts.
- No missing dependency.

Definition of Done:

- DI graph jelas dan platform bindings terpisah.

## Phase 9: Android Migration to Shared MVVM

Tujuan:

- Membuktikan shared layer benar tanpa mengubah UI Android besar-besaran.

Langkah:

1. `ScreenItemList` pakai shared `ItemsViewModel`/use case.
2. `ScreenAuction` pakai shared selection/auction ViewModel.
3. `ScreenPayment` pakai shared payment ViewModel.
4. `ScreenTakeItems` pakai shared pickup ViewModel.
5. `ScreenTransactions` pakai shared transactions ViewModel.
6. Auth screen/dialog pakai shared auth controller atau wrapper Android ViewModel.
7. Pastikan printer/camera tetap via Android adapter.
8. Pastikan behavior tidak berubah.

Validasi manual:

- Semua flow Android yang lama tetap berjalan.
- Payload server sama.
- SSE tetap jalan.

Definition of Done:

- Android UI memakai shared business layer.
- Android tetap menjadi acuan behavior.

## Phase 10: Desktop Migration to Shared MVVM

Tujuan:

- Menghapus business logic Desktop lama dan memakai shared business layer.

Langkah:

1. Inject shared ViewModel/controller ke desktop screens.
2. Replace `DesktopRemoteItemsRepository` calls through shared repository/use case.
3. Replace status labels to shared status.
4. Replace create/edit/delete UI action to shared ViewModel.
5. Replace auction single item logic with shared selection/auction state.
6. Replace payment mark-paid logic with shared payment state.
7. Replace pickup per-item logic with shared pickup order state.
8. Replace transactions calculation with shared report state.
9. Remove or quarantine old demo repository.

Validasi:

- Desktop build.
- Desktop E2E with server.
- Data parity with Android.

Definition of Done:

- Desktop has no separate business rules.

## Phase 11: Modular UI Adaptation

Tujuan:

- Membangun UI desktop khusus desktop tanpa business duplication.

Langkah:

1. Keep sidebar shell.
2. Build desktop components:
   - screen header.
   - search/filter.
   - status badge.
   - selected panel.
   - order group card.
   - custom dialogs.
3. Every screen consumes shared state.
4. Every button calls shared action.
5. No server payload code in composables.

Validasi:

- Inspect UI files for forbidden direct payload creation.

Definition of Done:

- UI desktop berbeda layout, tapi business action sama.

## Phase 12: Gradle Multi-Module Optional Split

Tujuan:

- Jika package modular sudah stabil, pecah `:shared` menjadi modules untuk scalability.

Kapan dilakukan:

- Setelah business parity tercapai.
- Setelah tests ada.
- Setelah dependency boundaries jelas.

Target modules:

```text
:shared:core
:shared:domain
:shared:data
:shared:presentation
:shared:di
```

Langkah:

1. Buat modules bertahap.
2. Pindahkan package dari `:shared`.
3. Update dependencies:
   - domain depends on core.
   - data depends on domain.
   - presentation depends on domain.
   - di depends on data + presentation.
4. Update Android/Desktop dependencies.
5. Build after each module move.

Validasi:

- No cyclic dependency.
- Android/Desktop build.

Definition of Done:

- Module boundaries enforce Clean Architecture.

## Phase 13: Test Suite and Quality Gate

Tujuan:

- Mencegah regression dan business duplication kembali.

Tests:

1. Domain model tests.
2. Mapper tests.
3. Utility tests.
4. Validation tests.
5. Use case tests.
6. Presentation state tests.
7. Repository error mapping tests.
8. Manual E2E Android/Desktop/SSE.

Quality gate:

- No business logic in UI.
- No duplicate status enum.
- No duplicate payment method enum.
- No model field loss.
- No direct PocketBase calls from UI.
- No update payload from partial model.

Definition of Done:

- Tests and manual checklist pass.

## Phase 14: Cleanup and Documentation

Tujuan:

- Menghapus peninggalan refactor dan mendokumentasikan arsitektur final.

Langkah:

1. Hapus old `SharedItem` jika tidak dipakai.
2. Hapus old mapper yang field-loss.
3. Hapus old Desktop demo repository.
4. Hapus duplicate status labels.
5. Hapus debug prints.
6. Update README/quickstart.
7. Update architecture docs.
8. Update contribution rules:
   - business logic must go shared.
   - platform logic must use interface.
   - UI must not create server payload.

Definition of Done:

- Codebase bersih.
- Developer berikutnya tahu aturan arsitektur.

## Dependency Order Summary

Urutan wajib:

1. Phase 0: Architecture baseline.
2. Phase 1: Shared package structure.
3. Phase 2: Domain model foundation.
4. Phase 3: Utility and validation foundation.
5. Phase 4: Repository interfaces.
6. Phase 5: Data adapters.
7. Phase 6: Use cases.
8. Phase 7: Shared MVVM presentation.
9. Phase 8: DI.
10. Phase 9: Android migration.
11. Phase 10: Desktop migration.
12. Phase 11: Desktop UI adaptation.
13. Phase 12: Optional Gradle multi-module.
14. Phase 13: Tests and quality gate.
15. Phase 14: Cleanup and docs.

## Final Acceptance Criteria

Refactor dianggap sukses jika:

- Android dan Desktop memakai use case yang sama untuk CRUD, auction, payment, pickup, transactions.
- Android dan Desktop memakai status enum yang sama.
- Android dan Desktop memakai model item lengkap yang sama.
- Desktop UI berbeda hanya layout/interaksi, bukan business rule.
- DI memisahkan shared dependencies dan platform dependencies.
- Clean Architecture boundary jelas.
- MVVM shared state/action dipakai oleh kedua platform.
- Tidak ada business logic penting yang hanya ada di Android atau hanya ada di Desktop.
- SSE berjalan di kedua platform.
- Session expired berjalan di kedua platform.
- Manual E2E Android <-> Desktop lulus.
