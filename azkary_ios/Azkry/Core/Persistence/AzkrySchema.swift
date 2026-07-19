import Foundation
import SwiftData

enum AzkrySchemaV1: VersionedSchema {
    static let versionIdentifier = Schema.Version(1, 0, 0)
    static let models: [any PersistentModel.Type] = [
        DhikrCategory.self,
        Dhikr.self,
        DhikrDailyCount.self,
        PrayerLog.self,
        WorshipLog.self,
        FavoriteDhikr.self,
    ]
}

enum AzkryMigrationPlan: SchemaMigrationPlan {
    static let schemas: [any VersionedSchema.Type] = [AzkrySchemaV1.self]
    static let stages: [MigrationStage] = []
}

enum AzkrySchema {
    static let schema = Schema(versionedSchema: AzkrySchemaV1.self)

    static func makeModelContainer(inMemory: Bool = false) throws -> ModelContainer {
        if !inMemory,
           let applicationSupport = FileManager.default.urls(
               for: .applicationSupportDirectory,
               in: .userDomainMask
           ).first {
            try FileManager.default.createDirectory(
                at: applicationSupport,
                withIntermediateDirectories: true
            )
        }
        let configuration = ModelConfiguration(
            schema: schema,
            isStoredInMemoryOnly: inMemory
        )
        return try ModelContainer(
            for: schema,
            migrationPlan: AzkryMigrationPlan.self,
            configurations: [configuration]
        )
    }
}
