import SwiftUI

struct PagesView: View {
    @State private var viewModel = PagesViewModel()

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 11) {
                ForEach(viewModel.pages) { page in
                    if page.key == .friday {
                        NavigationLink(value: MainDestination.friday) {
                            PageRow(key: page.key)
                        }
                        .buttonStyle(.plain)
                    } else {
                        NavigationLink(value: MainDestination.page(page.key)) {
                            PageRow(key: page.key)
                        }
                        .buttonStyle(.plain)
                    }
                }

                if let section = viewModel.sayyidIstighfar {
                    PageSectionCard(section: section)
                        .padding(.top, 5)
                }
            }
            .padding(AzkryTheme.Layout.pageHorizontalPadding)
        }
        .background(AzkryColors.background)
        .navigationTitle("صفحات")
        .task { viewModel.load() }
    }
}

private struct PageRow: View {
    let key: PageKey

    var body: some View {
        HStack(spacing: 14) {
            Image(systemName: "chevron.left")
                .foregroundStyle(AzkryColors.textSecondary)
            Spacer()
            Text(key.title)
                .font(AzkryTypography.headline)
                .foregroundStyle(AzkryColors.textPrimary)
                .multilineTextAlignment(.trailing)
            Image(systemName: key.systemImage)
                .font(.system(size: 19, weight: .medium))
                .foregroundStyle(AzkryColors.indigo)
                .frame(width: 44, height: 44)
                .background(AzkryColors.surfaceElevated, in: .rect(cornerRadius: 14))
        }
        .padding(.horizontal, 16)
        .frame(minHeight: 68)
        .background(AzkryColors.surface, in: .rect(cornerRadius: 20))
        .overlay {
            RoundedRectangle(cornerRadius: 20).stroke(AzkryColors.border, lineWidth: 0.75)
        }
    }
}

struct PageSectionCard: View {
    let section: PageSection

    var body: some View {
        VStack(alignment: .trailing, spacing: 12) {
            HStack(alignment: .top) {
                ShareLink(item: shareText) {
                    Image(systemName: "square.and.arrow.up")
                        .frame(width: 44, height: 44)
                }
                .accessibilityLabel("مشاركة")
                Spacer()
                if let heading = section.heading {
                    Text(heading).font(AzkryTypography.headline)
                }
            }

            Text(section.body)
                .font(AzkryTypography.dhikr)
                .multilineTextAlignment(.trailing)
                .frame(maxWidth: .infinity, alignment: .trailing)

            if let note = section.note {
                Text(note)
                    .font(AzkryTypography.callout)
                    .foregroundStyle(AzkryColors.textSecondary)
                    .frame(maxWidth: .infinity, alignment: .trailing)
            }
            if let source = section.source {
                Text(source)
                    .font(AzkryTypography.caption)
                    .foregroundStyle(AzkryColors.green)
                    .frame(maxWidth: .infinity, alignment: .trailing)
            }
        }
        .padding(18)
        .foregroundStyle(AzkryColors.textPrimary)
        .background(AzkryColors.surface, in: .rect(cornerRadius: AzkryTheme.Radius.card))
        .overlay {
            RoundedRectangle(cornerRadius: AzkryTheme.Radius.card)
                .stroke(AzkryColors.border, lineWidth: 0.75)
        }
    }

    private var shareText: String {
        [section.heading, section.body, section.source].compactMap(\.self).joined(separator: "\n\n")
    }
}
