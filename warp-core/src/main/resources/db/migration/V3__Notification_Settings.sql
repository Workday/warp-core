SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';


CREATE TABLE IF NOT EXISTS `NotificationSettings` (
  `idNotificationSettings` INT(11) NOT NULL AUTO_INCREMENT,
  `idTestDefinition` INT(11) NOT NULL COMMENT 'Foreign key pointing to the test definition.',
  `flappingDetectionEnabled` TINYINT(1) NOT NULL DEFAULT FALSE COMMENT 'Whether arbiter flapping detection is enabled.',
  `responseTimeRequirement` DOUBLE NOT NULL,
  `alertOnNth` INT(11) NOT NULL DEFAULT 1,
  PRIMARY KEY (`idNotificationSettings`),
  UNIQUE INDEX `definition_idx` (`idTestDefinition` ASC),
  CONSTRAINT `definition_NotificationSettings`
    FOREIGN KEY (`idTestDefinition`)
    REFERENCES `TestDefinition` (`idTestDefinition`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;