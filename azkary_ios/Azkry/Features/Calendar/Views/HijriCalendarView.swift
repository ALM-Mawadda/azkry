import SwiftUI

struct HijriCalendarView: View {
    @State private var viewModel = HijriCalendarViewModel()
    private let columns = Array(repeating: GridItem(.flexible(), spacing: 5), count: 7)
    private let weekdays = ["ح", "ن", "ث", "ر", "خ", "ج", "س"]

    var body: some View {
        ScrollView {
            VStack(spacing: 18) {
                HStack {
                    Button("الشهر التالي", systemImage: "chevron.left") { viewModel.next() }
                        .labelStyle(.iconOnly)
                        .frame(width: 44, height: 44)
                    Spacer()
                    VStack(spacing: 4) {
                        Text(viewModel.month.title).font(AzkryTypography.title)
                        Text("\(viewModel.month.year) هـ")
                            .font(AzkryTypography.callout)
                            .foregroundStyle(AzkryColors.textSecondary)
                    }
                    Spacer()
                    Button("الشهر السابق", systemImage: "chevron.right") { viewModel.previous() }
                        .labelStyle(.iconOnly)
                        .frame(width: 44, height: 44)
                }

                LazyVGrid(columns: columns, spacing: 8) {
                    ForEach(weekdays, id: \.self) { day in
                        Text(day)
                            .font(AzkryTypography.caption)
                            .foregroundStyle(AzkryColors.textSecondary)
                            .frame(maxWidth: .infinity, minHeight: 34)
                    }
                    ForEach(viewModel.month.cells) { cell in
                        if let day = cell.day {
                            Text("\(day)")
                                .font(AzkryTypography.body)
                                .foregroundStyle(cell.isToday ? .white : AzkryColors.textPrimary)
                                .frame(maxWidth: .infinity, minHeight: 44)
                                .background(cell.isToday ? AzkryColors.indigo : .clear, in: .circle)
                                .accessibilityLabel(cell.isToday ? "اليوم، \(day)" : "\(day)")
                        } else {
                            Color.clear.frame(minHeight: 44).accessibilityHidden(true)
                        }
                    }
                }
                .padding(14)
                .background(AzkryColors.surface, in: .rect(cornerRadius: AzkryTheme.Radius.card))

                Button("العودة إلى اليوم", action: viewModel.today)
                    .buttonStyle(.bordered)
                    .frame(minHeight: 44)
            }
            .padding(AzkryTheme.Layout.pageHorizontalPadding)
        }
        .background(AzkryColors.background)
        .navigationTitle("التقويم الهجري")
        .task { viewModel.load() }
    }
}
