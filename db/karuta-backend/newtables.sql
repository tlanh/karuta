--
-- Table structure for table `usersfolder`
--

DROP TABLE IF EXISTS `usersfolder`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usersfolder` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parentid` bigint DEFAULT NULL,
  `active` int NOT NULL DEFAULT '1',
  `code` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `label` text CHARACTER SET utf8 COLLATE utf8_unicode_ci,
  `modif_date` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `parentid_idx` (`parentid`),
  CONSTRAINT `parentid` FOREIGN KEY (`parentid`) REFERENCES `usersfolder` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usersfolder_user`
--

DROP TABLE IF EXISTS `usersfolder_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usersfolder_user` (
  `fid` bigint NOT NULL,
  `userid` bigint NOT NULL,
  PRIMARY KEY (`fid`,`userid`),
  KEY `userid` (`userid`),
  CONSTRAINT `fid` FOREIGN KEY (`fid`) REFERENCES `usersfolder` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `userid` FOREIGN KEY (`userid`) REFERENCES `credential` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
