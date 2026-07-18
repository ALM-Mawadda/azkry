package com.azkry.app.features.pages.services

import com.azkry.app.features.pages.models.AzkryPage
import com.azkry.app.features.pages.models.PageKey
import com.azkry.app.features.pages.models.PageSection

/** Content source for the صفحات hub and its topic pages. */
interface PagesService {
    fun pages(): List<AzkryPage>

    fun page(key: PageKey): AzkryPage?

    /** The سيد الاستغفار card shown inline at the bottom of the hub. */
    fun sayyidIstighfar(): PageSection
}
