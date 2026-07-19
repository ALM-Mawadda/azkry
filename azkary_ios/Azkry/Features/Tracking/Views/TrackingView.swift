import SwiftData
import SwiftUI

struct TrackingView: View {
    @Environment(\.modelContext) private var modelContext
    @State private var viewModel = TrackingViewModel()

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                if let errorMessage = viewModel.errorMessage {
                    InlineErrorView(message: errorMessage)
                }

                VStack(spacing: 10) {
                    Text("\(viewModel.completedCount) من \(viewModel.totalCount)")
                        .font(AzkryTypography.title)
                    ProgressView(
                        value: Double(viewModel.completedCount),
                        total: Double(viewModel.totalCount)
                    )
                    .tint(AzkryColors.green)
                    Text("كل خطوة صغيرة تصنع ورداً ثابتاً")
                        .font(AzkryTypography.callout)
                        .foregroundStyle(AzkryColors.textSecondary)
                }
                .padding(18)
                .frame(maxWidth: .infinity)
                .background(AzkryColors.surface, in: .rect(cornerRadius: AzkryTheme.Radius.card))

                sectionTitle("الصلوات")
                ForEach(Prayer.obligatory) { prayer in
                    trackingRow(
                        title: prayer.title,
                        systemImage: "checkmark.circle",
                        completed: viewModel.prayers[prayer] ?? false,
                        action: { viewModel.toggle(prayer, in: modelContext) }
                    )
                }

                sectionTitle("ورد اليوم")
                ForEach(TrackingViewModel.tasks) { task in
                    trackingRow(
                        title: task.title,
                        systemImage: task.systemImage,
                        completed: viewModel.worship[task.id] ?? false,
                        action: { viewModel.toggle(task, in: modelContext) }
                    )
                }
            }
            .padding(AzkryTheme.Layout.pageHorizontalPadding)
        }
        .background(AzkryColors.background)
        .navigationTitle("متابعتي")
        .task { viewModel.load(in: modelContext) }
    }

    private func sectionTitle(_ title: String) -> some View {
        Text(title)
            .font(AzkryTypography.title2)
            .frame(maxWidth: .infinity, alignment: .trailing)
    }

    private func trackingRow(
        title: String,
        systemImage: String,
        completed: Bool,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            HStack(spacing: 14) {
                Image(systemName: completed ? "checkmark.circle.fill" : "circle")
                    .font(.system(size: 23))
                    .foregroundStyle(completed ? AzkryColors.green : AzkryColors.textSecondary)
                    .frame(width: 44, height: 44)
                Text(title)
                    .font(AzkryTypography.headline)
                Spacer()
                Image(systemName: systemImage)
                    .foregroundStyle(AzkryColors.textSecondary)
            }
            .foregroundStyle(AzkryColors.textPrimary)
            .padding(.horizontal, 16)
            .frame(minHeight: 62)
            .background(AzkryColors.surface, in: .rect(cornerRadius: 20))
        }
        .buttonStyle(.plain)
        .accessibilityLabel(title)
        .accessibilityValue(completed ? "مكتمل" : "غير مكتمل")
    }
}
