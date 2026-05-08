# Quick Start Guide

Panduan cepat untuk menjalankan AuctionApp setelah migrasi KMP.

---

## 🚀 Running Android App

### Prerequisites
- Android Studio
- Android device atau emulator (API 26+)

### Steps

1. **Open project di Android Studio**
   ```
   File → Open → pilih folder AuctionApp
   ```

2. **Sync Gradle**
   ```
   File → Sync Project with Gradle Files
   ```

3. **Run Android app**
   ```
   Pilih :app configuration
   Klik Run (▶️)
   ```

   Atau via command line:
   ```powershell
   .\gradlew.bat :app:installDebug
   ```

4. **Build APK**
   ```powershell
   .\gradlew.bat :app:assembleDebug
   ```
   
   Output: `app/build/outputs/apk/debug/app-debug.apk`

---

## 🖥️ Running Desktop App

### Prerequisites
- JDK 21 (sudah ada di `C:\Program Files\Android\openjdk\jdk-21.0.8`)
- Windows 10/11

### Option 1: Run from Gradle (Development)

```powershell
# Set Java 21
$env:JAVA_HOME = "C:\Program Files\Android\openjdk\jdk-21.0.8"

# Run desktop app
.\gradlew.bat :desktopApp:run
```

### Option 2: Build & Install Package (Production)

#### Build MSI Installer

```powershell
# Set Java 21
$env:JAVA_HOME = "C:\Program Files\Android\openjdk\jdk-21.0.8"

# Build MSI
.\gradlew.bat :desktopApp:packageMsi
```

Output: `desktopApp/build/compose/binaries/main/msi/AuctionApp-1.0.0.msi`

#### Build EXE Installer

```powershell
# Set Java 21
$env:JAVA_HOME = "C:\Program Files\Android\openjdk\jdk-21.0.8"

# Build EXE
.\gradlew.bat :desktopApp:packageExe
```

Output: `desktopApp/build/compose/binaries/main/exe/AuctionApp-1.0.0.exe`

#### Install & Run

1. Double-click MSI atau EXE
2. Ikuti wizard instalasi
3. Launch dari Start Menu → AuctionApp

---

## 🧪 Running Tests

### Shared Module Tests

```powershell
# Set Java 21
$env:JAVA_HOME = "C:\Program Files\Android\openjdk\jdk-21.0.8"

# Run desktop tests
.\gradlew.bat :shared:desktopTest
```

### Android Tests

```powershell
# Unit tests
.\gradlew.bat :app:testDebugUnitTest

# Instrumented tests (requires device/emulator)
.\gradlew.bat :app:connectedDebugAndroidTest
```

---

## 🔧 Build All Targets

Untuk verify semua target compile:

```powershell
# Set Java 21
$env:JAVA_HOME = "C:\Program Files\Android\openjdk\jdk-21.0.8"

# Build Android
.\gradlew.bat :app:assembleDebug

# Build Shared Desktop
.\gradlew.bat :shared:compileKotlinDesktop

# Build Desktop App
.\gradlew.bat :desktopApp:compileKotlin

# Run Desktop Tests
.\gradlew.bat :shared:desktopTest
```

---

## 📱 First Run - Android

1. **Launch app**
2. **Login**
   - Email: `[your-email]`
   - Password: `[your-password]`
3. **Verify features**
   - Item list loads
   - Can add/edit/delete items
   - Camera scan works
   - Auction flow works
   - Payment flow works

---

## 🖥️ First Run - Desktop

1. **Launch app** (via Gradle atau installed package)
2. **Login**
   - Klik "Login" di side navigation footer
   - Email: `[your-email]`
   - Password: `[your-password]`
3. **Verify features**
   - Item list loads
   - Can add/edit/delete items
   - Search works
   - Auction flow works
   - Payment flow works

---

## 🔄 Realtime Sync Test

1. **Open Android app** dan login
2. **Open Desktop app** dan login (same account)
3. **Add item di Android** → verify muncul di Desktop
4. **Edit item di Desktop** → verify update di Android
5. **Delete item di Android** → verify hilang di Desktop

---

## 🐛 Troubleshooting

### Android Build Fails

**Problem**: Gradle sync error atau build fail

**Solution**:
```powershell
# Clean build
.\gradlew.bat clean

# Stop daemons
.\gradlew.bat --stop

# Rebuild
.\gradlew.bat :app:assembleDebug
```

### Desktop Build Fails

**Problem**: Java version error

**Solution**:
```powershell
# Verify Java 21
$env:JAVA_HOME = "C:\Program Files\Android\openjdk\jdk-21.0.8"
& "$env:JAVA_HOME\bin\java.exe" -version

# Should show: openjdk version "21.0.8"
```

**Problem**: Kotlin version error

**Solution**: Pastikan menggunakan JDK 21, bukan JDK 25

### Desktop App Won't Start

**Problem**: Window tidak muncul

**Solution**:
1. Check console output untuk error
2. Verify session file: `~/.auctionapp/session.properties`
3. Try delete session file dan login ulang

### Network Error

**Problem**: API calls fail

**Solution**:
1. Verify internet connection
2. Check PocketBase server: `https://pb.janissaryid.com`
3. Verify credentials

### Session Expired

**Problem**: Terus logout otomatis

**Solution**:
1. Login ulang dengan credentials valid
2. Check token expiry di server
3. Verify session persistence:
   - Android: DataStore di app data
   - Desktop: `~/.auctionapp/session.properties`

---

## 📂 Project Structure

```
AuctionApp/
├─ app/                          # Android app
│  ├─ src/main/java/...         # Android source
│  └─ build.gradle.kts          # Android build config
│
├─ shared/                       # KMP shared module
│  ├─ src/
│  │  ├─ commonMain/            # Shared business logic
│  │  ├─ androidMain/           # Android implementations
│  │  └─ desktopMain/           # Desktop implementations
│  └─ build.gradle.kts          # Shared build config
│
├─ desktopApp/                   # Desktop app
│  ├─ src/main/kotlin/...       # Desktop source
│  └─ build.gradle.kts          # Desktop build config
│
├─ gradle/                       # Gradle wrapper
├─ build.gradle.kts             # Root build config
├─ settings.gradle.kts          # Module settings
│
└─ Documentation/
   ├─ KMP_MIGRATION_GUIDE.md    # Migration guide (source of truth)
   ├─ MIGRATION_STATUS.md       # Migration checkpoint log
   ├─ MIGRATION_SUMMARY.md      # Migration overview
   ├─ MANUAL_TESTING_GUIDE.md   # Testing checklist
   └─ QUICK_START.md            # This file
```

---

## 🎯 Common Tasks

### Add New Feature

1. **Domain layer** (`shared/commonMain/domain/`)
   - Add model
   - Add repository interface
   - Add use case

2. **Data layer** (`shared/commonMain/data/`)
   - Add repository implementation
   - Add DTO if needed
   - Add mapper

3. **Presentation layer** (`shared/commonMain/presentation/`)
   - Add ViewModel
   - Add UiState/UiAction

4. **DI** (`shared/commonMain/di/`)
   - Register in Koin modules

5. **Android UI** (`app/src/main/`)
   - Add screen/composable
   - Use shared ViewModel

6. **Desktop UI** (`desktopApp/src/main/`)
   - Add screen/dialog
   - Use shared ViewModel

### Update Dependencies

1. Edit `gradle/libs.versions.toml`
2. Sync Gradle
3. Test all targets

### Debug Shared Code

**Android**:
- Set breakpoint di shared code
- Run Android app in debug mode
- Breakpoint akan hit

**Desktop**:
- Set breakpoint di shared code
- Run Desktop app in debug mode dari IDE
- Breakpoint akan hit

---

## 📞 Need Help?

- **Migration Guide**: `KMP_MIGRATION_GUIDE.md`
- **Migration Status**: `MIGRATION_STATUS.md`
- **Testing Guide**: `MANUAL_TESTING_GUIDE.md`
- **Summary**: `MIGRATION_SUMMARY.md`

---

**Happy Coding! 🚀**
