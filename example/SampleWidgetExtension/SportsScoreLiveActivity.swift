import ActivityKit
import WidgetKit
import SwiftUI

struct SportsLiveActivity: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: SportsLiveActivityAttributes.self) { context in
            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    Image("Pick")
                        .resizable()
                        .frame(width: 18, height: 18)
                        
                    Text("픽대리").font(.headline).foregroundColor(.primary).lineLimit(1)
                }.padding(.top, 5)

                VStack(alignment: .leading, spacing: 4) {
                    Text(context.state.title).font(.system(size: 18, weight: .bold)).foregroundColor(.primary).lineLimit(1)
                    Text(context.state.subTitle).font(.system(size: 16, weight: .regular)).foregroundColor(.secondary).lineLimit(1)
                }.padding(.bottom, 10)

                VStack {
                    GeometryReader { geometry in
                        ZStack(alignment: .leading) {
                            ProgressView(value: CGFloat(context.state.progress), total: 100.0)
                                .progressViewStyle(LinearProgressViewStyle())
                                .frame(height: 8)
                                .background(RoundedRectangle(cornerRadius: 4).fill(Color.gray.opacity(0.3)))

                            Image("Cars")
                                .resizable()
                                .frame(width: 32, height: 32)
                                .offset(x: (CGFloat(context.state.progress) / 100.0) * geometry.size.width - 16, y: -24)
                        }
                    }
                    .padding(.top, 18)
                    .padding(.bottom, 10)
                    .frame(height: 32) 
                }.padding(.bottom, 10)

                Spacer()
            }.padding(.top, 15).padding(.bottom, 8).padding(.horizontal, 15).activityBackgroundTint(.white).activitySystemActionForegroundColor(.black)
        } dynamicIsland: { context in
            DynamicIsland {
                // Expanded UI goes here.  Compose the expanded UI through
                // various regions, like leading/trailing/center/bottom
                DynamicIslandExpandedRegion(.leading) {
                    Text("Leading")
                }
                DynamicIslandExpandedRegion(.trailing) {
                    Text("Trailing")
                }
                DynamicIslandExpandedRegion(.bottom) {
                    Text("Bottom")
                    // more content
                }
            } compactLeading: {
                Text("L")
            } compactTrailing: {
                Text("T")
            } minimal: {
                Text("Min")
            }
            .widgetURL(URL(string: "http://www.apple.com"))
            .keylineTint(Color.red)
        }
    }
}

struct SportsLiveActivityPreviews: PreviewProvider {
    static let future = Calendar.current.date(byAdding: .minute, value: (Int(15) ), to: Date())!
    static let attributes = SportsLiveActivityAttributes()
    static let contentState = SportsLiveActivityAttributes.ContentState(title: "귀하의 차량이 곧 도착합니다!", subTitle: "초기화 중...", progress: 0)

    static var previews: some View {
        attributes
            .previewContext(contentState, viewKind: .dynamicIsland(.compact))
            .previewDisplayName("Island Compact")
        attributes
            .previewContext(contentState, viewKind: .dynamicIsland(.expanded))
            .previewDisplayName("Island Expanded")
        attributes
            .previewContext(contentState, viewKind: .dynamicIsland(.minimal))
            .previewDisplayName("Minimal")
    }
}

