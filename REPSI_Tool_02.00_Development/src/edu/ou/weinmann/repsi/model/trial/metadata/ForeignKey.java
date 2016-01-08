package edu.ou.weinmann.repsi.model.trial.metadata;

import edu.ou.weinmann.repsi.model.util.DatabaseAccessor;
import edu.ou.weinmann.repsi.model.util.Global;

import java.util.ArrayList;
import java.util.Map;

/**
 * Manages one foreign key of a database table.
 * 
 * @author Walter Weinmann
 * 
 */
public final class ForeignKey {

    private static final int FOREIGN_KEY_VALUES_INITIAL_SIZE_100 = 100;

    private static final int FOREIGN_KEY_VALUES_MAX_SIZE_1000 = 1000;

    private final ArrayList<String> fkColumnNames;

    private final String fkName;

    private final String fkTableName;

    private int[] ordPositions;

    private final ArrayList<String> pkColumnNames;

    private ArrayList<ArrayList<Object>> pkColumnValues;

    private final String pkTableName;

    protected ForeignKey(final String parFkName, final int parKeySeq,
            final String parFkTableName, final String parFkColumnName,
            final String parPkTableName, final String parPkColumnName) {

        super();

        assert parFkName != null : "Precondition: String foreign key name is missing (null)";
        assert parKeySeq != 0 : "Precondition: String key sequence number is missing (0)";
        assert parFkTableName != null : "Precondition: String foreign table name is missing (null)";
        assert parFkColumnName != null : "Precondition: String foreign key column name is missing (null)";
        assert parPkTableName != null : "Precondition: String primary table name is missing (null)";
        assert parPkColumnName != null : "Precondition: String primary key column name is missing (null)";

        fkName = parFkName;

        fkColumnNames =
                new ArrayList<String>(Global.INITIAL_CAPACITY_FOREIGN_KEYS);
        pkColumnNames =
                new ArrayList<String>(Global.INITIAL_CAPACITY_PRIMARY_KEYS);

        setColumnName(parKeySeq - 1, parFkColumnName, parPkColumnName);

        fkTableName = parFkTableName;
        pkTableName = parPkTableName;
    }

    protected void determineOrdPositions(final Map<String, Column> parColumns) {

        ordPositions = new int[pkColumnNames.size()];

        for (int i = 0; i < pkColumnNames.size(); i++) {

            ordPositions[i] =
                    parColumns.get(fkColumnNames.get(i)).getOrdPosition();

            parColumns.get(fkColumnNames.get(i)).setForeignKeyColumn(true);
        }
    }

    protected boolean determinePkColumnValues(final DatabaseAccessor parDBAccess) {

        pkColumnValues =
                new ArrayList<ArrayList<Object>>(
                        FOREIGN_KEY_VALUES_INITIAL_SIZE_100);

        final StringBuffer lvSelect = new StringBuffer();
        final StringBuffer lvOrderBy = new StringBuffer();

        for (int i = 0; i < pkColumnNames.size(); i++) {
            final String lvColumnName = pkColumnNames.get(i);

            if (i != 0) {
                lvSelect.append(", ");
                lvOrderBy.append(", ");
            }

            lvSelect.append(lvColumnName);
            lvOrderBy.append(lvColumnName);
        }

        // No primary key defined **********************************************
        if (lvSelect.length() == 0) {
            pkColumnValues.trimToSize();
            return true;
        }

        if (!parDBAccess.executeQuery("SELECT " + lvSelect.toString()
                + " FROM " + pkTableName + " ORDER BY " + lvOrderBy.toString()
                + ";")) {
            return false;
        }

        int lvColumnCount = 0;

        while (parDBAccess.next()
                && lvColumnCount < FOREIGN_KEY_VALUES_MAX_SIZE_1000) {

            final ArrayList<Object> lvColumnValue =
                    new ArrayList<Object>(Global.INITIAL_CAPACITY_FOREIGN_KEYS);

            for (int i = 0; i < pkColumnNames.size(); i++) {
                lvColumnValue.add(parDBAccess.getColumn(i + 1));
            }

            pkColumnValues.add(lvColumnValue);

            lvColumnCount++;
        }

        pkColumnValues.trimToSize();

        return true;
    }

    protected String[] getFkColumnNames() {

        return fkColumnNames.toArray(new String[fkColumnNames.size()]);
    }

    protected String getFkName() {

        return fkName;
    }

    protected String getFkTableName() {

        return fkTableName;
    }

    protected Object getHighestValue(final int parOrdPosition) {

        for (int i = 0; i < pkColumnValues.size(); i++) {
            if (ordPositions[i] == parOrdPosition) {
                return pkColumnValues.get(i);
            }
        }

        return null;
    }

    protected int[] getOrdPositions() {

        return ordPositions;
    }

    protected String[] getPkColumnNames() {

        return pkColumnNames.toArray(new String[pkColumnNames.size()]);
    }

    protected Object[] getPkColumnValues(final int parIndex) {

        return pkColumnValues.get(parIndex).toArray(
                new Object[pkColumnValues.get(parIndex).size()]);
    }

    protected String getPkTableName() {

        return pkTableName;
    }

    protected void setColumnName(final int parIndex,
            final String parFkColumnName, final String parPkColumnName) {

        fkColumnNames.add(parIndex, parFkColumnName);
        pkColumnNames.add(parIndex, parPkColumnName);
    }

    protected int sizePkColumnValues() {

        return pkColumnValues.size();
    }
}
