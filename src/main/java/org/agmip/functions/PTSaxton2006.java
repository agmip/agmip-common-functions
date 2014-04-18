package org.agmip.functions;

import java.util.HashMap;
import static org.agmip.common.Functions.*;
import org.agmip.common.Functions.CompareMode;
import static org.agmip.util.MapUtil.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementations for the equations in the paper of 2006 PT transfer
 * calculation. Method naming rule: All getXXX method will be used in DOME
 * function call by reflection. All calcXXX method is used for implementing the
 * equation from corresponding paper.
 *
 * @author Meng Zhang
 */
public class PTSaxton2006 { //implements PTCalculatorDOMEIF {

    private static final Logger LOG = LoggerFactory.getLogger(PTSaxton2006.class);
    private static final String CONST_LN1500_LN33 = substract(log("1500"), log("33"));

    /**
     * For calculating SLLL
     *
     * @param soilParas should include 1. Sand weight percentage by layer
     * ([0,100]%), 2. Clay weight percentage by layer ([0,100]%), 3. Organic
     * matter weight percentage by layer ([0,100]%), (= SLOC * 1.72)
     * @return Soil water, lower limit, fraction
     */
    public static String getSLLL(String[] soilParas) {
        if (soilParas != null && soilParas.length >= 3) {
            return divide(calcMoisture1500Kpa(soilParas[0], soilParas[1], soilParas[2]), "100", 3);
        } else {
            return null;
        }
    }

    /**
     * For calculating SLDUL
     *
     * @param soilParas should include 1. Sand weight percentage by layer
     * ([0,100]%), 2. Clay weight percentage by layer ([0,100]%), 3. Organic
     * matter weight percentage by layer ([0,100]%), (= SLOC * 1.72)
     * @return Soil water, drained upper limit, fraction
     */
    public static String getSLDUL(String[] soilParas) {
        if (soilParas != null && soilParas.length >= 3) {
            return divide(calcMoisture33Kpa(soilParas[0], soilParas[1], soilParas[2]), "100", 3);
        } else {
            return null;
        }
    }

    /**
     * For calculating SLSAT
     *
     * @param soilParas should include 1. Sand weight percentage by layer
     * ([0,100]%), 2. Clay weight percentage by layer ([0,100]%), 3. Organic
     * matter weight percentage by layer ([0,100]%), (= SLOC * 1.72)
     * @return Soil water, saturated, fraction
     */
    public static String getSLSAT(String[] soilParas) {
        if (soilParas != null && soilParas.length >= 3) {
            return divide(calcSaturatedMoisture(soilParas[0], soilParas[1], soilParas[2]), "100", 3);
        } else {
            return null;
        }
    }

    /**
     * For calculating SKSAT
     *
     * @param soilParas should include 1. Sand weight percentage by layer
     * ([0,100]%), 2. Clay weight percentage by layer ([0,100]%), 3. Organic
     * matter weight percentage by layer ([0,100]%), (= SLOC * 1.72), 4. Grave
     * weight percentage by layer ([0,100]%)
     * @return Saturated hydraulic conductivity, cm/h
     */
    public static String getSKSAT(String[] soilParas) {
        if (soilParas != null && soilParas.length >= 3) {
            if (soilParas.length >= 4) {
                return divide(calcSatBulk(soilParas[0], soilParas[1], soilParas[2], divide(soilParas[3], "100")), "10", 3);
            } else {
                return divide(calcSatMatric(soilParas[0], soilParas[1], soilParas[2]), "10", 3);
            }
        } else {
            return null;
        }
    }

    /**
     * For calculating SLBDM
     *
     * @param soilParas should include 1. Sand weight percentage by layer
     * ([0,100]%), 2. Clay weight percentage by layer ([0,100]%), 3. Organic
     * matter weight percentage by layer ([0,100]%), (= SLOC * 1.72), 4. Grave
     * weight percentage by layer ([0,100]%)
     * @return Soil bulk density when moist for layer, g/cm3
     */
    public static String getSLBDM(String[] soilParas) {
        if (soilParas != null && soilParas.length >= 3) {
            if (soilParas.length >= 4) {
                return round(calcGravePlusDensity(soilParas[0], soilParas[1], soilParas[2], divide(soilParas[3], "100")), 2);
            } else {
                return round(calcNormalDensity(soilParas[0], soilParas[1], soilParas[2]), 2);
            }
        } else {
            return null;
        }
    }

    /**
     * Equation 1 for calculating 1500 kPa moisture, %v
     *
     * @param slsnd Sand weight percentage by layer ([0,100]%)
     * @param slcly Clay weight percentage by layer ([0,100]%)
     * @param omPct Organic matter weight percentage by layer ([0,100]%), (=
     * SLOC * 1.72)
     */
    public static String calcMoisture1500Kpa(String slsnd, String slcly, String omPct) {

        if ((slsnd = checkPctVal(slsnd)) == null
                || (slcly = checkPctVal(slcly)) == null
                || (omPct = checkPctVal(omPct)) == null) {
            LOG.error("Invalid input parameters for calculating 1500 kPa moisture, %v");
            return null;
        }
        String ret = sum(product("-0.02736", slsnd), product("0.55518", slcly), product("0.684", omPct),
                product("0.0057", slsnd, omPct), product("-0.01482", slcly, omPct), product("0.0007752", slsnd, slcly), "1.534");
        LOG.debug("Calculate result for 1500 kPa moisture, %v is {}", ret);

        return ret;
    }

    /**
     * Equation 2 for 33 kPa moisture, normal density, %v
     *
     * @param slsnd Sand weight percentage by layer ([0,100]%)
     * @param slcly Clay weight percentage by layer ([0,100]%)
     * @param omPct Organic matter weight percentage by layer ([0,100]%), (=
     * SLOC * 1.72)
     */
    public static String calcMoisture33Kpa(String slsnd, String slcly, String omPct) {

        String ret;
        if ((slsnd = checkPctVal(slsnd)) == null
                || (slcly = checkPctVal(slcly)) == null
                || (omPct = checkPctVal(omPct)) == null) {
            LOG.error("Invalid input parameters for calculating 33 kPa moisture, normal density, %v");
            return null;
        }
        String mt33Fst = calcMoisture33KpaFst(slsnd, slcly, omPct);
        ret = sum(product(pow(mt33Fst, "2"), "0.01283"), product(mt33Fst, "0.626"), "-1.5");
        LOG.debug("Calculate result for 33 kPa moisture, normal density, %v is {}", ret);

        return ret;
    }

    /**
     * Equation 2 for calculating 33 kPa moisture, first solution, %v
     *
     * @param slsnd Sand weight percentage by layer ([0,100]%)
     * @param slcly Clay weight percentage by layer ([0,100]%)
     * @param omPct Organic matter weight percentage by layer ([0,100]%), (=
     * SLOC * 1.72)
     */
    private static String calcMoisture33KpaFst(String slsnd, String slcly, String omPct) {

        String ret = sum(product("-0.251", slsnd), product("0.195", slcly), product("1.1", omPct),
                product("0.006", slsnd, omPct), product("-0.027", slcly, omPct), product("0.00452", slsnd, slcly), "29.9");
        LOG.debug("Calculate result for 33 kPa moisture, first solution, %v is {}", ret);
        return ret;
    }

    /**
     * Equation 3 for SAT-33 kPa moisture, normal density, %v
     *
     * @param slsnd Sand weight percentage by layer ([0,100]%)
     * @param slcly Clay weight percentage by layer ([0,100]%)
     * @param omPct Organic matter weight percentage by layer ([0,100]%), (=
     * SLOC * 1.72)
     */
    public static String calcMoistureSAT33Kpa(String slsnd, String slcly, String omPct) {

        if ((slsnd = checkPctVal(slsnd)) == null
                || (slcly = checkPctVal(slcly)) == null
                || (omPct = checkPctVal(omPct)) == null) {
            LOG.error("Invalid input parameters for calculating 33 kPa moisture, normal density, %v");
            return null;
        }
        String ret = sum(product("0.45481", slsnd), product("0.055624", slcly), product("3.5992", omPct),
                product("-0.029448", slsnd, omPct), product("-0.044172", slcly, omPct), product("-0.00955424", slsnd, slcly), "2.0608");
        LOG.debug("Calculate result for 33 kPa moisture, normal density, %v is {}", ret);

        return ret;
    }

    /**
     * Equation 5 for calculating Saturated moisture (0 kPa), normal density, %v
     *
     * @param slsnd Sand weight percentage by layer ([0,100]%)
     * @param slcly Clay weight percentage by layer ([0,100]%)
     * @param omPct Organic matter weight percentage by layer ([0,100]%), (=
     * SLOC * 1.72)
     */
    public static String calcSaturatedMoisture(String slsnd, String slcly, String omPct) {

        String mt33 = calcMoisture33Kpa(slsnd, slcly, omPct);
        String mtSAT33 = calcMoistureSAT33Kpa(slsnd, slcly, omPct);
        String ret = sum(mt33, mtSAT33, product(slsnd, "-0.097"), "4.3");
        LOG.debug("Calculate result for Saturated moisture (0 kPa), normal density, %v is {}", ret);

        return ret;
    }

    /**
     * Equation 6 for calculating Normal density, g/cm-3
     *
     * @param slsnd Sand weight percentage by layer ([0,100]%)
     * @param slcly Clay weight percentage by layer ([0,100]%)
     * @param omPct Organic matter weight percentage by layer ([0,100]%), (=
     * SLOC * 1.72)
     */
    public static String calcNormalDensity(String slsnd, String slcly, String omPct) {

        String satMt = calcSaturatedMoisture(slsnd, slcly, omPct);
        String ret = product(substract("100", satMt), "0.0265");
        LOG.debug("Calculate result for Normal density, g/cm-3 is {}", ret);

        return ret;
    }

    /**
     * Equation 7 for calculating Adjusted density, g/cm-3
     *
     * @param slsnd Sand weight percentage by layer ([0,100]%)
     * @param slcly Clay weight percentage by layer ([0,100]%)
     * @param omPct Organic matter weight percentage by layer ([0,100]%), (=
     * SLOC * 1.72)
     * @param df Density adjustment Factor (0.9–1.3)
     */
    public static String calcAdjustedDensity(String slsnd, String slcly, String omPct, String df) {

        if (compare(df, "0.9", CompareMode.NOTLESS) && compare(df, "1.3", CompareMode.NOTGREATER)) {
            String normalDensity = calcNormalDensity(slsnd, slcly, omPct);
            String ret = product(normalDensity, df);
            LOG.debug("Calculate result for Adjusted density, g/cm-3 is {}", ret);
            return ret;
        } else {
            LOG.error("Density adjustment Factor is out of range (0.9 - 1.3) : {}", df);
            return null;
        }
    }

    /**
     * Equation 8 for calculating Saturated moisture (0 kPa), adjusted density,
     * ([0,100]%)
     *
     * @param slsnd Sand weight percentage by layer ([0,100]%)
     * @param slcly Clay weight percentage by layer ([0,100]%)
     * @param omPct Organic matter weight percentage by layer ([0,100]%), (=
     * SLOC * 1.72)
     * @param df Density adjustment Factor (0.9–1.3)
     */
    public static String calcAdjustedSaturatedMoisture(String slsnd, String slcly, String omPct, String df) {

        String adjDensity = calcAdjustedDensity(slsnd, slcly, omPct, df);
        String ret = substract("100", divide(adjDensity, "0.0265"));
        LOG.debug("Calculate result for Saturated moisture (0 kPa), adjusted density, ([0,100]%) is {}", ret);
        return ret;
    }

    /**
     * Equation 9 for calculating 33 kPa moisture, adjusted density,([0,100]%)
     *
     * @param slsnd Sand weight percentage by layer ([0,100]%)
     * @param slcly Clay weight percentage by layer ([0,100]%)
     * @param omPct Organic matter weight percentage by layer ([0,100]%), (=
     * SLOC * 1.72)
     * @param df Density adjustment Factor (0.9–1.3)
     */
    public static String calcAdjustedMoisture33Kpa(String slsnd, String slcly, String omPct, String df) {

        String mt33 = calcMoisture33Kpa(slsnd, slcly, omPct);
        String satMt = calcSaturatedMoisture(slsnd, slcly, omPct);
        String adjSatMt = calcAdjustedSaturatedMoisture(slsnd, slcly, omPct, df);
        String ret = substract(mt33, product(substract(satMt, adjSatMt), "0.2"));
        LOG.debug("Calculate result for 33 kPa moisture, adjusted density, ([0,100]%) is {}", ret);
        return ret;
    }

    /**
     * Equation 10 for calculating SAT-33 kPa moisture, adjusted
     * density,([0,100]%)
     *
     * @param slsnd Sand weight percentage by layer ([0,100]%)
     * @param slcly Clay weight percentage by layer ([0,100]%)
     * @param omPct Organic matter weight percentage by layer ([0,100]%), (=
     * SLOC * 1.72)
     * @param df Density adjustment Factor (0.9–1.3)
     */
    public static String calcAdjustedMoistureSAT33Kpa(String slsnd, String slcly, String omPct, String df) {

        String adjSatMt = calcAdjustedSaturatedMoisture(slsnd, slcly, omPct, df);
        String adjMt33 = calcAdjustedMoisture33Kpa(slsnd, slcly, omPct, df);
        String ret = substract(adjSatMt, adjMt33);
        LOG.debug("Calculate result for SAT-33 kPa moisture, adjusted, ([0,100]%) is {}", ret);
        return ret;
    }

    /**
     * Equation 15 for calculating Slope of logarithmic tension-moisture curve
     *
     * @param slsnd Sand weight percentage by layer ([0,100]%)
     * @param slcly Clay weight percentage by layer ([0,100]%)
     * @param omPct Organic matter weight percentage by layer ([0,100]%), (=
     * SLOC * 1.72)
     */
    private static String calcLamda(String slsnd, String slcly, String omPct) {

        String mt33 = divide(calcMoisture33Kpa(slsnd, slcly, omPct), "100");
        String mt1500 = divide(calcMoisture1500Kpa(slsnd, slcly, omPct), "100");
        String ret = divide(substract(log(mt33), log(mt1500)), CONST_LN1500_LN33);
        LOG.debug("Calculate result for Slope of logarithmic tension-moisture curve is {}", ret);
        return ret;
    }

    /**
     * Equation 16 for calculating Saturated conductivity (matric soil), mm/h
     *
     * @param slsnd Sand weight percentage by layer ([0,100]%)
     * @param slcly Clay weight percentage by layer ([0,100]%)
     * @param omPct Organic matter weight percentage by layer ([0,100]%), (=
     * SLOC * 1.72)
     */
    public static String calcSatMatric(String slsnd, String slcly, String omPct) {

        String satMt = divide(calcSaturatedMoisture(slsnd, slcly, omPct), "100");
        String mt33 = divide(calcMoisture33Kpa(slsnd, slcly, omPct), "100");
        String lamda = calcLamda(slsnd, slcly, omPct);
        String ret = product("1930", pow(substract(satMt, mt33), substract("3", lamda)));
        LOG.debug("Calculate result for Saturated conductivity (matric soil), mm/h is {}", ret);
        return ret;
    }

    /**
     * Equation 20 for calculating Bulk soil density (matric plus gravel),
     * g/cm-3
     *
     * @param slsnd Sand weight percentage by layer ([0,100]%)
     * @param slcly Clay weight percentage by layer ([0,100]%)
     * @param omPct Organic matter weight percentage by layer ([0,100]%), (=
     * SLOC * 1.72)
     * @param slcf Grave weight fraction by layer (g/g)
     */
    public static String calcGravePlusDensity(String slsnd, String slcly, String omPct, String slcf) {

        String slbdm = calcNormalDensity(slsnd, slcly, omPct);
        String alpha = divide(slbdm, "2.65");
        String slcfv = divide(product(alpha, slcf), substract("1", slcf, product("-1", slcf, alpha)));
        String ret = sum(product(slbdm, substract("1", slcfv)), product(slcfv, "2.65"));
        LOG.debug("Calculate result for Bulk soil density (matric plus gravel), g/cm-3 is {}", ret);

        return ret;
    }

    /**
     * Equation 22 for calculating Saturated conductivity (bulk soil), mm/h
     *
     * @param slsnd Sand weight percentage by layer ([0,100]%)
     * @param slcly Clay weight percentage by layer ([0,100]%)
     * @param omPct Organic matter weight percentage by layer ([0,100]%), (=
     * SLOC * 1.72)
     * @param slcf Grave weight fraction by layer (g/g)
     */
    public static String calcSatBulk(String slsnd, String slcly, String omPct, String slcf) {

        String satMatric = calcSatMatric(slsnd, slcly, omPct);
        String slbdm = calcNormalDensity(slsnd, slcly, omPct);
        String alpha = divide(slbdm, "2.65");
        String ratio = divide(substract("1", slcf), substract("1", slcf, product("-1.5", slcf, alpha)));
        String ret = product(satMatric, ratio);
        LOG.debug("Calculate result for Saturated conductivity (bulk soil), mm/h is {}", ret);

        return ret;
    }
}
