package org.agmip.functions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import static org.agmip.common.Functions.*;
import static org.agmip.util.MapUtil.*;
import org.agmip.util.MapUtil.BucketEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide static functions for weather data handling
 *
 * @author Meng zhang
 */
public class WeatherHelper {

    private static final Logger LOG = LoggerFactory.getLogger(WeatherHelper.class);

    public static HashMap<String, String> getTavAndAmp(HashMap wthData) {

        HashMap<String, String> results = new HashMap<String, String>();
        HashMap<Integer, MonthlyAvg> tyear = new HashMap<Integer, MonthlyAvg>();
        MonthlyAvg tmonth;
        MonthlyAvg tavAllYears;
        ArrayList<String> tampAllYears;
        String tav;
        String tamp;

        ArrayList<HashMap<String, String>> dailyArr = new BucketEntry(wthData).getDataList();

        // Load daily data
        for (int i = 0; i < dailyArr.size(); i++) {

            String date;
            int year;
            int month;
            String tmax;
            String tmin;
            String tavgDaily;

            HashMap<String, String> dailyData = dailyArr.get(i);
            date = getValueOr(dailyData, "w_date", "").trim();
            if (date.equals("")) {
                LOG.debug("There is daily data do not having a valid date");
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

            tmax = getValueOr(dailyData, "tmax", "").trim();
            tmin = getValueOr(dailyData, "tmin", "").trim();
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
            String[] tavgs = tmonth.getAllTavg();
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
        tav = average(tavAllYears.getAllTavg());
        tamp = average(tampAllYears.toArray(new String[0]));

        if (tav != null) {
            results.put("tav", tav);
        }
        if (tamp != null) {
            results.put("tamp", tamp);
        }
        return results;
    }

    private static class MonthlyAvg {

        private int[] days = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        private String[] tavg = new String[12];

        public void add(int month, String tavg) {
            if (tavg == null) {
                return;
            }
            if (this.tavg[month] == null) {
                this.tavg[month] = tavg;
                days[month] = 1;
            } else {
                this.tavg[month] = sum(this.tavg[month], tavg);
                days[month]++;
            }
        }

        public String getTavg(int month) {
            if (days[month] <= 0) {
                return null;
            } else {
                return divide(tavg[month], days[month] + "");
            }
        }

        public String[] getAllTavg() {
            String[] ret = new String[days.length];
            for (int i = 0; i < days.length; i++) {
                ret[i] = getTavg(i);
            }
            return ret;
        }
    }
}
