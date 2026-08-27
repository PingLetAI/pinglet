import SwiftUI

extension Color {
    static let pingletBackground = Color(red: 0.965, green: 0.949, blue: 0.914)
    static let pingletPaper = Color(red: 0.995, green: 0.988, blue: 0.965)
    static let pingletInk = Color(red: 0.065, green: 0.072, blue: 0.058)
    static let pingletMutedInk = Color(red: 0.29, green: 0.30, blue: 0.27)
    static let pingletGold = Color(red: 0.90, green: 0.66, blue: 0.16)
    static let pingletMint = Color(red: 0.76, green: 0.88, blue: 0.82)
    static let pingletClay = Color(red: 0.73, green: 0.28, blue: 0.18)
    static let pingletLine = Color.pingletInk.opacity(0.11)
}

struct PingLetCanvas: View {
    var body: some View {
        ZStack {
            LinearGradient(
                colors: [.pingletPaper, .pingletBackground],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            Circle()
                .fill(Color.pingletMint.opacity(0.42))
                .frame(width: 270, height: 270)
                .blur(radius: 1)
                .offset(x: 160, y: -310)
            Circle()
                .fill(Color.pingletClay.opacity(0.10))
                .frame(width: 220, height: 220)
                .offset(x: -170, y: 360)
        }
        .ignoresSafeArea()
    }
}

struct PingLetSectionLabel: View {
    let title: String
    var trailing: String? = nil
    var body: some View {
        HStack {
            Text(title.uppercased())
                .font(.system(size: 12, weight: .bold, design: .rounded))
                .tracking(1.4)
                .foregroundStyle(Color.pingletClay)
            Spacer()
            if let trailing {
                Text(trailing.uppercased())
                    .font(.system(size: 11, weight: .semibold, design: .rounded))
                    .tracking(0.8)
                    .foregroundStyle(Color.pingletMutedInk.opacity(0.72))
            }
        }
    }
}

struct PingLetCard<Content: View>: View {
    var dark = false
    @ViewBuilder let content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 13) { content }
            .padding(20)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(
                dark ? AnyShapeStyle(Color.pingletInk) : AnyShapeStyle(.ultraThinMaterial),
                in: RoundedRectangle(cornerRadius: 26, style: .continuous)
            )
            .foregroundStyle(dark ? Color.pingletPaper : Color.pingletInk)
            .overlay(
                RoundedRectangle(cornerRadius: 26, style: .continuous)
                    .stroke(dark ? Color.white.opacity(0.07) : Color.pingletLine, lineWidth: 1)
            )
            .shadow(color: Color.pingletInk.opacity(dark ? 0.16 : 0.07), radius: 18, x: 0, y: 9)
    }
}

struct PingLetPrimaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.system(size: 14, weight: .bold, design: .rounded))
            .tracking(0.7)
            .frame(maxWidth: .infinity, minHeight: 54)
            .foregroundStyle(Color.pingletPaper)
            .background(Color.pingletInk.opacity(configuration.isPressed ? 0.78 : 1), in: RoundedRectangle(cornerRadius: 18, style: .continuous))
            .scaleEffect(configuration.isPressed ? 0.985 : 1)
            .animation(.easeOut(duration: 0.14), value: configuration.isPressed)
    }
}
