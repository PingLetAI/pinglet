import { ArgumentsHost, Catch, ExceptionFilter, HttpException, HttpStatus, Logger } from '@nestjs/common';
import { randomUUID } from 'crypto';

@Catch()
export class SanitizedExceptionFilter implements ExceptionFilter {
  private readonly logger = new Logger(SanitizedExceptionFilter.name);

  catch(error: unknown, host: ArgumentsHost) {
    const http = host.switchToHttp();
    const request = http.getRequest();
    const response = http.getResponse();
    const requestId = randomUUID();
    const status = error instanceof HttpException ? error.getStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
    const raw = error instanceof HttpException ? error.getResponse() : null;
    const safeClientError = error instanceof HttpException && status < 500
      ? this.clientPayload(raw, error)
      : { message: 'Internal server error' };

    if (status >= 500) {
      const name = error instanceof Error ? error.name : 'UnknownError';
      this.logger.error(`[${requestId}] ${request.method} ${request.originalUrl || request.url} -> ${status} ${name}`);
    }

    response.setHeader('X-Request-Id', requestId);
    response.status(status).json({ statusCode: status, ...safeClientError, requestId });
  }

  private clientPayload(raw: string | object | null, error: HttpException) {
    if (typeof raw === 'string') return { message: raw };
    if (raw && typeof raw === 'object') {
      const value = raw as Record<string, unknown>;
      return {
        ...(typeof value.code === 'string' ? { code: value.code } : {}),
        message: value.message ?? error.message,
      };
    }
    return { message: error.message };
  }
}
