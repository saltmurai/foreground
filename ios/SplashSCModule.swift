import ExpoModulesCore
import UIKit

public class SplashSCModule: Module {
    private var splashView: UIView?

    public func definition() -> ModuleDefinition {
        Name("SplashSC")

        OnCreate {
            self.showSplashScreen()
        }

        Function("show") {
            self.showSplashScreen()
        }

        Function("hide") {
            DispatchQueue.main.async {
                guard let splashView = self.splashView else { return }

                UIView.animate(withDuration: 0.3, animations: {
                    splashView.alpha = 0
                }) { _ in
                    splashView.removeFromSuperview()
                    self.splashView = nil
                }
            }
        }
    }

    private func showSplashScreen() {
        DispatchQueue.main.async {
            if self.splashView == nil {
                self.createAndShowSplashScreen()
            }
        }
    }

    private func createAndShowSplashScreen() {
        guard let window = UIApplication.shared.windows.first(where: { $0.isKeyWindow }) else { return }

        if self.splashView == nil {
            let splashView = UIView(frame: window.bounds)
            splashView.backgroundColor = UIColor.black

            if let splashImage = UIImage(named: "SplashImage") {
                let splashImageView = UIImageView(image: splashImage)
                splashImageView.contentMode = .scaleAspectFill
                splashImageView.frame = splashView.bounds

                splashView.addSubview(splashImageView)
            }

            window.addSubview(splashView)
            self.splashView = splashView
        }
    }
}
