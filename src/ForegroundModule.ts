import { NativeModule, requireNativeModule } from "expo";

import { ForegroundModuleEvents } from "./Foreground.types";

declare class ForegroundModule extends NativeModule<ForegroundModuleEvents> {
  PI: number;
  hello(): string;
  setValueAsync(value: string): Promise<void>;
  startForegroundService: (
    endpoint: string,
    title: string,
    subtext: string
  ) => Promise<void>;
  stopForegroundService: () => Promise<void>;
}

// This call loads the native module object from the JSI.
export default requireNativeModule<ForegroundModule>("Foreground");
