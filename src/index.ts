import ForegroundModule from './ForegroundModule';

export function startForegroundService(endpoint: string, title: string, subtext: string, progress: number) {
  return ForegroundModule.startForegroundService(endpoint, title, subtext, progress);
}

export function stopForegroundService(): string {
  return ForegroundModule.stopForegroundService();
}
