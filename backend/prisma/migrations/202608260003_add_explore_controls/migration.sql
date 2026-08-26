CREATE TABLE "ContentReport" (
  "id" TEXT NOT NULL,
  "reporterUserId" TEXT NOT NULL,
  "contentItemId" TEXT NOT NULL,
  "reason" TEXT NOT NULL,
  "status" TEXT NOT NULL DEFAULT 'PENDING',
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updatedAt" TIMESTAMP(3) NOT NULL,
  CONSTRAINT "ContentReport_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "BlockedExploreSource" (
  "id" TEXT NOT NULL,
  "userId" TEXT NOT NULL,
  "sourceKey" TEXT NOT NULL,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT "BlockedExploreSource_pkey" PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX "ContentReport_reporterUserId_contentItemId_key"
ON "ContentReport"("reporterUserId", "contentItemId");
CREATE INDEX "ContentReport_status_createdAt_idx"
ON "ContentReport"("status", "createdAt");
CREATE UNIQUE INDEX "BlockedExploreSource_userId_sourceKey_key"
ON "BlockedExploreSource"("userId", "sourceKey");

ALTER TABLE "ContentReport"
ADD CONSTRAINT "ContentReport_reporterUserId_fkey"
FOREIGN KEY ("reporterUserId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "ContentReport"
ADD CONSTRAINT "ContentReport_contentItemId_fkey"
FOREIGN KEY ("contentItemId") REFERENCES "ContentItem"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "BlockedExploreSource"
ADD CONSTRAINT "BlockedExploreSource_userId_fkey"
FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;
