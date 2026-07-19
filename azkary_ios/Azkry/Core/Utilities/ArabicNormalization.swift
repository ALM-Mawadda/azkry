import Foundation

enum ArabicNormalization {
    static func normalize(_ value: String) -> String {
        let stripped = String(value.unicodeScalars.filter { scalar in
            !isArabicMark(scalar.value)
        })

        return stripped
            .replacingOccurrences(of: "إ", with: "ا")
            .replacingOccurrences(of: "أ", with: "ا")
            .replacingOccurrences(of: "آ", with: "ا")
            .replacingOccurrences(of: "ٱ", with: "ا")
            .replacingOccurrences(of: "ى", with: "ي")
            .replacingOccurrences(of: "ة", with: "ه")
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .lowercased()
    }

    static func contains(_ value: String, query: String) -> Bool {
        let normalizedQuery = normalize(query)
        return normalizedQuery.isEmpty || normalize(value).contains(normalizedQuery)
    }

    private static func isArabicMark(_ value: UInt32) -> Bool {
        (0x0610...0x061A).contains(value)
            || (0x064B...0x065F).contains(value)
            || value == 0x0670
            || (0x06D6...0x06ED).contains(value)
    }
}
