import SwiftUI

struct TasbihCounterView: View {
    var embedded = false
    @State private var viewModel = CounterViewModel()

    var body: some View {
        @Bindable var viewModel = viewModel

        ScrollView {
            VStack(spacing: 24) {
                Picker("الهدف", selection: $viewModel.target) {
                    Text("33").tag(33)
                    Text("100").tag(100)
                    Text("مفتوح").tag(0)
                }
                .pickerStyle(.segmented)
                .accessibilityLabel("هدف العداد")

                Button {
                    viewModel.increment()
                } label: {
                    VStack(spacing: 12) {
                        Text(viewModel.count, format: .number.locale(Locale(identifier: "en_US_POSIX")))
                            .font(.system(size: 72, weight: .bold, design: .rounded))
                            .minimumScaleFactor(0.5)
                        Text(viewModel.target > 0 ? "من \(viewModel.target)" : "اضغط للتسبيح")
                            .font(AzkryTypography.headline)
                            .foregroundStyle(AzkryColors.textSecondary)
                    }
                    .foregroundStyle(AzkryColors.textPrimary)
                    .frame(maxWidth: .infinity)
                    .frame(minHeight: 280)
                    .background(
                        AzkryColors.surface,
                        in: .rect(cornerRadius: AzkryTheme.Radius.card)
                    )
                    .overlay {
                        RoundedRectangle(cornerRadius: AzkryTheme.Radius.card)
                            .stroke(AzkryColors.indigo.opacity(0.8), lineWidth: 2)
                    }
                }
                .buttonStyle(.plain)
                .accessibilityLabel("عداد التسبيح، \(viewModel.count)")
                .accessibilityHint("يزيد العداد مرة واحدة")

                Button("تصفير العداد", systemImage: "arrow.counterclockwise") {
                    viewModel.reset()
                }
                .buttonStyle(.bordered)
                .frame(minHeight: AzkryTheme.Layout.minimumTapTarget)
            }
            .padding(AzkryTheme.Layout.pageHorizontalPadding)
        }
        .frame(maxWidth: .infinity, minHeight: embedded ? 500 : nil)
        .background(AzkryColors.background)
        .navigationTitle(embedded ? "" : "العداد")
        .task { viewModel.load() }
    }
}
