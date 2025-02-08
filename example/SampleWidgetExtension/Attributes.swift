import ActivityKit
import WidgetKit
import SwiftUI

struct SportsLiveActivityAttributes: ActivityAttributes {
    public struct ContentState: Codable, Hashable {
        var title: String
        var subTitle: String
        var progress: Int
    }
}
