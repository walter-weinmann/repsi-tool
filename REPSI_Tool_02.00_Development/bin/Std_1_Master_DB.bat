set CLASSPATH=.;lib\repsi-tool.jar;lib\commmons-cli-1.0.jar;lib\commmons-math-1.1.jar;lib\jxl.jar;lib\ojdbc14.jar

set REPSI_JAVA_HOME=%JAVA_HOME_5%

rem drop an existing database schema *******************************************
%REPSI_JAVA_HOME%\bin\java -jar lib\repsi-tool.jar -mode master -fn0 in\std_master_db_schema_drop.xml -fn0xml

rem create the new database schema *********************************************
%REPSI_JAVA_HOME%\bin\java -jar lib\repsi-tool.jar -mode master -fn0 in\std_master_db_schema_create.sql

rem create the mandatory part of the database instance *************************
%REPSI_JAVA_HOME%\bin\java -jar lib\repsi-tool.jar -mode master -fn0 in\std_master_db_instance_mand.xls -fn0xls

rem create the optional part of the database instance **************************
%REPSI_JAVA_HOME%\bin\java -jar lib\repsi-tool.jar -mode master -fn0 in\std_master_db_instance_opt.xls -fn0xls

rem pause
