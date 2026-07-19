import Foundation
import Observation

@MainActor
@Observable
final class AppState {
    var path: [MainDestination] = []

    init() {
        #if DEBUG
        switch ProcessInfo.processInfo.environment["AZKRY_DEBUG_DESTINATION"] {
        case "mushaf":
            path = [.mushaf]
        case "reader":
            path = [.surah(SurahOpenRequest(surahNumber: 1))]
        case "search":
            path = [.search]
        case "adhkar":
            path = [.adhkar]
        case "exclusive":
            path = [.exclusive]
        case "qibla":
            path = [.qibla]
        default:
            break
        }
        #endif
    }
}
