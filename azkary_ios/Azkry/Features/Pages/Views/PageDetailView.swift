import SwiftUI

struct PageDetailView: View {
    let key: PageKey
    @State private var viewModel = PagesViewModel()

    private let columns = [GridItem(.adaptive(minimum: 118), spacing: 10)]

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 13) {
                if let page = viewModel.selectedPage {
                    ForEach(page.sections) { section in
                        PageSectionCard(section: section)
                    }

                    if !page.names.isEmpty {
                        LazyVGrid(columns: columns, spacing: 10) {
                            ForEach(Array(page.names.enumerated()), id: \.offset) { index, name in
                                VStack(spacing: 7) {
                                    Text("\(index + 1)")
                                        .font(AzkryTypography.caption)
                                        .foregroundStyle(AzkryColors.textSecondary)
                                    Text(name)
                                        .font(AzkryTypography.dhikr)
                                        .multilineTextAlignment(.center)
                                }
                                .frame(maxWidth: .infinity, minHeight: 88)
                                .padding(8)
                                .background(AzkryColors.surface, in: .rect(cornerRadius: 18))
                            }
                        }
                    }
                }
            }
            .padding(AzkryTheme.Layout.pageHorizontalPadding)
        }
        .background(AzkryColors.background)
        .navigationTitle(key.title)
        .navigationBarTitleDisplayMode(.inline)
        .task { viewModel.load(key: key) }
    }
}
