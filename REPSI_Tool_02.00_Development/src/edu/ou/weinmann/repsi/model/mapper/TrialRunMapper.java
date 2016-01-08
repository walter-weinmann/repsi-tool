package edu.ou.weinmann.repsi.model.mapper;

import edu.ou.weinmann.repsi.model.util.DatabaseAccessor;
import edu.ou.weinmann.repsi.model.util.Global;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Maps the data from the trial run to the database.
 * 
 * @author Walter Weinmann
 * 
 */
public class TrialRunMapper {

    private static final Logger LOGGER =
            Logger.getLogger(TrialRunMapper.class.getPackage().getName());

    private static final String PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL =
            "Precondition: DatabaseAccessor is missing (null)";

    private final int databaseInstanceId;

    private final DatabaseAccessor dbAccess;

    private final String startTime;

    private final int testSuiteId;

    private final TrialRunProtocolMapper trialRunProtocol;

    /**
     * Constructs a <code>TrialRunMapper</code> object.
     * 
     * @param parTrialRunProtocol The <code>TralRunProtocol</code> object.
     * @param parSQLSyntaxCodeTarget The type of the SQL syntax version of the
     *            database system.
     * @param parDatabaseInstanceId The identification of the
     *            <code>DatabaseInstance</code> object.
     * @param parTestSuiteId The identification of the <code>TestSuite</code>
     *            object.
     * @param parStartTime The current time stamp.
     */
    public TrialRunMapper(final TrialRunProtocolMapper parTrialRunProtocol,
            final String parSQLSyntaxCodeTarget,
            final int parDatabaseInstanceId, final int parTestSuiteId,
            final String parStartTime) {

        super();

        assert parTrialRunProtocol != null : "Precondition: TrialRunProtocol is missing (null)";
        assert parSQLSyntaxCodeTarget != null : "Precondition: String SQL syntax code target is missing (null)";
        assert parStartTime != null : "Precondition: String start time is missing (null)";

        dbAccess =
                new DatabaseAccessor(Global.DATABASE_SCHEMA_IDENTIFIER_MASTER,
                        parSQLSyntaxCodeTarget, true);

        databaseInstanceId = parDatabaseInstanceId;
        startTime = parStartTime;
        testSuiteId = parTestSuiteId;
        trialRunProtocol = parTrialRunProtocol;
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
     * Creates the row in the database table <code>TMD_TRIAL_RUN</code>.
     * 
     * @param parColumnsDatabaseInstance The <code>Map</code> object
     *            containing the columns of the database table
     *            <code>TMD_DATABASE_INSTANCE</code>.
     * @param parColumnsTestSuite The <code>Map</code> object containing the
     *            columns of the database table <code>TMD_TEST_SUITE</code>.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean initialise(
            final Map<String, Object> parColumnsDatabaseInstance,
            final Map<String, Object> parColumnsTestSuite) {

        if (parColumnsDatabaseInstance == null) {
            throw new IllegalArgumentException(
                    "Map containg the columns of the database instance is missing (null)");
        }

        if (parColumnsTestSuite == null) {
            throw new IllegalArgumentException(
                    "Map containg the columns of the test suite is missing (null)");
        }

        assert dbAccess != null : PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL;

        final String lvStatement =
                ("INSERT INTO TMD_TRIAL_RUN "
                        + "(DATABASE_INSTANCE_ID, TEST_SUITE_ID, START_TIME, "
                        + "CREATED_BY, DATE_CREATED, DATABASE_SYSTEM_NAME, "
                        + "DATABASE_SYSTEM_VENDOR_NAME, DATABASE_SYSTEM_VERSION, "
                        + "JDBC_DRIVER, JDBC_URL, OPERATING_SYSTEM_NAME, "
                        + "OPERATING_SYSTEM_VENDOR_NAME, OPERATING_SYSTEM_VERSION, "
                        + "PROCESSOR_NAME, PROCESSOR_VENDOR_NAME, RAM_SIZE_MB, "
                        + "SCHEMA_NAME, SQL_SYNTAX_CODE, STATUS_CODE, "
                        + "TEST_SUITE_DESCRIPTION, TEST_SUITE_NAME, USER_NAME) "
                        + "VALUES ("
                        + databaseInstanceId
                        + ", "
                        + testSuiteId
                        + ", "
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
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsTestSuite.get("TEST_SUITE_DESCRIPTION")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsTestSuite.get("TEST_SUITE_NAME")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsDatabaseInstance
                                .get(Global.COLUMN_NAME_USER_NAME) + "')")
                        .replaceAll("'null'", Global.NULL);

        if (!dbAccess.executeUpdate(lvStatement)) {
            final String lvMsg =
                    "TrialRunMapper: Table TMD_TRIAL_RUN could not be created, statement="
                            + lvStatement;
            trialRunProtocol.createErrorProtocol(lvMsg, false);
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
                "UPDATE TMD_TRIAL_RUN " + "SET DESCRIPTION = '"
                        + parDescription + "' WHERE DATABASE_INSTANCE_ID = "
                        + databaseInstanceId + " AND TEST_SUITE_ID = "
                        + testSuiteId + " AND START_TIME = " + startTime;

        if (!dbAccess.executeUpdate(lvStatement)) {
            trialRunProtocol.createErrorProtocol(
                    "TrialRunMapper: Table TMD_TRIAL_RUN could not be updated, statement="
                            + lvStatement, false);
            return false;
        }

        return dbAccess.commit();
    }

    /**
     * Updates in the database the columns <code>END_TIME</code> and
     * <code>STATUS_CODE</code>.
     * 
     * @param parStatus The new status.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean setStatus(final String parStatus) {

        if (parStatus == null) {
            throw new IllegalArgumentException("Status is missing (null)");
        }

        if ("".equals(parStatus)) {
            throw new IllegalArgumentException("Status is missing (empty)");
        }

        assert dbAccess != null : PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL;

        final String lvStatement =
                "UPDATE TMD_TRIAL_RUN "
                        + "SET END_TIME = CAST(TO_TIMESTAMP('"
                        + new SimpleDateFormat(
                                Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_JAVA)
                                .format(new Date()) + "', '"
                        + Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_SQL
                        + "') AS TIMESTAMP(9)), STATUS_CODE = '" + parStatus
                        + "' WHERE DATABASE_INSTANCE_ID = "
                        + databaseInstanceId + " AND TEST_SUITE_ID = "
                        + testSuiteId + " AND START_TIME = " + startTime;

        if (!dbAccess.executeUpdate(lvStatement)) {
            trialRunProtocol.createErrorProtocol(
                    "TrialRunMapper: Table TMD_TRIAL_RUN could not be updated, statement="
                            + lvStatement, false);
            return false;
        }

        return dbAccess.commit();
    }
}
