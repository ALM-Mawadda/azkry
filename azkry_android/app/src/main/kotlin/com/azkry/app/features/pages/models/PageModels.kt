package com.azkry.app.features.pages.models

/** Stable identity for each topic page in the صفحات hub. */
enum class PageKey {
    AsmaulHusna,
    Friday,
    Rawatib,
    Duha,
    DuaEtiquette,
    DhikrAndDua,
    ForbiddenTimes,
    RamadanQada,
    DeceasedDuas,
    AyahTafsir,
}

/**
 * One block of a topic page. [isDhikr] bodies render in the Amiri dhikr
 * style and gain a share action; [note] is a short explanatory comment
 * shown under the body (used by آية وتفسير).
 */
data class PageSection(
    val heading: String? = null,
    val body: String,
    val source: String? = null,
    val note: String? = null,
    val isDhikr: Boolean = false,
)

/**
 * A topic page: either regular [sections], or a [names] grid (أسماء الله
 * الحسنى) optionally preceded by intro sections. The Friday entry carries
 * no content — the shell routes it to the existing Friday screen.
 */
data class AzkryPage(
    val key: PageKey,
    val title: String,
    val sections: List<PageSection> = emptyList(),
    val names: List<String> = emptyList(),
)
