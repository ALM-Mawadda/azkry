import SwiftData
import SwiftUI

struct AdhkarLibraryView: View {
    @Environment(\.modelContext) private var modelContext
    @State private var viewModel = AdhkarLibraryViewModel()

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                if let errorMessage = viewModel.errorMessage {
                    InlineErrorView(message: errorMessage)
                }

                ForEach(viewModel.categories, id: \.key) { category in
                    NavigationLink(value: MainDestination.dhikrCategory(category.key)) {
                        AdhkarCategoryCard(category: category)
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel("\(category.title)، \(category.items.count) ذكر ودعاء")
                }
            }
            .padding(AzkryTheme.Layout.pageHorizontalPadding)
        }
        .background(AzkryColors.background)
        .navigationTitle("الأذكار والأدعية")
        .task { viewModel.load(in: modelContext) }
    }
}

struct ExclusiveAdhkarView: View {
    @Environment(\.modelContext) private var modelContext
    @State private var viewModel = AdhkarLibraryViewModel()

    private var categories: [DhikrCategory] {
        viewModel.categories.filter { $0.key.hasPrefix("x_") }
    }

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                if let errorMessage = viewModel.errorMessage {
                    InlineErrorView(message: errorMessage)
                }

                ForEach(categories, id: \.key) { category in
                    NavigationLink(value: MainDestination.dhikrCategory(category.key)) {
                        AdhkarCategoryCard(category: category, highlighted: true)
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(
                        "\(category.title)، \(category.items.count) ذكر ودعاء"
                    )
                }
            }
            .padding(AzkryTheme.Layout.pageHorizontalPadding)
        }
        .background(AzkryColors.background)
        .navigationTitle("حصريات")
        .task { viewModel.load(in: modelContext, includeExclusive: true) }
    }
}

private struct AdhkarCategoryCard: View {
    let category: DhikrCategory
    var highlighted = false

    var body: some View {
        HStack(spacing: 14) {
            Image(systemName: category.iconKey.systemImageForDhikr)
                .font(.system(size: 20, weight: .medium))
                .frame(width: 46, height: 46)
                .foregroundStyle(highlighted ? AzkryColors.yellow : AzkryColors.textPrimary)
                .background(
                    highlighted
                        ? AzkryColors.yellow.opacity(0.12)
                        : AzkryColors.surfaceElevated,
                    in: .rect(cornerRadius: 15)
                )

            VStack(alignment: .leading, spacing: 5) {
                Text(category.title)
                    .font(AzkryTypography.headline)
                Text("\(category.items.count) ذكر ودعاء")
                    .font(AzkryTypography.caption)
                    .foregroundStyle(AzkryColors.textSecondary)
            }

            Spacer(minLength: 8)
            Image(systemName: "chevron.left")
                .foregroundStyle(AzkryColors.textSecondary)
        }
        .foregroundStyle(AzkryColors.textPrimary)
        .padding(16)
        .background(AzkryColors.surface, in: .rect(cornerRadius: AzkryTheme.Radius.card))
        .overlay {
            RoundedRectangle(cornerRadius: AzkryTheme.Radius.card)
                .stroke(AzkryColors.border, lineWidth: 0.75)
        }
    }
}

private extension String {
    var systemImageForDhikr: String {
        switch self {
        case "morning": "sunrise.fill"
        case "evening": "sunset.fill"
        case "sleep": "moon.stars.fill"
        case "waking": "alarm.fill"
        case "prayer", "after_prayer": "hands.sparkles.fill"
        case "quran": "book.closed.fill"
        case "kaaba": "building.columns.fill"
        case "family", "kids": "person.2.fill"
        case "tasbih": "circle.grid.cross.fill"
        default: "sparkles"
        }
    }
}
