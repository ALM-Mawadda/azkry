package com.azkry.app.core.i18n

import android.app.LocaleManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.azkry.app.app.AppLanguage
import java.util.Locale

object AppLocale {
    fun localeTagFor(language: AppLanguage): String? = language.code

    fun applicationLocaleTagsFor(language: AppLanguage): String =
        localeTagFor(language).orEmpty()

    fun localeFor(language: AppLanguage): Locale? =
        localeTagFor(language)?.let(Locale::forLanguageTag)

    /**
     * Returns a context whose `getResources()` is locked to [language] but
     * whose `baseContext` is preserved. Wrapping (rather than returning the
     * raw `createConfigurationContext` result) keeps Hilt's activity-context
     * walk intact — `HiltViewModelFactory.create` requires
     * `ContextWrapper.baseContext -> Activity` to resolve, which a plain
     * `ContextImpl` from `createConfigurationContext` would break.
     */
    fun localizedContext(context: Context, language: AppLanguage): Context {
        val locale = localeFor(language) ?: return context
        return LocalizedContext(context, locale)
    }

    fun applyPlatformAppLocale(context: Context, language: AppLanguage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags(applicationLocaleTagsFor(language))
        }
    }
}

private class LocalizedContext(base: Context, locale: Locale) : ContextWrapper(base) {
    private val localizedResources: Resources = run {
        val configuration = Configuration(base.resources.configuration).apply {
            setLocales(LocaleList(locale))
            setLayoutDirection(locale)
        }
        base.createConfigurationContext(configuration).resources
    }

    override fun getResources(): Resources = localizedResources
}

@Composable
fun ProvideAppLocale(
    language: AppLanguage,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val systemConfiguration = LocalConfiguration.current
    val localizedContext = remember(context, language, systemConfiguration) {
        AppLocale.localizedContext(context, language)
    }
    val localizedConfiguration = localizedContext.resources.configuration
    val layoutDirection = remember(localizedConfiguration) {
        if (localizedConfiguration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            LayoutDirection.Rtl
        } else {
            LayoutDirection.Ltr
        }
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration,
        LocalLayoutDirection provides layoutDirection,
        content = content,
    )
}
