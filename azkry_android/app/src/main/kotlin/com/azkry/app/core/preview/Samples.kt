package com.azkry.app.core.preview

import com.azkry.app.core.models.Dhikr
import com.azkry.app.core.models.DhikrCategory
import com.azkry.app.core.models.DhikrCategoryKeys

/**
 * In-memory fixtures for `@Preview` composables and unit tests. Production
 * code must never reference this object.
 */
object Samples {
    val morningCategory = DhikrCategory(
        id = 1,
        key = DhikrCategoryKeys.MORNING,
        title = "أذكار الصباح",
        iconKey = "sun",
        sortOrder = 0,
    )

    val eveningCategory = DhikrCategory(
        id = 2,
        key = DhikrCategoryKeys.EVENING,
        title = "أذكار المساء",
        iconKey = "moon",
        sortOrder = 1,
    )

    val categories = listOf(morningCategory, eveningCategory)

    val ayatAlKursi = Dhikr(
        id = 1,
        categoryId = morningCategory.id,
        text = "الله لا إله إلا هو الحي القيوم، لا تأخذه سنة ولا نوم، له ما في السماوات وما في الأرض.",
        repeatCount = 1,
        source = "آية الكرسي",
        sortOrder = 0,
    )

    val tasbihDhikr = Dhikr(
        id = 2,
        categoryId = morningCategory.id,
        text = "سبحان الله وبحمده.",
        repeatCount = 100,
        source = "رواه مسلم",
        sortOrder = 1,
    )

    val longSourcelessDhikr = Dhikr(
        id = 3,
        categoryId = morningCategory.id,
        text = "اللهم إني أسألك العفو والعافية في الدنيا والآخرة، اللهم إني أسألك العفو والعافية في ديني ودنياي وأهلي ومالي، اللهم استر عوراتي وآمن روعاتي.",
        repeatCount = 1,
        source = null,
        sortOrder = 2,
    )

    val adhkar = listOf(ayatAlKursi, tasbihDhikr, longSourcelessDhikr)
}
