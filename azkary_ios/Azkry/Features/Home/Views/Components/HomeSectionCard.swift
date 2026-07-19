import SwiftUI

struct HomeSectionLinkAction {
    let title: String
    let subtitle: String?
    let systemImage: String
    let action: () -> Void
}

struct HomeSectionCard: View {
    let title: String
    let subtitle: String?
    let systemImage: String
    let action: () -> Void
    let secondary: HomeSectionLinkAction?

    init(
        title: String,
        subtitle: String? = nil,
        systemImage: String,
        action: @escaping () -> Void,
        secondary: HomeSectionLinkAction? = nil
    ) {
        self.title = title
        self.subtitle = subtitle
        self.systemImage = systemImage
        self.action = action
        self.secondary = secondary
    }

    var body: some View {
        VStack(spacing: 0) {
            row(
                title: title,
                subtitle: subtitle,
                systemImage: systemImage,
                action: action,
                highlighted: false
            )

            if let secondary {
                Divider().padding(.horizontal, 16)
                row(
                    title: secondary.title,
                    subtitle: secondary.subtitle,
                    systemImage: secondary.systemImage,
                    action: secondary.action,
                    highlighted: true
                )
            }
        }
        .background(AzkryColors.surface, in: .rect(cornerRadius: AzkryTheme.Radius.card))
        .overlay {
            RoundedRectangle(cornerRadius: AzkryTheme.Radius.card, style: .continuous)
                .stroke(AzkryColors.border, lineWidth: 0.75)
        }
        .clipShape(.rect(cornerRadius: AzkryTheme.Radius.card))
    }

    private func row(
        title: String,
        subtitle: String?,
        systemImage: String,
        action: @escaping () -> Void,
        highlighted: Bool
    ) -> some View {
        Button(action: action) {
            HStack(spacing: 14) {
                Image(systemName: systemImage)
                    .font(.system(size: 19, weight: .medium))
                    .foregroundStyle(highlighted ? AzkryColors.blue : AzkryColors.textPrimary)
                    .frame(width: 44, height: 44)
                    .background(
                        highlighted ? AzkryColors.blue.opacity(0.12) : AzkryColors.surfaceElevated,
                        in: .rect(cornerRadius: 14)
                    )

                VStack(alignment: .leading, spacing: 4) {
                    Text(title).font(AzkryTypography.headline)
                    if let subtitle {
                        Text(subtitle)
                            .font(AzkryTypography.caption)
                            .foregroundStyle(AzkryColors.textSecondary)
                    }
                }

                Spacer(minLength: 8)
                Image(systemName: "chevron.left")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundStyle(AzkryColors.textSecondary)
            }
            .foregroundStyle(AzkryColors.textPrimary)
            .padding(.horizontal, 16)
            .frame(maxWidth: .infinity, minHeight: 72)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel([title, subtitle].compactMap { $0 }.joined(separator: ". "))
    }
}
