import SwiftUI

struct SkyBackground: View {
    let phase: HeaderPhase

    private var gradient: LinearGradient {
        let colors: [Color] = switch phase {
        case .dawn: [AzkryColors.dawnTop, AzkryColors.dawnBottom]
        case .day: [AzkryColors.dayTop, AzkryColors.dayBottom]
        case .afternoon: [AzkryColors.afternoonTop, AzkryColors.afternoonBottom]
        case .dusk: [AzkryColors.duskTop, AzkryColors.duskBottom]
        case .night: [AzkryColors.nightTop, AzkryColors.nightBottom]
        }
        return LinearGradient(colors: colors, startPoint: .top, endPoint: .bottom)
    }

    private var starOpacity: Double {
        switch phase {
        case .night: 0.9
        case .dawn, .dusk: 0.22
        case .day, .afternoon: 0
        }
    }

    var body: some View {
        ZStack {
            gradient

            if phase == .day || phase == .afternoon {
                Circle()
                    .fill(
                        RadialGradient(
                            colors: [Color.white.opacity(0.45), Color.white.opacity(0)],
                            center: .center,
                            startRadius: 2,
                            endRadius: 88
                        )
                    )
                    .frame(width: 176, height: 176)
                    .offset(x: -115, y: -105)
                    .accessibilityHidden(true)
            }

            Canvas { context, size in
                for index in 0..<52 {
                    let x = Double((index * 47 + 13) % 101) / 100 * size.width
                    let y = Double((index * 31 + 7) % 89) / 100 * size.height * 0.74
                    let radius = CGFloat(index % 5 == 0 ? 1.5 : 0.85)
                    let rect = CGRect(
                        x: x - radius,
                        y: y - radius,
                        width: radius * 2,
                        height: radius * 2
                    )
                    context.fill(
                        Path(ellipseIn: rect),
                        with: .color(.white.opacity(starOpacity))
                    )
                }
            }
            .accessibilityHidden(true)
        }
    }
}
