import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { JwtModule } from '@nestjs/jwt';
import { AuthModule } from './auth/auth.module';
import { UsersModule } from './users/users.module';
import { DevicesModule } from './devices/devices.module';
import { ContentModule } from './content/content.module';
import { CatalogsModule } from './catalogs/catalogs.module';
import { FeedModule } from './feed/feed.module';
import { IngestionModule } from './ingestion/ingestion.module';
import { EventsModule } from './events/events.module';
import { AdminModule } from './admin/admin.module';
import { QueueModule } from './queue/queue.module';
import { SyncModule } from './sync/sync.module';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true }),
    JwtModule.register({
      secret: process.env.JWT_SECRET || 'dev-secret',
      signOptions: { expiresIn: '15m' },
      global: true,
    }),
    AuthModule,
    UsersModule,
    DevicesModule,
    ContentModule,
    CatalogsModule,
    FeedModule,
    IngestionModule,
    EventsModule,
    AdminModule,
    QueueModule,
    SyncModule,
  ],
})
export class AppModule {}
