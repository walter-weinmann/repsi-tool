package edu.ou.weinmann.repsi.model.mapper;

import edu.ou.weinmann.repsi.model.util.DatabaseAccessor;
import edu.ou.weinmann.repsi.model.util.Global;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Maps the data from the trial run actions to the database.
 * 
 * @author Walter Weinmann
 * 
 */
public class TrialRunActionMapper {

    private static final String AND_SEQUENCE_NUMBER_ACTION =
            " AND SEQUENCE_NUMBER_ACTION = ";

    private static final String AND_START_TIME = " AND START_TIME = ";

    private static final String AND_TEST_SUITE_ID = " AND TEST_SUITE_ID = ";

    private static final Logger LOGGER =
            Logger.getLogger(TrialRunActionMapper.class.getPackage().getName());

    private static final String PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL =
            "Precondition: DatabaseAccessor is missing (null)";

    private static final String SINGLEQUOTE_COMMA_SPACE_SINGLEQUOTE = "', '";

    private static final String UPDATE_TMD_TRIAL_RUN_ACTION =
            "UPDATE TMD_TRIAL_RUN_ACTION ";

    private static final String WHERE_DATABASE_INSTANCE_ID =
            "' WHERE DATABASE_INSTANCE_ID = ";

    private final int databaseInstanceId;

    private final DatabaseAccessor dbAccess;

    private long sequenceNumberAction;

    private final String startTime;

    private final int testSuiteId;

    private final TrialRunProtocolMapper trialRunProtocol;

    /**
     * Constructs a <code>TrialRunActionMapper</code> object.
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
    public TrialRunActionMapper(
            final TrialRunProtocolMapper parTrialRunProtocol,
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
        sequenceNumberAction = 0L;
        startTime = parStartTime;
        testSuiteId = parTestSuiteId;
        trialRunProtocol = parTrialRunProtocol;
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

    private void checkPreconditionEndActionError(final Date parStartTime,
            final Date parEndTime) {

        if (parStartTime == null) {
            throw new IllegalArgumentException(
                    "Start date and time is missing (null)");
        }

        if (parEndTime == null) {
            throw new IllegalArgumentException(
                    "End date and time is missing (null)");
        }
    }

    private void checkPreconditionEndActionError(final String parErrorMessage) {

        if (parErrorMessage == null) {
            throw new IllegalArgumentException(
                    "Error message is missing (null)");
        }

        if ("".equals(parErrorMessage)) {
            throw new IllegalArgumentException(
                    "Error message is missing (empty)");
        }
    }

    private void checkPreconditionStartAction(final String parStatement) {

        if (parStatement == null) {
            throw new IllegalArgumentException("Statement is missing (null)");
        }

        if ("".equals(parStatement)) {
            throw new IllegalArgumentException("Statement is missing (empty)");
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

    private boolean handleDatabaseError(final String parStatement,
            final String parMethod) {

        final String lvMsg =
                "TrialRunActionMapper " + parMethod
                        + ": Table TMD_TRIAL_RUN_ACTION ("
                        + sequenceNumberAction
                        + ") could not be created, statement="
                        + parStatement.replaceAll("'", "''");

        trialRunProtocol.createErrorProtocol(lvMsg, false);
        LOGGER.log(Level.SEVERE, lvMsg);

        return false;
    }

    /**
     * Creates the row in the database table <code>TMD_TRIAL_RUN_ACTION</code>.
     * 
     * @param parColumnsTestSuiteAction The <code>Map</code> object containing
     *            the database columns.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean initialise(
            final Map<String, Object> parColumnsTestSuiteAction) {

        if (parColumnsTestSuiteAction == null) {
            throw new IllegalArgumentException(
                    "Map containg the columns of the test suite action is missing (null)");
        }

        assert dbAccess != null : PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL;

        final String lvStatement =
                ("INSERT INTO TMD_TRIAL_RUN_ACTION "
                        + "(DATABASE_INSTANCE_ID, TEST_SUITE_ID, START_TIME, "
                        + "SEQUENCE_NUMBER_ACTION, APPLIED_PATTERN_ORDER_BY, "
                        + "APPLIED_PATTERN_SELECT_STMNT, EXECUTION_FREQUENCY, "
                        + "OPERATION_CODE, OPERATION_TYPE, "
                        + "PATTERN_SQL_IDIOM_NAME, SQL_SYNTAX_CODE, TABLE_NAME, "
                        + "TEST_QUERY_PAIR_DESCRIPTION, "
                        + "TEST_SUITE_ACTION_DESCRIPTION, TEST_SUITE_OPERATION_NAME, "
                        + "TEST_TABLE_DESCRIPTION, UNAPPLIED_PATTERN_ORDER_BY, "
                        + "UNAPPLIED_PATTERN_SELECT_STMNT) VALUES ("
                        + databaseInstanceId
                        + ", "
                        + testSuiteId
                        + ", "
                        + startTime
                        + ", "
                        + sequenceNumberAction
                        + Global.SEPARATOR_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsTestSuiteAction
                                .get("APPLIED_PATTERN_ORDER_BY")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsTestSuiteAction
                                .get("APPLIED_PATTERN_SELECT_STMNT")
                        + "', "
                        + parColumnsTestSuiteAction.get("EXECUTION_FREQUENCY")
                        + Global.SEPARATOR_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsTestSuiteAction.get("OPERATION_CODE")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsTestSuiteAction.get("OPERATION_TYPE")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsTestSuiteAction
                                .get("PATTERN_SQL_IDIOM_NAME")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsTestSuiteAction.get("SQL_SYNTAX_CODE")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsTestSuiteAction.get("TABLE_NAME")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsTestSuiteAction
                                .get("TEST_QUERY_PAIR_DESCRIPTION")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsTestSuiteAction
                                .get("TEST_SUITE_ACTION_DESCRIPTION")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsTestSuiteAction
                                .get("TEST_SUITE_OPERATION_NAME")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsTestSuiteAction
                                .get("TEST_TABLE_DESCRIPTION")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsTestSuiteAction
                                .get("UNAPPLIED_PATTERN_ORDER_BY")
                        + Global.SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE
                        + parColumnsTestSuiteAction
                                .get("UNAPPLIED_PATTERN_SELECT_STMNT") + "')")
                        .replaceAll("'null'", Global.NULL);

        if (!dbAccess.executeUpdate(lvStatement)) {
            return handleDatabaseError(lvStatement, "initialise()");
        }

        return dbAccess.commit();
    }

    /**
     * Rest the database column <code>APPLIED_PATTERN_SELECT_STMNT</code>.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean resetAppliedPatternSelectStmnt() {

        assert dbAccess != null : PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL;

        final String lvStatement =
                UPDATE_TMD_TRIAL_RUN_ACTION
                        + "SET APPLIED_PATTERN_ORDER_BY = null, "
                        + "APPLIED_PATTERN_SELECT_STMNT = null "
                        + "WHERE DATABASE_INSTANCE_ID = " + databaseInstanceId
                        + AND_TEST_SUITE_ID + testSuiteId + AND_START_TIME
                        + startTime + AND_SEQUENCE_NUMBER_ACTION
                        + sequenceNumberAction;
        if (!dbAccess.executeUpdate(lvStatement)) {
            return handleDatabaseError(lvStatement,
                    "resetAppliedPatternSelectStmnt()");
        }

        return dbAccess.commit();
    }

    /**
     * Rest the database column <code>UNAPPLIED_PATTERN_SELECT_STMNT</code>.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean resetUnappliedPatternSelectStmnt() {

        assert dbAccess != null : PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL;

        final String lvStatement =
                UPDATE_TMD_TRIAL_RUN_ACTION
                        + "SET UNAPPLIED_PATTERN_ORDER_BY = null, "
                        + "UNAPPLIED_PATTERN_SELECT_STMNT = null "
                        + "WHERE DATABASE_INSTANCE_ID = " + databaseInstanceId
                        + AND_TEST_SUITE_ID + testSuiteId + AND_START_TIME
                        + startTime + AND_SEQUENCE_NUMBER_ACTION
                        + sequenceNumberAction;
        if (!dbAccess.executeUpdate(lvStatement)) {
            return handleDatabaseError(lvStatement,
                    "resetUnappliedPatternSelectStmnt()");
        }

        return dbAccess.commit();
    }

    /**
     * Updates in the database the columns
     * <code>APPLIED_DURATION_MICRO_SECOND</code>,
     * <code>APPLIED_END_TIME</code>, and <code>APPLIED_START_TIME</code>.
     * 
     * @param parStartTime The new start date and time.
     * @param parEndTime The new end date and time.
     * @param parDuration The new duration of the query execution.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean setAppliedEndAction(final Date parStartTime,
            final Date parEndTime, final long parDuration) {

        checkPreconditionEndActionError(parStartTime, parEndTime);

        assert dbAccess != null : PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL;

        final String lvStatement =
                UPDATE_TMD_TRIAL_RUN_ACTION
                        + "SET APPLIED_DURATION = "
                        + parDuration
                        + ", APPLIED_END_TIME = CAST(TO_TIMESTAMP('"
                        + new SimpleDateFormat(
                                Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_JAVA)
                                .format(parEndTime)
                        + SINGLEQUOTE_COMMA_SPACE_SINGLEQUOTE
                        + Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_SQL
                        + "') AS TIMESTAMP(9)), APPLIED_START_TIME = CAST(TO_TIMESTAMP('"
                        + new SimpleDateFormat(
                                Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_JAVA)
                                .format(parStartTime)
                        + SINGLEQUOTE_COMMA_SPACE_SINGLEQUOTE
                        + Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_SQL
                        + "') AS TIMESTAMP(9)), APPLIED_STATUS = '"
                        + Global.TRIAL_RUN_STATUS_END_ACTION
                        + WHERE_DATABASE_INSTANCE_ID + databaseInstanceId
                        + AND_TEST_SUITE_ID + testSuiteId + AND_START_TIME
                        + startTime + AND_SEQUENCE_NUMBER_ACTION
                        + sequenceNumberAction;

        if (!dbAccess.executeUpdate(lvStatement)) {
            return handleDatabaseError(lvStatement, "setAppliedEndAction()");
        }

        return dbAccess.commit();
    }

    /**
     * Updates in the database the columns <code>APPLIED_ERROR_MESSAGE</code>
     * and <code>APPLIED_STATUS</code>.
     * 
     * @param parErrorMessage The new applied error message.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean setAppliedEndActionError(final String parErrorMessage) {

        checkPreconditionEndActionError(parErrorMessage);

        assert dbAccess != null : PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL;

        final String lvStatement =
                UPDATE_TMD_TRIAL_RUN_ACTION + "SET APPLIED_ERROR_MESSAGE = '"
                        + parErrorMessage + "', APPLIED_STATUS = '"
                        + Global.TRIAL_RUN_STATUS_END_ACTION
                        + WHERE_DATABASE_INSTANCE_ID + databaseInstanceId
                        + AND_TEST_SUITE_ID + testSuiteId + AND_START_TIME
                        + startTime + AND_SEQUENCE_NUMBER_ACTION
                        + sequenceNumberAction;

        if (!dbAccess.executeUpdate(lvStatement)) {
            return handleDatabaseError(lvStatement,
                    "setAppliedEndActionError()");
        }

        return dbAccess.commit();
    }

    /**
     * Updates in the database the columns
     * <code>APPLIED_PATTERN_SELECT_STMNT</code> and
     * <code>APPLIED_STATUS</code>.
     * 
     * @param parStatement The new applied <code>SQL</code> statement.
     * @param parOrderBy Rhe new <code>ORDER BY</code> clause.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean setAppliedStartAction(final String parStatement,
            final String parOrderBy) {

        checkPreconditionStartAction(parStatement);

        assert dbAccess != null : PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL;

        final String lvStatement =
                UPDATE_TMD_TRIAL_RUN_ACTION
                        + "SET APPLIED_PATTERN_ORDER_BY = '" + parOrderBy
                        + "', APPLIED_PATTERN_SELECT_STMNT = '" + parStatement
                        + "', APPLIED_STATUS = '"
                        + Global.TRIAL_RUN_STATUS_START_ACTION
                        + WHERE_DATABASE_INSTANCE_ID + databaseInstanceId
                        + AND_TEST_SUITE_ID + testSuiteId + AND_START_TIME
                        + startTime + AND_SEQUENCE_NUMBER_ACTION
                        + sequenceNumberAction;

        if (!dbAccess.executeUpdate(lvStatement)) {
            return handleDatabaseError(lvStatement, "setAppliedStartAction()");
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
                UPDATE_TMD_TRIAL_RUN_ACTION + "SET COMPARISON_EQUALS = '"
                        + parEquals + "', COMPARISON_MESSAGE = " + lvMessage
                        + " WHERE DATABASE_INSTANCE_ID = " + databaseInstanceId
                        + AND_TEST_SUITE_ID + testSuiteId + AND_START_TIME
                        + startTime + AND_SEQUENCE_NUMBER_ACTION
                        + sequenceNumberAction;

        if (!dbAccess.executeUpdate(lvStatement)) {
            return handleDatabaseError(lvStatement, "setComparison()");
        }

        return dbAccess.commit();
    }

    /**
     * Sets the current action sequence number.
     * 
     * @param parSequenceNumberAction The current action sequence number.
     */
    public final void setSequenceNumberAction(final long parSequenceNumberAction) {

        sequenceNumberAction = parSequenceNumberAction;
    }

    /**
     * Updates in the database the column <code>TABLE_NAME</code>.
     * 
     * @param parTableName The new table name.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean setTableName(final String parTableName) {

        if (parTableName == null) {
            throw new IllegalArgumentException("Table name is missing (null)");
        }

        if ("".equals(parTableName)) {
            throw new IllegalArgumentException("Table name is missing (empty)");
        }

        assert dbAccess != null : PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL;

        final String lvStatement =
                UPDATE_TMD_TRIAL_RUN_ACTION + "SET TABLE_NAME = '"
                        + parTableName + WHERE_DATABASE_INSTANCE_ID
                        + databaseInstanceId + AND_TEST_SUITE_ID + testSuiteId
                        + AND_START_TIME + startTime
                        + AND_SEQUENCE_NUMBER_ACTION + sequenceNumberAction;

        if (!dbAccess.executeUpdate(lvStatement)) {
            return handleDatabaseError(lvStatement, "setTableName()");
        }

        return dbAccess.commit();
    }

    /**
     * Updates in the database the columns
     * <code>UNAPPLIED_DURATION_MICRO_SECOND</code>,
     * <code>UNAPPLIED_END_TIME</code>, and <code>UNAPPLIED_START_TIME</code>.
     * 
     * @param parStartTime The new start date and time.
     * @param parEndTime The new end date and time.
     * @param parDuration The new duration of the query execution.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean setUnappliedEndAction(final Date parStartTime,
            final Date parEndTime, final long parDuration) {

        checkPreconditionEndActionError(parStartTime, parEndTime);

        assert dbAccess != null : PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL;

        final String lvStatement =
                UPDATE_TMD_TRIAL_RUN_ACTION
                        + "SET UNAPPLIED_DURATION = "
                        + parDuration
                        + ", UNAPPLIED_END_TIME = CAST(TO_TIMESTAMP('"
                        + new SimpleDateFormat(
                                Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_JAVA)
                                .format(parEndTime)
                        + SINGLEQUOTE_COMMA_SPACE_SINGLEQUOTE
                        + Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_SQL
                        + "') AS TIMESTAMP(9)), UNAPPLIED_START_TIME = CAST(TO_TIMESTAMP('"
                        + new SimpleDateFormat(
                                Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_JAVA)
                                .format(parStartTime)
                        + SINGLEQUOTE_COMMA_SPACE_SINGLEQUOTE
                        + Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_SQL
                        + "') AS TIMESTAMP(9)), UNAPPLIED_STATUS = '"
                        + Global.TRIAL_RUN_STATUS_END_ACTION
                        + WHERE_DATABASE_INSTANCE_ID + databaseInstanceId
                        + AND_TEST_SUITE_ID + testSuiteId + AND_START_TIME
                        + startTime + AND_SEQUENCE_NUMBER_ACTION
                        + sequenceNumberAction;

        if (!dbAccess.executeUpdate(lvStatement)) {
            return handleDatabaseError(lvStatement, "setUnappliedEndAction()");
        }

        return dbAccess.commit();
    }

    /**
     * Updates in the database the columns <code>UNAPPLIED_ERROR_MESSAGE</code>
     * and <code>UNAPPLIED_STATUS</code>.
     * 
     * @param parErrorMessage The new unapplied error message.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean setUnappliedEndActionError(final String parErrorMessage) {

        checkPreconditionEndActionError(parErrorMessage);

        assert dbAccess != null : PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL;

        final String lvStatement =
                UPDATE_TMD_TRIAL_RUN_ACTION + "SET UNAPPLIED_ERROR_MESSAGE = '"
                        + parErrorMessage + "', UNAPPLIED_STATUS = '"
                        + Global.TRIAL_RUN_STATUS_END_ACTION
                        + WHERE_DATABASE_INSTANCE_ID + databaseInstanceId
                        + AND_TEST_SUITE_ID + testSuiteId + AND_START_TIME
                        + startTime + AND_SEQUENCE_NUMBER_ACTION
                        + sequenceNumberAction;

        if (!dbAccess.executeUpdate(lvStatement)) {
            return handleDatabaseError(lvStatement,
                    "setUnappliedEndActionError()");
        }

        return dbAccess.commit();
    }

    /**
     * Updates in the database the columns
     * <code>UNAPPLIED_PATTERN_SELECT_STMNT</code> and
     * <code>UNAPPLIED_STATUS</code>.
     * 
     * @param parStatement The new unapplied <code>SQL</code> statement.
     * @param parOrderBy Rhe new <code>ORDER BY</code> clause.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean setUnappliedStartAction(final String parStatement,
            final String parOrderBy) {

        checkPreconditionStartAction(parStatement);

        assert dbAccess != null : PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL;

        final String lvStatement =
                UPDATE_TMD_TRIAL_RUN_ACTION
                        + "SET UNAPPLIED_PATTERN_ORDER_BY = '" + parOrderBy
                        + "', UNAPPLIED_PATTERN_SELECT_STMNT = '"
                        + parStatement + "', UNAPPLIED_STATUS = '"
                        + Global.TRIAL_RUN_STATUS_START_ACTION
                        + WHERE_DATABASE_INSTANCE_ID + databaseInstanceId
                        + AND_TEST_SUITE_ID + testSuiteId + AND_START_TIME
                        + startTime + AND_SEQUENCE_NUMBER_ACTION
                        + sequenceNumberAction;

        if (!dbAccess.executeUpdate(lvStatement)) {
            return handleDatabaseError(lvStatement, "setUnappliedStartAction()");
        }

        return dbAccess.commit();
    }
}
