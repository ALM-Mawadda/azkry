package com.azkry.app.features.pages.viewmodels

import androidx.lifecycle.ViewModel
import com.azkry.app.features.pages.models.AzkryPage
import com.azkry.app.features.pages.models.PageKey
import com.azkry.app.features.pages.models.PageSection
import com.azkry.app.features.pages.services.PagesService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Backs both the صفحات hub list and the individual topic pages. */
@HiltViewModel
class PagesViewModel @Inject constructor(
    private val pagesService: PagesService,
) : ViewModel() {
    val pages: List<AzkryPage> = pagesService.pages()

    val sayyidIstighfar: PageSection = pagesService.sayyidIstighfar()

    fun page(key: PageKey): AzkryPage? = pagesService.page(key)
}
