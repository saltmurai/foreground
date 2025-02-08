import WidgetKit
import SwiftUI

@main
struct SampleWidgetExtensionBundle: WidgetBundle {
    var body: some Widget {
        SampleWidgetExtension()
        SportsLiveActivity()
    }
}
