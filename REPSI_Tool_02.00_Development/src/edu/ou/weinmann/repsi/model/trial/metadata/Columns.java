package edu.ou.weinmann.repsi.model.trial.metadata;

import edu.ou.weinmann.repsi.model.mapper.TrialRunProtocolMapper;

import edu.ou.weinmann.repsi.model.trial.util.DataGenerator;

import edu.ou.weinmann.repsi.model.util.DatabaseAccessor;
import edu.ou.weinmann.repsi.model.util.Global;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the meta data a database table: primary key, imported foreign keys
 * and database columns.
 * 
 * @author Walter Weinmann
 * 
 */
public final class Columns {

    private static final Logger LOGGER =
            Logger.getLogger(Columns.class.getPackage().getName());

    private String[] columnNames;

    private final Map<String, Column> columns;

    private ForeignKeys foreignKeys;

    private PrimaryKey primaryKey;

    private final Random random;

    private ArrayList<String> randomFkNames;

    private ArrayList<Object> randomFkValues;

    private final String tableName;

    /**
     * Constructs a <code>Columns</code> object.
     * 
     * @param parTrialRunProtocol The <code>TrialRunProtocolMapper</code>
     *            object.
     * @param parDBAccess The <code>DatabaseAccessor</code> object.
     * @param parCatalog The database catlogue name.
     * @param parSchema The database schema name.
     * @param parTableName The name of an existing database table in the given
     *            database.
     */
    public Columns(final TrialRunProtocolMapper parTrialRunProtocol,
            final DatabaseAccessor parDBAccess, final String parCatalog,
            final String parSchema, final String parTableName) {

        super();

        assert parDBAccess != null : "Precondition: DatabaseAccessor is missing (null)";
        assert parTableName != null : "Precondition: String table name is missing (null)";

        tableName = parTableName;
        random = new Random();

        // Determine the database table columns ********************************
        columns =
                determineColumns(parDBAccess, parCatalog, parSchema, tableName);

        if (columns == null) {
            return;
        }

        if (columns.size() == 0) {
            LOGGER.log(Level.SEVERE, "Database column meta data: table="
                    + tableName + " - columns missing");
        }

        // Determine the primary key *******************************************
        primaryKey =
                determinePrimaryKey(parTrialRunProtocol, parDBAccess,
                        parCatalog, parSchema);

        // Determine the foreign keys ******************************************
        foreignKeys =
                new ForeignKeys(parDBAccess, parCatalog, parSchema, tableName,
                        columns);

        // Protocol the database table columns *********************************
        columnNames = new String[columns.size()];

        final Set<String> lvSet = columns.keySet();
        final Iterator<String> lvIt = lvSet.iterator();

        while (lvIt.hasNext()) {
            final String lvColumnName = lvIt.next();

            columnNames[columns.get(lvColumnName).getOrdPosition()] =
                    lvColumnName;
        }
    }

    private Map<String, Column> determineColumns(
            final DatabaseAccessor parDBAccess, final String parCatalog,
            final String parSchemaPattern, final String parTableNamePattern) {

        // Database connection *************************************************
        final Connection lvConnection = parDBAccess.getConnectionObject();

        assert lvConnection != null : "Invariant: Connection is null";

        // ResultSet with the column descriptions ******************************
        ResultSet lvResultSet;

        try {
            lvResultSet =
                    lvConnection.getMetaData().getColumns(parCatalog,
                            parSchemaPattern, parTableNamePattern, "%");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "connection.getMetaData().getExportedKeys(" + parCatalog
                            + "," + parSchemaPattern + ","
                            + parTableNamePattern + ")", e);
            return null;
        }

        assert lvResultSet != null : "Invariant: ResultSet (connection.getMetaData().xxx) is null";

        // ResultSet with database meta data ***********************************
        ResultSetMetaData lvMetaResultSet;

        try {
            lvMetaResultSet = lvResultSet.getMetaData();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "resultSetObject.getMetaData()", e);
            return null;
        }

        assert lvMetaResultSet != null : "Invariant: ResultSet (ResultSetMetaData) is null";

        // Process the columns *************************************************
        final Map<String, Column> lvColumns = new HashMap<String, Column>();

        try {
            while (lvResultSet.next()) {
                final String lvColumnName =
                        lvResultSet.getString(Global.META_DATA_COLUMN_NAME);

                final Column lvColumn =
                        new Column(
                                lvColumnName,
                                lvResultSet
                                        .getInt(Global.META_DATA_ORDINAL_POSITION),
                                lvResultSet.getInt(Global.META_DATA_DATA_TYPE),
                                lvResultSet
                                        .getInt(Global.META_DATA_COLUMN_SIZE),
                                lvResultSet
                                        .getInt(Global.META_DATA_DECIMAL_DIGITS),
                                lvResultSet
                                        .getString(Global.META_DATA_IS_NULLABLE));
                lvColumns.put(lvColumnName, lvColumn);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "resultSetObject.next() / determineColumns()", e);

            parDBAccess.closeResultSet(lvResultSet);

            return null;
        }

        if (!parDBAccess.closeResultSet(lvResultSet)) {
            return null;
        }

        return lvColumns;
    }

    private PrimaryKey determinePrimaryKey(
            final TrialRunProtocolMapper parTrialRunProtocol,
            final DatabaseAccessor parDBAccess, final String parCatalog,
            final String parSchema) {

        final PrimaryKey lvPrimaryKey =
                new PrimaryKey(parTrialRunProtocol, parDBAccess, parCatalog,
                        parSchema, tableName, columns);

        final String[] lvPkColumnNames = lvPrimaryKey.getColumnNames();

        for (int i = 0; i < lvPkColumnNames.length; i++) {
            final Column lvPkColumn = columns.get(lvPkColumnNames[i]);
            lvPkColumn.setKeySeq(i + 1);
            columns.put(lvPkColumnNames[i], lvPkColumn);
        }

        return lvPrimaryKey;
    }

    /**
     * Determines a random set of foreign key values.
     * 
     * @return <code>true</code> if the processing finished without errors.
     */
    public boolean determineRandomForeignKeys() {

        randomFkNames =
                new ArrayList<String>(Global.INITIAL_CAPACITY_FOREIGN_KEYS);
        randomFkValues =
                new ArrayList<Object>(Global.INITIAL_CAPACITY_FOREIGN_KEYS);

        final Set<String> lvSet = foreignKeys.getForeignKeys().keySet();

        final Iterator<String> lvIt = lvSet.iterator();

        while (lvIt.hasNext()) {
            final ForeignKey lvForeignKey =
                    foreignKeys.getForeignKeys().get(lvIt.next());

            final int lvSizePkColumnValues = lvForeignKey.sizePkColumnValues();

            if (lvSizePkColumnValues == 0) {
                LOGGER.log(Level.SEVERE,
                        "No appropriate foreign key data existing yet, table="
                                + tableName + " foreign key="
                                + lvForeignKey.getFkName());
                return false;
            }

            final String[] lvRandomFkNames = lvForeignKey.getFkColumnNames();

            final Object[] lvRandomFkValues =
                    lvForeignKey.getPkColumnValues(random
                            .nextInt(lvSizePkColumnValues));

            for (int i = 0; i < randomFkNames.size(); i++) {
                for (int j = 0; j < lvRandomFkNames.length; j++) {

                    if (randomFkNames.get(i).equals(lvRandomFkNames[j])) {
                        LOGGER.log(Level.SEVERE,
                                "Several foreign keys including the same column is not yet suported, table="
                                        + tableName + " foreign key="
                                        + lvForeignKey.getFkName() + " column="
                                        + randomFkNames.get(i));
                        return false;
                    }
                }
            }

            for (int j = 0; j < lvRandomFkNames.length; j++) {
                randomFkNames.add(lvRandomFkNames[j]);
                randomFkValues.add(lvRandomFkValues[j]);
            }
        }

        return true;
    }

    /**
     * Determines a random primary key.
     * 
     * @param parDataGenerator The <code>DataGenerator</code> object.
     * 
     * @return <code>true</code> if the processing finished without errors.
     */
    public boolean determineRandomPrimaryKey(
            final DataGenerator parDataGenerator) {

        if (parDataGenerator == null) {
            throw new IllegalArgumentException("DataGenerator is missing");
        }

        return primaryKey.determineRandomPrimaryKey(parDataGenerator, columns);
    }

    /**
     * Returns the required <code>Column</code> object.
     * 
     * @param parColumnName The name of the required dstabase column.
     * 
     * @return the <code>Column</code> object or <code>null</code> if the
     *         required database column was not found.
     */
    public Column getColumn(final String parColumnName) {

        if (parColumnName == null || "".equals(parColumnName)) {
            throw new IllegalArgumentException("Column name is missing");
        }

        if (!columns.containsKey(parColumnName)) {
            throw new IllegalArgumentException("Column name unknown");
        }

        return columns.get(parColumnName);
    }

    /**
     * Returns the database column name related to a given ordinal position.
     * 
     * @param parOrdinalPosition The ordinal position of the column in the
     *            database table (starting with 1).
     * 
     * @return the database column name or <code>null</code> if the required
     *         ordinal position is out of range.
     */
    public String getColumnName(final int parOrdinalPosition) {

        if (parOrdinalPosition == 0) {
            throw new IllegalArgumentException("Index starts with 1");
        }

        if (parOrdinalPosition > columnNames.length) {
            throw new IllegalArgumentException("Index exceeds maximum value "
                    + columnNames.length);
        }

        return columnNames[parOrdinalPosition - 1];
    }

    /**
     * Returns the <code>ForeignKeys</code> object of this database table.
     * 
     * @return a <code>ForeignKeys</code> object containing the foreign keys.
     */
    public ForeignKeys getForeignKeys() {

        return foreignKeys;
    }

    /**
     * Returns the <code>PrimaryKey</code> object of this database table.
     * 
     * @return a <code>PrimaryKey</code> object containing the primary key.
     */
    public PrimaryKey getPrimaryKey() {

        return primaryKey;
    }

    /**
     * Returns a random foreign key.
     * 
     * @param parFkColumnName The name of the foreign key column.
     * 
     * @return a valid value for the given foreign key column.
     */
    public Object getRandomFkValue(final String parFkColumnName) {

        if (parFkColumnName == null || "".equals(parFkColumnName)) {
            throw new IllegalArgumentException(
                    "Foreign key column name is missing");
        }

        for (int i = 0; i < randomFkValues.size(); i++) {
            if (randomFkNames.get(i).equals(parFkColumnName)) {
                return randomFkValues.get(i);
            }
        }

        return null;
    }

    /**
     * Returns a random primary key.
     * 
     * @param parPkColumnName The name of the primary key column.
     * 
     * @return a valid value for the given primary key column.
     */
    public Object getRandomPkValue(final String parPkColumnName) {

        if (parPkColumnName == null || "".equals(parPkColumnName)) {
            throw new IllegalArgumentException(
                    "Primary key column name is missing");
        }

        return primaryKey.getRandomPkValue(parPkColumnName);
    }

    /**
     * Creates a protocol entry.
     * 
     * @param parTrialRunProtocol The <code>TrialRunProtocolMapper</code>
     *            object.
     * 
     */
    public void protocol(final TrialRunProtocolMapper parTrialRunProtocol) {

        if (parTrialRunProtocol == null) {
            throw new IllegalArgumentException(
                    "TrialRunProtocolMapper is missing");
        }

        primaryKey.protocol();

        foreignKeys.protocol(parTrialRunProtocol);

        final Set<String> lvSet = columns.keySet();
        final Iterator<String> lvIt = lvSet.iterator();

        while (lvIt.hasNext()) {
            final String lvColumnName = lvIt.next();

            final Column lvColumn = columns.get(lvColumnName);

            columnNames[lvColumn.getOrdPosition()] = lvColumnName;

            parTrialRunProtocol
                    .createProtocol("Database column meta data: column #"
                            + lvColumn.getOrdPosition() + ": name="
                            + lvColumnName + " data type="
                            + lvColumn.getDataType() + " size="
                            + lvColumn.getColumnSize() + " decimal factor="
                            + lvColumn.getDecimalFactor() + " decimal digits="
                            + lvColumn.getDecimalDigits() + " is nullable="
                            + lvColumn.getIsNullable() + " key sequence="
                            + lvColumn.getKeySeq());
        }
    }

    /**
     * Returns the number of columns in this database table.
     * 
     * @return the number of columns in this database table
     */
    public int sizeColumns() {

        return columns.size();
    }
}
