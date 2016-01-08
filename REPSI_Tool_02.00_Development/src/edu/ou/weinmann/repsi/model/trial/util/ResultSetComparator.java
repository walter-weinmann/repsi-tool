package edu.ou.weinmann.repsi.model.trial.util;

import edu.ou.weinmann.repsi.model.util.DatabaseAccessor;
import edu.ou.weinmann.repsi.model.util.Global;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Compares two <code>ResultSet</code> objects for equality.
 * 
 * @author Walter Weinmann
 * 
 */
public class ResultSetComparator {

    private static final String COLUMN = " column=";

    private static final String IS_DIFFERENT_RESULT_SET =
            "' is different, ResultSet '";

    private static final String RESULT_SET = " - ResultSet '";

    private static final String SQLEXCEPTION = ", SQLException=";

    private static final String RESULT_SET_COMPARATOR_COMPARE_RESULT_SET_RESULT_SET =
            "ResultSetComparator - compareResultSet(): ResultSet '";

    private static final String RESULT_SET_COMPARATOR_COMPARE_RESULT_SET_ROW =
            "ResultSetComparator - compareResultSet(): row=";

    private String[] description;

    private String lastErrorMsg;

    private int numberColumns;

    private String[] orderBy;

    private ResultSetMetaData parResultSetMetaData;

    private String[] selectStmnt;

    private String sqlSyntaxCode;

    /**
     * Constructs a <code>ResultSetComparator</code> object.
     */
    public ResultSetComparator() {

        super();

        lastErrorMsg = "";

        description = new String[] { "Not applied", "Applied" };
        orderBy = new String[] { "", "" };
        selectStmnt = new String[] { "", "" };
        sqlSyntaxCode = "";
    }

    /**
     * Compares two <code>ResultSet</code> objects.
     * 
     * @param parDBAccess The <code>DatabaseAccessor</code> object.
     * @return <code>true</code> if both <code>ResultSet</code>s are equal,
     *         or <code>false</code> otherwise.
     */
    public final boolean compare(final DatabaseAccessor[] parDBAccess) {

        if (parDBAccess.length != 2) {
            lastErrorMsg =
                    "Comparison not possible, number of DatabaseAccessor objects needed is 2, not "
                            + parDBAccess.length;
            return false;
        }

        if (parDBAccess[0] == null) {
            lastErrorMsg =
                    "Comparison not possible, DatabaseAccessor '"
                            + description[0] + "' is missing";
            return false;
        }

        if (parDBAccess[1] == null) {
            lastErrorMsg =
                    "Comparison not possible, DatabaseAccessor '"
                            + description[1] + "' is missing";
            return false;
        }

        final ResultSet[] lvResultSet = new ResultSet[2];

        if ("".equals(selectStmnt[0]) && selectStmnt[1] == null) {
            lastErrorMsg =
                    "Comparison not possible, no SELECT statement available";
            return false;
        }

        if (selectStmnt[0] == null) {
            lastErrorMsg =
                    "Comparison not possible, SELECT statement '"
                            + description[0] + "' missing";
            return false;
        }

        if (!parDBAccess[0].executeQuery(
                concatenate(selectStmnt[0], orderBy[0]), sqlSyntaxCode)) {
            lastErrorMsg = parDBAccess[0].getTrialRunErrorMessage();
            return false;
        }

        lvResultSet[0] = parDBAccess[0].getResultSetObject();

        if (selectStmnt[1] == null) {
            lastErrorMsg =
                    "Comparison not possible, SELECT statement '"
                            + description[1] + "' missing";
            return false;
        }

        if (!parDBAccess[1].executeQuery(
                concatenate(selectStmnt[1], orderBy[1]), sqlSyntaxCode)) {
            lastErrorMsg = parDBAccess[1].getTrialRunErrorMessage();
            return false;
        }

        lvResultSet[1] = parDBAccess[1].getResultSetObject();

        if (!compareResultSetMetaData(lvResultSet)) {
            return false;
        }

        return compareResultSet(lvResultSet);
    }

    private boolean compareResultSet(final ResultSet[] parResultSet) {

        int lvCurrentRow = 0;

        try {
            while (parResultSet[0].next()) {

                lvCurrentRow++;

                if (!(parResultSet[1].next())) {
                    lastErrorMsg =
                            RESULT_SET_COMPARATOR_COMPARE_RESULT_SET_RESULT_SET
                                    + description[0]
                                    + "' has more rows than ResultSet '"
                                    + description[1] + "' (" + lvCurrentRow
                                    + " row(s))";
                    return false;
                }

                for (int i = 1; i <= numberColumns; i++) {

                    switch (parResultSetMetaData.getColumnType(i)) {
                        case java.sql.Types.CHAR:
                        case java.sql.Types.VARCHAR:

                            final String lvString0 =
                                    parResultSet[0].getString(i);
                            final String lvString1 =
                                    parResultSet[1].getString(i);

                            if (lvString0 == null) {
                                if (lvString1 == null) {
                                    break;
                                }
                            } else {
                                if (lvString1 != null) {
                                    if (lvString0.equals(parResultSet[1]
                                            .getString(i))) {
                                        break;
                                    }
                                }
                            }

                            lastErrorMsg =
                                    RESULT_SET_COMPARATOR_COMPARE_RESULT_SET_ROW
                                            + lvCurrentRow
                                            + COLUMN
                                            + i
                                            + " : different values in ResultSet '"
                                            + description[1] + "'="
                                            + parResultSet[0].getString(i)
                                            + " and in ResultSet '"
                                            + description[0] + "'="
                                            + parResultSet[1].getString(i);

                            return false;

                        case java.sql.Types.DATE:

                            final Date lvDate0 = parResultSet[0].getDate(i);
                            final Date lvDate1 = parResultSet[1].getDate(i);

                            if (lvDate0 == null) {
                                if (lvDate1 == null) {
                                    break;
                                }
                            } else {
                                if (lvDate1 != null) {
                                    if (parResultSet[0].getDate(i).compareTo(
                                            parResultSet[1].getDate(i)) == 0) {
                                        break;
                                    }
                                }
                            }

                            lastErrorMsg =
                                    RESULT_SET_COMPARATOR_COMPARE_RESULT_SET_ROW
                                            + lvCurrentRow
                                            + COLUMN
                                            + i
                                            + " : different values in ResultSet '"
                                            + description[1]
                                            + "'="
                                            + parResultSet[0].getDate(i)
                                                    .toString()
                                            + " and in ResultSet '"
                                            + description[0]
                                            + "'="
                                            + parResultSet[1].getDate(i)
                                                    .toString();

                            return false;

                        case java.sql.Types.DECIMAL:
                        case java.sql.Types.NUMERIC:

                            final BigDecimal lvNumeric0 =
                                    parResultSet[0].getBigDecimal(i);
                            final BigDecimal lvNumeric1 =
                                    parResultSet[1].getBigDecimal(i);

                            if (lvNumeric0 == null) {
                                if (lvNumeric1 == null) {
                                    break;
                                }
                            } else {
                                if (lvNumeric1 != null) {
                                    if (parResultSet[0].getBigDecimal(i)
                                            .compareTo(
                                                    parResultSet[1]
                                                            .getBigDecimal(i)) == 0) {
                                        break;
                                    }
                                }
                            }

                            lastErrorMsg =
                                    RESULT_SET_COMPARATOR_COMPARE_RESULT_SET_ROW
                                            + lvCurrentRow
                                            + COLUMN
                                            + i
                                            + " : different values in ResultSet '"
                                            + description[1]
                                            + "'="
                                            + parResultSet[0].getBigDecimal(i)
                                                    .toString()
                                            + " and in ResultSet '"
                                            + description[0]
                                            + "'="
                                            + parResultSet[1].getBigDecimal(i)
                                                    .toString();

                            return false;
                        default:
                            lastErrorMsg =
                                    RESULT_SET_COMPARATOR_COMPARE_RESULT_SET_ROW
                                            + lvCurrentRow
                                            + COLUMN
                                            + i
                                            + " - SQL type="
                                            + parResultSetMetaData
                                                    .getColumnType(i)
                                            + Global.ERROR_NOT_YET_IMPLEMENTED;
                            return false;
                    }
                }
            }

        } catch (SQLException e) {
            lastErrorMsg =
                    RESULT_SET_COMPARATOR_COMPARE_RESULT_SET_RESULT_SET
                            + description[0] + SQLEXCEPTION + e + " ("
                            + e.getMessage() + ")";
            return false;
        }

        try {
            if (parResultSet[1].next()) {
                lastErrorMsg =
                        RESULT_SET_COMPARATOR_COMPARE_RESULT_SET_RESULT_SET
                                + description[1]
                                + "' has more rows than ResultSet '"
                                + description[0] + "' (" + lvCurrentRow
                                + " row(s))";
                return false;
            }
        } catch (SQLException e) {
            lastErrorMsg =
                    RESULT_SET_COMPARATOR_COMPARE_RESULT_SET_RESULT_SET
                            + description[0] + SQLEXCEPTION + e + " ("
                            + e.getMessage() + ")";
            return false;
        }

        return true;
    }

    private boolean compareResultSetMetaData(final ResultSet[] parResultSet) {

        try {
            parResultSetMetaData = parResultSet[0].getMetaData();
        } catch (SQLException e) {
            lastErrorMsg =
                    "ResultSetComparator - compareResultSetMetaData(): ResultSet '"
                            + description[0] + SQLEXCEPTION + e + " ("
                            + e.getMessage() + ")";
            return false;
        }

        ResultSetMetaData lvMetaData2;

        try {
            lvMetaData2 = parResultSet[1].getMetaData();

            numberColumns = parResultSetMetaData.getColumnCount();

            if (!(numberColumns == lvMetaData2.getColumnCount())) {
                lastErrorMsg =
                        "ResultSetComparator - compareResultSetMetaData(): Number of columns is different, ResultSet '"
                                + description[0]
                                + "' "
                                + numberColumns
                                + " column(s) - ResultSet '"
                                + description[1]
                                + "' "
                                + lvMetaData2.getColumnCount()
                                + " column(s)";
                return false;
            }

            for (int i = 1; i <= numberColumns; i++) {

                if (!(parResultSetMetaData.getColumnClassName(i)
                        .equals(lvMetaData2.getColumnClassName(i)))) {
                    lastErrorMsg =
                            "ResultSetComparator - compareResultSetMetaData(): Java class of column "
                                    + i
                                    + IS_DIFFERENT_RESULT_SET
                                    + description[0]
                                    + "' "
                                    + parResultSetMetaData
                                            .getColumnClassName(i) + RESULT_SET
                                    + description[1] + "' "
                                    + lvMetaData2.getColumnClassName(i);
                    return false;
                }

                if (!((parResultSetMetaData.getColumnTypeName(i).equals(
                        Global.SQL_COLUMN_TYPE_CHAR) || parResultSetMetaData
                        .getColumnTypeName(i).equals(
                                Global.SQL_COLUMN_TYPE_VARCHAR2)) && (lvMetaData2
                        .getColumnTypeName(i).equals(
                                Global.SQL_COLUMN_TYPE_CHAR) || lvMetaData2
                        .getColumnTypeName(i).equals(
                                Global.SQL_COLUMN_TYPE_VARCHAR2)))) {
                    if (!(parResultSetMetaData.getColumnTypeName(i)
                            .equals(lvMetaData2.getColumnTypeName(i)))) {
                        lastErrorMsg =
                                "ResultSetComparator - compareResultSetMetaData(): database type of column "
                                        + i
                                        + IS_DIFFERENT_RESULT_SET
                                        + description[0]
                                        + "' "
                                        + parResultSetMetaData
                                                .getColumnTypeName(i)
                                        + RESULT_SET + description[1] + "' "
                                        + lvMetaData2.getColumnTypeName(i);
                        return false;
                    }
                }

                if (!(parResultSetMetaData.getColumnDisplaySize(i) == lvMetaData2
                        .getColumnDisplaySize(i))) {
                    lastErrorMsg =
                            "ResultSetComparator - compareResultSetMetaData(): maximum width of column "
                                    + i
                                    + IS_DIFFERENT_RESULT_SET
                                    + description[0]
                                    + "' "
                                    + parResultSetMetaData
                                            .getColumnDisplaySize(i)
                                    + RESULT_SET + description[1] + "' "
                                    + lvMetaData2.getColumnDisplaySize(i);
                    return false;
                }

                if (!(parResultSetMetaData.getPrecision(i) == lvMetaData2
                        .getPrecision(i))) {
                    lastErrorMsg =
                            "ResultSetComparator - compareResultSetMetaData(): precision of column "
                                    + i + IS_DIFFERENT_RESULT_SET
                                    + description[0] + "' "
                                    + parResultSetMetaData.getPrecision(i)
                                    + RESULT_SET + description[1] + "' "
                                    + lvMetaData2.getPrecision(i);
                    return false;
                }

                if (!(parResultSetMetaData.isSigned(i) == lvMetaData2
                        .isSigned(i))) {
                    lastErrorMsg =
                            "ResultSetComparator - compareResultSetMetaData(): signed numbers of column "
                                    + i + IS_DIFFERENT_RESULT_SET
                                    + description[0] + "' "
                                    + parResultSetMetaData.isSigned(i)
                                    + RESULT_SET + description[1] + "' "
                                    + lvMetaData2.isSigned(i);
                    return false;
                }

            }

        } catch (SQLException e) {
            lastErrorMsg =
                    "ResultSetComparator - compareResultSetMetaData(): ResultSet '"
                            + description[1] + SQLEXCEPTION + e + " ("
                            + e.getMessage() + ")";
            return false;
        }

        return true;
    }

    private String concatenate(final String parSelectStmnt,
            final String parOrderBy) {

        if ("NOSORT".equals(parOrderBy.toUpperCase(Locale.getDefault()))) {
            return parSelectStmnt;
        }

        final String lvSelectStmnt = parSelectStmnt.trim();

        if (lvSelectStmnt.charAt(lvSelectStmnt.length() - 1) == ';') {
            return lvSelectStmnt.substring(0, lvSelectStmnt.length() - 1)
                    + " ORDER BY " + parOrderBy;
        }

        return lvSelectStmnt + " ORDER BY " + parOrderBy;
    }

    /**
     * Returns the last error message.
     * 
     * @return the last error message.
     */
    public final String getLastErrorMsg() {

        return lastErrorMsg;
    }

    /**
     * Sets the description at the required position.
     * 
     * @param parDescription The new description.
     * @param parPos The required position.
     */
    public final void setDescription(final String parDescription,
            final int parPos) {

        if ("".equals(parDescription)) {
            throw new IllegalArgumentException("Description missing");
        }

        if (!(parPos == 0 || parPos == 1)) {
            throw new IllegalArgumentException(
                    "Required position must be 0 or 1");
        }

        description[parPos] = parDescription;
    }

    /**
     * Sets the order by clause at the required position.
     * 
     * @param parOrderBy The new order by clause.
     * @param parPos The required position.
     */
    public final void setOrderBy(final String parOrderBy, final int parPos) {

        if (!(parPos == 0 || parPos == 1)) {
            throw new IllegalArgumentException(
                    "Required position must be 0 or 1");
        }

        if (parOrderBy == null) {
            return;
        }

        orderBy[parPos] = parOrderBy;
    }

    /**
     * Sets the <code>SELECT</code> statement at the required position.
     * 
     * @param parSelectStmnt The new <code>SELECT</code> statement.
     * @param parPos The required position.
     */
    public final void setSelectStmnt(final String parSelectStmnt,
            final int parPos) {

        if ("".equals(parSelectStmnt)) {
            throw new IllegalArgumentException("SELECT statement missing");
        }

        if (parSelectStmnt == null) {
            throw new IllegalArgumentException(
                    "'null' is not a valid SELECT statement");
        }

        if (!(parPos == 0 || parPos == 1)) {
            throw new IllegalArgumentException(
                    "Required position must be 0 or 1");
        }

        selectStmnt[parPos] = parSelectStmnt;
    }

    /**
     * Sets the <code>SQL</code> syntax code.
     * 
     * @param parSqlSyntaxCode The new <code>SQL</code> syntax code.
     */
    public final void setSqlSyntaxCode(final String parSqlSyntaxCode) {

        if ("".equals(parSqlSyntaxCode)) {
            sqlSyntaxCode = Global.SQL_SYNTAX_CODE_SQL_99;
            return;
        }

        if (sqlSyntaxCode == null) {
            sqlSyntaxCode = Global.SQL_SYNTAX_CODE_SQL_99;
            return;
        }

        sqlSyntaxCode = Global.SQL_SYNTAX_CODE_SQL_99;
    }

}
