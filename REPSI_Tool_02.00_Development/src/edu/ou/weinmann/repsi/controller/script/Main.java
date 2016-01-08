package edu.ou.weinmann.repsi.controller.script;

import edu.ou.weinmann.repsi.model.calibration.Calibration;
import edu.ou.weinmann.repsi.model.database.Database;

import edu.ou.weinmann.repsi.model.trial.Trial;
import edu.ou.weinmann.repsi.model.util.Global;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Command line interface to the REPSI tool. The interface is based on the
 * Jakarta Commons CLI library which simplifies the handling of command line
 * parameters. The main parameter is called <code>mode</code> and this
 * parameter determines the functionality to be executed:
 * <ul>
 * <li><code>calibration</code> perform a calibration run.</li>
 * <li><code>master</code> modify the schema or instance of the master
 * database.</li>
 * <li><code>result_cn</code> export the results of a calibration run into an
 * Excel file.</li>
 * <li><code>result_r</code> export the results of a calibration run into a R
 * compatible files.</li>
 * <li><code>result_tl</code> export the results of one or all trial runs
 * into an Excel file.</li>
 * <li><code>test modify</code> the schema or instance of a test database.</li>
 * <li>t<code>rial</code> perform a trial run.</li>
 * </ul>
 * <br>
 * Further details can be found in the document 'REPSI Tool User Manual'.
 * 
 * @author Walter Weinmann
 * 
 */
public final class Main {

    private static final Logger LOGGER =
            Logger.getLogger(Main.class.getPackage().getName());

    private static final String MSG_ARGUMENT_MUST_BE_AN_INTEGER =
            ": argument must be an integer";

    private static final String MSG_OPTION_ALLOWS_NO_ARGUMENT =
            ": option allows no argument";

    private static final String MSG_OPTION_IS_MANDATORY_WITH_MODE =
            ": option is mandatory with mode=";

    private static final String MSG_OPTION_NOT_ALLOWED_WITH_MODE =
            ": option not allowed with mode=";

    private static final String MSG_OPTION_REQUIRES_AN_ARGUMENT =
            ": option requires an argument";

    private static final String OBJECT_TYPE_NANOTIME = "nanotime";

    private static final String OBJECT_TYPE_QUERY = "query";

    private static final String OPTION = "Option ";

    private static final String OPTION_CYC_CODE = "cyc";

    private static final String OPTION_CYC_NAME = "number of cycles to run";

    private static final String OPTION_DES_CODE = "des";

    private static final String OPTION_DES_NAME = "description of the run";

    private static final String OPTION_DI_CODE = "di";

    private static final String OPTION_DI_NAME =
            "database instance (identification)";

    private static final String OPTION_EFN_CODE = "efn";

    private static final String OPTION_EFN_NAME = "name of the Excel file";

    private static final String OPTION_EFNALL_CODE = "efnall";

    private static final String OPTION_EFNALL_NAME =
            "all trial runs into the Excel file";

    private static final String OPTION_EXALT_CODE = "exalt";

    private static final String OPTION_EXALT_NAME =
            "execute the queries alternating without and with the application of the pattern";

    private static final String OPTION_EXCON_CODE = "excon";

    private static final String OPTION_EXCON_NAME =
            "execute first the query without application of the pattern and then the query with application of the pattern";

    private static final String OPTION_FN_CODE = "fn";

    private static final String OPTION_FN_NAME =
            "name of the file which contains DDL / DML statements";

    private static final String OPTION_FNEXCEL_NAME =
            "file contains an Excel file";

    private static final String OPTION_FNXML_NAME =
            "file contains an XML document";

    private static final String OPTION_FS_CODE = "fs";

    private static final String OPTION_FS_NAME = "fetch size";

    private static final String OPTION_IDN_CODE = "idn";

    private static final String OPTION_IDN_NAME = "name of the input directory";

    private static final String OPTION_IGN1_CODE = "ign1";

    private static final String OPTION_IGN1_NAME = "ignore the first reading";

    private static final String OPTION_MODE_CALIBRATION = "calibration";

    private static final String OPTION_MODE_CODE = "mode";

    private static final String OPTION_MODE_MASTER = "master";

    private static final String OPTION_MODE_NAME =
            "processing mode (calibration/master/result_cn/result_tl/test/trial)";

    private static final String OPTION_MODE_RESULT_CN = "result_cn";

    private static final String OPTION_MODE_RESULT_R = "result_R";

    private static final String OPTION_MODE_RESULT_TL = "result_tl";

    private static final String OPTION_MODE_TEST = "test";

    private static final String OPTION_MODE_TRIAL = "trial";

    private static final String OPTION_OBJ_CODE = "obj";

    private static final String OPTION_OBJ_NAME = "object to calibrate";

    private static final String OPTION_ODN_CODE = "odn";

    private static final String OPTION_ODN_NAME =
            "name of the output directory";

    private static final String OPTION_PF_CODE = "pf";

    private static final String OPTION_PF_NAME = "name of the properties file";

    private static final String OPTION_PFXML_CODE = "pfxml";

    private static final String OPTION_PFXML_NAME =
            "properties file contains an XML document";

    private static final String OPTION_PREC_CODE = "prec";

    private static final String OPTION_PREC_NAME =
            "exponent (base 10) of the time precision: 0 (nanosecond), 3 (microsecond), ...";

    private static final String OPTION_TQP_CODE = "tqp";

    private static final String OPTION_TQP_NAME =
            "test query pair (identification)";

    private static final String OPTION_TS_CODE = "ts";

    private static final String OPTION_TS_NAME = "test suite (identification)";

    private static final String OPTION_VERB_CODE = "verb";

    private static final String OPTION_VERB_NAME =
            "print a statistical overview";

    private static final int STATUS_ERROR = 1;

    private static final int STATUS_OK = 0;

    private static int argumentCyc;

    private static int argumentDi;

    private static String argumentDes;

    private static String argumentEfn;

    private static String[] argumentFn;

    private static int argumentFs;

    private static String argumentIdn;

    private static String argumentMode;

    private static String argumentObj;

    private static String argumentOdn;

    private static String argumentPf;

    private static long argumentPrec;

    private static int argumentTqp;

    private static int argumentTs;

    private static Database database;

    private static boolean isEfnall;

    private static boolean isExalt;

    private static boolean[] isFnxls;

    private static boolean[] isFnxml;

    private static boolean isExcon;

    private static boolean isIgn1;

    private static boolean isPfxml;

    private static boolean isVerbose;

    /**
     * Initialises the database accessor of the master database.
     */
    private Main() {

        super();
    }

    private static int checkCmdLineOptions(final CommandLine parCmdLine) {

        if (!checkOptionMode(parCmdLine, OPTION_MODE_CODE)) {
            return STATUS_ERROR;
        }

        if (!checkOptionObj(parCmdLine, OPTION_OBJ_CODE)) {
            return STATUS_ERROR;
        }

        if (!checkOptionCyc(parCmdLine, OPTION_CYC_CODE)) {
            return STATUS_ERROR;
        }

        if (!checkOptionDes(parCmdLine, OPTION_DES_CODE)) {
            return STATUS_ERROR;
        }

        if (!checkOptionDi(parCmdLine, OPTION_DI_CODE)) {
            return STATUS_ERROR;
        }

        if (!checkOptionEfn(parCmdLine, OPTION_EFN_CODE)) {
            return STATUS_ERROR;
        }

        if (!checkOptionEfnall(parCmdLine, OPTION_EFNALL_CODE)) {
            return STATUS_ERROR;
        }

        if (!checkOptionExalt(parCmdLine, OPTION_EXALT_CODE)) {
            return STATUS_ERROR;
        }

        if (!checkOptionExcon(parCmdLine, OPTION_EXCON_CODE)) {
            return STATUS_ERROR;
        }

        if (!checkOptionFn(parCmdLine)) {
            return STATUS_ERROR;
        }

        if (!checkOptionFs(parCmdLine, OPTION_FS_CODE)) {
            return STATUS_ERROR;
        }

        if (!checkOptionIdn(parCmdLine, OPTION_IDN_CODE)) {
            return STATUS_ERROR;
        }

        if (!checkOptionIgn1(parCmdLine, OPTION_IGN1_CODE)) {
            return STATUS_ERROR;
        }

        if (!checkOptionOdn(parCmdLine, OPTION_ODN_CODE)) {
            return STATUS_ERROR;
        }

        if (!checkOptionPf(parCmdLine, OPTION_PF_CODE)) {
            return STATUS_ERROR;
        }

        if (!checkOptionPfxml(parCmdLine, OPTION_PFXML_CODE)) {
            return STATUS_ERROR;
        }

        if (!checkOptionPrec(parCmdLine, OPTION_PREC_CODE)) {
            return STATUS_ERROR;
        }

        if (!checkOptionTqp(parCmdLine, OPTION_TQP_CODE)) {
            return STATUS_ERROR;
        }

        if (!checkOptionTs(parCmdLine, OPTION_TS_CODE)) {
            return STATUS_ERROR;
        }

        if (!checkOptionVerb(parCmdLine, OPTION_VERB_CODE)) {
            return STATUS_ERROR;
        }

        return STATUS_OK;
    }

    private static boolean checkOptionCyc(final CommandLine parCmdLine,
            final String parOptionCode) {

        if (!parCmdLine.hasOption(parOptionCode)) {
            if (OPTION_MODE_CALIBRATION.equals(argumentMode)) {
                argumentCyc = 50;
            }

            return true;
        }

        if (!(OPTION_MODE_CALIBRATION.equals(argumentMode) || OPTION_MODE_TRIAL
                .equals(argumentMode))) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_NOT_ALLOWED_WITH_MODE + argumentMode);
            return false;
        }

        final String lvCycleIn = parCmdLine.getOptionValue(OPTION_CYC_CODE);

        if (lvCycleIn == null) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_REQUIRES_AN_ARGUMENT);
            return false;
        }

        try {
            argumentCyc = Integer.parseInt(lvCycleIn);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_ARGUMENT_MUST_BE_AN_INTEGER);
            return false;
        }

        if (argumentCyc < 1) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + ": argument must be greater than zero");
            return false;
        }

        if (OPTION_MODE_CALIBRATION.equals(argumentMode) && argumentCyc > 999) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + ": argument must be less than 1000");
            return false;
        }

        return true;
    }

    private static boolean checkOptionDes(final CommandLine parCmdLine,
            final String parOptionCode) {

        if (!parCmdLine.hasOption(parOptionCode)) {
            return true;
        }

        if (!(OPTION_MODE_CALIBRATION.equals(argumentMode) || OPTION_MODE_TRIAL
                .equals(argumentMode))) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_NOT_ALLOWED_WITH_MODE + argumentMode);
            return false;
        }

        argumentDes = parCmdLine.getOptionValue(parOptionCode);

        if (argumentDes == null) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_REQUIRES_AN_ARGUMENT);
            return false;
        }

        return true;
    }

    private static boolean checkOptionDi(final CommandLine parCmdLine,
            final String parOptionCode) {

        if (!parCmdLine.hasOption(parOptionCode)) {

            if (OPTION_MODE_CALIBRATION.equals(argumentMode)
                    || OPTION_MODE_TEST.equals(argumentMode)
                    || OPTION_MODE_TRIAL.equals(argumentMode)) {
                LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                        + MSG_OPTION_IS_MANDATORY_WITH_MODE + argumentMode);
                return false;
            }

            return true;
        }

        if (!(OPTION_MODE_CALIBRATION.equals(argumentMode)
                || OPTION_MODE_TEST.equals(argumentMode) || OPTION_MODE_TRIAL
                .equals(argumentMode))) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_NOT_ALLOWED_WITH_MODE + argumentMode);
            return false;
        }

        final String lvDatabaseInstanceIdIn =
                parCmdLine.getOptionValue(parOptionCode);

        if (lvDatabaseInstanceIdIn == null) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_REQUIRES_AN_ARGUMENT);
            return false;
        }

        try {
            argumentDi = Integer.parseInt(lvDatabaseInstanceIdIn);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_ARGUMENT_MUST_BE_AN_INTEGER);
            return false;
        }

        return true;
    }

    private static boolean checkOptionEfn(final CommandLine parCmdLine,
            final String parOptionCode) {

        if (!parCmdLine.hasOption(parOptionCode)) {
            if (OPTION_MODE_RESULT_CN.equals(argumentMode)) {
                argumentEfn = "out/CalibrationData.xls";
            } else if (OPTION_MODE_RESULT_TL.equals(argumentMode)) {
                argumentEfn = "out/TrialRunData.xls";
            }

            return true;
        }

        if (!(OPTION_MODE_RESULT_CN.equals(argumentMode) || OPTION_MODE_RESULT_TL
                .equals(argumentMode))) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_NOT_ALLOWED_WITH_MODE + argumentMode);
            return false;
        }

        argumentEfn = parCmdLine.getOptionValue(parOptionCode);

        if (argumentEfn == null) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_REQUIRES_AN_ARGUMENT);
            return false;
        }

        return true;
    }

    private static boolean checkOptionEfnall(final CommandLine parCmdLine,
            final String parOptionCode) {

        isEfnall = parCmdLine.hasOption(parOptionCode);

        if (!isEfnall) {
            return true;
        }

        if (!OPTION_MODE_RESULT_TL.equals(argumentMode)) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_NOT_ALLOWED_WITH_MODE + argumentMode);
            return false;
        }

        if (parCmdLine.getOptionValue(parOptionCode) != null) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_ALLOWS_NO_ARGUMENT);
            return false;
        }

        return true;
    }

    private static boolean checkOptionExalt(final CommandLine parCmdLine,
            final String parOptionCode) {

        isExalt = parCmdLine.hasOption(parOptionCode);

        if (!isExalt) {
            return true;
        }

        if (!OPTION_MODE_CALIBRATION.equals(argumentMode)) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_NOT_ALLOWED_WITH_MODE + argumentMode);
            return false;
        }

        if (!OBJECT_TYPE_QUERY.equals(argumentObj)) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + ": option not allowed with obj=" + argumentObj);
            return false;
        }

        if (parCmdLine.getOptionValue(parOptionCode) != null) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_ALLOWS_NO_ARGUMENT);
            return false;
        }

        return true;
    }

    private static boolean checkOptionExcon(final CommandLine parCmdLine,
            final String parOptionCode) {

        isExcon = parCmdLine.hasOption(parOptionCode);

        if (!isExcon) {
            if (!isExalt && OPTION_MODE_CALIBRATION.equals(argumentMode)
                    && OBJECT_TYPE_QUERY.equals(argumentObj)) {
                LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                        + ": option exalt or excon mandatory with mode="
                        + argumentMode + " and obj=" + argumentObj);
                return false;
            }

            return true;
        }

        if (!OPTION_MODE_CALIBRATION.equals(argumentMode)) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_NOT_ALLOWED_WITH_MODE + argumentMode);
            return false;
        }

        if (!OBJECT_TYPE_QUERY.equals(argumentObj)) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + ": option not allowed with obj=" + argumentObj);
            return false;
        }

        if (parCmdLine.getOptionValue(parOptionCode) != null) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_ALLOWS_NO_ARGUMENT);
            return false;
        }

        return true;
    }

    private static boolean checkOptionFn(final CommandLine parCmdLine) {

        int lvNumber = 0;

        for (int i = 0; i < argumentFn.length; i++) {

            if (!parCmdLine.hasOption(OPTION_FN_CODE + i)
                    && !parCmdLine.hasOption(OPTION_FN_CODE + i
                            + Global.FILE_TYPE_EXCEL)
                    && !parCmdLine.hasOption(OPTION_FN_CODE + i
                            + Global.FILE_TYPE_XML)) {
                continue;
            }

            if (!(OPTION_MODE_MASTER.equals(argumentMode) || OPTION_MODE_TEST
                    .equals(argumentMode))) {

                if (parCmdLine.hasOption(OPTION_FN_CODE + i)) {
                    LOGGER.log(Level.SEVERE, OPTION + OPTION_FN_CODE + i
                            + MSG_OPTION_NOT_ALLOWED_WITH_MODE + argumentMode);
                    return false;
                }

                if (parCmdLine.hasOption(OPTION_FN_CODE + i
                        + Global.FILE_TYPE_EXCEL)) {
                    LOGGER.log(Level.SEVERE, OPTION + OPTION_FN_CODE + i
                            + "xls: option not allowed with mode="
                            + argumentMode);
                    return false;
                }

                LOGGER.log(Level.SEVERE, OPTION + OPTION_FN_CODE + i
                        + "xml: option not allowed with mode=" + argumentMode);

                return false;
            }

            if (!parCmdLine.hasOption(OPTION_FN_CODE + i)) {
                LOGGER.log(Level.SEVERE, OPTION + OPTION_FN_CODE + i
                        + MSG_OPTION_IS_MANDATORY_WITH_MODE + argumentMode);
                return false;
            }

            argumentFn[i] = parCmdLine.getOptionValue(OPTION_FN_CODE + i);

            if (argumentFn[i] == null) {
                LOGGER.log(Level.SEVERE, OPTION + OPTION_FN_CODE + i
                        + MSG_OPTION_REQUIRES_AN_ARGUMENT);
                return false;
            }

            lvNumber++;

            isFnxls[i] =
                    parCmdLine.hasOption(OPTION_FN_CODE + i
                            + Global.FILE_TYPE_EXCEL);
            isFnxml[i] =
                    parCmdLine.hasOption(OPTION_FN_CODE + i
                            + Global.FILE_TYPE_XML);

            if (isFnxls[i] && isFnxml[i]) {
                LOGGER.log(Level.SEVERE, "Either option " + OPTION_FN_CODE + i
                        + "xls or option " + OPTION_FN_CODE + i
                        + "xml - but not both together");
                return false;
            }
        }

        if (lvNumber == 0
                && (OPTION_MODE_MASTER.equals(argumentMode) || OPTION_MODE_TEST
                        .equals(argumentMode))) {
            LOGGER.log(Level.SEVERE, OPTION + OPTION_FN_CODE
                    + ".: at least one occurence required with mode="
                    + argumentMode);
            return false;
        }

        return true;
    }

    private static boolean checkOptionFs(final CommandLine parCmdLine,
            final String parOptionCode) {

        if (!parCmdLine.hasOption(parOptionCode)) {
            if (OPTION_MODE_CALIBRATION.equals(argumentMode)
                    || OPTION_MODE_TRIAL.equals(argumentMode)) {
                argumentFs = 10;
            }

            return true;
        }

        if (!(OPTION_MODE_CALIBRATION.equals(argumentMode) || OPTION_MODE_TRIAL
                .equals(argumentMode))) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_NOT_ALLOWED_WITH_MODE + argumentMode);
            return false;
        }

        final String lvFetchSize = parCmdLine.getOptionValue(parOptionCode);

        if (lvFetchSize == null) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_REQUIRES_AN_ARGUMENT);
            return false;
        }

        try {
            argumentFs = Integer.parseInt(lvFetchSize);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_ARGUMENT_MUST_BE_AN_INTEGER);
            return false;
        }

        return true;
    }

    private static boolean checkOptionIdn(final CommandLine parCmdLine,
            final String parOptionCode) {

        if (!parCmdLine.hasOption(parOptionCode)) {
            argumentIdn = "in/R";

            return true;
        }

        if (!OPTION_MODE_RESULT_R.equals(argumentMode)) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_NOT_ALLOWED_WITH_MODE + argumentMode);
            return false;
        }

        argumentIdn = parCmdLine.getOptionValue(parOptionCode);

        if (argumentIdn == null) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_REQUIRES_AN_ARGUMENT);
            return false;
        }

        return true;
    }

    private static boolean checkOptionIgn1(final CommandLine parCmdLine,
            final String parOptionCode) {

        isIgn1 = parCmdLine.hasOption(parOptionCode);

        if (!isIgn1) {
            return true;
        }

        if (!OPTION_MODE_CALIBRATION.equals(argumentMode)) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_NOT_ALLOWED_WITH_MODE + argumentMode);
            return false;
        }

        if (parCmdLine.getOptionValue(parOptionCode) != null) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_ALLOWS_NO_ARGUMENT);
            return false;
        }

        return true;
    }

    private static boolean checkOptionMode(final CommandLine parCmdLine,
            final String parOptionCode) {

        if (!parCmdLine.hasOption(parOptionCode)) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_IS_MANDATORY_WITH_MODE + argumentMode);
            return false;
        }

        argumentMode = parCmdLine.getOptionValue(parOptionCode);

        if (argumentMode == null) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_REQUIRES_AN_ARGUMENT);
            return false;
        }

        if (OPTION_MODE_CALIBRATION.equals(argumentMode)
                || OPTION_MODE_MASTER.equals(argumentMode)
                || OPTION_MODE_RESULT_CN.equals(argumentMode)
                || OPTION_MODE_RESULT_R.equals(argumentMode)
                || OPTION_MODE_RESULT_TL.equals(argumentMode)
                || OPTION_MODE_TEST.equals(argumentMode)
                || OPTION_MODE_TRIAL.equals(argumentMode)) {
            return true;
        }

        LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                + ": option has wrong argument");

        return false;
    }

    private static boolean checkOptionObj(final CommandLine parCmdLine,
            final String parOptionCode) {

        if (!parCmdLine.hasOption(parOptionCode)) {
            if (OPTION_MODE_CALIBRATION.equals(argumentMode)) {
                LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                        + MSG_OPTION_IS_MANDATORY_WITH_MODE + argumentMode);
                return false;
            }

            return true;
        }

        if (!OPTION_MODE_CALIBRATION.equals(argumentMode)) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_NOT_ALLOWED_WITH_MODE + argumentMode);
            return false;
        }

        argumentObj = parCmdLine.getOptionValue(parOptionCode);

        if (argumentObj == null) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_REQUIRES_AN_ARGUMENT);
            return false;
        }

        if (!(OBJECT_TYPE_NANOTIME.equals(argumentObj) || OBJECT_TYPE_QUERY
                .equals(argumentObj))) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + ": option has wrong argument");
            return false;
        }

        return true;
    }

    private static boolean checkOptionOdn(final CommandLine parCmdLine,
            final String parOptionCode) {

        if (!parCmdLine.hasOption(parOptionCode)) {
            argumentOdn = "out/R";

            return true;
        }

        if (!OPTION_MODE_RESULT_R.equals(argumentMode)) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_NOT_ALLOWED_WITH_MODE + argumentMode);
            return false;
        }

        argumentOdn = parCmdLine.getOptionValue(parOptionCode);

        if (argumentOdn == null) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_REQUIRES_AN_ARGUMENT);
            return false;
        }

        return true;
    }

    private static boolean checkOptionPf(final CommandLine parCmdLine,
            final String parOptionCode) {

        if (!parCmdLine.hasOption(parOptionCode)) {
            return true;
        }

        argumentPf = parCmdLine.getOptionValue(parOptionCode);

        if ("".equals(argumentPf)) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_REQUIRES_AN_ARGUMENT);
            return false;
        }

        return true;
    }

    private static boolean checkOptionPfxml(final CommandLine parCmdLine,
            final String parOptionCode) {

        isPfxml = parCmdLine.hasOption(parOptionCode);

        if (!isPfxml) {
            return true;
        }

        if (parCmdLine.getOptionValue(parOptionCode) != null) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_ALLOWS_NO_ARGUMENT);
            return false;
        }

        if ("".equals(argumentPf)) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + ": option only allowed in connection with option pf");
            return false;
        }

        return true;
    }

    private static boolean checkOptionPrec(final CommandLine parCmdLine,
            final String parOptionCode) {

        if (!parCmdLine.hasOption(parOptionCode)) {
            return true;
        }

        if (!(OPTION_MODE_CALIBRATION.equals(argumentMode) || OPTION_MODE_TRIAL
                .equals(argumentMode))) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_NOT_ALLOWED_WITH_MODE + argumentMode);
            return false;
        }

        int lvPrecision;

        final String lvPrecisionIn = parCmdLine.getOptionValue(parOptionCode);

        if (lvPrecisionIn == null) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_REQUIRES_AN_ARGUMENT);
            return false;
        }

        try {
            lvPrecision = Integer.parseInt(lvPrecisionIn);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_ARGUMENT_MUST_BE_AN_INTEGER);
            return false;
        }

        if (lvPrecision < 0) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + ": argument must be positive");
            return false;
        }

        argumentPrec =
                Double
                        .valueOf(
                                Math
                                        .pow(10., Double
                                                .parseDouble(lvPrecisionIn)))
                        .intValue();

        return true;
    }

    private static boolean checkOptionTqp(final CommandLine parCmdLine,
            final String parOptionCode) {

        if (!parCmdLine.hasOption(parOptionCode)) {

            if (OPTION_MODE_CALIBRATION.equals(argumentMode)
                    && OBJECT_TYPE_QUERY.equals(argumentObj)) {
                LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                        + MSG_OPTION_IS_MANDATORY_WITH_MODE + argumentMode
                        + " and obj=" + argumentObj);
                return false;
            }

            return true;
        }

        if (!OPTION_MODE_CALIBRATION.equals(argumentMode)) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_NOT_ALLOWED_WITH_MODE + argumentMode);
            return false;
        }

        if (!OBJECT_TYPE_QUERY.equals(argumentObj)) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + ": option not allowed with obj=" + argumentObj);
            return false;
        }

        final String lvTestQueryPairIdIn =
                parCmdLine.getOptionValue(parOptionCode);

        if (lvTestQueryPairIdIn == null) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_REQUIRES_AN_ARGUMENT);
            return false;
        }

        try {
            argumentTqp = Integer.parseInt(lvTestQueryPairIdIn);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_ARGUMENT_MUST_BE_AN_INTEGER);
            return false;
        }

        return true;
    }

    private static boolean checkOptionTs(final CommandLine parCmdLine,
            final String parOptionCode) {

        if (!parCmdLine.hasOption(parOptionCode)) {

            if (OPTION_MODE_TRIAL.equals(argumentMode)) {
                LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                        + MSG_OPTION_IS_MANDATORY_WITH_MODE + argumentMode);
                return false;
            }

            return true;
        }

        if (!OPTION_MODE_TRIAL.equals(argumentMode)) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_NOT_ALLOWED_WITH_MODE + argumentMode);
            return false;
        }

        final String lvTestSuiteIdIn = parCmdLine.getOptionValue(parOptionCode);

        if (lvTestSuiteIdIn == null) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_REQUIRES_AN_ARGUMENT);
            return false;
        }

        try {
            argumentTs = Integer.parseInt(lvTestSuiteIdIn);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_ARGUMENT_MUST_BE_AN_INTEGER);
            return false;
        }

        return true;
    }

    private static boolean checkOptionVerb(final CommandLine parCmdLine,
            final String parOptionCode) {

        isVerbose = parCmdLine.hasOption(parOptionCode);

        if (!isVerbose) {
            return true;
        }

        if (!OPTION_MODE_CALIBRATION.equals(argumentMode)) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_NOT_ALLOWED_WITH_MODE + argumentMode);
            return false;
        }

        if (parCmdLine.getOptionValue(parOptionCode) != null) {
            LOGGER.log(Level.SEVERE, OPTION + parOptionCode
                    + MSG_OPTION_ALLOWS_NO_ARGUMENT);
            return false;
        }

        return true;
    }

    private static Options defineCmdLineOptions() {

        final Options lvOptions = new Options();

        lvOptions.addOption(OPTION_EXALT_CODE, false, OPTION_EXALT_NAME);

        lvOptions.addOption(OPTION_EXCON_CODE, false, OPTION_EXCON_NAME);
        lvOptions.addOption(OPTION_CYC_CODE, true, OPTION_CYC_NAME);

        lvOptions.addOption(OPTION_DES_CODE, true, OPTION_DES_NAME);
        lvOptions.addOption(OPTION_DI_CODE, true, OPTION_DI_NAME);

        lvOptions.addOption(OPTION_EFN_CODE, true, OPTION_EFN_NAME);
        lvOptions.addOption(OPTION_EFNALL_CODE, false, OPTION_EFNALL_NAME);

        for (int i = 0; i < 10; i++) {
            lvOptions.addOption(OPTION_FN_CODE + i, true, OPTION_FN_NAME);
            lvOptions.addOption(OPTION_FN_CODE + i + Global.FILE_TYPE_EXCEL,
                    false, OPTION_FNEXCEL_NAME);
            lvOptions.addOption(OPTION_FN_CODE + i + Global.FILE_TYPE_XML,
                    false, OPTION_FNXML_NAME);
        }

        lvOptions.addOption(OPTION_FS_CODE, true, OPTION_FS_NAME);

        lvOptions.addOption(OPTION_IDN_CODE, true, OPTION_IDN_NAME);
        lvOptions.addOption(OPTION_IGN1_CODE, false, OPTION_IGN1_NAME);

        lvOptions.addOption(OPTION_MODE_CODE, true, OPTION_MODE_NAME);

        lvOptions.addOption(OPTION_OBJ_CODE, true, OPTION_OBJ_NAME);
        lvOptions.addOption(OPTION_ODN_CODE, true, OPTION_ODN_NAME);

        lvOptions.addOption(OPTION_PF_CODE, true, OPTION_PF_NAME);
        lvOptions.addOption(OPTION_PFXML_CODE, false, OPTION_PFXML_NAME);
        lvOptions.addOption(OPTION_PREC_CODE, true, OPTION_PREC_NAME);

        lvOptions.addOption(OPTION_TQP_CODE, true, OPTION_TQP_NAME);
        lvOptions.addOption(OPTION_TS_CODE, true, OPTION_TS_NAME);

        lvOptions.addOption(OPTION_VERB_CODE, false, OPTION_VERB_NAME);

        return lvOptions;
    }

    private static Calibration determineCalibration() {

        if ("".equals(argumentPf)) {
            return new Calibration();
        }

        return new Calibration(argumentPf, isPfxml);
    }

    private static Database determineDatabase(final CommandLine parCmdLine) {

        if (parCmdLine.hasOption(OPTION_PF_CODE)) {
            return new Database(parCmdLine.getOptionValue(OPTION_PF_CODE),
                    parCmdLine.hasOption(OPTION_PFXML_CODE));
        }

        return new Database();
    }

    private static Trial determineTrial(final CommandLine parCmdLine) {

        if (parCmdLine.hasOption(OPTION_PF_CODE)) {
            return new Trial(parCmdLine.getOptionValue(OPTION_PF_CODE),
                    parCmdLine.hasOption(OPTION_PFXML_CODE));
        }

        return new Trial();
    }

    private static int execute(final CommandLine parCmdLine) {

        if (OPTION_MODE_CALIBRATION.equals(argumentMode)) {
            return executeCalibration();
        }

        if (OPTION_MODE_MASTER.equals(argumentMode)
                || OPTION_MODE_TEST.equals(argumentMode)) {
            return executeDatabase(parCmdLine);
        }

        if (OPTION_MODE_RESULT_CN.equals(argumentMode)) {
            return executeResultCalibration();
        }

        if (OPTION_MODE_RESULT_R.equals(argumentMode)) {
            return executeResultR();
        }

        if (OPTION_MODE_RESULT_TL.equals(argumentMode)) {
            return executeResultTrial(parCmdLine);
        }

        if (OPTION_MODE_TRIAL.equals(argumentMode)) {
            return executeTrial(parCmdLine);
        }

        LOGGER.log(Level.SEVERE, OPTION + OPTION_MODE_CODE
                + ": mode not yet implemented");

        return STATUS_ERROR;
    }

    private static int executeCalibration() {

        if (OBJECT_TYPE_NANOTIME.equals(argumentObj)) {
            if (!determineCalibration().calibrateSimpleMethod(argumentObj,
                    argumentCyc, argumentDi, argumentDes, isIgn1, argumentPrec,
                    isVerbose)) {
                return STATUS_ERROR;
            }
        } else if (OBJECT_TYPE_QUERY.equals(argumentObj)) {
            if (!determineCalibration().calibrateQuery(argumentTqp,
                    argumentCyc, argumentDi, argumentDes, isExalt, isExcon,
                    argumentFs, isIgn1, argumentPrec, isVerbose)) {
                return STATUS_ERROR;
            }
        } else {
            LOGGER.log(Level.SEVERE, OPTION + OPTION_OBJ_CODE
                    + ": not yet implemented");
            return STATUS_ERROR;
        }

        return STATUS_OK;
    }

    private static int executeDatabase(final CommandLine parCmdLine) {

        database = determineDatabase(parCmdLine);

        // Process a file with DDL or DML statements (-fn9 xxx -fn9xml) ********
        for (int i = 0; i < 10; i++) {

            if ("".equals(argumentFn[i])) {
                continue;
            }

            if (OPTION_MODE_MASTER.equals(argumentMode)) {
                if (isFnxls[i]) {
                    if (!database.modifyMasterDatabase(argumentFn[i],
                            Global.FILE_TYPE_EXCEL)) {
                        return STATUS_ERROR;
                    }
                } else if (isFnxml[i]) {
                    if (!database.modifyMasterDatabase(argumentFn[i],
                            Global.FILE_TYPE_XML)) {
                        return STATUS_ERROR;
                    }
                } else {
                    if (!database.modifyMasterDatabase(argumentFn[i], "")) {
                        return STATUS_ERROR;
                    }
                }
            } else {
                if (isFnxls[i]) {
                    if (!database.modifyTestDatabase(argumentDi, argumentFn[i],
                            Global.FILE_TYPE_EXCEL)) {
                        return STATUS_ERROR;
                    }
                } else if (isFnxml[i]) {
                    if (!database.modifyTestDatabase(argumentDi, argumentFn[i],
                            Global.FILE_TYPE_XML)) {
                        return STATUS_ERROR;
                    }
                } else {
                    if (!database.modifyTestDatabase(argumentDi, argumentFn[i],
                            "")) {
                        return STATUS_ERROR;
                    }
                }
            }
        }

        return STATUS_OK;
    }

    private static int executeResultCalibration() {

        if (!determineCalibration().calibrationDataToExcel(argumentEfn)) {
            return STATUS_ERROR;
        }

        return STATUS_OK;
    }

    private static int executeResultR() {

        if (!determineCalibration().calibrationDataToR(argumentIdn)) {
            return STATUS_ERROR;
        }

        return STATUS_OK;
    }

    private static int executeResultTrial(final CommandLine parCmdLine) {

        if (!determineTrial(parCmdLine).trialDataToExcel(argumentEfn, isEfnall)) {
            return STATUS_ERROR;
        }

        return STATUS_OK;
    }

    private static int executeTrial(final CommandLine parCmdLine) {

        if (!determineTrial(parCmdLine).runTrial(argumentDi, argumentTs,
                argumentDes, argumentFs, argumentCyc, argumentPrec)) {
            return STATUS_ERROR;
        }

        return STATUS_OK;
    }

    /**
     * Command line interface to the REPSI tool. The interface is based on the
     * Jakarta Commons CLI library which simplifies the handling of command line
     * parameters. The main parameter is called <code>mode</code> and this
     * parameter determines the functionality to be executed:
     * <ul>
     * <li><code>calibration</code> perform a calibration run.</li>
     * <li><code>master</code> modify the schema or instance of the master
     * database.</li>
     * <li><code>result_cn</code> export the results of a calibration run
     * into an Excel file.</li>
     * <li><code>result_r</code> export the results of a calibration run into
     * a R compatible files.</li>
     * <li><code>result_tl</code> export the results of one or all trial runs
     * into an Excel file.</li>
     * <li><code>test modify</code> the schema or instance of a test
     * database.</li>
     * <li>t<code>rial</code> perform a trial run.</li>
     * </ul>
     * <br>
     * Further details can be found in the document 'REPSI Tool User Manual'.
     * 
     * @param args here not supported
     * 
     */
    public static void main(final String[] args) {

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.entering(Main.class.getName(), "main", args);
        }

        int lvStatus = STATUS_OK;

        resetOptionsAndArguments();

        // Create and initialise an Options object *****************************
        final Options lvOptions = defineCmdLineOptions();

        // Create the parser and parse the command line arguments **************
        CommandLine parCmdLine = null;

        try {
            // Parse the command line arguments
            parCmdLine = new GnuParser().parse(lvOptions, args);

            lvStatus += checkCmdLineOptions(parCmdLine);

            if (lvStatus == STATUS_OK) {
                lvStatus += execute(parCmdLine);
            }

        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE,
                    "Parsing of the command line arguments failed", e);
            lvStatus++;
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.exiting(Main.class.getName(), "main", Integer
                    .valueOf(lvStatus));
        }

        if (lvStatus == STATUS_OK) {
            LOGGER.log(Level.INFO, "Task completed successfully");
        } else {
            LOGGER.log(Level.SEVERE, "Task due to errors aborted");
        }

        System.exit(lvStatus);
    }

    private static void resetOptionsAndArguments() {

        argumentCyc = 1;
        argumentDi = 0;
        argumentDes = "n/a";
        argumentEfn = "";
        argumentFn = new String[] { "", "", "", "", "", "", "", "", "", "", };
        argumentFs = 10;
        argumentMode = "";
        argumentObj = "";
        argumentPf = "";
        argumentPrec = 1L;
        argumentTqp = 0;
        argumentTs = 0;

        isEfnall = false;
        isExalt = false;
        isFnxls =
                new boolean[] { false, false, false, false, false, false,
                        false, false, false, false, };
        isFnxml =
                new boolean[] { false, false, false, false, false, false,
                        false, false, false, false, };
        isExcon = false;
        isIgn1 = false;
        isPfxml = false;
        isVerbose = false;
    }
}
