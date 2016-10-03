package adjustments;

import com.rits.cloning.Cloner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.agmip.common.Functions;
import org.agmip.util.JSONAdapter;
import org.agmip.util.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Meng Zhang
 */
public class WeatherAdjustAdaptor {
    
    private static final Logger LOG = LoggerFactory.getLogger(WeatherAdjustAdaptor.class);

    private final static HashMap<String, HashMap> wthMap = new HashMap();
    private final static HashMap<String, String> expWthMap = new HashMap();
    private final static HashMap<String, ArrayList<HashMap>> wthAdjMap = new HashMap();
    
    public WeatherAdjustAdaptor(Map data) {
        
        Collection<String> wstIds = expWthMap.values();
        ArrayList<HashMap<String, Object>> flattenedData = MapUtil.flatPack((HashMap) data);
        for (HashMap exp : flattenedData) {
            String exname = MapUtil.getValueOr(exp, "exname", "");
            String wstId = MapUtil.getValueOr(exp, "wst_id", "");
            wthMap.put(wstId, MapUtil.getObjectOr(exp, "weather", new HashMap()));
            ArrayList<HashMap> adjustmengs = MapUtil.getObjectOr(exp, "adjustments", new ArrayList());
            if (!adjustmengs.isEmpty()) {
                String newWstId = null;
                ArrayList<HashMap> adjArr = MapUtil.getObjectOr(wthAdjMap, wstId, new ArrayList());
                if (adjArr.isEmpty()) {
                    wthAdjMap.put(wstId, adjArr);
                }
                
                for (HashMap adjData : adjArr) {
                    ArrayList<HashMap> adjustmengs2 = MapUtil.getObjectOr(adjData, "adjustments", new ArrayList());
                    if (adjustmengs2.equals(adjustmengs)) {
                        newWstId = MapUtil.getValueOr(adjData, "new_wst_id", "");
                        break;
                    }
                }
                
                if (newWstId == null) {
                    int count = 1;
                    newWstId = wstId + "_" + count;
                    while(wstIds.contains(newWstId)) {
                        count++;
                        newWstId = wstId + "_" + count;
                    }
                    HashMap tmp = new HashMap();
                    adjArr.add(tmp);
                    tmp.put("new_wst_id", newWstId);
                    tmp.put("adjustments", adjustmengs);
                }
                expWthMap.put(exname, newWstId);
                
            }
        }
        
    }
    
    public static void init() {
        wthMap.clear();
        expWthMap.clear();
        wthAdjMap.clear();
    }
    
    public boolean hasAdjustments() {
        return !expWthMap.isEmpty();
    }
    
    public String getAdjustedWstId(String exname) {
        return expWthMap.get(exname);
    }
    
    public Iterator<WeatherAdjustment> getIterator(final String wstId) {
        
        Iterator<WeatherAdjustment> ret = new Iterator<WeatherAdjustment>() {

            private final ArrayList<HashMap> adjArr = MapUtil.getObjectOr(wthAdjMap, wstId, new ArrayList());
            private int curIdx = 0;
            
            @Override
            public boolean hasNext() {
                return curIdx < adjArr.size();
            }

            @Override
            public WeatherAdjustment next() {
                Cloner c = new Cloner();
                HashMap orgWthData = c.deepClone(wthMap.get(wstId));
                
                WeatherAdjustment ret = new WeatherAdjustment(orgWthData, adjArr.get(curIdx));
                curIdx++;
                return ret;
            }

            @Override
            public void remove() {
                curIdx++;
            }
        };
        
        return ret;
    }
    
    public class WeatherAdjustment {
        
        private final HashMap wthData;
        private final HashMap adjustment;
        private final ArrayList<HashMap<String, String>> adjArr;
        private int curIdx = 0;
        private final List<String> supportVars = Arrays.asList(new String[]{"tmax", "tmin", "srad", "wind", "rain", "co2y", "tdew"});
        
        public WeatherAdjustment(HashMap wthData, HashMap adjustment) {
            this.wthData = wthData;
            this.adjustment = adjustment;
            this.adjArr = MapUtil.getObjectOr(adjustment, "adjustments", new ArrayList());
            
            while (hasNextWthAdjRule()) {
                HashMap<String, String> rule = adjArr.get(curIdx);
                
                // TODO Add support for date offset in the future
                String var = MapUtil.getValueOr(rule, "variable", "");
                String adjVal = MapUtil.getValueOr(rule, "value", "");
                String method = MapUtil.getValueOr(rule, "method", "");
                
                if (var.equalsIgnoreCase("co2y")) {
                    adjustSingleValue(this.wthData, var, adjVal, method);
                } else {
                    adjustListValue(var, adjVal, method);
                }
                curIdx++;
            }
            this.wthData.put("wst_id", getNewWstId());
        }
        
        public final String getNewWstId() {
            return MapUtil.getValueOr(adjustment, "new_wst_id", "");
        }
        
        public HashMap getAdjustedWthData() {
            return this.wthData;
        }
        
        public String getAdjustedWthJson() throws IOException {
            return JSONAdapter.toJSON(this.wthData);
        }
        
        private boolean hasNextWthAdjRule() {
            
            while (curIdx < adjArr.size()) {
                HashMap<String, String> adj = adjArr.get(curIdx);
                String var = MapUtil.getValueOr(adj, "variable", "");
                if (supportVars.contains(var)) {
                    return true;
                } else {
                    LOG.warn("Found unsupported adjust variable [" + var + "], this rule will be ignored");
                }
                curIdx++;
            }
            
            return false;
        }
        
        private void adjustSingleValue(HashMap data, String var, String adjVal, String method) {
            
            String val = MapUtil.getValueOr(data, var, "");
            
            if (val.equals("")) {
                if (method.equals("substitute")) {
                    data.put(var, adjVal);
                }
                return;
            }
            
            if (method.equals("multiply")) {
                data.put(var, Functions.multiply(val, adjVal));
            } else if (method.equals("delta")) {
                data.put(var, Functions.sum(val, adjVal));
            } else if (method.equals("substitute")) {
                data.put(var, adjVal);
            } else {
                LOG.warn("Found unsupported adjust method [" + method + "], this rule will be ignored");
            }
        }
        
        private void adjustListValue(String var, String adjVal, String method) {
            
            ArrayList<HashMap> dailyWeathers = MapUtil.getObjectOr(wthData, "dailyWeather", new ArrayList());
            
            for (HashMap dailyWeather : dailyWeathers) {
                adjustSingleValue(dailyWeather, var, adjVal, method);
            }
        }
    }
}
