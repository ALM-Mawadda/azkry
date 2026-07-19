# Hilt, Room, Kotlinx Serialization, coroutines, and Compose publish their own
# consumer rules. The app uses generated adapters/components directly, so no
# project package needs to be exempted wholesale from shrinking or obfuscation.
# Persisted enum identifiers are explicit keys in source rather than class or
# field names, and therefore remain stable in minified builds.
