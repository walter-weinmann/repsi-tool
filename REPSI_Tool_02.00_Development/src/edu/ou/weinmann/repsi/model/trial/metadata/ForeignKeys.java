package edu.ou.weinmann.repsi.model.trial.metadata;

import edu.ou.weinmann.repsi.model.mapper.TrialRunProtocolMapper;

import edu.ou.weinmann.repsi.model.util.DatabaseAccessor;
import edu.ou.weinmann.repsi.model.util.Global;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the foreign keys of a database table.
 * 
 * @author Walter Weinmann
 * 
 */
public final class ForeignKeys {

    private static final Logger LOGGER =
            Logger.getLogger(ForeignKeys.class.getPackage().getName());

    private final Map<String, ForeignKey> foreignKeys;

    protected ForeignKeys(final DatabaseAccessor parDBAccess,
            final String parCatalog, final String parSchema,
            final String parTableName, final Map<String, Column> parColumns) {

        super();

        assert parDBAccess != null : "Precondition: DatabaseAccessor is missing (null)";
        assert parTableName != null : "Precondition: String table name is missing (null)";

        foreignKeys =
                determineForeignKeys(parDBAccess, parCatalog, parSchema,
                        parTableName);

        final Set<String> lvSet = foreignKeys.keySet();
        final Iterator<String> lvIt1 = lvSet.iterator();

        while (lvIt1.hasNext()) {
            final String lvFkName = lvIt1.next();

            foreignKeys.get(lvFkName).determineOrdPositions(parColumns);

            foreignKeys.get(lvFkName).determinePkColumnValues(parDBAccess);
        }
    }

    private Map<String, ForeignKey> determineForeignKeys(
            final DatabaseAccessor parDBAccess, final String parCatalog,
            final String parSchema, final String parTableName) {

        // Database connection *************************************************
        final Connection lvConnection = parDBAccess.getConnectionObject();

        assert lvConnection != null : "Invariant: Connection is null";

        // ResultSet with the foreign key column descriptions ******************
        ResultSet lvResultSet;

        try {
            lvResultSet =
                    lvConnection.getMetaData().getImportedKeys(parCatalog,
                            parSchema, parTableName);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "connection.getMetaData().getImportedKeys(" + parCatalog
                            + "," + parSchema + "," + parTableName + ")", e);
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

        // Process the foreign key columns *************************************
        final Map<String, ForeignKey> lvForeignKeys =
                new HashMap<String, ForeignKey>(
                        Global.INITIAL_CAPACITY_FOREIGN_KEYS);

        try {
            while (lvResultSet.next()) {
                final String lvFkName =
                        lvResultSet
                                .getString(Global.META_DATA_FOREIGN_KEY_NAME);

                if (lvForeignKeys.containsKey(lvFkName)) {
                    final ForeignKey lvForeignKey = lvForeignKeys.get(lvFkName);
                    lvForeignKey
                            .setColumnName(
                                    lvResultSet
                                            .getShort(Global.META_DATA_KEY_SEQUENCE) - 1,
                                    lvResultSet
                                            .getString(Global.META_DATA_FOREIGN_KEY_COLUMN_NAME),
                                    lvResultSet
                                            .getString(Global.META_DATA_PRIMARY_KEY_COLUMN_NAME));
                    lvForeignKeys.put(lvFkName, lvForeignKey);
                } else {
                    final ForeignKey lvForeignKey =
                            new ForeignKey(
                                    lvFkName,
                                    lvResultSet
                                            .getShort(Global.META_DATA_KEY_SEQUENCE),
                                    lvResultSet
                                            .getString(Global.META_DATA_FOREIGN_KEY_TABLE_NAME),
                                    lvResultSet
                                            .getString(Global.META_DATA_FOREIGN_KEY_COLUMN_NAME),
                                    lvResultSet
                                            .getString(Global.META_DATA_PRIMARY_KEY_TABLE_NAME),
                                    lvResultSet
                                            .getString(Global.META_DATA_PRIMARY_KEY_COLUMN_NAME));
                    lvForeignKeys.put(lvFkName, lvForeignKey);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "resultSetObject.next() / determineForeignKeys()", e);

            parDBAccess.closeResultSet(lvResultSet);

            return null;
        }

        if (!parDBAccess.closeResultSet(lvResultSet)) {
            return null;
        }

        // Loading ordinal positions and primary key values ********************

        return lvForeignKeys;
    }

    protected Map<String, ForeignKey> getForeignKeys() {

        return foreignKeys;
    }

    protected void protocol(final TrialRunProtocolMapper parTrialRunProtocol) {

        final Set<String> lvSet = foreignKeys.keySet();
        final Iterator<String> lvIt1 = lvSet.iterator();

        while (lvIt1.hasNext()) {
            final String lvFkName = lvIt1.next();

            parTrialRunProtocol
                    .createProtocol("Database column meta data: foreign key: name="
                            + lvFkName);

            final ForeignKey lvForeignKey = foreignKeys.get(lvFkName);

            final String[] lvFkColumnNames = lvForeignKey.getFkColumnNames();
            final int[] lvOrdPositions = lvForeignKey.getOrdPositions();
            final String[] lvPkColumnNames = lvForeignKey.getPkColumnNames();

            for (int i = 0; i < lvFkColumnNames.length; i++) {
                parTrialRunProtocol
                        .createProtocol("Database column meta data: foreign key: column #"
                                + (i + 1)
                                + " name="
                                + lvFkColumnNames[i]
                                + " primary table="
                                + lvForeignKey.getPkTableName()
                                + " column="
                                + lvPkColumnNames[i]
                                + " ord. pos="
                                + lvOrdPositions[i]);
            }
        }
    }
}
