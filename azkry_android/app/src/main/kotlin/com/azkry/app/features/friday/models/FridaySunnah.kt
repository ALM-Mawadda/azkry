package com.azkry.app.features.friday.models

import androidx.annotation.StringRes
import com.azkry.app.R
import com.azkry.app.features.tracking.models.WorshipTask

/**
 * The Friday sunan checklist. [Kahf] deliberately maps onto the tracking
 * task's key so checking it here also counts in worship tracking; the others
 * use their own keys, which the scoring ignores.
 */
enum class FridaySunnah(@param:StringRes val titleRes: Int) {
    Ghusl(R.string.friday_ghusl),
    Perfume(R.string.friday_perfume),
    BestClothes(R.string.friday_best_clothes),
    EarlyMosque(R.string.friday_early_mosque),
    Dua(R.string.friday_dua),
    Kahf(R.string.friday_kahf),
    ;

    val taskKey: String
        get() = if (this == Kahf) WorshipTask.Kahf.name else "Friday$name"
}
