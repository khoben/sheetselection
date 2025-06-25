// Copied from androidx.activity package (v1.8.1) and adapted to use with window
// https://github.com/androidx/androidx/blob/b9da304caa28daca10b49a92b060d54d5164447a/activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt

package com.khoben.sheetselection

import android.app.UiModeManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Parcelable
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.parcelize.Parcelize

// The light scrim color used in the platform API 29+
// https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/com/android/internal/policy/DecorView.java;drc=6ef0f022c333385dba2c294e35b8de544455bf19;l=142
@VisibleForTesting
internal val DefaultLightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

// The dark scrim color used in the platform.
// https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/res/res/color/system_bar_background_semi_transparent.xml
// https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/res/remote_color_resources_res/values/colors.xml;l=67
@VisibleForTesting
internal val DefaultDarkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)

private var Impl: EdgeToEdgeImpl? = null

/**
 * Enables the edge-to-edge display for this [Window].
 *
 * To set it up with the default style, call this method:
 * ```
 * window?.enableEdgeToEdge()
 * ```
 *
 * The default style configures the system bars with a transparent background when contrast can be
 * enforced by the system (API 29 or above). On older platforms (which only have 3-button/2-button
 * navigation modes), an equivalent scrim is applied to ensure contrast with the system bars.
 *
 * See [EdgeToEdgeConfig] for more customization options.
 *
 * @param config The [EdgeToEdgeConfig] for the status bar and the navigation bar.
 */
@JvmName("enable")
@JvmOverloads
fun Window.enableEdgeToEdge(config: EdgeToEdgeConfig = EdgeToEdgeConfig()) {
    val view = decorView
    val statusBarIsDark = config.statusBarStyle.detectDarkMode(view.resources)
    val navigationBarIsDark = config.navigationBarStyle.detectDarkMode(view.resources)
    val impl = Impl ?: if (Build.VERSION.SDK_INT >= 29) {
        EdgeToEdgeApi29()
    } else if (Build.VERSION.SDK_INT >= 26) {
        EdgeToEdgeApi26()
    } else if (Build.VERSION.SDK_INT >= 23) {
        EdgeToEdgeApi23()
    } else if (Build.VERSION.SDK_INT >= 21) {
        EdgeToEdgeApi21()
    } else {
        EdgeToEdgeBase()
    }.also { Impl = it }
    impl.setUp(
        config.statusBarStyle,
        config.navigationBarStyle,
        this,
        view,
        statusBarIsDark,
        navigationBarIsDark
    )
}

/**
 * @param statusBarStyle The [WindowSystemBarStyle] for the status bar.
 * @param navigationBarStyle The [WindowSystemBarStyle] for the navigation bar.
 */
@Parcelize
class EdgeToEdgeConfig(
    internal val statusBarStyle: WindowSystemBarStyle = WindowSystemBarStyle.auto(
        Color.TRANSPARENT,
        Color.TRANSPARENT
    ),
    internal val navigationBarStyle: WindowSystemBarStyle = WindowSystemBarStyle.auto(
        DefaultLightScrim,
        DefaultDarkScrim
    )
) : Parcelable

/**
 * The style for the status bar or the navigation bar used in [enableEdgeToEdge].
 */
@Parcelize
class WindowSystemBarStyle private constructor(
    private val lightScrim: Int,
    internal val darkScrim: Int,
    internal val nightMode: Int,
    internal val detectDarkMode: (Resources) -> Boolean
) : Parcelable {

    companion object {

        /**
         * Creates a new instance of [WindowSystemBarStyle]. This style detects the dark mode
         * automatically.
         * - On API level 29 and above, the bar will be transparent in the gesture navigation mode.
         *   If this is used for the navigation bar, it will have the scrim automatically applied
         *   by the system in the 3-button navigation mode. _Note that neither of the specified
         *   colors are used_. If you really want a custom color on these API levels, use [dark] or
         *   [light].
         * - On API level 28 and below, the bar will be one of the specified scrim colors depending
         *   on the dark mode.
         * @param lightScrim The scrim color to be used for the background when the app is in light
         * mode.
         * @param darkScrim The scrim color to be used for the background when the app is in dark
         * mode. This is also used on devices where the system icon color is always light.
         * @param detectDarkMode Optional. Detects whether UI currently uses dark mode or not. The
         * default implementation can detect any of the standard dark mode features from the
         * platform, appcompat, and Jetpack Compose.
         *
         * **!! Please annotate your custom implementation** with `@JvmSerializableLambda`.
         * This will allow serialization to work with Kotlin 2.0+.
         */
        @JvmStatic
        @JvmOverloads
        fun auto(
            @ColorInt lightScrim: Int,
            @ColorInt darkScrim: Int,
            detectDarkMode: (Resources) -> Boolean = @JvmSerializableLambda { resources ->
                (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                        Configuration.UI_MODE_NIGHT_YES
            }
        ): WindowSystemBarStyle {
            return WindowSystemBarStyle(
                lightScrim = lightScrim,
                darkScrim = darkScrim,
                nightMode = UiModeManager.MODE_NIGHT_AUTO,
                detectDarkMode = detectDarkMode
            )
        }

        /**
         * Creates a new instance of [WindowSystemBarStyle]. This style consistently applies the specified
         * scrim color regardless of the system navigation mode.
         *
         * @param scrim The scrim color to be used for the background. It is expected to be dark
         * for the contrast against the light system icons.
         */
        @JvmStatic
        fun dark(@ColorInt scrim: Int): WindowSystemBarStyle {
            return WindowSystemBarStyle(
                lightScrim = scrim,
                darkScrim = scrim,
                nightMode = UiModeManager.MODE_NIGHT_YES,
                detectDarkMode = @JvmSerializableLambda { _ -> true }
            )
        }

        /**
         * Creates a new instance of [WindowSystemBarStyle]. This style consistently applies the specified
         * scrim color regardless of the system navigation mode.
         *
         * @param scrim The scrim color to be used for the background. It is expected to be light
         * for the contrast against the dark system icons.
         * @param darkScrim The scrim color to be used for the background on devices where the
         * system icon color is always light. It is expected to be dark.
         */
        @JvmStatic
        fun light(@ColorInt scrim: Int, @ColorInt darkScrim: Int): WindowSystemBarStyle {
            return WindowSystemBarStyle(
                lightScrim = scrim,
                darkScrim = darkScrim,
                nightMode = UiModeManager.MODE_NIGHT_NO,
                detectDarkMode = @JvmSerializableLambda { _ -> false }
            )
        }
    }

    internal fun getScrim(isDark: Boolean) = if (isDark) darkScrim else lightScrim

    internal fun getScrimWithEnforcedContrast(isDark: Boolean): Int {
        return when {
            nightMode == UiModeManager.MODE_NIGHT_AUTO -> Color.TRANSPARENT
            isDark -> darkScrim
            else -> lightScrim
        }
    }
}

private interface EdgeToEdgeImpl {

    fun setUp(
        statusBarStyle: WindowSystemBarStyle,
        navigationBarStyle: WindowSystemBarStyle,
        window: Window,
        view: View,
        statusBarIsDark: Boolean,
        navigationBarIsDark: Boolean
    )
}

private class EdgeToEdgeBase : EdgeToEdgeImpl {

    override fun setUp(
        statusBarStyle: WindowSystemBarStyle,
        navigationBarStyle: WindowSystemBarStyle,
        window: Window,
        view: View,
        statusBarIsDark: Boolean,
        navigationBarIsDark: Boolean
    ) {
        // No edge-to-edge before SDK 21.
    }
}

@RequiresApi(21)
private class EdgeToEdgeApi21 : EdgeToEdgeImpl {

    @Suppress("DEPRECATION")
    @DoNotInline
    override fun setUp(
        statusBarStyle: WindowSystemBarStyle,
        navigationBarStyle: WindowSystemBarStyle,
        window: Window,
        view: View,
        statusBarIsDark: Boolean,
        navigationBarIsDark: Boolean
    ) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    }
}

@RequiresApi(23)
private class EdgeToEdgeApi23 : EdgeToEdgeImpl {

    @DoNotInline
    override fun setUp(
        statusBarStyle: WindowSystemBarStyle,
        navigationBarStyle: WindowSystemBarStyle,
        window: Window,
        view: View,
        statusBarIsDark: Boolean,
        navigationBarIsDark: Boolean
    ) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = statusBarStyle.getScrim(statusBarIsDark)
        window.navigationBarColor = navigationBarStyle.darkScrim
        WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = !statusBarIsDark
    }
}

@RequiresApi(26)
private class EdgeToEdgeApi26 : EdgeToEdgeImpl {

    @DoNotInline
    override fun setUp(
        statusBarStyle: WindowSystemBarStyle,
        navigationBarStyle: WindowSystemBarStyle,
        window: Window,
        view: View,
        statusBarIsDark: Boolean,
        navigationBarIsDark: Boolean
    ) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = statusBarStyle.getScrim(statusBarIsDark)
        window.navigationBarColor = navigationBarStyle.getScrim(navigationBarIsDark)
        WindowInsetsControllerCompat(window, view).run {
            isAppearanceLightStatusBars = !statusBarIsDark
            isAppearanceLightNavigationBars = !navigationBarIsDark
        }
    }
}

@RequiresApi(29)
private class EdgeToEdgeApi29 : EdgeToEdgeImpl {

    @DoNotInline
    override fun setUp(
        statusBarStyle: WindowSystemBarStyle,
        navigationBarStyle: WindowSystemBarStyle,
        window: Window,
        view: View,
        statusBarIsDark: Boolean,
        navigationBarIsDark: Boolean
    ) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = statusBarStyle.getScrimWithEnforcedContrast(statusBarIsDark)
        window.navigationBarColor =
            navigationBarStyle.getScrimWithEnforcedContrast(navigationBarIsDark)
        window.isStatusBarContrastEnforced = false
        window.isNavigationBarContrastEnforced =
            navigationBarStyle.nightMode == UiModeManager.MODE_NIGHT_AUTO
        WindowInsetsControllerCompat(window, view).run {
            isAppearanceLightStatusBars = !statusBarIsDark
            isAppearanceLightNavigationBars = !navigationBarIsDark
        }
    }
}
