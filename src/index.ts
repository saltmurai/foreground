import ForegroundModule from './ForegroundModule';
import SplashSCModule from './SplashSCModule';

export const Foreground = {
  startForegroundService: (endpoint: string, title: string, subtext: string, progress: number) =>
    ForegroundModule.startForegroundService(endpoint, title, subtext, progress),

  stopForegroundService: () => ForegroundModule.stopForegroundService(),

  areActivitiesEnabled: () => ForegroundModule.areActivitiesEnabled(),

  startActivity: (...args: any) => ForegroundModule.startActivity(...args),

  updateActivity: (...args: any) => ForegroundModule.updateActivity(...args),

  endActivity: (...args: any) => ForegroundModule.endActivity(...args),
};

export const SplashScreen = {
  hideSplash: () => SplashSCModule.hide(),
  showSplash: () => SplashSCModule.show(),
};
