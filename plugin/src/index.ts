import { ConfigPlugin, IOSConfig, withPlugins } from "@expo/config-plugins";
import { withConfig } from "./withConfig";
import { withPodfile } from "./withPodfile";

import { withXcode } from "./withXcode";
import { withWidgetExtensionEntitlements } from "./withWidgetExtensionEntitlements";
import withSplashBackground from "./withSplashBackground";

const withWidgetsAndLiveActivities: ConfigPlugin<{
  frequentUpdates?: boolean;
  widgetsFolder?: string;
  deploymentTarget?: string;
  moduleFileName?: string;
  attributesFileName?: string;
  groupIdentifier?: string;
  backgroundImage?: string;
}> = (
  config,
  {
    frequentUpdates = false,
    widgetsFolder = "widgets",
    deploymentTarget = "16.2",
    moduleFileName = "Module.swift",
    attributesFileName = "Attributes.swift",
    groupIdentifier,
    backgroundImage
  }
) => {
  const targetName = `${IOSConfig.XcodeUtils.sanitizedName(
    config.name
  )}Widgets`;
  const bundleIdentifier = `${config.ios?.bundleIdentifier}.${targetName}`;

  config.ios = {
    ...config.ios,
    infoPlist: {
      ...config.ios?.infoPlist,
      NSSupportsLiveActivities: true,
      NSSupportsLiveActivitiesFrequentUpdates: frequentUpdates,
    },
  };

  config = withPlugins(config, [
    [
      withXcode,
      {
        targetName,
        bundleIdentifier,
        deploymentTarget,
        widgetsFolder,
        moduleFileName,
        attributesFileName,
        backgroundImage
      },
    ],
    [withWidgetExtensionEntitlements, { targetName, groupIdentifier }],
    [withPodfile, { targetName }],
    [withConfig, { targetName, bundleIdentifier, groupIdentifier }],
    [withSplashBackground, { backgroundImage }],
  ]);

  return config;
};

export default withWidgetsAndLiveActivities;
