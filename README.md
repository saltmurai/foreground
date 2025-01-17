# foreground-ss

React Native Expo foreground service

# Install guide

```
npx expo install foreground-ss
```

add this script to the root of the expo project name `withForegroundService.js`

```
const { withAndroidManifest } = require('@expo/config-plugins');

const withForegroundService = (config) => {
  return withAndroidManifest(config, (config) => {
    const permissions = [
        'android.permission.POST_NOTIFICATIONS',
        'android.permission.FOREGROUND_SERVICE',
        'android.permission.FOREGROUND_SERVICE_DATA_SYNC',
      ];

      if (!config.modResults.manifest['uses-permission']) {
        config.modResults.manifest['uses-permission'] = [];
      }

      permissions.forEach((permission) => {
        const alreadyExists = config.modResults.manifest['uses-permission'].some(
          (item) => item.$['android:name'] === permission
        );

        if (!alreadyExists) {
          config.modResults.manifest['uses-permission'].push({
            $: { 'android:name': permission },
          });
        }
      });

    const service = {
      $: {
        'android:name': 'expo.modules.foreground.ForegroundService',
        'android:enabled': 'true',
        'android:exported': 'true',
        'android:foregroundServiceType': 'dataSync',
      },
    };

    // Ensure the <application> tag exists and add the service
    if (!config.modResults.manifest.application[0].service) {
      config.modResults.manifest.application[0].service = [];
    }

    const hasService = config.modResults.manifest.application[0].service.some(
      (existingService) =>
        existingService.$['android:name'] === 'expo.modules.foreground.ForegroundService'
    );

    if (!hasService) {
      config.modResults.manifest.application[0].service.push(service);
    }

    return config;
  });
};

module.exports = withForegroundService;

```

add config to `app.json`

```
"plugins": [..., "./withForegroundService.js"],

```

# Useage

```
npm run android
```

In code to start a foreground service the app need

- Allow Post Notification permission
- Allow FOREGROUND permission
- Allow FOREGROUND_DATA_SYNC permisiion

Example useage

```js
import * as ForegroundModule from "foreground-ss";

const requestNotificationPermission = async () => {
  if (Platform.OS === "android") {
    try {
      PermissionsAndroid.check("android.permission.POST_NOTIFICATIONS")
        .then((response) => {
          if (!response) {
            PermissionsAndroid.request(
              "android.permission.POST_NOTIFICATIONS",
              {
                title: "Notification",
                message:
                  "App needs access to your notification " +
                  "so you can get Updates",
                buttonNeutral: "Ask Me Later",
                buttonNegative: "Cancel",
                buttonPositive: "OK",
              }
            );
          }
        })
        .catch((err) => {
          console.log("Notification Error=====>", err);
        });
    } catch (err) {
      console.log(err);
    }
  }
};

export default function Home() {
  // request for foreground service permission
  useEffect(() => {
    requestNotificationPermission();
  }, []);

  const handlePress = () => {
    ForegroundModule.default.startForegroundService(
      // API end point return the data
      "https://dc70-101-99-23-76.ngrok-free.app/data"
    );
  };
  const handleStop = () => {
    ForegroundModule.default.stopForegroundService();
  };
  return (
    <>
      <Stack.Screen options={{ title: "Home" }} />
      <Container>
        <ScreenContent path="app/index.tsx" title="Home" />
        <Button title="Show Details" onPress={handlePress} />
        <Button title="Stop Service" onPress={handleStop} />
      </Container>
    </>
  );
}
```
