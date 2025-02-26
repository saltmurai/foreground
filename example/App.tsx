import { Foreground, SplashScreen } from "foreground";
import { useEffect, useState } from "react";
import {
  Button,
  Modal,
  PermissionsAndroid,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from "react-native";
const quarter = 1;
const scoreLeft = 2;
const scoreRight = 3;
const bottomText = "Hello, world!";
export default function App() {
  const [isPopupVisible, setIsPopupVisible] = useState(false);
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

  useEffect(() => {
    // SplashScreen.showSplash();
    setTimeout(() => {
      // SplashScreen.hideSplash();
    setIsPopupVisible(true)

    }, 3000);
    // setIsPopupVisible(true)
  }, []);
 

  return (
    <View style={{ flex: 1, alignItems: "center", justifyContent: "center" }}>
      {Platform.OS === "ios" ? (
        <View>
          <Text>
            Start:{" "}
            {Foreground.startForegroundService(
              "https://pick-api.xyz/ride/info/test/widget?uid=9",
              "hi",
              "ha",
              40
            )}
          </Text>
          <Text>Stop: {Foreground.stopForegroundService()}</Text>
          {Foreground.areActivitiesEnabled() ? (
            <View>
              <Button
                onPress={() =>
                  // Foreground.startActivity(
                  //   "귀하의 차량이 곧 도착합니다! title",
                  //   "귀하의 차량이 곧 도착합니다! hihihi",
                  //   20,
                  //   "https://pick-api.xyz/ride/info/test/widget?uid=9"
                  // )
                  {
                    SplashScreen.showSplash();
                    setTimeout(() => {
                      // SplashScreen.hideSplash();
                      setIsPopupVisible(true);
                    }, 3000);
                  }
                }
                title="Start Live Activity"
              />
              <Button
                onPress={() =>
                  Foreground.updateActivity(
                    "귀하의 차량이 곧 도착합니다! title update",
                    "귀하의 차량이 곧 도착합니다! hihihi update",
                    80
                  )
                }
                title="Update Live Activity"
              />
              <Button
                onPress={() =>
                  Foreground.endActivity(
                    "귀하의 차량이 곧 도착합니다! title end",
                    "귀하의 차량이 곧 도착합니다! hihihi end",
                    100
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
              // Foreground.startForegroundService("https://pick-api.xyz/ride/info/test/widget?uid=9", "hi", "ha", 80);
              SplashScreen.showSplash();
              setTimeout(() => {
                // SplashScreen.hideSplash();
                setIsPopupVisible(true);
              }, 3000);
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
      <Modal
        animationType="slide"
        transparent={true}
        visible={isPopupVisible}
        onRequestClose={() => {
          setIsPopupVisible(!isPopupVisible);
          SplashScreen.hideSplash();
        }}
      >
        <View style={styles.centeredView}>
          <View style={styles.modalView}>
            <Text style={styles.modalText}>Hello World!</Text>
            <Pressable
              style={[styles.button, styles.buttonClose]}
              onPress={() => {
                setIsPopupVisible(!isPopupVisible), SplashScreen.hideSplash();
              }}
            >
              <Text style={styles.textStyle}>Hide Modal</Text>
            </Pressable>
          </View>
        </View>
      </Modal>
    </View>
  );
}
const styles = StyleSheet.create({
  centeredView: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  },
  modalView: {
    margin: 20,
    backgroundColor: "white",
    borderRadius: 20,
    padding: 35,
    alignItems: "center",
    shadowColor: "#000",
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.25,
    shadowRadius: 4,
    elevation: 5,
  },
  button: {
    borderRadius: 20,
    padding: 10,
    elevation: 2,
  },
  buttonOpen: {
    backgroundColor: "#F194FF",
  },
  buttonClose: {
    backgroundColor: "#2196F3",
  },
  textStyle: {
    color: "white",
    fontWeight: "bold",
    textAlign: "center",
  },
  modalText: {
    marginBottom: 15,
    textAlign: "center",
  },
});
