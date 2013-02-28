package org.agmip.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static org.agmip.common.Functions.*;
import org.agmip.common.Functions.CompareMode;
import org.agmip.util.MapUtil;
import static org.agmip.util.MapUtil.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide static functions for soil data handling
 *
 * @author Meng Zhang
 */
public class SoilHelper {

    private static final Logger LOG = LoggerFactory.getLogger(SoilHelper.class);

    /**
     * Calculate root growth factor (0-1) for each soil layer
     *
     * @param data The data map
     * @param var The variable name that will be used for storing the result
     * @param m Maximum value in the top PP cm of soil (units depend on
     * variable)
     * @param pp depth of top of curve (pivot point) (cm)
     * @param rd maximum rooting depth (cm), or depth at which the value is 2%
     * of the maximum value
     *
     * @return An {@code ArrayList} of {@code root distribution} for each layer
     * of given soil
     */
    public static HashMap<String, ArrayList<String>> getRootDistribution(HashMap data, String var, String m, String pp, String rd) {

        String[] sllbs;
        String k;
//        double[] dSllbs;
//        double mid;
//        double dPp;
//        double dRd;
//        double dM;
//        double dK;
        HashMap<String, ArrayList<String>> results = new HashMap<String, ArrayList<String>>();
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<HashMap<String, String>> soilLayers = MapUtil.getBucket(data, "soil").getDataList();

        if (soilLayers == null) {
            return results;
        } else if (soilLayers.isEmpty()) {
            LOG.error("----  SOIL LAYER DATA IS EMPTY");
            return results;
        } else {
            try {
//                dPp = Double.parseDouble(pp);
//                dRd = Double.parseDouble(rd);
//                dM = Double.parseDouble(m);
//                dSllbs = new double[soilLayers.size()];
                sllbs = new String[soilLayers.size()];
//                dK = Math.log(0.02) / (dRd - dPp);
                k = divide(Math.log(0.02) + "", substract(rd, pp));
                for (int i = 0; i < soilLayers.size(); i++) {
                    sllbs[i] = soilLayers.get(i).get("sllb");
//                    dSllbs[i] = Double.parseDouble(getObjectOr(soilLayers.get(i), "sllb", "").toString());
                }
            } catch (NumberFormatException e) {
                LOG.error("INVALID INPUT NUMBER [" + e.getMessage() + "]");
                return results;
            }
        }

        // First layer
//        soilLayers.get(0).put(var, getGrowthFactor(dSllbs[0] / 2, dPp, dK, dM, 3));
        result.add(round(getGrowthFactor(divide(sllbs[0], "2"), pp, k, m), 3));

        // Other layers
        for (int i = 1; i < sllbs.length; i++) {
//            mid = (dSllbs[i] + sllbs[i - 1]) / 2;
//            String slrgf = getGrowthFactor(mid, dPp, dK, dM, 3);
            result.add(round(getGrowthFactor(average(sllbs[i], sllbs[i - 1]), pp, k, m), 3));
        }

        results.put(var, result);
        return results;
    }

    /**
     * soil factors which decline exponentially between PP and RD (units depend
     * on variable, same units as M (Maximum value, will use default value 1)
     *
     * @param mid The mid point value between two layers
     * @param pp depth of top soil, or pivot point of curve (cm)
     * @param k exponential decay rate
     * @param m Maximum value in the top PP cm of soil (units depend on
     * @return The growth factor (0-m)
     */
    protected static String getGrowthFactor(String mid, String pp, String k, String m) {
        if (compare(mid, pp, CompareMode.NOTGREATER)) {
            return m;
        } else {
            return multiply(m, exp(multiply(k, substract(mid, pp))));
        }
    }

    /**
     * soil factors which decline exponentially between PP and RD (units depend
     * on variable, same units as M (Maximum value, will use default value 1)
     *
     * @param mid The mid point value between two layers
     * @param pp depth of top soil, or pivot point of curve (cm)
     * @param k exponential decay rate
     * @return The growth factor (0-1)
     */
    protected static double getGrowthFactor(double mid, double pp, double k) {
        return getGrowthFactor(mid, pp, k, 1f);
    }

    /**
     * soil factors which decline exponentially between PP and RD (units depend
     * on variable, same units as M
     *
     * @param mid The mid point value between two layers
     * @param pp depth of top soil, or pivot point of curve (cm)
     * @param k exponential decay rate
     * @param m Maximum value in the top PP cm of soil (units depend on
     * variable)
     * @return The growth factor (0-M)
     */
    protected static double getGrowthFactor(double mid, double pp, double k, double m) {
        if (mid <= pp) {
            return m;
        } else {
            return m * Math.exp(k * (mid - pp));
        }
    }

    /**
     * soil factors which decline exponentially between PP and RD (units depend
     * on variable, same units as M, the output accuracy will depend on prec
     *
     * @param mid The mid point value between two layers
     * @param pp depth of top soil, or pivot point of curve (cm)
     * @param k exponential decay rate
     * @param m Maximum value in the top PP cm of soil (units depend on
     * variable)
     * @param prec The output decimal precision (0 for no decimal part)
     * @return The growth factor (0-M)
     */
    protected static String getGrowthFactor(double mid, double pp, double k, double m, int prec) {
        prec = prec < 0 ? 0 : prec;
        return String.format("%." + prec + "f", getGrowthFactor(mid, pp, k, m));
    }

    /**
     * soil factors which decline exponentially between PP and RD (units depend
     * on variable, same units as M, the output accuracy will depend on prec
     *
     * @param mid The mid point value between two layers
     * @param pp depth of top soil, or pivot point of curve (cm)
     * @param k exponential decay rate
     * @param prec The output decimal precision (0 for no decimal part)
     * @return The growth factor (0-1)
     */
    protected static String getGrowthFactor(double mid, double pp, double k, int prec) {
        return String.format("%." + prec + "f", getGrowthFactor(mid, pp, k, 1, prec));
    }

    /**
     * Get soil layer data array from data holder. Only get the first soil site.
     *
     * @param data The experiment data holder
     * @return
     */
    protected static ArrayList getSoilLayer(Map data) {
        HashMap soils = (HashMap) getObjectOr(data, "soil", new HashMap());

        if (soils.isEmpty()) {
            LOG.error("SOIL DATA IS EMPTY");
            return null;
        } else {
            return getObjectOr(soils, "soilLayer", new ArrayList());
        }
    }
}
