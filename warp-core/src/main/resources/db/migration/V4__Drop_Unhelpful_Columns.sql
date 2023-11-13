ALTER TABLE TestDefinition DROP COLUMN `active`;
ALTER TABLE TestDefinition DROP COLUMN productName;
ALTER TABLE TestDefinition DROP COLUMN subProductName;
DROP INDEX `reportingDescription` on TestDefinition;
