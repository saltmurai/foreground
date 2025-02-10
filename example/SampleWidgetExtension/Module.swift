import ExpoModulesCore
import ActivityKit

internal class MissingCurrentWindowSceneException: Exception {
    override var reason: String {
        "Cannot determine the current window scene in which to present the modal for requesting a review."
    }
}

private func fetchDataAndUpdateActivity(endpoint: String) async -> Void {
    if #available(iOS 16.2, *) {
        let rideInfo = await fetchProgressFromAPI(endpoint: endpoint)
        print("Ride Info:", rideInfo)
        NSLog("Message: %@", String(describing: rideInfo.rideStatus))

        if rideInfo.rideStatus == "UNKNOWN" {
           let contentState = SportsLiveActivityAttributes.ContentState(title: "Done", subTitle: "Done sub", progress: 100)
           let finalContent = ActivityContent(state: contentState, staleDate: nil)
           Task {
                for activity in Activity<SportsLiveActivityAttributes>.activities {
                    await activity.end(finalContent, dismissalPolicy: .immediate)
                }
            }
        }

        let textTitle: String
        let subTextTitle: String
        let prog: Int

        switch rideInfo.rideStatus {
            case "RECEPTION":
                textTitle = "Text for reception"
                subTextTitle = "subText for reception"
                prog = 20
            case "DISPATCH":
                textTitle = "Text for dispatch"
                subTextTitle = "subText for dispatch"
                prog = 50
            case "ENROUTE":
                textTitle = "Text for enroute"
                subTextTitle = "subText for enroute"
                prog = 80
            default:
                textTitle = "귀하의 차량이 곧 도착합니다!"
                subTextTitle = "초기화 중..."
                prog = 10
        }

        let contentState = SportsLiveActivityAttributes.ContentState(title: textTitle, subTitle: subTextTitle, progress: prog)
        let updatedContent = ActivityContent(state: contentState, staleDate: nil)
        Task {
            for activity in Activity<SportsLiveActivityAttributes>.activities {
                await activity.update(updatedContent)
            }
        }
    }
}

private func fetchProgressFromAPI(endpoint: String) async -> RideResult {
    guard let url = URL(string: endpoint) else {
        print("Invalid URL:", endpoint)
        return RideResult.defaultValue()
    }
    
    do {
        let (data, _) = try await URLSession.shared.data(from: url)
        let decodedResponse = try JSONDecoder().decode(RideResponse.self, from: data)
        
        guard let result = decodedResponse.result else {
            print("API response missing 'result'")
            return RideResult.defaultValue()
        }

        return result
    } catch {
        print("Error fetching ride status:", error.localizedDescription)
        return RideResult.defaultValue()
    }
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
        
        Function("startActivity") { (title: String, subTitle: String, progress: Int, endpoint: String) -> Void in
            if #available(iOS 16.2, *) {
                let attributes = SportsLiveActivityAttributes()
                let contentState = SportsLiveActivityAttributes.ContentState(title: title, subTitle: subTitle, progress: progress)
                
                let activityContent = ActivityContent(state: contentState, staleDate: Calendar.current.date(byAdding: .minute, value: 30, to: Date())!)
                
                do {
                    let activity = try Activity.request(attributes: attributes, content: activityContent)

                    timer = Timer.scheduledTimer(withTimeInterval: 5.0, repeats: true) { _ in
                       Task {
                            await fetchDataAndUpdateActivity(endpoint: endpoint)
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
                        await activity.end(finalContent, dismissalPolicy: .immediate)
                    }
                }
            }
        }
    }
}

struct RideResponse: Decodable {
    let code: Int
    let result: RideResult?
}

struct RideResult: Decodable {
    let startMemo: String
    let endMemo: String
    let rideId: Int
    let rideStatus: String
    
    static func defaultValue() -> RideResult {
        return RideResult(
            startMemo: "UNKNOWN",
            endMemo: "UNKNOWN",
            rideId: 0,
            rideStatus: "UNKNOWN hihi"
        )
    }
}