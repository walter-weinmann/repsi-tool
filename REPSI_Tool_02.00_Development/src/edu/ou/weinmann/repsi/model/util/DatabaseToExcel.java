package edu.ou.weinmann.repsi.model.util;

import java.io.File;
import java.io.IOException;

import java.math.BigDecimal;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Locale;

import jxl.Workbook;

import jxl.format.Colour;

import jxl.write.DateFormat;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormats;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import jxl.write.biff.RowsExceededException;

/**
 * Create an Excel compatible file out of database tables.
 * 
 * @author Walter Weinmann
 * 
 */
public class DatabaseToExcel {

    private static final int MAX_EXCEL_COLUMN_256 = 256;

    private String lastErrorMsg;

    private ResultSetMetaData resultSetMetaData;

    private WritableCellFormat[] writableCellFormat;

    private WritableSheet writableSheet;

    private WritableWorkbook writableWorkbook;

    /**
     * Constructs a <code>DatabaseToExcel</code> object.
     */
    public DatabaseToExcel() {

        super();

        lastErrorMsg = "";

        writableSheet = null;
    }

    /**
     * Closes the current workbook.
     * 
     * @return <code>true</code> if the creation of the file was completed
     *         without any error.
     */
    public final boolean closeWorkbook() {

        assert writableWorkbook != null : "Precondition: WritableWorkbook is null";

        try {
            writableWorkbook.write();
        } catch (IOException e) {
            lastErrorMsg =
                    "DatabaseToExcel - closeWorkbook(): write, IOException="
                            + e + " (" + e.getMessage() + ")";
            return false;
        }

        try {
            writableWorkbook.close();
        } catch (IOException e) {
            lastErrorMsg =
                    "DatabaseToExcel - closeWorkbook(): close, IOException="
                            + e + " (" + e.getMessage() + ")";
            return false;
        } catch (WriteException e) {
            lastErrorMsg =
                    "DatabaseToExcel - closeWorkbook(): close, WriteException="
                            + e + " (" + e.getMessage() + ")";
            return false;
        }

        return true;
    }

    /**
     * Creates a new sheet.
     * 
     * @param parSheetName The name of the worksheet.
     * @param parSheetPosition The position of the worksheet in the Excel file.
     * @param parResultSet The <code>ResultSet</code> to be converted into
     *            Excel format.
     * @param parHeader Whether a header is required.
     * 
     * @return <code>true</code> if the creation of the sheet was completed
     *         without any error.
     */
    public final boolean createSheet(final String parSheetName,
            final int parSheetPosition, final ResultSet parResultSet,
            final boolean parHeader) {

        if (parResultSet == null) {
            throw new IllegalArgumentException("ResultSet is missing");
        }

        assert writableWorkbook != null : "Precondition: WritableWorkbook is null";

        writableSheet =
                writableWorkbook.createSheet(parSheetName, parSheetPosition);

        if (parHeader && !(createSheetHeader(parSheetName, parResultSet))) {
            return false;
        }

        int lvRow = 0;

        try {
            while (parResultSet.next()) {

                lvRow++;

                if (!createSheetDetail(parSheetName, parResultSet, lvRow)) {
                    return false;
                }
            }
        } catch (SQLException e) {
            lastErrorMsg =
                    "DatabaseToExcel - createSheet(): next(), SQLException="
                            + e + " (" + e.getMessage() + ")";
            return false;
        }

        assert writableSheet != null : "Postcondition: WritableSheet is null";

        return true;
    }

    private boolean createSheetDetail(final String parSheetName,
            final ResultSet parResultSet, final int parRow) {

        if (parResultSet == null) {
            throw new IllegalArgumentException("ResultSet is missing");
        }

        try {
            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {

                final int parColumn = i - 1;

                if (i > MAX_EXCEL_COLUMN_256) {
                    continue;
                }

                final Object parColumnContent = parResultSet.getObject(i);

                if (parColumnContent == null) {
                    continue;
                }

                try {
                    switch (resultSetMetaData.getColumnType(i)) {
                        case java.sql.Types.ARRAY:

                            if ("TMD_CALIBRATION_STATISTIC"
                                    .equals(parSheetName)) {
                                final BigDecimal[] lvReadings =
                                        DatabaseAccessor.getArrayBigDecimal(
                                                parResultSet, i);

                                for (int j = 0; j < lvReadings.length; j++) {
                                    writableSheet.addCell(new Number(parColumn
                                            + j, parRow, lvReadings[j]
                                            .longValue(),
                                            writableCellFormat[parColumn]));

                                    if (i + j + 1 > MAX_EXCEL_COLUMN_256) {
                                        break;
                                    }
                                }
                            }

                            break;
                        case java.sql.Types.CHAR:
                        case java.sql.Types.VARCHAR:

                            writableSheet.addCell(new Label(parColumn, parRow,
                                    (String) parColumnContent));

                            break;
                        case java.sql.Types.DATE:

                            writableSheet.addCell(new DateTime(parColumn,
                                    parRow, (Date) parColumnContent,
                                    writableCellFormat[parColumn]));

                            break;
                        case java.sql.Types.DECIMAL:
                        case java.sql.Types.NUMERIC:

                            if (resultSetMetaData.getScale(i) == 0) {
                                writableSheet.addCell(new Number(parColumn,
                                        parRow, ((BigDecimal) parColumnContent)
                                                .longValue(),
                                        writableCellFormat[parColumn]));
                                break;
                            }

                            writableSheet.addCell(new Number(parColumn, parRow,
                                    ((BigDecimal) parColumnContent)
                                            .doubleValue(),
                                    writableCellFormat[parColumn]));

                            break;
                        case java.sql.Types.TIMESTAMP:

                            writableSheet.addCell(new DateTime(parColumn,
                                    parRow, parResultSet.getTimestamp(i),
                                    writableCellFormat[parColumn]));

                            break;
                        default:
                            lastErrorMsg =
                                    "DatabaseToExcel - createSheetRow(): getColumnType() SQL type="
                                            + resultSetMetaData
                                                    .getColumnType(i)
                                            + Global.ERROR_NOT_YET_IMPLEMENTED;
                            return false;
                    }
                } catch (final RowsExceededException e) {
                    lastErrorMsg =
                            "DatabaseToExcel - createSheetRow(): addCell() row="
                                    + parRow + " column=" + parColumn
                                    + ", RowsExceededException=" + e + " ("
                                    + e.getMessage() + ")";
                    return false;
                } catch (final SQLException e) {
                    lastErrorMsg =
                            "DatabaseToExcel - createSheetRow(): getColumnType() / getDate() column="
                                    + parColumn + ", SQLException=" + e + " ("
                                    + e.getMessage() + ")";
                    return false;
                } catch (final WriteException e) {
                    lastErrorMsg =
                            "DatabaseToExcel - createSheetRow(): addCell() row="
                                    + parRow + " column=" + parColumn
                                    + ", WriteException=" + e + " ("
                                    + e.getMessage() + ")";
                    return false;
                }
            }
        } catch (final SQLException e) {
            lastErrorMsg =
                    "DatabaseToExcel - createSheetRow(): getColumnCount(), SQLException="
                            + e + " (" + e.getMessage() + ")";
            return false;
        }

        return true;
    }

    private boolean createSheetHeader(final String parSheetName,
            final ResultSet parResultSet) {

        if (parResultSet == null) {
            throw new IllegalArgumentException("ResultSet is missing");
        }

        try {
            resultSetMetaData = parResultSet.getMetaData();

            writableCellFormat =
                    new WritableCellFormat[resultSetMetaData.getColumnCount()];

            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {

                final int parColumn = i - 1;

                try {
                    writableSheet.addCell(new Label(parColumn, 0,
                            formatColumnLabel(resultSetMetaData
                                    .getColumnLabel(i))));
                    final WritableCell lvCell =
                            writableSheet.getWritableCell(parColumn, 0);
                    final WritableCellFormat lvFormat =
                            new WritableCellFormat(lvCell.getCellFormat());
                    lvFormat.setBackground(Colour.GRAY_25);
                    lvFormat
                            .setFont(new WritableFont(WritableFont.ARIAL,
                                    WritableFont.DEFAULT_POINT_SIZE,
                                    WritableFont.BOLD));
                    lvCell.setCellFormat(lvFormat);

                    switch (resultSetMetaData.getColumnType(i)) {
                        case java.sql.Types.ARRAY:

                            if ("TMD_CALIBRATION_STATISTIC"
                                    .equals(parSheetName)) {
                                writableCellFormat[parColumn] =
                                        new WritableCellFormat(
                                                NumberFormats.THOUSANDS_INTEGER);
                            }

                            break;
                        case java.sql.Types.CHAR:
                        case java.sql.Types.VARCHAR:

                            writableCellFormat[parColumn] = null;

                            break;
                        case java.sql.Types.DATE:

                            writableCellFormat[parColumn] =
                                    new WritableCellFormat(new DateFormat(
                                            Global.DATE_FORMAT_DD_MM_YYYY_JAVA));

                            break;
                        case java.sql.Types.DECIMAL:
                        case java.sql.Types.NUMERIC:

                            if (resultSetMetaData.getScale(i) == 0) {
                                writableCellFormat[parColumn] =
                                        new WritableCellFormat(
                                                NumberFormats.THOUSANDS_INTEGER);
                            } else {
                                writableCellFormat[parColumn] =
                                        new WritableCellFormat(
                                                NumberFormats.THOUSANDS_FLOAT);
                            }

                            break;
                        case java.sql.Types.TIMESTAMP:

                            writableCellFormat[parColumn] =
                                    new WritableCellFormat(
                                            new DateFormat(
                                                    Global.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_JAVA));

                            break;
                        default:
                            lastErrorMsg =
                                    "DatabaseToExcel - createSheetRow(): getColumnType() SQL type="
                                            + resultSetMetaData
                                                    .getColumnType(i)
                                            + Global.ERROR_NOT_YET_IMPLEMENTED;
                            return false;
                    }

                } catch (RowsExceededException e) {
                    lastErrorMsg =
                            "DatabaseToExcel - createSheetLabel(): addCell() row=0 column="
                                    + parColumn + ", RowsExceededException="
                                    + e + " (" + e.getMessage() + ")";
                    return false;
                } catch (WriteException e) {
                    lastErrorMsg =
                            "DatabaseToExcel - createSheetLabel(): addCell() row=0 column="
                                    + parColumn + ", WriteException=" + e
                                    + " (" + e.getMessage() + ")";
                    return false;
                }
            }
        } catch (SQLException e) {
            lastErrorMsg =
                    "DatabaseToExcel - createSheetLabel(): getMetaData(), SQLException="
                            + e + " (" + e.getMessage() + ")";
            return false;
        }

        return true;
    }

    /**
     * Creates a new workbook.
     * 
     * @param parFileName The complete file name including the directory.
     * 
     * @return <code>true</code> if the creation of the file was completed
     *         without any error.
     */
    public final boolean createWorkbook(final String parFileName) {

        try {
            writableWorkbook = Workbook.createWorkbook(new File(parFileName));
        } catch (IOException e) {
            lastErrorMsg =
                    "DatabaseToExcel - createWorkbook(): Filename "
                            + parFileName + ", IOException=" + e + " ("
                            + e.getMessage() + ")";
            return false;
        }

        assert writableWorkbook != null : "Postcondition: WritableWorkbook is null";

        return true;
    }

    private String formatColumnLabel(final String parLabel) {

        if (parLabel == null) {
            return "";
        }

        if (parLabel.length() < 2) {
            return parLabel;
        }

        final StringBuffer lvLabel =
                new StringBuffer(parLabel.replaceAll("_", " ").toLowerCase(Locale.getDefault()));

        for (int i = 0; i < lvLabel.length(); i++) {
            if (i == 0 || lvLabel.charAt(i - 1) == ' ') {
                lvLabel.replace(i, i + 1, lvLabel.substring(i, i + 1)
                        .toUpperCase(Locale.getDefault()));
            }
        }

        return lvLabel.toString().replaceAll(" Jdbc", " JDBC").replaceAll(
                " Mb", " MB").replaceAll(" Ram", " RAM").replaceAll(" Sql",
                " SQL").replaceAll(" Url", " URL").replaceAll("Jdbc ", "JDBC ")
                .replaceAll("Mb ", " MB ").replaceAll("Ram ", "RAM ")
                .replaceAll("Sql ", "SQL ").replaceAll("Url ", "URL ");
    }

    /**
     * Returns the last error message.
     * 
     * @return the last error message.
     */
    public final String getLastErrorMsg() {

        return lastErrorMsg;
    }
}
