package edu.ou.weinmann.repsi.model.calibration;

import edu.ou.weinmann.repsi.model.mapper.CalibrationMapper;
import edu.ou.weinmann.repsi.model.mapper.DatabaseInstanceMapper;

import edu.ou.weinmann.repsi.model.trial.util.ResultSetComparator;

import edu.ou.weinmann.repsi.model.util.Configurator;
import edu.ou.weinmann.repsi.model.util.DatabaseAccessor;
import edu.ou.weinmann.repsi.model.util.DatabaseToExcel;
import edu.ou.weinmann.repsi.model.util.Global;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.math.BigDecimal;

import java.sql.SQLException;
import java.sql.Timestamp;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.sql.TIMESTAMP;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

/**
 * Manages the calibration functionality of the REPSI tool:
 * 
 * <ul>
 * <li>executes a calibration run with the method
 * <code>System.nanoTime, </code></li>
 * <li>executes a calibration run with a SQL test query pair,</li>
 * <li>extracts the results of all calibration runs from the database into an
 * Excel file.</li>
 * </ul>
 * 
 * @author Walter Weinmann
 * 
 */
public final class Calibration {

    private static final Logger LOGGER =
            Logger.getLogger(Calibration.class.getPackage().getName());

    private static final int MAX_COLUMNS_CALIBRATION_STATISTIC = 16;

    private static final String OBJECT_TYPE_NANOTIME = "nanotime";

    private static final String OBJECT_TYPE_NANOTIME_EXT = "System.nanoTime";

    private static final String OBJECT_TYPE_QUERY_EXT = "Test Query Pair";

    private String appliedPatternOrderBy;

    private String appliedPatternSelectStmnt;

    private Map<String, Object> columnsDatabaseInstance;

    private Configurator configurator;

    private DatabaseToExcel databaseToExcel;

    private DatabaseAccessor dbAccessMaster1;

    private DatabaseAccessor dbAccessMaster2;

    private DatabaseAccessor dbAccessTest;

    private DatabaseAccessor dbAccessTestApplied;

    private DatabaseAccessor dbAccessTestUnapplied;

    private DescriptiveStatistics descriptiveStatistics;

    private String object;

    private String patternSQLIdiomName;

    private String proprtiesFilename;

    private boolean propertiesXml;

    private String sqlSyntaxCodeTqp;

    private String unappliedPatternOrderBy;

    private String unappliedPatternSelectStmnt;

    /**
     * Constructs a <code>Calibration</code> object. The name of the used
     * properties file is taken from mthe class
     * <code>edu.ou.weinmann.repsi.model.util.Global</code> and does not
     * constitute an XML document.
     */
    public Calibration() {

        this(Global.PROPERTIES_FILE_NAME, false);
    }

    /**
     * Constructs a <code>Calibration</code> object.
     * 
     * @param parProprtiesFilename complete filename of the properties file
     *            including the directory.
     * @param parPropertiesXml <code>true</code> if the properties file
     *            constitutes an XML document.
     */
    public Calibration(final String parProprtiesFilename,
            final boolean parPropertiesXml) {

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.entering(this.getClass().getName(), "Trial", new Object[] {
                    parProprtiesFilename, Boolean.valueOf(parPropertiesXml), });
        }

        Configurator.removeInstance();

        configurator =
                Configurator
                        .getInstance(parProprtiesFilename, parPropertiesXml);

        dbAccessMaster1 =
                new DatabaseAccessor(
                        Global.DATABASE_SCHEMA_IDENTIFIER_MASTER,
                        Configurator
                                .getInstance(parProprtiesFilename,
                                        parPropertiesXml)
                                .getProperty(
                                        Global.PROPERTY_PATH_1_DATABASE
                                                + "."
                                                + Global.DATABASE_SCHEMA_IDENTIFIER_MASTER
                                                + "."
                                                + Global.PROPERTY_PATH_3_SQL_SYNTAX_CODE),
                        false);

        proprtiesFilename = parProprtiesFilename;
        propertiesXml = parPropertiesXml;

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.exiting(this.getClass().getName(), "Trial");
        }
    }

    private double[] calculateTimeQuantities(final long[] parTimeQuantities,
            final boolean parIgnoreFirst, final long parPrecision) {

        int lvStart = 1;

        if (parIgnoreFirst) {
            lvStart++;
        }

        if (parTimeQuantities.length < lvStart + 1) {
            return new double[] {};
        }

        final double[] lvNanoPeriods =
                new double[parTimeQuantities.length - lvStart];

        int lvCurr = 0;

        for (int i = lvStart; i < parTimeQuantities.length; i++) {

            final long lvNanoSeconds =
                    parTimeQuantities[i] - parTimeQuantities[i - 1];

            long lvTimePeriods;

            if (parPrecision == 1) {
                lvTimePeriods = lvNanoSeconds;
            } else {
                lvTimePeriods = lvNanoSeconds / parPrecision;
                if ((lvNanoSeconds - lvTimePeriods * parPrecision) > parPrecision / 2) {
                    lvTimePeriods++;
                }
            }

            lvNanoPeriods[lvCurr++] = lvTimePeriods;
        }

        return lvNanoPeriods;
    }

    private void calculateStatistics(final double[] parMicroPeriods,
            final String parObjectTypeExt, final boolean parVerbose) {

        if (parVerbose) {
            calculateStatisticsProtEntry(parObjectTypeExt);
        }

        if (parMicroPeriods.length <= 1) {
            descriptiveStatistics = null;

            if (parVerbose) {
                if (parMicroPeriods.length == 1) {
                    System.out.printf("Value #1:           %,38.0f\n", Double
                            .valueOf(parMicroPeriods[0]));
                    System.out
                            .println("-------------------------------------------------------------------");
                }
                System.out.printf("Number of values:   %,38d\n", Integer
                        .valueOf(1));
            }
        } else {

            descriptiveStatistics = DescriptiveStatistics.newInstance();

            int lvCurr = 0;

            for (int i = 0; i < parMicroPeriods.length; i++) {
                descriptiveStatistics.addValue(parMicroPeriods[i]);
                if (parVerbose && i < 5) {
                    System.out.printf("Value #" + ++lvCurr
                            + ":           %,38.0f\n", Double
                            .valueOf(parMicroPeriods[i]));
                }
            }

            if (parVerbose) {
                calculateStatisticsProt(descriptiveStatistics);
            }
        }

        if (parVerbose) {
            calculateStatisticsProtExit(parObjectTypeExt);
        }
    }

    private void calculateStatisticsProt(final DescriptiveStatistics lvStats) {

        System.out
                .println("---------------------------------------------------------------------");
        System.out.printf("Number of values:   %,38d\n", Long.valueOf(lvStats
                .getN()));
        System.out.printf("Arithmetic mean:    %,38.0f\n", Double
                .valueOf(lvStats.getMean()));
        System.out.printf("Geometric  mean:    %,38.0f\n", Double
                .valueOf(lvStats.getGeometricMean()));
        System.out.printf("Kurtosis:           %,38.0f\n", Double
                .valueOf(lvStats.getKurtosis()));
        System.out.printf("Skewness:           %,38.0f\n", Double
                .valueOf(lvStats.getSkewness()));
        System.out.printf("Standard Deviation: %,38.0f\n", Double
                .valueOf(lvStats.getStandardDeviation()));
        System.out.printf("Variance:           %,38.0f\n", Double
                .valueOf(lvStats.getVariance()));
        System.out.printf("Minimum:            %,38.0f\n", Double
                .valueOf(lvStats.getMin()));
        System.out.printf("Quartile 1:         %,38.0f\n", Double
                .valueOf(lvStats.getPercentile(25)));
        System.out.printf("Median:             %,38.0f\n", Double
                .valueOf(lvStats.getPercentile(50)));
        System.out.printf("Quartile 3:         %,38.0f\n", Double
                .valueOf(lvStats.getPercentile(75)));
        System.out.printf("Maximum:            %,38.0f\n", Double
                .valueOf(lvStats.getMax()));
    }

    private void calculateStatisticsProtExit(final String parObjectTypeExt) {

        System.out
                .println("---------------------------------------------------------------------");
        System.out.println("End   Calibration of Object " + parObjectTypeExt);
        System.out
                .println("=====================================================================");
    }

    private void calculateStatisticsProtEntry(final String parObjectTypeExt) {

        System.out
                .println("=====================================================================");
        System.out.println("Start Calibration of Object " + parObjectTypeExt);
        System.out
                .println("---------------------------------------------------------------------");
    }

    /**
     * Exports all the data of a callibration run from the master database into
     * an Excel file.
     * 
     * @param parFileName The complete file name of the Excel file including the
     *            directory.
     * 
     * @return <code>true</code> if the calibration data were exported without
     *         any error, and <code>false</code> otherwise.
     */
    public boolean calibrationDataToExcel(final String parFileName) {

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.entering(this.getClass().getName(),
                    "calibrationDataToExcel", new Object[] { parFileName, });
        }

        if (parFileName == null || "".equals(parFileName)) {
            throw new IllegalArgumentException("Filename is missing");
        }

        // Create a database connection and a statement ************************
        if (!(getConnectionMaster1() && createStatementMaster1())) {
            return false;
        }

        // Processing **********************************************************
        boolean lvIsOk = false;

        if (calibrationDataToExcelInitialise(parFileName)
                && calibrationDataToExcelAllData()
                && calibrationDataToExcelFinalise()) {
            lvIsOk = true;
        }

        // Commit the transaction and close the database connection ************
        if (!(commitMaster1() && closeConnectionMaster1())) {
            return false;
        }

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.exiting(this.getClass().getName(), "calibrationDataToExcel",
                    Boolean.valueOf(lvIsOk));
        }

        return lvIsOk;
    }

    private boolean calibrationDataToExcelAllData() {

        // Table TMD_CALIBRATION ***********************************************
        if (!dbAccessMaster1
                .executeQuery("SELECT * FROM TMD_CALIBRATION ORDER BY "
                        + "START_TIME;")) {
            return false;
        }

        int lvSheet = 0;

        if (!databaseToExcel.createSheet("TMD_CALIBRATION", lvSheet++,
                dbAccessMaster1.getResultSetObject(), true)) {
            LOGGER.log(Level.SEVERE, databaseToExcel.getLastErrorMsg());
            return false;
        }

        // Table TMD_CALIBRATION_STATISTIC *************************************
        if (!dbAccessMaster1
                .executeQuery("SELECT * FROM TMD_CALIBRATION_STATISTIC ORDER BY "
                        + "START_TIME, SEQUENCE_NUMBER;")) {
            return false;
        }

        if (!databaseToExcel.createSheet("TMD_CALIBRATION_STATISTIC", lvSheet,
                dbAccessMaster1.getResultSetObject(), true)) {
            LOGGER.log(Level.SEVERE, databaseToExcel.getLastErrorMsg());
            return false;
        }

        return true;
    }

    private boolean calibrationDataToExcelFinalise() {

        if (!databaseToExcel.closeWorkbook()) {
            LOGGER.log(Level.SEVERE, databaseToExcel.getLastErrorMsg());
            return false;
        }

        return true;
    }

    private boolean calibrationDataToExcelInitialise(final String parFileName) {

        databaseToExcel = new DatabaseToExcel();

        if (!databaseToExcel.createWorkbook(parFileName)) {
            LOGGER.log(Level.SEVERE, databaseToExcel.getLastErrorMsg());
            return false;
        }

        return true;
    }

    /**
     * Exports all the data of a callibration run from the master database into
     * an R style command files.
     * 
     * @param parDirectoryNameIn The directory to store the R command files.
     * 
     * @return <code>true</code> if the command files were created without any
     *         error, and <code>false</code> otherwise.
     */
    public boolean calibrationDataToR(final String parDirectoryNameIn) {

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.entering(this.getClass().getName(), "calibrationDataToR",
                    new Object[] { parDirectoryNameIn, });
        }

        if (parDirectoryNameIn == null || "".equals(parDirectoryNameIn)) {
            throw new IllegalArgumentException(
                    "Directory to store the input files is missing");
        }

        // Create a database connection and a statement ************************
        if (!(getConnectionMaster1() && createStatementMaster1())) {
            return false;
        }

        if (!(calibrationDataToRInitialise() && getConnectionMaster2() && createStatementMaster2())) {
            closeConnectionMaster1();
            return false;
        }

        // Processing **********************************************************
        boolean lvIsOk = false;

        if (calibrationDataToRAllObject(parDirectoryNameIn)
                && calibrationDataToRAllQuery(parDirectoryNameIn)) {
            lvIsOk = true;
        }

        // Commit the transaction and close the database connection ************
        if (!(commitMaster2() && closeConnectionMaster2())) {
            closeConnectionMaster1();
            return false;
        }

        if (!(commitMaster1() && closeConnectionMaster1())) {
            return false;
        }

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.exiting(this.getClass().getName(), "calibrationDataToR",
                    Boolean.valueOf(lvIsOk));
        }

        return lvIsOk;
    }

    private boolean calibrationDataToRAllObject(final String parDirectoryNameIn) {

        // Table TMD_CALIBRATION ***********************************************
        if (!dbAccessMaster1
                .executeQuery("SELECT CN.START_TIME AS START_TIME, "
                        + "CNS.OBJECT AS OBJECT, CNS.READINGS AS READINGS "
                        + "FROM TMD_CALIBRATION CN, TMD_CALIBRATION_STATISTIC CNS "
                        + "WHERE CN.START_TIME = CNS.START_TIME "
                        + "AND CN.TEST_QUERY_PAIR_ID IS NULL "
                        + "ORDER BY START_TIME;")) {
            return false;
        }

        while (dbAccessMaster1.next()) {

            Timestamp lvStartTime;

            try {
                lvStartTime =
                        ((TIMESTAMP) dbAccessMaster1.getColumn(1))
                                .timestampValue();
            } catch (SQLException e) {
                LOGGER
                        .log(
                                Level.SEVERE,
                                "SQLException with oracle.sql.TIMESTAMP.timestampValue() / TMD_TRIAL_RUN",
                                e);
                return false;
            }

            final String lvObject = (String) dbAccessMaster1.getColumn(2);

            final BigDecimal[] lvReadings =
                    dbAccessMaster1.getArrayBigDecimal(3);

            // Open command file ***********************************************

            BufferedWriter lvFile = null;

            try {
                new File(parDirectoryNameIn).mkdir();
                lvFile =
                        new BufferedWriter(new FileWriter(parDirectoryNameIn
                                + "/Object_"
                                + lvObject
                                + "_"
                                + lvStartTime.toString().replaceAll(" ", "_")
                                        .replaceAll(":", "-") + ".R"));
            } catch (IOException e) {
                LOGGER
                        .log(
                                Level.SEVERE,
                                "IOException with new BufferedWriter(new FileWriter(...))",
                                e);
                return false;
            }

            // Remove existing vector ******************************************
            try {
                lvFile.write("if (exists(\"R\")) remove(\"R\")");
                lvFile.newLine();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,
                        "IOException with write() - remove existing vector", e);
                return false;
            }

            // Create vector ***********************************************
            try {
                lvFile.write("R<-c(");

                for (int j = 0; j < lvReadings.length; j++) {
                    if (j > 0) {
                        lvFile.append(',');
                    }

                    lvFile.write(Long.toString(lvReadings[j].longValue()));
                }

                lvFile.append(')');
                lvFile.newLine();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,
                        "IOException with write() - create vector", e);
                return false;
            }

            // Create boxplots *************************************************
            try {
                lvFile.write("boxplot(R,"
                        + "col=\"lightblue\",horizontal=TRUE,"
                        + "match=TRUE,notch=TRUE)");
                lvFile.newLine();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,
                        "IOException with write() - create boxplot", e);
                return false;
            }

            // Create boxplots.stats *******************************************
            try {
                lvFile.write("boxplot.stats(R)");
                lvFile.newLine();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,
                        "IOException with write() - create boxplot.stats", e);
                return false;
            }

            // Create summaries ************************************************
            try {
                lvFile.write("summary(R)");
                lvFile.newLine();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,
                        "IOException with write() - create summary", e);
                return false;
            }

            // Close command file **********************************************
            try {
                lvFile.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "IOException with close() - file", e);
            }
        }

        return true;
    }

    private boolean calibrationDataToRAllQuery(final String parDirectoryNameIn) {

        // Table TMD_CALIBRATION ***********************************************
        if (!dbAccessMaster1
                .executeQuery("SELECT START_TIME, TEST_QUERY_PAIR_ID "
                        + "FROM TMD_CALIBRATION "
                        + "WHERE TEST_QUERY_PAIR_ID IS NOT NULL "
                        + "ORDER BY START_TIME;")) {
            return false;
        }

        while (dbAccessMaster1.next()) {

            Timestamp lvStartTime;

            try {
                lvStartTime =
                        ((TIMESTAMP) dbAccessMaster1.getColumn(1))
                                .timestampValue();
            } catch (SQLException e) {
                LOGGER
                        .log(
                                Level.SEVERE,
                                "SQLException with oracle.sql.TIMESTAMP.timestampValue() / TMD_TRIAL_RUN",
                                e);
                return false;
            }

            final String lvTestQueryPairId =
                    dbAccessMaster1.getColumn(2).toString();

            // Open command file ***********************************************

            BufferedWriter lvFile = null;

            try {
                new File(parDirectoryNameIn).mkdir();
                lvFile =
                        new BufferedWriter(new FileWriter(parDirectoryNameIn
                                + "/Query_"
                                + lvTestQueryPairId
                                + "_"
                                + lvStartTime.toString().replaceAll(" ", "_")
                                        .replaceAll(":", "-") + ".R"));
            } catch (IOException e) {
                LOGGER
                        .log(
                                Level.SEVERE,
                                "IOException with new BufferedWriter(new FileWriter(...))",
                                e);
                return false;
            }

            // Remove existing vectors *****************************************
            try {
                lvFile.write("if (exists(\"A_A\")) remove(\"A_A\")");
                lvFile.newLine();
                lvFile.write("if (exists(\"A_U\")) remove(\"A_U\")");
                lvFile.newLine();
                lvFile.write("if (exists(\"C_A\")) remove(\"C_A\")");
                lvFile.newLine();
                lvFile.write("if (exists(\"C_U\")) remove(\"C_U\")");
                lvFile.newLine();
            } catch (IOException e) {
                LOGGER
                        .log(
                                Level.SEVERE,
                                "IOException with write() - remove existing vectors",
                                e);
                return false;
            }

            // Table TMD_CALIBRATION_STATISTIC
            // *****************************************************************
            if (!dbAccessMaster2.executeQuery("SELECT OBJECT, READINGS "
                    + "FROM TMD_CALIBRATION_STATISTIC "
                    + "WHERE START_TIME = CAST(TO_TIMESTAMP('"
                    + lvStartTime.toString() + "', '"
                    + Global.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS_SSS_SQL
                    + "') AS TIMESTAMP(9)) ORDER BY " + "SEQUENCE_NUMBER;")) {
                return false;
            }

            while (dbAccessMaster2.next()) {
                final String lvObject = (String) dbAccessMaster2.getColumn(1);

                final boolean lvAlternating = lvObject.contains("Alternating");
                final boolean lvApplied = lvObject.contains("Applied");

                final BigDecimal[] lvReadings =
                        dbAccessMaster2.getArrayBigDecimal(2);

                // Create vector ***********************************************
                try {
                    if (lvAlternating) {
                        if (lvApplied) {
                            lvFile.write("A_A<-c(");
                        } else {
                            lvFile.write("A_U<-c(");
                        }
                    } else {
                        if (lvApplied) {
                            lvFile.write("C_A<-c(");
                        } else {
                            lvFile.write("C_U<-c(");
                        }
                    }

                    for (int j = 0; j < lvReadings.length; j++) {
                        if (j > 0) {
                            lvFile.append(',');
                        }

                        lvFile.write(Long.toString(lvReadings[j].longValue()));
                    }

                    lvFile.append(')');
                    lvFile.newLine();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE,
                            "IOException with write() - create vector", e);
                    return false;
                }
            }

            // Create boxplots *************************************************
            try {
                lvFile.write("if (exists(\"A_U\")) boxplot(A_A,A_U,"
                        + "col=\"lightblue\",horizontal=TRUE,"
                        + "match=TRUE,names=c(\"(A_A)\",\"(A_U)\"),"
                        + "notch=TRUE)");
                lvFile.newLine();
                lvFile.write("if (exists(\"C_U\")) boxplot(C_A,C_U,"
                        + "col=\"lightblue\",horizontal=TRUE,"
                        + "match=TRUE,names=c(\"(C_A)\",\"(C_U)\"),"
                        + "notch=TRUE)");
                lvFile.newLine();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,
                        "IOException with write() - create boxplots", e);
                return false;
            }

            // Create boxplots.stats *******************************************
            try {
                lvFile.write("if (exists(\"A_U\")) boxplot.stats(A_U)");
                lvFile.newLine();
                lvFile.write("if (exists(\"A_A\")) boxplot.stats(A_A)");
                lvFile.newLine();
                lvFile.write("if (exists(\"C_U\")) boxplot.stats(C_U)");
                lvFile.newLine();
                lvFile.write("if (exists(\"C_A\")) boxplot.stats(C_A)");
                lvFile.newLine();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,
                        "IOException with write() - create boxplots.stats", e);
                return false;
            }

            // Create summaries ************************************************
            try {
                lvFile.write("if (exists(\"A_U\")) summary(A_U)");
                lvFile.newLine();
                lvFile.write("if (exists(\"A_A\")) summary(A_A)");
                lvFile.newLine();
                lvFile.write("if (exists(\"C_U\")) summary(C_U)");
                lvFile.newLine();
                lvFile.write("if (exists(\"C_A\")) summary(C_A)");
                lvFile.newLine();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,
                        "IOException with write() - create summaries", e);
                return false;
            }

            // Close command file **********************************************
            try {
                lvFile.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "IOException with close() - file", e);
            }
        }

        return true;
    }

    private boolean calibrationDataToRInitialise() {

        dbAccessMaster2 =
                new DatabaseAccessor(
                        Global.DATABASE_SCHEMA_IDENTIFIER_MASTER,
                        Configurator
                                .getInstance(proprtiesFilename, propertiesXml)
                                .getProperty(
                                        Global.PROPERTY_PATH_1_DATABASE
                                                + "."
                                                + Global.DATABASE_SCHEMA_IDENTIFIER_MASTER
                                                + "."
                                                + Global.PROPERTY_PATH_3_SQL_SYNTAX_CODE),
                        false);

        return true;
    }

    /**
     * Calibrates the execution of a pair of SQL statements.
     * 
     * @param parTestQueryPairId The identification of the test query pair to be
     *            calibrated.
     * @param parCycles The number of cycles to be executed.
     * @param parDatabaseInstanceId The identification of the used test database
     *            instance.
     * @param parDescription The description of the calibration run.
     * @param parAlternating Whether in every cycle should be performed
     *            alternatively the execution of the first query and then of the
     *            second query.
     * @param parConsecutive Whether first all cycles should be done with the
     *            first query and afterwards the same number of cycles with the
     *            second query.
     * @param parFetchSize The fetch size.
     * @param parIgnoreFirst Whether the first measured response time (reading)
     *            should be ignored.
     * @param parPrecision The exponent (base 10) of the required precision for
     *            the response time. Calibrates the execution of a simple Java
     *            method.
     * @param parVerbose Whether to print a statistical overview.
     * 
     * @return <code>true</code> if the processing ended without any error,
     *         and <code>false</code> otherwise.
     */
    public boolean calibrateQuery(final int parTestQueryPairId,
            final int parCycles, final int parDatabaseInstanceId,
            final String parDescription, final boolean parAlternating,
            final boolean parConsecutive, final int parFetchSize,
            final boolean parIgnoreFirst, final long parPrecision,
            final boolean parVerbose) {

        final CalibrationMapper lvCalibration =
                createCalibration(parDatabaseInstanceId, Integer
                        .toString(parTestQueryPairId), parDescription);

        if (lvCalibration == null) {
            return false;
        }

        if (!getTestQueryPair(lvCalibration, parTestQueryPairId)) {
            return false;
        }

        dbAccessTest =
                new DatabaseAccessor(Global.DATABASE_SCHEMA_IDENTIFIER_TEST,
                        (String) columnsDatabaseInstance
                                .get(Global.COLUMN_NAME_SQL_SYNTAX_CODE),
                        (String) columnsDatabaseInstance
                                .get(Global.COLUMN_NAME_JDBC_DRIVER), false);

        dbAccessTestApplied =
                new DatabaseAccessor(Global.DATABASE_SCHEMA_IDENTIFIER_TEST,
                        (String) columnsDatabaseInstance
                                .get(Global.COLUMN_NAME_SQL_SYNTAX_CODE),
                        (String) columnsDatabaseInstance
                                .get(Global.COLUMN_NAME_JDBC_DRIVER), false);

        dbAccessTestUnapplied =
                new DatabaseAccessor(Global.DATABASE_SCHEMA_IDENTIFIER_TEST,
                        (String) columnsDatabaseInstance
                                .get(Global.COLUMN_NAME_SQL_SYNTAX_CODE),
                        (String) columnsDatabaseInstance
                                .get(Global.COLUMN_NAME_JDBC_DRIVER), false);

        // Create a database connection.
        if (!dbAccessTest.getConnection((String) columnsDatabaseInstance
                .get("JDBC_URL"), (String) columnsDatabaseInstance
                .get(Global.COLUMN_NAME_USER_NAME),
                (String) columnsDatabaseInstance.get("PASSWORD"))) {
            LOGGER.log(Level.SEVERE,
                    "Problem with getConnection() / trial database");
            return false;
        }

        // Create a statement.
        if (!dbAccessTest.createStatement()) {
            dbAccessTest.closeConnection();

            LOGGER.log(Level.SEVERE,
                    "Problem with createStatement() / trial database");
            return false;
        }

        if (!dbAccessTest.setFetchSize(parFetchSize)) {
            dbAccessTest.closeConnection();

            LOGGER.log(Level.SEVERE, "Problem with setFetchSize("
                    + parFetchSize + ") / trial database");
            return false;
        }

        // Enable SQL Trace.
        if (dbAccessTest.getSqlSyntaxCodeTarget().equals(
                Global.SQL_SYNTAX_CODE_ORACLE_10G)) {
            if (!dbAccessTest.executeQuery("ALTER SESSION SET sql_trace=true;")) {
                dbAccessTest.closeConnection();

                LOGGER
                        .log(
                                Level.SEVERE,
                                "Problem with executeQuery(\"ALTER SESSION SET sql_trace=true;\") / trial database");
                return false;
            }
        }

        // Create a database connection. Applied
        if (!dbAccessTestApplied.getConnection((String) columnsDatabaseInstance
                .get("JDBC_URL"), (String) columnsDatabaseInstance
                .get(Global.COLUMN_NAME_USER_NAME),
                (String) columnsDatabaseInstance
                        .get(Global.COLUMN_NAME_PASSWORD))) {

            LOGGER.log(Level.SEVERE,
                    "Problem with getConnection() / trial database (applied)");
            return false;
        }

        // Create a statement.
        if (!dbAccessTestApplied.createStatement()) {
            dbAccessTestApplied.closeConnection();

            LOGGER
                    .log(Level.SEVERE,
                            "Problem with createStatement() / trial database (applied)");
            return false;
        }

        // Create a database connection. Unapplied
        if (!dbAccessTestUnapplied.getConnection(
                (String) columnsDatabaseInstance
                        .get(Global.COLUMN_NAME_JDBC_URL),
                (String) columnsDatabaseInstance
                        .get(Global.COLUMN_NAME_USER_NAME),
                (String) columnsDatabaseInstance
                        .get(Global.COLUMN_NAME_PASSWORD))) {

            LOGGER
                    .log(Level.SEVERE,
                            "Problem with getConnection() / trial database (not applied)");
            return false;
        }

        // Create a statement.
        if (!dbAccessTestUnapplied.createStatement()) {
            dbAccessTestUnapplied.closeConnection();

            LOGGER
                    .log(Level.SEVERE,
                            "Problem with createStatement() / trial database (not applied)");
            return false;
        }

        evaluateQuery(lvCalibration, parCycles, parAlternating, parConsecutive,
                parIgnoreFirst, parPrecision, parVerbose);

        return calibrateQueryCloseConnection(lvCalibration);
    }

    private boolean calibrateQueryCloseConnection(
            final CalibrationMapper parCalibration) {

        // Compare the result sets.
        boolean lvReturn = calibrateQueryComparison(parCalibration);

        // Disable SQL Trace.
        if (dbAccessTest.getSqlSyntaxCodeTarget().equals(
                Global.SQL_SYNTAX_CODE_ORACLE_10G)) {
            if (!dbAccessTest
                    .executeQuery("ALTER SESSION SET sql_trace=false;")) {
                LOGGER
                        .log(
                                Level.SEVERE,
                                "Problem with executeQuery(\"ALTER SESSION SET sql_trace=false;\") / trial database");
                lvReturn = false;
            }
        }

        // Close the connections.
        if (!dbAccessTest.closeConnection()) {
            LOGGER.log(Level.SEVERE,
                    "Problem with closeConnection() / trial database");
            lvReturn = false;
        }

        if (!dbAccessTestApplied.closeConnection()) {
            LOGGER
                    .log(Level.SEVERE,
                            "Problem with closeConnection() / trial database (applied)");
            lvReturn = false;
        }

        if (!dbAccessTestUnapplied.closeConnection()) {
            LOGGER
                    .log(Level.SEVERE,
                            "Problem with closeConnection() / trial database (not applied)");
            lvReturn = false;
        }

        return lvReturn;
    }

    private boolean calibrateQueryComparison(
            final CalibrationMapper parCalibration) {

        if ("".equals(appliedPatternOrderBy)
                || "".equals(unappliedPatternOrderBy)) {
            return true;
        }

        final ResultSetComparator lvComparator = new ResultSetComparator();

        lvComparator.setOrderBy(unappliedPatternOrderBy, 0);
        lvComparator.setOrderBy(appliedPatternOrderBy, 1);

        lvComparator.setSelectStmnt(unappliedPatternSelectStmnt, 0);
        lvComparator.setSelectStmnt(appliedPatternSelectStmnt, 1);

        lvComparator.setSqlSyntaxCode(sqlSyntaxCodeTqp);

        if (!lvComparator.compare(new DatabaseAccessor[] {
                dbAccessTestUnapplied, dbAccessTestApplied, })) {
            final String lvMessage = lvComparator.getLastErrorMsg();

            if (!(parCalibration.setComparison("N", lvMessage))) {
                return false;
            }

            return true;
        }

        if (!(parCalibration.setComparison("Y", ""))) {
            return false;
        }

        return true;
    }

    /**
     * Calibrates the execution of a simple Java method.
     * 
     * @param parObject The specification of the Java method to be calibrated.
     * @param parCycles The number of cycles to be executed.
     * @param parDatabaseInstanceId The identification of the used test database
     *            instance.
     * @param parDescription The description of the calibration run.
     * @param parIgnoreFirst Whether the first measured response time (reading)
     *            should be ignored.
     * @param parPrecision The exponent (base 10) of the required precision for
     *            the response time.
     * @param parVerbose Whether to print a statistical overview.
     * 
     * @return <code>true</code> if the processing ended without any error,
     *         and <code>false</code> otherwise.
     */
    public boolean calibrateSimpleMethod(final String parObject,
            final int parCycles, final int parDatabaseInstanceId,
            final String parDescription, final boolean parIgnoreFirst,
            final long parPrecision, final boolean parVerbose) {

        final CalibrationMapper lvCalibration =
                createCalibration(parDatabaseInstanceId, "", parDescription);
        if (lvCalibration == null) {
            return false;
        }

        if (OBJECT_TYPE_NANOTIME.equals(parObject)) {
            if (!evaluateTimeQuantities(lvCalibration, parCycles,
                    parIgnoreFirst, parPrecision, parVerbose)) {
                return false;
            }
        } else {
            LOGGER.log(Level.SEVERE, "Unknown object type" + parObject
                    + " / calibrateSimpleMethod()");
            return false;
        }

        return true;
    }

    private boolean closeConnectionMaster1() {

        if (!dbAccessMaster1.closeConnection()) {

            LOGGER.log(Level.SEVERE,
                    "Error with closeConnection() - closeConnectionMaster1()");
            return false;
        }

        return true;
    }

    private boolean closeConnectionMaster2() {

        if (!dbAccessMaster2.closeConnection()) {

            LOGGER.log(Level.SEVERE,
                    "Error with closeConnection() - closeConnectionMaster2()");
            return false;
        }

        return true;
    }

    private boolean commitMaster1() {

        if (!dbAccessMaster1.commit()) {
            closeConnectionMaster1();

            LOGGER.log(Level.SEVERE, "Error with commit() - commitMaster1()");
            return false;
        }

        return true;
    }

    private boolean commitMaster2() {

        if (!dbAccessMaster2.commit()) {
            closeConnectionMaster2();

            LOGGER.log(Level.SEVERE, "Error with commit() - commitMaster2()");
            return false;
        }

        return true;
    }

    private CalibrationMapper createCalibration(
            final int parDatabaseInstanceId, final String parTestQueryPairId,
            final String parDescription) {

        final String lvCurrStartTime =
                "CAST(TO_TIMESTAMP('"
                        + new SimpleDateFormat(
                                Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_JAVA)
                                .format(new Date()) + "', '"
                        + Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_SQL
                        + "') AS TIMESTAMP(9))";

        final CalibrationMapper lvCalibration =
                new CalibrationMapper(
                        configurator
                                .getProperty(Global.PROPERTY_PATH_1_DATABASE
                                        + "."
                                        + Global.DATABASE_SCHEMA_IDENTIFIER_MASTER
                                        + "."
                                        + Global.PROPERTY_PATH_3_SQL_SYNTAX_CODE),
                        lvCurrStartTime);

        columnsDatabaseInstance =
                new DatabaseInstanceMapper(configurator
                        .getProperty(Global.PROPERTY_PATH_1_DATABASE + "."
                                + Global.DATABASE_SCHEMA_IDENTIFIER_MASTER
                                + "." + Global.PROPERTY_PATH_3_SQL_SYNTAX_CODE))
                        .getDatabaseInstance(parDatabaseInstanceId);
        if (columnsDatabaseInstance == null) {
            return null;
        }

        String lvTestQueryPairId = parTestQueryPairId;

        if (lvTestQueryPairId.equals("")) {
            lvTestQueryPairId = "NULL";
        }

        columnsDatabaseInstance.put("TEST_QUERY_PAIR_ID", lvTestQueryPairId);

        // Callibration Persistence ********************************************
        if (!lvCalibration.initialiseCalibration(columnsDatabaseInstance)) {
            return null;
        }

        lvCalibration.setDescription(parDescription);

        return lvCalibration;
    }

    private boolean createCalibrationStatistics(
            final CalibrationMapper lvCalibration,
            final Map<String, Object> lvColumns) {

        if (!lvCalibration.initialiseCalibrationStatistic(lvColumns)) {
            return false;
        }

        if (!lvCalibration.setStatus()) {
            return false;
        }

        return true;
    }

    private boolean createStatementMaster1() {

        if (!dbAccessMaster1.createStatement()) {
            closeConnectionMaster1();

            LOGGER.log(Level.SEVERE,
                    "Error with createStatement() - createStatementMaster1()");
            return false;
        }

        return true;
    }

    private boolean createStatementMaster2() {

        if (!dbAccessMaster2.createStatement()) {
            closeConnectionMaster2();

            LOGGER.log(Level.SEVERE,
                    "Error with createStatement() - createStatementMaster2()");
            return false;
        }

        return true;
    }

    private boolean evaluateTimeQuantities(
            final CalibrationMapper parCalibration, final int parCycles,
            final boolean parIgnoreFirst, final long parPrecision,
            final boolean parVerbose) {

        int lvMax = parCycles + 1;

        if (parIgnoreFirst) {
            lvMax++;
        }

        final long[] lvNanoTimes = new long[lvMax];

        for (int i = 0; i < lvMax; i++) {
            lvNanoTimes[i] = System.nanoTime();
        }

        final double[] lvMicroPeriods =
                calculateTimeQuantities(lvNanoTimes, parIgnoreFirst,
                        parPrecision);

        calculateStatistics(lvMicroPeriods, OBJECT_TYPE_NANOTIME_EXT,
                parVerbose);

        final Map<String, Object> lvColumns = getColumnsCalibrationStatistic();

        lvColumns.put(Global.COLUMN_NAME_OBJECT, "'" + OBJECT_TYPE_NANOTIME_EXT
                + "'");
        lvColumns.put(Global.COLUMN_NAME_SQL_STATEMENT, Global.NULL);

        if (!createCalibrationStatistics(parCalibration, lvColumns)) {
            return false;
        }

        return true;
    }

    private void evaluateQuery(final CalibrationMapper parCalibration,
            final int parCycles, final boolean parAlternating,
            final boolean parConsecutive, final boolean parIgnoreFirst,
            final long parPrecision, final boolean parVerbose) {

        int lvMax = parCycles;
        if (parIgnoreFirst) {
            lvMax++;
        }

        final double[] lvTimeQuantitiesApplied = new double[parCycles];
        final double[] lvTimeQuantitiesUnapplied = new double[parCycles];

        // Statistics: Alternating - Applied & Unapplied Version of the Query
        if (parAlternating) {
            executeQueryAlternating(parCalibration, parIgnoreFirst, lvMax,
                    parPrecision, lvTimeQuantitiesApplied,
                    lvTimeQuantitiesUnapplied, parVerbose);
        }

        if (!parConsecutive) {
            return;
        }

        // Statistics: Consecutive - Unapplied Version of the Query
        evaluateQueryUnappliedConsecutive(parCalibration, parIgnoreFirst,
                lvMax, parPrecision, lvTimeQuantitiesUnapplied, parVerbose);

        // Statistics: Consecutive - Applied Version of the Query
        evaluateQueryAppliedConsecutive(parCalibration, parIgnoreFirst, lvMax,
                parPrecision, lvTimeQuantitiesApplied, parVerbose);
    }

    private void evaluateQueryApplied(final CalibrationMapper parCalibration,
            final double[] parTimeQuantities, final String parObject,
            final boolean parVerbose) {

        calculateStatistics(parTimeQuantities, parObject, parVerbose);

        final Map<String, Object> lvColumns = getColumnsCalibrationStatistic();

        lvColumns.put(Global.COLUMN_NAME_OBJECT, "'" + parObject + "'");
        lvColumns.put(Global.COLUMN_NAME_ORDER_BY, "'" + appliedPatternOrderBy
                + "'");
        lvColumns.put(Global.COLUMN_NAME_SQL_STATEMENT, "'"
                + appliedPatternSelectStmnt.replaceAll("'", "''") + "'");

        createCalibrationStatistics(parCalibration, lvColumns);
    }

    private void evaluateQueryAppliedConsecutive(
            final CalibrationMapper parCalibration,
            final boolean parIgnoreFirst, final int parMax,
            final long parPrecision, final double[] parTimeQuantities,
            final boolean parVerbose) {

        int lvCurr = 0;

        for (int i = 0; i < parMax; i++) {
            if (!dbAccessTest.executeQueryTrialRun(appliedPatternSelectStmnt,
                    sqlSyntaxCodeTqp)) {
                return;
            }

            if (!(parIgnoreFirst && (i == 0))) {
                parTimeQuantities[lvCurr++] =
                        dbAccessTest.getTrialTimeQuantities(parPrecision);
            }
        }

        evaluateQueryApplied(parCalibration, parTimeQuantities,
                OBJECT_TYPE_QUERY_EXT + " (Consecutive - Applied)", parVerbose);
    }

    private void evaluateQueryUnapplied(final CalibrationMapper parCalibration,
            final double[] parTimeQuantities, final String parObject,
            final boolean parVerbose) {

        calculateStatistics(parTimeQuantities, parObject, parVerbose);

        final Map<String, Object> lvColumns = getColumnsCalibrationStatistic();

        lvColumns.put(Global.COLUMN_NAME_OBJECT, "'" + parObject + "'");
        lvColumns.put(Global.COLUMN_NAME_ORDER_BY, "'"
                + unappliedPatternOrderBy + "'");
        lvColumns.put(Global.COLUMN_NAME_SQL_STATEMENT, "'"
                + unappliedPatternSelectStmnt.replaceAll("'", "''") + "'");

        createCalibrationStatistics(parCalibration, lvColumns);
    }

    private void evaluateQueryUnappliedConsecutive(
            final CalibrationMapper parCalibration,
            final boolean parIgnoreFirst, final int parMax,
            final long parPrecision, final double[] parTimeQuantities,
            final boolean parVerbose) {

        int lvCurr = 0;

        for (int i = 0; i < parMax; i++) {
            if (!dbAccessTest.executeQueryTrialRun(unappliedPatternSelectStmnt,
                    sqlSyntaxCodeTqp)) {
                return;
            }

            if (!(parIgnoreFirst && (i == 0))) {
                parTimeQuantities[lvCurr++] =
                        dbAccessTest.getTrialTimeQuantities(parPrecision);
            }
        }

        evaluateQueryUnapplied(parCalibration, parTimeQuantities,
                OBJECT_TYPE_QUERY_EXT + " (Consecutive - Unapplied)",
                parVerbose);
    }

    private void executeQueryAlternating(
            final CalibrationMapper parCalibration,
            final boolean parIgnoreFirst, final int parMax,
            final long parPrecision, final double[] parTimeQuantitiesApplied,
            final double[] parTimeQuantitiesUnapplied, final boolean parVerbose) {

        int lvCurr = 0;

        // Statistics: Alternating
        for (int i = 0; i < parMax; i++) {
            if (!dbAccessTest.executeQueryTrialRun(appliedPatternSelectStmnt,
                    sqlSyntaxCodeTqp)) {
                return;
            }

            if (!(parIgnoreFirst && (i == 0))) {
                parTimeQuantitiesApplied[lvCurr] =
                        dbAccessTest.getTrialTimeQuantities(parPrecision);
            }

            if (!dbAccessTest.executeQueryTrialRun(unappliedPatternSelectStmnt,
                    sqlSyntaxCodeTqp)) {
                return;
            }

            if (!(parIgnoreFirst && (i == 0))) {
                parTimeQuantitiesUnapplied[lvCurr] =
                        dbAccessTest.getTrialTimeQuantities(parPrecision);
            }

            if (!(parIgnoreFirst && (i == 0))) {
                lvCurr++;
            }
        }

        // Statistics: Alternating - Unapplied Version of the Query
        evaluateQueryUnapplied(parCalibration, parTimeQuantitiesUnapplied,
                OBJECT_TYPE_QUERY_EXT + " (Alternating - Unapplied)",
                parVerbose);

        // Statistics: Alternating - Applied Version of the Query
        evaluateQueryApplied(parCalibration, parTimeQuantitiesApplied,
                OBJECT_TYPE_QUERY_EXT + " (Alternating - Applied)", parVerbose);
    }

    private Map<String, Object> getColumnsCalibrationStatistic() {

        final Map<String, Object> lvColumns =
                new HashMap<String, Object>(MAX_COLUMNS_CALIBRATION_STATISTIC);

        if (descriptiveStatistics == null) {
            return initColumnsCalibrationStatistic();
        }

        lvColumns.put(Global.COLUMN_NAME_ARITHMETIC_MEAN, new BigDecimal(
                descriptiveStatistics.getMean()).setScale(5,
                BigDecimal.ROUND_HALF_EVEN).toString());
        lvColumns.put(Global.COLUMN_NAME_GEOMETRIC_MEAN, new BigDecimal(
                descriptiveStatistics.getGeometricMean()).setScale(5,
                BigDecimal.ROUND_HALF_EVEN).toString());

        final double lvKurtosis = descriptiveStatistics.getKurtosis();
        if (Double.isNaN(lvKurtosis)) {
            lvColumns.put(Global.COLUMN_NAME_KURTOSIS, Global.NULL);
        } else {
            lvColumns.put(Global.COLUMN_NAME_KURTOSIS, new BigDecimal(
                    lvKurtosis).setScale(5, BigDecimal.ROUND_HALF_EVEN)
                    .toString());
        }

        lvColumns.put(Global.COLUMN_NAME_MAXIMUM_VALUE, new BigDecimal(
                descriptiveStatistics.getMax()).toString());

        lvColumns.put(Global.COLUMN_NAME_MINIMUM_VALUE, new BigDecimal(
                descriptiveStatistics.getMin()).toString());

        final long lvNumberOfValues = descriptiveStatistics.getN();

        lvColumns.put(Global.COLUMN_NAME_NUMBER_OF_VALUES, new BigDecimal(
                lvNumberOfValues).toString());

        getColumnsValibrationStatisticPercentile(lvColumns);

        final StringBuffer lvReadings = new StringBuffer("CT_MD_READINGS(");

        for (int i = 0; i < lvNumberOfValues; i++) {

            if (i > 0) {
                lvReadings.append(',');
            }

            lvReadings.append(new BigDecimal(descriptiveStatistics
                    .getElement(i)).toString());
        }

        lvColumns.put(Global.COLUMN_NAME_READINGS, lvReadings.append(')')
                .toString());

        final double lvSkewness = descriptiveStatistics.getSkewness();
        if (Double.isNaN(lvKurtosis)) {
            lvColumns.put(Global.COLUMN_NAME_SKEWNESS, Global.NULL);
        } else {
            lvColumns.put(Global.COLUMN_NAME_SKEWNESS, new BigDecimal(
                    lvSkewness).setScale(5, BigDecimal.ROUND_HALF_EVEN)
                    .toString());
        }

        lvColumns.put(Global.COLUMN_NAME_STANDARD_DEVIATION, new BigDecimal(
                descriptiveStatistics.getStandardDeviation()).setScale(5,
                BigDecimal.ROUND_HALF_EVEN).toString());

        lvColumns.put(Global.COLUMN_NAME_VARIANCE, new BigDecimal(
                descriptiveStatistics.getVariance()).setScale(5,
                BigDecimal.ROUND_HALF_EVEN).toString());

        return lvColumns;
    }

    private void getColumnsValibrationStatisticPercentile(
            final Map<String, Object> parColumns) {

        parColumns.put(Global.COLUMN_NAME_PERCENTILE_25, new BigDecimal(
                descriptiveStatistics.getPercentile(25)).setScale(5,
                BigDecimal.ROUND_HALF_EVEN).toString());
        parColumns.put(Global.COLUMN_NAME_PERCENTILE_50, new BigDecimal(
                descriptiveStatistics.getPercentile(50)).setScale(5,
                BigDecimal.ROUND_HALF_EVEN).toString());
        parColumns.put(Global.COLUMN_NAME_PERCENTILE_75, new BigDecimal(
                descriptiveStatistics.getPercentile(75)).setScale(5,
                BigDecimal.ROUND_HALF_EVEN).toString());
    }

    private boolean getConnectionMaster1() {

        if (!dbAccessMaster1.getConnection()) {
            LOGGER.log(Level.SEVERE, "Error with getConnectionMaster1()");
            return false;
        }

        return true;
    }

    private boolean getConnectionMaster2() {

        if (!dbAccessMaster2.getConnection()) {
            LOGGER.log(Level.SEVERE, "Error with getConnectionMaster2()");
            return false;
        }

        return true;
    }

    /**
     * Returns the type of the <code>Calibration</code> object.
     * 
     * @return the type of the <code>Calibration</code> object.
     */
    public String getObject() {

        return object;
    }

    private boolean getTestQueryPair(final CalibrationMapper parCalibration,
            final int parTestQueryPairId) {

        final DatabaseAccessor lvDBAccess =
                new DatabaseAccessor(
                        Global.DATABASE_SCHEMA_IDENTIFIER_MASTER,
                        configurator
                                .getProperty(Global.PROPERTY_PATH_1_DATABASE
                                        + "."
                                        + Global.DATABASE_SCHEMA_IDENTIFIER_MASTER
                                        + "."
                                        + Global.PROPERTY_PATH_3_SQL_SYNTAX_CODE),
                        false);

        // Create a database connection.
        if (!lvDBAccess.getConnection(configurator
                .getProperty(Global.PROPERTY_PATH_1_DATABASE + "."
                        + Global.DATABASE_SCHEMA_IDENTIFIER_MASTER + "."
                        + Global.PROPERTY_PATH_3_URL), configurator
                .getProperty(Global.PROPERTY_PATH_1_DATABASE + "."
                        + Global.DATABASE_SCHEMA_IDENTIFIER_MASTER + "."
                        + Global.PROPERTY_PATH_3_USER_NAME), configurator
                .getProperty(Global.PROPERTY_PATH_1_DATABASE + "."
                        + Global.DATABASE_SCHEMA_IDENTIFIER_MASTER + "."
                        + Global.PROPERTY_PATH_3_PASSWORD))) {
            LOGGER.log(Level.SEVERE,
                    "Problem with getConnection() / master database");
            return false;
        }

        // Create a statement.
        if (!lvDBAccess.createStatement()) {
            lvDBAccess.closeConnection();

            LOGGER.log(Level.SEVERE,
                    "Problem with createStatement() / master database");
            return false;
        }

        final String lvStatement =
                "SELECT APPLIED_PATTERN_ORDER_BY, APPLIED_PATTERN_SELECT_STMNT, "
                        + "NAME, SQL_SYNTAX_CODE, UNAPPLIED_PATTERN_ORDER_BY, "
                        + "UNAPPLIED_PATTERN_SELECT_STMNT "
                        + "FROM TMD_PATTERN_SQL_IDIOM PNSI, TMD_TEST_QUERY_PAIR  TTQP "
                        + "WHERE TTQP.TEST_QUERY_PAIR_ID = "
                        + parTestQueryPairId
                        + " AND TTQP.PATTERN_SQL_IDIOM_ID = "
                        + "PNSI.PATTERN_SQL_IDIOM_ID";

        if (!lvDBAccess.executeQuery(lvStatement)) {
            LOGGER.log(Level.SEVERE, "Test query pair=" + parTestQueryPairId
                    + " problem with SQL statement=" + lvStatement);
            lvDBAccess.closeConnection();
            return false;
        }

        if (!lvDBAccess.next()) {
            LOGGER.log(Level.SEVERE, "Test query pair=" + parTestQueryPairId
                    + " is not available");
            lvDBAccess.closeConnection();
            return false;
        }

        appliedPatternOrderBy =
                (String) lvDBAccess
                        .getColumn(Global.COLUMN_NAME_APPLIED_PATTERN_ORDER_BY);
        if (appliedPatternOrderBy == null) {
            appliedPatternOrderBy = "";
        }

        appliedPatternSelectStmnt =
                (String) lvDBAccess
                        .getColumn(Global.COLUMN_NAME_APPLIED_PATTERN_SELECT_STMNT);
        patternSQLIdiomName =
                (String) lvDBAccess.getColumn(Global.COLUMN_NAME_NAME);
        sqlSyntaxCodeTqp =
                (String) lvDBAccess
                        .getColumn(Global.COLUMN_NAME_SQL_SYNTAX_CODE);

        unappliedPatternOrderBy =
                (String) lvDBAccess
                        .getColumn(Global.COLUMN_NAME_UNAPPLIED_PATTERN_ORDER_BY);
        if (unappliedPatternOrderBy == null) {
            unappliedPatternOrderBy = "";
        }

        unappliedPatternSelectStmnt =
                (String) lvDBAccess
                        .getColumn(Global.COLUMN_NAME_UNAPPLIED_PATTERN_SELECT_STMNT);

        if (!parCalibration.setPatternSqlIdiomName(patternSQLIdiomName)) {
            lvDBAccess.closeConnection();
            return false;
        }

        if (!parCalibration.setSqlSyntaxCodeTqp(sqlSyntaxCodeTqp)) {
            lvDBAccess.closeConnection();
            return false;
        }

        lvDBAccess.closeConnection();

        return true;
    }

    private Map<String, Object> initColumnsCalibrationStatistic() {

        final Map<String, Object> lvColumns =
                new HashMap<String, Object>(MAX_COLUMNS_CALIBRATION_STATISTIC);

        lvColumns.put(Global.COLUMN_NAME_ARITHMETIC_MEAN, Global.NULL);
        lvColumns.put(Global.COLUMN_NAME_GEOMETRIC_MEAN, Global.NULL);
        lvColumns.put(Global.COLUMN_NAME_KURTOSIS, Global.NULL);
        lvColumns.put(Global.COLUMN_NAME_MAXIMUM_VALUE, Global.NULL);
        lvColumns.put(Global.COLUMN_NAME_MINIMUM_VALUE, Global.NULL);
        lvColumns.put(Global.COLUMN_NAME_NUMBER_OF_VALUES, Global.NULL);
        lvColumns.put(Global.COLUMN_NAME_PERCENTILE_25, Global.NULL);
        lvColumns.put(Global.COLUMN_NAME_PERCENTILE_50, Global.NULL);
        lvColumns.put(Global.COLUMN_NAME_PERCENTILE_75, Global.NULL);
        lvColumns.put(Global.COLUMN_NAME_READINGS, Global.NULL);
        lvColumns.put(Global.COLUMN_NAME_SKEWNESS, Global.NULL);
        lvColumns.put(Global.COLUMN_NAME_STANDARD_DEVIATION, Global.NULL);
        lvColumns.put(Global.COLUMN_NAME_VARIANCE, Global.NULL);

        return lvColumns;
    }

    /**
     * Sets the type of the <code>Calibration</code> object.
     * 
     * @param parObject The type of the <code>Calibration</code> object.
     */
    public void setObject(final String parObject) {

        object = parObject;
    }
}
