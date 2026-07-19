import SwiftUI

enum AzkryColors {
    // MARK: - Page and surface
    static let background = Color(
        light: Color(hex: "F6F7FA"),
        dark: Color(hex: "080B15")
    )
    static let surface = Color(
        light: Color(hex: "FFFFFF"),
        dark: Color(hex: "101522")
    )
    static let surfaceElevated = Color(
        light: Color(hex: "F0F2F7"),
        dark: Color(hex: "171D2C")
    )
    static let mushafBackground = Color(
        light: Color(hex: "FBF7EF"),
        dark: Color(hex: "0B0F14")
    )

    // MARK: - Text and borders
    static let textPrimary = Color(
        light: Color(hex: "111827"),
        dark: Color(hex: "F2F5F8")
    )
    static let textSecondary = Color(
        light: Color(hex: "667085"),
        dark: Color(hex: "A7B2C3")
    )
    static let textOnSky = Color(hex: "F2F5F8")
    static let textOnSkySecondary = Color(hex: "C9D4E2")
    static let border = Color(
        light: Color(hex: "DDE2EA"),
        dark: Color(hex: "273044")
    )

    // MARK: - Accents
    static let indigo = Color(hex: "5867DF")
    static let blue = Color(hex: "4F8EDC")
    static let green = Color(hex: "4FA784")
    static let yellow = Color(hex: "F3C95C")
    static let danger = Color(hex: "DF6670")

    // MARK: - Home header
    static let prayerStrip = Color(hex: "2C3150")
    static let prayerStripOutline = Color.white.opacity(0.28)
    static let nightTop = Color(hex: "090C1A")
    static let nightBottom = Color(hex: "242A4B")
    static let dawnTop = Color(hex: "18264C")
    static let dawnBottom = Color(hex: "C98269")
    static let dayTop = Color(hex: "3E6FB4")
    static let dayBottom = Color(hex: "7FA3D2")
    static let afternoonTop = Color(hex: "365E99")
    static let afternoonBottom = Color(hex: "C89467")
    static let duskTop = Color(hex: "252A59")
    static let duskBottom = Color(hex: "A35E70")
}
