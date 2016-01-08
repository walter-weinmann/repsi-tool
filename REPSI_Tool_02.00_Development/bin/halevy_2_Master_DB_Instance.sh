#!/bin/bash
export CLASSPATH=.:lib/repsi-tool.jar:lib/commmons-cli-1.0.jar:lib/commmons-math-1.1.jar:lib/jxl.jar:lib/ojdbc14.jar

export REPSI_JAVA_HOME=$JAVA_HOME

# create the relevant part of the database instance ****************************
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar -mode master -fn0 in/halevy_master_db_instance.xls -fn0xls
