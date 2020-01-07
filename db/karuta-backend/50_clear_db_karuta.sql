set foreign_key_checks = false;  -- mysql

DELETE FROM annotation;
DELETE FROM configuration;
DELETE FROM credential;
DELETE FROM credential_group;

DELETE FROM credential_group_members;
DELETE FROM credential_substitution;
DELETE FROM data_table;
DELETE FROM group_group;
DELETE FROM group_info;
DELETE FROM group_user;
DELETE FROM group_rights;
DELETE FROM group_right_info;
DELETE FROM log_table;
DELETE FROM node;
DELETE FROM portfolio;
DELETE FROM portfolio_group;
DELETE FROM portfolio_group_members;
DELETE FROM resource_table;

set foreign_key_checks = true;  -- mysql
