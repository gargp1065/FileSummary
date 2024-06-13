#!/bin/bash
conffile=/u01/eirsapp/configuration/configuration.properties
typeset -A config # init array

while read line
do
    if echo $line | grep -F = &>/dev/null
    then
        varname=$(echo "$line" | cut -d '=' -f 1)
        config[$varname]=$(echo "$line" | cut -d '=' -f 2-)
    fi
done < $conffile
conn1="mysql -h${config[dbIp]} -P${config[dbPort]} -u${config[dbUsername]} -p${config[dbPassword]}"
conn="mysql -h${config[dbIp]} -P${config[dbPort]} -u${config[dbUsername]} -p${config[dbPassword]} ${config[appdbName]}"

echo "creating app database."
${conn1} -e "CREATE DATABASE IF NOT EXISTS app;"
echo "app database successfully created!"

`${conn} <<EOFMYSQL

CREATE TABLE if not exists sms_usage_1205 (
  id int NOT NULL AUTO_INCREMENT,
  processed_on timestamp DEFAULT CURRENT_TIMESTAMP,
  sms_recieved_date timestamp NULL DEFAULT NULL,
  short_code int DEFAULT NULL,
  msisdn varchar(20) DEFAULT NULL,
  request_from_customer varchar(250) DEFAULT NULL,
  response_date_time timestamp NULL DEFAULT NULL,
  response varchar(250) DEFAULT NULL,
  request_lang varchar(20) DEFAULT NULL,
  response_lang varchar(20) DEFAULT NULL,
  operator varchar(20) DEFAULT NULL,
  file_name varchar(50) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE if not exists ussd_usage_1205 (
  id int NOT NULL AUTO_INCREMENT,
  processed_on timestamp DEFAULT CURRENT_TIMESTAMP,
  modified_on timestamp DEFAULT CURRENT_TIMESTAMP,
  request_init_date timestamp NULL DEFAULT NULL,
  request_type varchar(10) DEFAULT NULL,
  ussd_session_id varchar(100) DEFAULT NULL,
  msisdn varchar(20) DEFAULT NULL,
  imsi varchar(20) DEFAULT NULL,
  input varchar(255) DEFAULT NULL,
  session_end_date_time timestamp NULL DEFAULT NULL,
  reason_session_close varchar(250) DEFAULT NULL,
  response_lang varchar(20) DEFAULT NULL,
  operator varchar(20) DEFAULT NULL,
  file_name varchar(50) DEFAULT NULL,
  PRIMARY KEY (id)
);



insert into cfg_feature_alert (alert_id, description, feature) values ('alert5500', 'The file does not exists for USSD summary.','USSD Summary');
insert into cfg_feature_alert (alert_id, description, feature) values ('alert5501', 'The processing failed for USSD file <e> for operator <process_name>','USSD Summary');
insert into cfg_feature_alert (alert_id, description, feature) values ('alert5502', 'The processing failed for USSD summary.','USSD Summary');
insert into cfg_feature_alert (alert_id, description, feature) values ('alert5600', 'The file does not exists for SMS summary.','SMS Summary');
insert into cfg_feature_alert (alert_id, description, feature) values ('alert5601', 'The processing failed for SMS file <e> for operator <process_name>','SMS Summary');
insert into cfg_feature_alert (alert_id, description, feature) values ('alert5602', 'The processing failed for SMS summary.','SMS Summary');
EOFMYSQL`

echo "creating aud database."
${conn1} -e "CREATE DATABASE IF NOT EXISTS aud;"
echo "aud database successfully created!"

conn2="mysql -h${config[dbIp]} -P${config[dbPort]} -u${config[dbUsername]} -p${config[dbPassword]} ${config[auddbName]}"

`${conn2} << EOFMYSQL
alter table modules_audit_trail modify error_message varchar(1000);
EOFMYSQL`
echo "tables creation completed."
echo "                                             *
						  ***
						 *****
						  ***
						   *                           "
echo "********************Thank You DB Process is completed now for SMS USSD Summary Module*****************"
