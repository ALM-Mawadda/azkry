enum FridaySunnah: String, CaseIterable, Identifiable, Sendable {
    case ghusl
    case perfume
    case bestClothes
    case earlyMosque
    case dua
    case kahf

    var id: String { rawValue }

    var title: String {
        switch self {
        case .ghusl: "الاغتسال"
        case .perfume: "التطيّب"
        case .bestClothes: "أحسن الثياب"
        case .earlyMosque: "التبكير للمسجد"
        case .dua: "تحرّي ساعة الإجابة"
        case .kahf: "قراءة سورة الكهف"
        }
    }
}
