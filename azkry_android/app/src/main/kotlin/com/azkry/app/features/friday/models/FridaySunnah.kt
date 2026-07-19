package com.azkry.app.features.friday.models

import androidx.annotation.StringRes
import com.azkry.app.R
import com.azkry.app.core.models.WorshipTask

/**
 * The Friday sunan checklist. [Kahf] deliberately maps onto the tracking
 * task's key so checking it here also counts in worship tracking; the others
 * use their own keys, which the scoring ignores.
 */
enum class FridaySunnah(
    @param:StringRes val titleRes: Int,
    val taskKey: String,
) {
    Ghusl(R.string.friday_ghusl, "FridayGhusl"),
    Perfume(R.string.friday_perfume, "FridayPerfume"),
    BestClothes(R.string.friday_best_clothes, "FridayBestClothes"),
    EarlyMosque(R.string.friday_early_mosque, "FridayEarlyMosque"),
    Dua(R.string.friday_dua, "FridayDua"),
    Kahf(R.string.friday_kahf, WorshipTask.Kahf.key),
}
