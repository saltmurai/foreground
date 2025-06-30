import ActivityKit
import SwiftUI
import WidgetKit

struct SportsLiveActivityAttributes: ActivityAttributes {
    public struct ContentState: Codable, Hashable {
        var title: String
        var subTitle: String
        var progress: Int
        var margin: Int
        var iconProgress: String
        var iconEnd: String
    }
}
