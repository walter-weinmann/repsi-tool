package edu.ou.weinmann.repsi.model.database;

import edu.ou.weinmann.repsi.model.util.Configurator;
import edu.ou.weinmann.repsi.model.util.DatabaseAccessor;
import edu.ou.weinmann.repsi.model.util.Global;
import edu.ou.weinmann.repsi.model.util.SQLRewriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * Manages the functionality to modify the schema or instance of a relational
 * database.
 * 
 * @author Walter Weinmann
 * 
 */
public class Database extends DefaultHandler {

    private static final String FILE = "File=";

    private static final Logger LOGGER =
            Logger.getLogger(Database.class.getPackage().getName());

    private static final String NOT_FOUND = " not found";

    private static final String SHEET = "Sheet=";

    private String[] columnNames;

    private Map<String, Object> columnsDatabaseInstance;

    private int[] dataTypes;

    private final DatabaseAccessor dbAccessMaster;

    private DatabaseAccessor dbAccessTest;

    private boolean isMaster;

    private final SQLRewriter sqlRewriter = new SQLRewriter();

    private String sqlSyntaxSource = Global.SQL_SYNTAX_CODE_SQL_99;

    private String sqlSyntaxTarget = Global.SQL_SYNTAX_CODE_SQL_99;

    private StringBuffer statementBuffer;

    /**
     * Constructs a <code>Database</code> object. The name of the used
     * properties file is taken from mthe class
     * <code>edu.ou.weinmann.repsi.model.util.Global</code> and does not
     * constitute an XML document.
     */
    public Database() {

        this(Global.PROPERTIES_FILE_NAME, false);
    }

    /**
     * Constructs a <code>Database</code> object.
     * 
     * @param parProprtiesFilename complete filename of the properties file
     *            including the directory.
     * @param parPropertiesXml <code>true</code> if the properties file
     *            constitutes an XML document.
     */
    public Database(final String parProprtiesFilename,
            final boolean parPropertiesXml) {

        super();

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.entering(this.getClass().getName(), "Database",
                    new Object[] { parProprtiesFilename,
                            Boolean.valueOf(parPropertiesXml), });
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
            LOGGER.exiting(this.getClass().getName(), "Database");
        }
    }

    /**
     * Receive notification of character data inside an element.
     * 
     * @param parCharacters The characters from the element.
     * @param parStart The start position in the character array.
     * @param parLength The number of characters to use from the character
     *            array.
     * 
     * @throws SAXException Any SAX exception, possibly wrapping another
     *             exception.
     * 
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    @SuppressWarnings("unused")
    public final void characters(final char[] parCharacters,
            final int parStart, final int parLength) throws SAXException {

        statementBuffer.append(new String(parCharacters, parStart, parLength));
    }

    /**
     * Receive notification of the end of an element.
     * 
     * @param parUri The Namespace URI, or the empty string if the element has
     *            no Namespace URI or if Namespace processing is not being
     *            performed.
     * @param parLocalName The local name (without prefix), or the empty string
     *            if Namespace processing is not being performed.
     * @param parQualifiedName The qualified name (with prefix), or the empty
     *            string if qualified names are not available.
     * 
     * @throws SAXException any SAX exception, possibly wrapping another
     *             exception
     * 
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    @SuppressWarnings("unused")
    public final void endElement(final String parUri,
            final String parLocalName, final String parQualifiedName)
            throws SAXException {

        if (parQualifiedName.equals("Session")) {
            return;
        }

        if (!(parQualifiedName.equals("DDL_Statement") || parQualifiedName
                .equals("DML_Statement"))) {
            LOGGER.log(Level.SEVERE, "Error with endElement(), uri=" + parUri
                    + " localName=" + parLocalName + " qName="
                    + parQualifiedName);
            return;
        }

        final String lvStatementTranslated =
                sqlRewriter.rewrite(sqlSyntaxSource, sqlSyntaxTarget,
                        statementBuffer.toString());

        if (!sqlRewriter.getLastErrorMsg().equals("")) {
            LOGGER.log(Level.SEVERE, sqlRewriter.getLastErrorMsg());
            return;
        }

        if ("".equals(lvStatementTranslated)) {
            return;
        }

        if (isMaster) {
            if (!dbAccessMaster.executeUpdateDirect(lvStatementTranslated,
                    false)) {
                LOGGER.log(Level.SEVERE, "Error with executeUpdateDirect("
                        + lvStatementTranslated + ") - master database");
            }

            return;
        }

        if (!dbAccessTest.executeUpdateDirect(lvStatementTranslated, false)) {
            LOGGER.log(Level.SEVERE, "Error with executeUpdateDirect("
                    + lvStatementTranslated + ") - test database");
        }
    }

    private boolean executeStatement(
            final DatabaseAccessor parDatabaseAccessor,
            final String parStatement) {

        final String lvStatementTranslated =
                sqlRewriter.rewrite(sqlSyntaxSource, sqlSyntaxTarget,
                        parStatement);

        if (!sqlRewriter.getLastErrorMsg().equals("")) {
            LOGGER.log(Level.SEVERE, sqlRewriter.getLastErrorMsg());
            return false;
        }

        if ("".equals(lvStatementTranslated)) {
            return true;
        }

        if (!parDatabaseAccessor.executeUpdateDirect(lvStatementTranslated,
                false)) {
            LOGGER.log(Level.SEVERE, "Error with executeUpdateDirect("
                    + lvStatementTranslated + ")");
            return false;
        }

        return true;
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

    private boolean getDatabaseInstance(final int parDatabaseInstanceId) {

        columnsDatabaseInstance =
                getColumns("SELECT JDBC_DRIVER, JDBC_URL, PASSWORD, "
                        + "SQL_SYNTAX_CODE, USER_NAME "
                        + "FROM TMD_DATABASE_INSTANCE DEI, "
                        + "TMD_DATABASE_SYSTEM DES "
                        + "WHERE DEI.DATABASE_SYSTEM_CODE = "
                        + "DES.DATABASE_SYSTEM_CODE AND DATABASE_INSTANCE_ID = "
                        + parDatabaseInstanceId + ";");
        if (columnsDatabaseInstance == null) {
            LOGGER.log(Level.SEVERE, "Database instance="
                    + parDatabaseInstanceId + " is not available");
            return false;
        }

        return true;
    }

    private String getSheetDetail(final DatabaseAccessor parDatabaseAccessor,
            final String parTableName, final int parRowNumber,
            final Cell[] parCells) {

        boolean lvIsDateCreated = true;

        final ResultSet lvRS =
                parDatabaseAccessor.checkColumnName(parTableName,
                        "DATE_CREATED");

        if (lvRS == null) {
            lvIsDateCreated = false;
        }

        final StringBuffer lvStatement1 =
                new StringBuffer("INSERT INTO " + parTableName + "(");
        final StringBuffer lvStatement2 = new StringBuffer(") VALUES (");

        if (lvIsDateCreated) {
            lvStatement1.append("CREATED_BY, DATE_CREATED");
            lvStatement2.append("'" + parDatabaseAccessor.getUserName()
                    + "', SYSDATE");
        }

        for (int j = 0; j < parCells.length && j < columnNames.length; j++) {

            final String lvValueHilf = parCells[j].getContents();

            if ("".equals(lvValueHilf)) {
                continue;
            }

            if (!(!lvIsDateCreated && (j == 0))) {
                lvStatement1.append(", ");
                lvStatement2.append(", ");
            }

            lvStatement1.append(columnNames[j]);

            switch (dataTypes[j]) {
                case java.sql.Types.CHAR:
                case java.sql.Types.VARCHAR:

                    lvStatement2.append("'" + lvValueHilf.replaceAll("'", "''")
                            + "'");

                    break;
                case java.sql.Types.DATE:

                    lvStatement2.append("CAST(" + lvValueHilf + " AS DATE)");

                    break;
                case java.sql.Types.DECIMAL:
                case java.sql.Types.NUMERIC:

                    lvStatement2.append(lvValueHilf);

                    break;
                default:
                    LOGGER.log(Level.SEVERE, SHEET + parTableName
                            + " data type=" + dataTypes[j]
                            + " is not yet supported, row #="
                            + (parRowNumber + 1) + ", column #=" + (j + 1));
                    return "";
            }
        }

        return lvStatement1.append(lvStatement2).append(");").toString();
    }

    private boolean getSheetHeader(final DatabaseAccessor parDatabaseAccessor,
            final Sheet parSheet, final String parTableName) {

        final Cell[] lvCells = parSheet.getRow(0);

        columnNames = new String[lvCells.length];
        dataTypes = new int[lvCells.length];

        for (int i = 0; i < lvCells.length; i++) {
            columnNames[i] = lvCells[i].getContents();
            final ResultSet lvRS =
                    parDatabaseAccessor.checkColumnName(parTableName,
                            columnNames[i]);

            if (lvRS == null) {

                LOGGER.log(Level.SEVERE, "Database table=" + parTableName
                        + ", column=" + columnNames[i] + NOT_FOUND);
                return false;
            }

            try {
                dataTypes[i] = lvRS.getInt("DATA_TYPE");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "getSheetHeader(...)", e);
                return false;
            }
        }

        return true;
    }

    private boolean modifyDatabaseByExcel(
            final DatabaseAccessor parDatabaseAccessor, final String parFileName) {

        // Open excel file.
        Workbook lvWorkbook = null;

        try {
            lvWorkbook = Workbook.getWorkbook(new File(parFileName));
        } catch (FileNotFoundException e1) {
            parDatabaseAccessor.closeConnection();

            LOGGER.log(Level.SEVERE, FILE + parFileName + NOT_FOUND, e1);
            return false;
        } catch (IOException e2) {
            parDatabaseAccessor.closeConnection();

            LOGGER.log(Level.SEVERE, FILE + parFileName + " - IOException", e2);
            return false;
        } catch (jxl.read.biff.BiffException e3) {
            parDatabaseAccessor.closeConnection();

            LOGGER.log(Level.SEVERE, FILE + parFileName
                    + " - jxl.read.biff.BiffException", e3);
            return false;
        }

        boolean lvIsOk = true;

        final String[] lvSheetNames = lvWorkbook.getSheetNames();

        for (int i = 0; i < lvSheetNames.length; i++) {
            lvIsOk =
                    modifyDatabaseByExcelSheet(parDatabaseAccessor, lvWorkbook
                            .getSheet(lvSheetNames[i]), lvSheetNames[i]);
            if (!lvIsOk) {
                return false;
            }
        }

        // Close excel file.
        lvWorkbook.close();

        return lvIsOk;
    }

    private boolean modifyDatabaseByExcelSheet(
            final DatabaseAccessor parDatabaseAccessor, final Sheet parSheet,
            final String parTableName) {

        boolean lvIsOk = true;

        LOGGER.log(Level.FINE, "Processing database table " + parTableName);

        if (parDatabaseAccessor.checkTableName(parTableName) == null) {

            LOGGER.log(Level.SEVERE, "Database table=" + parTableName
                    + NOT_FOUND);
            return false;
        }

        final int lvNumberRows = parSheet.getRows();

        if (lvNumberRows == 0) {
            LOGGER.log(Level.SEVERE, SHEET + parTableName + " is empty");
            return false;
        }

        if (!getSheetHeader(parDatabaseAccessor, parSheet, parTableName)) {
            return false;
        }

        if (lvNumberRows == 1) {
            LOGGER.log(Level.WARNING, SHEET + parTableName
                    + " contains no details");
            return true;
        }

        for (int i = 1; i < lvNumberRows; i++) {
            final Cell[] lvCells = parSheet.getRow(i);

            if (lvCells.length > columnNames.length) {
                LOGGER.log(Level.WARNING, SHEET + parTableName
                        + " header column(s) missing, row #=" + (i + 1));
            }

            final String lvStatement =
                    getSheetDetail(parDatabaseAccessor, parTableName, i,
                            lvCells);
            if ("".equals(lvStatement)) {
                return false;
            }

            if (!executeStatement(parDatabaseAccessor, lvStatement)) {
                lvIsOk = false;
            }
        }

        return lvIsOk;
    }

    private boolean modifyDatabaseByFlatFile(
            final DatabaseAccessor parDatabaseAccessor, final String parFileName) {

        // Open flat file.
        BufferedReader lvFlatFile = null;

        try {
            lvFlatFile = new BufferedReader(new FileReader(parFileName));
        } catch (FileNotFoundException e) {
            parDatabaseAccessor.closeConnection();

            LOGGER.log(Level.SEVERE, FILE + parFileName + NOT_FOUND, e);
            return false;
        }

        boolean lvIsOk;

        // Process file.
        try {
            lvIsOk =
                    modifyDatabaseByFlatFileStatements(parDatabaseAccessor,
                            lvFlatFile);
        } catch (IOException e) {
            parDatabaseAccessor.closeConnection();

            LOGGER.log(Level.SEVERE, "Error with readLine()", e);
            return false;
        }

        // Close flat file.
        try {
            lvFlatFile.close();
        } catch (IOException e) {
            parDatabaseAccessor.closeConnection();

            LOGGER.log(Level.SEVERE, "Error with close()", e);
            return false;
        }

        return lvIsOk;
    }

    private boolean modifyDatabaseByFlatFileStatements(
            final DatabaseAccessor parDatabaseAccessor,
            final BufferedReader parFlatFile) throws IOException {

        boolean lvIsOk = true;

        String lvStatement;

        while ((lvStatement = parFlatFile.readLine()) != null) {

            final String lvStatementTranslated =
                    sqlRewriter.rewrite(sqlSyntaxSource, sqlSyntaxTarget,
                            lvStatement);

            if (!sqlRewriter.getLastErrorMsg().equals("")) {
                LOGGER.log(Level.SEVERE, sqlRewriter.getLastErrorMsg());
                lvIsOk = false;
                continue;
            }

            if ("".equals(lvStatementTranslated)) {
                continue;
            }

            if (!parDatabaseAccessor.executeUpdateDirect(lvStatementTranslated,
                    false)) {
                LOGGER.log(Level.SEVERE, "Error with executeUpdateDirect("
                        + lvStatementTranslated + ")");
                lvIsOk = false;
                continue;
            }
        }

        return lvIsOk;
    }

    private boolean modifyDatabaseByXml(final String parFileName) {

        try {
            SAXParserFactory.newInstance().newSAXParser().parse(
                    new File(parFileName), this);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, "XML file=" + parFileName
                    + ", SAXException=", e);
            return false;
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, "XML file=" + parFileName
                    + ", ParserConfigurationException=", e);
            return false;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "XML file=" + parFileName
                    + ", IOException=", e);
            return false;
        }

        return true;
    }

    /**
     * Modifies the master database schema or instance based on an Excel file, a
     * flat file or an XML document containing SQL statements.
     * 
     * Reads the SQL statements from an Excel file, a flat file or an XML
     * document and processes the contained data by considering the SQL syntax
     * property related to the SQL statements in the input source and the SQL
     * syntax property related to the master database which is defined in the
     * file <code>configuration.properties</code>.
     * 
     * @param parFileName The complete file name including the directory.
     * @param parFileType The file type: <bold>xls</bold> if the input source
     *            consists of an Excel file, <bold>xml</bold> if the input
     *            source consists of an XML document and anything else if the
     *            imput source consists of a simple flat file.
     * 
     * @return <code>true</code> if the processing of the file was completed
     *         without any error, and <code>false</code> otherwise.
     */
    public final boolean modifyMasterDatabase(final String parFileName,
            final String parFileType) {

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.entering(this.getClass().getName(), "modifyMasterDatabase",
                    new Object[] { parFileName, sqlSyntaxSource });
        }

        isMaster = true;

        if (parFileName == null || "".equals(parFileName)) {
            throw new IllegalArgumentException("Filename is missing");
        }

        if (!modifyMasterDatabaseOpen()) {
            return false;
        }

        sqlSyntaxTarget =
                Configurator.getInstance().getProperty(
                        Global.PROPERTY_PATH_1_DATABASE + "."
                                + Global.DATABASE_SCHEMA_IDENTIFIER_MASTER
                                + "." + Global.PROPERTY_PATH_3_SQL_SYNTAX_CODE);

        boolean lvIsOk;

        if (Global.FILE_TYPE_EXCEL.equals(parFileType)) {
            lvIsOk = modifyDatabaseByExcel(dbAccessMaster, parFileName);
        } else if (Global.FILE_TYPE_XML.equals(parFileType)) {
            lvIsOk = modifyDatabaseByXml(parFileName);
        } else {
            lvIsOk = modifyDatabaseByFlatFile(dbAccessMaster, parFileName);
        }

        if (!modifyMasterDatabaseClose(dbAccessMaster, "master database")) {
            return false;
        }

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.exiting(this.getClass().getName(), "modifyMasterDatabase",
                    Boolean.valueOf(lvIsOk));
        }

        return lvIsOk;
    }

    private boolean modifyMasterDatabaseClose(
            final DatabaseAccessor parDatabaseAccessor,
            final String parDatabaseType) {

        // Commit the transaction.
        if (!parDatabaseAccessor.commit()) {
            parDatabaseAccessor.closeConnection();

            LOGGER
                    .log(Level.SEVERE, "Error with commit() - "
                            + parDatabaseType);
            return false;
        }

        // Close the database connection.
        if (!parDatabaseAccessor.closeConnection()) {
            LOGGER.log(Level.SEVERE, "Error with closeConnection() - "
                    + parDatabaseType);
            return false;
        }

        return true;
    }

    private boolean modifyMasterDatabaseOpen() {

        // Create a database connection.
        if (!dbAccessMaster.getConnection()) {
            LOGGER.log(Level.SEVERE,
                    "Error with getConnection() - master database");
            return false;
        }

        // Create a statement.
        if (!dbAccessMaster.createStatement()) {
            dbAccessMaster.closeConnection();

            LOGGER.log(Level.SEVERE,
                    "Error with createStatement() - master database");
            return false;
        }

        return true;
    }

    /**
     * Modifies a test database schema or instance based on an Excel file, a
     * flat file or an XML document containing SQL statements.
     * 
     * Reads the SQL statements from an Excel file, a flat file or an XML
     * document and processes the contained data by considering the SQL syntax
     * property related to the SQL statements in the input source and the SQL
     * syntax property related to the master database which is defined in the
     * file <code>configuration.properties</code>.
     * 
     * @param parDatabaseInstanceId The identification of the test database
     *            instance.
     * @param parFileName The complete file name including the directory.
     * @param parFileType The file type: <bold>xls</bold> if the input source
     *            consists of an Excel file, <bold>xml</bold> if the input
     *            source consists of an XML document and anything else if the
     *            imput source consists of a simple flat file.
     * 
     * @return <code>true</code> if the processing of the file was completed
     *         without any error, and <code>false</code> otherwise.
     */
    public final boolean modifyTestDatabase(final int parDatabaseInstanceId,
            final String parFileName, final String parFileType) {

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.entering(this.getClass().getName(), "modifyTestDatabase",
                    new Object[] { parFileName, sqlSyntaxSource });
        }

        isMaster = false;

        if (parFileName == null || "".equals(parFileName)) {
            throw new IllegalArgumentException("Filename is missing");
        }

        if (!modifyMasterDatabaseOpen()) {
            return false;
        }

        // Determine the data related to the database instance *****************
        if (!getDatabaseInstance(parDatabaseInstanceId)) {
            return false;
        }

        sqlSyntaxTarget =
                (String) columnsDatabaseInstance.get("SQL_SYNTAX_CODE");

        dbAccessTest =
                new DatabaseAccessor(Global.DATABASE_SCHEMA_IDENTIFIER_TEST,
                        sqlSyntaxTarget, (String) columnsDatabaseInstance
                                .get("JDBC_DRIVER"), false);

        if (!modifyTestDatabaseOpen()) {
            return false;
        }

        boolean lvIsOk;

        if (Global.FILE_TYPE_EXCEL.equals(parFileType)) {
            lvIsOk = modifyDatabaseByExcel(dbAccessTest, parFileName);
        } else if (Global.FILE_TYPE_XML.equals(parFileType)) {
            lvIsOk = modifyDatabaseByXml(parFileName);
        } else {
            lvIsOk = modifyDatabaseByFlatFile(dbAccessTest, parFileName);
        }

        if (!modifyMasterDatabaseClose(dbAccessTest, "test database")) {
            return false;
        }

        if (!modifyMasterDatabaseClose(dbAccessMaster, "master database")) {
            return false;
        }

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.exiting(this.getClass().getName(), "modifyTestDatabase",
                    Boolean.valueOf(lvIsOk));
        }

        return lvIsOk;
    }

    private boolean modifyTestDatabaseOpen() {

        // Create a database connection.
        if (!dbAccessTest.getConnection((String) columnsDatabaseInstance
                .get("JDBC_URL"), (String) columnsDatabaseInstance
                .get(Global.COLUMN_NAME_USER_NAME),
                (String) columnsDatabaseInstance.get("PASSWORD"))) {
            LOGGER.log(Level.SEVERE,
                    "Error with getConnection() - test database");
            return false;
        }

        // Create a statement.
        if (!dbAccessTest.createStatement()) {
            dbAccessTest.closeConnection();

            LOGGER.log(Level.SEVERE,
                    "Error with createStatement - test database()");
            return false;
        }

        return true;
    }

    /**
     * Sets the type of the SQL syntax version.
     * 
     * @param parSqlSyntaxCode The type of the SQL syntax version.
     */
    public final void setSqlSyntaxSource(final String parSqlSyntaxCode) {

        if (parSqlSyntaxCode == null || "".equals(parSqlSyntaxCode)) {
            throw new IllegalArgumentException("SQL syntax code source missing");
        }

        sqlSyntaxSource = parSqlSyntaxCode;
    }

    /**
     * Receive notification of the start of an element.
     * 
     * @param parUri The Namespace URI, or the empty string if the element has
     *            no Namespace URI or if Namespace processing is not being
     *            performed.
     * @param parLocalName The local name (without prefix), or the empty string
     *            if Namespace processing is not being performed.
     * @param parQualifiedName The qualified name (with prefix), or the empty
     *            string if qualified names are not available.
     * @param parAttributes The attributes attached to the element. If there are
     *            no attributes, it shall be an empty Attributes object.
     * 
     * @throws SAXException any SAX exception, possibly wrapping another
     *             exception
     * 
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    @SuppressWarnings("unused")
    public final void startElement(final String parUri,
            final String parLocalName, final String parQualifiedName,
            final Attributes parAttributes) throws SAXException {

        statementBuffer = new StringBuffer();

        if (!(parQualifiedName.equals("DDL_Statement")
                || parQualifiedName.equals("DML_Statement") || parQualifiedName
                .equals("Session"))) {
            LOGGER.log(Level.SEVERE, "Error with startElement(), uri=" + parUri
                    + " localName=" + parLocalName + " qName="
                    + parQualifiedName + " atts=" + parAttributes.toString());
        }
    }
}
