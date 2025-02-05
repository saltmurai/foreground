import * as Foreground from "foreground";
import {
  PermissionsAndroid,
  Platform,
  Text,
  TouchableOpacity,
  View,
} from "react-native";

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
      {/* {Platform.OS === "ios" ? (
        <View>
          <Text>Start: {Foreground.startForegroundService("", "hi", "ha", 80)}</Text>
          <Text>Stop: {Foreground.stopForegroundService()}</Text>
        </View>
      ) : null} */}

      <View>
        <TouchableOpacity
          onPress={() => {
            Foreground.startForegroundService("", "hi", "ha", 80);
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
    </View>
  );
}
