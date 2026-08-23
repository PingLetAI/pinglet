import { Injectable } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';

@Injectable()
export class JwtHelper {
  constructor(private readonly jwtService: JwtService) {}

  async sign(payload: any, expiresIn: string | number) {
    return this.jwtService.sign(payload, { expiresIn });
  }

  async verify<T extends object = any>(token: string): Promise<T> {
    return this.jwtService.verify<T>(token);
  }
}
