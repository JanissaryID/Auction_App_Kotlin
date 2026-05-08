# Manual Testing Guide - Phase 19

Dokumen ini membantu tim melakukan manual regression testing untuk menyelesaikan Phase 19 migrasi KMP.

## Prerequisites

### Android Testing
- Device Android atau emulator dengan API 26+
- APK debug: `app/build/outputs/apk/debug/app-debug.apk`
- Kredensial login yang valid

### Desktop Testing
- Windows PC
- Java 21 (sudah terinstall di `C:\Program Files\Android\openjdk\jdk-21.0.8`)
- Kredensial login yang sama dengan Android

### Windows Package Testing
- Clean Windows environment (VM atau PC lain)
- File installer:
  - `desktopApp/build/compose/binaries/main/msi/AuctionApp-1.0.0.msi`
  - `desktopApp/build/compose/binaries/main/exe/AuctionApp-1.0.0.exe`

---

## Android Manual Regression Checklist

### 1. Authentication & Session
- [ ] **App starts**: Buka aplikasi, tidak ada crash
- [ ] **Login dialog works**: Masukkan email & password valid, login berhasil
- [ ] **Saved token restores**: Tutup app, buka lagi, otomatis login
- [ ] **Home profile displays**: Nama user dan avatar/initial tampil
- [ ] **Logout works**: Logout berhasil, kembali ke login screen
- [ ] **Session expired**: Simulasi token expired (atau tunggu timeout), muncul notifikasi "Sesi telah habis"

### 2. Items Management
- [ ] **Item list loads**: Daftar barang tampil setelah login
- [ ] **Add item**: Tambah barang baru, muncul di list
- [ ] **Add multiple items**: Tambah barang dengan jumlah > 1, semua variant muncul dengan suffix
- [ ] **Edit item**: Edit nama/kode/harga barang, perubahan tersimpan
- [ ] **Delete item**: Hapus barang, hilang dari list
- [ ] **Scan barcode with camera**: Buka scanner, scan barcode, barang terdeteksi

### 3. Auction Flow
- [ ] **Auction flow**: Pilih barang status 0, set pemenang & harga, submit berhasil
- [ ] **Bluetooth print works**: Setelah auction, print label via Bluetooth printer

### 4. Payment Flow
- [ ] **Payment flow**: Pilih barang status 1, pilih metode bayar, submit berhasil
- [ ] **List payment**: Barang yang sudah dibayar muncul di list pembayaran

### 5. Pickup Flow
- [ ] **Take items**: Pilih order yang sudah dibayar, konfirmasi pengambilan, status jadi 3

### 6. Transactions & Export
- [ ] **Transactions**: Lihat history transaksi, data benar
- [ ] **Export Excel**: Export transaksi ke Excel, file tersimpan di Downloads/Documents

### 7. Realtime Updates
- [ ] **SSE works**: Buka app di 2 device, ubah data di device 1, otomatis update di device 2

---

## Desktop Manual Regression Checklist

### 1. Window & Layout
- [ ] **Desktop window opens**: Window 1280x800 terbuka tanpa error
- [ ] **Side navigation visible**: Nav kiri dengan menu Dashboard, Barang, Lelang, dll
- [ ] **Resize works**: Resize window, layout tetap rapi

### 2. Authentication & Session
- [ ] **Login dialog works**: Klik login, masukkan email & password, berhasil
- [ ] **Enter key submits**: Di login dialog, tekan Enter untuk submit
- [ ] **Saved token restores**: Tutup app, buka lagi, otomatis login
- [ ] **Profile displays**: Nama user dan initial avatar tampil di header
- [ ] **Logout works**: Buka profile dialog, logout, kembali ke state logged out
- [ ] **Session expired**: Simulasi token expired, muncul notifikasi

### 3. Items Management (Barang)
- [ ] **Item list loads**: Tabel barang tampil dengan semua kolom
- [ ] **Search items works**: Ketik di search box, hasil filter sesuai
- [ ] **Status filter works**: Klik filter status, list berubah sesuai status
- [ ] **Add item**: Klik Add, isi form, submit, barang baru muncul
- [ ] **Add multiple items**: Set jumlah > 1, semua variant muncul dengan suffix
- [ ] **Edit item**: Klik Edit, ubah data, submit, perubahan tersimpan
- [ ] **Delete item**: Klik Delete, konfirmasi, barang hilang
- [ ] **Delete multiple items**: Select beberapa barang, klik Delete Selected, semua terhapus
- [ ] **Item detail dialog**: Klik Detail, semua metadata tampil

### 4. Auction Flow (Lelang)
- [ ] **Select items**: Klik Add Item, pilih barang status 0, muncul di editor table
- [ ] **Barcode entry**: Klik Barcode, ketik kode, barang ditambahkan
- [ ] **Edit buyer & price**: Ketik langsung di table, nilai tersimpan
- [ ] **Submit auction**: Klik Submit, semua barang jadi status 1
- [ ] **Duplicate prevention**: Coba tambah barang yang sudah ada, tidak duplikat
- [ ] **Clear selection**: Klik Clear, editor table kosong

### 5. Payment Flow (Pembayaran)
- [ ] **Select items**: Klik Add Item, pilih barang status 1, muncul di receipt table
- [ ] **Barcode entry**: Klik Barcode, ketik kode barang status 1, ditambahkan
- [ ] **Total calculation**: Total harga dihitung dengan benar
- [ ] **Choose payment method**: Klik Pay, pilih Cash/QRIS/Kredit
- [ ] **Submit payment**: Konfirmasi, semua barang jadi status 2 dengan orderID sama

### 6. Pickup Flow (Pengambilan)
- [ ] **Select items**: Klik Add Item, pilih barang status 2, muncul di editor table
- [ ] **Barcode entry**: Klik Barcode, ketik kode barang status 2, ditambahkan
- [ ] **Confirm pickup**: Klik Confirm, semua barang jadi status 3

### 7. Transactions (Transaksi)
- [ ] **View history**: Tabel transaksi tampil, dikelompokkan per orderID
- [ ] **View detail**: Klik Detail, dialog tampil dengan item list per order
- [ ] **Search works**: Ketik orderID atau nama pemenang, hasil filter sesuai
- [ ] **Metrics correct**: Total transaksi, omzet, dll dihitung dengan benar

### 8. Realtime & Cross-Platform
- [ ] **Realtime updates from Android**: Ubah data di Android, otomatis update di Desktop
- [ ] **Realtime updates from Desktop**: Ubah data di Desktop, otomatis update di Android

### 9. Keyboard UX
- [ ] **Enter submits forms**: Di dialog, tekan Enter untuk submit
- [ ] **Escape closes dialogs**: Tekan Esc untuk tutup dialog
- [ ] **Search auto-focus**: Klik search field, langsung bisa ketik

---

## Windows Package Installation Checklist

### 1. Installation
- [ ] **Install MSI**: Double-click MSI, ikuti wizard, instalasi berhasil
- [ ] **Install EXE**: Double-click EXE, ikuti wizard, instalasi berhasil
- [ ] **Shortcut created**: Shortcut "AuctionApp" muncul di Start Menu

### 2. First Run
- [ ] **Launch from shortcut**: Klik shortcut, app terbuka
- [ ] **Login works**: Login dengan kredensial valid, berhasil
- [ ] **Session file created**: File `~/.auctionapp/session.properties` terbuat

### 3. Functionality
- [ ] **Network works**: Semua API call berhasil (login, fetch items, dll)
- [ ] **All features work**: Semua fitur (Items, Auction, Payment, Pickup, Transactions) berfungsi
- [ ] **Session persists**: Tutup app, buka lagi, token restore berhasil

### 4. Uninstallation
- [ ] **Uninstall works**: Uninstall via Control Panel, app terhapus bersih
- [ ] **Session file removed**: File `~/.auctionapp/session.properties` terhapus (optional)

---

## Running Desktop App for Testing

### Option 1: Run from Gradle (Development)

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\openjdk\jdk-21.0.8"
.\gradlew.bat :desktopApp:run
```

### Option 2: Run from Package (Production)

1. Build package:
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\openjdk\jdk-21.0.8"
.\gradlew.bat :desktopApp:packageMsi
```

2. Install MSI dari `desktopApp/build/compose/binaries/main/msi/AuctionApp-1.0.0.msi`

3. Launch dari Start Menu

---

## Expected Behavior Parity

### Business Logic
- **Item status flow**: 0 → 1 (auction) → 2 (payment) → 3 (pickup)
- **OrderID format**: `Order-[RANDOM]` untuk semua item dalam 1 transaksi
- **Price formatting**: Rupiah dengan separator ribuan
- **Duplicate prevention**: Tidak bisa tambah item yang sudah ada di selection

### Data Consistency
- **Android ↔ Desktop**: Data harus sama persis
- **Realtime sync**: Perubahan di 1 platform langsung terlihat di platform lain
- **Session persistence**: Token tersimpan dan restore otomatis

### UI Differences (Expected)
- **Android**: Mobile UI dengan bottom navigation, floating action button
- **Desktop**: Desktop UI dengan side navigation, table layout, custom dialogs
- **Android**: Camera scan untuk barcode
- **Desktop**: Manual input untuk barcode (keyboard-wedge scanner supported)
- **Android**: Bluetooth printer
- **Desktop**: No printer (future: system print)

---

## Reporting Issues

Jika menemukan bug atau behavior yang tidak sesuai, catat:

1. **Platform**: Android atau Desktop
2. **Step to reproduce**: Langkah-langkah untuk reproduce bug
3. **Expected behavior**: Behavior yang diharapkan
4. **Actual behavior**: Behavior yang terjadi
5. **Screenshot/log**: Jika ada

Contoh:

```
Platform: Desktop
Step: Login → Barang → Add Item → Set jumlah 3 → Submit
Expected: 3 item dengan suffix -1, -2, -3
Actual: Hanya 1 item yang terbuat
Screenshot: [attach]
```

---

## Completion Criteria

Phase 19 dianggap selesai jika:

- ✅ Semua build target compile successfully (DONE)
- ⏳ Android manual regression passes (semua checklist ✓)
- ⏳ Desktop manual regression passes (semua checklist ✓)
- ⏳ Windows package installation verified (semua checklist ✓)

Setelah semua checklist selesai, migrasi KMP dianggap **COMPLETE** dan siap untuk production deployment.
