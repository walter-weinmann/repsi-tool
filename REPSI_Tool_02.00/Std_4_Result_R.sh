#!/bin/bash
export CLASSPATH=.:lib/repsi-tool-02.00.jar:lib/commmons-cli-1.0.jar:lib/commmons-math-1.1.jar:lib/jxl.jar:lib/ojdbc14.jar

export REPSI_JAVA_HOME=$JAVA_HOME

$REPSI_JAVA_HOME/bin/java -jar lib/repsi-tool-02.00.jar -mode result_R
