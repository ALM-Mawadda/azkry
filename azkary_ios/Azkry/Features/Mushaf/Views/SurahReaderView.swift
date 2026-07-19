import SwiftUI

struct SurahReaderView: View {
    let request: SurahOpenRequest
    @State private var viewModel = SurahReaderViewModel()
    @State private var visiblePage: Int?

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                if let errorMessage = viewModel.errorMessage {
                    InlineErrorView(message: errorMessage)
                        .padding(.bottom, 16)
                }

                if let surah = viewModel.surah {
                    SurahHeaderBand(name: surah.name)

                    ForEach(Array(viewModel.pages.enumerated()), id: \.element.id) { index, page in
                        QuranPageSection(
                            page: page,
                            basmala: index == 0 ? viewModel.basmala : nil
                        )
                        .id(page.page)
                    }
                }
            }
            .scrollTargetLayout()
            .padding(.horizontal, AzkryTheme.Layout.pageHorizontalPadding)
            .padding(.bottom, 32)
        }
        .scrollPosition(id: $visiblePage, anchor: .top)
        .background(AzkryColors.mushafBackground)
        .navigationTitle(viewModel.surah?.name ?? "المصحف")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button(action: viewModel.toggleBookmark) {
                    Image(systemName: viewModel.isCurrentPageBookmarked
                        ? "bookmark.fill"
                        : "bookmark")
                        .foregroundStyle(
                            viewModel.isCurrentPageBookmarked
                                ? AzkryColors.yellow
                                : AzkryColors.textPrimary
                        )
                        .frame(width: 44, height: 44)
                }
                .accessibilityLabel(
                    viewModel.isCurrentPageBookmarked
                        ? "إزالة علامة القراءة"
                        : "إضافة علامة قراءة"
                )
                .accessibilityValue(
                    viewModel.currentPage.map { "الصفحة \($0)" } ?? ""
                )
            }
        }
        .task(id: request) {
            viewModel.load(request: request)
            await Task.yield()
            visiblePage = viewModel.initialPage
        }
        .onChange(of: visiblePage) { _, page in
            if let page { viewModel.pageBecameVisible(page) }
        }
    }
}

private struct SurahHeaderBand: View {
    let name: String

    var body: some View {
        HStack(spacing: 14) {
            ornament
            Text(name)
                .font(AzkryTypography.title)
                .multilineTextAlignment(.center)
                .lineLimit(1)
                .minimumScaleFactor(0.72)
            ornament
        }
        .foregroundStyle(AzkryColors.textPrimary)
        .padding(.horizontal, 18)
        .frame(maxWidth: .infinity, minHeight: 68)
        .background(AzkryColors.surface, in: .rect(cornerRadius: 20))
        .overlay {
            RoundedRectangle(cornerRadius: 20)
                .stroke(AzkryColors.yellow.opacity(0.45), lineWidth: 0.8)
        }
        .padding(.vertical, 12)
        .accessibilityAddTraits(.isHeader)
    }

    private var ornament: some View {
        Capsule()
            .fill(AzkryColors.yellow.opacity(0.36))
            .frame(maxWidth: .infinity, maxHeight: 2)
    }
}

private struct QuranPageSection: View {
    let page: QuranReaderPage
    let basmala: String?

    var body: some View {
        VStack(spacing: 14) {
            if let basmala {
                Text(basmala)
                    .font(AzkryTypography.quran)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity)
                    .padding(.top, 8)
                    .accessibilityLabel("بسم الله الرحمن الرحيم")
            }

            quranText
                .font(AzkryTypography.quran)
                .lineSpacing(10)
                .multilineTextAlignment(.trailing)
                .frame(maxWidth: .infinity, alignment: .trailing)
                .accessibilityLabel(accessibilityText)

            HStack(spacing: 10) {
                Rectangle()
                    .fill(AzkryColors.border)
                    .frame(height: 0.5)
                Text("صفحة \(page.page) · الجزء \(page.juz)")
                    .font(AzkryTypography.caption)
                    .foregroundStyle(AzkryColors.textSecondary)
                    .fixedSize(horizontal: true, vertical: false)
                Rectangle()
                    .fill(AzkryColors.border)
                    .frame(height: 0.5)
            }
            .padding(.vertical, 10)
        }
        .foregroundStyle(AzkryColors.textPrimary)
    }

    private var quranText: Text {
        var content = AttributedString()
        for (index, ayah) in page.ayahs.enumerated() {
            let spacing = index == 0 ? "" : " "
            content.append(AttributedString(spacing + ayah.text + " "))
            var marker = AttributedString("﴿\(arabicDigits(ayah.number))﴾")
            marker.foregroundColor = AzkryColors.yellow
            content.append(marker)
        }
        return Text(content)
    }

    private var accessibilityText: String {
        page.ayahs.map { "الآية \($0.number). \($0.text)" }.joined(separator: " ")
    }

    private func arabicDigits(_ value: Int) -> String {
        value.formatted(.number.locale(Locale(identifier: "ar")))
    }
}
