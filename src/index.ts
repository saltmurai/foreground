import ForegroundModule from './ForegroundModule';

export function startForegroundService(endpoint: string, title: string, subtext: string, progress: number) {
  return ForegroundModule.startForegroundService(endpoint, title, subtext, progress);
}

export function stopForegroundService(): string {
  return ForegroundModule.stopForegroundService();
}

export function areActivitiesEnabled(): boolean {
  return ForegroundModule.areActivitiesEnabled();
}

export function startActivity(...args: any): void {
  return ForegroundModule.startActivity(...args);
}

export function updateActivity(...args: any): void {
  return ForegroundModule.updateActivity(...args);
}

export function endActivity(...args: any): void {
  return ForegroundModule.endActivity(...args);
}
