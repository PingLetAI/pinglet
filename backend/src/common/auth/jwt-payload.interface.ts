export interface JwtPayload {
  sub: string;
  deviceId: string;
  installationId: string;
  type: 'access' | 'refresh';
}
