import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        IosRootKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        // Compose 铺满全屏（含状态栏/Home indicator 区域），
        // 安全区由 Compose 内容自行适配，避免全屏黑页面顶部/底部露白
        ComposeView()
            .ignoresSafeArea()
    }
}
