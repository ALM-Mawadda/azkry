import SwiftData
import SwiftUI

struct DhikrReaderView: View {
    let categoryKey: String

    @Environment(\.modelContext) private var modelContext
    @State private var viewModel = DhikrReaderViewModel()

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 14) {
                if let errorMessage = viewModel.errorMessage {
                    InlineErrorView(message: errorMessage)
                }

                ForEach(viewModel.orderedItems, id: \.stableKey) { dhikr in
                    DhikrReadingCard(
                        dhikr: dhikr,
                        count: viewModel.counts[dhikr.stableKey] ?? 0,
                        isFavorite: viewModel.favoriteKeys.contains(dhikr.stableKey),
                        onCount: { viewModel.increment(dhikr, in: modelContext) },
                        onFavorite: { viewModel.toggleFavorite(dhikr, in: modelContext) }
                    )
                }
            }
            .padding(AzkryTheme.Layout.pageHorizontalPadding)
        }
        .background(AzkryColors.background)
        .navigationTitle(viewModel.category?.title ?? "الأذكار")
        .navigationBarTitleDisplayMode(.inline)
        .task { viewModel.load(categoryKey: categoryKey, in: modelContext) }
    }
}

private struct DhikrReadingCard: View {
    let dhikr: Dhikr
    let count: Int
    let isFavorite: Bool
    let onCount: () -> Void
    let onFavorite: () -> Void

    private var isComplete: Bool { count >= dhikr.repeatCount }

    var body: some View {
        VStack(alignment: .trailing, spacing: 14) {
            HStack(alignment: .top) {
                Button(action: onFavorite) {
                    Image(systemName: isFavorite ? "heart.fill" : "heart")
                        .foregroundStyle(isFavorite ? AzkryColors.danger : AzkryColors.textSecondary)
                        .frame(
                            width: AzkryTheme.Layout.minimumTapTarget,
                            height: AzkryTheme.Layout.minimumTapTarget
                        )
                }
                .accessibilityLabel(isFavorite ? "إزالة من المفضلة" : "إضافة إلى المفضلة")

                Spacer()

                if let title = dhikr.title, !title.isEmpty {
                    Text(title)
                        .font(AzkryTypography.headline)
                        .foregroundStyle(AzkryColors.textSecondary)
                        .multilineTextAlignment(.trailing)
                }
            }

            Text(dhikr.text)
                .font(AzkryTypography.dhikr)
                .foregroundStyle(AzkryColors.textPrimary)
                .multilineTextAlignment(.trailing)
                .frame(maxWidth: .infinity, alignment: .trailing)

            if let virtue = dhikr.virtue, !virtue.isEmpty {
                Text(virtue)
                    .font(AzkryTypography.callout)
                    .foregroundStyle(AzkryColors.green)
                    .frame(maxWidth: .infinity, alignment: .trailing)
            }

            if let source = dhikr.source, !source.isEmpty {
                Text(source)
                    .font(AzkryTypography.caption)
                    .foregroundStyle(AzkryColors.textSecondary)
                    .frame(maxWidth: .infinity, alignment: .trailing)
            }

            Button(action: onCount) {
                HStack {
                    Text(isComplete ? "تمّ" : "اضغط للعد")
                    Spacer()
                    Text("\(count) / \(dhikr.repeatCount)")
                        .environment(\.layoutDirection, .leftToRight)
                }
                .font(AzkryTypography.headline)
                .foregroundStyle(isComplete ? AzkryColors.green : AzkryColors.textPrimary)
                .padding(.horizontal, 16)
                .frame(minHeight: 52)
                .background(AzkryColors.surfaceElevated, in: .capsule)
            }
            .buttonStyle(.plain)
            .disabled(isComplete)
            .accessibilityLabel("\(count) من \(dhikr.repeatCount)")
            .accessibilityHint(isComplete ? "اكتمل الذكر" : "يزيد العداد مرة واحدة")
        }
        .padding(18)
        .background(AzkryColors.surface, in: .rect(cornerRadius: AzkryTheme.Radius.card))
        .overlay {
            RoundedRectangle(cornerRadius: AzkryTheme.Radius.card)
                .stroke(isComplete ? AzkryColors.green.opacity(0.6) : AzkryColors.border, lineWidth: 1)
        }
    }
}
