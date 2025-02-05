import ExpoModulesCore
import UserNotifications

public class ForegroundModule: Module {
  public func definition() -> ModuleDefinition {
    Name("Foreground")

    Function("startForegroundService") { (endpoint: String, title: String, subtext: String, progress: Int) -> String in
      return "start"
    }

    Function("stopForegroundService") { () -> String in
      return "stop"
    }
  }
}
