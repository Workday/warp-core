SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

CREATE TABLE `BuildTag` (
  `idBuildTag` int(11) NOT NULL AUTO_INCREMENT,
  `idBuild` int(11) NOT NULL COMMENT 'Foreign key pointing to the build.',
  `idTagName` int(11) NOT NULL COMMENT 'Foreign key pointing to the tag description.',
  `value` varchar(512) CHARACTER SET latin1 NOT NULL COMMENT 'Value of a given tag. ',
  PRIMARY KEY (`idBuildTag`),
  UNIQUE KEY `idBuild_TagName_unique` (`idTagName`,`idBuild`),
  KEY `idBuild_idx` (`idBuild`),
  KEY `idTagName_idx` (`idTagName`),
  CONSTRAINT `idTagDescription_TestCaseTag` FOREIGN KEY (`idTagName`) REFERENCES `TagName` (`idTagName`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `idBuild_Tag` FOREIGN KEY (`idBuild`) REFERENCES `Build` (`idBuild`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `BuildMetaTag` (
  `idBuildTag` int(11) NOT NULL,
  `idTagName` int(11) NOT NULL,
  `value` varchar(255) CHARACTER SET latin1 NOT NULL,
  PRIMARY KEY (`idBuildTag`,`idTagName`),
  KEY `idTagName_BuildMetaTag_idx` (`idTagName`),
  CONSTRAINT `idTagName_BuildMetaTag` FOREIGN KEY (`idTagName`) REFERENCES `TagName` (`idTagName`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `idBuildTag_BuildMetaTag` FOREIGN KEY (`idBuildTag`) REFERENCES `BuildTag` (`idBuildTag`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;