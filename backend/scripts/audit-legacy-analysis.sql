-- Read-only inventory. Run against a backup or approved production connection.
-- Does not print personal text, delete records, or change published content.
-- Extra share-sheet text is not necessarily private; inspect flagged records
-- before deciding whether to regenerate analysis or remove catalog excerpts.
SELECT
  i."contentItemId",
  COUNT(*) AS "affectedIngestionCount",
  COUNT(DISTINCT i."userId") AS "affectedAccountCount",
  BOOL_OR(c."visibility" = 'COMMUNITY') AS "sharedWithExplore",
  MIN(i."createdAt") AS "firstCreatedAt",
  MAX(i."createdAt") AS "lastCreatedAt"
FROM "Ingestion" i
LEFT JOIN "ContentItem" c ON c."id" = i."contentItemId"
WHERE i."sourceDocument" LIKE '%USER SHARED CONTEXT:%'
GROUP BY i."contentItemId"
ORDER BY COUNT(DISTINCT i."userId") DESC;
