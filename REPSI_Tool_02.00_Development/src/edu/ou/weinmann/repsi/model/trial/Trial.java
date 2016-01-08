package edu.ou.weinmann.repsi.model.trial;

import edu.ou.weinmann.repsi.model.mapper.DatabaseInstanceMapper;
import edu.ou.weinmann.repsi.model.mapper.TrialRunActionMapper;
import edu.ou.weinmann.repsi.model.mapper.TrialRunMapper;
import edu.ou.weinmann.repsi.model.mapper.TrialRunProtocolMapper;

import edu.ou.weinmann.repsi.model.trial.metadata.Columns;

import edu.ou.weinmann.repsi.model.trial.util.DataGenerator;
import edu.ou.weinmann.repsi.model.trial.util.ResultSetComparator;

import edu.ou.weinmann.repsi.model.util.Configurator;
import edu.ou.weinmann.repsi.model.util.DatabaseAccessor;
import edu.ou.weinmann.repsi.model.util.DatabaseToExcel;
import edu.ou.weinmann.repsi.model.util.Global;

import java.math.BigDecimal;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.sql.TIMESTAMP;

/**
 * Manages the trial run functionality of the REPSI tool:
 * 
 * <ul>
 * <li>executes a trail run</li>
 * <li>extracts the results of trial runs from the database into an Excel file.</li>
 * </ul>
 * 
 * @author Walter Weinmann
 * 
 * Manages the trial runs.
 * 
 * @author Walter Weinmann
 * 
 */
public class Trial {

    private static final String AS_TIMESTAMP_9 = "') AS TIMESTAMP(9))";

    private static final Logger LOGGER =
            Logger.getLogger(Trial.class.getPackage().getName());

    private static final String SINGLEQUOTE_COMMA_SPACE_SINGLEQUOTE = "', '";

    private Map<String, Object> columnsDatabaseInstance;

    private Map<String, Object> columnsTestSuite;

    private int currDatabaseInstanceId;

    private long currNumberOfErrors;

    private long currSequenceNumberAction;

    private String currStartTime;

    private int currTestSuiteId;

    private DatabaseToExcel databaseToExcel;

    private final DatabaseAccessor dbAccessMaster;

    private DatabaseAccessor dbAccessTest;

    private DatabaseAccessor dbAccessTestApplied;

    private DatabaseAccessor dbAccessTestUnapplied;

    private boolean isAborted;

    private String sqlSyntaxCodeTarget;

    private TrialRunMapper trialRun;

    private TrialRunActionMapper trialRunAction;

    private TrialRunProtocolMapper trialRunProtocol;

    /**
     * Constructs a <code>Trial</code> object. The name of the used properties
     * file is taken from mthe class
     * <code>edu.ou.weinmann.repsi.model.util.Global</code> and does not
     * constitute an XML document.
     */
    public Trial() {

        this(Global.PROPERTIES_FILE_NAME, false);
    }

    /**
     * Constructs a <code>Trial</code> object.
     * 
     * @param parProprtiesFilename complete filename of the properties file
     *            including the directory.
     * @param parPropertiesXml <code>true</code> if the properties file
     *            constitutes an XML document.
     */
    public Trial(final String parProprtiesFilename,
            final boolean parPropertiesXml) {

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.entering(this.getClass().getName(), "Trial", new Object[] {
                    parProprtiesFilename, Boolean.valueOf(parPropertiesXml), });
        }

        Configurator.removeInstance();

        dbAccessMaster =
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

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.exiting(this.getClass().getName(), "Trial");
        }
    }

    private boolean closeConnectionMaster() {

        if (!dbAccessMaster.closeConnection()) {

            LOGGER.log(Level.SEVERE, "Error with closeConnectionMaster()");
            return false;
        }

        return true;
    }

    private boolean commitMaster() {

        if (!dbAccessMaster.commit()) {
            closeConnectionMaster();

            LOGGER.log(Level.SEVERE, "Error with commitMaster()");
            return false;
        }

        return true;
    }

    private boolean createStatementMaster() {

        if (!dbAccessMaster.createStatement()) {
            closeConnectionMaster();

            LOGGER.log(Level.SEVERE, "Error with createStatementMaster()");
            return false;
        }

        return true;
    }

    private Object getColumn(final String parStmnt) {

        if (!dbAccessMaster.executeQuery(parStmnt)) {
            return null;
        }

        if (!dbAccessMaster.next()) {
            return null;
        }

        return dbAccessMaster.getColumn(1);
    }

    private Map<String, Object> getColumns(final String parStmnt) {

        if (!dbAccessMaster.executeQuery(parStmnt)) {
            return null;
        }

        if (!dbAccessMaster.next()) {
            return null;
        }

        return dbAccessMaster.getColumns();
    }

    private boolean getConnectionMaster() {

        if (!dbAccessMaster.getConnection()) {
            LOGGER.log(Level.SEVERE, "Error with getConnectionMaster()");
            return false;
        }

        return true;
    }

    private boolean getSQLSyntaxCodeTarget() {

        sqlSyntaxCodeTarget =
                (String) getColumn("SELECT SQL_SYNTAX_CODE "
                        + "FROM TMD_DATABASE_INSTANCE DEI, "
                        + "TMD_DATABASE_SYSTEM DES "
                        + "WHERE DEI.DATABASE_INSTANCE_ID = "
                        + currDatabaseInstanceId
                        + " AND DEI.DATABASE_SYSTEM_CODE = "
                        + "DES.DATABASE_SYSTEM_CODE");
        if (sqlSyntaxCodeTarget == null) {
            final String lvMsg =
                    "Database instance=" + currDatabaseInstanceId
                            + " is not available";
            trialRunProtocol.createErrorProtocol(lvMsg, false);
            LOGGER.log(Level.SEVERE, lvMsg);
            return false;
        }

        return true;
    }

    private boolean getTestSuite() {

        columnsTestSuite =
                getColumns("SELECT DESCRIPTION AS TEST_SUITE_DESCRIPTION, "
                        + "NAME AS TEST_SUITE_NAME " + "FROM TMD_TEST_SUITE "
                        + "WHERE TEST_SUITE_ID = " + currTestSuiteId + ";");
        if (columnsTestSuite == null) {
            final String lvMsg =
                    "Test suite=" + currTestSuiteId + " is not available";
            trialRunProtocol.createErrorProtocol(lvMsg, false);
            LOGGER.log(Level.SEVERE, lvMsg);
            return false;
        }

        return true;
    }

    private ArrayList<HashMap<String, Object>> getTestSuiteActions() {

        if (!dbAccessMaster
                .executeQuery("SELECT TTSA.SEQUENCE_NUMBER AS SEQUENCE_NUMBER, "
                        + "NULL AS APPLIED_PATTERN_ORDER_BY, "
                        + "NULL AS APPLIED_PATTERN_SELECT_STMNT, "
                        + "NULL AS TEST_QUERY_PAIR_DESCRIPTION, "
                        + "TTSA.DESCRIPTION AS TEST_SUITE_ACTION_DESCRIPTION, "
                        + "TTT.DESCRIPTION AS TEST_TABLE_DESCRIPTION, "
                        + "TTSA.EXECUTION_FREQUENCY AS EXECUTION_FREQUENCY, "
                        + "TTSA.OPERATION_CODE AS OPERATION_CODE, "
                        + "TTSO.OPERATION_TYPE AS OPERATION_TYPE, "
                        + "NULL AS PATTERN_SQL_IDIOM_NAME, "
                        + "TTT.SQL_SYNTAX_CODE AS SQL_SYNTAX_CODE, "
                        + "TTSA.TABLE_NAME AS TABLE_NAME, "
                        + "TTSO.NAME AS TEST_SUITE_OPERATION_NAME, "
                        + "NULL AS UNAPPLIED_PATTERN_ORDER_BY, "
                        + "NULL AS UNAPPLIED_PATTERN_SELECT_STMNT "
                        + "FROM TMD_TEST_SUITE_ACTION TTSA, "
                        + "TMD_TEST_SUITE_OPERATION TTSO, "
                        + "TMD_TEST_TABLE TTT WHERE TEST_SUITE_ID = "
                        + currTestSuiteId
                        + " AND TTSA.TABLE_NAME IS NOT NULL "
                        + "AND TTSA.TABLE_NAME = TTT.NAME "
                        + "AND TTSA.OPERATION_CODE = TTSO.OPERATION_CODE "
                        + "UNION SELECT TTSA.SEQUENCE_NUMBER AS SEQUENCE_NUMBER, "
                        + "TTQP.APPLIED_PATTERN_ORDER_BY AS APPLIED_PATTERN_ORDER_BY, "
                        + "TTQP.APPLIED_PATTERN_SELECT_STMNT AS APPLIED_PATTERN_SELECT_STMNT, "
                        + "TTQP.DESCRIPTION AS TEST_QUERY_PAIR_DESCRIPTION, "
                        + "TTSA.DESCRIPTION AS TEST_SUITE_ACTION_DESCRIPTION, "
                        + "NULL AS TEST_TABLE_DESCRIPTION, "
                        + "TTSA.EXECUTION_FREQUENCY AS EXECUTION_FREQUENCY, "
                        + "TTSA.OPERATION_CODE AS OPERATION_CODE, "
                        + "TTSO.OPERATION_TYPE AS OPERATION_TYPE, "
                        + "PNSI.NAME AS PATTERN_SQL_IDIOM_NAME, "
                        + "TTQP.SQL_SYNTAX_CODE AS SQL_SYNTAX_CODE, "
                        + "NULL AS TABLE_NAME, "
                        + "TTSO.NAME AS TEST_SUITE_OPERATION_NAME, "
                        + "TTQP.UNAPPLIED_PATTERN_ORDER_BY AS UNAPPLIED_PATTERN_ORDER_BY, "
                        + "TTQP.UNAPPLIED_PATTERN_SELECT_STMNT AS UNAPPLIED_PATTERN_SELECT_STMNT "
                        + "FROM TMD_PATTERN_SQL_IDIOM PNSI, "
                        + "TMD_TEST_QUERY_PAIR TTQP, "
                        + "TMD_TEST_SUITE_ACTION TTSA, "
                        + "TMD_TEST_SUITE_OPERATION TTSO "
                        + "WHERE TEST_SUITE_ID = "
                        + currTestSuiteId
                        + " AND TTSA.TEST_QUERY_PAIR_ID IS NOT NULL "
                        + "AND TTSA.TEST_QUERY_PAIR_ID = "
                        + "TTQP.TEST_QUERY_PAIR_ID "
                        + "AND TTSA.OPERATION_CODE = TTSO.OPERATION_CODE "
                        + "AND TTQP.PATTERN_SQL_IDIOM_ID = PNSI.PATTERN_SQL_IDIOM_ID "
                        + "ORDER BY 1;")) {
            final String lvMsg =
                    "Actions of test suite=" + currTestSuiteId
                            + " are not available";
            trialRunProtocol.createErrorProtocol(lvMsg, false);
            LOGGER.log(Level.SEVERE, lvMsg);
            return null;
        }

        final ArrayList<HashMap<String, Object>> lvColumnsTestSuiteActions =
                new ArrayList<HashMap<String, Object>>();

        int lvPos = 0;

        while (dbAccessMaster.next()) {
            lvColumnsTestSuiteActions.add(lvPos++,
                    (HashMap<String, Object>) dbAccessMaster.getColumns());
        }

        return lvColumnsTestSuiteActions;
    }

    private boolean processInstance(final String parOperationCode,
            final Map<String, Object> parColumnsTestSuiteAction) {

        final String lvTableName =
                (String) parColumnsTestSuiteAction
                        .get(Global.COLUMN_NAME_TABLE_NAME);

        if (!(trialRunProtocol.createProtocol("Database table", lvTableName) && trialRunAction
                .setTableName(lvTableName))) {
            return false;
        }

        final long lvExecutionFrequency =
                ((BigDecimal) parColumnsTestSuiteAction
                        .get("EXECUTION_FREQUENCY")).longValue();

        LOGGER.log(Level.FINER, "Database table " + lvTableName
                + ": Starting generating " + lvExecutionFrequency + " rows");

        if (parOperationCode.equals(Global.OPERATION_CODE_INSERT_ROW)) {
            if (!processInstanceCreateRow(lvTableName, lvExecutionFrequency)) {
                return false;
            }
        } else {
            trialRunProtocol.createErrorProtocol("Operation code="
                    + parOperationCode + Global.ERROR_NOT_YET_IMPLEMENTED,
                    false);
            return false;
        }

        return true;
    }

    private boolean processInstanceCreateRow(final String parTableName,
            final long parExecutionFrequency) {

        final String lvCatalog = "";

        String lvSchemaPattern = "";

        if (sqlSyntaxCodeTarget.equals(Global.SQL_SYNTAX_CODE_ORACLE_10G)) {
            lvSchemaPattern =
                    (String) columnsDatabaseInstance
                            .get(Global.COLUMN_NAME_USER_NAME);
        }

        final Columns lvColumns =
                new Columns(trialRunProtocol, dbAccessTest, lvCatalog,
                        lvSchemaPattern, parTableName);

        lvColumns.protocol(trialRunProtocol);

        final DataGenerator lvDataGenerator =
                new DataGenerator(trialRunProtocol, dbAccessTest, parTableName,
                        lvColumns);

        final boolean lvOk = lvDataGenerator.generateRow(parExecutionFrequency);

        if (trialRunProtocol.isAborted()) {
            return false;
        }

        if (!trialRunProtocol.createProtocol("Number of rows required",
                parExecutionFrequency)) {
            return false;
        }

        if (!trialRunProtocol.createProtocol("Number of rows generated",
                lvDataGenerator.getNumberRowsGenerated())) {
            return false;
        }

        return lvOk;
    }

    private boolean processQuery(final String parOperationCode,
            final Map<String, Object> parColumnsTestSuiteAction,
            final long parPrecision) {

        if (parOperationCode.equals(Global.OPERATION_CODE_EXECUTE_QUERY)) {
            final ResultSet lvResultSetUnapplied =
                    processQueryUnapplied(parColumnsTestSuiteAction,
                            parPrecision);
            if (lvResultSetUnapplied == null) {
                return false;
            }

            final ResultSet lvResultSetApplied =
                    processQueryApplied(parColumnsTestSuiteAction, parPrecision);
            if (lvResultSetApplied == null) {
                return false;
            }

            return processQueryComparison(parColumnsTestSuiteAction);
        }

        if (parOperationCode
                .equals(Global.OPERATION_CODE_EXECUTE_QUERY_APPLIED)) {

            if (!trialRunAction.resetUnappliedPatternSelectStmnt()) {
                return false;
            }

            if (processQueryApplied(parColumnsTestSuiteAction, parPrecision) != null) {
                return true;
            }
        } else if (parOperationCode
                .equals(Global.OPERATION_CODE_EXECUTE_QUERY_UNAPPLIED)) {

            if (!trialRunAction.resetAppliedPatternSelectStmnt()) {
                return false;
            }

            if (processQueryUnapplied(parColumnsTestSuiteAction, parPrecision) != null) {
                return true;
            }
        } else {
            trialRunProtocol.createErrorProtocol("Operation code="
                    + parOperationCode + Global.ERROR_NOT_YET_IMPLEMENTED,
                    false);
        }

        return false;
    }

    private ResultSet processQueryApplied(
            final Map<String, Object> parColumnsTestSuiteAction,
            final long parPrecision) {

        String lvOrderBy =
                (String) parColumnsTestSuiteAction
                        .get(Global.COLUMN_NAME_APPLIED_PATTERN_ORDER_BY);

        if (lvOrderBy == null) {
            lvOrderBy = Global.NULL;
        }

        final String lvStatement =
                (String) parColumnsTestSuiteAction
                        .get(Global.COLUMN_NAME_APPLIED_PATTERN_SELECT_STMNT);

        final String lvSQLSyntaxCode =
                (String) parColumnsTestSuiteAction
                        .get(Global.COLUMN_NAME_SQL_SYNTAX_CODE);

        if (!(trialRunAction.setAppliedStartAction(lvStatement, lvOrderBy)
                && trialRunProtocol.createProtocol("Applied statement",
                        lvStatement) && trialRunProtocol.createProtocol(
                "SQL syntax code", lvSQLSyntaxCode))) {
            return null;
        }

        if (!dbAccessTestApplied.executeQueryTrialRun(lvStatement,
                lvSQLSyntaxCode)) {
            final String lvErrorMessage =
                    dbAccessTestApplied.getTrialRunErrorMessage();

            if (!(trialRunAction.setAppliedEndActionError(lvErrorMessage) && trialRunProtocol
                    .createProtocol("Error", lvErrorMessage))) {
                return null;
            }
        }

        int lvNumberRows = 0;

        while (dbAccessTestApplied.next()) {
            lvNumberRows++;
        }

        final Date lvEndTime = dbAccessTestApplied.getTrialRunEndTime();
        final long lvMicroSeconds =
                dbAccessTestApplied.getTrialTimeQuantities(parPrecision);
        final Date lvStartTime = dbAccessTestApplied.getTrialRunStartTime();

        if (!(trialRunAction.setAppliedEndAction(lvStartTime, lvEndTime,
                lvMicroSeconds) && processQueryProtocolFinal(lvStartTime,
                lvEndTime, lvMicroSeconds, lvNumberRows))) {
            return null;
        }

        return dbAccessTestApplied.getResultSetObject();
    }

    private boolean processQueryComparison(
            final Map<String, Object> parColumnsTestSuiteAction) {

        final String lvAppliedPatternOrderBy =
                (String) parColumnsTestSuiteAction
                        .get(Global.COLUMN_NAME_APPLIED_PATTERN_ORDER_BY);
        final String lvUnappliedPatternOrderBy =
                (String) parColumnsTestSuiteAction
                        .get(Global.COLUMN_NAME_UNAPPLIED_PATTERN_ORDER_BY);

        if ("".equals(lvAppliedPatternOrderBy)
                || "".equals(lvUnappliedPatternOrderBy)) {
            return true;
        }

        final ResultSetComparator lvComparator = new ResultSetComparator();

        lvComparator.setOrderBy(lvUnappliedPatternOrderBy, 0);
        lvComparator.setOrderBy(lvAppliedPatternOrderBy, 1);

        lvComparator.setSelectStmnt((String) parColumnsTestSuiteAction
                .get(Global.COLUMN_NAME_UNAPPLIED_PATTERN_SELECT_STMNT), 0);
        lvComparator.setSelectStmnt((String) parColumnsTestSuiteAction
                .get(Global.COLUMN_NAME_APPLIED_PATTERN_SELECT_STMNT), 1);

        lvComparator.setSqlSyntaxCode((String) parColumnsTestSuiteAction
                .get(Global.COLUMN_NAME_SQL_SYNTAX_CODE));

        if (!lvComparator.compare(new DatabaseAccessor[] {
                dbAccessTestUnapplied, dbAccessTestApplied, })) {
            final String lvMessage = lvComparator.getLastErrorMsg();

            if (!(trialRunProtocol.createProtocol(lvMessage) && trialRunAction
                    .setComparison("N", lvMessage))) {
                return false;
            }

            return true;
        }

        if (!(trialRunProtocol
                .createProtocol("Both queries produced the same ResultSet") && trialRunAction
                .setComparison("Y", ""))) {
            return false;
        }

        return true;
    }

    private boolean processQueryProtocolFinal(final Date parStartTime,
            final Date parEndTime, final long parMicroSeconds,
            final int parNumberRows) {

        if (!(trialRunProtocol.createProtocol("Start timestamp",
                new SimpleDateFormat(
                        Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_JAVA)
                        .format(parStartTime))
                && trialRunProtocol.createProtocol("End   timestamp, rows="
                        + new DecimalFormat(Global.NUMBER_FORMAT_JAVA)
                                .format(parNumberRows), new SimpleDateFormat(
                        Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_JAVA)
                        .format(parEndTime)) && trialRunProtocol
                .createProtocol("Duration in microseconds", new DecimalFormat(
                        Global.NUMBER_FORMAT_LONG_JAVA).format(parMicroSeconds)))) {
            return false;
        }

        return true;
    }

    private ResultSet processQueryUnapplied(
            final Map<String, Object> parColumnsTestSuiteAction,
            final long parPrecision) {

        String lvOrderBy =
                (String) parColumnsTestSuiteAction
                        .get(Global.COLUMN_NAME_UNAPPLIED_PATTERN_ORDER_BY);

        if (lvOrderBy == null) {
            lvOrderBy = Global.NULL;
        }

        final String lvStatement =
                (String) parColumnsTestSuiteAction
                        .get(Global.COLUMN_NAME_UNAPPLIED_PATTERN_SELECT_STMNT);

        final String lvSQLSyntaxCode =
                (String) parColumnsTestSuiteAction
                        .get(Global.COLUMN_NAME_SQL_SYNTAX_CODE);

        if (!(trialRunAction.setUnappliedStartAction(lvStatement, lvOrderBy)
                && trialRunProtocol.createProtocol("Unapplied statement",
                        lvStatement) && trialRunProtocol.createProtocol(
                "SQL syntax code", lvSQLSyntaxCode))) {
            return null;
        }

        if (!dbAccessTestUnapplied.executeQueryTrialRun(lvStatement,
                lvSQLSyntaxCode)) {
            final String lvErrorMessage =
                    dbAccessTestUnapplied.getTrialRunErrorMessage();

            if (!(trialRunAction.setUnappliedEndActionError(lvErrorMessage) && trialRunProtocol
                    .createProtocol("Error", lvErrorMessage))) {
                return null;
            }
        }

        int lvNumberRows = 0;

        while (dbAccessTestApplied.next()) {
            lvNumberRows++;
        }

        final Date lvEndTime = dbAccessTestUnapplied.getTrialRunEndTime();
        final long lvMicroSeconds =
                dbAccessTestUnapplied.getTrialTimeQuantities(parPrecision);
        final Date lvStartTime = dbAccessTestUnapplied.getTrialRunStartTime();

        if (!(trialRunAction.setUnappliedEndAction(lvStartTime, lvEndTime,
                lvMicroSeconds) && processQueryProtocolFinal(lvStartTime,
                lvEndTime, lvMicroSeconds, lvNumberRows))) {
            return null;
        }

        return dbAccessTestUnapplied.getResultSetObject();
    }

    private boolean processSchema(final String parOperationCode,
            final Map<String, Object> parColumnsTestSuiteAction) {

        final String lvTableName =
                (String) parColumnsTestSuiteAction
                        .get(Global.COLUMN_NAME_TABLE_NAME);

        if (!(trialRunProtocol.createProtocol("Database table", lvTableName) && trialRunAction
                .setTableName(lvTableName))) {
            return false;
        }

        if (parOperationCode.equals(Global.OPERATION_CODE_CREATE_TABLE)) {
            if (!processSchemaCreate(lvTableName)) {
                return false;
            }
        } else if (parOperationCode
                .equals(Global.OPERATION_CODE_DROP_AND_CREATE_TABLE)) {
            processSchemaDrop(lvTableName);

            if (!processSchemaCreate(lvTableName)) {
                return false;
            }
        } else if (parOperationCode.equals(Global.OPERATION_CODE_DROP_TABLE)) {
            if (!processSchemaDrop(lvTableName)) {
                return false;
            }
        } else {
            trialRunProtocol.createErrorProtocol("Operation code="
                    + parOperationCode + Global.ERROR_NOT_YET_IMPLEMENTED,
                    false);
            return false;
        }

        return true;
    }

    private boolean processSchemaCreate(final String parTableName) {

        if (!dbAccessMaster.executeQuery("SELECT DDL_STATEMENT "
                + "FROM TMD_TEST_TABLE_DDL WHERE NAME = '" + parTableName
                + "' ORDER BY SEQUENCE_NUMBER;")) {
            trialRunProtocol.createErrorProtocol(
                    "DDL statements of test table=" + parTableName
                            + " are not available", false);
            return false;
        }

        int lvPos = 0;

        while (dbAccessMaster.next()) {

            if (!processSchemaDDL((String) dbAccessMaster.getColumn(1))) {
                return false;
            }

            lvPos++;
        }

        if (lvPos == 0) {
            trialRunProtocol
                    .createErrorProtocol(
                            "DDL statements missing in table TMD_TEST_TABLE_DDL",
                            false);
            return false;
        }

        return true;
    }

    private boolean processSchemaDDL(final String parStmnt) {

        if (!trialRunProtocol.createProtocol("DDL Statement", parStmnt)) {
            return false;
        }

        if (!dbAccessTest.executeUpdate(parStmnt)) {
            trialRunProtocol.createErrorProtocol("Error with executeUpdate("
                    + parStmnt + ")", false);
            return false;
        }

        return true;
    }

    private boolean processSchemaDrop(final String parTableName) {

        final String lvStatement = "DROP TABLE " + parTableName + " CASCADE;";

        if (!trialRunProtocol.createProtocol("DDL Statement", lvStatement)) {
            return false;
        }

        if (!processSchemaDDL(lvStatement)) {
            return false;
        }

        return true;
    }

    /**
     * Runs a trial and records the measured results.
     * 
     * @param parDatabaseInstanceId The identification of the used test database
     *            instance.
     * @param parTestSuiteId The identification of the used test test suite.
     * @param parFetchSize The fetch size.
     * @param parFrequency The number of cycles to be executed.
     * @param parDescription The description of the calibration run.
     * @param parPrecision The exponent (base 10) of the required precision for
     *            the response time. Calibrates the execution of a simple Java
     *            method.
     * 
     * @return <code>true</code> if the processing ended without any error,
     *         and <code>false</code> otherwise.
     */
    public final boolean runTrial(final int parDatabaseInstanceId,
            final int parTestSuiteId, final String parDescription,
            final int parFetchSize, final int parFrequency,
            final long parPrecision) {

        final DecimalFormat lvDecimalFormatCounter =
                new DecimalFormat("##,##0");

        long lvCurrCycle = 0;

        while (lvCurrCycle < parFrequency) {

            lvCurrCycle++;

            LOGGER.log(Level.FINER, "Trial run cycle # "
                    + lvDecimalFormatCounter.format(lvCurrCycle));

            if (!runTrialSingle(parDatabaseInstanceId, parTestSuiteId,
                    parDescription, parFetchSize, parPrecision)) {
                return false;
            }
        }

        return true;
    }

    private boolean runTrialAction(
            final Map<String, Object> parColumnsTestSuiteAction,
            final long parPrecision) {

        currSequenceNumberAction =
                ((BigDecimal) parColumnsTestSuiteAction.get("SEQUENCE_NUMBER"))
                        .longValue();

        trialRunAction.setSequenceNumberAction(currSequenceNumberAction);
        trialRunProtocol.setSequenceNumberAction(currSequenceNumberAction);

        if (!trialRunProtocol.createProtocol("Run Trial - Start Action "
                + currSequenceNumberAction)) {
            return false;
        }

        if (!runTrialActionInitialise(parColumnsTestSuiteAction)) {
            return false;
        }

        final String lvOperationType =
                (String) parColumnsTestSuiteAction.get("OPERATION_TYPE");

        if (!trialRunProtocol.createProtocol("Operation type", lvOperationType)) {
            return false;
        }

        final String lvOperationCode =
                (String) parColumnsTestSuiteAction.get("OPERATION_CODE");

        if (!trialRunProtocol.createProtocol("Operation code", lvOperationCode
                + " ["
                + (String) parColumnsTestSuiteAction
                        .get("TEST_SUITE_OPERATION_NAME") + "]")) {
            return false;
        }

        if (lvOperationType.equals(Global.OPERATION_TYPE_INSTANCE)) {
            if (!processInstance(lvOperationCode, parColumnsTestSuiteAction)) {
                return false;
            }
        } else if (lvOperationType.equals(Global.OPERATION_TYPE_QUERY)) {
            if (!processQuery(lvOperationCode, parColumnsTestSuiteAction,
                    parPrecision)) {
                return false;
            }
        } else if (lvOperationType.equals(Global.OPERATION_TYPE_SCHEMA)) {
            if (!processSchema(lvOperationCode, parColumnsTestSuiteAction)) {
                return false;
            }
        } else {
            trialRunProtocol
                    .createErrorProtocol("Operation type=" + lvOperationType
                            + Global.ERROR_NOT_YET_IMPLEMENTED, false);
            return false;
        }

        if (!trialRunProtocol.createProtocol("Run Trial - End   Action "
                + currSequenceNumberAction)) {
            return false;
        }

        return true;
    }

    private boolean runTrialActionInitialise(
            final Map<String, Object> parColumnsTestSuiteAction) {

        // Trial Run Action Persistence ****************************************
        return trialRunAction.initialise(parColumnsTestSuiteAction);
    }

    private boolean runTrialActions(final int parFetchSize,
            final long parPrecision) {

        ArrayList<HashMap<String, Object>> lvColumnsTestSuiteActions;

        lvColumnsTestSuiteActions = getTestSuiteActions();

        if (lvColumnsTestSuiteActions == null) {
            trialRunProtocol.createErrorProtocol(
                    "Problem determining the test suite actions", false);
            return false;
        }

        if (lvColumnsTestSuiteActions.size() == 0) {
            trialRunProtocol.createErrorProtocol(
                    "No test suite actions avilable", false);
            return false;
        }

        if (!runTrialActionsConnectToTestDatabase(parFetchSize)) {
            return false;
        }

        for (int i = 0; i < lvColumnsTestSuiteActions.size(); i++) {
            if (!runTrialAction(lvColumnsTestSuiteActions.get(i), parPrecision)) {

                if (!isAborted) {
                    isAborted = trialRunProtocol.isAborted();
                }

                if (isAborted) {
                    return false;
                }
            }
        }

        if (!runTrialActionsDisconnectFromTestDatabase()) {
            return false;
        }

        return true;
    }

    private boolean runTrialActionsConnectToTestDatabase(final int parFetchSize) {

        dbAccessTest =
                new DatabaseAccessor(Global.DATABASE_SCHEMA_IDENTIFIER_TEST,
                        sqlSyntaxCodeTarget, (String) columnsDatabaseInstance
                                .get("JDBC_DRIVER"), false);

        dbAccessTestApplied =
                new DatabaseAccessor(Global.DATABASE_SCHEMA_IDENTIFIER_TEST,
                        sqlSyntaxCodeTarget, (String) columnsDatabaseInstance
                                .get("JDBC_DRIVER"), false);

        dbAccessTestUnapplied =
                new DatabaseAccessor(Global.DATABASE_SCHEMA_IDENTIFIER_TEST,
                        sqlSyntaxCodeTarget, (String) columnsDatabaseInstance
                                .get("JDBC_DRIVER"), false);

        // Create a database connection.
        if (!dbAccessTest.getConnection((String) columnsDatabaseInstance
                .get("JDBC_URL"), (String) columnsDatabaseInstance
                .get(Global.COLUMN_NAME_USER_NAME),
                (String) columnsDatabaseInstance.get("PASSWORD"))) {
            trialRunProtocol.createErrorProtocol(
                    "Problem with getConnection() / trial database", false);
            return false;
        }

        if (!dbAccessTestApplied.getConnection((String) columnsDatabaseInstance
                .get("JDBC_URL"), (String) columnsDatabaseInstance
                .get(Global.COLUMN_NAME_USER_NAME),
                (String) columnsDatabaseInstance.get("PASSWORD"))) {
            trialRunProtocol.createErrorProtocol(
                    "Problem with getConnection() / trial database applied",
                    false);
            return false;
        }

        if (!dbAccessTestUnapplied.getConnection(
                (String) columnsDatabaseInstance.get("JDBC_URL"),
                (String) columnsDatabaseInstance
                        .get(Global.COLUMN_NAME_USER_NAME),
                (String) columnsDatabaseInstance.get("PASSWORD"))) {
            trialRunProtocol.createErrorProtocol(
                    "Problem with getConnection() / trial database unapplied",
                    false);
            return false;
        }

        // Create a statement.
        if (!dbAccessTest.createStatement()) {
            dbAccessTest.closeConnection();

            trialRunProtocol.createErrorProtocol(
                    "Problem with createStatement() / trial database", false);
            return false;
        }

        if (!dbAccessTest.setFetchSize(parFetchSize)) {
            dbAccessTest.closeConnection();

            trialRunProtocol.createErrorProtocol("Problem with setFetchSize("
                    + parFetchSize + ") / trial database", false);
            return false;
        }

        if (!dbAccessTestApplied.createStatement()) {
            dbAccessTestApplied.closeConnection();

            trialRunProtocol.createErrorProtocol(
                    "Problem with createStatement() / trial database applied",
                    false);
            return false;
        }

        if (!dbAccessTestUnapplied.createStatement()) {
            dbAccessTestUnapplied.closeConnection();

            trialRunProtocol
                    .createErrorProtocol(
                            "Problem with createStatement() / trial database unapplied",
                            false);
            return false;
        }

        return true;
    }

    private boolean runTrialActionsDisconnectFromTestDatabase() {

        // Commit the transaction.
        if (!dbAccessTest.commit()) {
            dbAccessTest.closeConnection();

            trialRunProtocol.createErrorProtocol(
                    "Problem with commit() / trial database", false);
            return false;
        }

        if (!dbAccessTestApplied.commit()) {
            dbAccessTestApplied.closeConnection();

            trialRunProtocol.createErrorProtocol(
                    "Problem with commit() / trial database applied", false);
            return false;
        }

        if (!dbAccessTestUnapplied.commit()) {
            dbAccessTestUnapplied.closeConnection();

            trialRunProtocol.createErrorProtocol(
                    "Problem with commit() / trial database unapplied", false);
            return false;
        }

        // Close the database connection.
        if (!dbAccessTest.closeConnection()) {
            trialRunProtocol.createErrorProtocol(
                    "Problem with closeConnection() / trial database", false);
            return false;
        }

        if (!dbAccessTestApplied.closeConnection()) {
            trialRunProtocol.createErrorProtocol(
                    "Problem with closeConnection() / trial database applied",
                    false);
            return false;
        }

        if (!dbAccessTestUnapplied.closeConnection()) {
            trialRunProtocol
                    .createErrorProtocol(
                            "Problem with closeConnection() / trial database unapplied",
                            false);
            return false;
        }

        return true;
    }

    private boolean runTrialFinalise() {

        trialRunProtocol.setSequenceNumberAction(0L);

        if (!trialRunProtocol.createProtocol("Run Trial - Start Finalisation")) {
            return false;
        }

        if (!trialRun.setStatus(Global.TRIAL_RUN_STATUS_START_FINALISATION)) {
            return false;
        }

        if (!trialRunProtocol.createProtocol("Run Trial - End   Finalisation")) {
            return false;
        }

        if (!trialRun.setStatus(Global.TRIAL_RUN_STATUS_END_FINALISATION)) {
            return false;
        }

        if (!trialRunProtocol.createProtocol("Run Trial - End   Program")) {
            return false;
        }

        if (!trialRun.setStatus(Global.TRIAL_RUN_STATUS_END_PROGRAM)) {
            return false;
        }

        return true;
    }

    private boolean runTrialInitialise(final int parDatabaseInstanceId,
            final int parTestSuiteId, final String parDescription) {

        // Determine the data related to the database instance *****************
        if (!getSQLSyntaxCodeTarget()) {
            return false;
        }

        columnsDatabaseInstance =
                new DatabaseInstanceMapper(sqlSyntaxCodeTarget)
                        .getDatabaseInstance(parDatabaseInstanceId);
        if (columnsDatabaseInstance == null) {
            return false;
        }

        trialRunProtocol =
                new TrialRunProtocolMapper(sqlSyntaxCodeTarget,
                        parDatabaseInstanceId, parTestSuiteId, currStartTime);

        trialRun =
                new TrialRunMapper(trialRunProtocol, sqlSyntaxCodeTarget,
                        parDatabaseInstanceId, parTestSuiteId, currStartTime);

        trialRunAction =
                new TrialRunActionMapper(trialRunProtocol, sqlSyntaxCodeTarget,
                        parDatabaseInstanceId, parTestSuiteId, currStartTime);

        // Determine the data related to the test suite ************************
        if (!getTestSuite()) {
            return false;
        }

        // Trial Run Persistence ***********************************************
        if (!trialRun.initialise(columnsDatabaseInstance, columnsTestSuite)) {
            return false;
        }

        if (!trialRun.setDescription(parDescription)) {
            return false;
        }

        // Trial Run TrialRunProtocolMapper
        // *********************************************************************
        if (!runTrialInitialiseProtocol()) {
            return false;
        }

        if (!dbAccessMaster.commit()) {
            final String lvMsg = "Commit() failed / master database";
            trialRunProtocol.createErrorProtocol(lvMsg, false);
            LOGGER.log(Level.SEVERE, lvMsg);
            return false;
        }

        return true;
    }

    private void runTrialInitialiseGlobals(final int parDatabaseInstanceId,
            final int parTestSuiteId) {

        columnsDatabaseInstance = null;
        columnsTestSuite = null;

        currDatabaseInstanceId = parDatabaseInstanceId;
        currNumberOfErrors = 0L;
        currSequenceNumberAction = 0L;
        currStartTime =
                "CAST(TO_TIMESTAMP('"
                        + new SimpleDateFormat(
                                Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_JAVA)
                                .format(new Date())
                        + SINGLEQUOTE_COMMA_SPACE_SINGLEQUOTE
                        + Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_SQL
                        + AS_TIMESTAMP_9;
        currTestSuiteId = parTestSuiteId;

        isAborted = false;
    }

    private boolean runTrialInitialiseProtocol() {

        if (!trialRunProtocol.createProtocol("Run Trial - Start Program")) {
            return false;
        }

        if (!trialRun.setStatus(Global.TRIAL_RUN_STATUS_START_PROGRAM)) {
            return false;
        }

        if (!trialRunProtocol
                .createProtocol("Run Trial - Start Initialisation")) {
            return false;
        }

        if (!trialRun.setStatus(Global.TRIAL_RUN_STATUS_START_INITIALISATION)) {
            return false;
        }

        if (!trialRunProtocol
                .createProtocol("Run Trial - Parameter Database Instance: "
                        + (String) columnsDatabaseInstance
                                .get("DATABASE_SYSTEM_CODE")
                        + " & "
                        + (String) columnsDatabaseInstance
                                .get("OPERATING_SYSTEM_CODE")
                        + " & "
                        + (String) columnsDatabaseInstance
                                .get("PROCESSOR_CODE") + " ["
                        + currDatabaseInstanceId + "]")) {
            return false;
        }

        if (!trialRunProtocol.createProtocol("Target SQL Syntax",
                sqlSyntaxCodeTarget)) {
            return false;
        }

        if (!trialRunProtocol
                .createProtocol("Run Trial - Parameter Test Suite:        "
                        + (String) columnsTestSuite.get("TEST_SUITE_NAME")
                        + " [" + currTestSuiteId + "]")) {
            return false;
        }

        if (!trialRunProtocol
                .createProtocol("Run Trial - End   Initialisation")) {
            return false;
        }

        if (!trialRun.setStatus(Global.TRIAL_RUN_STATUS_END_INITIALISATION)) {
            return false;
        }

        return true;
    }

    private boolean runTrialSingle(final int parDatabaseInstanceId,
            final int parTestSuiteId, final String parDescription,
            final int parFetchSize, final long parPrecision) {

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.entering(this.getClass().getName(), "runTrial",
                    new Object[] { Integer.valueOf(currDatabaseInstanceId),
                            Integer.valueOf(currTestSuiteId), });
        }

        // Initialise global variables *****************************************
        runTrialInitialiseGlobals(parDatabaseInstanceId, parTestSuiteId);

        // Create a database connection and a statement ************************
        if (!(getConnectionMaster() && createStatementMaster())) {
            return false;
        }

        // Processing **********************************************************
        boolean lvIsOk = false;

        if (runTrialInitialise(parDatabaseInstanceId, parTestSuiteId,
                parDescription)
                && runTrialActions(parFetchSize, parPrecision)
                && runTrialFinalise()) {

            if (currNumberOfErrors == 0) {
                lvIsOk = true;
            } else {
                if (currNumberOfErrors == 1) {
                    LOGGER
                            .log(Level.SEVERE,
                                    "One error occurred during processing, please check the run trial protocol");
                } else {
                    LOGGER
                            .log(
                                    Level.SEVERE,
                                    currNumberOfErrors
                                            + " errors occurred during processing, please check the run trial protocol");
                }
            }
        }

        // Commit the transaction **********************************************
        if (!commitMaster()) {
            return false;
        }

        // Close the database connection ***************************************
        if (trialRun != null) {
            trialRun.closeConnection();
        }

        if (trialRunAction != null) {
            trialRunAction.closeConnection();
        }

        if (trialRunProtocol != null) {
            trialRunProtocol.closeConnection();
        }

        if (!closeConnectionMaster()) {
            return false;
        }

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.exiting(this.getClass().getName(), "runTrial", Boolean
                    .valueOf(lvIsOk));
        }

        return lvIsOk;
    }

    /**
     * Exports all the data of a trial run from the master database into an
     * Excel file.
     * 
     * @param parFileName The complete file name of the Excel file including the
     *            directory.
     * @param parAllData Whether all data are required instead of the latest
     *            trial run only.
     * 
     * @return <code>true</code> if the calibration data were exported without
     *         any error, and <code>false</code> otherwise.
     */
    public final boolean trialDataToExcel(final String parFileName,
            final boolean parAllData) {

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.entering(this.getClass().getName(), "trialDataToExcel",
                    new Object[] { parFileName, Boolean.valueOf(parAllData), });
        }

        if (parFileName == null || "".equals(parFileName)) {
            throw new IllegalArgumentException("Filename is missing");
        }

        // Create a database connection and a statement ************************
        if (!(getConnectionMaster() && createStatementMaster())) {
            return false;
        }

        // Processing **********************************************************
        boolean lvIsOk = false;

        if (trialDataToExcelInitialise(parFileName)
                && trialDataToExcelTrialRun(parAllData)
                && trialDataToExcelFinalise()) {
            lvIsOk = true;
        }

        // Commit the transaction and close the database connection ************
        if (!(commitMaster() && closeConnectionMaster())) {
            return false;
        }

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.exiting(this.getClass().getName(), "trialDataToExcel",
                    Boolean.valueOf(lvIsOk));
        }

        return lvIsOk;
    }

    private boolean trialDataToExcelFinalise() {

        if (!databaseToExcel.closeWorkbook()) {
            LOGGER.log(Level.SEVERE, databaseToExcel.getLastErrorMsg());
            return false;
        }

        return true;
    }

    private boolean trialDataToExcelInitialise(final String parFileName) {

        databaseToExcel = new DatabaseToExcel();

        if (!databaseToExcel.createWorkbook(parFileName)) {
            LOGGER.log(Level.SEVERE, databaseToExcel.getLastErrorMsg());
            return false;
        }

        return true;
    }

    private boolean trialDataToExcelTrialRun(final boolean parAllData) {

        if (parAllData) {
            return trialDataToExcelTrialRunAllData();
        }

        // Table TMD_TRIAL_RUN *************************************************
        if (!dbAccessMaster
                .executeQuery("SELECT DATABASE_INSTANCE_ID, TEST_SUITE_ID, "
                        + "START_TIME FROM TMD_TRIAL_RUN "
                        + "ORDER BY START_TIME DESC;")) {
            return false;
        }

        if (!(dbAccessMaster.next())) {
            return true;
        }

        final String lvDatabaseInstanceId =
                ((BigDecimal) dbAccessMaster.getColumn(1)).toString();
        final String lvTestSuiteId =
                ((BigDecimal) dbAccessMaster.getColumn(2)).toString();
        final TIMESTAMP lvStartTime = (TIMESTAMP) dbAccessMaster.getColumn(3);

        try {
            if (!dbAccessMaster.executeQuery("SELECT * FROM TMD_TRIAL_RUN "
                    + "WHERE DATABASE_INSTANCE_ID = " + lvDatabaseInstanceId
                    + " AND TEST_SUITE_ID = " + lvTestSuiteId
                    + " AND START_TIME = CAST(TO_TIMESTAMP('"
                    + lvStartTime.timestampValue()
                    + SINGLEQUOTE_COMMA_SPACE_SINGLEQUOTE
                    + Global.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS_SSS_SQL
                    + AS_TIMESTAMP_9 + ";")) {
                return false;
            }
        } catch (SQLException e) {
            LOGGER
                    .log(
                            Level.SEVERE,
                            "SQLException with oracle.sql.TIMESTAMP.timestampValue() / TMD_TRIAL_RUN",
                            e);
            return false;
        }

        int lvSheet = 0;

        if (!databaseToExcel.createSheet("TMD_TRIAL_RUN", lvSheet++,
                dbAccessMaster.getResultSetObject(), true)) {
            LOGGER.log(Level.SEVERE, databaseToExcel.getLastErrorMsg());
            return false;
        }

        // Table TMD_TRIAL_RUN_ACTION ******************************************
        try {
            if (!dbAccessMaster
                    .executeQuery("SELECT * FROM TMD_TRIAL_RUN_ACTION "
                            + "WHERE DATABASE_INSTANCE_ID = "
                            + lvDatabaseInstanceId + " AND TEST_SUITE_ID = "
                            + lvTestSuiteId
                            + " AND START_TIME = CAST(TO_TIMESTAMP('"
                            + lvStartTime.timestampValue()
                            + SINGLEQUOTE_COMMA_SPACE_SINGLEQUOTE
                            + Global.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS_SSS_SQL
                            + AS_TIMESTAMP_9
                            + " ORDER BY SEQUENCE_NUMBER_ACTION;")) {
                return false;
            }
        } catch (SQLException e) {
            LOGGER
                    .log(
                            Level.SEVERE,
                            "SQLException with oracle.sql.TIMESTAMP.timestampValue() / TMD_TRIAL_RUN_ACTION",
                            e);
            return false;
        }

        if (!databaseToExcel.createSheet("TMD_TRIAL_RUN_ACTION", lvSheet++,
                dbAccessMaster.getResultSetObject(), true)) {
            LOGGER.log(Level.SEVERE, databaseToExcel.getLastErrorMsg());
            return false;
        }

        // Table TMD_TRIAL_PROTOCOL ********************************************
        try {
            if (!dbAccessMaster
                    .executeQuery("SELECT * FROM TMD_TRIAL_RUN_PROTOCOL "
                            + "WHERE DATABASE_INSTANCE_ID = "
                            + lvDatabaseInstanceId + " AND TEST_SUITE_ID = "
                            + lvTestSuiteId
                            + " AND START_TIME = CAST(TO_TIMESTAMP('"
                            + lvStartTime.timestampValue()
                            + SINGLEQUOTE_COMMA_SPACE_SINGLEQUOTE
                            + Global.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS_SSS_SQL
                            + AS_TIMESTAMP_9
                            + "ORDER BY SEQUENCE_NUMBER_PROTOCOL;")) {
                return false;
            }
        } catch (SQLException e) {
            LOGGER
                    .log(
                            Level.SEVERE,
                            "SQLException with oracle.sql.TIMESTAMP.timestampValue() / TMD_TRIAL_RUN_PROTOCOL",
                            e);
            return false;
        }

        if (!databaseToExcel.createSheet("TMD_TRIAL_RUN_PROTOCOL", lvSheet,
                dbAccessMaster.getResultSetObject(), true)) {
            LOGGER.log(Level.SEVERE, databaseToExcel.getLastErrorMsg());
            return false;
        }

        return true;
    }

    private boolean trialDataToExcelTrialRunAllData() {

        // Table TMD_TRIAL_RUN *************************************************
        if (!dbAccessMaster
                .executeQuery("SELECT * FROM TMD_TRIAL_RUN ORDER BY "
                        + "DATABASE_INSTANCE_ID, TEST_SUITE_ID, START_TIME;")) {
            return false;
        }

        int lvSheet = 0;

        if (!databaseToExcel.createSheet("TMD_TRIAL_RUN", lvSheet++,
                dbAccessMaster.getResultSetObject(), true)) {
            LOGGER.log(Level.SEVERE, databaseToExcel.getLastErrorMsg());
            return false;
        }

        // Table TMD_TRIAL_RUN_ACTION ******************************************
        if (!dbAccessMaster
                .executeQuery("SELECT * FROM TMD_TRIAL_RUN_ACTION ORDER BY "
                        + "DATABASE_INSTANCE_ID, TEST_SUITE_ID, START_TIME, "
                        + "SEQUENCE_NUMBER_ACTION;")) {
            return false;
        }

        if (!databaseToExcel.createSheet("TMD_TRIAL_RUN_ACTION", lvSheet++,
                dbAccessMaster.getResultSetObject(), true)) {
            LOGGER.log(Level.SEVERE, databaseToExcel.getLastErrorMsg());
            return false;
        }

        // Table TMD_TRIAL_PROTOCOL ********************************************
        if (!dbAccessMaster
                .executeQuery("SELECT * FROM TMD_TRIAL_RUN_PROTOCOL ORDER BY "
                        + "DATABASE_INSTANCE_ID, TEST_SUITE_ID, START_TIME, "
                        + "SEQUENCE_NUMBER_PROTOCOL;")) {
            return false;
        }

        if (!databaseToExcel.createSheet("TMD_TRIAL_RUN_PROTOCOL", lvSheet,
                dbAccessMaster.getResultSetObject(), true)) {
            LOGGER.log(Level.SEVERE, databaseToExcel.getLastErrorMsg());
            return false;
        }

        return true;
    }
}
