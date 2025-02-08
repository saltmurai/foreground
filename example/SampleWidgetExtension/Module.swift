import ExpoModulesCore
import ActivityKit

internal class MissingCurrentWindowSceneException: Exception {
    override var reason: String {
        "Cannot determine the current window scene in which to present the modal for requesting a review."
    }
}

private func fetchDataAndUpdateActivity() async -> Void {
    if #available(iOS 16.2, *) {
        let newProgress = await fetchProgressFromAPI()
        let contentState = SportsLiveActivityAttributes.ContentState(title: "hihi", subTitle: "update", progress: newProgress)
        let updatedContent = ActivityContent(state: contentState, staleDate: nil)
        Task {
            for activity in Activity<SportsLiveActivityAttributes>.activities {
                await activity.update(updatedContent)
            }
        }
    }
}

private func fetchProgressFromAPI() async -> Int {
    return Int.random(in: 0...100)
}

public class ForegroundModule: Module {
    private var timer: Timer? 

    public func definition() -> ModuleDefinition {
        Name("Foreground")

        Function("startForegroundService") { (endpoint: String, title: String, subtext: String, progress: Int) -> String in
            return "start"
        }

        Function("stopForegroundService") { () -> String in
            return "stop"
        }
        
        Function("areActivitiesEnabled") { () -> Bool in
            if #available(iOS 16.2, *) {
                return ActivityAuthorizationInfo().areActivitiesEnabled
            } else {
                return false
            }
        }
        
        Function("startActivity") { (title: String, subTitle: String, progress: Int) -> Void in
            if #available(iOS 16.2, *) {
                let attributes = SportsLiveActivityAttributes()
                let contentState = SportsLiveActivityAttributes.ContentState(title: title, subTitle: subTitle, progress: progress)
                
                let activityContent = ActivityContent(state: contentState, staleDate: Calendar.current.date(byAdding: .minute, value: 30, to: Date())!)
                
                do {
                    let activity = try Activity.request(attributes: attributes, content: activityContent)

                    timer = Timer.scheduledTimer(withTimeInterval: 5.0, repeats: true) { _ in
                       Task {
                            await fetchDataAndUpdateActivity()
                        }
                    }
                } catch (let error) {

                }
            }
        }

        Function("updateActivity") { (title: String, subTitle: String, progress: Int) -> Void in
            if #available(iOS 16.2, *) {
                let contentState = SportsLiveActivityAttributes.ContentState(title: title, subTitle: subTitle, progress: progress)
                let alertConfiguration = AlertConfiguration(title: "Ride update", body: "This is the alert text", sound: .default)
                let updatedContent = ActivityContent(state: contentState, staleDate: nil)
                
                Task {
                    for activity in Activity<SportsLiveActivityAttributes>.activities {
                        await activity.update(updatedContent, alertConfiguration: alertConfiguration)
                    }
                }
            }
        }
        
        Function("endActivity") { (title: String, subTitle: String, progress: Int) -> Void in
            if #available(iOS 16.2, *) {
                let contentState = SportsLiveActivityAttributes.ContentState(title: title, subTitle: subTitle, progress: progress)
                let finalContent = ActivityContent(state: contentState, staleDate: nil)
                
                Task {
                    for activity in Activity<SportsLiveActivityAttributes>.activities {
                        await activity.end(finalContent, dismissalPolicy: .default)
                    }
                }
            }
        }
    }
}