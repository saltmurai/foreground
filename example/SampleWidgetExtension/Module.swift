import ActivityKit
import ExpoModulesCore

internal class MissingCurrentWindowSceneException: Exception {
    override var reason: String {
        "Cannot determine the current window scene in which to present the modal for requesting a review."
    }
}

private func fetchDataAndUpdateActivity(endpoint: String) async {
    if #available(iOS 16.2, *) {
        let rideInfo = await fetchProgressFromAPI(endpoint: endpoint)
        print("Ride Info:", rideInfo)
        NSLog("Message: %@", String(describing: rideInfo.rideStatus))

        if rideInfo.rideStatus == "UNKNOWN" {
            let contentState = SportsLiveActivityAttributes.ContentState(
                title: "Done",
                subTitle: "Done sub",
                progress: 100,
                margin: 95,
                iconProgress: "Cars",
                iconEnd: "Location"
            )
            let finalContent = ActivityContent(state: contentState, staleDate: nil)
            Task {
                for activity in Activity<SportsLiveActivityAttributes>.activities {
                    await activity.end(finalContent, dismissalPolicy: .immediate)
                }
            }
        }

        let (
            textTitle,
            subTextTitle,
            prog,
            margin,
            iconProgress,
            iconEnd
        ) =
            switch rideInfo.rideStatus {
            case "RECEPTION":
                (
                    rideInfo.displayTime + "까지 기사가 배차될 예정입니다.", "출발: " + rideInfo.startMemo,
                    Int(rideInfo.progressPercentage), 3, "Counselor", "Cars"
                )
            case "DISPATCH":
                (
                    rideInfo.displayTime + "까지 기사가 도착할 예정입니다.", "출발: " + rideInfo.startMemo,
                    Int(rideInfo.progressPercentage), Int(rideInfo.progressPercentage), "Driver",
                    "Location"
                )
            case "ENROUTE":
                (
                    "안전하게 목적지까지 운행중입니다.", "출발: " + rideInfo.startMemo,
                    Int(rideInfo.progressPercentage),
                    Int(rideInfo.progressPercentage), "Cars", "Location"
                )
            default:
                ("귀하의 차량이 곧 도착합니다!", "초기화 중...", 10, 3, "Counselor", "Cars")
            }

        let contentState = SportsLiveActivityAttributes.ContentState(
            title: textTitle, subTitle: subTextTitle, progress: prog, margin: margin,
            iconProgress: iconProgress, iconEnd: iconEnd)
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

        Function("startForegroundService") {
            (endpoint: String, title: String, subtext: String, progress: Int) -> String in
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

        Function("startActivity") {
            (endpoint: String) -> Void in
            if #available(iOS 16.2, *) {
                let attributes = SportsLiveActivityAttributes()
                let contentState = SportsLiveActivityAttributes.ContentState(
                    title: "귀하의 차량이 곧 도착합니다!", subTitle: "초기화 중...", progress: 10, margin: 3,
                    iconProgress: "Counselor", iconEnd: "Cars")

                let activityContent = ActivityContent(
                    state: contentState,
                    staleDate: Calendar.current.date(byAdding: .minute, value: 30, to: Date())!)

                let finalContent = ActivityContent(state: contentState, staleDate: nil)

                Task {
                    for activity in Activity<SportsLiveActivityAttributes>.activities {
                        await activity.end(finalContent, dismissalPolicy: .immediate)
                    }
                }

                do {
                    let activity = try Activity.request(
                        attributes: attributes, content: activityContent)

                    timer = Timer.scheduledTimer(withTimeInterval: 5.0, repeats: true) { _ in
                        Task {
                            await fetchDataAndUpdateActivity(endpoint: endpoint)
                        }
                    }
                } catch (let error) {

                }
            }
        }

        Function("updateActivity") {
            (
                title: String, subTitle: String, progress: Int, margin: Int, iconProgress: String,
                iconEnd: String
            ) -> Void in
            if #available(iOS 16.2, *) {
                let contentState = SportsLiveActivityAttributes.ContentState(
                    title: title, subTitle: subTitle, progress: progress, margin: margin,
                    iconProgress: iconProgress, iconEnd: iconEnd)
                let alertConfiguration = AlertConfiguration(
                    title: "Ride update", body: "This is the alert text", sound: .default)
                let updatedContent = ActivityContent(state: contentState, staleDate: nil)

                Task {
                    for activity in Activity<SportsLiveActivityAttributes>.activities {
                        await activity.update(
                            updatedContent, alertConfiguration: alertConfiguration)
                    }
                }
            }
        }

        Function("endActivity") { () -> Void in
            if #available(iOS 16.2, *) {
                let contentState = SportsLiveActivityAttributes.ContentState(
                    title: "여행이 완료되었습니다!", subTitle: "이용해 주셔서 감사합니다.", progress: 100, margin: 95,
                    iconProgress: "Cars", iconEnd: "Location")
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
    let displayTime: String
    let progressPercentage: Double

    static func defaultValue() -> RideResult {
        return RideResult(
            startMemo: "UNKNOWN",
            endMemo: "UNKNOWN",
            rideId: 0,
            rideStatus: "UNKNOWN",
            displayTime: "UNKNOWN",
            progressPercentage: 0.0
        )
    }
}
