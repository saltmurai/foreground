// Reexport the native module. On web, it will be resolved to ForegroundModule.web.ts
// and on native platforms to ForegroundModule.ts
export { default } from './ForegroundModule';
export * from './Foreground.types';
