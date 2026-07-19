import SwiftUI

struct InlineErrorView: View {
    let message: String

    var body: some View {
        Label(message, systemImage: "exclamationmark.triangle.fill")
            .font(AzkryTypography.callout)
            .foregroundStyle(AzkryColors.danger)
            .frame(maxWidth: .infinity, alignment: .trailing)
            .padding(12)
            .background(AzkryColors.danger.opacity(0.12), in: .rect(cornerRadius: 16))
            .accessibilityLabel("خطأ: \(message)")
    }
}
