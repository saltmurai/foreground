import * as Foreground from "foreground";
import {
  Button,
  PermissionsAndroid,
  Platform,
  Text,
  TouchableOpacity,
  View,
} from "react-native";
const quarter = 1;
const scoreLeft = 2;
const scoreRight = 3;
const bottomText = "Hello, world!";
export default function App() {
  const requestNotificationPermission = async () => {
    try {
      if (Platform.OS === "android") {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS,
          {
            title: "Pick App Notification Permission",
            message: "Pick app wants to enable notifications",
            buttonNeutral: "Ask Me Later",
            buttonNegative: "Cancel",
            buttonPositive: "OK",
          }
        );
        if (granted === PermissionsAndroid.RESULTS.GRANTED) {
          console.log("Notification allowed");
        } else {
          console.log("Notification denied");
        }
      }
    } catch (err) {
      console.log(err);
    }
  };
  requestNotificationPermission();
  return (
    <View style={{ flex: 1, alignItems: "center", justifyContent: "center" }}>
      {Platform.OS === "ios" ? (
        <View>
          <Text>
            Start: {Foreground.startForegroundService("https://pick-api.xyz/ride/info/test/widget?uid=9", "hi", "ha", 40)}
          </Text>
          <Text>Stop: {Foreground.stopForegroundService()}</Text>
          {Foreground.areActivitiesEnabled() ? (
            <View>
              <Button
                onPress={() =>
                  Foreground.startActivity(
                    "귀하의 차량이 곧 도착합니다! title",
                    "귀하의 차량이 곧 도착합니다! hihihi",
                    20,
                    "https://pick-api.xyz/ride/info/test/widget?uid=9"
                  )
                }
                title="Start Live Activity"
              />
              <Button
                onPress={() =>
                  Foreground.updateActivity(
                    "귀하의 차량이 곧 도착합니다! title update",
                    "귀하의 차량이 곧 도착합니다! hihihi update",
                    80,
                  )
                }
                title="Update Live Activity"
              />
              <Button
                onPress={() =>
                  Foreground.endActivity(
                    "귀하의 차량이 곧 도착합니다! title end",
                    "귀하의 차량이 곧 도착합니다! hihihi end",
                    100,
                  )
                }
                title="End Live Activity"
              />
            </View>
          ) : null}
        </View>
      ) : (
        <View>
          <TouchableOpacity
            onPress={() => {
              Foreground.startForegroundService("https://pick-api.xyz/ride/info/test/widget?uid=9", "hi", "ha", 80);
            }}
          >
            <Text>Start</Text>
          </TouchableOpacity>
          <TouchableOpacity
            onPress={() => {
              Foreground.stopForegroundService();
            }}
          >
            <Text>Stop</Text>
          </TouchableOpacity>
        </View>
      )}
    </View>
  );
}
