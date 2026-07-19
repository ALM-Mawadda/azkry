import SwiftUI

struct FridayView: View {
    @State private var viewModel = FridayViewModel()
    private let columns = [GridItem(.adaptive(minimum: 145), spacing: 10)]

    var body: some View {
        ScrollView {
            VStack(spacing: 14) {
                VStack(alignment: .trailing, spacing: 14) {
                    Text("سنن يوم الجمعة")
                        .font(AzkryTypography.title2)
                        .frame(maxWidth: .infinity, alignment: .trailing)

                    LazyVGrid(columns: columns, spacing: 10) {
                        ForEach(FridaySunnah.allCases) { sunnah in
                            Button { viewModel.toggle(sunnah) } label: {
                                HStack(spacing: 8) {
                                    Image(systemName: viewModel.completed.contains(sunnah) ? "checkmark.circle.fill" : "circle")
                                    Text(sunnah.title)
                                        .multilineTextAlignment(.center)
                                }
                                .font(AzkryTypography.callout)
                                .foregroundStyle(viewModel.completed.contains(sunnah) ? AzkryColors.yellow : AzkryColors.textPrimary)
                                .frame(maxWidth: .infinity, minHeight: 58)
                                .padding(.horizontal, 8)
                                .background(AzkryColors.surfaceElevated, in: .rect(cornerRadius: 18))
                                .overlay {
                                    RoundedRectangle(cornerRadius: 18)
                                        .stroke(viewModel.completed.contains(sunnah) ? AzkryColors.yellow : AzkryColors.border)
                                }
                            }
                            .buttonStyle(.plain)
                            .accessibilityValue(viewModel.completed.contains(sunnah) ? "مكتملة" : "غير مكتملة")
                        }
                    }
                }
                .padding(18)
                .background(AzkryColors.surface, in: .rect(cornerRadius: AzkryTheme.Radius.card))

                Text("اللَّهُمَّ صَلِّ وَسَلِّمْ وَبَارِكْ عَلَى نَبِيِّنَا مُحَمَّدٍ")
                    .font(AzkryTypography.dhikr)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity)
                    .padding(22)
                    .background(AzkryColors.surface, in: .rect(cornerRadius: AzkryTheme.Radius.card))

                NavigationLink(
                    value: MainDestination.surah(SurahOpenRequest(surahNumber: 18))
                ) {
                    Label("قراءة سورة الكهف", systemImage: "book.closed.fill")
                        .font(AzkryTypography.headline)
                        .foregroundStyle(AzkryColors.green)
                        .frame(maxWidth: .infinity, minHeight: 64)
                        .background(AzkryColors.green.opacity(0.13), in: .rect(cornerRadius: 22))
                }
                .buttonStyle(.plain)

                VStack(alignment: .trailing, spacing: 12) {
                    Text("من فضائل الجمعة")
                        .font(AzkryTypography.title2)
                    Text("خَيْرُ يَوْمٍ طَلَعَتْ عَلَيْهِ الشَّمْسُ يَوْمُ الْجُمُعَةِ.")
                        .font(AzkryTypography.dhikr)
                    Text("فيها ساعة لا يوافقها عبد مسلم يسأل الله خيرًا إلا أعطاه إياه.")
                        .font(AzkryTypography.body)
                        .foregroundStyle(AzkryColors.textSecondary)
                }
                .frame(maxWidth: .infinity, alignment: .trailing)
                .padding(18)
                .background(AzkryColors.surface, in: .rect(cornerRadius: AzkryTheme.Radius.card))
            }
            .padding(AzkryTheme.Layout.pageHorizontalPadding)
        }
        .background(AzkryColors.background)
        .navigationTitle("يوم الجمعة")
        .task { viewModel.load() }
    }
}
