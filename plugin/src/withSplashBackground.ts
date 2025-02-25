import { ConfigPlugin, withAndroidManifest, withDangerousMod } from "@expo/config-plugins";
import fs from "fs";
import path from "path";

const withSplashBackground: ConfigPlugin<{ backgroundImage?: string }> = (config, { backgroundImage }) => {
  if (!backgroundImage) {
    return config;
  }

  // Copy ảnh vào android/app/src/main/res/drawable/
  config = withDangerousMod(config, ["android", async (config) => {
    const projectRoot = config.modRequest.projectRoot;
    const sourcePath = path.join(projectRoot, backgroundImage);
    const fileExt = path.extname(backgroundImage);
    const imageName = `bgintro${fileExt}`;

    const drawableDir = path.join(
      projectRoot,
      "android",
      "app",
      "src",
      "main",
      "res",
      "drawable"
    );
    const targetPath = path.join(drawableDir, imageName);

    // Kiểm tra nếu file nguồn tồn tại
    if (!fs.existsSync(sourcePath)) {
      throw new Error(`Splash image not found: ${sourcePath}`);
    }

    // Đảm bảo thư mục drawable tồn tại
    const targetDir = path.dirname(targetPath);
    if (!fs.existsSync(targetDir)) {
      fs.mkdirSync(targetDir, { recursive: true });
    }

    // Copy ảnh sang thư mục drawable
    fs.copyFileSync(sourcePath, targetPath);
    console.log(`✅ Copied splash image to: ${targetPath}`);

    return config;
  }]);

  // Cập nhật AndroidManifest.xml
  return withAndroidManifest(config, async (config) => {
    const manifest = config.modResults.manifest;

    if (!manifest.application || !Array.isArray(manifest.application) || manifest.application.length === 0) {
      throw new Error("AndroidManifest.xml is missing <application> tag.");
    }

    const application = manifest.application[0];

    // Đảm bảo 'meta-data' tồn tại
    if (!application["meta-data"]) {
      application["meta-data"] = [];
    }

    // Thêm hoặc cập nhật BACKGROUND_IMAGE meta-data

    const metaDataName = "expo.modules.splashscreen.BACKGROUND_IMAGE";
    const existingMetaData = application["meta-data"].find((item) => item["$"]["android:name"] === metaDataName);

    if (existingMetaData) {
      existingMetaData["$"]["android:value"] = `@drawable/bgintro`;
    } else {
      application["meta-data"].push({
        $: {
          "android:name": metaDataName,
          "android:value": `@drawable/bgintro`,
        },
      });
    }

    return config;
  });
};

export default withSplashBackground;
