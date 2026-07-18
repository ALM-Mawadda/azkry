package com.azkry.app.core.utilities

private val DIACRITICS = Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")

/**
 * Normalizes Arabic for matching: strips diacritics/Quranic marks, unifies
 * alef and taa-marbuta variants. Search compares normalized forms so typing
 * plain letters matches fully vocalized content.
 */
fun String.normalizeArabic(): String =
    replace(DIACRITICS, "")
        .replace('أ', 'ا')
        .replace('إ', 'ا')
        .replace('آ', 'ا')
        .replace('ٱ', 'ا')
        .replace('ى', 'ي')
        .replace('ة', 'ه')
        .replace("ـ", "")

fun String.containsArabic(query: String): Boolean =
    normalizeArabic().contains(query.normalizeArabic())
