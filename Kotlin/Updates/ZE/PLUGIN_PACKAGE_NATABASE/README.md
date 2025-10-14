# 🥐 NataBase Frontend Package - Plug & Play Installation Guide

**Created:** October 14, 2025  
**Package Version:** 1.0  
**Author:** Joseph's Team  
**Status:** Ready for Backend Integration

---

## 📦 What's Included

This package contains **all frontend UI components** for the NataBase app:

### ✅ Complete Screens
1. **Login Screen** (MainActivity) - 3 responsive layouts (tablet/landscape/portrait)
2. **Croissant Menu** (CroissantMenuActivity) - 6 products with 2 layouts
3. **Pão Menu** (PaoMenuActivity) - 4 products + 2 placeholders with 2 layouts

### ✅ Resources
- **Fonts:** Inclusive Sans (regular + bold)
- **Colors:** Caramel bakery theme (#E0AB61, #663F07, #97713C)
- **Drawables:** 15+ custom components (buttons, overlays, icons)
- **Layouts:** 7 responsive XML layouts
- **Strings:** All Portuguese text externalized
- **Themes:** Material3 custom theme

---

## 🚀 Quick Installation (5 Steps)

### Step 1: Copy Resources to Your Project
```
📁 YOUR_PROJECT/app/src/main/
├── res/
│   ├── drawable/        ← Copy all files from PLUGIN_PACKAGE_NATABASE/res/drawable/
│   ├── font/            ← Copy all files from PLUGIN_PACKAGE_NATABASE/res/font/
│   ├── layout/          ← Copy all files from PLUGIN_PACKAGE_NATABASE/res/layout/
│   ├── layout-land/     ← Copy all files from PLUGIN_PACKAGE_NATABASE/res/layout-land/
│   ├── layout-port/     ← Copy all files from PLUGIN_PACKAGE_NATABASE/res/layout-port/
│   └── values/          ← Merge colors.xml, strings.xml, themes.xml
└── java/com/example/natabaseprime/
    ├── MainActivity.kt
    ├── CroissantMenuActivity.kt
    └── PaoMenuActivity.kt
```

### Step 2: Merge Values Files
**IMPORTANT:** Don't overwrite your existing values files! Instead:

**colors.xml** - Add these colors:
```xml
<color name="caramel_background">#E0AB61</color>
<color name="deep_brown">#663F07</color>
<color name="medium_brown">#97713C</color>
<color name="dark_brown_text">#2B1B04</color>
```

**strings.xml** - Copy all strings from the package

**themes.xml** - Add the NataBase theme or merge with your existing theme

### Step 3: Update AndroidManifest.xml
Add these activities inside `<application>` tag:
```xml
<!-- NataBase Activities -->
<activity
    android:name=".MainActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>

<activity
    android:name=".CroissantMenuActivity"
    android:exported="false"
    android:screenOrientation="unspecified" />

<activity
    android:name=".PaoMenuActivity"
    android:exported="false"
    android:screenOrientation="unspecified" />
```

### Step 4: Update build.gradle (app level)
Ensure these dependencies are present:
```gradle
dependencies {
    implementation "androidx.cardview:cardview:1.0.0"
    implementation "com.google.android.material:material:1.9.0"
    // ... your other dependencies
}
```

### Step 5: Sync & Build
```
File → Sync Project with Gradle Files
Build → Make Project
```

---

## 🎨 Design Specifications

### Color Palette
| Color Name | Hex Code | Usage |
|------------|----------|-------|
| Caramel Background | `#E0AB61` | Main background |
| Deep Brown | `#663F07` | Top bar, buttons |
| Medium Brown | `#97713C` | Sidebar (login) |
| Dark Brown Text | `#2B1B04` | Text on light backgrounds |
| White | `#FFFFFF` | Text on dark backgrounds |

### Typography
- **Font Family:** Inclusive Sans
- **Sizes:** Title (60sp), Subtitle (40sp), Button (30sp), Body (20sp), Small (14sp)

### Layout Strategy
- `layout/` → Tablet default (3-column for login, side images for menus)
- `layout-land/` → Landscape devices (3-column login)
- `layout-port/` → Portrait phones (scrollable, centered design)

---

## 🔧 Backend Integration Points

### MainActivity.kt - Login Screen
```kotlin
private fun handleLogin() {
    // TODO: Add your authentication logic here
    // Current: Shows toast, validates empty fields
    // Needed: API call to verify credentials
}

private fun handleGoogleLogin() {
    // TODO: Implement Google OAuth integration
    // Current: Shows placeholder toast
}
```

### CroissantMenuActivity.kt - Croissant Menu
```kotlin
private fun handleEnviar() {
    // TODO: Send order to backend
}

private fun handleRestart() {
    // TODO: Clear current session/order
}

private fun handleLogout() {
    // TODO: Clear user session, return to login
}

private fun handleProductClick(productName: String) {
    // TODO: Navigate to product detail or add to order
    // Parameter: productName (e.g., "CHOC E AVELÃ", "SIMPLES")
}

private fun handleBack() {
    // TODO: Navigate back to main menu
    // Current: Calls finish()
}
```

### PaoMenuActivity.kt - Pão Menu
Same structure as CroissantMenuActivity with 4 products:
- Baguete
- Bola Lenha
- Pão Cereais
- Pão Rustico Fatias

---

## 📱 Component Reference

### Login Screen (menulogin_pro.xml)
**IDs:**
- `editTextUsername` - Username input field
- `editTextPassword` - Password input field
- `buttonLogin` - Main login button
- `buttonGoogleLogin` - Google OAuth button

### Croissant Menu (croissant_menu.xml)
**Top Bar IDs:**
- `buttonEnviar` - Send button
- `buttonRestart` - Restart button
- `buttonLogout` - Logout button
- `textViewClock` - Live clock (HH:mm format)

**Product Card IDs:**
- `cardChocAvela` - Chocolate & Hazelnut
- `cardSimples` - Simple croissant
- `cardMulticereais` - Multigrain
- `cardMulticereaisMisto` - Multigrain mixed
- `cardMisto` - Mixed
- `cardPaoDesusMisto` - Pão de Deus mixed

**Navigation:**
- `buttonRetroceder` - Back button

### Pão Menu (pao_menu.xml)
**Product Card IDs:**
- `cardBaguete` - Baguette
- `cardBolaLenha` - Bola Lenha
- `cardPaoCereais` - Bread with cereals
- `cardPaoRusticoFatias` - Rustic bread slices

**Note:** 2 empty placeholder cards (faded, non-clickable) maintain grid consistency

---

## 🎯 Features & Highlights

### ✅ Fully Responsive
- Automatic layout selection based on device size/orientation
- Optimized for tablets, landscape phones, and portrait phones

### ✅ Accessibility Ready
- All images have `contentDescription`
- High contrast text (white on dark brown with 50% translucent overlay)
- Touch-friendly button sizes (minimum 56dp height)

### ✅ Modern UI/UX
- Material3 design components
- CardView elevation and rounded corners (12dp radius)
- Ripple effects on clickable items
- Consistent spacing and padding

### ✅ Internationalization
- All strings externalized in `strings.xml`
- Easy to add more languages (create `values-pt/`, `values-es/`, etc.)

### ✅ Performance Optimized
- Efficient layout hierarchies
- Proper use of `layout_weight` for flexible sizing
- Background images with `scaleType="centerCrop"`

---

## 🐛 Troubleshooting

### Issue: Fonts not displaying
**Solution:** Ensure `res/font/` directory exists and contains:
- `inclusive_sans.xml`
- `inclusive_sans_regular.ttf`
- `inclusive_sans_bold.ttf`

### Issue: Background image not showing
**Solution:** Verify `padariabackground_login.jpg` is in `res/drawable/` (not `res/images/`)

### Issue: Button cut off at bottom
**Solution:** Layout uses flexible spacer View with `layout_weight="1"` to prevent cutoff. Check that parent LinearLayout has `android:gravity="center_horizontal"` (NOT "center")

### Issue: Overlay not visible
**Solution:** Overlay uses `#80663F07` (50% opacity). If still not visible, increase alpha value (e.g., `#B3663F07` for 70%)

### Issue: Layout not switching on device rotation
**Solution:** Ensure activities in AndroidManifest.xml have `android:screenOrientation="unspecified"` (NOT "portrait" or "landscape")

---

## 📝 Customization Guide

### Change Color Scheme
Edit `res/values/colors.xml`:
```xml
<color name="caramel_background">#YOUR_COLOR</color>
<color name="deep_brown">#YOUR_COLOR</color>
```

### Change Font
Replace TTF files in `res/font/` and update `inclusive_sans.xml` font family

### Add New Products
1. Add string to `strings.xml`: `<string name="product_name">DISPLAY NAME</string>`
2. Copy existing CardView block in layout XML
3. Update IDs, row/column positions, and text reference
4. Add click handler in Activity.kt file

### Modify Grid Layout
**Tablet:** Edit `columnCount` in GridLayout (currently 3 for croissants, 3 for pães)  
**Portrait:** Edit `columnCount` (currently 2) and `rowCount`

---

## 📞 Support & Contact

**For Backend Team:**
- All TODO sections are clearly marked in Activity files
- Component IDs are consistent across all layouts
- Test data/toasts already implemented for UI testing

**For Frontend Issues:**
- Check this README troubleshooting section
- Verify all files copied correctly to proper directories
- Ensure Gradle sync completed successfully

---

## ✨ Credits

**UI Design & Implementation:** Joseph  
**Font:** Inclusive Sans (Google Fonts)  
**Color Palette:** Custom NataBase Bakery Theme  
**Framework:** Android Material3

---

## 📄 License

Internal use for NataBase group project. All rights reserved.

---

**Last Updated:** October 14, 2025  
**Package Status:** ✅ Production Ready - Awaiting Backend Integration

🥐 Happy Coding! 🍞
