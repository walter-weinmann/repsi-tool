package edu.ou.weinmann.repsi.model.trial.metadata;

import edu.ou.weinmann.repsi.model.mapper.TrialRunProtocolMapper;

import edu.ou.weinmann.repsi.model.trial.util.DataGenerator;

import edu.ou.weinmann.repsi.model.util.DatabaseAccessor;
import edu.ou.weinmann.repsi.model.util.Global;

import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the primary key of a database table.
 * 
 * @author Walter Weinmann
 * 
 */
public final class PrimaryKey {

    private static final Logger LOGGER =
            Logger.getLogger(PrimaryKey.class.getPackage().getName());

    private final ArrayList<String> columnNames;

    private final Object[] highestValues;

    private int sizeColumns;

    private final TrialRunProtocolMapper trialRunProtocol;

    protected PrimaryKey(final TrialRunProtocolMapper parTrialRunProtocol,
            final DatabaseAccessor parDBAccess, final String parCatalog,
            final String parSchema, final String parTableName,
            final Map<String, Column> parColumns) {

        super();

        assert parDBAccess != null : "Precondition: DatabaseAccessor is missing (null)";
        assert parTableName != null : "Precondition: String table name is missing (null)";

        trialRunProtocol = parTrialRunProtocol;

        columnNames =
                determineColumnNames(parDBAccess, parCatalog, parSchema,
                        parTableName);

        highestValues = new Object[sizeColumns];

        determineHighestValue(parDBAccess, parTableName, parColumns);
    }

    private ArrayList<String> determineColumnNames(
            final DatabaseAccessor parDBAccess, final String parCatalog,
            final String parSchema, final String parTableName) {

        // Database connection *************************************************
        final Connection lvConnection = parDBAccess.getConnectionObject();

        assert lvConnection != null : "Invariant: Connection is null";

        // ResultSet with the primary key column descriptions ******************
        ResultSet lvResultSetPrimaryKeys;

        try {
            lvResultSetPrimaryKeys =
                    lvConnection.getMetaData().getPrimaryKeys(parCatalog,
                            parSchema, parTableName);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "connection.getMetaData().getPrimaryKeys("
                    + parCatalog + "," + parSchema + "," + parTableName + ")",
                    e);
            return null;
        }

        assert lvResultSetPrimaryKeys != null : "Invariant: ResultSet (connection.getMetaData().xxx) is null";

        // Process the primary key columns *************************************
        final ArrayList<String> lvColumns =
                new ArrayList<String>(Global.INITIAL_CAPACITY_PRIMARY_KEYS);

        try {
            while (lvResultSetPrimaryKeys.next()) {

                final int lvKeySeq =
                        lvResultSetPrimaryKeys
                                .getShort(Global.META_DATA_KEY_SEQUENCE);

                while (lvColumns.size() < lvKeySeq) {
                    lvColumns.add("");
                }

                lvColumns.set(lvKeySeq - 1, lvResultSetPrimaryKeys
                        .getString(Global.META_DATA_COLUMN_NAME));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "resultSetObject.next() / determineColumnNames()", e);

            parDBAccess.closeResultSet(lvResultSetPrimaryKeys);

            return null;
        }

        if (!parDBAccess.closeResultSet(lvResultSetPrimaryKeys)) {
            return null;
        }

        sizeColumns = lvColumns.size();

        return lvColumns;
    }

    private boolean determineHighestValue(final DatabaseAccessor parDBAccess,
            final String parTableName, final Map<String, Column> parColumns) {

        final StringBuffer lvSelect = new StringBuffer();
        final StringBuffer lvOrderBy = new StringBuffer();

        for (int i = 0; i < sizeColumns; i++) {
            final String lvColumnName = columnNames.get(i);

            if (i != 0) {
                lvSelect.append(", ");
                lvOrderBy.append(", ");
            }

            lvSelect.append(lvColumnName);
            lvOrderBy.append(lvColumnName).append(" DESC");
        }

        // No primary key defined **********************************************
        if (lvSelect.length() == 0) {
            return true;
        }

        if (!parDBAccess.executeQuery("SELECT " + lvSelect.toString()
                + " FROM " + parTableName + " ORDER BY " + lvOrderBy.toString()
                + ";")) {
            return false;
        }

        // No data existing ****************************************************
        if (!parDBAccess.next()) {
            initialiseHighestValues(parColumns);
            return true;
        }

        for (int i = 0; i < sizeColumns; i++) {
            highestValues[i] = parDBAccess.getColumn(i + 1);
        }

        return true;
    }

    protected boolean determineRandomPrimaryKey(
            final DataGenerator parDataGenerator,
            final Map<String, Column> parColumns) {

        boolean lvIsNew = false;

        for (int i = sizeColumns - 1; i >= 0; i--) {
            final Column lvColumn = parColumns.get(getColumnNames()[i]);

            if (lvColumn.isForeignKeyColumn()) {
                lvIsNew = true;
                continue;
            }

            switch (lvColumn.getDataType()) {
                case java.sql.Types.CHAR:
                case java.sql.Types.VARCHAR:

                    lvIsNew =
                            determineRandomPrimaryKeyVarchar(parDataGenerator,
                                    lvColumn, lvColumn.getKeySeq() - 1, lvIsNew);
                    if (trialRunProtocol.isAborted()) {
                        return false;
                    }

                    break;
                case java.sql.Types.DATE:

                    lvIsNew =
                            determineRandomPrimaryKeyDate(parDataGenerator,
                                    lvColumn, lvColumn.getKeySeq() - 1, lvIsNew);
                    if (trialRunProtocol.isAborted()) {
                        return false;
                    }

                    break;
                case java.sql.Types.DECIMAL:
                case java.sql.Types.NUMERIC:

                    lvIsNew =
                            determineRandomPrimaryKeyNumeric(parDataGenerator,
                                    lvColumn, lvColumn.getKeySeq() - 1, lvIsNew);
                    if (trialRunProtocol.isAborted()) {
                        return false;
                    }

                    break;
                default:
                    trialRunProtocol.createErrorProtocol("SQL type="
                            + lvColumn.getDataType()
                            + Global.ERROR_NOT_YET_IMPLEMENTED, true);
                    return false;
            }
        }

        if (!lvIsNew) {
            trialRunProtocol.createErrorProtocol(
                    "Range of primary key exceeded", true);
            return false;
        }

        return true;
    }

    private boolean determineRandomPrimaryKeyDate(
            final DataGenerator parDataGenerator, final Column parColumn,
            final int parKeySeq, final boolean parIsNew) {

        boolean lvIsNew = parIsNew;

        Date lvValueDate;

        if (lvIsNew) {
            lvValueDate = (Date) getRandomPkValue(parColumn.getColumnName());
        } else {
            lvValueDate =
                    parDataGenerator.generateColumnValueDateKey(parColumn);
            if (lvValueDate == null) {
                lvValueDate = DataGenerator.generateColumnValueDateKeyLow();
            } else {
                lvIsNew = true;
            }
        }

        highestValues[parKeySeq] = lvValueDate;

        return lvIsNew;
    }

    private boolean determineRandomPrimaryKeyNumeric(
            final DataGenerator parDataGenerator, final Column parColumn,
            final int parKeySeq, final boolean parIsNew) {

        boolean lvIsNew = parIsNew;

        BigDecimal lvValueNumeric;

        if (lvIsNew) {
            lvValueNumeric =
                    (BigDecimal) getRandomPkValue(parColumn.getColumnName());
        } else {
            lvValueNumeric =
                    parDataGenerator.generateColumnValueNumericKey(parColumn);
            if (lvValueNumeric == null) {
                lvValueNumeric =
                        DataGenerator.generateColumnValueNumericKeyLow();
            } else {
                lvIsNew = true;
            }
        }

        highestValues[parKeySeq] = lvValueNumeric;

        return lvIsNew;
    }

    private boolean determineRandomPrimaryKeyVarchar(
            final DataGenerator parDataGenerator, final Column parColumn,
            final int parKeySeq, final boolean parIsNew) {

        boolean lvIsNew = parIsNew;

        String lvValueVarchar;

        if (lvIsNew) {
            lvValueVarchar =
                    (String) getRandomPkValue(parColumn.getColumnName());
        } else {
            lvValueVarchar =
                    parDataGenerator.generateColumnValueVarcharKey(parColumn);
            if ("".equals(lvValueVarchar)) {
                lvValueVarchar =
                        DataGenerator
                                .generateColumnValueVarcharKeyLow(parColumn);
            } else {
                lvIsNew = true;
            }
        }

        highestValues[parKeySeq] = lvValueVarchar;

        return lvIsNew;
    }

    protected String[] getColumnNames() {

        return columnNames.toArray(new String[sizeColumns]);
    }

    /**
     * Returns a random primary key column.
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

        for (int i = 0; i < highestValues.length; i++) {
            if (columnNames.get(i).equals(parPkColumnName)) {
                return highestValues[i];
            }
        }

        return null;
    }

    private void initialiseHighestValues(final Map<String, Column> parColumns) {

        for (int i = 0; i < sizeColumns; i++) {
            final Column lvColumn = parColumns.get(columnNames.get(i));

            switch (lvColumn.getDataType()) {
                case java.sql.Types.CHAR:
                case java.sql.Types.VARCHAR:

                    highestValues[i] =
                            DataGenerator.generateColumnValueVarcharKeyLow();

                    break;
                case java.sql.Types.DATE:

                    highestValues[i] =
                            DataGenerator.generateColumnValueDateKeyLow();

                    break;
                case java.sql.Types.DECIMAL:
                case java.sql.Types.NUMERIC:

                    highestValues[i] =
                            DataGenerator.generateColumnValueNumericKeyLow();

                    break;
                default:
                    trialRunProtocol.createErrorProtocol("SQL type="
                            + lvColumn.getDataType()
                            + Global.ERROR_NOT_YET_IMPLEMENTED, true);
                    return;
            }
        }
    }

    protected void protocol() {

        String lvLastValue = "";

        for (int i = 0; i < sizeColumns; i++) {
            if (i < highestValues.length) {
                lvLastValue = highestValues[i].toString();
            }

            trialRunProtocol
                    .createProtocol("Database column meta data: primary key: column #"
                            + (i + 1)
                            + " name="
                            + columnNames.get(i)
                            + " last value=" + lvLastValue);
        }
    }
}
