import SwiftData
import SwiftUI

struct AdhkarSearchView: View {
    @Environment(\.modelContext) private var modelContext
    @State private var viewModel = AdhkarSearchViewModel()

    var body: some View {
        @Bindable var viewModel = viewModel

        Group {
            if let errorMessage = viewModel.errorMessage {
                InlineErrorView(message: errorMessage)
                    .padding()
            } else if viewModel.query.isEmpty {
                ContentUnavailableView(
                    "ابحث في أذكاري والمصحف",
                    systemImage: "magnifyingglass",
                    description: Text("اكتب كلمة من الدعاء أو المصدر أو اسم السورة.")
                )
            } else if !viewModel.hasResults {
                ContentUnavailableView.search(text: viewModel.query)
            } else {
                List {
                    if !viewModel.surahResults.isEmpty {
                        Section("سور القرآن") {
                            ForEach(viewModel.surahResults) { surah in
                                NavigationLink(
                                    value: MainDestination.surah(
                                        SurahOpenRequest(surahNumber: surah.number)
                                    )
                                ) {
                                    HStack(spacing: 12) {
                                        Text("\(surah.number)")
                                            .font(AzkryTypography.caption)
                                            .frame(width: 36, height: 36)
                                            .background(AzkryColors.surfaceElevated, in: .circle)
                                        Spacer()
                                        VStack(alignment: .trailing, spacing: 4) {
                                            Text(surah.name)
                                                .font(AzkryTypography.quran)
                                            Text("\(surah.ayahCount) آيات · صفحة \(surah.page)")
                                                .font(AzkryTypography.caption)
                                                .foregroundStyle(AzkryColors.textSecondary)
                                        }
                                    }
                                }
                                .listRowBackground(AzkryColors.surface)
                            }
                        }
                    }

                    if !viewModel.results.isEmpty {
                        Section("الأذكار والأدعية") {
                            ForEach(viewModel.results, id: \.stableKey) { item in
                                NavigationLink(
                                    value: MainDestination.dhikrCategory(
                                        item.category?.key ?? "misc"
                                    )
                                ) {
                                    VStack(alignment: .trailing, spacing: 6) {
                                        Text(item.title ?? item.category?.title ?? "ذكر")
                                            .font(AzkryTypography.headline)
                                        Text(item.text)
                                            .font(AzkryTypography.body)
                                            .foregroundStyle(AzkryColors.textSecondary)
                                            .lineLimit(2)
                                    }
                                }
                                .listRowBackground(AzkryColors.surface)
                            }
                        }
                    }
                }
                .scrollContentBackground(.hidden)
            }
        }
        .background(AzkryColors.background)
        .navigationTitle("البحث")
        .searchable(text: $viewModel.query, prompt: "ذكر، دعاء، أو سورة")
        .onChange(of: viewModel.query) { _, _ in viewModel.filter() }
        .task { viewModel.load(in: modelContext) }
    }
}
