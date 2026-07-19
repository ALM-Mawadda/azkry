import SwiftUI

enum AzkryTypography {
    static let largeTitle = Font.custom("Almarai-ExtraBold", size: 31, relativeTo: .largeTitle)
    static let title = Font.custom("Almarai-Bold", size: 24, relativeTo: .title2)
    static let title2 = Font.custom("Almarai-Bold", size: 20, relativeTo: .title3)
    static let headline = Font.custom("Almarai-Bold", size: 16, relativeTo: .headline)
    static let body = Font.custom("Almarai-Regular", size: 16, relativeTo: .body)
    static let callout = Font.custom("Almarai-Regular", size: 14, relativeTo: .callout)
    static let caption = Font.custom("Almarai-Regular", size: 12, relativeTo: .caption)
    static let dhikr = Font.custom("Amiri-Regular", size: 24, relativeTo: .title2)
    static let quran = Font.custom("AmiriQuran-Regular", size: 25, relativeTo: .title2)
    static let wordmark = Font.custom("Amiri-Regular", size: 30, relativeTo: .title)
}
