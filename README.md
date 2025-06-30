# PICK Widget && Intro Screen Service

## Customize and Optimize the Module
This is a package that integrates a custom native module using Expo’s expo-modules-core library.
Refer to [Expo Modules](https://docs.expo.dev/bare/installing-expo-modules/) for more details.

## Building and Publishing Package
### Install dependencies 
  ```bash
  npm install
  ```
### Build package
  ```bash
  npm run new-build
  ```
### Publishing Package
1. Login to npm

```bash
npm login
```

2. Patch version

```bash
npm patch version
```

3. Publish package

```bash
npm publish
```

## Usage and Integration Guide
### Install guide
  ```bash
  npx expo install foreground-ss
  ```
OR
  ```bash
  npm install foreground-ss
  ```

### Configuration guide
1. Add this script to the root of the expo project name `withForegroundService.js`
```javascript
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

2. Add config to `app.config.json`
```javascript
const foregroundServicePlugin = require("./plugins/withForegroundService");

module.exports = {
  expo: {
    ...,
    plugins: [
      ..., 
      foregroundServicePlugin,
      [
        "foreground-ss",
        { 
          "frequentUpdates": true, 
          "widgetsFolder": "SampleWidgetExtension", 
          "backgroundImage": "./assets/images/bg-intro-spring.png" 
        },
      ]
    ],
  }
}
```

3. Add folder example/SampleWidgetExtension to root of the exp project

# Usage
In code to start a foreground service the app need
- Allow Post Notification permission
- Allow FOREGROUND permission
- Allow FOREGROUND_DATA_SYNC permisiion

### Example Usage
#### Request notification permission
```javascript
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
```

#### Example Usage Intro Screen (Only work for Android)
For IOS, you can use `expo-splash-screen`
This module will automatically start when it is initialized; you just need to use hideSplash to hide it.

```javascript
import { SplashScreen as SplashScreenAndroid } from "foreground-ss";
import * as SplashScreenIOS from "expo-splash-screen";
import { Platform } from "react-native";

Platform.OS !== "android" ? SplashScreenIOS.preventAutoHideAsync() : null;

if (Platform.OS === "android") {
  await SplashScreenAndroid.hideSplash();
} else {
  await SplashScreenIOS.hideAsync();
}
```

#### Example Usage Widget
Only need to call the module whenever a new ride is created.
The module will automatically call the API to check the latest ride information and update the widget.

```javascript
import { Foreground } from "foreground-ss";
import { Platform } from "react-native";

if(Platform.OS === 'ios') {
  if(rideId) {
      Foreground.startActivity(`${baseUrl}/ride/info/test/widget?uid=${uid}`)
  }
}
if(Platform.OS === 'android') {
  if(rideId) {
      Foreground.startForegroundService(`${baseUrl}/ride/info/test/widget?uid=${uid}`, "귀하의 차량이 곧 도착합니다!", "초기화 중...", 10)
  }
}
```