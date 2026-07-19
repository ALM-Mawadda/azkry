import Testing
@testable import Azkry

@Suite("Core/ArabicNormalization")
struct ArabicNormalizationTests {
    @Test("Search ignores tashkeel and common letter variants")
    func normalization() {
        #expect(ArabicNormalization.contains("أَذْكَارُ الصَّبَاحِ", query: "اذكار الصباح"))
        #expect(ArabicNormalization.contains("رَحْمَة", query: "رحمه"))
        #expect(ArabicNormalization.contains("هُدَى", query: "هدي"))
        #expect(!ArabicNormalization.contains("المساء", query: "الصباح"))
    }
}
