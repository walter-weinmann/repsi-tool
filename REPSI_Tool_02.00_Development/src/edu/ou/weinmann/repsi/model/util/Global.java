package edu.ou.weinmann.repsi.model.util;

/**
 * Provides global constants.
 * 
 * @author Walter Weinmann
 * 
 */
public interface Global {

    /**
     * Database column name.
     */
    String COLUMN_NAME_APPLIED_PATTERN_ORDER_BY = "APPLIED_PATTERN_ORDER_BY";

    /**
     * Database column name.
     */
    String COLUMN_NAME_APPLIED_PATTERN_SELECT_STMNT =
            "APPLIED_PATTERN_SELECT_STMNT";

    /**
     * Database column name.
     */
    String COLUMN_NAME_ARITHMETIC_MEAN = "ARITHMETIC_MEAN";

    /**
     * Database column name.
     */
    String COLUMN_NAME_GEOMETRIC_MEAN = "GEOMETRIC_MEAN";

    /**
     * Database column name.
     */
    String COLUMN_NAME_JDBC_DRIVER = "JDBC_DRIVER";

    /**
     * Database column name.
     */
    String COLUMN_NAME_JDBC_URL = "JDBC_URL";

    /**
     * Database column name.
     */
    String COLUMN_NAME_KURTOSIS = "KURTOSIS";

    /**
     * Database column name.
     */
    String COLUMN_NAME_MAXIMUM_VALUE = "MAXIMUM_VALUE";

    /**
     * Database column name.
     */
    String COLUMN_NAME_MINIMUM_VALUE = "MINIMUM_VALUE";

    /**
     * Database column name.
     */
    String COLUMN_NAME_NAME = "NAME";

    /**
     * Database column name.
     */
    String COLUMN_NAME_NUMBER_OF_VALUES = "NUMBER_OF_VALUES";

    /**
     * Database column name.
     */
    String COLUMN_NAME_OBJECT = "OBJECT";

    /**
     * Database column name.
     */
    String COLUMN_NAME_ORDER_BY = "ORDER_BY";

    /**
     * Database column name.
     */
    String COLUMN_NAME_PASSWORD = "PASSWORD";

    /**
     * Database column name.
     */
    String COLUMN_NAME_PATTERN_SQL_IDIOM_NAME = "PATTERN_SQL_IDIOM_NAME";

    /**
     * Database column name.
     */
    String COLUMN_NAME_PERCENTILE_25 = "PERCENTILE_25";

    /**
     * Database column name.
     */
    String COLUMN_NAME_PERCENTILE_50 = "PERCENTILE_50";

    /**
     * Database column name.
     */
    String COLUMN_NAME_PERCENTILE_75 = "PERCENTILE_75";

    /**
     * Database column name.
     */
    String COLUMN_NAME_READINGS = "READINGS";

    /**
     * Database column name.
     */
    String COLUMN_NAME_SKEWNESS = "SKEWNESS";

    /**
     * Database column name.
     */
    String COLUMN_NAME_SQL_STATEMENT = "SQL_STATEMENT";

    /**
     * Database column name.
     */
    String COLUMN_NAME_SQL_SYNTAX_CODE = "SQL_SYNTAX_CODE";

    /**
     * Database column name.
     */
    String COLUMN_NAME_STANDARD_DEVIATION = "STANDARD_DEVIATION";

    /**
     * Database column name.
     */
    String COLUMN_NAME_TABLE_NAME = "TABLE_NAME";

    /**
     * Database column name.
     */
    String COLUMN_NAME_UNAPPLIED_PATTERN_ORDER_BY =
            "UNAPPLIED_PATTERN_ORDER_BY";

    /**
     * Database column name.
     */
    String COLUMN_NAME_UNAPPLIED_PATTERN_SELECT_STMNT =
            "UNAPPLIED_PATTERN_SELECT_STMNT";

    /**
     * Database column name.
     */
    String COLUMN_NAME_USER_NAME = "USER_NAME";

    /**
     * Database column name.
     */
    String COLUMN_NAME_VARIANCE = "VARIANCE";

    /**
     * Database schema identifier of the master database.
     */
    String DATABASE_SCHEMA_IDENTIFIER_MASTER = "master";

    /**
     * Database schema identifier of the test database.
     */
    String DATABASE_SCHEMA_IDENTIFIER_TEST = "test";

    /**
     * Date format - date - Java format.
     */
    String DATE_FORMAT_DD_MM_YYYY_JAVA = "dd-MM-yyyy";

    /**
     * Date format - date - SQL format.
     */
    String DATE_FORMAT_DD_MM_YYYY_SQL = "DD-MM-YYYY";

    /**
     * Date format - timestamp incl. milliseconds - Java format.
     */
    String DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_JAVA = "dd-MM-yyyy HH:mm:ss.SSS";

    /**
     * Date format - timestamp incl. milliseconds - SQL format.
     */
    String DATE_FORMAT_DD_MM_YYYY_HH_MM_SS_SSS_SQL =
            "DD-MM-YYYY HH24:MI:SS.FF3";

    /**
     * Date format - timestamp incl. milliseconds - SQL format.
     */
    String DATE_FORMAT_YYYY_MM_DD_HH_MM_SS_SSS_SQL =
            "YYYY-MM-DD HH24:MI:SS.FF3";

    /**
     * Day factor in milliseconds.
     */
    long DAY_FACTOR = 24 * 60 * 60000;

    /**
     * Error message - not yet implemented.
     */
    String ERROR_NOT_YET_IMPLEMENTED = " not yet implemented";

    /**
     * File type - Excel.
     */
    String FILE_TYPE_EXCEL = "xls";

    /**
     * File type - XML.
     */
    String FILE_TYPE_XML = "xml";

    /**
     * Initial capacity of foreign key columns.
     */
    int INITIAL_CAPACITY_FOREIGN_KEYS = 2;

    /**
     * Initial capacity of primary key columns.
     */
    int INITIAL_CAPACITY_PRIMARY_KEYS = 2;

    /**
     * Is nullable - yes.
     */
    String IS_NULLABLE_YES = "YES";

    /**
     * Maximum number of statistical measurement values.
     */
    long MAX_STATISTICAL_OUTLINE = 99999;

    /**
     * Maximum size of rows processed inside one transaction.
     */
    long MAX_TRANSACTION_SIZE = 5000;

    /**
     * Meta data - column name.
     */
    String META_DATA_COLUMN_NAME = "COLUMN_NAME";

    /**
     * Meta data - column size.
     */
    String META_DATA_COLUMN_SIZE = "COLUMN_SIZE";

    /**
     * Meta data - data type.
     */
    String META_DATA_DATA_TYPE = "DATA_TYPE";

    /**
     * Meta data - decimal digits.
     */
    String META_DATA_DECIMAL_DIGITS = "DECIMAL_DIGITS";

    /**
     * Meta data - decimal factor.
     */
    String META_DATA_DECIMAL_FACTOR = "DECIMAL_FACTOR";

    /**
     * Meta data - foreign key column name.
     */
    String META_DATA_FOREIGN_KEY_COLUMN_NAME = "FKCOLUMN_NAME";

    /**
     * Meta data - foreign key name.
     */
    String META_DATA_FOREIGN_KEY_NAME = "FK_NAME";

    /**
     * Meta data - foreign key table name.
     */
    String META_DATA_FOREIGN_KEY_TABLE_NAME = "FKTABLE_NAME";

    /**
     * Meta data - is nullable.
     */
    String META_DATA_IS_NULLABLE = "IS_NULLABLE";

    /**
     * Meta data - key sequence number.
     */
    String META_DATA_KEY_SEQUENCE = "KEY_SEQ";

    /**
     * Meta data - ordinal position.
     */
    String META_DATA_ORDINAL_POSITION = "ORDINAL_POSITION";

    /**
     * Meta data - primary key column name.
     */
    String META_DATA_PRIMARY_KEY_COLUMN_NAME = "PKCOLUMN_NAME";

    /**
     * Meta data - primary key name.
     */
    String META_DATA_PRIMARY_KEY_NAME = "PK_NAME";

    /**
     * Meta data - primary key table name.
     */
    String META_DATA_PRIMARY_KEY_TABLE_NAME = "PKTABLE_NAME";

    /**
     * Minimal value - VARCHAR.
     */
    String MINIMAL_VALUE_VARCHAR = "0";

    /**
     * Number format - Java format.
     */
    String NUMBER_FORMAT_JAVA = "###,###,##0";

    /**
     * Number format - long - Java format.
     */
    String NUMBER_FORMAT_LONG_JAVA = "###,###,###,###,###,##0";

    /**
     * Null value.
     */
    String NULL = "null";

    /**
     * Operation code - create table.
     */
    String OPERATION_CODE_CREATE_TABLE = "CT";

    /**
     * Operation code - drop and create table.
     */
    String OPERATION_CODE_DROP_AND_CREATE_TABLE = "DC";

    /**
     * Operation code - drop table.
     */
    String OPERATION_CODE_DROP_TABLE = "DT";

    /**
     * Operation code - execute query.
     */
    String OPERATION_CODE_EXECUTE_QUERY = "EQ";

    /**
     * Operation code - execute query applied.
     */
    String OPERATION_CODE_EXECUTE_QUERY_APPLIED = "EA";

    /**
     * Operation code - execute query unapplied.
     */
    String OPERATION_CODE_EXECUTE_QUERY_UNAPPLIED = "EU";

    /**
     * Operation code - insert row.
     */
    String OPERATION_CODE_INSERT_ROW = "IR";

    /**
     * Operation type - instance.
     */
    String OPERATION_TYPE_INSTANCE = "Instance";

    /**
     * Operation type - query.
     */
    String OPERATION_TYPE_QUERY = "Query";

    /**
     * Operation type - schema.
     */
    String OPERATION_TYPE_SCHEMA = "Schema";

    /**
     * Properties file name.
     */
    String PROPERTIES_FILE_NAME = "config/configuration.properties";

    /**
     * Property file path element - level 1 - database.
     */
    String PROPERTY_PATH_1_DATABASE = "database";

    /**
     * Property file path element - level 3 - driver.
     */
    String PROPERTY_PATH_3_DRIVER = "driver";

    /**
     * Property file path element - level 3 - password.
     */
    String PROPERTY_PATH_3_PASSWORD = "password";

    /**
     * Property file path element - level 3 - SQL syntax code.
     */
    String PROPERTY_PATH_3_SQL_SYNTAX_CODE = "sql.syntax.code";

    /**
     * Property file path element - level 3 - database URL.
     */
    String PROPERTY_PATH_3_URL = "url";

    /**
     * Property file path element - level 3 - user name.
     */
    String PROPERTY_PATH_3_USER_NAME = "username";

    /**
     * Separator - comma, space & single quote.
     */
    String SEPARATOR_COMMA_SPACE_SINGLE_QUOTE = ", '";

    /**
     * Separator - single quote, comma, space & single quote.
     */
    String SEPARATOR_SINGLE_QUOTE_COMMA_SPACE_SINGLE_QUOTE = "', '";

    /**
     * SQL column type - CHAR.
     */
    String SQL_COLUMN_TYPE_CHAR = "CHAR";

    /**
     * SQL column type - VARCHAR2.
     */
    String SQL_COLUMN_TYPE_VARCHAR2 = "VARCHAR2";

    /**
     * SQL syntax code - Oracle 10g Release 2.
     */
    String SQL_SYNTAX_CODE_ORACLE_10G = "ORACLE-10G";

    /**
     * SQL syntax code - standard SQL:1999.
     */
    String SQL_SYNTAX_CODE_SQL_99 = "SQL:1999";

    /**
     * Trial run status: end action.
     */
    String TRIAL_RUN_STATUS_END_ACTION = "EA";

    /**
     * Trial run status: end finalisation.
     */
    String TRIAL_RUN_STATUS_END_FINALISATION = "EF";

    /**
     * Trial run status: end initialisation.
     */
    String TRIAL_RUN_STATUS_END_INITIALISATION = "EI";

    /**
     * Trial run status: end program.
     */
    String TRIAL_RUN_STATUS_END_PROGRAM = "EP";

    /**
     * Trial run status: start action.
     */
    String TRIAL_RUN_STATUS_START_ACTION = "SA";

    /**
     * Trial run status: start finalisation.
     */
    String TRIAL_RUN_STATUS_START_FINALISATION = "SF";

    /**
     * Trial run status: start initialisation.
     */
    String TRIAL_RUN_STATUS_START_INITIALISATION = "SI";

    /**
     * Trial run status: start program.
     */
    String TRIAL_RUN_STATUS_START_PROGRAM = "SP";

}
