DROP INDEX `reportingDescription` on TestDefinition;
ALTER TABLE TestDefinition DROP COLUMN `active`;
ALTER TABLE TestDefinition DROP COLUMN productName;
ALTER TABLE TestDefinition DROP COLUMN subProductName;
ALTER TABLE TestDefinition DROP COLUMN methodName;
ALTER TABLE TestDefinition DROP COLUMN className;
