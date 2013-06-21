package org.agmip.common.functions;

import static org.agmip.common.functions.BaseFunctions.acos;
import static org.agmip.common.functions.BaseFunctions.average;
import static org.agmip.common.functions.BaseFunctions.compare;
import static org.agmip.common.functions.BaseFunctions.convertFromAgmipDateString;
import static org.agmip.common.functions.BaseFunctions.cos;
import static org.agmip.common.functions.BaseFunctions.divide;
import static org.agmip.common.functions.BaseFunctions.exp;
import static org.agmip.common.functions.BaseFunctions.log;
import static org.agmip.common.functions.BaseFunctions.multiply;
import static org.agmip.common.functions.BaseFunctions.pow;
import static org.agmip.common.functions.BaseFunctions.product;
import static org.agmip.common.functions.BaseFunctions.round;
import static org.agmip.common.functions.BaseFunctions.sin;
import static org.agmip.common.functions.BaseFunctions.sqrt;
import static org.agmip.common.functions.BaseFunctions.subtract;
import static org.agmip.common.functions.BaseFunctions.sum;
import static org.agmip.common.functions.BaseFunctions.tan;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.agmip.ace.AceRecord;
import org.agmip.ace.AceWeather;
import org.agmip.common.functions.BaseFunctions.CompareMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide static functions for weather data handling
 *
 * @author Meng Zhang
 * @author Christopher Villalobos
 */
public class WeatherFunctions {

    private static final Logger LOG = LoggerFactory.getLogger(WeatherFunctions.class);

    /**
     * Calculate the AMP (annual amplitude of mean monthly temperature oC) and
     * TAV (Annual average ambient temperature oC)
     *
     * @param data The data map
     *
     * @return A {@code HashMap} contains {@code TAV} and {@code TAMP}, the key
     * is their ICASA variable name
     */
    /*
    public static HashMap<String, String> getTavAndAmp(HashMap data) {
        HashMap<String, String> results = new HashMap<String, String>();
        HashMap<Integer, MonthlyAvg> tyear = new HashMap<Integer, MonthlyAvg>();
        MonthlyAvg tmonth;
        MonthlyAvg tavAllYears;
        ArrayList<String> tampAllYears;
        String tav;
        String tamp;
        ArrayList<HashMap<String, String>> dailyArr = getDailyData(data);

        // Load daily data
        for (int i = 0; i < dailyArr.size(); i++) {

            String date;
            int year;
            int month;
            String tmax;
            String tmin;
            String tavgDaily;

            HashMap<String, String> dailyData = dailyArr.get(i);
            date = record.getValueOr("w_date", "").trim();
            if (date.equals("")) {
                LOG.warn("There is daily data do not having a valid date");
                continue;
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(convertFromAgmipDateString(date));
                year = cal.get(Calendar.YEAR);
                month = cal.get(Calendar.MONTH);
                tmonth = tyear.get(year);
                if (tmonth == null) {
                    tmonth = new MonthlyAvg();
                    tyear.put(year, tmonth);
                }
            }

            tmax = record.getValueOr("tmax", "").trim();
            tmin = record.getValueOr("tmin", "").trim();
            tavgDaily = average(tmax, tmin);
            if (tavgDaily != null) {
                tmonth.add(month, tavgDaily);
            }
        }

        // Calculate daily data
        tavAllYears = new MonthlyAvg();
        tampAllYears = new ArrayList();
        for (Iterator<Integer> it = tyear.keySet().iterator(); it.hasNext();) {
            int year = it.next();
            tmonth = tyear.get(year);
            String[] tavgs = tmonth.getAllAvg();
            // TAV
            for (int month = 0; month < tavgs.length; month++) {
                tavAllYears.add(month, tavgs[month]);
            }
            // TAMP
            String tampt = substract(max(tavgs), min(tavgs));
            if (tampt != null) {
                tampAllYears.add(tampt);
            }

        }
        tav = average(2, removeNull(tavAllYears.getAllAvg()));
        tamp = average(2, tampAllYears.toArray(new String[0]));

        if (tav != null) {
            results.put("tav", tav);
        }
        if (tamp != null) {
            results.put("tamp", tamp);
        }
        return results;
    }
    */
    /**
     * Calculate the monthly average for each month
     */
    /*
    private static class MonthlyAvg {

        private int[] days = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        private String[] avg = new String[12];

        public void add(int month, String val) {
            if (val == null) {
                return;
            }
            if (this.avg[month] == null) {
                this.avg[month] = val;
                days[month] = 1;
            } else {
                this.avg[month] = sum(this.avg[month], val);
                days[month]++;
            }
        }

        public String getAvg(int month) {
            if (days[month] <= 0) {
                return null;
            } else {
                return divide(avg[month], days[month] + "");
            }
        }

        public String[] getAllAvg() {
            String[] ret = new String[days.length];
            for (int i = 0; i < days.length; i++) {
                ret[i] = getAvg(i);
            }
            return ret;
        }
    }
*/
    /**
     * Get weather daily data array from data holder.
     *
     * @param data The experiment data holder
     * @return The weather daily data array
     */
    /*
    protected static ArrayList getDailyData(HashMap data) {
        if (data.containsKey("dailyWeather")) {
            return (ArrayList) getObjectOr(data, "dailyWeather", new ArrayList());
        } else {
            return new ArrayList();
        }
    }
     */
    /**
     * Get weather station data set from data holder.
     *
     * @param data The experiment data holder
     * @return The weather station data set
     */
    /*
    protected static HashMap getWthData(HashMap data) {
        if (data.containsKey("weather")) {
            return getObjectOr(data, "weather", new HashMap());
        } else {
            return data;
        }
    }
    */
    
    public static class FAO56 {
        
        /**
         * Station specific values necessary for FAO56 Eto calculations.
         */

        public static class EtoStationValues {
            /**
             * Elevation of station
             */
            public String elevation = null;
            
            /**
             * Psychrometric constant (γ) at elevation
             * @see elevation
             * @see <a target="_blank" href = "http://www.fao.org/docrep/x0490e/x0490e07.htm#psychrometric%20constant%20(g)">FAO56 Documentation - Chapter 3 - Psychrometric constant</a>
             */
            public String gamma     = null;
            
            /**
             * Latitude of station in radians
             */
            public String phi       = null;
            
            /**
             * Psychrometric constant (γ) of the instrument.
             * @see <a target="_blank" href="http://www.fao.org/docrep/x0490e/x0490e07.htm#calculation%20procedures">FAO56 Documentation - Chapter 3 - Calculation Procedures</a>
             */
            public String rPsy      = null;
            
            /**
             * Angstrom A coefficient, monthly
             */
            public String amth      = null;
            
            /**
             * Angstrom B coefficient, monthly
             */
            public String bmth      = null;
            
            /**
             * Reference height for wind speed
             */
            public String refHeight = null;

            /**
             * Extract and calculate default variables about the station to be used
             * within {@code FAO56#getDailyEto}.
             * 
             * Extracts elevation, reference height, and angrstrom coefficients
             *  of the weather station and calculates values for gamma, phi, and
             *  rPsy. 
             * 
             * @param weather The station data to extract values from.
             * @throws IOException
             */
            public void calculateEtoStationValues(AceWeather weather) throws IOException {
                elevation = weather.getValueOr("wst_elev", "");
                String latitude  = weather.getValueOr("wst_lat", "");
                String psyvnt    = weather.getValueOr("psyvnt", "").toUpperCase();
                amth             = weather.getValueOr("amth", "0.25");
                bmth             = weather.getValueOr("bmth", "0.50");
                refHeight        = weather.getValueOr("wndht", "");
                
                // Step 1. Atmospheric pressure (P) [kPa]
                String P = multiply("101.3", pow(divide(subtract("293", product("0.0065", elevation)), "293"), "5.26"));

                // Step 2. Psychrometric constant (γ)
                gamma = product("0.664742", "0.001", P);

                // Eq.17 (Step 8)
                // latitude [rad]
                phi = divide(product(latitude, Math.PI + ""), "180");

                // Get other potentially necessary meta data from Ace data set
                String aPsy = "";
                if (psyvnt.equals("FORCED")) {
                    aPsy = "0.000662";
                } else if (psyvnt.equals("NATURAL")) {
                    aPsy = "0.00800";
                }
                rPsy = multiply(aPsy, P);
            }
        }
   
        /** 
         * Calculate the reference evapotranspiration (ETo) by means of the
         * FAO-Penman Monteith equation. (Derived from FAO56)
         * 
         * @see <a target="_blank" href="http://www.fao.org/docrep/x0490e/x0490e05.htm#part%20a%20%20%20reference%20evapotranspiration%20(eto)">FAO56 Documentation - Part A</a> 
         * 
         * @param record Daily Weather record
         * @param sv FAO56 Eto Station Values
         * @return Daily Eto value or null
         * 
         * @throws IOException
         */
        public static String getDailyEto(AceRecord record, FAO56.EtoStationValues sv) throws IOException {
            // Calculate daily ETO

            // Get daily TMAX and TMIN
            String pi   = Math.PI+"";
            String date = record.getValueOr("w_date", "");
            String tMin = record.getValueOr("tmin", "").trim();
            String tMax = record.getValueOr("tmax", "").trim();
            if (tMin.equals("") || tMax.equals("")) {
                LOG.error("Unable to calculate Eto without TMIN or TMAX for date: {}", date);
                return null;
            }

            // Step 3. Mean air temperature (Tmean) [°C]
            String tMean = average(tMin, tMax);

            // Step 4. Saturation vapour pressure (es)
            String e_tMax = multiply("0.6108", exp(divide(multiply("17.27", tMax), sum(tMax, "237.3"))));
            String e_tMin = multiply("0.6108", exp(divide(multiply("17.27", tMin), sum(tMin, "237.3"))));
            String es = average(e_tMax, e_tMin);

            // Step 5. Slope of the saturation vapour pressure curve (Δ)
            String slope = divide(product("4098", "0.6108", exp(divide(multiply("17.27", tMean), sum(tMean, "237.3")))), pow(sum(tMean, "237.3"), "2"));

            // Step 6. Actual vapour pressure (ea) [kPa]
            String ea;
            String alt1;
            String alt2;
            // Method 1 IF VPRSD is available in the ACE data base
            if (!(alt1 = record.getValueOr("vprsd", "").trim()).equals("")) {
                // ea = VPRSD
                ea = alt1;

            } // Method 2 IF TDEW is available in the ACE data base
            else if (!(alt1 = record.getValueOr("tdew", "").trim()).equals("")) {
                // derive ea from the given dew point temperature
                ea = multiply("0.6108", exp(divide(multiply("17.27", alt1), sum(alt1, "237.3"))));

            } // Method 3 IF RHMND and RHMXD are available in the ACE data base
            else if (!(alt1 = record.getValueOr("rhmxd", "").trim()).equals("")
                    && !(alt2 = record.getValueOr("rhmnd", "").trim()).equals("")) {
                // ea from the given maximum and minimum Relative Humidity
                ea = average(product(e_tMin, alt1, "0.01"), product(e_tMax, alt2, "0.01"));

            } // Method 4 IF TDRY, TWET and PSYVNT are available in the ACE data base
            else if (!(alt1 = record.getValueOr("tdry", "").trim()).equals("")
                    && !(alt2 = record.getValueOr("twet", "").trim()).equals("")
                    && sv.rPsy != null) {
                // derive ea from the psychrometric data
                String e_tWet = multiply("0.6108", exp(divide(multiply("17.27", alt2), sum(alt2, "237.3"))));
                ea = subtract(e_tWet, multiply(sv.rPsy, subtract(alt1, alt2)));

            } // Method 5 use Tmin as an approximation of Tdew
            else {
                ea = e_tMin;
            }

            // Step 7. Vapour pressure difference (es - ea)
            String vpDiff = subtract(es, ea);

            // Step 8. Extra terrestrial radiation (Ra) and daylight hours (N)
            Date w_date = convertFromAgmipDateString(date);
            if (w_date == null) {
                LOG.error("Unable to calculate Eto with invalid date: {}", date);
                return null;
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime((w_date));
            String J = cal.get(Calendar.DAY_OF_YEAR) + "";
            String dr = sum("1", multiply("0.033", cos(product("2", divide(pi + "", "365"), J))));
            String delta = multiply("0.409", sin(subtract(product("2", divide(pi, "365"), J), "1.39")));
            String omegas = acos(product("-1", tan(sv.phi), tan(delta)));
            String ra = divide(product("1440", "0.0820", dr, sum(product(omegas, sin(sv.phi), sin(delta)), product(cos(sv.phi), cos(delta), sin(omegas)))), pi); // TODO
            String N = divide(multiply("24", omegas), pi);

            // Step 9. Solar radiation (Rs)
            String rs;
            // Method 1. IF SRAD is available in the ACE data base
            if (!(alt1 = record.getValueOr("srad", "").trim()).equals("")) {
                rs = alt1;
            } // Method 2. IF SUNH is available in the ACE data base
            else if (!(alt1 = record.getValueOr("sunh", "").trim()).equals("")) {
                rs = multiply(sum(sv.amth, divide(multiply(sv.bmth, alt1), N)), ra);
            } // Method 3. LSE use Tmin and Tmax to estimate Rs by means of the Hargreaves equation
            else {
                //TODO: Update "0.16" fixed value to lookup by coastal (0.19) or land (0.16)
                rs = product("0.16", sqrt(subtract(tMax, tMin)), ra);
            }

            // Step 10. Clear-Sky solar radiation (Rso)
            String rso = multiply(sum("0.75", product("2e-5", sv.elevation)), ra);

            // Step 11. Net solar radiation (Rns)
            String rns = multiply(subtract("1", "0.23"), rs);

            // Step 12. Net long wave  radiation (Rnl)
            String rnl = product("4.903e-9",
                    average(pow(sum(tMax, "273.16"), "4"), pow(sum(tMin, "273.16"), "4")),
                    subtract("0.34", multiply("0.14", sqrt(ea))),
                    subtract(divide(multiply("1.35", rs), rso), "0.35"));

            // Step 13. Net radiation (Rn)
            String rn = subtract(rns, rnl);

            // Step 14. Wind speed at 2 meter above ground level (u2)
            // Method 1. WIND is given in the ACE data base
            String u2;
            if (!(alt1 = record.getValueOr("wind", "").trim()).equals("")) {
                String uz = divide(multiply("1000", alt1), "86400", 4);
                // CASE 1. Reference height for wind speed measurement (WNDHT)  is 2 meter
                if (compare(alt2 = sv.refHeight.trim(), "2", CompareMode.EQUAL)) {
                    u2 = uz;
                } // CASE 2. Reference height for wind speed measurement (WNDHT)  is NOT 2 meter
                else {
                    u2 = divide(multiply(uz, "4.87"), log(subtract(multiply("67.8", alt2), "5.42")));
                }
            } else {
                u2 = "2";
            }

            // Step 15. Reference evapotranspiration (ETo)
            String eto = divide(
                    sum(product("0.408", slope, rn), divide(product(sv.gamma, "900", u2, vpDiff), sum(tMean, "273"))),
                    sum(slope, multiply(sv.gamma, sum("1", multiply("0.34", u2)))));
            return round(eto, 2);
        }
    }
}

