package edu.ou.weinmann.repsi.model.mapper;

import edu.ou.weinmann.repsi.model.util.DatabaseAccessor;
import edu.ou.weinmann.repsi.model.util.Global;

import java.text.SimpleDateFormat;

import java.util.Date;

/**
 * Maps the data from the trial run protocol to the database.
 * 
 * @author Walter Weinmann
 * 
 */
public class TrialRunProtocolMapper {

    private boolean aborted;

    private final int databaseInstanceId;

    private final DatabaseAccessor dbAccess;

    private int numberOfAborts;

    private int numberOfErrors;

    private long sequenceNumberAction;

    private long sequenceNumberProtocol;

    private final String startTime;

    private final int testSuiteId;

    /**
     * Constructs a <code>TrialRunProtocolMapper</code> object.
     * 
     * @param parSQLSyntaxCodeTarget The type of the SQL syntax version of the
     *            database system.
     * @param parDatabaseInstanceId The identification of the
     *            <code>DatabaseInstance</code> object.
     * @param parTestSuiteId The identification of the <code>TestSuite</code>
     *            object.
     * @param parStartTime The current time stamp.
     */
    public TrialRunProtocolMapper(final String parSQLSyntaxCodeTarget,
            final int parDatabaseInstanceId, final int parTestSuiteId,
            final String parStartTime) {

        super();

        assert parSQLSyntaxCodeTarget != null : "Precondition: String SQL syntax code target is missing (null)";
        assert parStartTime != null : "Precondition: String start time is missing (null)";

        dbAccess =
                new DatabaseAccessor(Global.DATABASE_SCHEMA_IDENTIFIER_MASTER,
                        parSQLSyntaxCodeTarget, true);

        databaseInstanceId = parDatabaseInstanceId;
        aborted = false;
        sequenceNumberAction = 0L;
        sequenceNumberProtocol = 0L;
        numberOfErrors = 0;
        startTime = parStartTime;
        testSuiteId = parTestSuiteId;
    }

    /**
     * Close the database connection.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean closeConnection() {

        assert dbAccess != null : "Precondition: DatabaseAccessor is missing (null)";

        return dbAccess.closeConnection();
    }

    /**
     * Create an error protocol entry.
     * 
     * @param parMessage The error message to be contained in the protocol.
     * @param parAborted Whether the processing should be termintated
     *            immedeately.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean createErrorProtocol(final String parMessage,
            final boolean parAborted) {

        if (parMessage == null) {
            throw new IllegalArgumentException("Message is missing (null)");
        }

        if ("".equals(parMessage)) {
            throw new IllegalArgumentException("Message is missing (empty)");
        }

        String lvErrorType;

        if (parAborted) {
            numberOfAborts++;
            lvErrorType = "Abort: ";
            aborted = true;
        } else {
            numberOfErrors++;
            lvErrorType = "Error: ";
        }

        return createProtocol(lvErrorType + parMessage);
    }

    /**
     * Creates an protocol entry.
     * 
     * @param parMessage The error message to be contained in the protocol.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean createProtocol(final String parMessage) {

        if (parMessage == null) {
            throw new IllegalArgumentException("Message is missing (null)");
        }

        if ("".equals(parMessage)) {
            throw new IllegalArgumentException("Message is missing (empty)");
        }

        assert dbAccess != null : "Precondition: DatabaseAccessor is missing (null)";

        sequenceNumberProtocol++;

        if (!dbAccess.executeUpdate(("INSERT INTO TMD_TRIAL_RUN_PROTOCOL "
                + "(DATABASE_INSTANCE_ID, TEST_SUITE_ID, START_TIME, "
                + "SEQUENCE_NUMBER_PROTOCOL, CREATED, MESSAGE, "
                + "SEQUENCE_NUMBER_ACTION) VALUES ("
                + databaseInstanceId
                + ", "
                + testSuiteId
                + ", "
                + startTime
                + ", "
                + sequenceNumberProtocol
                + ", CAST(TO_TIMESTAMP('"
                + new SimpleDateFormat(
                        Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_JAVA)
                        .format(new Date()) + "', '"
                + Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_SQL
                + "') AS TIMESTAMP(9)), '" + parMessage.replaceAll("'", "''")
                + "', " + sequenceNumberAction + ")").replaceAll("'null'",
                Global.NULL))) {
            return false;
        }

        return dbAccess.commit();
    }

    /**
     * Creates an protocol entry.
     * 
     * @param parMessagePart1 The left part of the information to be contained
     *            in the protocol.
     * @param parMessagePart2 The right part of the information to be contained
     *            in the protocol.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean createProtocol(final String parMessagePart1,
            final long parMessagePart2) {

        assert dbAccess != null : "Precondition: DatabaseAccessor is missing (null)";

        return createProtocol(parMessagePart1, Long.toString(parMessagePart2));
    }

    /**
     * Creates an protocol entry.
     * 
     * @param parMessagePart1 The left part of the information to be contained
     *            in the protocol.
     * @param parMessagePart2 The right part of the information to be contained
     *            in the protocol.
     * 
     * @return <code>true</code> if the operation succeeeded and
     *         <code>false</code> otherwise.
     */
    public final boolean createProtocol(final String parMessagePart1,
            final String parMessagePart2) {

        if (parMessagePart1 == null) {
            throw new IllegalArgumentException(
                    "Message part 1 is missing (null)");
        }

        if ("".equals(parMessagePart1)) {
            throw new IllegalArgumentException(
                    "Message part 1 is missing (empty)");
        }

        if (parMessagePart2 == null) {
            throw new IllegalArgumentException(
                    "Message part 2 is missing (null)");
        }

        final StringBuffer lvMsg = new StringBuffer(parMessagePart1);

        while (lvMsg.length() < 30) {
            lvMsg.append(' ');
        }

        if (!("".equals(parMessagePart2))) {
            lvMsg.append('=').append(parMessagePart2);
        }

        return createProtocol(lvMsg.toString());
    }

    /**
     * Returns the current number of aborts reported.
     * 
     * @return the current number of aborts reported.
     */
    public final int getNumberOfAborts() {

        return numberOfAborts;
    }

    /**
     * Returns the current number of errors reported.
     * 
     * @return the current number of errors reported.
     */
    public final int getNumberOfErrors() {

        return numberOfErrors;
    }

    /**
     * Returns whether the processing was required to be terminated immedeately.
     * 
     * @return <code>true</code> if the processing was required to be
     *         terminated immedeately.
     */
    public final boolean isAborted() {

        return aborted;
    }

    /**
     * Sets the current action sequence number.
     * 
     * @param parSequenceNumberAction The current action sequence number.
     */
    public final void setSequenceNumberAction(final long parSequenceNumberAction) {

        sequenceNumberAction = parSequenceNumberAction;
    }
}
