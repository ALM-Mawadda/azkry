import SwiftData
import SwiftUI

struct FavoritesView: View {
    var embedded = false

    @Environment(\.modelContext) private var modelContext
    @State private var viewModel = FavoritesViewModel()

    var body: some View {
        Group {
            if let errorMessage = viewModel.errorMessage {
                InlineErrorView(message: errorMessage)
                    .padding()
            } else if viewModel.items.isEmpty {
                ContentUnavailableView(
                    "لا توجد مفضلة بعد",
                    systemImage: "heart",
                    description: Text("أضف الأدعية التي تريد الرجوع إليها بسرعة.")
                )
            } else {
                ScrollView {
                    LazyVStack(spacing: 10) {
                        ForEach(viewModel.items, id: \.stableKey) { item in
                            NavigationLink(
                                value: MainDestination.dhikrCategory(item.category?.key ?? "misc")
                            ) {
                                VStack(alignment: .trailing, spacing: 8) {
                                    Text(item.title ?? item.category?.title ?? "ذكر")
                                        .font(AzkryTypography.headline)
                                    Text(item.text)
                                        .font(AzkryTypography.dhikr)
                                        .lineLimit(3)
                                        .multilineTextAlignment(.trailing)
                                }
                                .foregroundStyle(AzkryColors.textPrimary)
                                .frame(maxWidth: .infinity, alignment: .trailing)
                                .padding(16)
                                .background(
                                    AzkryColors.surface,
                                    in: .rect(cornerRadius: AzkryTheme.Radius.card)
                                )
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(AzkryTheme.Layout.pageHorizontalPadding)
                }
            }
        }
        .frame(maxWidth: .infinity, minHeight: embedded ? 500 : nil)
        .background(AzkryColors.background)
        .navigationTitle(embedded ? "" : "المفضلة")
        .task { viewModel.load(in: modelContext) }
    }
}
