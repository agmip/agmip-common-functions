package org.agmip.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import static org.agmip.common.Functions.*;
import org.agmip.common.Functions.CompareMode;
import org.agmip.util.MapUtil;
import static org.agmip.util.MapUtil.*;
import org.agmip.util.MapUtil.BucketEntry;
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
        HashMap<String, ArrayList<String>> results = new HashMap<String, ArrayList<String>>();
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<HashMap<String, String>> soilLayers = getSoilLayer(data);

        if (soilLayers == null) {
            return results;
        } else if (soilLayers.isEmpty()) {
            LOG.error("----  SOIL LAYER DATA IS EMPTY");
            return results;
        } else {
            try {
                sllbs = new String[soilLayers.size()];
                k = divide(Math.log(0.02) + "", substract(rd, pp));
                for (int i = 0; i < soilLayers.size(); i++) {
                    sllbs[i] = soilLayers.get(i).get("sllb");
                }
            } catch (NumberFormatException e) {
                LOG.error("INVALID INPUT NUMBER [" + e.getMessage() + "]");
                return results;
            }
        }

        // First layer
        result.add(round(getGrowthFactor(divide(sllbs[0], "2"), pp, k, m), 3));

        // Other layers
        for (int i = 1; i < sllbs.length; i++) {
            result.add(round(getGrowthFactor(average(sllbs[i], sllbs[i - 1]), pp, k, m), 3));
        }

        results.put(var, result);
        return results;
    }

    /**
     * Given a total inorganic N amount for the soil profile, this function
     * distributes the N over the soil layers assuming a constant concentration
     * of NO3 (90%) and NH4 (10%)
     *
     * @param data The data set
     * @param icin Total soil N over the profile (kg[N]/ha)
     * @return Three {@code ArrayList} Corresponded to
     * {@code ICN_TOT, ICNO3 ICNH4} for each layer of given soil
     */
    public static HashMap<String, ArrayList<String>> getIcnDistribution(HashMap data, String icin) {
        HashMap<String, ArrayList<String>> results = new HashMap<String, ArrayList<String>>();
        ArrayList<HashMap<String, String>> soilLayers;
        soilLayers = getSoilLayer(data);

        icin = sum(icin);
        if (icin == null) {
            LOG.error("Input variable ICIN come with invalid  value icin={}", icin);
            return results;
        }

        String lastSllb = "0";
        String[] productSBXTH = new String[soilLayers.size()];
        for (int i = 0; i < soilLayers.size(); i++) {
            HashMap<String, String> soilLayer = soilLayers.get(i);
            String sllb = getValueOr(soilLayer, "sllb", "");
            String slbdm = getValueOr(soilLayer, "slbdm", "");
            String thick = substract(sllb, lastSllb);
            productSBXTH[i] = product(slbdm, thick);
            if (productSBXTH[i] == null) {
                LOG.error("Invalid SLLB and/or SLBDM in the soil layer data with value sllb={}, slbdm={}", sllb, slbdm);
                return results;
            }
            lastSllb = sllb;
        }

        String totalSBXTH = sum(productSBXTH);
        if (compare(totalSBXTH, "0", CompareMode.EQUAL)) {
            LOG.error("Total SLBDM * thick is 0");
            return results;
        }
        String nppm = divide(product(icin, "10"), totalSBXTH);
        String icnh4 = product("0.1", nppm);
        String icno3 = product("0.9", nppm);

        ArrayList<String> icnTotArr = new ArrayList();
        ArrayList<String> icnh4Arr = new ArrayList();
        ArrayList<String> icno3Arr = new ArrayList();

        for (int i = 0; i < productSBXTH.length; i++) {
            String icn_tot = divide(product(productSBXTH[i], icin), totalSBXTH);
            icnTotArr.add(round(icn_tot, 2));
            icnh4Arr.add(round(icnh4, 2));
            icno3Arr.add(round(icno3, 2));
        }

        results.put("icn_tot", icnTotArr);
        results.put("icnh4", icnh4Arr);
        results.put("icno3", icno3Arr);
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
     * Get soil layer data array from data holder.
     *
     * @param data The experiment data holder
     * @return The soil layer data array
     */
    protected static ArrayList getSoilLayer(HashMap data) {
        if (data.containsKey("soil") || !data.containsKey("soilLayer")) {
            return MapUtil.getBucket(data, "soil").getDataList();
        } else {
            return new BucketEntry(data).getDataList();
        }
    }

    /**
     * Splitting the original soil layers into homogeneous layers with soil
     * thicknesses which do not exceed limits of drainage models. The parameters
     * for new layer will depend on the original layers.
     *
     * @param data The experiment data holder
     * @return The soil layer data array with new added layers
     */
    public static ArrayList<HashMap<String, String>> splittingSoillayer(HashMap data, boolean isICLayer) {
        if (isICLayer) {
            return splittingLayers(ExperimentHelper.getICLayer(data), "icbl");
        } else {
            return splittingLayers(getSoilLayer(data), "sllb");
        }
    }

    private static ArrayList<HashMap<String, String>> splittingLayers(ArrayList<HashMap<String, String>> soilLayers, String depthVal) {
        ArrayList<HashMap<String, String>> ret = new ArrayList();
        String lastDepth = "0";
        String curDepth;
        String thickness;
        HashMap<String, String> layer = new HashMap();

        int idx = 0;
        String[] fixedTopLayerDeps = {"5", "15"};
        String[] fixedTopLayerThks = {"5.00", "10.00"};
        for (int i = 0; i < fixedTopLayerDeps.length; i++) {
            if (idx >= soilLayers.size()) {
                break;
            }
            int start = idx;
            ArrayList<String> weights = new ArrayList();
            for (; idx < soilLayers.size(); idx++) {
                layer = soilLayers.get(idx);
                curDepth = MapUtil.getValueOr(layer, depthVal, "");
                if (compare(curDepth, fixedTopLayerDeps[i], CompareMode.NOTLESS)) {
                    thickness = substract(fixedTopLayerDeps[i], lastDepth);
                    weights.add(divide(thickness, fixedTopLayerThks[i]));
                    lastDepth = fixedTopLayerDeps[i];
                    if (compare(curDepth, fixedTopLayerDeps[i], CompareMode.EQUAL)) {
                        idx++;
                    }
                    break;
                } else {
                    thickness = substract(curDepth, lastDepth);
                    weights.add(divide(thickness, fixedTopLayerThks[i]));
                    lastDepth = curDepth;
                }
            }
            
            if (compare(lastDepth, fixedTopLayerDeps[i], CompareMode.LESS)) {
                LOG.warn("The soil layer is deep enough for LYRSET() function!");
                continue;
            }
            
            HashMap newLayer = new HashMap();
            if (weights.size() == 1 && weights.get(0).equals("1")) {
                newLayer.putAll(layer);
            } else {
                HashSet<String> vars = new HashSet();
                vars.addAll(layer.keySet());
                vars.remove(depthVal);
                String val;
                for (String var : vars) {
                    val = "0";
                    for (int j = 0; j < weights.size(); j++, start++) {
                        layer = soilLayers.get(start);
                        val = sum(val, product(layer.get(var), weights.get(j)));
                        if (val == null) {
                            break;
                        }
                    }
                    if (val != null && !val.equals("")) {
                        newLayer.put(var, val);
                    }
                }
            }
            newLayer.put(depthVal, fixedTopLayerDeps[i]);
            ret.add(newLayer);
        }

        for (int i = idx; i < soilLayers.size(); i++, lastDepth = curDepth) {
            layer = soilLayers.get(i);
            curDepth = MapUtil.getValueOr(layer, depthVal, "");
            thickness = substract(curDepth, lastDepth);
            String pt;
//            if (compare(curDepth, "15", CompareMode.NOTGREATER)) {
//                pt = "10";
//            } else
            if (compare(curDepth, "60", CompareMode.NOTGREATER)) { // TODO might change to 90 to match with current DSSAT handling
                pt = "15";
            } else if (compare(curDepth, "200", CompareMode.NOTGREATER)) {
                pt = "30";
            } else {
                pt = "60";
            }
            ret.addAll(createNewLayers(layer, pt, thickness, lastDepth, depthVal));
            ret.add(layer);
        }
        return ret;
    }

    private static ArrayList<HashMap<String, String>> createNewLayers(HashMap layer, String pt, String thickness, String lastDepth, String depthVal) {
        ArrayList<HashMap<String, String>> ret = new ArrayList();
        if (compare(thickness, pt, CompareMode.NOTGREATER)) {
            return ret;
        }
        int newLayerNum = numericStringToBigInteger(divide(thickness, pt, 0)).intValue();
        if (compare(thickness, multiply(pt, newLayerNum + ""), CompareMode.GREATER)) {
            newLayerNum++;
        }
        String increase = divide(thickness, newLayerNum + "", 0);
        for (int i = 0; i < newLayerNum - 1; i++) {
            lastDepth = sum(lastDepth, increase);
            HashMap newLayer = new HashMap();
            newLayer.putAll(layer);
            newLayer.put(depthVal, lastDepth);
            ret.add(newLayer);
        }
        return ret;
    }
}
