protocol FridayServiceProtocol: Sendable {
    func completed(dateKey: String) -> Set<FridaySunnah>
    func set(_ sunnah: FridaySunnah, completed: Bool, dateKey: String)
}
