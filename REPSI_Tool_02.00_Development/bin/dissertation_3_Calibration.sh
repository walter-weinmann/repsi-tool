#!/bin/bash
export CLASSPATH=.:lib/repsi-tool.jar:lib/commmons-cli-1.0.jar:lib/commmons-math-1.1.jar:lib/jxl.jar:lib/ojdbc14.jar

export DES=Dissertation
export DI=3
export PREC=3
export REPSI_JAVA_HOME=$JAVA_HOME
#export VERBOSE=-verbose:gc
export VERBOSE=

# Start Processing **************************************************************
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80101 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80102 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80103 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80104 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80105 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80111 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80112 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80113 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80114 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80115 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80121 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80122 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80123 -prec $PREC -exalt -des $DES

$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80301 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80311 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80321 -prec $PREC -exalt -des $DES

$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80401 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80402 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80411 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80421 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80431 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80432 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80433 -prec $PREC -exalt -des $DES

$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80501 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80502 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80503 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80511 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80512 -prec $PREC -exalt -des $DES

$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80601 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80602 -prec $PREC -exalt -des $DES

$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80701 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80702 -prec $PREC -exalt -des $DES

$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80801 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80802 -prec $PREC -exalt -des $DES

$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80901 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80902 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80903 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80911 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80912 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 80913 -prec $PREC -exalt -des $DES

$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 81001 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 81002 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 81003 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 81011 -prec $PREC -exalt -des $DES

$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 86101 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 86111 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 86121 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 86131 -prec $PREC -exalt -des $DES

$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 86301 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 86302 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 86303 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 86304 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 86305 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 86306 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 86307 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 86308 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 86309 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 86310 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 86311 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 86312 -prec $PREC -exalt -des $DES

$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 86401 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 86411 -prec $PREC -exalt -des $DES

$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 87201 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 87211 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 87212 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 87213 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 87214 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 87215 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 87216 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 87221 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 87222 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 87231 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 87232 -prec $PREC -exalt -des $DES

$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 87301 -prec $PREC -exalt -des $DES

$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 87401 -prec $PREC -exalt -des $DES
$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 87402 -prec $PREC -exalt -des $DES

$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool.jar $VERBOSE -mode calibration -obj query -di $DI -tqp 99901 -prec $PREC -exalt -des $DES
