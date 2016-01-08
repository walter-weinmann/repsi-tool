package edu.ou.weinmann.repsi.model.mapper;

import edu.ou.weinmann.repsi.model.util.DatabaseAccessor;
import edu.ou.weinmann.repsi.model.util.Global;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Maps the data from the calibration run to the database.
 * 
 * @author Walter Weinmann
 * 
 */
public class CalibrationMapper {

    private static final Logger LOGGER =
            Logger.getLogger(CalibrationMapper.class.getPackage().getName());

    private static final String PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL =
            "Precondition: DatabaseAccessor is missing (null)";

    private final DatabaseAccessor dbAccess;

    private int sequenceNumber;

    private final String startTime;

    /**
     * Constructs a <code>CalibrationMapper</code> object.
     * 
     * @param parSQLSyntaxCodeTarget The type of the SQL syntax version of the
     *            database system.
     * @param parStartTime The current time stamp.
     */
    public CalibrationMapper(final String parSQLSyntaxCodeTarget,
            final String parStartTime) {

        super();

        assert parSQLSyntaxCodeTarget != null : "Precondition: SQL syntax code target is missing (null)";
        assert parStartTime != null : "Precondition: start time is missing (null)";

        dbAccess =
                new DatabaseAccessor(Global.DATABASE_SCHEMA_IDENTIFIER_MASTER,
                        parSQLSyntaxCodeTarget, true);

        startTime = parStartTime;
    }

    private void checkPreconditionComparison(final String parEquals,
            final String parMessage) {

        if (parEquals == null) {
            throw new IllegalArgumentException(
                    "Comparison equals is missing (null)");
        }

        if ("".equals(parEquals)) {
            throw new IllegalArgumentException(
                    "Comparison equals is missing (empty)");
        }

        if (!("N".equals(parEquals) || "Y".equals(parEquals))) {
            throw new IllegalArgumentException("Comparison equals ("
                    + parEquals + ") is invalid (only N or Y allowed)");
        }

        if (parMessage == null) {
            throw new IllegalArgumentException(
                    "Comparison message is missing (null)");
        }

        if ("N".equals(parEquals) && "".equals(parEquals)) {
            throw new IllegalArgumentException(
                    "Comparison message is missing (empty) - mandatory if not equal");
        }
    }

    /**
     * Close the database connection.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean closeConnection() {

        assert dbAccess != null : PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL;

        return dbAccess.closeConnection();
    }

    /**
     * Creates the row in the database table <code>TMD_CALIBRATION</code>.
     * 
     * @param parColumnsDatabaseInstance The <code>Map</code> object
     *            containing the database columns.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean initialiseCalibration(
            final Map<String, Object> parColumnsDatabaseInstance) {

        if (parColumnsDatabaseInstance == null) {
            throw new IllegalArgumentException(
                    "Map containg the columns of the database instance is missing (null)");
        }

        assert dbAccess != null : PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL;

        final String lvStatement =
                ("INSERT INTO TMD_CALIBRATION "
                        + "(START_TIME, CREATED_BY, DATE_CREATED, "
                        + "DATABASE_SYSTEM_NAME, DATABASE_SYSTEM_VENDOR_NAME, "
                        + "DATABASE_SYSTEM_VERSION, JDBC_DRIVER, JDBC_URL, "
                        + "OPERATING_SYSTEM_NAME, OPERATING_SYSTEM_VENDOR_NAME, "
                        + "OPERATING_SYSTEM_VERSION, PROCESSOR_NAME, "
                        + "PROCESSOR_VENDOR_NAME, RAM_SIZE_MB, SCHEMA_NAME, "
                        + "SQL_SYNTAX_CODE_DEI, STATUS_CODE, TEST_QUERY_PAIR_ID, "
                        + "USER_NAME) VALUES ("
                        + startTime
                        + Global.SEPARATOR_COMMA_SPACE_SINGLE_QUOTE
                        + dbAccess.getUserName()
                        + "', CAST(TO_TIMESTAMP('"
                        + new SimpleDateFormat(
                                Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_JAVA)
                                .format(new Date())
                        + "', '"
                        + Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_SQL
                        + "') AS TIMESTAMP(9)), '"
                        + parColumnsDatabaseInstance
                                .get("DATABASE_SYSTEM_NAME")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsDatabaseInstance
                                .get("DATABASE_SYSTEM_VENDOR_NAME")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsDatabaseInstance
                                .get("DATABASE_SYSTEM_VERSION")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsDatabaseInstance.get("JDBC_DRIVER")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsDatabaseInstance.get("JDBC_URL")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsDatabaseInstance
                                .get("OPERATING_SYSTEM_NAME")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsDatabaseInstance
                                .get("OPERATING_SYSTEM_VENDOR_NAME")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsDatabaseInstance
                                .get("OPERATING_SYSTEM_VERSION")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsDatabaseInstance.get("PROCESSOR_NAME")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsDatabaseInstance
                                .get("PROCESSOR_VENDOR_NAME")
                        + "', "
                        + parColumnsDatabaseInstance.get("RAM_SIZE_MB")
                        + Global.SEPARATOR_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsDatabaseInstance.get("SCHEMA_NAME")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsDatabaseInstance.get("SQL_SYNTAX_CODE")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + Global.TRIAL_RUN_STATUS_START_PROGRAM
                        + "', "
                        + parColumnsDatabaseInstance.get("TEST_QUERY_PAIR_ID")
                        + Global.SEPARATOR_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsDatabaseInstance
                                .get(Global.COLUMN_NAME_USER_NAME) + "')")
                        .replaceAll("'null'", Global.NULL);

        if (!dbAccess.executeUpdate(lvStatement)) {
            final String lvMsg =
                    "CalibrationMapper: Table TMD_CALIBRATION could not be created, statement="
                            + lvStatement;
            LOGGER.log(Level.SEVERE, lvMsg);
            return false;
        }

        sequenceNumber = 0;

        return dbAccess.commit();
    }

    /**
     * Creates the row in the database table
     * <code>TMD_CALIBRATION_STATISTIC</code>.
     * 
     * @param parColumnsCalibrationStatistic The <code>Map</code> object
     *            containing the database columns.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean initialiseCalibrationStatistic(
            final Map<String, Object> parColumnsCalibrationStatistic) {

        if (parColumnsCalibrationStatistic == null) {
            throw new IllegalArgumentException(
                    "Map containg the columns of the calibration statistic is missing (null)");
        }

        assert dbAccess != null : PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL;

        final String lvStatement =
                ("INSERT INTO TMD_CALIBRATION_STATISTIC "
                        + "(START_TIME, SEQUENCE_NUMBER, ARITHMETIC_MEAN, "
                        + "GEOMETRIC_MEAN, KURTOSIS, MAXIMUM_VALUE, "
                        + "MINIMUM_VALUE, NUMBER_OF_VALUES, OBJECT, ORDER_BY, "
                        + "PERCENTILE_25, PERCENTILE_50, PERCENTILE_75, "
                        + "READINGS, SKEWNESS, SQL_STATEMENT, "
                        + "STANDARD_DEVIATION, VARIANCE) " + "VALUES ("
                        + startTime
                        + ", "
                        + ++sequenceNumber
                        + ", "
                        + parColumnsCalibrationStatistic
                                .get(Global.COLUMN_NAME_ARITHMETIC_MEAN)
                        + ", "
                        + parColumnsCalibrationStatistic
                                .get(Global.COLUMN_NAME_GEOMETRIC_MEAN)
                        + ", "
                        + parColumnsCalibrationStatistic
                                .get(Global.COLUMN_NAME_KURTOSIS)
                        + ", "
                        + parColumnsCalibrationStatistic
                                .get(Global.COLUMN_NAME_MAXIMUM_VALUE)
                        + ", "
                        + parColumnsCalibrationStatistic
                                .get(Global.COLUMN_NAME_MINIMUM_VALUE)
                        + ", "
                        + parColumnsCalibrationStatistic
                                .get(Global.COLUMN_NAME_NUMBER_OF_VALUES)
                        + ", "
                        + parColumnsCalibrationStatistic
                                .get(Global.COLUMN_NAME_OBJECT)
                        + ", "
                        + parColumnsCalibrationStatistic
                                .get(Global.COLUMN_NAME_ORDER_BY)
                        + ", "
                        + parColumnsCalibrationStatistic
                                .get(Global.COLUMN_NAME_PERCENTILE_25)
                        + ", "
                        + parColumnsCalibrationStatistic
                                .get(Global.COLUMN_NAME_PERCENTILE_50)
                        + ", "
                        + parColumnsCalibrationStatistic
                                .get(Global.COLUMN_NAME_PERCENTILE_75)
                        + ", "
                        + parColumnsCalibrationStatistic
                                .get(Global.COLUMN_NAME_READINGS)
                        + ", "
                        + parColumnsCalibrationStatistic
                                .get(Global.COLUMN_NAME_SKEWNESS)
                        + ", "
                        + parColumnsCalibrationStatistic
                                .get(Global.COLUMN_NAME_SQL_STATEMENT)
                        + ", "
                        + parColumnsCalibrationStatistic
                                .get(Global.COLUMN_NAME_STANDARD_DEVIATION)
                        + ", "
                        + parColumnsCalibrationStatistic
                                .get(Global.COLUMN_NAME_VARIANCE) + ")")
                        .replaceAll("'null'", Global.NULL);

        if (!dbAccess.executeUpdate(lvStatement)) {
            final String lvMsg =
                    "CalibrationMapper: Table TMD_CALIBRATION_STATISTIC could not be created, statement="
                            + lvStatement;
            LOGGER.log(Level.SEVERE, lvMsg);
            return false;
        }

        return dbAccess.commit();
    }

    /**
     * Updates in the database the columns <code>COMPARISON_EQUALS</code> and
     * <code>COMPARISON_MESSAGE</code>.
     * 
     * @param parEquals Y if both <code>ResultSet</code>s are equal, or N
     *            otherwise.
     * @param parMessage The new message reasoning the inequality of both
     *            <code>ResultSet</code>.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean setComparison(final String parEquals,
            final String parMessage) {

        checkPreconditionComparison(parEquals, parMessage);

        assert dbAccess != null : PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL;

        String lvMessage;

        if ("".equals(parMessage)) {
            lvMessage = Global.NULL;
        } else {
            lvMessage = "'" + parMessage.replaceAll("'", "''") + "'";
        }

        final String lvStatement =
                "UPDATE TMD_CALIBRATION " + "SET COMPARISON_EQUALS = '"
                        + parEquals + "', COMPARISON_MESSAGE = " + lvMessage
                        + " WHERE START_TIME = " + startTime;

        if (!dbAccess.executeUpdate(lvStatement)) {
            final String lvMsg =
                    "CalibrationMapper: Table TMD_CALIBRATION could not be updated, statement="
                            + lvStatement;
            LOGGER.log(Level.SEVERE, lvMsg);
            return false;
        }

        return dbAccess.commit();
    }

    /**
     * Updates in the database the columns <code>DESCRIPTION</code>.
     * 
     * @param parDescription The new description.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean setDescription(final String parDescription) {

        if (parDescription == null) {
            throw new IllegalArgumentException("Description is missing (null)");
        }

        if ("".equals(parDescription)) {
            throw new IllegalArgumentException("Description is missing (empty)");
        }

        assert dbAccess != null : PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL;

        final String lvStatement =
                "UPDATE TMD_CALIBRATION " + "SET DESCRIPTION = '"
                        + parDescription.replaceAll("'", "''")
                        + "' WHERE START_TIME = " + startTime;

        if (!dbAccess.executeUpdate(lvStatement)) {
            final String lvMsg =
                    "CalibrationMapper: Table TMD_CALIBRATION could not be updated, statement="
                            + lvStatement;
            LOGGER.log(Level.SEVERE, lvMsg);
            return false;
        }

        return dbAccess.commit();
    }

    /**
     * Updates in the database the columns <code>PATTERN_SQL_IDIOM_NAME</code>.
     * 
     * @param parPatternSqlIdiomName The new pattern SQL idiom name.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean setPatternSqlIdiomName(
            final String parPatternSqlIdiomName) {

        if (parPatternSqlIdiomName == null) {
            throw new IllegalArgumentException(
                    "Pattern SQL idiom name is missing (null)");
        }

        if ("".equals(parPatternSqlIdiomName)) {
            throw new IllegalArgumentException(
                    "Pattern SQL idiom name is missing (empty)");
        }

        assert dbAccess != null : PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL;

        final String lvStatement =
                "UPDATE TMD_CALIBRATION " + "SET PATTERN_SQL_IDIOM_NAME = '"
                        + parPatternSqlIdiomName.replaceAll("'", "''")
                        + "' WHERE START_TIME = " + startTime;

        if (!dbAccess.executeUpdate(lvStatement)) {
            final String lvMsg =
                    "CalibrationMapper: Table TMD_CALIBRATION could not be updated, statement="
                            + lvStatement;
            LOGGER.log(Level.SEVERE, lvMsg);
            return false;
        }

        return dbAccess.commit();
    }

    /**
     * Updates in the database the columns <code>SQL_SYNTAX_CODE_TTQP</code>.
     * 
     * @param parSqlSyntaxCodeTtqp The new SQL syntax code.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean setSqlSyntaxCodeTqp(final String parSqlSyntaxCodeTtqp) {

        if (parSqlSyntaxCodeTtqp == null) {
            throw new IllegalArgumentException(
                    "SQL syntax code is missing (null)");
        }

        if ("".equals(parSqlSyntaxCodeTtqp)) {
            throw new IllegalArgumentException(
                    "SQL syntax code is missing (empty)");
        }

        assert dbAccess != null : PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL;

        final String lvStatement =
                "UPDATE TMD_CALIBRATION SET SQL_SYNTAX_CODE_TTQP = '"
                        + parSqlSyntaxCodeTtqp.replaceAll("'", "''")
                        + "' WHERE START_TIME = " + startTime;

        if (!dbAccess.executeUpdate(lvStatement)) {
            final String lvMsg =
                    "CalibrationMapper: Table TMD_CALIBRATION could not be updated, statement="
                            + lvStatement;
            LOGGER.log(Level.SEVERE, lvMsg);
            return false;
        }

        return dbAccess.commit();
    }

    /**
     * Updates in the database the columns <code>END_TIME</code> and
     * <code>STATUS_CODE</code>.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean setStatus() {

        assert dbAccess != null : PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL;

        final String lvStatement =
                "UPDATE TMD_CALIBRATION "
                        + "SET END_TIME = CAST(TO_TIMESTAMP('"
                        + new SimpleDateFormat(
                                Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_JAVA)
                                .format(new Date())
                        + "', '"
                        + Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_SQL
                        + "') AS TIMESTAMP(9)), STATUS_CODE = 'EP' WHERE START_TIME = "
                        + startTime;

        if (!dbAccess.executeUpdate(lvStatement)) {
            final String lvMsg =
                    "CalibrationMapper: Table TMD_CALIBRATION could not be updated, statement="
                            + lvStatement;
            LOGGER.log(Level.SEVERE, lvMsg);
            return false;
        }

        return dbAccess.commit();
    }
}
