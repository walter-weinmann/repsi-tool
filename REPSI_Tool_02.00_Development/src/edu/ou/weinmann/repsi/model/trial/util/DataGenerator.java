package edu.ou.weinmann.repsi.model.trial.util;

import edu.ou.weinmann.repsi.model.mapper.TrialRunProtocolMapper;

import edu.ou.weinmann.repsi.model.trial.metadata.Column;
import edu.ou.weinmann.repsi.model.trial.metadata.Columns;

import edu.ou.weinmann.repsi.model.util.DatabaseAccessor;
import edu.ou.weinmann.repsi.model.util.Global;

import java.math.BigDecimal;

import java.sql.Date;

import java.text.SimpleDateFormat;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates the columns and rows of a given test database instance.
 * 
 * @author Walter Weinmann
 * 
 */
public class DataGenerator {

    private static final char[] ALPHA_NUMERIC_CHARS = createAlphaNumericChars();

    private static final int ALPHA_NUMERIC_SIZE = ALPHA_NUMERIC_CHARS.length;

    private static final String COLUMN_IS_MISSING = "Column is missing";

    private static final int DATE_DAY_RANGE = 100 * 365 + 1;

    private static final long DATE_DAY_SECONDS = 24 * 60 * 60;

    private static final Logger LOGGER =
            Logger.getLogger(DatabaseAccessor.class.getPackage().getName());

    private final Map<Character, Integer> alphaNumericMap =
            new HashMap<Character, Integer>(ALPHA_NUMERIC_SIZE);

    private final Columns columns;

    private final DatabaseAccessor dbAccess;

    private long numberRowsGenerated;

    private final Random random;

    private final String tableName;

    private long transactionSize;

    private final TrialRunProtocolMapper trialRunProtocol;

    /**
     * Constructs a <code>DataGenerator</code> object.
     * 
     * @param parTrialRunProtocol The <code>TralRunProtocol</code> object.
     * @param parDBAccess The <code>DatabaseAccessor</code> object.
     * @param parTableName Rhe name of an existing database table in the given
     *            database.
     * @param parColumns The <code>Columns</code> object containg the meta
     *            data description of the given database table.
     */
    public DataGenerator(final TrialRunProtocolMapper parTrialRunProtocol,
            final DatabaseAccessor parDBAccess, final String parTableName,
            final Columns parColumns) {

        super();

        assert parTrialRunProtocol != null : "Precondition: TrialRunProtocol is missing (null)";
        assert parDBAccess != null : "Precondition: DatabaseAccessor is missing (null)";
        assert parTableName != null : "Precondition: String table name is missing (null)";
        assert parColumns != null : "Precondition: Columns is missing (null)";

        columns = parColumns;

        for (int i = 0; i < ALPHA_NUMERIC_SIZE; i++) {
            alphaNumericMap.put(Character.valueOf(ALPHA_NUMERIC_CHARS[i]),
                    Integer.valueOf(i));
        }

        dbAccess = parDBAccess;
        numberRowsGenerated = 0;
        random = new Random();
        tableName = parTableName;
        trialRunProtocol = parTrialRunProtocol;
    }

    private static char[] createAlphaNumericChars() {

        final StringBuffer lvBufferDigits = new StringBuffer(10);
        final StringBuffer lvBufferLower = new StringBuffer(26);
        final StringBuffer lvBufferUpper = new StringBuffer(26);

        for (char c = 0; c < 255; c++) {

            if (c >= 'a' && c <= 'z') {
                lvBufferLower.append(c);
                continue;
            }

            if (c >= 'A' && c <= 'Z') {
                lvBufferUpper.append(c);
                continue;
            }

            if (c >= '0' && c <= '9') {
                lvBufferDigits.append(c);
            }
        }

        return lvBufferLower.append(lvBufferUpper).append(lvBufferDigits)
                .toString().toCharArray();
    }

    private String generateColumnValueDate(final Column parColumn) {

        if (parColumn.isForeignKeyColumn()) {
            return "CAST(TO_DATE('"
                    + new SimpleDateFormat(Global.DATE_FORMAT_DD_MM_YYYY_JAVA)
                            .format((Date) columns.getRandomFkValue(parColumn
                                    .getColumnName())) + "', '"
                    + Global.DATE_FORMAT_DD_MM_YYYY_SQL + "') AS DATE)";
        }

        if (parColumn.getKeySeq() != 0) {
            return "CAST(TO_DATE('"
                    + new SimpleDateFormat(Global.DATE_FORMAT_DD_MM_YYYY_JAVA)
                            .format((Date) columns.getRandomPkValue(parColumn
                                    .getColumnName())) + "', '"
                    + Global.DATE_FORMAT_DD_MM_YYYY_SQL + "') AS DATE)";
        }

        if (parColumn.getIsNullable().equals(Global.IS_NULLABLE_YES)
                && (random.nextBoolean())) {
            return Global.NULL;
        }

        return "CAST(TO_DATE('"
                + new SimpleDateFormat(Global.DATE_FORMAT_DD_MM_YYYY_JAVA)
                        .format(new Date(random.nextInt(DATE_DAY_RANGE)
                                * DATE_DAY_SECONDS)) + "', '"
                + Global.DATE_FORMAT_DD_MM_YYYY_SQL + "') AS DATE)";
    }

    /**
     * Generates a value for a given primary key column of type
     * <code>DATE</code>.
     * 
     * @param parColumn The name of the database column.
     * 
     * @return a valid value of a <code>DATE</code> column, or
     *         <code>null</code> if any error occured.
     */
    public final Date generateColumnValueDateKey(final Column parColumn) {

        if (parColumn == null) {
            throw new IllegalArgumentException(COLUMN_IS_MISSING);
        }

        final Date lvLastValue =
                (Date) columns.getPrimaryKey().getRandomPkValue(
                        parColumn.getColumnName());

        final Date lvNextValue =
                new Date(lvLastValue.getTime() + Global.DAY_FACTOR);

        if (lvNextValue.before(lvLastValue)) {
            return null;
        }

        return lvNextValue;
    }

    /**
     * Generates the lowest possible <code>DATE</code> value for a primary
     * key.
     * 
     * @return a <code>DATE</code> object.
     */
    public static final Date generateColumnValueDateKeyLow() {

        return new Date(-Global.DAY_FACTOR);
    }

    private String generateColumnValueNumeric(final Column parColumn) {

        if (parColumn == null) {
            throw new IllegalArgumentException(COLUMN_IS_MISSING);
        }

        if (parColumn.isForeignKeyColumn()) {
            return ((BigDecimal) columns.getRandomFkValue(parColumn
                    .getColumnName())).toString();
        }

        if (parColumn.getKeySeq() != 0) {
            return ((BigDecimal) columns.getRandomPkValue(parColumn
                    .getColumnName())).toString();
        }

        if (parColumn.getIsNullable().equals(Global.IS_NULLABLE_YES)
                && (random.nextBoolean())) {
            return Global.NULL;
        }

        final BigDecimal lvNextValue =
                new BigDecimal(random.nextDouble()
                        * parColumn.getDecimalFactor().doubleValue()).setScale(
                        parColumn.getDecimalDigits(),
                        BigDecimal.ROUND_HALF_EVEN);

        if (lvNextValue.compareTo(parColumn.getDecimalFactor()) == 0) {
            return lvNextValue.subtract(BigDecimal.ONE).toString();
        }

        return lvNextValue.toString();
    }

    /**
     * Generates a value for a given primary key column of type
     * <code>BigDecimal</code>.
     * 
     * @param parColumn The name of the database column.
     * 
     * @return a valid value of a <code>BigDecimal</code> column, or
     *         <code>null</code> if any error occured.
     */
    public final BigDecimal generateColumnValueNumericKey(final Column parColumn) {

        if (parColumn == null) {
            throw new IllegalArgumentException(COLUMN_IS_MISSING);
        }

        BigDecimal lvLastValue =
                (BigDecimal) columns.getPrimaryKey().getRandomPkValue(
                        parColumn.getColumnName());

        lvLastValue = lvLastValue.add(BigDecimal.ONE);

        if (lvLastValue.compareTo(parColumn.getDecimalFactor().subtract(
                BigDecimal.ONE)) <= 0) {
            return lvLastValue;
        }

        return null;
    }

    /**
     * Generates the lowest possible <code>BigDecimal</code> value for a
     * primary key.
     * 
     * @return a <code>BigDecimal</code> object.
     */
    public static final BigDecimal generateColumnValueNumericKeyLow() {

        return BigDecimal.ZERO;
    }

    private String generateColumnValueVarchar(final Column parColumn,
            final int parDataType) {

        if (parColumn == null) {
            throw new IllegalArgumentException(COLUMN_IS_MISSING);
        }

        if (parColumn.isForeignKeyColumn()) {
            return (String) columns.getRandomFkValue(parColumn.getColumnName());
        }

        if (parColumn.getKeySeq() != 0) {
            return (String) columns.getRandomPkValue(parColumn.getColumnName());
        }

        if (parColumn.getIsNullable().equals(Global.IS_NULLABLE_YES)
                && (random.nextBoolean())) {
            return Global.NULL;
        }

        final Object lvObject =
                columns.getRandomFkValue(parColumn.getColumnName());

        if (lvObject != null) {
            return (String) lvObject;
        }

        int lvLength;

        if (parDataType == java.sql.Types.CHAR) {
            lvLength = parColumn.getColumnSize();
        } else {
            lvLength = random.nextInt(parColumn.getColumnSize() + 1);
            if (lvLength == 0) {
                if (parColumn.getIsNullable().equals(Global.IS_NULLABLE_YES)) {
                    return Global.NULL;
                }

                lvLength++;
            }
        }

        final StringBuffer lvValue = new StringBuffer(lvLength);

        for (int i = 0; i < lvLength; i++) {
            lvValue.append(ALPHA_NUMERIC_CHARS[random
                    .nextInt(ALPHA_NUMERIC_SIZE)]);
        }

        return lvValue.toString();
    }

    /**
     * Generates a value for a given primary key column of type
     * <code>String</code>.
     * 
     * @param parColumn The name of the database column.
     * 
     * @return a valid value of a <code>String</code> column, or
     *         <code>null</code> if any error occured.
     */
    public final String generateColumnValueVarcharKey(final Column parColumn) {

        if (parColumn == null) {
            throw new IllegalArgumentException(COLUMN_IS_MISSING);
        }

        final String lvLastValue =
                (String) columns.getPrimaryKey().getRandomPkValue(
                        parColumn.getColumnName());

        if ((lvLastValue == null) || "".equals(lvLastValue)) {

            return generateColumnValueVarcharKeyLow(parColumn);
        }

        // Change a single char value ******************************************
        final char[] lvChar = lvLastValue.toCharArray();

        for (int i = lvChar.length - 1; i >= 0; i--) {

            if (lvChar[i] == ALPHA_NUMERIC_CHARS[ALPHA_NUMERIC_SIZE - 1]) {
                continue;
            }

            lvChar[i] =
                    ALPHA_NUMERIC_CHARS[alphaNumericMap.get(
                            Character.valueOf(lvChar[i])).intValue() + 1];

            for (int j = i + 1; j < lvChar.length; j++) {
                lvChar[j] = ALPHA_NUMERIC_CHARS[0];
            }

            return new StringBuffer(lvChar.length).append(lvChar).toString();
        }

        // Maximum length exceeded *********************************************
        return "";
    }

    /**
     * Generates the lowest possible <code>String</code> value for a primary
     * key.
     * 
     * @return a <code>String</code> object.
     */
    public static final String generateColumnValueVarcharKeyLow() {

        return "";
    }

    /**
     * Generates the lowest possible <code>String</code> value for a given
     * primary key column.
     * 
     * @param parColumn The name of the database column.
     * 
     * @return a <code>String</code> object.
     */
    public static final String generateColumnValueVarcharKeyLow(
            final Column parColumn) {

        if (parColumn == null) {
            throw new IllegalArgumentException(COLUMN_IS_MISSING);
        }

        final int lvSize = parColumn.getColumnSize() - 1;
        final StringBuffer lvBuffer = new StringBuffer(lvSize);

        for (int i = 0; i < lvSize; i++) {
            lvBuffer.append(ALPHA_NUMERIC_CHARS[0]);
        }

        return lvBuffer.append(ALPHA_NUMERIC_CHARS[0]).toString();
    }

    /**
     * Generates set of rows in a database table.
     * 
     * @param parExecutionFrequency The number of rows to be generated.
     * 
     * @return <code>true</code> if the required number of rows was generated
     *         successfully, and <code>false</code> otherwise.
     */
    public final boolean generateRow(final long parExecutionFrequency) {

        if (parExecutionFrequency == 0) {
            return true;
        }

        transactionSize = 0;

        for (int i = 0; i < parExecutionFrequency; i++) {

            insertRow();

            if (trialRunProtocol.isAborted()) {
                return false;
            }

            if (transactionSize >= Global.MAX_TRANSACTION_SIZE) {
                if (!dbAccess.commit()) {
                    return false;
                }

                transactionSize = 0;

                LOGGER
                        .log(Level.FINER,
                                "Transaction limit succeeded, maximum="
                                        + Global.MAX_TRANSACTION_SIZE
                                        + " - total processed currently="
                                        + numberRowsGenerated);
            }
        }

        if (!dbAccess.commit()) {
            return false;
        }

        return true;
    }

    /**
     * Returns the effective number of generated rows.
     * 
     * @return the effective number of generated rows.
     */
    public final long getNumberRowsGenerated() {

        return numberRowsGenerated;
    }

    private boolean insertRow() {

        final StringBuffer lvNames = new StringBuffer();
        final StringBuffer lvValues = new StringBuffer();

        if (!columns.determineRandomForeignKeys()) {
            return false;
        }

        if (!columns.determineRandomPrimaryKey(this)) {
            return false;
        }

        for (int i = 1; i <= columns.sizeColumns(); i++) {

            if (i != 1) {
                lvNames.append(',');
                lvValues.append(',');
            }

            final String lvColumnName = columns.getColumnName(i);
            final Column lvColumn = columns.getColumn(lvColumnName);

            lvNames.append(lvColumnName);

            final int lvDataType = lvColumn.getDataType();

            switch (lvDataType) {
                case java.sql.Types.CHAR:
                case java.sql.Types.VARCHAR:
                    final String lvValue =
                            generateColumnValueVarchar(lvColumn, lvDataType);
                    if (lvValue.equals(Global.NULL)) {
                        lvValues.append(lvValue);
                    } else {
                        lvValues.append('\'' + lvValue + '\'');
                    }
                    break;
                case java.sql.Types.DATE:
                    lvValues.append(generateColumnValueDate(lvColumn));
                    break;
                case java.sql.Types.DECIMAL:
                case java.sql.Types.NUMERIC:
                    lvValues.append(generateColumnValueNumeric(lvColumn));
                    break;
                default:
                    trialRunProtocol.createErrorProtocol("SQL type="
                            + lvColumn.getDataType()
                            + Global.ERROR_NOT_YET_IMPLEMENTED, true);
                    return false;
            }
        }

        final boolean lvOk =
                dbAccess.executeUpdate("INSERT INTO " + tableName + " ("
                        + lvNames.toString() + ") VALUES ("
                        + lvValues.toString() + ");", true);

        if (lvOk) {
            numberRowsGenerated++;
            transactionSize++;
        }

        return lvOk;
    }
}
