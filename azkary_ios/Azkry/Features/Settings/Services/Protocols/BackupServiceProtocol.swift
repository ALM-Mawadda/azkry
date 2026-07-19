import Foundation
import SwiftData

@MainActor
protocol BackupServiceProtocol {
    func exportData(in context: ModelContext) throws -> Data
    func restore(from url: URL, in context: ModelContext) throws
}
