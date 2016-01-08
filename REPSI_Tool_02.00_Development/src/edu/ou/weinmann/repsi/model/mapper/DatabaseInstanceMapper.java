package edu.ou.weinmann.repsi.model.mapper;

import edu.ou.weinmann.repsi.model.util.DatabaseAccessor;
import edu.ou.weinmann.repsi.model.util.Global;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Maps the data from the database instance to the database.
 * 
 * @author Walter Weinmann
 * 
 */
public class DatabaseInstanceMapper {

    private static final Logger LOGGER =
            Logger.getLogger(DatabaseInstanceMapper.class.getPackage()
                    .getName());

    private static final String PRECONDITION_DATABASE_ACCESSOR_IS_MISSING_NULL =
            "Precondition: DatabaseAccessor is missing (null)";

    private final DatabaseAccessor dbAccess;

    /**
     * Constructs a <code>DatabaseInstanceMapper</code> object.
     * 
     * @param parSQLSyntaxCodeTarget The type of the SQL syntax version of the
     *            database system.
     */
    public DatabaseInstanceMapper(final String parSQLSyntaxCodeTarget) {

        super();

        assert parSQLSyntaxCodeTarget != null : "Precondition: String SQL syntax code target is missing (null)";

        dbAccess =
                new DatabaseAccessor(Global.DATABASE_SCHEMA_IDENTIFIER_MASTER,
                        parSQLSyntaxCodeTarget, true);

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

    private Map<String, Object> getColumns(final String parStmnt) {

        if (!dbAccess.executeQuery(parStmnt)) {
            return null;
        }

        if (!dbAccess.next()) {
            return null;
        }

        return dbAccess.getColumns();
    }

    /**
     * Returns a <code>Map</code> containing the relevant columns of the
     * requested database instance.
     * 
     * @param parDatabaseInstanceId The identification of the database instance.
     * 
     * @return a <code>Map</code> containing the relevant columns of the
     *         database instance.
     */
    public final Map<String, Object> getDatabaseInstance(
            final int parDatabaseInstanceId) {

        final Map<String, Object> lvColumnsDatabaseInstance =
                getColumns("SELECT JDBC_DRIVER, JDBC_URL, "
                        + "DEI.DATABASE_SYSTEM_CODE AS DATABASE_SYSTEM_CODE, "
                        + "DEI.OPERATING_SYSTEM_CODE AS OPERATING_SYSTEM_CODE, "
                        + "DEI.PROCESSOR_CODE AS PROCESSOR_CODE, "
                        + "DES.NAME AS DATABASE_SYSTEM_NAME, "
                        + "OGS.NAME AS OPERATING_SYSTEM_NAME, "
                        + "PR.NAME AS PROCESSOR_NAME, "
                        + "VRDES.NAME AS DATABASE_SYSTEM_VENDOR_NAME, "
                        + "VROGS.NAME AS OPERATING_SYSTEM_VENDOR_NAME, "
                        + "VRPR.NAME AS PROCESSOR_VENDOR_NAME, "
                        + "PASSWORD, RAM_SIZE_MB, SCHEMA_NAME, "
                        + "SQL_SYNTAX_CODE, USER_NAME, "
                        + "DES.VERSION AS DATABASE_SYSTEM_VERSION, "
                        + "OGS.VERSION AS OPERATING_SYSTEM_VERSION "
                        + "FROM TMD_DATABASE_INSTANCE DEI, "
                        + "TMD_DATABASE_SYSTEM DES, "
                        + "TMD_OPERATING_SYSTEM OGS, TMD_PROCESSOR PR, "
                        + "TMD_VENDOR VRDES, TMD_VENDOR VROGS, "
                        + "TMD_VENDOR VRPR WHERE DATABASE_INSTANCE_ID = "
                        + parDatabaseInstanceId
                        + " AND DEI.DATABASE_SYSTEM_CODE = "
                        + "DES.DATABASE_SYSTEM_CODE "
                        + " AND DEI.OPERATING_SYSTEM_CODE = "
                        + "OGS.OPERATING_SYSTEM_CODE "
                        + " AND DEI.PROCESSOR_CODE = PR.PROCESSOR_CODE "
                        + "AND DES.VENDOR_CODE = VRDES.VENDOR_CODE "
                        + "AND OGS.VENDOR_CODE = VROGS.VENDOR_CODE "
                        + "AND PR.VENDOR_CODE = VRPR.VENDOR_CODE;");
        if (lvColumnsDatabaseInstance == null) {
            final String lvMsg =
                    "Database instance=" + parDatabaseInstanceId
                            + " is not available";
            LOGGER.log(Level.SEVERE, lvMsg);
        }

        return lvColumnsDatabaseInstance;
    }
}
