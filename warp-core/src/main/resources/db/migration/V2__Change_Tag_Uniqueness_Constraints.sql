-- Mon Nov 16 15:55:08 2020

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

-- Remove unique constraints from these tables for being too broad
-- Oddly a constraint on three fields in this case broadens the uniqueness criteria, rendering this constraint unhelpful
ALTER TABLE TestExecutionTag DROP INDEX idTestExecution_value_TagName_unique;
ALTER TABLE TestDefinitionTag DROP INDEX idTestDefinition_value_TagName_unique;

-- Create new unique indices on tables, omitting the "value" from consideration
ALTER TABLE `TestExecutionTag`
ADD UNIQUE INDEX `idTestExecution_TagName_unique` (`idTagName` ASC, `idTestExecution` ASC);

ALTER TABLE `TestDefinitionTag`
ADD UNIQUE INDEX `idTestDefinition_TagName_unique` (`idTagName` ASC, `idTestDefinition` ASC);

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
