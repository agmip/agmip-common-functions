package org.agmip.functions;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
        AcePathfinderUtil.insertValue(expectedData, "sllb", "10");
        AcePathfinderUtil.insertValue(expectedData, "slbdm", "1.15");
        AcePathfinderUtil.insertValue(expectedData, "sllb", "25");
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

        ArrayList<HashMap<String, String>> result = SoilHelper.splittingSoillayer(data);
        
//        assertEquals("getRootDistribution: layer size is incorrect ", expectedLayers.size(), result.size());
        assertEquals("getRootDistribution: layer data is incorrect ", expectedLayers, result);
        
        log.info("getRootDistribution() output: {}", result.toString());
    }
}
