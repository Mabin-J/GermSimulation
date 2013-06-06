CREATE TABLE IF NOT EXISTS `connection` (
  `idx` int(11) NOT NULL AUTO_INCREMENT,
  `neuron_idx` int(11) NOT NULL,
  `target_idx` int(11) NOT NULL,
  `time1` int(11) NOT NULL DEFAULT '0',
  `time2` int(11) NOT NULL DEFAULT '0',
  `time3` int(11) NOT NULL DEFAULT '0',
  `score` int(11) NOT NULL,
  `signalPower` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`idx`),
  KEY `neuron_idx` (`neuron_idx`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

CREATE TABLE `neurons` (
  `idx` int(11) NOT NULL,
  `threshold` int(11) NOT NULL,
  `type` tinyint(4) NOT NULL,
  PRIMARY KEY (`idx`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
