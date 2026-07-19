package com.azkry.app.core.utilities

private const val ARABIC_INDIC_DIGITS = "٠١٢٣٤٥٦٧٨٩"

fun Int.toArabicIndicDigits(): String =
    toString().map { character ->
        if (character.isDigit()) ARABIC_INDIC_DIGITS[character - '0'] else character
    }.joinToString("")
