# AI KMP Refactor Execution Playbook

Tanggal: 2026-05-07

Dokumen ini adalah panduan utama untuk AI agent mana pun yang akan mengerjakan refactor KMP project ini. Tujuannya adalah menjaga satu visi, mencegah kerja 2x di business logic, dan memastikan agent berbeda tidak merusak pekerjaan agent sebelumnya.

Gunakan dokumen ini sebelum menyentuh kode.

## North Star

Project ini sedang diarahkan ke KMP modular, Clean Architecture, MVVM, dan DI.

Target utama:

```text
Business logic ditulis satu kali di shared/commonMain.
Android dan Desktop memakai business logic yang sama.
UI Android dan UI Desktop boleh berbeda sesuai platform.
Platform adapter boleh berbeda.
Business rule tidak boleh berbeda.
```

Android saat ini adalah referensi behavior bisnis. Desktop harus menjadi adaptasi UI desktop atas business logic yang sama, bukan implementasi business logic kedua.

## Dokumen Wajib Dibaca

Sebelum mulai, baca file ini berurutan:

1. `AI_KMP_REFACTOR_EXECUTION_PLAYBOOK.md`
2. `KMP_SHARED_BUSINESS_ARCHITECTURE_GUIDELINES.md`
3. `KMP_MODULAR_CLEAN_MVVM_PHASES.md`
4. `KMP_MIGRATION_CHECKLIST.md`
5. Source Android terkait phase yang dikerjakan.
6. Source Desktop terkait phase yang dikerjakan.

Jika dokumen lama seperti `BUSINESS_LOGIC_REFACTOR_PHASES.md`, `ANDROID_TO_DESKTOP_BUSINESS_PARITY.md`, atau `DESKTOP_UI_ADAPTATION_GUIDELINES.md` masih terbuka di IDE, abaikan. Fokus resmi adalah dokumen KMP dan playbook ini.

## Prinsip Non-Negotiable

- Jangan menulis business logic yang sama dua kali untuk Android dan Desktop.
- Jangan membuat status enum Desktop sendiri.
- Jangan membuat payment method Desktop sendiri.
- Jangan membuat payload server di Composable UI.
- Jangan update server dari model parsial yang membuang field.
- Jangan mengubah behavior Android tanpa alasan eksplisit.
- Jangan menghapus atau revert perubahan agent/user lain tanpa instruksi jelas.
- Jangan melakukan refactor lintas area jika phase saat ini tidak membutuhkannya.
- Jangan mempercantik UI sebelum business layer shared siap.
- Jangan membuat dependency domain ke Android, Desktop, Compose, Room, Preferences, atau Ktor implementation detail.

## Source of Truth

Behavior bisnis:

- Source of truth awal: Android existing behavior.
- Source of truth akhir: shared domain/use cases/presentation.

UI:

- Android UI boleh tetap mobile style.
- Desktop UI harus desktop-friendly:
  - sidebar kiri.
  - content kanan.
  - split panel jika membantu.
  - custom dialog untuk detail/konfirmasi/input.
  - tidak banyak pindah halaman.

Architecture:

- Source of truth: `KMP_SHARED_BUSINESS_ARCHITECTURE_GUIDELINES.md`.

Execution phases:

- Source of truth: `KMP_MODULAR_CLEAN_MVVM_PHASES.md`.

## Current Target Architecture

```text
shared/commonMain
  core
  domain
    model
    repository interfaces
    use cases
    validation
  data
    common-safe implementation if possible
    DTO and mapper
  presentation
    shared MVVM state/action/effect
  di

app/android
  Android Compose UI
  Android platform adapters
  Android DI bindings

desktopApp
  Desktop Compose UI
  Desktop platform adapters
  Desktop DI bindings
```

Allowed platform-specific code:

- Android DataStore vs Desktop Preferences.
- Android Bluetooth printer vs Desktop printer abstraction.
- Android camera scan vs Desktop barcode text input.
- Android MediaStore export vs Desktop file saver.
- Platform permissions.
- Platform UI layout.

Not allowed platform-specific code:

- create item rule.
- edit item rule.
- auction rule.
- payment rule.
- pickup rule.
- transaction totals.
- status lifecycle.
- session expiry behavior.
- SSE refresh behavior.

## Required Business Model

Shared domain model must preserve full server fields:

```text
id
orderID
admin
nameItem
buyer
price
maxPrice
codeItem
basePrice
user
status
typePayment
```

Status lifecycle:

```text
0 = Belum Ditawar
1 = Ditawar
2 = Dibayar
3 = Diambil
```

Payment methods:

```text
Cash
QRIS
Kredit
```

If any code path drops `basePrice`, `maxPrice`, `typePayment`, `user`, `admin`, or `orderID`, treat it as a bug.

## Phase Execution Protocol

Every AI agent must follow this protocol.

### 1. Before Starting

Run/read:

```powershell
git status --short
rg --files -g "*.kt" -g "*.kts" -g "*.md"
```

Then:

- Identify the exact phase from `KMP_MODULAR_CLEAN_MVVM_PHASES.md`.
- Identify the smallest safe scope.
- Identify files likely to be edited.
- Check if those files already have uncommitted changes.
- If files contain changes you did not make, work with them. Do not revert.

### 2. During Work

Rules:

- Keep changes phase-scoped.
- Prefer additive migration over destructive rewrite.
- Maintain Android behavior while moving logic shared.
- Build after meaningful changes.
- Add tests for domain/use case changes when practical.
- Do not edit generated build output.
- Do not delete old code until replacement is compiled and wired.

### 3. After Work

Before final response:

- Run relevant build/test.
- Run `git status --short`.
- Summarize changed files.
- State what phase was advanced.
- State what was verified.
- State remaining risks/gaps.

Required final handoff format:

```text
Phase worked:
Files changed:
Behavior changed:
Verification:
Remaining work:
Notes for next agent:
```

## File Ownership Guidance

Use this to avoid collisions.

### Shared Domain Agent

Owns:

- `shared/src/commonMain/.../domain/`
- `shared/src/commonMain/.../core/`
- domain tests.

Must not edit:

- Android UI screens unless phase explicitly says migration.
- Desktop UI screens unless phase explicitly says migration.

### Shared Data Agent

Owns:

- shared repository contracts.
- DTO/mapper.
- remote repository abstraction.
- error mapping.

Must coordinate with:

- Android platform adapter.
- Desktop platform adapter.

### Shared Presentation Agent

Owns:

- shared ViewModel/controller.
- state/action/effect.
- selection state.

Must not create:

- platform-specific UI layout.

### Android Migration Agent

Owns:

- `app/src/main/java/...`
- Android DI.
- Android platform adapters.

Must preserve:

- existing Android user-visible behavior.
- Bluetooth/camera/MediaStore behavior.

### Desktop Migration Agent

Owns:

- `desktopApp/src/main/kotlin/...`
- Desktop DI.
- Desktop platform adapters.

Must preserve:

- desktop shell/sidebar direction.
- desktop-specific UI adaptation.

### Test Agent

Owns:

- shared unit tests.
- fake repositories.
- mapper tests.
- use case tests.

Must not:

- rewrite production code broadly unless fixing test-discovered bug.

## Safe Migration Strategy

Prefer this pattern:

1. Add shared model/use case.
2. Add mapper from old model to new model.
3. Add tests.
4. Wire Android to shared logic.
5. Verify Android behavior.
6. Wire Desktop to shared logic.
7. Verify Desktop behavior.
8. Remove old duplicate logic.

Do not do this:

1. Delete Android logic first.
2. Rewrite Desktop in parallel.
3. Hope both still match.

The safe path is boring. Boring is good here.

## Phase Gate Checklist

Use this as a gate before moving to the next major phase.

### Gate A: Shared Model Ready

- `AuctionItem` or equivalent full model exists.
- All server fields preserved.
- Status 0/1/2/3 modeled.
- Payment methods modeled.
- Mapper tests pass.

### Gate B: Shared Repository Contracts Ready

- Auth contract exists.
- Items CRUD contract exists.
- Realtime contract exists.
- Session contract exists.
- Printer/report contracts exist if needed by use cases.
- Domain does not import platform code.

### Gate C: Shared Use Cases Ready

- Create items use case.
- Edit item use case.
- Delete items use case.
- Save auction use case.
- Process payment use case.
- Complete pickup order use case.
- Transaction/report use case.
- Tests cover payload and field preservation.

### Gate D: Shared MVVM Ready

- StateFlow-based state exists.
- Actions do not expose platform details.
- Effects/messages are platform neutral.
- Selection state shared.

### Gate E: Android Migration Ready

- Android uses shared business logic.
- Android behavior is unchanged.
- Android build passes.
- Android E2E manual flow passes.

### Gate F: Desktop Migration Ready

- Desktop uses shared business logic.
- Desktop UI does not build server payload manually.
- Desktop business behavior matches Android.
- Desktop build passes.
- Desktop E2E manual flow passes.

## Business Flow Requirements

### Create Items

Must match Android:

- name normalized like Android.
- code normalized like Android.
- basePrice and maxPrice are digit strings.
- auto max price is base x3.
- quantity creates suffix `1..n`.
- payload includes `admin = "admin"`.
- payload includes `user = currentUserId`.
- do not force status `1`.

### Edit Item

Must:

- update name/code/basePrice/maxPrice.
- preserve buyer, price, orderID, admin, user, status, typePayment.

### Auction

Must:

- only select status `0`.
- require buyer for every selected item.
- require price for every selected item.
- update buyer, price, status `1`.
- preserve other fields.

### Payment

Must:

- only select status `1`.
- generate one orderID for the whole batch.
- update every selected item to status `2`.
- set `typePayment`.
- preserve other fields.

### Pickup

Must:

- operate per order group.
- only process status `2`.
- update every item in order to status `3`.
- preserve other fields.

### Transactions

Must:

- group by orderID.
- calculate basePrice, maxPrice, price totals.
- count payment methods.
- show/export full fields.

### SSE

Must:

- start after login.
- subscribe to `Items`.
- refresh on create/update/delete.
- retry after failure.
- stop and clear state on logout.
- trigger session expiry on auth errors.

## Desktop UI Direction

Desktop UI must not be a mobile clone.

Keep:

- sidebar left.
- content right.
- no Home screen.
- default `Daftar Barang`.
- desktop dialogs for details/input.
- split panels for batch flows.

Recommended:

- Lelang: item list left, selected/edit panel right.
- Pembayaran: item list left, receipt panel right.
- Ambil Barang: order groups, confirm dialog.
- Transaksi: grouped table, detail dialog.

UI is allowed to differ.
Business action is not allowed to differ.

## DI Rules

Use DI to keep platform code separated.

Shared DI:

- use cases.
- shared ViewModels/controllers.
- common utilities.

Android DI:

- Android storage.
- Android printer.
- Android report saver.
- Android barcode/camera.
- Android platform repositories.

Desktop DI:

- Desktop storage.
- Desktop printer.
- Desktop report saver.
- Desktop barcode input.
- Desktop platform repositories.

Forbidden:

- UI instantiating repositories manually.
- Use case instantiating platform services manually.
- Domain importing platform DI.

## Testing Requirements

Prioritize tests in this order:

1. Mapper full field preservation.
2. Status lifecycle.
3. Create item payload.
4. Edit preserve field.
5. Auction validation and payload.
6. Payment orderID/typePayment.
7. Pickup per order.
8. Transaction totals/report.
9. Selection duplicate behavior.
10. SSE refresh trigger with fake repository.

Manual E2E before claiming parity:

1. Login Android and Desktop.
2. Create item on Android, verify Desktop updates.
3. Create item on Desktop, verify Android updates.
4. Auction on Android, verify Desktop.
5. Payment on Desktop, verify Android.
6. Pickup on Android, verify Desktop.
7. Export/report sanity check.

## Git and Change Safety

Important:

- The working tree may already have user or previous-agent changes.
- Never run destructive commands like `git reset --hard`.
- Never revert files you did not intentionally change.
- Do not delete generated or source files unless the phase explicitly requires it.
- If a file has unrelated changes, avoid touching it.
- If you must touch a file with unrelated changes, read it first and preserve all existing changes.

Before editing:

```powershell
git status --short
```

After editing:

```powershell
git diff -- <files you changed>
```

## Build and Verification Commands

Use commands appropriate to the phase.

Shared compile:

```powershell
.\gradlew.bat :shared:compileKotlinJvm
```

Desktop compile:

```powershell
.\gradlew.bat :desktopApp:classes
```

Android compile:

```powershell
.\gradlew.bat :app:assembleDebug
```

All practical checks:

```powershell
.\gradlew.bat build
```

If Gradle needs Java on this machine:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
```

## Common Failure Modes

Watch for these:

- Desktop status label says `Tersedia` for status `1`.
- Shared model drops `basePrice` or `typePayment`.
- Payment uses `updateItemStatus(2)` without orderID/typePayment.
- Pickup updates one item instead of all items in order.
- Desktop only refreshes manually, no SSE.
- Android migration changes user-visible flow.
- UI creates `ItemResponse` payload directly.
- Use case overwrites fields with null because input model is partial.

## Agent Handoff Template

Every agent must end with this:

```text
Phase worked:
Scope:
Files changed:
Build/test run:
Result:
Known gaps:
Next recommended step:
Risk notes:
```

Example:

```text
Phase worked: Phase 2 Domain Model Foundation
Scope: Added AuctionItem, ItemStatus, PaymentMethod and mapper tests.
Files changed: shared/src/commonMain/.../AuctionItem.kt, ...
Build/test run: .\gradlew.bat :shared:compileKotlinJvm
Result: Passed.
Known gaps: Android still uses old ItemResponse in UI.
Next recommended step: Add repository contracts.
Risk notes: None.
```

## Final Success Criteria

The refactor is successful when:

- Android and Desktop share one business model.
- Android and Desktop share one set of use cases.
- Android and Desktop share MVVM state/actions where practical.
- UI differs only by platform UX.
- Platform adapters are behind interfaces.
- DI graph is clear.
- SSE works on both platforms.
- Session expiry works on both platforms.
- Server payload is identical for equivalent actions.
- No major business logic remains duplicated in platform UI.
