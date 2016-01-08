package edu.ou.weinmann.repsi.model.trial.metadata;

import java.math.BigDecimal;

/**
 * Manages one column of a database table.
 * 
 * @author Walter Weinmann
 * 
 */
public final class Column {

    private final String columnName;

    private final int columnSize;

    private final int dataType;

    private final int decimalDigits;

    private boolean isForeignKeyColumn;

    private int keySeq;

    private final String isNullable;

    private final int ordPosition;

    protected Column(final String parColumnName, final int parOrdinalPosition,
            final int parDataType, final int parColumnSize,
            final int parDecimalDigits, final String parIsNullable) {

        super();

        columnName = parColumnName;
        columnSize = parColumnSize;
        dataType = parDataType;
        decimalDigits = parDecimalDigits;
        keySeq = 0;
        isNullable = parIsNullable;
        ordPosition = parOrdinalPosition - 1;
    }

    /**
     * Returns the column name.
     * 
     * @return the column name.
     */
    public String getColumnName() {

        return columnName;
    }

    /**
     * Returns the column size.
     * 
     * @return the column size.
     */
    public int getColumnSize() {

        return columnSize;
    }

    /**
     * Returns the SQL type of <code>java.sql.Types</code>.
     * 
     * @return the SQL type of <code>java.sql.Types</code>.
     */
    public int getDataType() {

        return dataType;
    }

    /**
     * Returns the number of fractional digits.
     * 
     * @return the number of fractional digits.
     */
    public int getDecimalDigits() {

        return decimalDigits;
    }

    /**
     * Returns the decimal factor.
     * 
     * @return the decimal factor.
     */
    public BigDecimal getDecimalFactor() {

        return new BigDecimal(Math.pow(10D, columnSize));
    }

    /**
     * Returns whether the column can include <code>null</code>.
     * 
     * @return "YES", if the column can include <code>null</code>, "NO", if
     *         the column cannot include <code>null</code>, and an empty
     *         string, if the nullability for the column is unknown.
     */
    public String getIsNullable() {

        return isNullable;
    }

    /**
     * Returns the sequence number within primary key.
     * 
     * @return sequence number within primary key.
     */
    public int getKeySeq() {

        return keySeq;
    }

    protected int getOrdPosition() {

        return ordPosition;
    }

    /**
     * Returns whether the column is a foreign key column.
     * 
     * @return <code>true</code> if the column is a foreign key column.
     */
    public boolean isForeignKeyColumn() {

        return isForeignKeyColumn;
    }

    protected void setForeignKeyColumn(final boolean parIsForeignKeyColumn) {

        isForeignKeyColumn = parIsForeignKeyColumn;
    }

    protected void setKeySeq(final int parKeySeq) {

        this.keySeq = parKeySeq;
    }

}
