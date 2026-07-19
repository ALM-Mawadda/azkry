import SwiftUI

struct QiblaView: View {
    var embedded = false
    @State private var viewModel = QiblaViewModel()

    var body: some View {
        ScrollView {
            VStack(spacing: 22) {
                Text(viewModel.city)
                    .font(AzkryTypography.headline)
                    .foregroundStyle(AzkryColors.textSecondary)

                ZStack {
                    Circle()
                        .fill(AzkryColors.surface)
                    Circle()
                        .stroke(AzkryColors.border, lineWidth: 1)

                    ZStack {
                        ForEach(0..<12, id: \.self) { index in
                            Capsule()
                                .fill(index == 0 ? AzkryColors.danger : AzkryColors.textSecondary)
                                .frame(width: index == 0 ? 4 : 2, height: index == 0 ? 18 : 10)
                                .offset(y: -128)
                                .rotationEffect(.degrees(Double(index) * 30))
                        }
                    }
                    .rotationEffect(.degrees(viewModel.compassRotation))

                    Image(systemName: "location.north.fill")
                        .font(.system(size: 86, weight: .medium))
                        .foregroundStyle(viewModel.isAligned ? AzkryColors.green : AzkryColors.indigo)
                        .rotationEffect(.degrees(viewModel.qiblaRotation))
                        .accessibilityHidden(true)

                    Circle()
                        .fill(AzkryColors.yellow)
                        .frame(width: 15, height: 15)
                }
                .frame(width: 290, height: 290)

                Text(
                    viewModel.isAligned
                        ? "أنت باتجاه القبلة"
                        : "اتجاه القبلة \(Int(viewModel.bearing.rounded()))° من الشمال"
                )
                    .font(AzkryTypography.title2)
                    .multilineTextAlignment(.center)
                    .foregroundStyle(viewModel.isAligned ? AzkryColors.green : AzkryColors.textPrimary)
                    .accessibilityLabel(
                        viewModel.isAligned
                            ? "أنت باتجاه القبلة"
                            : "اتجاه القبلة \(Int(viewModel.bearing.rounded())) درجة من الشمال"
                    )

                Text(
                    viewModel.compassMessage
                        ?? "حرّك الهاتف ببطء حتى يشير السهم إلى أعلى الشاشة."
                )
                    .font(AzkryTypography.callout)
                    .foregroundStyle(AzkryColors.textSecondary)
                    .multilineTextAlignment(.center)
            }
            .frame(maxWidth: .infinity)
            .padding(.horizontal, AzkryTheme.Layout.pageHorizontalPadding)
            .padding(.vertical, 24)
        }
        .frame(maxWidth: .infinity, minHeight: embedded ? 500 : nil)
        .background(AzkryColors.background)
        .navigationTitle(embedded ? "" : "القبلة")
        .task { await viewModel.run() }
    }
}
