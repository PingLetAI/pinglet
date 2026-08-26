import { INestApplication, ValidationPipe } from '@nestjs/common';
import { NestFactory } from '@nestjs/core';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';
import { AppModule } from './app.module';
import { SanitizedExceptionFilter } from './common/http/sanitized-exception.filter';
import { validateProductionEnvironment } from './common/config/production-config';

function setupSwagger(app: INestApplication) {
  const config = new DocumentBuilder()
    .setTitle('PingLet API')
    .setDescription('Rotating ambient message widget backend')
    .setVersion('0.1.0')
    .addBearerAuth()
    .build();

  const doc = SwaggerModule.createDocument(app, config);
  SwaggerModule.setup('api/docs', app, doc);
}

async function bootstrap() {
  validateProductionEnvironment();
  const app = await NestFactory.create(AppModule);
  const port = Number(process.env.APP_PORT || 3000);

  app.useGlobalPipes(new ValidationPipe({ whitelist: true, transform: true }));
  app.useGlobalFilters(new SanitizedExceptionFilter());
  app.setGlobalPrefix('api/v1');
  app.getHttpAdapter().getInstance().set('trust proxy', 1);
  app.enableCors();

  if (process.env.ENABLE_SWAGGER === 'true' || process.env.NODE_ENV !== 'production') setupSwagger(app);

  await app.listen(port);
}

bootstrap().catch((error) => {
  // eslint-disable-next-line no-console
  console.error(error);
  process.exit(1);
});
