import { Controller, Get, Param } from '@nestjs/common';
import { ApiTags } from '@nestjs/swagger';
import { CatalogsService } from './catalogs.service';

@ApiTags('catalogs')
@Controller('catalogs')
export class CatalogsController {
  constructor(private readonly service: CatalogsService) {}

  @Get()
  list() {
    return this.service.list();
  }

  @Get(':id/items')
  items(@Param('id') id: string) {
    return this.service.listItemsById(id);
  }
}
