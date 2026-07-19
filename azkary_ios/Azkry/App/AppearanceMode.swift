import SwiftUI

enum AppearanceMode: Int, CaseIterable, Identifiable {
    case system
    case light
    case dark

    var id: Int { rawValue }

    var colorScheme: ColorScheme? {
        switch self {
        case .system: nil
        case .light: .light
        case .dark: .dark
        }
    }
}
