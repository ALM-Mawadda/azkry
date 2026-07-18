package com.azkry.app.core.i18n

import android.content.Context
import androidx.annotation.StringRes
import com.azkry.app.core.error.UserFacingMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface StringProvider {
    fun get(@StringRes resId: Int, vararg formatArgs: Any): String
}

@Singleton
class AndroidStringProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : StringProvider {
    override fun get(resId: Int, vararg formatArgs: Any): String =
        context.getString(resId, *formatArgs)
}

object EmptyStringProvider : StringProvider {
    override fun get(resId: Int, vararg formatArgs: Any): String = ""
}

/**
 * Resolves a throwable to a message safe to show the user.
 *
 * The raw message is used only when the exception explicitly implements
 * [UserFacingMessage]; otherwise the localized [fallbackResId] is shown.
 */
fun Throwable.localizedMessageOr(
    strings: StringProvider,
    @StringRes fallbackResId: Int,
    vararg formatArgs: Any,
): String {
    if (this is UserFacingMessage) {
        message?.takeIf { it.isNotBlank() }?.let { return it }
    }
    return strings.get(fallbackResId, *formatArgs)
}
