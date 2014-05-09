create database hack;


DROP TABLE IF EXISTS `agents`;  
CREATE TABLE `agents` (  
  `ip` varchar(20) NOT NULL,
  `port` int NOT NULL,
  `protocol` varchar(10) NOT NULL,
  `city` varchar(20),
  `country` varchar(20),
  `username` varchar(20),
  `password` varchar(40),
  `state`  int,
  PRIMARY KEY (`ip`, `port`)  
) ENGINE=innodb  DEFAULT CHARSET=utf8;  