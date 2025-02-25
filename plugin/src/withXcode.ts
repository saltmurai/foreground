import { ConfigPlugin, withXcodeProject } from "@expo/config-plugins";
import * as path from "path";
import * as fs from "fs";

import { addXCConfigurationList } from "./xcode/addXCConfigurationList";
import { addProductFile } from "./xcode/addProductFile";
import { addToPbxNativeTargetSection } from "./xcode/addToPbxNativeTargetSection";
import { addToPbxProjectSection } from "./xcode/addToPbxProjectSection";
import { addTargetDependency } from "./xcode/addTargetDependency";
import { addPbxGroup } from "./xcode/addPbxGroup";
import { addBuildPhases } from "./xcode/addBuildPhases";
import { getWidgetFiles } from "./lib/getWidgetFiles";

export const withXcode: ConfigPlugin<{
  targetName: string;
  bundleIdentifier: string;
  deploymentTarget: string;
  widgetsFolder: string;
  moduleFileName: string;
  attributesFileName: string;
  backgroundImage: string;
}> = (
  config,
  {
    targetName,
    bundleIdentifier,
    deploymentTarget,
    widgetsFolder,
    moduleFileName,
    attributesFileName,
    backgroundImage
  }
) => {
  return withXcodeProject(config, (config) => {
    const xcodeProject = config.modResults;
    const widgetsPath = path.join(config.modRequest.projectRoot, widgetsFolder);

    const targetUuid = xcodeProject.generateUuid();
    const groupName = "Embed Foundation Extensions";
    const { projectRoot, platformProjectRoot, projectName } = config.modRequest;
    const marketingVersion = config.version;

    const assetsPath = path.join(platformProjectRoot, projectName || config.name || config.slug, "Images.xcassets", "SplashImage.imageset");

    if (!fs.existsSync(assetsPath)) {
      fs.mkdirSync(assetsPath, { recursive: true });
    }
    const sourcePath = path.join(projectRoot, backgroundImage);
    const targetImgPath = path.join(assetsPath, "bgintro.png");

    if (fs.existsSync(sourcePath)) {
      fs.copyFileSync(sourcePath, targetImgPath);
      console.log(`✅ Copied splash image to iOS assets: ${targetImgPath}`);

      const contentsJson = {
        images: [{ idiom: "universal", filename: "bgintro.png" }],
        info: { version: 1, author: "expo" },
      };

      fs.writeFileSync(path.join(assetsPath, "Contents.json"), JSON.stringify(contentsJson, null, 2));
    } else {
      console.warn(`⚠️ Splash image not found: ${sourcePath}`);
    }

    const targetPath = path.join(platformProjectRoot, targetName);

    const widgetFiles = getWidgetFiles(
      widgetsPath,
      targetPath,
      moduleFileName,
      attributesFileName
    );

    const xCConfigurationList = addXCConfigurationList(xcodeProject, {
      targetName,
      currentProjectVersion: config.ios!.buildNumber || "1",
      bundleIdentifier,
      deploymentTarget,
      marketingVersion,
    });

    const productFile = addProductFile(xcodeProject, {
      targetName,
      groupName,
    });

    const target = addToPbxNativeTargetSection(xcodeProject, {
      targetName,
      targetUuid,
      productFile,
      xCConfigurationList,
    });

    addToPbxProjectSection(xcodeProject, target);

    addTargetDependency(xcodeProject, target);

    addBuildPhases(xcodeProject, {
      targetUuid,
      groupName,
      productFile,
      widgetFiles,
    });

    addPbxGroup(xcodeProject, {
      targetName,
      widgetFiles,
    });

    return config;
  });
};
