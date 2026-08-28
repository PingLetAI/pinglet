const enabled = (value?: string) => ['1', 'true', 'yes', 'on'].includes((value || '').trim().toLowerCase());

export function validateProductionEnvironment() {
  if (process.env.NODE_ENV !== 'production') return;

  const missing: string[] = [];
  for (const name of ['DATABASE_URL', 'REDIS_URL', 'JWT_SECRET', 'ADMIN_SECRET', 'OTP_SECRET']) {
    if (!process.env[name]?.trim()) missing.push(name);
  }
  if (missing.length) throw new Error(`Production configuration is missing required values: ${missing.join(', ')}`);

  for (const name of ['JWT_SECRET', 'ADMIN_SECRET', 'OTP_SECRET']) {
    if ((process.env[name] || '').length < 32) throw new Error(`${name} must contain at least 32 characters in production`);
  }
  if (enabled(process.env.EMAIL_OTP_DEV_MODE)) {
    throw new Error('EMAIL_OTP_DEV_MODE must be false in production');
  }
  for (const name of ['APP_REVIEW_OTP', 'PLAY_REVIEW_OTP']) {
    const value = process.env[name]?.trim();
    if (value && !/^\d{6}$/.test(value)) throw new Error(`${name} must contain exactly 6 digits`);
  }
}
