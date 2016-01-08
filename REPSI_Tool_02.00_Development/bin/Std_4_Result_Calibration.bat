set CLASSPATH=.;lib\repsi-tool.jar;lib\commmons-cli-1.0.jar;lib\commmons-math-1.1.jar;lib\jxl.jar;lib\ojdbc14.jar

set REPSI_JAVA_HOME=%JAVA_HOME_5%

%REPSI_JAVA_HOME%\bin\java -jar lib\repsi-tool.jar -mode result_cn -efn out\CalibrationData.xls
pause
