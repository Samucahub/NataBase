# ✅ NataBase Installation Checklist

**For Backend Team:** Follow this step-by-step to integrate the frontend package.

---

## 🎯 Pre-Installation

- [ ] **Backup your project** (commit to Git or create a copy)
- [ ] **Read README.md** (5 min overview)
- [ ] **Verify package contents** (33 files - see FILE_LIST.md)
- [ ] **Have Android Studio open** with your project loaded
- [ ] **Ensure project builds** without errors currently

---

## 📦 Step 1: Copy Resource Files (10 min)

### 1.1 Drawable Resources
- [ ] Navigate to `PLUGIN_PACKAGE_NATABASE/res/drawable/`
- [ ] Copy **ALL 11 files** to `YOUR_PROJECT/app/src/main/res/drawable/`
- [ ] **Critical:** Verify `padariabackground_login.jpg` copied successfully
- [ ] **Critical:** Verify `overlay_brown_translucent.xml` copied successfully

### 1.2 Font Resources
- [ ] Create folder `YOUR_PROJECT/app/src/main/res/font/` (if doesn't exist)
- [ ] Copy **ALL 3 files** from `PLUGIN_PACKAGE_NATABASE/res/font/`
- [ ] Verify files: `inclusive_sans.xml`, `inclusive_sans_regular.ttf`, `inclusive_sans_bold.ttf`
- [ ] Check file sizes: TTF files should be ~200KB each

### 1.3 Layout Resources
- [ ] Copy **3 files** from `PLUGIN_PACKAGE_NATABASE/res/layout/` to `YOUR_PROJECT/app/src/main/res/layout/`
  - [ ] `menulogin_pro.xml`
  - [ ] `croissant_menu.xml`
  - [ ] `pao_menu.xml`

### 1.4 Layout-Land Resources
- [ ] Create folder `YOUR_PROJECT/app/src/main/res/layout-land/` (if doesn't exist)
- [ ] Copy **1 file**: `menulogin_pro.xml` from `PLUGIN_PACKAGE_NATABASE/res/layout-land/`

### 1.5 Layout-Port Resources
- [ ] Create folder `YOUR_PROJECT/app/src/main/res/layout-port/` (if doesn't exist)
- [ ] Copy **3 files** from `PLUGIN_PACKAGE_NATABASE/res/layout-port/`
  - [ ] `menulogin_pro.xml`
  - [ ] `croissant_menu.xml`
  - [ ] `pao_menu.xml`

---

## 🎨 Step 2: Merge Values Files (5 min)

**⚠️ IMPORTANT:** Don't overwrite! Merge carefully.

### 2.1 colors.xml
- [ ] Open `YOUR_PROJECT/app/src/main/res/values/colors.xml`
- [ ] Open `PLUGIN_PACKAGE_NATABASE/res/values/colors.xml` in text editor
- [ ] **Add these 4 colors** (copy-paste):
```xml
<color name="caramel_background">#E0AB61</color>
<color name="deep_brown">#663F07</color>
<color name="medium_brown">#97713C</color>
<color name="dark_brown_text">#2B1B04</color>
```
- [ ] Save file

### 2.2 strings.xml
- [ ] Open `YOUR_PROJECT/app/src/main/res/values/strings.xml`
- [ ] Open `PLUGIN_PACKAGE_NATABASE/res/values/strings.xml`
- [ ] **Copy ALL strings** from package (32 strings)
- [ ] Paste at end of your strings.xml (before `</resources>`)
- [ ] Check for duplicate names (app_name might conflict)
- [ ] Save file

### 2.3 themes.xml
- [ ] Open `YOUR_PROJECT/app/src/main/res/values/themes.xml`
- [ ] Open `PLUGIN_PACKAGE_NATABASE/res/values/themes.xml`
- [ ] **Option A:** Copy entire `Theme.NataBasePrime` theme
- [ ] **Option B:** Merge color references into your existing theme
- [ ] Save file

---

## 💻 Step 3: Copy Activity Files (3 min)

### 3.1 Create Package Structure
- [ ] Verify package path: `YOUR_PROJECT/app/src/main/java/com/example/natabaseprime/`
- [ ] Adjust package name if different (Find & Replace in .kt files)

### 3.2 Copy Activities
- [ ] Copy from `PLUGIN_PACKAGE_NATABASE/java/`:
  - [ ] `MainActivity.kt`
  - [ ] `CroissantMenuActivity.kt`
  - [ ] `PaoMenuActivity.kt`
- [ ] Paste into your package folder

### 3.3 Update Package Names (if needed)
If your package is NOT `com.example.natabaseprime`:
- [ ] Open each .kt file
- [ ] Replace `package com.example.natabaseprime` with YOUR package name
- [ ] Example: `package com.yourcompany.yourapp`

---

## 📱 Step 4: Update AndroidManifest.xml (2 min)

- [ ] Open `YOUR_PROJECT/app/src/main/AndroidManifest.xml`
- [ ] Find the `<application>` tag
- [ ] **Add these 3 activities** (copy-paste inside `<application>`):

```xml
<!-- NataBase Login -->
<activity
    android:name=".MainActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>

<!-- NataBase Croissant Menu -->
<activity
    android:name=".CroissantMenuActivity"
    android:exported="false"
    android:screenOrientation="unspecified" />

<!-- NataBase Pão Menu -->
<activity
    android:name=".PaoMenuActivity"
    android:exported="false"
    android:screenOrientation="unspecified" />
```

- [ ] **Note:** If you already have a launcher activity, remove `<intent-filter>` from MainActivity
- [ ] Save file

---

## 🔧 Step 5: Update build.gradle (2 min)

- [ ] Open `YOUR_PROJECT/app/build.gradle` (Module level)
- [ ] Find the `dependencies` section
- [ ] **Verify these dependencies exist** (add if missing):

```gradle
dependencies {
    implementation 'androidx.core:core-ktx:1.10.1'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.9.0'
    implementation 'androidx.cardview:cardview:1.0.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
}
```

- [ ] Save file

---

## 🔄 Step 6: Sync & Build (3 min)

- [ ] **File → Sync Project with Gradle Files**
- [ ] Wait for sync to complete (check bottom status bar)
- [ ] **Build → Clean Project**
- [ ] **Build → Rebuild Project**
- [ ] Check for errors in "Build" tab (bottom of screen)

### Expected Results:
✅ Build should succeed with "BUILD SUCCESSFUL"  
✅ No red error messages in Problems tab  
⚠️ Yellow warnings are OK (but review them)

---

## 🧪 Step 7: Test Installation (5 min)

### 7.1 Visual Inspection
- [ ] Open `layout/menulogin_pro.xml` in Design view
- [ ] Verify colors appear correctly (caramel background)
- [ ] Verify font displays (should be Inclusive Sans)
- [ ] Verify background image shows in preview

### 7.2 Run on Emulator/Device
- [ ] Click **Run** (green play button) or press Shift+F10
- [ ] Select device/emulator
- [ ] Wait for app to install and launch

### 7.3 Test Login Screen
- [ ] Verify caramel background appears
- [ ] Verify NataBase logo displays
- [ ] Verify Inclusive Sans font renders
- [ ] Try typing in username/password fields
- [ ] Click "ENTRAR" button (should show toast)
- [ ] Rotate device (test landscape layout)

### 7.4 Navigate to Menus (Manual)
**Note:** Since navigation isn't implemented yet, you need to launch activities manually.

**Option 1 - Test via Code (Temporary):**
Add to MainActivity's onCreate after setContentView:
```kotlin
// TEMPORARY: Auto-navigate to test menu
startActivity(Intent(this, CroissantMenuActivity::class.java))
```

**Option 2 - Test via Android Studio:**
- [ ] Click "Run" dropdown → Edit Configurations
- [ ] Change "Launch Activity" to "CroissantMenuActivity"
- [ ] Run app to test Croissant menu
- [ ] Repeat for PaoMenuActivity

### 7.5 Test Croissant Menu
- [ ] Verify 6 product cards display
- [ ] Verify background images show on cards
- [ ] Verify text is readable (white on translucent overlay)
- [ ] Verify side images appear (tablet/landscape)
- [ ] Click each product card (should show toast)
- [ ] Click "Retroceder" button (should close activity)
- [ ] Rotate device (test portrait layout)

### 7.6 Test Pão Menu
- [ ] Verify 4 product cards + 2 faded placeholders
- [ ] Verify faded cards are NOT clickable
- [ ] Verify active cards work (show toast)
- [ ] Test rotation (landscape/portrait)

---

## ✅ Post-Installation Verification

### File Checklist
- [ ] All 11 drawable files present in `res/drawable/`
- [ ] All 3 font files present in `res/font/`
- [ ] All 7 layout files in correct folders
- [ ] All 3 value files merged correctly
- [ ] All 3 Activity .kt files in package folder
- [ ] AndroidManifest.xml updated with 3 activities

### Build Checklist
- [ ] No compile errors (red text)
- [ ] Gradle sync successful
- [ ] App installs on device/emulator
- [ ] Login screen displays correctly
- [ ] Menus accessible (even if navigation not implemented)

### Design Checklist
- [ ] Caramel background color (#E0AB61) displays
- [ ] Deep brown top bar (#663F07) displays
- [ ] Inclusive Sans font renders (not default Roboto)
- [ ] Background image shows on cards
- [ ] 50% translucent overlay makes text readable
- [ ] Layouts adapt to device rotation

---

## 🐛 Troubleshooting

### ❌ Build Fails: "Unresolved reference: R"
**Solution:**
- Clean Project (Build → Clean Project)
- Invalidate Caches (File → Invalidate Caches → Invalidate and Restart)
- Check resource files for XML syntax errors

### ❌ Font Not Displaying
**Solution:**
- Verify `res/font/` folder exists (NOT `res/fonts/` with 's')
- Check TTF files are not corrupted (should be ~200KB each)
- Verify `inclusive_sans.xml` references correct filenames

### ❌ Colors Not Working
**Solution:**
- Check colors.xml is in `res/values/` (not `res/values-night/`)
- Verify all 4 colors copied correctly
- Clean and rebuild project

### ❌ Background Image Missing
**Solution:**
- Verify `padariabackground_login.jpg` in `res/drawable/` (not `res/images/`)
- Check file extension is `.jpg` (not `.jfif` or `.jpeg`)
- Image should be ~500KB, 1920x1080 pixels

### ❌ Layout Errors
**Solution:**
- Check `layout/`, `layout-land/`, `layout-port/` folders exist
- Verify file names match exactly (case-sensitive on Linux/Mac)
- Look for missing closing tags in XML

### ❌ Activity Not Found
**Solution:**
- Verify activities in AndroidManifest.xml
- Check package names match in .kt files
- Ensure activities have correct `android:name` attribute

---

## 🎯 Next Steps After Installation

### Immediate (Your Team)
1. **Test all screens** thoroughly on multiple devices/orientations
2. **Remove temporary test code** (auto-navigation, toasts)
3. **Plan navigation flow** (how users move between screens)

### Backend Integration
1. **Review TODO sections** in all Activity files
2. **Implement authentication** in MainActivity.handleLogin()
3. **Implement Google OAuth** in MainActivity.handleGoogleLogin()
4. **Add product selection logic** in menu activities
5. **Connect to backend APIs** (orders, user sessions, etc.)

### Optional Enhancements
- [ ] Add loading indicators (ProgressBar during API calls)
- [ ] Add error handling (network errors, invalid login)
- [ ] Add animations (transitions between screens)
- [ ] Add menu navigation (bottom nav bar or drawer)
- [ ] Replace placeholder images with real product photos

---

## 📞 Support

**If you encounter issues:**
1. Check this INSTALLATION_CHECKLIST.md troubleshooting section
2. Review README.md for detailed explanations
3. Verify FILE_LIST.md to ensure all files present
4. Contact Joseph (frontend developer) for assistance

---

## 🎉 Success Criteria

**Installation is successful when:**
✅ App builds without errors  
✅ Login screen displays with caramel theme  
✅ Inclusive Sans font renders  
✅ Product menus show images with readable text  
✅ Layouts adapt to device rotation  
✅ All buttons clickable (even if just showing toasts)

**You're ready for backend integration when:**
✅ All TODO sections reviewed  
✅ Component IDs documented  
✅ Navigation flow planned  
✅ API endpoints defined

---

**Estimated Total Time:** 30 minutes  
**Difficulty Level:** Easy (copy-paste + basic Android knowledge)  
**Risk Level:** Low (doesn't modify existing code, only adds new files)

**Good luck! 🚀**
