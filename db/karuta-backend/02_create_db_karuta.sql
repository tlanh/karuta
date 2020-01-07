-- phpMyAdmin SQL Dump
-- version 4.6.6deb4
-- https://www.phpmyadmin.net/
--
-- Client :  localhost:3306
-- Généré le :  Mar 07 Janvier 2020 à 09:49
-- Version du serveur :  10.1.41-MariaDB-0+deb9u1
-- Version de PHP :  7.2.23-1+0~20191008.27+debian9~1.gbp021266

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données :  `karuta-backend`
--

DELIMITER $$
--
-- Fonctions
--
CREATE DEFINER=`karuta`@`localhost` FUNCTION `bin2uuid` (`bin` BINARY(16)) RETURNS CHAR(36) CHARSET utf8mb4 BEGIN
  DECLARE hex CHAR(32);

  SET hex = HEX(bin);

  RETURN LOWER(CONCAT(LEFT(hex, 8),'-',
                      SUBSTR(hex, 9,4),'-',
                      SUBSTR(hex,13,4),'-',
                      SUBSTR(hex,17,4),'-',
                      RIGHT(hex, 12)
                          ));
END$$

CREATE DEFINER=`karuta`@`localhost` FUNCTION `uuid2bin` (`uuid` CHAR(36)) RETURNS BINARY(16) BEGIN
  RETURN UNHEX(REPLACE(uuid, '-',''));
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Structure de la table `annotation`
--

CREATE TABLE `annotation` (
  `nodeid` binary(16) NOT NULL,
  `rank` int(11) NOT NULL,
  `text` text COLLATE utf8_unicode_ci,
  `c_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `a_user` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `wad_identifier` varchar(45) COLLATE utf8_unicode_ci DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `configuration`
--

CREATE TABLE `configuration` (
  `id_configuration` int(10) UNSIGNED NOT NULL,
  `name` varchar(254) NOT NULL,
  `value` text,
  `modifDate` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Structure de la table `credential`
--

CREATE TABLE `credential` (
  `userid` bigint(20) NOT NULL,
  `login` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `can_substitute` int(11) NOT NULL DEFAULT '0',
  `is_admin` int(11) NOT NULL DEFAULT '0',
  `is_designer` int(11) NOT NULL DEFAULT '0',
  `active` int(11) NOT NULL DEFAULT '1',
  `display_firstname` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `display_lastname` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `password` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `token` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_date` bigint(20) DEFAULT NULL,
  `other` varchar(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT ''
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


--
-- Structure de la table `credential_group`
--

CREATE TABLE `credential_group` (
  `cg` bigint(20) NOT NULL,
  `label` varchar(255) COLLATE utf8_unicode_ci NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `credential_group_members`
--

CREATE TABLE `credential_group_members` (
  `cg` bigint(20) NOT NULL,
  `userid` bigint(20) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `credential_substitution`
--

CREATE TABLE `credential_substitution` (
  `userid` bigint(20) NOT NULL,
  `id` bigint(20) NOT NULL,
  `type` enum('USER','GROUP') COLLATE utf8_unicode_ci NOT NULL DEFAULT 'USER'
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `data_table`
--

CREATE TABLE `data_table` (
  `id` binary(16) NOT NULL,
  `owner` bigint(20) NOT NULL,
  `creator` bigint(20) NOT NULL,
  `type` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `mimetype` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `filename` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_date` bigint(20) DEFAULT NULL,
  `data` blob
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `group_group`
--

CREATE TABLE `group_group` (
  `gid` bigint(20) NOT NULL,
  `child_gid` bigint(20) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `group_info`
--

CREATE TABLE `group_info` (
  `gid` bigint(20) NOT NULL,
  `grid` bigint(20) DEFAULT NULL,
  `owner` bigint(20) NOT NULL,
  `label` varchar(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'Nouveau groupe'
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Structure de la table `group_rights`
--

CREATE TABLE `group_rights` (
  `grid` bigint(20) NOT NULL,
  `id` binary(16) NOT NULL,
  `RD` tinyint(1) NOT NULL DEFAULT '1',
  `WR` tinyint(1) NOT NULL DEFAULT '0',
  `DL` tinyint(1) NOT NULL DEFAULT '0',
  `SB` tinyint(1) NOT NULL DEFAULT '0',
  `AD` tinyint(1) NOT NULL DEFAULT '0',
  `types_id` text COLLATE utf8_unicode_ci,
  `rules_id` text COLLATE utf8_unicode_ci,
  `notify_roles` text COLLATE utf8_unicode_ci
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `group_right_info`
--

CREATE TABLE `group_right_info` (
  `grid` bigint(20) NOT NULL,
  `owner` bigint(20) NOT NULL,
  `label` varchar(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'Nouveau groupe',
  `change_rights` tinyint(1) NOT NULL DEFAULT '0',
  `portfolio_id` binary(16) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `group_user`
--

CREATE TABLE `group_user` (
  `gid` bigint(20) NOT NULL,
  `userid` bigint(20) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Structure de la table `log_table`
--

CREATE TABLE `log_table` (
  `log_id` int(12) NOT NULL,
  `log_date` datetime NOT NULL,
  `log_url` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `log_method` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `log_headers` text COLLATE utf8_unicode_ci NOT NULL,
  `log_in_body` text COLLATE utf8_unicode_ci,
  `log_out_body` text COLLATE utf8_unicode_ci,
  `log_code` int(12) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `node`
--

CREATE TABLE `node` (
  `node_uuid` binary(16) NOT NULL,
  `node_parent_uuid` binary(16) DEFAULT NULL,
  `node_children_uuid` text COLLATE utf8_unicode_ci,
  `node_order` int(12) NOT NULL,
  `metadata` text COLLATE utf8_unicode_ci NOT NULL,
  `metadata_wad` text COLLATE utf8_unicode_ci NOT NULL,
  `metadata_epm` text COLLATE utf8_unicode_ci NOT NULL,
  `res_node_uuid` binary(16) DEFAULT NULL,
  `res_res_node_uuid` binary(16) DEFAULT NULL,
  `res_context_node_uuid` binary(16) DEFAULT NULL,
  `shared_res` int(1) NOT NULL,
  `shared_node` int(1) NOT NULL,
  `shared_node_res` int(1) NOT NULL,
  `shared_res_uuid` binary(16) DEFAULT NULL,
  `shared_node_uuid` binary(16) DEFAULT NULL,
  `shared_node_res_uuid` binary(16) DEFAULT NULL,
  `asm_type` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `xsi_type` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `semtag` varchar(250) COLLATE utf8_unicode_ci DEFAULT NULL,
  `semantictag` varchar(250) COLLATE utf8_unicode_ci DEFAULT NULL,
  `label` varchar(250) COLLATE utf8_unicode_ci DEFAULT NULL,
  `code` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `descr` varchar(250) COLLATE utf8_unicode_ci DEFAULT NULL,
  `format` varchar(30) COLLATE utf8_unicode_ci DEFAULT NULL,
  `modif_user_id` int(12) NOT NULL,
  `modif_date` timestamp NULL DEFAULT NULL,
  `portfolio_id` binary(16) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `portfolio`
--

CREATE TABLE `portfolio` (
  `portfolio_id` binary(16) NOT NULL,
  `root_node_uuid` binary(16) DEFAULT NULL,
  `user_id` int(12) NOT NULL,
  `model_id` binary(16) DEFAULT NULL,
  `modif_user_id` int(12) NOT NULL,
  `modif_date` timestamp NULL DEFAULT NULL,
  `active` int(1) NOT NULL DEFAULT '1'
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


-- --------------------------------------------------------

--
-- Structure de la table `portfolio_group`
--

CREATE TABLE `portfolio_group` (
  `pg` bigint(20) NOT NULL,
  `label` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `type` enum('GROUP','PORTFOLIO') COLLATE utf8_unicode_ci NOT NULL,
  `pg_parent` bigint(20) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `portfolio_group_members`
--

CREATE TABLE `portfolio_group_members` (
  `pg` bigint(20) NOT NULL,
  `portfolio_id` binary(16) NOT NULL DEFAULT '\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0'
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `resource_table`
--

CREATE TABLE `resource_table` (
  `node_uuid` binary(16) NOT NULL,
  `xsi_type` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `content` text COLLATE utf8_unicode_ci,
  `user_id` int(11) DEFAULT NULL,
  `modif_user_id` int(12) NOT NULL,
  `modif_date` timestamp NULL DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Index pour les tables exportées
--

--
-- Index pour la table `annotation`
--
ALTER TABLE `annotation`
  ADD PRIMARY KEY (`nodeid`,`rank`);

--
-- Index pour la table `configuration`
--
ALTER TABLE `configuration`
  ADD PRIMARY KEY (`id_configuration`),
  ADD KEY `name` (`name`);

--
-- Index pour la table `credential`
--
ALTER TABLE `credential`
  ADD PRIMARY KEY (`userid`),
  ADD UNIQUE KEY `login` (`login`);

--
-- Index pour la table `credential_group`
--
ALTER TABLE `credential_group`
  ADD PRIMARY KEY (`cg`),
  ADD UNIQUE KEY `label` (`label`);

--
-- Index pour la table `credential_group_members`
--
ALTER TABLE `credential_group_members`
  ADD PRIMARY KEY (`cg`,`userid`);

--
-- Index pour la table `credential_substitution`
--
ALTER TABLE `credential_substitution`
  ADD PRIMARY KEY (`userid`,`id`,`type`);

--
-- Index pour la table `data_table`
--
ALTER TABLE `data_table`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `group_group`
--
ALTER TABLE `group_group`
  ADD PRIMARY KEY (`gid`,`child_gid`);

--
-- Index pour la table `group_info`
--
ALTER TABLE `group_info`
  ADD PRIMARY KEY (`gid`);

--
-- Index pour la table `group_rights`
--
ALTER TABLE `group_rights`
  ADD PRIMARY KEY (`grid`,`id`);

--
-- Index pour la table `group_right_info`
--
ALTER TABLE `group_right_info`
  ADD PRIMARY KEY (`grid`);

--
-- Index pour la table `group_user`
--
ALTER TABLE `group_user`
  ADD PRIMARY KEY (`gid`,`userid`);

--
-- Index pour la table `log_table`
--
ALTER TABLE `log_table`
  ADD PRIMARY KEY (`log_id`);

--
-- Index pour la table `node`
--
ALTER TABLE `node`
  ADD PRIMARY KEY (`node_uuid`),
  ADD KEY `portfolio_id` (`portfolio_id`),
  ADD KEY `node_parent_uuid` (`node_parent_uuid`),
  ADD KEY `node_order` (`node_order`),
  ADD KEY `asm_type` (`asm_type`),
  ADD KEY `res_node_uuid` (`res_node_uuid`),
  ADD KEY `res_res_node_uuid` (`res_res_node_uuid`),
  ADD KEY `res_context_node_uuid` (`res_context_node_uuid`);

--
-- Index pour la table `portfolio`
--
ALTER TABLE `portfolio`
  ADD PRIMARY KEY (`portfolio_id`),
  ADD KEY `root_node_uuid` (`root_node_uuid`);

--
-- Index pour la table `portfolio_group`
--
ALTER TABLE `portfolio_group`
  ADD PRIMARY KEY (`pg`),
  ADD UNIQUE KEY `label` (`label`);

--
-- Index pour la table `portfolio_group_members`
--
ALTER TABLE `portfolio_group_members`
  ADD PRIMARY KEY (`pg`,`portfolio_id`);

--
-- Index pour la table `resource_table`
--
ALTER TABLE `resource_table`
  ADD PRIMARY KEY (`node_uuid`);
ALTER TABLE `resource_table` ADD FULLTEXT KEY `content` (`content`);

--
-- AUTO_INCREMENT pour les tables exportées
--

--
-- AUTO_INCREMENT pour la table `configuration`
--
ALTER TABLE `configuration`
  MODIFY `id_configuration` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=426;
--
-- AUTO_INCREMENT pour la table `credential`
--
ALTER TABLE `credential`
  MODIFY `userid` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=112;
--
-- AUTO_INCREMENT pour la table `credential_group`
--
ALTER TABLE `credential_group`
  MODIFY `cg` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;
--
-- AUTO_INCREMENT pour la table `group_info`
--
ALTER TABLE `group_info`
  MODIFY `gid` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=26;
--
-- AUTO_INCREMENT pour la table `group_right_info`
--
ALTER TABLE `group_right_info`
  MODIFY `grid` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=26;
--
-- AUTO_INCREMENT pour la table `log_table`
--
ALTER TABLE `log_table`
  MODIFY `log_id` int(12) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT pour la table `portfolio_group`
--
ALTER TABLE `portfolio_group`
  MODIFY `pg` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=58;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
