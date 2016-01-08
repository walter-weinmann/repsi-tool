package edu.ou.weinmann.repsi.model.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adapts the syntactical variations of different SQL versions by rewriting the
 * SQL statements.
 * 
 * @author Walter Weinmann
 * 
 */
public class SQLRewriter {

    private static final Logger LOGGER =
            Logger.getLogger(SQLRewriter.class.getPackage().getName());

    private String[] domainKeys;

    private Map<String, String> domains;

    private String lastErrorMsg;

    /**
     * Constructs a <code>SQLRewriter</code> object.
     */
    public SQLRewriter() {

        super();

        deleteStoredDomainDefinitions();
        lastErrorMsg = "";
    }

    /**
     * Deletes the stored domain definitions.
     */
    public final void deleteStoredDomainDefinitions() {

        domains = new HashMap<String, String>();
        domainKeys = null;
    }

    /**
     * Returns the latest error message.
     * 
     * @return the latest error message.
     */
    public final String getLastErrorMsg() {

        return lastErrorMsg;
    }

    /**
     * Rewrites a SQL statement from one syntactical version to another
     * syntactical version.
     * 
     * @param parSQLSyntaxCodeSource The SQL syntax version of input statement.
     * @param parSQLSyntaxCodeTarget The SQL syntax version of output statement.
     * @param parStmnt The SQL statement.
     * 
     * @return the SQL statement in the new syntax version.
     */
    public final String rewrite(final String parSQLSyntaxCodeSource,
            final String parSQLSyntaxCodeTarget, final String parStmnt) {

        lastErrorMsg = "";

        // Handling of empty records or comments.
        if ("".equals(parStmnt) || parStmnt.length() > 1
                && parStmnt.startsWith("--")) {
            return "";
        }

        // No translation required.
        if (parSQLSyntaxCodeTarget.equals(parSQLSyntaxCodeSource)) {
            return parStmnt;
        }

        if (parSQLSyntaxCodeSource.equals(Global.SQL_SYNTAX_CODE_SQL_99)
                && parSQLSyntaxCodeTarget
                        .equals(Global.SQL_SYNTAX_CODE_ORACLE_10G)) {
            return rewriteSQL99ToORACLE10G(trimAndToUpper(parStmnt, true));
        }

        if (!(parSQLSyntaxCodeSource.equals(Global.SQL_SYNTAX_CODE_SQL_99) || parSQLSyntaxCodeSource
                .equals(Global.SQL_SYNTAX_CODE_ORACLE_10G))) {
            lastErrorMsg =
                    "SQL syntax code of source database system is unknown";
        } else if (!(parSQLSyntaxCodeTarget
                .equals(Global.SQL_SYNTAX_CODE_SQL_99) || parSQLSyntaxCodeTarget
                .equals(Global.SQL_SYNTAX_CODE_ORACLE_10G))) {
            lastErrorMsg =
                    "SQL syntax code of target database system is unknown";
        } else {
            lastErrorMsg = "No appropriate translation method is implemented";
        }

        LOGGER.log(Level.SEVERE, lastErrorMsg);

        return "";
    }

    private String rewriteSQL99ToORACLE10G(final String parStmntInput) {

        final String[] lvTokens = parStmntInput.split(" ", 3);

        if (lvTokens.length == 3) {
            if (lvTokens[0].toUpperCase(Locale.getDefault()).equals("CREATE")) {
                if (lvTokens[1].toUpperCase(Locale.getDefault()).equals(
                        "DOMAIN")) {
                    return storeDomainDefinition(parStmntInput, lvTokens[2]);
                }

                if (lvTokens[1].toUpperCase(Locale.getDefault())
                        .equals("TABLE")) {
                    return shiftRoundNotNull(substituteDomainReferences(parStmntInput));
                }
            }

            if (lvTokens[0].toUpperCase(Locale.getDefault()).equals("DROP")
                    && (lvTokens[1].toUpperCase(Locale.getDefault()).equals(
                            "TABLE") || lvTokens[1].toUpperCase(
                            Locale.getDefault()).equals("VIEW"))) {
                return parStmntInput.replaceFirst("CASCADE",
                        "CASCADE CONSTRAINTS");
            }
        }

        return parStmntInput;
    }

    private String shiftRoundNotNull(final String parStmnt) {

        final String[] lvTokens1 = parStmnt.split(" NOT NULL ");

        if (lvTokens1.length == 1) {
            return parStmnt;
        }

        final StringBuffer lvOutSB = new StringBuffer(lvTokens1[0]);

        for (int i = 1; i < lvTokens1.length; i++) {
            final String[] lvTokens2 = lvTokens1[i].split(",", 2);

            if (lvTokens2.length == 1) {
                if (i == lvTokens1.length - 1) {
                    lvOutSB.append(" "
                            + lvTokens1[i].substring(0,
                                    lvTokens1[i].length() - 1) + " NOT NULL)");

                    continue;
                }

                lastErrorMsg =
                        "Syntax error with NOT NULL: statement=" + parStmnt;
                LOGGER.log(Level.SEVERE, lastErrorMsg);

                return parStmnt;
            }

            lvOutSB.append(" " + lvTokens2[0] + " NOT NULL," + lvTokens2[1]);
        }

        return lvOutSB.toString();
    }

    private String storeDomainDefinition(final String parStmntOriginal,
            final String parStmntLeftOver) {

        final String[] lvTokens = parStmntLeftOver.split(" AS ", 2);

        if (lvTokens.length != 2) {
            return parStmntOriginal;
        }

        if (domains.containsKey(lvTokens[0])) {
            if (domains.get(lvTokens[0]).equals(lvTokens[1])) {
                return "";
            }

            lastErrorMsg =
                    "Domain definition is not unique: " + parStmntOriginal;
            LOGGER.log(Level.SEVERE, lastErrorMsg);

            return parStmntOriginal;
        }

        domains.put(lvTokens[0], lvTokens[1]);

        domainKeys = null;

        return "";
    }

    private String substituteDomainReferences(final String parStmnt) {

        if (domainKeys == null) {
            domainKeys = domains.keySet().toArray(new String[domains.size()]);
        }

        String lvOut = parStmnt;

        for (int i = 0; i < domainKeys.length; i++) {
            lvOut = lvOut.replaceAll(domainKeys[i], domains.get(domainKeys[i]));
        }
        return lvOut;
    }

    private String trimAndToUpper(final String parStmnt,
            final boolean parDeleteTrailingSemicolon) {

        assert parStmnt != null : "Precondition: input statement is missing (null)";

        if ("".equals(parStmnt)) {
            return "";
        }

        // Remove trailing characters (; and whitspace)
        String lvStatementIn;

        final int lvLengthIn = parStmnt.trim().length();

        if (parDeleteTrailingSemicolon
                && parStmnt.trim().charAt(lvLengthIn - 1) == ';') {
            lvStatementIn = parStmnt.trim().substring(0, lvLengthIn - 1).trim();
        } else {
            lvStatementIn = parStmnt.trim().substring(0, lvLengthIn);
        }

        final StringBuffer lvBufferOut = new StringBuffer();

        boolean lvIsInsideComment = false;
        boolean lvIsLeadingCharacter = true;
        boolean lvIsPrevWhitespace = false;

        for (int i = 0; i < lvStatementIn.length(); i++) {

            // Processing comments.
            if (lvStatementIn.charAt(i) == '\'') {
                if (lvIsInsideComment) {
                    lvIsInsideComment = false;
                } else {
                    lvIsInsideComment = true;
                }
            }

            if (lvIsInsideComment) {
                lvBufferOut.append(lvStatementIn.charAt(i));
                lvIsLeadingCharacter = false;
                continue;
            }

            // Processing whitespace.
            if (Character.isWhitespace(lvStatementIn.charAt(i))) {
                if (lvIsLeadingCharacter || lvIsPrevWhitespace) {
                    continue;
                }

                lvIsPrevWhitespace = true;

                // Surpress whitespace before ;
                if (lvStatementIn.length() > i
                        && lvStatementIn.charAt(i + 1) == ';') {
                    continue;
                }
            } else {
                lvIsPrevWhitespace = false;
            }

            lvIsLeadingCharacter = false;

            lvBufferOut.append(Character.toUpperCase(lvStatementIn.charAt(i)));
        }

        return lvBufferOut.toString();
    }
}
