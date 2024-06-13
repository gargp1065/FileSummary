#!/bin/bash
source ~/.bash_profile
set -x
VAR=""
build_path="${APP_HOME}/smsUssdSummary/"
build="smsUssdSummary.jar"
cd $build_path
status=`ps -ef | grep $build | grep application_$1_$2.properties | grep java`
if [ "$status" != "$VAR" ]
  then
    echo "Process Already Running"
  else
    echo "Starting Process"
    java -Dlog4j.configurationFile=file:./log4j2_$1.xml -jar $build --spring.config.location=file:./application_$1_$2.properties,/u01/eirsapp/configuration/configuration.properties 1>/dev/null 2>/dev/null &
    echo "Process Started"
fi