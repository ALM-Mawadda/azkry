import SwiftUI

struct MushafView: View {
    private enum Tab: String, CaseIterable, Identifiable {
        case surahs = "السور"
        case juzs = "الأجزاء"
        case pages = "الصفحات"

        var id: String { rawValue }
    }

    @State private var viewModel = MushafViewModel()
    @State private var selectedTab: Tab = .surahs

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                if let errorMessage = viewModel.errorMessage {
                    InlineErrorView(message: errorMessage)
                }

                if let lastRead = viewModel.lastRead {
                    LastReadCard(lastRead: lastRead)
                }

                KhatmahCard(
                    progress: viewModel.khatmahProgress,
                    targetRequest: targetRequest,
                    onStart: viewModel.startKhatmah,
                    onFinish: viewModel.finishKhatmah
                )

                if !viewModel.bookmarks.isEmpty {
                    BookmarksRow(bookmarks: viewModel.bookmarks)
                }

                Picker("عرض المصحف", selection: $selectedTab) {
                    ForEach(Tab.allCases) { tab in
                        Text(tab.rawValue).tag(tab)
                    }
                }
                .pickerStyle(.segmented)
                .accessibilityHint("اختر عرض السور أو الأجزاء أو صفحات المصحف")

                switch selectedTab {
                case .surahs:
                    ForEach(viewModel.surahs) { surah in
                        NavigationLink(
                            value: MainDestination.surah(
                                SurahOpenRequest(surahNumber: surah.number)
                            )
                        ) {
                            SurahRow(surah: surah)
                        }
                        .buttonStyle(.plain)
                    }
                case .juzs:
                    ForEach(viewModel.juzs) { juz in
                        NavigationLink(
                            value: MainDestination.surah(SurahOpenRequest(
                                surahNumber: juz.surahNumber,
                                startAyah: juz.ayahNumber
                            ))
                        ) {
                            MushafIndexRow(
                                title: "الجزء \(juz.number)",
                                subtitle: juzSubtitle(juz),
                                badge: "\(juz.page)"
                            )
                        }
                        .buttonStyle(.plain)
                    }
                case .pages:
                    ForEach(viewModel.pages) { page in
                        NavigationLink(
                            value: MainDestination.surah(SurahOpenRequest(
                                surahNumber: page.surahNumber,
                                startPage: page.page
                            ))
                        ) {
                            MushafIndexRow(
                                title: "صفحة \(page.page)",
                                subtitle: page.surahName,
                                badge: "\(page.page)"
                            )
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
            .padding(.horizontal, AzkryTheme.Layout.pageHorizontalPadding)
            .padding(.bottom, 28)
        }
        .background(AzkryColors.mushafBackground)
        .navigationTitle("المصحف")
        .onAppear { viewModel.load() }
    }

    private var targetRequest: SurahOpenRequest? {
        guard let target = viewModel.khatmahProgress?.targetPage,
              let page = viewModel.pages.first(where: { $0.page == target }) else {
            return nil
        }
        return SurahOpenRequest(surahNumber: page.surahNumber, startPage: page.page)
    }

    private func juzSubtitle(_ juz: JuzSummary) -> String {
        let name = viewModel.surahs.first(where: { $0.number == juz.surahNumber })?.name
            ?? "السورة \(juz.surahNumber)"
        return "\(name) · الآية \(juz.ayahNumber)"
    }
}

private struct LastReadCard: View {
    let lastRead: QuranLastRead
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var body: some View {
        NavigationLink(
            value: MainDestination.surah(SurahOpenRequest(
                surahNumber: lastRead.surahNumber,
                startAyah: lastRead.ayahNumber,
                startPage: lastRead.page
            ))
        ) {
            Group {
                if dynamicTypeSize.isAccessibilitySize {
                    VStack(alignment: .trailing, spacing: 10) {
                        HStack(spacing: 10) {
                            continuationArrow
                            Spacer(minLength: 8)
                            Text("متابعة القراءة")
                                .font(AzkryTypography.caption)
                                .foregroundStyle(AzkryColors.textSecondary)
                            bookmarkIcon
                        }
                        Text(lastRead.surahName)
                            .font(AzkryTypography.title2)
                            .foregroundStyle(AzkryColors.textPrimary)
                            .frame(maxWidth: .infinity, alignment: .trailing)
                        positionText
                    }
                } else {
                    HStack(spacing: 14) {
                        continuationArrow
                        Spacer(minLength: 8)
                        VStack(alignment: .trailing, spacing: 5) {
                            Text("متابعة القراءة")
                                .font(AzkryTypography.caption)
                                .foregroundStyle(AzkryColors.textSecondary)
                            Text(lastRead.surahName)
                                .font(AzkryTypography.headline)
                                .foregroundStyle(AzkryColors.textPrimary)
                            positionText
                        }
                        bookmarkIcon
                    }
                }
            }
            .padding(16)
            .background(AzkryColors.surface, in: .rect(cornerRadius: 22))
            .overlay {
                RoundedRectangle(cornerRadius: 22)
                    .stroke(AzkryColors.border, lineWidth: 0.7)
            }
        }
        .buttonStyle(.plain)
        .accessibilityLabel(
            "متابعة القراءة، \(lastRead.surahName)، الآية \(lastRead.ayahNumber)، الصفحة \(lastRead.page)"
        )
    }

    private var continuationArrow: some View {
        Image(systemName: "arrow.left")
            .font(.system(size: 18, weight: .semibold))
            .foregroundStyle(AzkryColors.textSecondary)
            .frame(width: 44, height: 44)
    }

    private var bookmarkIcon: some View {
        Image(systemName: "bookmark.fill")
            .font(.system(size: 19, weight: .medium))
            .foregroundStyle(AzkryColors.yellow)
            .frame(width: 44, height: 44)
            .background(AzkryColors.yellow.opacity(0.12), in: .circle)
    }

    private var positionText: some View {
        Text("الآية \(lastRead.ayahNumber) · الصفحة \(lastRead.page)")
            .font(AzkryTypography.caption)
            .foregroundStyle(AzkryColors.textSecondary)
            .frame(maxWidth: .infinity, alignment: .trailing)
    }
}

private struct KhatmahCard: View {
    let progress: KhatmahProgress?
    let targetRequest: SurahOpenRequest?
    let onStart: (Int, Date) -> Void
    let onFinish: () -> Void

    var body: some View {
        VStack(alignment: .trailing, spacing: 14) {
            HStack {
                Image(systemName: "seal.fill")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundStyle(AzkryColors.green)
                Spacer()
                Text("ختمة القرآن")
                    .font(AzkryTypography.title2)
            }

            if let progress {
                ProgressView(value: progress.fraction)
                    .tint(AzkryColors.green)
                    .accessibilityLabel("تقدم الختمة")
                    .accessibilityValue(
                        "الصفحة \(progress.currentPage) من \(QuranKhatmah.totalPages)"
                    )

                HStack(alignment: .firstTextBaseline) {
                    Text("هدف اليوم: صفحة \(progress.targetPage)")
                    Spacer()
                    Text("اليوم \(progress.dayNumber) من \(progress.totalDays)")
                }
                .font(AzkryTypography.callout)
                .foregroundStyle(AzkryColors.textSecondary)

                HStack(spacing: 10) {
                    Button("إنهاء الخطة", role: .destructive, action: onFinish)
                        .buttonStyle(.borderless)
                        .frame(minHeight: 44)
                    Spacer()
                    if let targetRequest {
                        NavigationLink(value: MainDestination.surah(targetRequest)) {
                            Label("ورد اليوم", systemImage: "book.pages.fill")
                                .font(AzkryTypography.headline)
                                .frame(minHeight: 44)
                        }
                    }
                }
            } else {
                Text("قسّم المصحف إلى ورد يومي واضح، وتابع تقدمك من آخر صفحة قرأتها.")
                    .font(AzkryTypography.callout)
                    .foregroundStyle(AzkryColors.textSecondary)
                    .frame(maxWidth: .infinity, alignment: .trailing)

                Menu {
                    Button("ختمة في 30 يومًا") { onStart(30, .now) }
                    Button("ختمة في 60 يومًا") { onStart(60, .now) }
                    Button("ختمة في 90 يومًا") { onStart(90, .now) }
                } label: {
                    Label("ابدأ خطة ختمة", systemImage: "calendar.badge.plus")
                        .font(AzkryTypography.headline)
                        .foregroundStyle(AzkryColors.green)
                        .frame(maxWidth: .infinity, minHeight: 48)
                        .background(AzkryColors.green.opacity(0.13), in: .rect(cornerRadius: 16))
                }
                .accessibilityHint("اختر مدة الختمة")
            }
        }
        .padding(18)
        .background(AzkryColors.surface, in: .rect(cornerRadius: AzkryTheme.Radius.card))
        .overlay {
            RoundedRectangle(cornerRadius: AzkryTheme.Radius.card)
                .stroke(AzkryColors.border, lineWidth: 0.7)
        }
    }
}

private struct BookmarksRow: View {
    let bookmarks: [QuranBookmark]

    var body: some View {
        VStack(alignment: .trailing, spacing: 10) {
            Text("علامات القراءة")
                .font(AzkryTypography.headline)
                .frame(maxWidth: .infinity, alignment: .trailing)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(bookmarks) { bookmark in
                        NavigationLink(
                            value: MainDestination.surah(SurahOpenRequest(
                                surahNumber: bookmark.surahNumber,
                                startPage: bookmark.page
                            ))
                        ) {
                            Label(
                                "\(bookmark.surahName) · \(bookmark.page)",
                                systemImage: "bookmark.fill"
                            )
                            .font(AzkryTypography.callout)
                            .foregroundStyle(AzkryColors.textPrimary)
                            .padding(.horizontal, 14)
                            .frame(minHeight: 44)
                            .background(AzkryColors.surfaceElevated, in: .capsule)
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
    }
}

private struct SurahRow: View {
    let surah: SurahSummary

    var body: some View {
        HStack(spacing: 14) {
            ZStack {
                RoundedRectangle(cornerRadius: 12)
                    .fill(AzkryColors.surfaceElevated)
                    .frame(width: 44, height: 44)
                Text("\(surah.number)")
                    .font(AzkryTypography.callout)
            }

            VStack(alignment: .trailing, spacing: 4) {
                Text(surah.name)
                    .font(AzkryTypography.quran)
                    .multilineTextAlignment(.trailing)
                Text("\(surah.ayahCount) آيات · صفحة \(surah.page)")
                    .font(AzkryTypography.caption)
                    .foregroundStyle(AzkryColors.textSecondary)
            }

            Spacer(minLength: 8)

            Text(surah.englishName)
                .font(AzkryTypography.caption)
                .foregroundStyle(AzkryColors.textSecondary)
                .multilineTextAlignment(.leading)
        }
        .foregroundStyle(AzkryColors.textPrimary)
        .frame(minHeight: 62)
        .padding(.horizontal, 14)
        .background(AzkryColors.surface, in: .rect(cornerRadius: 18))
        .overlay {
            RoundedRectangle(cornerRadius: 18)
                .stroke(AzkryColors.border, lineWidth: 0.6)
        }
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(surah.name)، \(surah.ayahCount) آيات، صفحة \(surah.page)")
    }
}

private struct MushafIndexRow: View {
    let title: String
    let subtitle: String
    let badge: String

    var body: some View {
        HStack(spacing: 14) {
            Image(systemName: "chevron.left")
                .foregroundStyle(AzkryColors.textSecondary)
            Spacer(minLength: 8)
            VStack(alignment: .trailing, spacing: 4) {
                Text(title)
                    .font(AzkryTypography.headline)
                Text(subtitle)
                    .font(AzkryTypography.caption)
                    .foregroundStyle(AzkryColors.textSecondary)
            }
            Text(badge)
                .font(AzkryTypography.callout)
                .frame(width: 44, height: 44)
                .background(AzkryColors.surfaceElevated, in: .circle)
        }
        .foregroundStyle(AzkryColors.textPrimary)
        .frame(minHeight: 62)
        .padding(.horizontal, 14)
        .background(AzkryColors.surface, in: .rect(cornerRadius: 18))
        .overlay {
            RoundedRectangle(cornerRadius: 18)
                .stroke(AzkryColors.border, lineWidth: 0.6)
        }
        .accessibilityElement(children: .combine)
    }
}
