package edu.ou.weinmann.repsi.model.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.jdbc.OracleResultSet;

/**
 * Accesses the database and manages the details related to
 * <code>Connection</code>, <code>Statement</code> and
 * <code>ResultSet</code>.
 * 
 * @author Walter Weinmann
 * 
 */
public class DatabaseAccessor implements Global {

    private static final String ERROR_PRECONDITION_CONNECTION_IS_MISSING_NULL =
            "Precondition: Connection is missing (null)";

    private static final String ERROR_PRECONDITION_RESULT_SET_IS_NULL =
            "Precondition: ResultSet is null";

    private static final String ERROR_PRECONDITION_STATEMENT_OBJECT_IS_NULL =
            "Precondition: StatementObject is null";

    private static final Logger LOGGER =
            Logger.getLogger(DatabaseAccessor.class.getPackage().getName());

    private static final String SQL_STATEMENT_IS_MISSING =
            "SQL statement is missing";

    private static final String SQL_SYNTAX_CODE_IS_MISSING =
            "SQL syntax code is missing";

    private ResultSet resultSetObject;

    private Connection connectionObject;

    private String databaseIdent;

    private int numberRows;

    private final String sqlSyntaxCodeTarget;

    private final SQLRewriter sqlRewriter = new SQLRewriter();

    private Statement statementObject;

    private Date trialRunEndTime;

    private String trialRunErrorMessage;

    private long trialNanoSeconds;

    private Date trialRunStartTime;

    private String userName;

    /**
     * Constructs a <code>DatabaseAccessor</code> object.
     * 
     * @param parDatabaseIdent The identification of the database to determine
     *            configuration parameters.
     * @param parSQLSyntaxCodeTarget The SQL syntax version of the database
     *            system.
     * @param parIsMapper Whether the object is created by an object of the
     *            package <code>edu.ou.weinmann.repsi.model.mapper</code>.
     */
    public DatabaseAccessor(final String parDatabaseIdent,
            final String parSQLSyntaxCodeTarget, final boolean parIsMapper) {

        this(parDatabaseIdent, parSQLSyntaxCodeTarget, "", parIsMapper);
    }

    /**
     * Constructs a <code>DatabaseAccessor</code> object.
     * 
     * @param parDatabaseIdent The identification of the database to determine
     *            configuration parameters.
     * @param parSQLSyntaxCodeTarget The SQL syntax version of the database
     *            system.
     * @param parDriver The database driver to be used instead of the one
     *            defined in the cofiguration parameters.
     * @param parIsMapper Whether the object is created by an object of the
     *            package <code>edu.ou.weinmann.repsi.model.mapper</code>.
     */
    public DatabaseAccessor(final String parDatabaseIdent,
            final String parSQLSyntaxCodeTarget, final String parDriver,
            final boolean parIsMapper) {

        if (parDatabaseIdent == null) {
            throw new IllegalArgumentException(
                    "Database identification is missing (null)");
        }

        if ("".equals(parDatabaseIdent)) {
            throw new IllegalArgumentException(
                    "Database identification is missing (empty)");
        }

        if (parSQLSyntaxCodeTarget == null) {
            throw new IllegalArgumentException(
                    "Target SQL syntax is missing (null)");
        }

        if (parDriver == null) {
            throw new IllegalArgumentException("Driver is missing (null)");
        }

        if ("".equals(parSQLSyntaxCodeTarget)) {
            sqlSyntaxCodeTarget = Global.SQL_SYNTAX_CODE_SQL_99;
        } else {
            sqlSyntaxCodeTarget = parSQLSyntaxCodeTarget;
        }

        String lvDriver;

        if ("".equals(parDriver)) {
            lvDriver =
                    Configurator.getInstance().getProperty(
                            Global.PROPERTY_PATH_1_DATABASE + "."
                                    + parDatabaseIdent + "."
                                    + Global.PROPERTY_PATH_3_DRIVER);
        } else {
            lvDriver = parDriver;
        }

        try {
            Class.forName(lvDriver);

            databaseIdent = parDatabaseIdent;
            resultSetObject = null;
            statementObject = null;
        } catch (java.lang.ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Class.forName(" + lvDriver + ")", e);
            return;
        }

        if (parIsMapper) {
            if (!getConnection()) {
                return;
            }

            createStatement();
        }
    }

    /**
     * Returns the metadata of a given database column.
     * 
     * @param parTableName The name of the database table.
     * @param parColumnName The name of the database column.
     * 
     * @return the current <code>ResultSet</code> object if the given database
     *         column exist, <code>null</code> otherwise.
     */
    public final ResultSet checkColumnName(final String parTableName,
            final String parColumnName) {

        assert connectionObject != null : ERROR_PRECONDITION_CONNECTION_IS_MISSING_NULL;

        try {
            final ResultSet lvRS =
                    connectionObject.getMetaData().getColumns(null,
                            userName.toUpperCase(Locale.getDefault()),
                            parTableName, parColumnName);

            if (lvRS.next()) {
                return lvRS;
            }

            return null;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "checkColumnName(\"" + parTableName
                    + "\")", e);
            return null;
        }
    }

    /**
     * Returns the metadata of a given database table.
     * 
     * @param parTableName The name of the database table.
     * 
     * @return the current <code>ResultSet</code> object if the given database
     *         table exist, <code>null</code> otherwise.
     */
    public final ResultSet checkTableName(final String parTableName) {

        assert connectionObject != null : ERROR_PRECONDITION_CONNECTION_IS_MISSING_NULL;

        try {
            final ResultSet lvRS =
                    connectionObject.getMetaData().getTables(null,
                            userName.toUpperCase(Locale.getDefault()),
                            parTableName, null);

            if (lvRS.next()) {
                return lvRS;
            }

            return null;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "checkTableName(\"" + parTableName + "\")", e);
            return null;
        }
    }

    /**
     * Closes the currently open connection.
     * 
     * @return <code>false</code> if the database
     *         <code>connectionObject</code> could not be closed, or
     *         <code>true</code> otherwise
     */
    public final boolean closeConnection() {

        assert connectionObject != null : ERROR_PRECONDITION_CONNECTION_IS_MISSING_NULL;

        if (resultSetObject != null) {
            try {
                resultSetObject.close();

            } catch (SQLException sqle) {
                LOGGER.log(Level.SEVERE, "resultSetObject.close()", sqle);
            }
        }

        if (statementObject != null) {
            try {
                statementObject.close();
            } catch (SQLException sqle) {
                LOGGER.log(Level.SEVERE, "statementObject.close()", sqle);
            }
        }

        try {
            if (connectionObject.isClosed()) {
                return true;
            }

            try {
                connectionObject.close();

                return true;
            } catch (SQLException sqle1) {
                LOGGER.log(Level.SEVERE, "connectionObject.close()", sqle1);
            }
        } catch (SQLException sqle2) {
            LOGGER.log(Level.SEVERE, "connectionObject.isClosed()", sqle2);
        }

        return false;

    }

    /**
     * Closes the given result set.
     * 
     * @param parResultSet The <code>ResultSet</code> object to be closed.
     * 
     * @return <code>false</code> if the result set could not be closed, or
     *         <code>true</code> otherwise.
     */
    public final boolean closeResultSet(final ResultSet parResultSet) {

        if (parResultSet == null) {
            throw new IllegalArgumentException("ResultSet is missing");
        }

        try {
            parResultSet.close();

            return true;
        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, "resultSetObject.close()", sqle);
        }

        return false;
    }

    /**
     * Makes the changes permanent which were created by all statements
     * associated with this <code>Connection</code> object since the last
     * <code>commit</code> or <code>rollback</code> was issued.
     * 
     * @return <code>false</code> if the changes could not be committed, or
     *         <code>true</code> otherwise.
     */
    public final boolean commit() {

        assert connectionObject != null : ERROR_PRECONDITION_CONNECTION_IS_MISSING_NULL;

        try {
            connectionObject.commit();

            return true;
        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, "connectionObject.commit()", sqle);
        }

        return false;
    }

    /**
     * Creates a <code>Statement</code> object associtated with this
     * <code>Connnection</code> object.
     * 
     * @return <code>true</code> if the <code>Statement</code> could be
     *         created, or <code>false</code> otherwise.
     */
    public final boolean createStatement() {

        assert connectionObject != null : ERROR_PRECONDITION_CONNECTION_IS_MISSING_NULL;

        try {
            statementObject = connectionObject.createStatement();

            return true;
        } catch (SQLException sqle) {
            LOGGER
                    .log(Level.SEVERE, "connectionObject.createStatement()",
                            sqle);
        }

        return false;
    }

    /**
     * Executes the <code>Statement</code> object by passing the specified SQL
     * statement to the database.
     * 
     * It is used for executing queries formulated using SQL:1999.
     * 
     * @param parStmnt The SQL statement.
     * 
     * @return <code>true</code> if the <code>Statement</code> could be
     *         executed without any problems, or <code>false</code> otherwise.
     */
    public final boolean executeQuery(final String parStmnt) {

        if (parStmnt == null || "".equals(parStmnt)) {
            throw new IllegalArgumentException(SQL_STATEMENT_IS_MISSING);
        }

        return executeQuery(parStmnt, Global.SQL_SYNTAX_CODE_SQL_99);
    }

    /**
     * Executes the <code>Statement</code> object by passing the specified SQL
     * statement to the database.
     * 
     * It is used for executing queries formulated using a given SQL syntax
     * version.
     * 
     * @param parStmnt The SQL statement.
     * @param parSQLSyntaxCodeSource Rhe SQL syntax version of the SQL
     *            statement.
     * 
     * @return <code>true</code> if the <code>Statement</code> could be
     *         executed without any problems, or <code>false</code> otherwise.
     */
    public final boolean executeQuery(final String parStmnt,
            final String parSQLSyntaxCodeSource) {

        if (parStmnt == null || "".equals(parStmnt)) {
            throw new IllegalArgumentException(SQL_STATEMENT_IS_MISSING);
        }

        if (parSQLSyntaxCodeSource == null || "".equals(parSQLSyntaxCodeSource)) {
            throw new IllegalArgumentException(SQL_SYNTAX_CODE_IS_MISSING);
        }

        final String lvStmntTranslated =
                sqlRewriter.rewrite(parSQLSyntaxCodeSource,
                        sqlSyntaxCodeTarget, parStmnt);

        try {
            if (statementObject == null) {
                LOGGER.log(Level.SEVERE, "statementObject.executeQuery("
                        + lvStmntTranslated + "): statementObject is null");

                return false;
            }

            if (!sqlRewriter.getLastErrorMsg().equals("")) {
                LOGGER.log(Level.SEVERE, sqlRewriter.getLastErrorMsg());

                return false;
            }

            resultSetObject = statementObject.executeQuery(lvStmntTranslated);

            return true;
        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, "statementObject.executeQuery("
                    + lvStmntTranslated + ")", sqle);
        }

        return false;
    }

    /**
     * Executes the <code>Statement</code> object by passing the specified SQL
     * statement to the database.
     * 
     * It is used for executing queries formulated using a given SQL syntax
     * version during a trial run.
     * 
     * @param parStmnt The SQL statement.
     * @param parSQLSyntaxCodeSource Rhe SQL syntax version of the SQL
     *            statement.
     * 
     * @return <code>true</code> if the <code>Statement</code> could be
     *         executed without any problems, or <code>false</code> otherwise.
     */
    public final boolean executeQueryTrialRun(final String parStmnt,
            final String parSQLSyntaxCodeSource) {

        if (parStmnt == null || "".equals(parStmnt)) {
            throw new IllegalArgumentException(SQL_STATEMENT_IS_MISSING);
        }

        if (parSQLSyntaxCodeSource == null || "".equals(parSQLSyntaxCodeSource)) {
            throw new IllegalArgumentException(SQL_SYNTAX_CODE_IS_MISSING);
        }

        trialRunEndTime = new Date();
        trialRunErrorMessage = "";
        trialNanoSeconds = 0L;
        trialRunStartTime = new Date();

        final String lvStmntTranslated =
                sqlRewriter.rewrite(parSQLSyntaxCodeSource,
                        sqlSyntaxCodeTarget, parStmnt);

        try {
            if (statementObject == null) {
                trialRunErrorMessage =
                        "statementObject.executeQueryTrialRun("
                                + lvStmntTranslated
                                + "): statementObject is null";
                LOGGER.log(Level.SEVERE, trialRunErrorMessage);

                return false;
            }

            if (!sqlRewriter.getLastErrorMsg().equals("")) {
                trialRunErrorMessage = sqlRewriter.getLastErrorMsg();
                LOGGER.log(Level.SEVERE, trialRunErrorMessage);

                return false;
            }

            trialRunStartTime = new Date();
            final long lvStart = System.nanoTime();

            resultSetObject = statementObject.executeQuery(lvStmntTranslated);

            numberRows = 0;

            while (resultSetObject.next()) {
                numberRows++;
            }

            trialNanoSeconds = System.nanoTime() - lvStart;
            trialRunEndTime = new Date();

            return true;
        } catch (SQLException sqle) {
            trialRunErrorMessage =
                    "statementObject.executeQueryTrialRun(" + lvStmntTranslated
                            + "), exception=" + sqle + " (" + sqle.getMessage()
                            + ")";
            LOGGER.log(Level.SEVERE, "statementObject.executeQueryTrialRun("
                    + lvStmntTranslated + ")", sqle);
        }

        return false;
    }

    /**
     * Executes the <code>Statement</code> object by passing the specified SQL
     * statement to the database.
     * 
     * It is used for executing updates with SQL statements formulated using
     * SQL:1999.
     * 
     * @param parStmnt The SQL statement.
     * 
     * @return <code>true</code> if the <code>Statement</code> could be
     *         executed without any problems, or <code>false</code> otherwise.
     */
    public final boolean executeUpdate(final String parStmnt) {

        if (parStmnt == null || "".equals(parStmnt)) {
            throw new IllegalArgumentException(SQL_STATEMENT_IS_MISSING);
        }

        return executeUpdate(parStmnt, Global.SQL_SYNTAX_CODE_SQL_99);
    }

    /**
     * Executes the <code>Statement</code> object by passing the specified SQL
     * statement to the database.
     * 
     * It is used for executing updates with SQL statements formulated using
     * SQL:1999. This method ignores duplicates.
     * 
     * @param parStmnt The SQL statement.
     * @param parIgnoreDuplicate Whether duplicates should be ignored.
     * 
     * @return <code>true</code> if the <code>Statement</code> could be
     *         executed without any problems, or <code>false</code> otherwise.
     */
    public final boolean executeUpdate(final String parStmnt,
            final boolean parIgnoreDuplicate) {

        if (parStmnt == null || "".equals(parStmnt)) {
            throw new IllegalArgumentException(SQL_STATEMENT_IS_MISSING);
        }

        return executeUpdate(parStmnt, Global.SQL_SYNTAX_CODE_SQL_99,
                parIgnoreDuplicate);
    }

    /**
     * Executes the <code>Statement</code> object by passing the specified SQL
     * statement to the database.
     * 
     * It is used for executing updates with rewriting of the query.
     * 
     * @param parStmnt The SQL statement.
     * @param parSQLSyntaxCodeSource The SQL syntax version of the SQL
     *            statement.
     * 
     * @return <code>true</code> if the <code>Statement</code> could be
     *         executed without any problems, or <code>false</code> otherwise.
     */
    public final boolean executeUpdate(final String parStmnt,
            final String parSQLSyntaxCodeSource) {

        if (parStmnt == null || "".equals(parStmnt)) {
            throw new IllegalArgumentException(SQL_STATEMENT_IS_MISSING);
        }

        if (parSQLSyntaxCodeSource == null || "".equals(parSQLSyntaxCodeSource)) {
            throw new IllegalArgumentException(SQL_SYNTAX_CODE_IS_MISSING);
        }

        return executeUpdateDirect(sqlRewriter.rewrite(parSQLSyntaxCodeSource,
                sqlSyntaxCodeTarget, parStmnt), false);
    }

    /**
     * Executes the <code>Statement</code> object by passing the specified SQL
     * statement to the database.
     * 
     * It is used for executing updates with rewriting of the query. This method
     * ignores duplicates.
     * 
     * @param parStmnt The SQL statement.
     * @param parSQLSyntaxCodeSource The SQL syntax version of the SQL
     *            statement.
     * @param parIgnoreDuplicate Whether duplicates should be ignored.
     * 
     * @return <code>true</code> if the <code>Statement</code> could be
     *         executed without any problems, or <code>false</code> otherwise.
     */
    public final boolean executeUpdate(final String parStmnt,
            final String parSQLSyntaxCodeSource,
            final boolean parIgnoreDuplicate) {

        if (parStmnt == null || "".equals(parStmnt)) {
            throw new IllegalArgumentException(SQL_STATEMENT_IS_MISSING);
        }

        if (parSQLSyntaxCodeSource == null || "".equals(parSQLSyntaxCodeSource)) {
            throw new IllegalArgumentException(SQL_SYNTAX_CODE_IS_MISSING);
        }

        return executeUpdateDirect(sqlRewriter.rewrite(parSQLSyntaxCodeSource,
                sqlSyntaxCodeTarget, parStmnt), parIgnoreDuplicate);
    }

    /**
     * Executes the <code>Statement</code> object by passing the specified SQL
     * statement to the database.
     * 
     * It is used for executing updates with SQL statements without any
     * rewriting of the SQL statement. This method ignores duplicates.
     * 
     * @param parStmnt The SQL statement.
     * @param parIgnoreDuplicate Whether duplicates should be ignored.
     * 
     * @return <code>true</code> if the <code>Statement</code> could be
     *         executed without any problems, or <code>false</code> otherwise.
     */
    public final boolean executeUpdateDirect(final String parStmnt,
            final boolean parIgnoreDuplicate) {

        if (parStmnt == null || "".equals(parStmnt)) {
            throw new IllegalArgumentException(SQL_STATEMENT_IS_MISSING);
        }

        if (statementObject == null) {
            LOGGER.log(Level.SEVERE, "statementObject.executeUpdate("
                    + parStmnt + "): statementObject is null");

            return false;
        }

        try {
            if (!sqlRewriter.getLastErrorMsg().equals("")) {
                LOGGER.log(Level.SEVERE, sqlRewriter.getLastErrorMsg());

                return false;
            }

            statementObject.executeUpdate(parStmnt);

            return true;
        } catch (SQLException e) {
            if (parIgnoreDuplicate) {
                LOGGER.log(Level.WARNING, "statementObject.executeUpdate("
                        + parStmnt + "), SQLException=" + e);
            } else {
                LOGGER.log(Level.SEVERE, "statementObject.executeUpdate("
                        + parStmnt + ")", e);
            }
        }

        return false;
    }

    /**
     * Retrieves the value of the designated column in the current row of this
     * <code>ResultSet</code> object as an <code>Array</code> object of
     * <code>BigDecimal</code>.
     * 
     * @param parPos The index of the desired column (starting with 1).
     * 
     * @return an <code>Array</code> object of <code>BigDecimal</code>.
     */
    public final BigDecimal[] getArrayBigDecimal(final int parPos) {

        assert resultSetObject != null : ERROR_PRECONDITION_RESULT_SET_IS_NULL;

        try {
            return (BigDecimal[]) ((OracleResultSet) resultSetObject).getARRAY(
                    parPos).getArray();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "resultSetObject.getArrayBigDecimal("
                    + parPos + ")", e);
            return new BigDecimal[] {};
        }
    }

    /**
     * Retrieves the value of the designated column in the current row of this
     * <code>ResultSet</code> object as an <code>Array</code> object of
     * <code>BigDecimal</code>.
     * 
     * @param parResultSet The <code>ResultSet</code> object.
     * @param parPos The index of the desired column (starting with 1).
     * 
     * @return an <code>Array</code> object of <code>BigDecimal</code>.
     */
    public static final BigDecimal[] getArrayBigDecimal(
            final ResultSet parResultSet, final int parPos) {

        assert parResultSet != null : ERROR_PRECONDITION_RESULT_SET_IS_NULL;

        try {
            return (BigDecimal[]) ((OracleResultSet) parResultSet).getARRAY(
                    parPos).getArray();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "resultSetObject.getArrayBigDecimal("
                    + parPos + ")", e);
            return new BigDecimal[] {};
        }
    }

    /**
     * Retrieves the value of the designated column in the current row of this
     * <code>ResultSet</code> object as an <code>Object</code>.
     * 
     * @param parPos The index of the desired column (starting with 1).
     * 
     * @return an <code>Object</code> if the access was successasful, and
     *         <code>null</code> otherwise.
     */
    public final Object getColumn(final int parPos) {

        if (parPos == 0) {
            throw new IllegalArgumentException("Column number is zero");
        }

        assert resultSetObject != null : ERROR_PRECONDITION_RESULT_SET_IS_NULL;

        try {
            return resultSetObject.getObject(parPos);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "resultSetObject.getObject(" + parPos
                    + ")", e);
            return null;
        }
    }

    /**
     * Retrieves the value of the designated column in the current row of this
     * <code>ResultSet</code> object as an <code>Object</code>.
     * 
     * @param parColumnName The name of the required database column.
     * 
     * @return an <code>Object</code> if the access was successasful, and
     *         <code>null</code> otherwise.
     */
    public final Object getColumn(final String parColumnName) {

        if (parColumnName == null || "".equals(parColumnName)) {
            throw new IllegalArgumentException("Column name is missing");
        }

        assert resultSetObject != null : ERROR_PRECONDITION_RESULT_SET_IS_NULL;

        try {
            return resultSetObject.getObject(parColumnName);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "resultSetObject.getObject("
                    + parColumnName + ")", e);
            return null;
        }
    }

    /**
     * Retrieves a <code>Map</code> object containing the values of all
     * columns of the database row.
     * 
     * @return a <code>Map</code> if the access was successasful, and
     *         <code>null</code> otherwise.
     */
    public final Map<String, Object> getColumns() {

        assert resultSetObject != null : ERROR_PRECONDITION_RESULT_SET_IS_NULL;

        final ResultSetMetaData lvMetaResultSet =
                getResultSetMetaData(resultSetObject);

        final int lvNumCols = getResultSetMetaDataSize(lvMetaResultSet);

        final Map<String, Object> lvColumns =
                new HashMap<String, Object>(lvNumCols);

        for (int i = 0; i < lvNumCols; i++) {
            final int lvPos = i + 1;
            try {
                final Object lvObject = resultSetObject.getObject(lvPos);

                if (lvObject != null) {
                    lvColumns.put(lvMetaResultSet.getColumnLabel(lvPos),
                            resultSetObject.getObject(lvPos));
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "resultSetMetaData.getColumnLabel("
                        + lvPos + ") / resultSetObject.getObject(" + lvPos
                        + ")", e);
                return null;
            }
        }

        return lvColumns;
    }

    /**
     * Attempts to establish a connection to the database URL given in the
     * properties.
     * 
     * @return <code>false</code> if the connection could not be established,
     *         or <code>true</code> otherwise.
     */
    public final boolean getConnection() {

        return getConnection(Configurator.getInstance().getProperty(
                Global.PROPERTY_PATH_1_DATABASE + "." + databaseIdent + "."
                        + Global.PROPERTY_PATH_3_URL), Configurator
                .getInstance().getProperty(
                        Global.PROPERTY_PATH_1_DATABASE + "." + databaseIdent
                                + "." + Global.PROPERTY_PATH_3_USER_NAME),
                Configurator.getInstance().getProperty(
                        Global.PROPERTY_PATH_1_DATABASE + "." + databaseIdent
                                + "." + Global.PROPERTY_PATH_3_PASSWORD));
    }

    /**
     * Attempts to establish a connection to the given database URL.
     * 
     * @param parUrl The database URL.
     * @param parUserName The name of the database user.
     * @param parPassword The password of the database user.
     * @return <code>false</code> if the connection could not be established,
     *         or <code>true</code> otherwise.
     */
    public final boolean getConnection(final String parUrl,
            final String parUserName, final String parPassword) {

        if (parUrl == null || "".equals(parUrl)) {
            throw new IllegalArgumentException("URL is missing");
        }

        if (parUserName == null || "".equals(parUserName)) {
            throw new IllegalArgumentException("Username is missing");
        }

        if (parPassword == null || "".equals(parPassword)) {
            throw new IllegalArgumentException("Password is missing");
        }

        try {
            connectionObject =
                    DriverManager.getConnection(parUrl, parUserName,
                            parPassword);

            assert connectionObject != null : "Postcondition: connectionObject is null";

            userName = parUserName;

            return true;
        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, "getConnection(" + parUrl + ", "
                    + parUserName + ", " + parPassword + ")", sqle);
        }

        return false;
    }

    /**
     * Returns the current <code>Connection</code> object.
     * 
     * @return the current <code>Connection</code> object.
     */
    public final Connection getConnectionObject() {

        return connectionObject;
    }

    /**
     * Returns the number of rows processed by the SQL query.
     * 
     * @return the number of rows processed by the SQL query.
     */
    public final int getNumberRows() {

        return numberRows;
    }

    private ResultSetMetaData getResultSetMetaData(final ResultSet parResultSet) {

        if (parResultSet == null) {
            throw new IllegalArgumentException("ResultSet is missing");
        }

        try {
            return parResultSet.getMetaData();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "resultSetObject.getMetaData()", e);
            return null;
        }
    }

    private int getResultSetMetaDataSize(
            final ResultSetMetaData parMetaResultSet) {

        if (parMetaResultSet == null) {
            throw new IllegalArgumentException("ResultSetMetaData is missing");
        }

        try {
            return parMetaResultSet.getColumnCount();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "resultSetMetaData.getColumnCount()", e);
            return 0;
        }
    }

    /**
     * Returns the current <code>ResultSet</code> object.
     * 
     * @return the current <code>ResultSet</code> object.
     */
    public final ResultSet getResultSetObject() {

        return resultSetObject;
    }

    /**
     * Returns the SQL syntax code of the target database.
     * 
     * @return the SQL syntax code of the target database.
     */
    public final String getSqlSyntaxCodeTarget() {
        return sqlSyntaxCodeTarget;
    }

    /**
     * Returns the current date after the execution of the SQL query.
     * 
     * @return the current date after the execution of the SQL query.
     */
    public final Date getTrialRunEndTime() {

        final Date lvDate = trialRunEndTime;

        return lvDate;
    }

    /**
     * Returns the error message triggered by the execution of the SQL query.
     * 
     * @return the error message triggered by the execution of the SQL query.
     */
    public final String getTrialRunErrorMessage() {

        return trialRunErrorMessage;
    }

    /**
     * Returns the current date before the execution of the SQL query.
     * 
     * @return the current date before the execution of the SQL query.
     */
    public final Date getTrialRunStartTime() {

        final Date lvDate = trialRunStartTime;

        return lvDate;
    }

    /**
     * Returns the response time in a desired precision.
     * 
     * @param parPrecision The exponent (base 10) of the desired presicion.
     * 
     * @return the response time in the desired precision.
     */
    public final long getTrialTimeQuantities(final long parPrecision) {

        if (parPrecision == 1) {
            return trialNanoSeconds;
        }

        long lvTime = trialNanoSeconds / parPrecision;

        if ((trialNanoSeconds - lvTime * parPrecision) >= parPrecision / 2) {
            lvTime++;
        }

        return lvTime;
    }

    /**
     * Returns the current user's name.
     * 
     * @return the current user's name.
     */
    public final String getUserName() {

        return userName;
    }

    /**
     * Moves the cursor forward one row from its current position.
     * 
     * @return <code>false</code> if there are no further rows to process, or
     *         <code>true</code> otherwise.
     */
    public final boolean next() {

        assert resultSetObject != null : "Precondition: ResultSet is missing (null)";

        try {
            return resultSetObject.next();
        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, "resultSetObject.next()", sqle);
        }

        return false;
    }

    /**
     * Undoes all changes made in the current transaction and releases any
     * database locks currently held by this <code>Connection</code> object.
     * 
     * @return <code>false</code> if the changes could not be made undone, or
     *         <code>true</code> otherwise.
     */
    public final boolean rollback() {

        assert connectionObject != null : ERROR_PRECONDITION_CONNECTION_IS_MISSING_NULL;

        try {
            connectionObject.rollback();

            return true;
        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, "connectionObject.rollback()", sqle);
        }

        return false;
    }

    /**
     * Creates a <code>Statement</code> object associtated with this
     * <code>Connnection</code> object.
     * 
     * @param parFetchSize The fetch size.
     * 
     * @return <code>true</code> if the fetch size could be modified, or
     *         <code>false</code> otherwise.
     */
    public final boolean setFetchSize(final int parFetchSize) {

        assert statementObject != null : ERROR_PRECONDITION_STATEMENT_OBJECT_IS_NULL;

        try {
            statementObject.setFetchSize(parFetchSize);

            return true;
        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, "connectionObject.setFetchSize()", sqle);
        }

        return false;
    }

}
