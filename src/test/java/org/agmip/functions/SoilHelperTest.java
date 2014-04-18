package org.agmip.functions;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import static org.agmip.util.MapUtil.*;
import static org.junit.Assert.*;
import org.agmip.ace.util.AcePathfinderUtil;
import org.agmip.util.MapUtil;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Meng Zhang
 */
public class SoilHelperTest {

    URL resource;
    private static final Logger log = LoggerFactory.getLogger(SoilHelperTest.class);


    @Before
    public void setUp() throws Exception {
        resource = this.getClass().getResource("/ufga8201_multi.json");
    }

    @Test
    public void testGetRootDistribution() throws IOException, Exception {
        String m = "1";
        String pp = "20";
        String rd = "180";
        String[] expected = {"1.000", "1.000", "0.941", "0.543", "0.261", "0.125", "0.060", "0.029"};
        ArrayList<String> acctual = null;

        HashMap<String, Object> data = new HashMap<String, Object>();
        AcePathfinderUtil.insertValue(data, "sllb", "5");
        AcePathfinderUtil.insertValue(data, "sllb", "15");
        AcePathfinderUtil.insertValue(data, "sllb", "30");
        AcePathfinderUtil.insertValue(data, "sllb", "60");
        AcePathfinderUtil.insertValue(data, "sllb", "90");
        AcePathfinderUtil.insertValue(data, "sllb", "120");
        AcePathfinderUtil.insertValue(data, "sllb", "150");
        AcePathfinderUtil.insertValue(data, "sllb", "180");

        HashMap<String, ArrayList<String>> result = SoilHelper.getRootDistribution(data, "slrgf", m, pp, rd);
        acctual = result.get("slrgf");
        for (int i = 0; i < expected.length; i++) {
            try {
                assertEquals("getRootDistribution: normal case " + i, expected[i], (String) acctual.get(i));
            } catch (Error e) {
                log.error(e.getMessage());
            }
        }
        log.info("getRootDistribution() output: {}", result.toString());
    }

    @Test
    public void testGetIcnDistribution() throws IOException, Exception {
        String icin = "25";
        String[] expIcnTot = {"1.91", "1.93", "4.02", "4.09", "4.35", "4.35", "4.35"};
        String expIcnh4 = "0.11";
        String expIcno3 = "1.00";
        ArrayList<String> acctual = null;

        HashMap<String, Object> data = new HashMap<String, Object>();
        AcePathfinderUtil.insertValue(data, "sllb", "15");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.15");
        AcePathfinderUtil.insertValue(data, "sllb", "30");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.16");
        AcePathfinderUtil.insertValue(data, "sllb", "60");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.21");
        AcePathfinderUtil.insertValue(data, "sllb", "90");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.23");
        AcePathfinderUtil.insertValue(data, "sllb", "120");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.31");
        AcePathfinderUtil.insertValue(data, "sllb", "150");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.31");
        AcePathfinderUtil.insertValue(data, "sllb", "180");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.31");

        HashMap<String, ArrayList<String>> result = SoilHelper.getIcnDistribution(data, icin);
        
        acctual = result.get("icn_tot");
        for (int i = 0; i < expIcnTot.length; i++) {
            try {
                assertEquals("getRootDistribution: icn_tot " + i, expIcnTot[i], (String) acctual.get(i));
            } catch (Error e) {
                log.error(e.getMessage());
            }
        }
        assertEquals("getRootDistribution: icn_tot ", expIcnTot.length, acctual.size());
        
        acctual = result.get("icnh4");
        for (int i = 0; i < expIcnTot.length; i++) {
            try {
                assertEquals("getRootDistribution: icnh4 " + i, expIcnh4, (String) acctual.get(i));
            } catch (Error e) {
                log.error(e.getMessage());
            }
        }
        assertEquals("getRootDistribution: icnh4 ", expIcnTot.length, acctual.size());
        
        acctual = result.get("icno3");
        for (int i = 0; i < expIcnTot.length; i++) {
            try {
                assertEquals("getRootDistribution: icno3 " + i, expIcno3, (String) acctual.get(i));
            } catch (Error e) {
                log.error(e.getMessage());
            }
        }
        assertEquals("getRootDistribution: icno3 ", expIcnTot.length, acctual.size());
        
        log.info("getRootDistribution() output: {}", result.toString());
    }

    @Test
    public void testSplittingSoillayer() throws IOException, Exception {

        // Test for regular soil layer data
        HashMap<String, Object> data = new HashMap<String, Object>();
        AcePathfinderUtil.insertValue(data, "sllb", "10");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.15");
        AcePathfinderUtil.insertValue(data, "sllb", "40");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.16");
        AcePathfinderUtil.insertValue(data, "sllb", "80");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.21");
        AcePathfinderUtil.insertValue(data, "sllb", "110");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.23");
        AcePathfinderUtil.insertValue(data, "sllb", "180");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.31");
        AcePathfinderUtil.insertValue(data, "sllb", "250");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.31");
        
        HashMap<String, Object> expectedData = new HashMap<String, Object>();
        AcePathfinderUtil.insertValue(expectedData, "sllb", "5");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.15");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "15");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.155");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "28");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.16");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "40");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.16");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "60");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.21");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "80");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.21");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "110");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.23");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "133");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.31");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "156");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.31");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "180");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.31");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "215");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.31");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "250");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.31");
        ArrayList<HashMap<String, String>> expectedLayers = MapUtil.getBucket(expectedData, "soil").getDataList();

        ArrayList<HashMap<String, String>> result = SoilHelper.splittingSoillayer(data, false);
        
        assertEquals("getRootDistribution: soil layer case is incorrect ", expectedLayers, result);
        
        log.info("getRootDistribution() output: {}", result.toString());
    }

    @Test
    public void testSplittingSoillayer2() throws IOException, Exception {

        // Test for initial condirion
        HashMap<String, Object> data = new HashMap<String, Object>();
        AcePathfinderUtil.insertValue(data, "icbl", "10");
        AcePathfinderUtil.insertValue(data, "ich2o", "1.15");
        AcePathfinderUtil.insertValue(data, "icbl", "40");
        AcePathfinderUtil.insertValue(data, "ich2o", "1.16");
        AcePathfinderUtil.insertValue(data, "icbl", "80");
        AcePathfinderUtil.insertValue(data, "ich2o", "1.21");
        AcePathfinderUtil.insertValue(data, "icbl", "110");
        AcePathfinderUtil.insertValue(data, "ich2o", "1.23");
        AcePathfinderUtil.insertValue(data, "icbl", "180");
        AcePathfinderUtil.insertValue(data, "ich2o", "1.31");
        AcePathfinderUtil.insertValue(data, "icbl", "250");
        AcePathfinderUtil.insertValue(data, "ich2o", "1.31");
        
        HashMap<String, Object> expectedData = new HashMap<String, Object>();
        AcePathfinderUtil.insertValue(expectedData, "icbl", "5");
        AcePathfinderUtil.insertValue(expectedData, "ich2o", "1.15");
        AcePathfinderUtil.insertValue(expectedData, "icbl", "15");
        AcePathfinderUtil.insertValue(expectedData, "ich2o", "1.155");
        AcePathfinderUtil.insertValue(expectedData, "icbl", "28");
        AcePathfinderUtil.insertValue(expectedData, "ich2o", "1.16");
        AcePathfinderUtil.insertValue(expectedData, "icbl", "40");
        AcePathfinderUtil.insertValue(expectedData, "ich2o", "1.16");
        AcePathfinderUtil.insertValue(expectedData, "icbl", "60");
        AcePathfinderUtil.insertValue(expectedData, "ich2o", "1.21");
        AcePathfinderUtil.insertValue(expectedData, "icbl", "80");
        AcePathfinderUtil.insertValue(expectedData, "ich2o", "1.21");
        AcePathfinderUtil.insertValue(expectedData, "icbl", "110");
        AcePathfinderUtil.insertValue(expectedData, "ich2o", "1.23");
        AcePathfinderUtil.insertValue(expectedData, "icbl", "133");
        AcePathfinderUtil.insertValue(expectedData, "ich2o", "1.31");
        AcePathfinderUtil.insertValue(expectedData, "icbl", "156");
        AcePathfinderUtil.insertValue(expectedData, "ich2o", "1.31");
        AcePathfinderUtil.insertValue(expectedData, "icbl", "180");
        AcePathfinderUtil.insertValue(expectedData, "ich2o", "1.31");
        AcePathfinderUtil.insertValue(expectedData, "icbl", "215");
        AcePathfinderUtil.insertValue(expectedData, "ich2o", "1.31");
        AcePathfinderUtil.insertValue(expectedData, "icbl", "250");
        AcePathfinderUtil.insertValue(expectedData, "ich2o", "1.31");
        ArrayList<HashMap<String, String>> expectedLayers = MapUtil.getBucket(expectedData, "initial_conditions").getDataList();

        ArrayList<HashMap<String, String>> result = SoilHelper.splittingSoillayer(data, true);
        
        assertEquals("getRootDistribution: IC layer case is incorrect ", expectedLayers, result);
        
        log.info("getRootDistribution() output: {}", result.toString());
    }

    @Test
    public void testSplittingSoillayer3() throws IOException, Exception {

        // Test for fixed top layers with the case that both are mixed layer
        HashMap<String, Object> data = new HashMap<String, Object>();
        AcePathfinderUtil.insertValue(data, "sllb", "3");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.1");
        AcePathfinderUtil.insertValue(data, "sllb", "7");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.2");
        AcePathfinderUtil.insertValue(data, "sllb", "50");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.3");
        
        HashMap<String, Object> expectedData = new HashMap<String, Object>();
        AcePathfinderUtil.insertValue(expectedData, "sllb", "5");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.14");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "15");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.28");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "27");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.3");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "39");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.3");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "50");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.3");
        ArrayList<HashMap<String, String>> expectedLayers = MapUtil.getBucket(expectedData, "soil").getDataList();

        ArrayList<HashMap<String, String>> result = SoilHelper.splittingSoillayer(data, false);
        
        assertEquals("getRootDistribution: soil top layer case is incorrect ", expectedLayers, result);
        
        log.info("getRootDistribution() output: {}", result.toString());
    }

    @Test
    public void testSplittingSoillayer4() throws IOException, Exception {

        // Test for fixed top layers with the case that both are mixed layer and 5/15 depth exists
        HashMap<String, Object> data = new HashMap<String, Object>();
        AcePathfinderUtil.insertValue(data, "sllb", "3");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.1");
        AcePathfinderUtil.insertValue(data, "sllb", "5");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.16");
        AcePathfinderUtil.insertValue(data, "sllb", "7");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.2");
        AcePathfinderUtil.insertValue(data, "sllb", "15");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.26");
        AcePathfinderUtil.insertValue(data, "sllb", "50");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.3");
        
        HashMap<String, Object> expectedData = new HashMap<String, Object>();
        AcePathfinderUtil.insertValue(expectedData, "sllb", "5");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.124");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "15");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.248");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "27");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.3");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "39");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.3");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "50");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.3");
        ArrayList<HashMap<String, String>> expectedLayers = MapUtil.getBucket(expectedData, "soil").getDataList();

        ArrayList<HashMap<String, String>> result = SoilHelper.splittingSoillayer(data, false);
        
        assertEquals("getRootDistribution: soil top layer case 2 is incorrect ", expectedLayers, result);
        
        log.info("getRootDistribution() output: {}", result.toString());
    }

    @Test
    public void testSplittingSoillayer5() throws IOException, Exception {

        // Test for fixed top layers with the case that both are non-mixed layer and 5/15 depth exists
        HashMap<String, Object> data = new HashMap<String, Object>();
        AcePathfinderUtil.insertValue(data, "sllb", "5");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.16");
        AcePathfinderUtil.insertValue(data, "sllb", "15");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.26");
        AcePathfinderUtil.insertValue(data, "sllb", "50");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.3");
        
        HashMap<String, Object> expectedData = new HashMap<String, Object>();
        AcePathfinderUtil.insertValue(expectedData, "sllb", "5");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.16");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "15");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.26");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "27");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.3");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "39");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.3");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "50");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.3");
        ArrayList<HashMap<String, String>> expectedLayers = MapUtil.getBucket(expectedData, "soil").getDataList();

        ArrayList<HashMap<String, String>> result = SoilHelper.splittingSoillayer(data, false);
        
        assertEquals("getRootDistribution: soil top layer case 3 is incorrect ", expectedLayers, result);
        
        log.info("getRootDistribution() output: {}", result.toString());
    }

    @Test
    public void testSplittingSoillayer6() throws IOException, Exception {

        // Test for fixed top layers with the case that both are mixed layer
        HashMap<String, Object> data = new HashMap<String, Object>();
        AcePathfinderUtil.insertValue(data, "sllb", "255");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.31");
        
        HashMap<String, Object> expectedData = new HashMap<String, Object>();
        AcePathfinderUtil.insertValue(expectedData, "sllb", "5");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.31");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "15");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.31");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "75");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.31");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "135");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.31");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "195");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.31");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "255");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.31");
        ArrayList<HashMap<String, String>> expectedLayers = MapUtil.getBucket(expectedData, "soil").getDataList();

        ArrayList<HashMap<String, String>> result = SoilHelper.splittingSoillayer(data, false);
        
        assertEquals("getRootDistribution: soil one layer case is incorrect ", expectedLayers, result);
        
        log.info("getRootDistribution() output: {}", result.toString());
    }

    @Test
    public void testSplittingSoillayer7() throws IOException, Exception {

        // Test for fixed top layers with the case that both are non-mixed layer and 5/15 depth not exists
        HashMap<String, Object> data = new HashMap<String, Object>();
        AcePathfinderUtil.insertValue(data, "sllb", "30");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.16");
        AcePathfinderUtil.insertValue(data, "sllb", "50");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.3");
        
        HashMap<String, Object> expectedData = new HashMap<String, Object>();
        AcePathfinderUtil.insertValue(expectedData, "sllb", "5");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.16");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "15");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.16");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "30");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.16");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "40");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.3");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "50");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.3");
        ArrayList<HashMap<String, String>> expectedLayers = MapUtil.getBucket(expectedData, "soil").getDataList();

        ArrayList<HashMap<String, String>> result = SoilHelper.splittingSoillayer(data, false);
        
        assertEquals("getRootDistribution: soil top layer case 4 is incorrect ", expectedLayers, result);
        
        log.info("getRootDistribution() output: {}", result.toString());
    }
    
    @Test
    public void testGetSoilValsFromOthPara() throws IOException, Exception {

        // Test for fixed top layers with the case that both are non-mixed layer and 5/15 depth not exists
        HashMap<String, Object> data = new HashMap<String, Object>();
        AcePathfinderUtil.insertValue(data, "sllb", "30");
        AcePathfinderUtil.insertValue(data, "slsnd", "20");
        AcePathfinderUtil.insertValue(data, "slcly", "50");
        AcePathfinderUtil.insertValue(data, "sloc", "2");
        AcePathfinderUtil.insertValue(data, "sllb", "50");
        AcePathfinderUtil.insertValue(data, "slsnd", "30");
        AcePathfinderUtil.insertValue(data, "slcly", "40");
        AcePathfinderUtil.insertValue(data, "sloc", "1");
        ArrayList vars = new ArrayList();
        vars.add("all");
        
        HashMap<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put("slll", Arrays.asList(new String[] {"0.297", "0.243"}));
        expectedData.put("sldul", Arrays.asList(new String[] {"0.419", "0.376"}));
        expectedData.put("slsat", Arrays.asList(new String[] {"0.514", "0.471"}));
        expectedData.put("sksat", Arrays.asList(new String[] {"0.205", "0.216"}));
        expectedData.put("slbdm", Arrays.asList(new String[] {"1.29", "1.40"}));
//        AcePathfinderUtil.insertValue(expectedData, "sllb", "30");
//        AcePathfinderUtil.insertValue(expectedData, "slll", "0.297");
//        AcePathfinderUtil.insertValue(expectedData, "sldul", "0.419");
//        AcePathfinderUtil.insertValue(expectedData, "slsat", "0.514");
//        AcePathfinderUtil.insertValue(expectedData, "sksat", "2.05");
//        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.29");
//        AcePathfinderUtil.insertValue(expectedData, "sllb", "50");
//        AcePathfinderUtil.insertValue(expectedData, "slll", "0.243");
//        AcePathfinderUtil.insertValue(expectedData, "sldul", "0.376");
//        AcePathfinderUtil.insertValue(expectedData, "slsat", "0.471");
//        AcePathfinderUtil.insertValue(expectedData, "sksat", "2.16");
//        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.40");
//        ArrayList<HashMap<String, String>> expectedLayers = MapUtil.getBucket(expectedData, "soil").getDataList();

        HashMap<String, ArrayList<String>> result = SoilHelper.getSoilValsFromOthPara(data, "PTSAXTON2006", vars);
        
        assertEquals("getSoilValsFromOthPara: All case is incorrect ", expectedData, result);
        
        log.info("getSoilValsFromOthPara() output: {}", result.toString());
    }
    
    @Test
    public void testGetSoilValsFromOthPara2() throws IOException, Exception {

        // Test for fixed top layers with the case that both are non-mixed layer and 5/15 depth not exists
        HashMap<String, Object> data = new HashMap<String, Object>();
        AcePathfinderUtil.insertValue(data, "sllb", "30");
        AcePathfinderUtil.insertValue(data, "slsnd", "20");
        AcePathfinderUtil.insertValue(data, "slcly", "50");
        AcePathfinderUtil.insertValue(data, "sloc", "2");
        AcePathfinderUtil.insertValue(data, "sllb", "50");
        AcePathfinderUtil.insertValue(data, "slsnd", "30");
        AcePathfinderUtil.insertValue(data, "slcly", "40");
        AcePathfinderUtil.insertValue(data, "sloc", "1");
        ArrayList vars = new ArrayList();
        vars.add("slll");
        vars.add("sldul");
        vars.add("slsat");
        vars.add("sksat");
        vars.add("slbdm");
        vars.add("sldul");
        vars.add("slni");
        
        HashMap<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put("slll", Arrays.asList(new String[] {"0.297", "0.243"}));
        expectedData.put("sldul", Arrays.asList(new String[] {"0.419", "0.376"}));
        expectedData.put("slsat", Arrays.asList(new String[] {"0.514", "0.471"}));
        expectedData.put("sksat", Arrays.asList(new String[] {"0.205", "0.216"}));
        expectedData.put("slbdm", Arrays.asList(new String[] {"1.29", "1.40"}));
//        AcePathfinderUtil.insertValue(expectedData, "sllb", "30");
//        AcePathfinderUtil.insertValue(expectedData, "slll", "0.297");
//        AcePathfinderUtil.insertValue(expectedData, "sldul", "0.419");
//        AcePathfinderUtil.insertValue(expectedData, "slsat", "0.514");
//        AcePathfinderUtil.insertValue(expectedData, "sksat", "2.05");
//        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.29");
//        AcePathfinderUtil.insertValue(expectedData, "sllb", "50");
//        AcePathfinderUtil.insertValue(expectedData, "slll", "0.243");
//        AcePathfinderUtil.insertValue(expectedData, "sldul", "0.376");
//        AcePathfinderUtil.insertValue(expectedData, "slsat", "0.471");
//        AcePathfinderUtil.insertValue(expectedData, "sksat", "2.16");
//        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.40");
//        ArrayList<HashMap<String, String>> expectedLayers = MapUtil.getBucket(expectedData, "soil").getDataList();

        HashMap<String, ArrayList<String>> result = SoilHelper.getSoilValsFromOthPara(data, "PTSAXTON2006", vars);
        
        assertEquals("getSoilValsFromOthPara: specific case is incorrect ", expectedData, result);
        
        log.info("getSoilValsFromOthPara2() output: {}", result.toString());
    }
    
    @Test
    public void testGetSoilValsFromOthPara3() throws IOException, Exception {

        // Test for fixed top layers with the case that both are non-mixed layer and 5/15 depth not exists
        HashMap<String, Object> data = new HashMap<String, Object>();
        AcePathfinderUtil.insertValue(data, "sllb", "30");
        AcePathfinderUtil.insertValue(data, "slsnd", "20");
        AcePathfinderUtil.insertValue(data, "slcly", "50");
        AcePathfinderUtil.insertValue(data, "sloc", "2");
        AcePathfinderUtil.insertValue(data, "slcf", "50");
        AcePathfinderUtil.insertValue(data, "sllb", "50");
        AcePathfinderUtil.insertValue(data, "slsnd", "30");
        AcePathfinderUtil.insertValue(data, "slcly", "40");
        AcePathfinderUtil.insertValue(data, "sloc", "1");
        AcePathfinderUtil.insertValue(data, "slcf", "50");
        ArrayList vars = new ArrayList();
        vars.add("all");
        
        HashMap<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put("slll", Arrays.asList(new String[] {"0.297", "0.243"}));
        expectedData.put("sldul", Arrays.asList(new String[] {"0.419", "0.376"}));
        expectedData.put("slsat", Arrays.asList(new String[] {"0.514", "0.471"}));
        expectedData.put("sksat", Arrays.asList(new String[] {"0.118", "0.120"}));
        expectedData.put("slbdm", Arrays.asList(new String[] {"1.73", "1.83"}));
//        AcePathfinderUtil.insertValue(expectedData, "sllb", "30");
//        AcePathfinderUtil.insertValue(expectedData, "slll", "0.297");
//        AcePathfinderUtil.insertValue(expectedData, "sldul", "0.419");
//        AcePathfinderUtil.insertValue(expectedData, "slsat", "0.514");
//        AcePathfinderUtil.insertValue(expectedData, "sksat", "2.05");
//        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.29");
//        AcePathfinderUtil.insertValue(expectedData, "sllb", "50");
//        AcePathfinderUtil.insertValue(expectedData, "slll", "0.243");
//        AcePathfinderUtil.insertValue(expectedData, "sldul", "0.376");
//        AcePathfinderUtil.insertValue(expectedData, "slsat", "0.471");
//        AcePathfinderUtil.insertValue(expectedData, "sksat", "2.16");
//        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.40");
//        ArrayList<HashMap<String, String>> expectedLayers = MapUtil.getBucket(expectedData, "soil").getDataList();

        HashMap<String, ArrayList<String>> result = SoilHelper.getSoilValsFromOthPara(data, "PTSAXTON2006", vars);
        
        assertEquals("getSoilValsFromOthPara: All case is incorrect ", expectedData, result);
        
        log.info("getSoilValsFromOthPara() output: {}", result.toString());
    }
}
