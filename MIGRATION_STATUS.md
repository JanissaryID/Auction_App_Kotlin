# AuctionApp KMP Migration Status

This file records migration checkpoints so every AI/model starts from the same known state.

## Phase 0 - Baseline Lock and Safety Net

Status: Done

Date: 2026-05-08

Branch:

```text
KMP-New-Point
```

Baseline commit:

```text
25ead64
```

Android baseline build:

```powershell
.\gradlew.bat clean :app:assembleDebug --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 3m 31s
39 actionable tasks: 39 executed
```

Notes:

- Current branch was already `KMP-New-Point`, so no new branch was created.
- Initial `.\gradlew.bat :app:assembleDebug` exceeded the 2-minute tool timeout and was stopped.
- A follow-up non-clean build failed due Kotlin incremental cache/dex output state after the timeout.
- Gradle daemons were stopped with `.\gradlew.bat --stop`.
- Final clean build passed.
- Existing warning observed:
  - `AppDatabase.kt` uses deprecated `fallbackToDestructiveMigration()`.
  - This is a warning only and does not block Phase 0.

Phase 0 exit criteria:

```text
[x] Android baseline build result is known.
[x] Migration guide exists.
[x] Migration status checkpoint exists.
[x] Android app remains the final baseline for future phases.
```

Next phase after Phase 0:

```text
Phase 1 - Gradle Module Activation
```

---

## Phase 1 - Gradle Module Activation

Status: Done

Date: 2026-05-08

Scope completed:

```text
[x] Included :shared in settings.gradle.kts
[x] Included :desktopApp in settings.gradle.kts
[x] Added root plugin aliases for Android library, Kotlin JVM, Kotlin Multiplatform, Kotlin serialization, and Compose Multiplatform
[x] Added shared/build.gradle.kts as KMP module with androidTarget and jvm("desktop")
[x] Added shared common placeholder source
[x] Added desktopApp/build.gradle.kts as Compose Desktop app
[x] Added desktop placeholder Main.kt window
[x] Updated .gitignore so submodule build/bin outputs are ignored
```

Files changed/created:

```text
.gitignore
settings.gradle.kts
build.gradle.kts
gradle/libs.versions.toml
shared/build.gradle.kts
shared/src/commonMain/kotlin/com/polytron/auctionapp/shared/SharedMarker.kt
desktopApp/build.gradle.kts
desktopApp/src/main/kotlin/com/polytron/auctionapp/desktop/Main.kt
MIGRATION_STATUS.md
```

Validation:

```powershell
.\gradlew.bat :shared:compileKotlinDesktop --no-daemon
```

Result:

```text
PASS
```

```powershell
.\gradlew.bat :app:assembleDebug --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 6m 11s
38 actionable tasks: 38 executed
```

```powershell
.\gradlew.bat :desktopApp:compileKotlin --no-daemon
```

Result:

```text
PASS
```

Desktop run smoke:

```powershell
.\gradlew.bat :desktopApp:run --no-daemon --console=plain
```

Result:

```text
PASS smoke
Run process was still active after 45 seconds with no stderr, consistent with a desktop window staying open.
Process tree was stopped intentionally after the smoke check.
```

Notes:

- First `:shared:compileKotlinDesktop` attempt failed because the custom `desktopMain` source set needed explicit Kotlin DSL source-set binding. Fixed in `shared/build.gradle.kts`.
- Android baseline still has the existing Room warning:
  - `fallbackToDestructiveMigration()` is deprecated.
  - Warning only; it does not block the build.
- Desktop Gradle configuration emitted a warning that `compose.material3` notation is deprecated and can later be replaced by an explicit dependency. It does not block Phase 1.

Phase 1 exit criteria:

```text
[x] Android still builds.
[x] Shared desktop target compiles.
[x] Desktop app compiles.
[x] Desktop run smoke starts and remains alive without startup stderr.
```

Next phase:

```text
Phase 1A - Clean Architecture, Koin, and MVVM Scaffold
```

---

## Phase 1A - Clean Architecture, Koin, and MVVM Scaffold

Status: Done

Date: 2026-05-08

Scope completed:

```text
[x] Added common core contracts: AppLogger and AppResult
[x] Added domain skeleton: model marker, repository marker, use case contracts, validation result
[x] Added data skeleton: local/remote data source markers, mapper contract, data repository marker
[x] Added presentation MVVM contracts: UiState, UiAction, UiEffect
[x] Documented ViewModel base decision in source comment
[x] Added common Koin modules: domainModule, dataModule, presentationModule, sharedModules
[x] Added Android platform Koin module placeholder
[x] Added Desktop platform Koin module placeholder
[x] Added desktopApp-only Koin module placeholder
[x] Added Koin core dependency to desktopApp
[x] Removed Phase 1 SharedMarker placeholder because architecture scaffold now provides real common source
```

ViewModel approach decision:

```text
Prefer KMP-compatible ViewModels registered through Koin.
If a later platform constraint blocks that, use plain state holders with explicit CoroutineScope while preserving UiState/UiAction/UiEffect.
```

Files changed/created:

```text
desktopApp/build.gradle.kts
desktopApp/src/main/kotlin/com/polytron/auctionapp/desktop/di/DesktopAppModule.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/core/logging/AppLogger.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/core/result/AppResult.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/data/local/LocalDataSource.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/data/mapper/Mapper.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/data/remote/datasource/RemoteDataSource.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/data/repository/DataRepository.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/di/DataModule.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/di/DomainModule.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/di/PresentationModule.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/di/SharedModules.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/model/DomainModel.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/repository/Repository.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/usecase/UseCase.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/validation/ValidationResult.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/presentation/ViewModelContract.kt
shared/src/androidMain/kotlin/com/polytron/auctionapp/di/AndroidPlatformModule.kt
shared/src/desktopMain/kotlin/com/polytron/auctionapp/di/DesktopPlatformModule.kt
deleted: shared/src/commonMain/kotlin/com/polytron/auctionapp/shared/SharedMarker.kt
MIGRATION_STATUS.md
```

Validation:

```powershell
.\gradlew.bat :shared:compileKotlinDesktop --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 1m
```

```powershell
.\gradlew.bat :shared:compileDebugKotlinAndroid --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 34s
```

```powershell
.\gradlew.bat :app:assembleDebug --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 57s
38 actionable tasks: 38 up-to-date
```

```powershell
.\gradlew.bat :desktopApp:compileKotlin --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 42s
```

Notes:

- No business logic was moved in this phase.
- No Android UI behavior was changed.
- Koin modules are intentionally empty placeholders until repositories, use cases, and ViewModels are migrated in later phases.
- Desktop Gradle configuration still emits the existing warning that `compose.material3` notation is deprecated and can later be replaced by an explicit dependency. It does not block Phase 1A.

Phase 1A exit criteria:

```text
[x] Architecture folders/source contracts exist.
[x] Koin module files exist.
[x] Shared desktop source set compiles.
[x] Shared Android source set compiles.
[x] Android app still builds.
[x] Desktop app still compiles.
[x] Later phases know where to place domain/data/presentation code.
```

Next phase:

```text
Phase 2 - Move Pure Models to shared/commonMain
```

---

## Phase 2 - Move Pure Models to shared/commonMain

Status: Done

Date: 2026-05-08

Scope completed:

```text
[x] Added pure shared AuthResult model
[x] Added pure shared ItemResponse model
[x] Added pure shared PaymentMethod enum
[x] Added pure shared RealtimeEvent model
[x] Added pure shared RealtimeSse model
[x] Added pure shared TypeScreenBarcode enum
[x] Added pure shared User model
[x] Removed PocketBase inheritance from shared models
[x] Kept Android app models untouched for baseline safety
```

Files changed/created:

```text
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/model/AuthResult.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/model/ItemResponse.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/model/PaymentMethod.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/model/RealtimeEvent.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/model/RealtimeSse.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/model/TypeScreenBarcode.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/model/User.kt
MIGRATION_STATUS.md
```

Model notes:

```text
Shared ItemResponse is a pure @Serializable data class.
It does not extend PocketBase Record.
It explicitly includes id, collectionId, collectionName, created, and updated as nullable metadata fields.
Business field names are preserved, including orderID.
Price/basePrice/maxPrice remain String? for migration safety.
Status remains Int? for migration safety.
```

Android rewire status:

```text
Not done in Phase 2.
The Android app still uses its existing app-local models.
Imports will be switched to shared models in Phase 8 after repository/session/state migration is ready.
```

Validation:

```powershell
.\gradlew.bat :shared:compileKotlinDesktop --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 41s
```

```powershell
.\gradlew.bat :shared:compileDebugKotlinAndroid --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 39s
```

```powershell
.\gradlew.bat :app:assembleDebug --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 39s
38 actionable tasks: 38 up-to-date
```

```powershell
.\gradlew.bat :desktopApp:compileKotlin --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 34s
```

Phase 2 exit criteria:

```text
[x] Common models compile in shared.
[x] No shared common model imports PocketBase base classes.
[x] No Android-only API exists in shared/commonMain models.
[x] Android baseline still builds.
[x] Desktop app still compiles.
```

Next phase:

```text
Phase 3 - Move Pure Utilities to shared/commonMain
```

---

## Phase 3 - Move Pure Utilities to shared/commonMain

Status: Done

Date: 2026-05-08

Scope completed:

```text
[x] Added shared formatRupiah utility
[x] Added shared formatCurrencyInput utility
[x] Added shared generateRandomAlphanumeric utility
[x] Rewired Android app to depend on :shared for these utilities
[x] Removed duplicate Android-local pure utility files
[x] Added shared desktop tests for formatting and random ID shape
```

Files changed/created:

```text
app/build.gradle.kts
deleted: app/src/main/java/com/polytron/auctionapp/utils/formatCurrencyInput.kt
deleted: app/src/main/java/com/polytron/auctionapp/utils/formatRupiah.kt
deleted: app/src/main/java/com/polytron/auctionapp/utils/generateRandomAlphanumeric.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/utils/formatCurrencyInput.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/utils/formatRupiah.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/utils/generateRandomAlphanumeric.kt
shared/src/commonTest/kotlin/com/polytron/auctionapp/utils/SharedUtilsTest.kt
MIGRATION_STATUS.md
```

Utility notes:

```text
Shared utilities keep the same public package: com.polytron.auctionapp.utils.
Android imports did not need source-level changes because the package stayed the same.
The app module now depends on :shared, and the old Android-local utility files were removed to avoid duplicate JVM classes.
Formatting is implemented with common Kotlin string grouping instead of java.text.NumberFormat so it remains valid in shared/commonMain.
Observed behavior is preserved for numeric strings, invalid values, digit filtering, and Long overflow handling.
```

Validation:

```powershell
.\gradlew.bat :shared:compileKotlinDesktop --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 1m 37s
```

```powershell
.\gradlew.bat :shared:desktopTest --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 2m 19s
```

```powershell
.\gradlew.bat :app:assembleDebug --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 3m 38s
```

```powershell
.\gradlew.bat :desktopApp:compileKotlin --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 50s
```

Phase 3 exit criteria:

```text
[x] Utilities compile in shared.
[x] Android uses shared utility implementations.
[x] Android formatting behavior remains the same for covered cases.
[x] Android baseline still builds.
[x] Desktop app still compiles.
```

Next phase:

```text
Phase 4 - Repository Contract in shared/commonMain
```

---

## Phase 4 - Repository Contract in shared/commonMain

Status: Done

Date: 2026-05-08

Scope completed:

```text
[x] Added shared ItemsRepository contract
[x] Used pure shared AuthResult, User, ItemResponse, and RealtimeSse models
[x] Preserved current Android repository function names and signatures semantically
[x] Placed contract in domain/repository according to the clean architecture file map
[x] Verified shared/commonMain has no Android, PocketBase, or Java imports
```

Files changed/created:

```text
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/repository/ItemsRepository.kt
MIGRATION_STATUS.md
```

Contract notes:

```text
The contract intentionally lives in com.polytron.auctionapp.domain.repository.
This follows the authoritative clean architecture map in KMP_MIGRATION_GUIDE.md even though the older Phase 4 allowed-files note still mentions data/remote/repository.
Android's existing app-local ItemsRepository was not rewired in this phase; that remains for a later repository/session/state migration.
No endpoint semantics, item status semantics, or API field names were changed.
```

Validation:

```powershell
.\gradlew.bat :shared:compileKotlinDesktop --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 49s
```

```powershell
rg -n "import (android|androidx|io\.github\.agrevster|java)\." shared\src\commonMain
```

Result:

```text
PASS
No matches.
```

```powershell
.\gradlew.bat :app:assembleDebug --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 1m 3s
```

Phase 4 exit criteria:

```text
[x] Repository contract is in common.
[x] Contract uses common models.
[x] No Android dependency exists in contract.
[x] Android baseline still builds.
```

Next phase:

```text
Phase 5 - Repository Implementation Strategy
```

---

## Phase 5 - Repository Implementation Strategy

Status: Done

Date: 2026-05-08

Strategy selected:

```text
Strategy A - Common Ktor Implementation
```

Scope completed:

```text
[x] Added common Ktor ItemsRepositoryImpl
[x] Added PocketBase auth and list response DTOs
[x] Implemented loginWithEmailPassword using /api/collections/users/auth-with-password
[x] Implemented loginWithToken as repository token restore
[x] Implemented getUser using /api/collections/users/records/{id}
[x] Implemented item CRUD using /api/collections/Items/records
[x] Implemented realtime SSE using /api/realtime
[x] Implemented realtime subscription POST using clientId and subscriptions payload
[x] Added SessionExpiryHandler bridge so 401/403 can call shared SessionManager after Phase 6
[x] Added Android and Desktop HttpClient(CIO) providers with SSE plugin installed
```

Files changed/created:

```text
shared/src/commonMain/kotlin/com/polytron/auctionapp/data/remote/dto/PocketBaseAuthResponse.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/data/remote/dto/PocketBaseListResponse.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/data/repository/ItemsRepositoryImpl.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/session/SessionExpiryHandler.kt
shared/src/androidMain/kotlin/com/polytron/auctionapp/di/AndroidPlatformModule.kt
shared/src/desktopMain/kotlin/com/polytron/auctionapp/di/DesktopPlatformModule.kt
MIGRATION_STATUS.md
```

Implementation notes:

```text
Base host remains pb.janissaryid.com.
Protocol remains https.
Item collection remains Items.
The implementation does not use PocketBase Kotlin model inheritance.
The shared repository is not wired into Android app-local DI yet; Android still uses its existing app repository to avoid a behavioral rewire before shared session/preferences are ready.
401/403 call SessionExpiryHandler.onSessionExpired(); Phase 6 should make shared SessionManager implement that handler and wire it through Koin.
Manual API behavior tests were not run because credentials/test data were not provided in this turn.
```

Validation:

```powershell
.\gradlew.bat :shared:compileKotlinDesktop --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 52s
```

```powershell
.\gradlew.bat :shared:compileDebugKotlinAndroid --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 49s
```

```powershell
.\gradlew.bat :app:assembleDebug --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 1m 24s
```

```powershell
.\gradlew.bat :desktopApp:compileKotlin --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 1m 18s
```

```powershell
.\gradlew.bat :shared:desktopTest --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 49s
```

```powershell
rg -n "import (android|androidx|io\.github\.agrevster|java)\." shared\src\commonMain
```

Result:

```text
PASS
No matches.
```

Phase 5 exit criteria:

```text
[x] Repository implementation compiles for Android and Desktop targets.
[x] Shared repository implementation has no Android or PocketBase dependency.
[x] Session expiry hook exists for 401/403.
[ ] Shared repository is wired to shared SessionManager. Blocked until Phase 6 moves session/preferences into shared.
[ ] Manual API behavior test. Needs real credentials/test data.
```

Next phase:

```text
Phase 6 - Preferences and Session in shared/commonMain
```

---

## Phase 6 - Preferences and Session in shared/commonMain

Status: Done

Date: 2026-05-08

Scope completed:

```text
[x] Added common UserPreferencesRepository contract
[x] Added common SessionManager
[x] SessionManager implements SessionExpiryHandler for repository 401/403 handling
[x] Replaced Android Log usage in shared SessionManager with AppLogger
[x] Used Mutex-backed duplicate-expiry guard instead of java AtomicBoolean in commonMain
[x] Added AndroidLogger and DesktopLogger
[x] Added Android DataStore-backed UserPreferencesRepository implementation
[x] Added Desktop properties-backed UserPreferencesRepository implementation
[x] Added Koin wiring for SessionManager, SessionExpiryHandler, and shared ItemsRepository
[x] Added platform Koin providers for AppLogger, UserPreferencesRepository, and HttpClient
[x] Added common tests for SessionManager session start, logout, and duplicate session-expired guard
```

Files changed/created:

```text
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/repository/UserPreferencesRepository.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/session/SessionManager.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/di/DataModule.kt
shared/src/androidMain/kotlin/com/polytron/auctionapp/core/logging/AndroidLogger.kt
shared/src/androidMain/kotlin/com/polytron/auctionapp/data/local/AndroidUserPreferencesRepository.kt
shared/src/androidMain/kotlin/com/polytron/auctionapp/di/AndroidPlatformModule.kt
shared/src/desktopMain/kotlin/com/polytron/auctionapp/core/logging/DesktopLogger.kt
shared/src/desktopMain/kotlin/com/polytron/auctionapp/data/local/DesktopUserPreferencesRepository.kt
shared/src/desktopMain/kotlin/com/polytron/auctionapp/di/DesktopPlatformModule.kt
shared/src/commonTest/kotlin/com/polytron/auctionapp/domain/session/SessionManagerTest.kt
MIGRATION_STATUS.md
```

Implementation notes:

```text
Android shared preferences use DataStore name user_prefs and the same keys as the current Android app-local implementation.
Desktop stores session data in ~/.auctionapp/session.properties.
The Android app still uses app-local preferences/session/repository wiring until a later Android rewire phase; this phase only prepares shared equivalents.
Shared Koin dataModule can now wire ItemsRepositoryImpl to shared SessionManager through SessionExpiryHandler once platform modules are included at app startup.
Manual persistence tests were not run because no Android/Desktop interactive login flow is wired to shared yet.
```

Validation:

```powershell
.\gradlew.bat :shared:compileKotlinDesktop --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 53s
```

```powershell
.\gradlew.bat :shared:compileDebugKotlinAndroid --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 1m 11s
```

```powershell
.\gradlew.bat :shared:desktopTest --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 1m 6s
```

```powershell
.\gradlew.bat :app:assembleDebug --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 1m 28s
```

```powershell
.\gradlew.bat :desktopApp:compileKotlin --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 36s
```

```powershell
rg -n "import (android|androidx|io\.github\.agrevster|java)\." shared\src\commonMain
```

Result:

```text
PASS
No matches.
```

Phase 6 exit criteria:

```text
[x] Session logic exists in shared.
[x] Persistence implementations exist for Android and Desktop.
[x] Shared SessionManager has duplicate session-expired protection.
[x] Shared/commonMain has no Android, PocketBase, or Java imports.
[x] Android baseline still builds.
[x] Desktop app still compiles.
[ ] Manual Android/Desktop persistence verification. Pending later rewire.
```

Next phase:

```text
Phase 7 - Shared State Holders / ViewModels
```

---

## Phase 7 - Shared State Holders / ViewModels

Status: Done

Date: 2026-05-08

Scope completed:

```text
[x] Added shared auth use cases: LoginUseCase, RestoreSessionUseCase, LogoutUseCase
[x] Added shared item use cases: FetchItemsUseCase, CreateItemUseCase, UpdateItemUseCase, DeleteItemUseCase
[x] Added shared realtime use cases: ObserveItemsRealtimeUseCase, SubscribeItemsRealtimeUseCase
[x] Added shared AuthViewModel plain state holder under presentation/auth
[x] Added shared ItemsViewModel plain state holder under presentation/items
[x] Added shared AuctionViewModel plain state holder under presentation/auction
[x] Preserved existing Android Auth field/state semantics
[x] Preserved existing Items fetch/SSE state semantics
[x] Preserved existing Auction selection/editing semantics
[x] Registered use cases in shared domainModule
[x] Registered shared presentation state holders in presentationModule
[x] Added shared tests for Auction duplicate prevention, editing cleanup, and price digit filtering
```

Files changed/created:

```text
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/usecase/auth/LoginUseCase.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/usecase/auth/LogoutUseCase.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/usecase/auth/RestoreSessionUseCase.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/usecase/items/CreateItemUseCase.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/usecase/items/DeleteItemUseCase.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/usecase/items/FetchItemsUseCase.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/usecase/items/UpdateItemUseCase.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/usecase/realtime/ObserveItemsRealtimeUseCase.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/domain/usecase/realtime/SubscribeItemsRealtimeUseCase.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/presentation/auth/AuthViewModel.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/presentation/items/ItemsViewModel.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/presentation/auction/AuctionViewModel.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/di/DomainModule.kt
shared/src/commonMain/kotlin/com/polytron/auctionapp/di/PresentationModule.kt
shared/src/commonTest/kotlin/com/polytron/auctionapp/presentation/auction/AuctionViewModelTest.kt
MIGRATION_STATUS.md
```

Architecture notes:

```text
Shared presentation uses plain Kotlin state holders with an injected CoroutineScope.
Android lifecycle remains in app wrappers; desktop can later provide its own scope.
State holders depend on use cases/interfaces and SessionManager, not concrete repository implementations.
PrinterViewModel remains Android-only.
```

Validation:

```powershell
.\gradlew.bat :shared:compileKotlinDesktop --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 1m 1s
```

```powershell
.\gradlew.bat :shared:desktopTest --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 57s
```

Phase 7 exit criteria:

```text
[x] Shared state logic exists.
[x] Android can consume shared state logic through wrappers.
[x] Desktop can consume shared state logic by providing a CoroutineScope.
[x] Shared state holders are registered through Koin presentationModule.
[x] Shared state holders depend on use cases/interfaces, not concrete repository implementations.
```

Next phase:

```text
Phase 8 - Android Rewire to shared
```

---

## Phase 8 - Android Rewire to shared

Status: Done (build verified; manual regression pending)

Date: 2026-05-08

Scope completed:

```text
[x] Android app already depends on :shared
[x] Android Koin startup now includes sharedModules + androidPlatformModule + appModule
[x] Android appModule no longer wires app-local repository/session/preferences
[x] Android AuthViewModel is now a lifecycle wrapper around shared presentation auth state
[x] Android ItemsViewModel is now a lifecycle wrapper around shared presentation items state
[x] Android AuctionViewModel is now a lifecycle wrapper around shared presentation auction state
[x] Android UI/components now import shared domain models
[x] Android MainActivity injects shared SessionManager
[x] Removed app-local duplicate models
[x] Removed app-local duplicate repository contracts/implementations
[x] Removed app-local duplicate preferences and SessionManager
[x] Removed direct app dependencies on Ktor, DataStore, and PocketBase
[x] Kept Android-only Bluetooth, camera, printer, Room, and Excel export in app
```

Files changed/created:

```text
app/build.gradle.kts
app/src/main/java/com/polytron/auctionapp/MainActivity.kt
app/src/main/java/com/polytron/auctionapp/di/AppModule.kt
app/src/main/java/com/polytron/auctionapp/di/MyApp.kt
app/src/main/java/com/polytron/auctionapp/ui/viewmodel/AuthViewModel.kt
app/src/main/java/com/polytron/auctionapp/ui/viewmodel/ItemsViewModel.kt
app/src/main/java/com/polytron/auctionapp/ui/viewmodel/AuctionViewModel.kt
app/src/main/java/com/polytron/auctionapp/navigation/NavGraph.kt
app/src/main/java/com/polytron/auctionapp/bluetooth/BluetoothPrinter.kt
app/src/main/java/com/polytron/auctionapp/utils/exportItemsToExcel.kt
app/src/main/java/com/polytron/auctionapp/view/components/ReceiptCard.kt
app/src/main/java/com/polytron/auctionapp/view/components/bottomsheet/AddOrEditItemBottomSheet.kt
app/src/main/java/com/polytron/auctionapp/view/components/bottomsheet/ItemDetailBottomSheet.kt
app/src/main/java/com/polytron/auctionapp/view/components/bottomsheet/PaymentBottomSheet.kt
app/src/main/java/com/polytron/auctionapp/view/components/itemcard/*.kt
app/src/main/java/com/polytron/auctionapp/view/screens/ScreenItemList.kt
app/src/main/java/com/polytron/auctionapp/view/screens/ScreenScanBarcode.kt
app/src/main/java/com/polytron/auctionapp/view/screens/ScreenTransactions.kt
deleted: app/src/main/java/com/polytron/auctionapp/model/*.kt
deleted: app/src/main/java/com/polytron/auctionapp/data/remote/model/*.kt
deleted: app/src/main/java/com/polytron/auctionapp/data/remote/repository/*.kt
deleted: app/src/main/java/com/polytron/auctionapp/data/local/repository/*.kt
deleted: app/src/main/java/com/polytron/auctionapp/data/session/SessionManager.kt
MIGRATION_STATUS.md
```

Rewire notes:

```text
Android behavior was not redesigned.
Camera, Bluetooth, printer, Room, and Android Excel export remain app-local.
Android UI still obtains Android lifecycle ViewModels through koinViewModel().
Those Android ViewModels delegate business state/actions to shared presentation state holders.
Search confirmed no app source references remain to app-local model/repository/session/preference packages, PocketBase, Ktor, or DataStore.
```

Validation:

```powershell
.\gradlew.bat :shared:compileKotlinDesktop --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 1m 1s
```

```powershell
.\gradlew.bat :shared:compileDebugKotlinAndroid --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 32s
```

```powershell
.\gradlew.bat :app:assembleDebug --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 2m 24s
```

```powershell
.\gradlew.bat :desktopApp:compileKotlin --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 38s
```

```powershell
.\gradlew.bat :shared:desktopTest --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 57s
```

```powershell
rg -n "import (android|androidx|io\.github\.agrevster|java)\." shared\src\commonMain
```

Result:

```text
PASS
No matches.
```

```powershell
rg -n "com\.polytron\.auctionapp\.model|data\.session|data\.remote\.repository|data\.local\.repository|data\.remote\.model|pocketbase|io\.ktor|datastore" app\src\main\java app\build.gradle.kts
```

Result:

```text
PASS
No matches.
```

Phase 8 exit criteria:

```text
[x] Android builds with shared business logic.
[x] Shared business logic is source of truth for auth/items/auction state and repository/session/preference contracts.
[x] Android-only camera and bluetooth remain in app.
[x] Android UI was not redesigned.
[ ] Manual Android regression checklist. Not run in this turn.
```

Next phase:

```text
Phase 9 - Desktop App Foundation
```

---

## Phase 9 - Desktop App Foundation

Status: Done

Date: 2026-05-08

Scope completed:

```text
[x] Desktop app starts Koin with sharedModules + desktopPlatformModule + desktopAppModule
[x] Desktop window uses 1280x800 initial size and 1100x700 minimum size
[x] Added DesktopTheme
[x] Added DesktopDestination enum
[x] Added DesktopDialog sealed state
[x] Added DesktopNavigator state holder
[x] Added DesktopShell with fixed left side navigation and right content area
[x] Added SideNavigation with Dashboard, Barang, Lelang, Pembayaran, Pengambilan, Transaksi
[x] Desktop root retrieves shared AuthViewModel, ItemsViewModel, and AuctionViewModel from Koin
[x] Desktop observes login state, user name, items, item loading, and auction selected item count
[x] Added centralized DesktopDialogHost
[x] Added basic login/profile dialog plumbing using shared auth state
[x] Added foundation Dashboard, Items, Auction, Payment, Pickup, and Transactions content views
[x] Added desktop runtime SLF4J no-op binding to keep Ktor/logging startup clean
```

Files changed/created:

```text
desktopApp/build.gradle.kts
desktopApp/src/main/kotlin/com/polytron/auctionapp/desktop/Main.kt
desktopApp/src/main/kotlin/com/polytron/auctionapp/desktop/app/AuctionDesktopApp.kt
desktopApp/src/main/kotlin/com/polytron/auctionapp/desktop/theme/DesktopTheme.kt
desktopApp/src/main/kotlin/com/polytron/auctionapp/desktop/navigation/DesktopDestination.kt
desktopApp/src/main/kotlin/com/polytron/auctionapp/desktop/navigation/DesktopDialog.kt
desktopApp/src/main/kotlin/com/polytron/auctionapp/desktop/navigation/DesktopNavigator.kt
desktopApp/src/main/kotlin/com/polytron/auctionapp/desktop/layout/DesktopShell.kt
desktopApp/src/main/kotlin/com/polytron/auctionapp/desktop/layout/SideNavigation.kt
desktopApp/src/main/kotlin/com/polytron/auctionapp/desktop/dialogs/DesktopDialogHost.kt
desktopApp/src/main/kotlin/com/polytron/auctionapp/desktop/screens/DashboardScreen.kt
desktopApp/src/main/kotlin/com/polytron/auctionapp/desktop/screens/ItemsScreen.kt
desktopApp/src/main/kotlin/com/polytron/auctionapp/desktop/screens/WorkflowScreens.kt
gradle/libs.versions.toml
MIGRATION_STATUS.md
```

Implementation notes:

```text
Desktop app does not import Android APIs.
Desktop screens receive shared ViewModels at the app root and pass state/actions downward.
Child screens/dialogs do not call Koin directly.
Dialog state does not mutate destination.
Login/profile dialog plumbing exists as foundation; full Desktop auth validation remains Phase 11.
Feature screens are foundation views only; full CRUD/auction/payment/pickup/transactions remain later phases.
```

Validation:

```powershell
.\gradlew.bat :desktopApp:compileKotlin --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 1m 4s
```

```powershell
.\gradlew.bat :desktopApp:run --no-daemon --console=plain
```

Result:

```text
PASS smoke
Run process was still active after 45 seconds with no stderr, consistent with a desktop window staying open.
Process tree was stopped intentionally after the smoke check.
```

```powershell
.\gradlew.bat :app:assembleDebug --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 1m 1s
```

```powershell
.\gradlew.bat :shared:desktopTest --no-daemon
```

Result:

```text
PASS
BUILD SUCCESSFUL in 1m 21s
```

```powershell
rg -n "android\.|androidx\.activity|androidx\.navigation|LocalContext|Toast|Bluetooth|Camera" desktopApp\src\main\kotlin
```

Result:

```text
PASS
No matches.
```

Phase 9 exit criteria:

```text
[x] Desktop window opens.
[x] Koin/shared dependencies start.
[x] Login state can be observed.
[x] Side navigation visible.
[x] No Android imports in desktop code.
```

Next phase:

```text
Phase 10 - Desktop Visual System
```
