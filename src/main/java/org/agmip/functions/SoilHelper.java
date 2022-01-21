package org.agmip.functions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.agmip.common.Functions;
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
     * @param isICLayer True for handling initial condition soil layers
     * @return The soil layer data array with new added layers
     */
    public static ArrayList<HashMap<String, String>> splittingSoillayer(HashMap data, boolean isICLayer) {
        if (isICLayer) {
            return splittingLayers(ExperimentHelper.getICLayer(data), "icbl", "5", "10");
        } else {
            return splittingLayers(getSoilLayer(data), "sllb", "5", "10");
        }
    }

    /**
     * Splitting the original soil layers into homogeneous layers with soil
     * thicknesses which do not exceed limits of drainage models.The parameters
     * for new layer will depend on the original layers.
     * @param data The experiment data holder
     * @param isICLayer True for handling initial condition soil layers
     * @param fstLyrThk
     * @param sndLyrThk
     * @return The soil layer data array with new added layers
     */
    public static ArrayList<HashMap<String, String>> splittingSoillayer(HashMap data, boolean isICLayer, String fstLyrThk, String sndLyrThk) {
        if (isICLayer) {
            return splittingLayers(ExperimentHelper.getICLayer(data), "icbl", fstLyrThk, sndLyrThk);
        } else {
            return splittingLayers(getSoilLayer(data), "sllb", fstLyrThk, sndLyrThk);
        }
    }

    private static ArrayList<HashMap<String, String>> splittingLayers(ArrayList<HashMap<String, String>> soilLayers, String depthVal, String fstLyrThk, String sndLyrThk) {
        ArrayList<HashMap<String, String>> ret = new ArrayList();
        String lastDepth = "0";
        String curDepth;
        String thickness;
        HashMap<String, String> layer = new HashMap();

        int idx = 0;
        String[] fixedTopLayerDeps = {fstLyrThk, Functions.sum(fstLyrThk, sndLyrThk)};
        String[] fixedTopLayerThks = {Functions.round(fstLyrThk, 2), Functions.round(sndLyrThk, 2)};
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
                    for (int j = 0; j < weights.size(); j++) {
                        layer = soilLayers.get(j + start);
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

    /**
     * Calculate the soil layer variables based on other soil parameters.
     * Currently following method is available for calculation:
     * PTSaxton2006 - based on the soil texture and organic matter.
     * More potential ways would be available in the future.
     *
     * @param data The data set
     * @param calcMethod The name of calculation method
     * @param calcVars The list of variables which will be applied with calculation
     * @return The list for calculation results for each variable
     */
    public static HashMap<String, ArrayList<String>> getSoilValsFromOthPara(HashMap data, String calcMethod, ArrayList<String> calcVars) {
        HashMap<String, ArrayList<String>> rets = new HashMap();
        ArrayList<Method> calcMtds;
        ArrayList<String> calcVarsFinal;
        
        if (calcMethod.equalsIgnoreCase("PTSaxton2006")) {
            calcMethod = "PTSaxton2006";
        }
        
        try {
            // Load calculation class
            String packageName = SoilHelper.class.getPackage().getName();
            Class calClass = Class.forName(packageName + "." + calcMethod);
            
            // Load method for each target variable
            calcMtds = new ArrayList();
            calcVarsFinal = new ArrayList();
            if (calcVars.contains("all")) {
                for (Method mtd : calClass.getDeclaredMethods()) {
                    String mtdName = mtd.getName();
                    if (mtdName.startsWith("get")) {
                        calcMtds.add(mtd);
                        String var = mtdName.substring(3).toLowerCase();
                        calcVarsFinal.add(var);
                        rets.put(var, new ArrayList());
                    }
                }
            } else {
                for (String var : calcVars) {
                    try {
                        if (!rets.containsKey(var)) {
                            calcMtds.add(calClass.getDeclaredMethod("get" + var.toUpperCase(), String[].class));
                            calcVarsFinal.add(var.toLowerCase());
                            rets.put(var, new ArrayList());
                        } else {
                            LOG.warn("Variable {} is repeated in the target variable list.", var);
                        }
                    } catch (NoSuchMethodException e) {
                        LOG.error("PT Calculation for {} is not valiable in {} method", var, calcMethod);
                    }
                }
            }
            
            // Invoke the calculation for each soil layer
            ArrayList<HashMap<String, String>> layers = getSoilLayer(data);
            for (HashMap<String, String> layer : layers) {
                String[] vals = new String[0];
                if (calcMethod.equals("PTSaxton2006")) {
                    String sand = getValueOr(layer, "slsnd", "");
                    String clay = getValueOr(layer, "slcly", "");
                    String om = getValueOr(layer, "slni", "");
                    String grave = getValueOr(layer, "slcf", "0");
                    if ("".equals(sand)) {
                        sand = substract("100", clay, getValueOr(layer, "slsil", ""));
                    }
                    if ("".equals(om)) {
                        om = product(getValueOr(layer, "sloc", ""), "1.72");
                    }
                    if (om == null || sand == null || clay.equals("")) {
                        LOG.warn("Invilid soil texture and organic matter data, PT calculation will skip this soil layer");
                        continue;
                    }
                    vals = new String[]{sand, clay, om, grave};
                }
                for (int i = 0; i < calcVarsFinal.size(); i++) {
                    String var = calcVarsFinal.get(i);
                    try {
                        String val;
                        val = (String) calcMtds.get(i).invoke(calClass, new Object[]{vals});
                        rets.get(var).add(val);
                    } catch (Exception e) {
                        LOG.error(Functions.getStackTrace(e));
                    }
                }
            }
            
        } catch (ClassNotFoundException e) {
            LOG.error("{} is not a valid name for PT calculation method", calcMethod);
            return rets;
        }

        return rets;
    }
    
    public static HashMap<String, ArrayList<String>> reduceWP(HashMap data, String rate) {
        HashMap<String, ArrayList<String>> results = new HashMap<String, ArrayList<String>>();
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<HashMap> layers = getSoilLayer(data);
        for (HashMap layer : layers) {
            String slll = MapUtil.getValueOr(layer, "slll", "");
            String sldul = MapUtil.getValueOr(layer, "sldul", "");
            rate = Functions.divide(rate, "100");
            slll = Functions.substract(slll, Functions.multiply(rate, Functions.substract(sldul, slll)));
            if (slll == null || slll.equals("")) {
                LOG.error("reduceWP function failed for soil data " + MapUtil.getValueOr(data, "soil_id", "[Unkonwn]"));
            }
            result.add(slll);
        }
        results.put("slll", result);
        return results;
    }
}