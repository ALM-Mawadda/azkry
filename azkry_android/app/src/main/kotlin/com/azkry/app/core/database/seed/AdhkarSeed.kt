package com.azkry.app.core.database.seed

import android.content.Context
import com.azkry.app.core.database.AdhkarDao
import com.azkry.app.core.models.Dhikr
import com.azkry.app.core.models.DhikrCategory
import com.azkry.app.core.models.DhikrCategoryKeys

/**
 * Bundled content for the adhkar tables. The main library (11 categories,
 * 339 items) ships as assets/adhkar/athkar_seed.json; the tasbih/istighfar/hamd
 * sets and the exclusive-section categories stay Kotlin-defined below.
 *
 * Seeding is revision-driven: bumping [CONTENT_REVISION] makes the next app
 * open atomically replace every seeded category with the current bundle
 * (favorites and daily counts reset with it — they reference the old rows).
 */
object AdhkarSeed {
    /** Bump when the bundled content changes shape or text. */
    const val CONTENT_REVISION = 3

    private const val SEED_ASSET = "adhkar/athkar_seed.json"

    suspend fun seed(context: Context, dao: AdhkarDao) {
        if ((dao.seedRevision() ?: 0) >= CONTENT_REVISION) return

        val imported = context.assets.open(SEED_ASSET)
            .bufferedReader()
            .use { it.readText() }
            .let(AthkarSeedParser::parse)
            .categories

        val all = imported + extraCategories
        validate(all)
        dao.replaceSeededContent(
            categoriesWithItems = all.mapIndexed { index, category ->
                DhikrCategory(
                    key = category.key,
                    title = category.title,
                    iconKey = category.iconKey,
                    sortOrder = index,
                ) to category.items.mapIndexed { itemIndex, item ->
                    Dhikr(
                        categoryId = 0,
                        text = item.text,
                        repeatCount = item.count,
                        source = item.source,
                        sortOrder = itemIndex,
                        title = item.title,
                        virtue = item.virtue,
                        stableKey = stableDhikrKey(category.key, item, itemIndex),
                    )
                }
            },
            revision = CONTENT_REVISION,
        )
    }

    private fun validate(categories: List<SeedCategory>) {
        require(categories.map { it.key }.distinct().size == categories.size) {
            "Seed category keys must be unique"
        }
        val stableKeys = categories.flatMap { category ->
            require(category.key.isNotBlank()) { "Seed category key must not be blank" }
            category.items.mapIndexed { index, item ->
                require(item.text.isNotBlank()) { "Seed dhikr text must not be blank" }
                require(item.count > 0) { "Seed dhikr repeat count must be positive" }
                stableDhikrKey(category.key, item, index)
            }
        }
        require(stableKeys.distinct().size == stableKeys.size) {
            "Seed dhikr stable keys must be unique"
        }
    }

    private val tasbih = SeedCategory(
        key = DhikrCategoryKeys.TASBIH,
        title = "تسابيح",
        iconKey = "tasbih",
        items = listOf(
            SeedDhikr("سبحان الله وبحمده.", 100, "رواه مسلم"),
            SeedDhikr("سبحان الله والحمد لله ولا إله إلا الله والله أكبر.", 100, "رواه مسلم"),
            SeedDhikr("لا حول ولا قوة إلا بالله.", 100, "كنز من كنوز الجنة - رواه البخاري"),
            SeedDhikr(
                "لا إله إلا الله وحده لا شريك له، له الملك وله الحمد، وهو على كل شيء قدير.",
                100,
                "رواه البخاري ومسلم",
            ),
            SeedDhikr("اللهم صل على محمد وعلى آل محمد.", 100),
        ),
    )

    private val istighfar = SeedCategory(
        key = DhikrCategoryKeys.ISTIGHFAR,
        title = "الإستغفار",
        iconKey = "istighfar",
        items = listOf(
            SeedDhikr("أستغفر الله.", 100, "رواه مسلم"),
            SeedDhikr(
                "أستغفر الله العظيم الذي لا إله إلا هو الحي القيوم وأتوب إليه.",
                3,
                "رواه أبو داود",
            ),
            SeedDhikr(
                "رب اغفر لي وتب علي، إنك أنت التواب الرحيم.",
                100,
                "رواه أبو داود",
            ),
            SeedDhikr(
                "اللهم اغفر لي ذنبي كله، دقه وجله، وأوله وآخره، وعلانيته وسره.",
                1,
                "رواه مسلم",
            ),
        ),
    )

    private val hamd = SeedCategory(
        key = DhikrCategoryKeys.HAMD,
        title = "الحمد والثناء",
        iconKey = "hamd",
        items = listOf(
            SeedDhikr("الحمد لله حمدا كثيرا طيبا مباركا فيه.", 1, "رواه البخاري"),
            SeedDhikr("الحمد لله رب العالمين.", 33),
            SeedDhikr(
                "اللهم لك الحمد كله، ولك الشكر كله، وإليك يرجع الأمر كله.",
                1,
            ),
            SeedDhikr(
                "اللهم لك الحمد أنت نور السماوات والأرض ومن فيهن، ولك الحمد أنت قيم السماوات والأرض ومن فيهن.",
                1,
                "رواه البخاري",
            ),
            SeedDhikr("سبحان الله وبحمده، سبحان الله العظيم.", 10, "رواه البخاري"),
        ),
    )

    private val umrah = SeedCategory(
        key = DhikrCategoryKeys.UMRAH,
        title = "العمرة",
        iconKey = "kaaba",
        items = listOf(
            SeedDhikr(
                "لبيك اللهم لبيك، لبيك لا شريك لك لبيك، إن الحمد والنعمة لك والملك، لا شريك لك.",
                1,
                "التلبية - رواه البخاري",
            ),
            SeedDhikr(
                "اللهم أنت السلام ومنك السلام، حينا ربنا بالسلام.",
                1,
                "عند دخول الحرم",
            ),
            SeedDhikr(
                "بسم الله والله أكبر.",
                1,
                "عند استلام الحجر الأسود",
            ),
            SeedDhikr(
                "ربنا آتنا في الدنيا حسنة وفي الآخرة حسنة وقنا عذاب النار.",
                1,
                "بين الركن اليماني والحجر الأسود - رواه أبو داود",
            ),
            SeedDhikr(
                "إن الصفا والمروة من شعائر الله. أبدأ بما بدأ الله به.",
                1,
                "عند الصعود إلى الصفا - رواه مسلم",
            ),
            SeedDhikr(
                "لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير، لا إله إلا الله وحده، أنجز وعده، ونصر عبده، وهزم الأحزاب وحده.",
                3,
                "على الصفا والمروة - رواه مسلم",
            ),
        ),
    )

    private val hajj = SeedCategory(
        key = DhikrCategoryKeys.HAJJ,
        title = "الحج",
        iconKey = "kaaba",
        items = listOf(
            SeedDhikr(
                "لبيك اللهم لبيك، لبيك لا شريك لك لبيك، إن الحمد والنعمة لك والملك، لا شريك لك.",
                1,
                "التلبية - رواه البخاري",
            ),
            SeedDhikr(
                "لا إله إلا الله وحده لا شريك له، له الملك وله الحمد، وهو على كل شيء قدير.",
                1,
                "خير دعاء يوم عرفة - رواه الترمذي",
            ),
            SeedDhikr(
                "الله أكبر.",
                7,
                "عند رمي كل جمرة - رواه البخاري",
            ),
            SeedDhikr(
                "اللهم اجعله حجا مبرورا، وسعيا مشكورا، وذنبا مغفورا.",
                1,
            ),
        ),
    )

    private val lovedOnes = SeedCategory(
        key = DhikrCategoryKeys.LOVED_ONES,
        title = "أذكار الأحبة",
        iconKey = "family",
        items = listOf(
            SeedDhikr("رب اغفر لي ولوالدي ولمن دخل بيتي مؤمنا وللمؤمنين والمؤمنات.", 1, "نوح 28"),
            SeedDhikr("رب ارحمهما كما ربياني صغيرا.", 1, "الإسراء 24"),
            SeedDhikr(
                "ربنا هب لنا من أزواجنا وذرياتنا قرة أعين واجعلنا للمتقين إماما.",
                1,
                "الفرقان 74",
            ),
            SeedDhikr("رب اجعلني مقيم الصلاة ومن ذريتي، ربنا وتقبل دعاء.", 1, "إبراهيم 40"),
            SeedDhikr(
                "اللهم اغفر لحينا وميتنا، وشاهدنا وغائبنا، وصغيرنا وكبيرنا، وذكرنا وأنثانا.",
                1,
                "رواه أبو داود",
            ),
        ),
    )

    private val kids = SeedCategory(
        key = DhikrCategoryKeys.KIDS,
        title = "أذكار للصغار",
        iconKey = "kids",
        items = listOf(
            SeedDhikr("بسم الله.", 1, "قبل الطعام - رواه البخاري"),
            SeedDhikr("الحمد لله.", 1, "بعد الطعام - رواه الترمذي"),
            SeedDhikr("باسمك اللهم أموت وأحيا.", 1, "قبل النوم - رواه البخاري"),
            SeedDhikr(
                "الحمد لله الذي أحيانا بعد ما أماتنا وإليه النشور.",
                1,
                "عند الاستيقاظ - رواه البخاري",
            ),
            SeedDhikr(
                "بسم الله، توكلت على الله، ولا حول ولا قوة إلا بالله.",
                1,
                "عند الخروج من المنزل - رواه الترمذي",
            ),
            SeedDhikr(
                "سبحان الذي سخر لنا هذا وما كنا له مقرنين، وإنا إلى ربنا لمنقلبون.",
                1,
                "عند الركوب - رواه مسلم",
            ),
        ),
    )

    private val extraCategories = listOf(
        tasbih,
        hamd,
        istighfar,
        umrah,
        hajj,
        lovedOnes,
        kids,
    )
}

/**
 * The positional fallback is part of the persisted backup contract. Existing
 * fallback items must retain their position; use an explicit [SeedDhikr.key]
 * before introducing a seed revision that would otherwise move them.
 */
internal fun stableDhikrKey(categoryKey: String, item: SeedDhikr, index: Int): String {
    val itemKey = item.key ?: "item_${index + 1}"
    require(itemKey.isNotBlank() && '/' !in itemKey) { "Invalid seed dhikr key: $itemKey" }
    return "$categoryKey/$itemKey"
}
