package org.agmip.translators.soil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.agmip.ace.AcePathfinder;
import org.agmip.ace.util.AcePathfinderUtil;
import org.agmip.functions.WeatherHelperTest;
import org.agmip.util.JSONAdapter;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Meng Zhang
 */
public class LayerReducerTest {

    private static final Logger LOG = LoggerFactory.getLogger(LayerReducerTest.class);

    @Before
    public void setUp() throws Exception {
//        resource = this.getClass().getResource("/ufga8201_multi.json");
    }

    @Test
    public void test() throws IOException, Exception {

        int expectedLayerNum = 3;
        ArrayList<HashMap<String, String>> result;

        HashMap<String, Object> data = new HashMap<String, Object>();
        
        AcePathfinderUtil.insertValue(data, "sllb", "5");
        AcePathfinderUtil.insertValue(data, "slll", "0.023");
        AcePathfinderUtil.insertValue(data, "sldul", "0.086");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.47");
        AcePathfinderUtil.insertValue(data, "sloc", "0.9");
        AcePathfinderUtil.insertValue(data, "sksat", "7.4");
        
        AcePathfinderUtil.insertValue(data, "sllb", "15");
        AcePathfinderUtil.insertValue(data, "slll", "0.023");
        AcePathfinderUtil.insertValue(data, "sldul", "0.086");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.47");
        AcePathfinderUtil.insertValue(data, "sloc", "0.69");
        AcePathfinderUtil.insertValue(data, "sksat", "7.4");
        
        AcePathfinderUtil.insertValue(data, "sllb", "30");
        AcePathfinderUtil.insertValue(data, "slll", "0.023");
        AcePathfinderUtil.insertValue(data, "sldul", "0.086");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.41");
        AcePathfinderUtil.insertValue(data, "sloc", "0.28");
        AcePathfinderUtil.insertValue(data, "sksat", "15.8");
        
        AcePathfinderUtil.insertValue(data, "sllb", "45");
        AcePathfinderUtil.insertValue(data, "slll", "0.023");
        AcePathfinderUtil.insertValue(data, "sldul", "0.086");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.43");
        AcePathfinderUtil.insertValue(data, "sloc", "0.2");
        AcePathfinderUtil.insertValue(data, "sksat", "28");
        
        AcePathfinderUtil.insertValue(data, "sllb", "60");
        AcePathfinderUtil.insertValue(data, "slll", "0.023");
        AcePathfinderUtil.insertValue(data, "sldul", "0.086");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.43");
        AcePathfinderUtil.insertValue(data, "sloc", "0.2");
        AcePathfinderUtil.insertValue(data, "sksat", "28");
        
        AcePathfinderUtil.insertValue(data, "sllb", "90");
        AcePathfinderUtil.insertValue(data, "slll", "0.027");
        AcePathfinderUtil.insertValue(data, "sldul", "0.13");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.52");
        AcePathfinderUtil.insertValue(data, "sloc", "0.09");
        AcePathfinderUtil.insertValue(data, "sksat", "27.6");
        
        AcePathfinderUtil.insertValue(data, "sllb", "120");
        AcePathfinderUtil.insertValue(data, "slll", "0.027");
        AcePathfinderUtil.insertValue(data, "sldul", "0.13");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.52");
        AcePathfinderUtil.insertValue(data, "sloc", "0.03");
        AcePathfinderUtil.insertValue(data, "sksat", "17.5");
        
        AcePathfinderUtil.insertValue(data, "sllb", "150");
        AcePathfinderUtil.insertValue(data, "slll", "0.027");
        AcePathfinderUtil.insertValue(data, "sldul", "0.13");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.46");
        AcePathfinderUtil.insertValue(data, "sloc", "0.03");
        AcePathfinderUtil.insertValue(data, "sksat", "0.3");
        
        AcePathfinderUtil.insertValue(data, "sllb", "180");
        AcePathfinderUtil.insertValue(data, "slll", "0.07");
        AcePathfinderUtil.insertValue(data, "sldul", "0.258");
        AcePathfinderUtil.insertValue(data, "slbdm", "1.46");
        AcePathfinderUtil.insertValue(data, "sloc", "0.03");
        AcePathfinderUtil.insertValue(data, "sksat", "0.1");
        
        ArrayList<HashMap<String, String>> soilLarys = (ArrayList) ((HashMap) data.get("soil")).get("soilLayer");

        LOG.info("Original dada: {}", JSONAdapter.toJSON(soilLarys));
        result = new LayerReducer(new SAReducerDecorator()).process(soilLarys);
        LOG.info("Processing result: {}", JSONAdapter.toJSON(result));
        assertEquals("the number of result layer is incorrect", expectedLayerNum, result.size());
    }
}
