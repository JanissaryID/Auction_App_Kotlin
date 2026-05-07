# CI/CD Pipeline Setup

## 📋 Overview

Automated build pipeline untuk Auction App menggunakan GitHub Actions.

---

## 🔧 Workflows

### 1. Android Build (`android-build.yml`) ✅

**Trigger:**
- Push ke `main` atau `develop`
- Pull request ke `main` atau `develop`

**Steps:**
1. Checkout code
2. Setup JDK 21
3. Build shared module
4. Build Android app (debug)
5. Upload APK artifact

**Artifacts:**
- `app-debug.apk` (retained for 7 days)

**Runner:** `ubuntu-latest`

---

### 2. Desktop Build (`desktop-build.yml`) ✅

**Trigger:**
- Push ke `main` atau `develop`
- Pull request ke `main` atau `develop`

**Jobs:**

#### Windows Build
- **Runner:** `windows-latest`
- **Steps:**
  1. Checkout code
  2. Setup JDK 21
  3. Build shared module
  4. Build desktop app
  5. Package EXE (optional)
  6. Upload artifacts

#### Linux Build
- **Runner:** `ubuntu-latest`
- **Steps:**
  1. Checkout code
  2. Setup JDK 21
  3. Build shared module
  4. Build desktop app

#### macOS Build
- **Runner:** `macos-latest`
- **Steps:**
  1. Checkout code
  2. Setup JDK 21
  3. Build shared module
  4. Build desktop app

**Artifacts:**
- Desktop Windows binaries (retained for 7 days)

---

### 3. Release Build (`release.yml`) ✅

**Trigger:**
- Push tag dengan pattern `v*` (e.g., `v1.0.0`)

**Jobs:**

#### Android Release
- Build release APK
- Upload to artifacts

#### Desktop Windows Release
- Package EXE installer
- Package MSI installer (optional)
- Upload to artifacts

#### Create GitHub Release
- Download all artifacts
- Create GitHub release
- Attach APK and installers
- Generate release notes

**Artifacts:**
- `app-release.apk` (retained for 30 days)
- Desktop installers (retained for 30 days)

---

## 🚀 Usage

### Running Builds

#### Automatic Triggers

**Development Builds:**
```bash
# Push to main or develop
git push origin main

# Create pull request
gh pr create --base main --head feature-branch
```

**Release Builds:**
```bash
# Create and push tag
git tag v1.0.0
git push origin v1.0.0
```

#### Manual Triggers

Via GitHub UI:
1. Go to Actions tab
2. Select workflow
3. Click "Run workflow"
4. Choose branch
5. Click "Run workflow" button

---

## 📦 Artifacts

### Development Artifacts

**Android:**
- Location: Actions → Workflow run → Artifacts
- File: `app-debug.apk`
- Retention: 7 days

**Desktop:**
- Location: Actions → Workflow run → Artifacts
- File: `desktop-windows.zip`
- Retention: 7 days

### Release Artifacts

**Location:** GitHub Releases page

**Files:**
- `app-release.apk` - Android app
- `AuctionApp-1.0.0.exe` - Windows installer
- `AuctionApp-1.0.0.msi` - Windows MSI (optional)

**Retention:** Permanent (until manually deleted)

---

## 🔐 Secrets & Variables

### Required Secrets

**For Release Workflow:**
- `GITHUB_TOKEN` - Automatically provided by GitHub

**For Signed Android Builds (Optional):**
- `KEYSTORE_FILE` - Base64 encoded keystore
- `KEYSTORE_PASSWORD` - Keystore password
- `KEY_ALIAS` - Key alias
- `KEY_PASSWORD` - Key password

### Setup Secrets

```bash
# Via GitHub CLI
gh secret set KEYSTORE_PASSWORD

# Via GitHub UI
Settings → Secrets and variables → Actions → New repository secret
```

---

## 📊 Build Status

### Badges

Add to README.md:

```markdown
![Android Build](https://github.com/username/AuctionApp/workflows/Android%20Build/badge.svg)
![Desktop Build](https://github.com/username/AuctionApp/workflows/Desktop%20Build/badge.svg)
```

### Monitoring

**View build status:**
1. Go to Actions tab
2. Select workflow
3. View recent runs

**Notifications:**
- Email notifications for failed builds
- GitHub notifications
- Slack integration (optional)

---

## 🐛 Troubleshooting

### Common Issues

#### 1. Gradle Build Failed

**Error:** `Task :app:compileDebugKotlin FAILED`

**Solution:**
```yaml
# Add to workflow
- name: Clean build
  run: ./gradlew clean
```

#### 2. Out of Memory

**Error:** `OutOfMemoryError: Java heap space`

**Solution:**
```yaml
# Add to workflow
- name: Build with more memory
  run: ./gradlew build -Xmx4g
```

#### 3. Cache Issues

**Error:** `Could not resolve dependencies`

**Solution:**
```yaml
# Clear cache
- name: Clear Gradle cache
  run: rm -rf ~/.gradle/caches
```

#### 4. Permission Denied

**Error:** `Permission denied: ./gradlew`

**Solution:**
```yaml
# Add before build
- name: Grant execute permission
  run: chmod +x gradlew
```

---

## 🔄 Workflow Optimization

### Caching

**Gradle Cache:**
```yaml
- uses: actions/setup-java@v4
  with:
    cache: gradle  # Enables Gradle caching
```

**Benefits:**
- Faster builds (2-5x speedup)
- Reduced network usage
- Lower costs

### Parallel Jobs

**Current:**
- Android and Desktop builds run in parallel
- Windows, Linux, macOS builds run in parallel

**Benefits:**
- Faster overall pipeline
- Early failure detection

### Conditional Steps

**Example:**
```yaml
- name: Package EXE
  if: github.ref == 'refs/heads/main'
  run: ./gradlew packageExe
```

---

## 📈 Metrics

### Build Times (Estimated)

| Workflow | Duration | Runner |
|----------|----------|--------|
| Android Build | 5-8 min | ubuntu-latest |
| Desktop Windows | 6-10 min | windows-latest |
| Desktop Linux | 5-8 min | ubuntu-latest |
| Desktop macOS | 6-10 min | macos-latest |
| Release Build | 15-25 min | Multiple |

### Resource Usage

**Free tier limits (GitHub Actions):**
- 2,000 minutes/month for private repos
- Unlimited for public repos

**Current usage estimate:**
- ~10 builds/day = ~100 minutes/day
- ~3,000 minutes/month

---

## 🎯 Best Practices

### 1. Branch Protection

**Setup:**
```
Settings → Branches → Add rule
- Require status checks to pass
- Require branches to be up to date
- Include administrators
```

**Required checks:**
- Android Build
- Desktop Build (Windows)

### 2. Pull Request Workflow

**Process:**
1. Create feature branch
2. Make changes
3. Push to GitHub
4. Create PR
5. Wait for CI checks
6. Review and merge

### 3. Release Process

**Steps:**
1. Update version in `build.gradle.kts`
2. Update CHANGELOG.md
3. Commit changes
4. Create tag: `git tag v1.0.0`
5. Push tag: `git push origin v1.0.0`
6. Wait for release workflow
7. Verify artifacts in Releases

### 4. Versioning

**Semantic Versioning:**
- `v1.0.0` - Major release
- `v1.1.0` - Minor release (new features)
- `v1.0.1` - Patch release (bug fixes)

---

## 🔮 Future Enhancements

### Phase 1: Testing
- [ ] Add unit test execution
- [ ] Add integration tests
- [ ] Code coverage reports
- [ ] Test result publishing

### Phase 2: Quality Checks
- [ ] Lint checks
- [ ] Code style checks
- [ ] Security scanning
- [ ] Dependency checks

### Phase 3: Advanced Features
- [ ] Automated changelog generation
- [ ] Slack notifications
- [ ] Deploy to Play Store (Android)
- [ ] Deploy to Microsoft Store (Desktop)

### Phase 4: Performance
- [ ] Build time optimization
- [ ] Artifact size optimization
- [ ] Incremental builds
- [ ] Remote cache

---

## 📚 References

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Gradle Build Cache](https://docs.gradle.org/current/userguide/build_cache.html)
- [Compose Desktop Packaging](https://github.com/JetBrains/compose-multiplatform/tree/master/tutorials/Native_distributions_and_local_execution)

---

## ✅ Checklist

### Setup
- [x] Create workflow files
- [x] Configure triggers
- [x] Setup artifacts
- [ ] Add secrets (if needed)
- [ ] Test workflows

### Monitoring
- [ ] Add status badges
- [ ] Setup notifications
- [ ] Monitor build times
- [ ] Track success rate

### Optimization
- [ ] Enable caching
- [ ] Optimize build steps
- [ ] Reduce artifact size
- [ ] Improve parallelization

---

**Setup Date:** May 7, 2026  
**Status:** ✅ **CONFIGURED**  
**Next Review:** After first successful builds
