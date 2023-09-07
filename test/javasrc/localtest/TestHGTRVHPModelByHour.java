/*
Copyright (c) 2023, Damon Hart-Davis

Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package localtest;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.hd.d.TRVmodel.data.DDNTemperatureDataCSV;
import org.hd.d.TRVmodel.hg.HGTRVHPMModel;
import org.hd.d.TRVmodel.hg.HGTRVHPMModelByHour;
import org.hd.d.TRVmodel.hg.HGTRVHPMModelByHour.ScenarioResult;
import org.hd.d.TRVmodel.hg.HGTRVHPMModelParameterised;

import junit.framework.TestCase;

/**Test the parameterised Heat Geek TRV-with-HP model against hourly temperature data. */
public final class TestHGTRVHPModelByHour extends TestCase
    {
    /**Test with default (as published page) parameters and synthetic single external temperature per original model. */
    public static void testWithDefaultParameters() throws IOException
	    {
    	// The HG page external temperature of -3 is the only important datum here.
    	final String fragment = """
Datetime,Timezone,Date,Time,Temp (?C),% Estimated
2023-01-01 00:00,GMT,2023-01-01,00:00,-3,0
	    			""";
    	final DDNTemperatureDataCSV temperatureDefault = DDNTemperatureDataCSV.parseDDNTemperatureDataCSV(new StringReader(fragment));
    	final HGTRVHPMModelParameterised.ModelParameters modelDefaultParams = new HGTRVHPMModelParameterised.ModelParameters();

    	final HGTRVHPMModelByHour scenario = new HGTRVHPMModelByHour(modelDefaultParams, temperatureDefault);

    	final ScenarioResult result = scenario.runScenario(false);
    	assertNotNull(result);
    	assertEquals("expect the HG-reported result, ie setback increase heat pump electricity demand", 1.0, result.hoursFractionSetbackRaisesDemand(), 0.0001);

    	final double powerNoSetback = result.demand().noSetback().heatPumpElectricity();
    	final double powerWithSetback = result.demand().withSetback().heatPumpElectricity();
	    assertEquals(HGTRVHPMModel.HEAT_PUMP_POWER_IN_NO_SETBACK_W, powerNoSetback, 1);
	    assertEquals(HGTRVHPMModel.HEAT_PUMP_POWER_IN_B_SETBACK_W, powerWithSetback, 1);

	    // The overall point of the Heat Geek example!
	    assertTrue("electrical power goes UP with B rooms set back", powerNoSetback < powerWithSetback);
	    }

    /**Test with errors fixed and a representative London temperature year. */
    public static void testForLondon2018() throws IOException
	    {
    	final File path = DDNTemperatureDataCSV.DATA_EGLL_2018;
    	final DDNTemperatureDataCSV temperatures =
    			DDNTemperatureDataCSV.loadDDNTemperatureDataCSV(path);
    	final HGTRVHPMModelParameterised.ModelParameters modelParams =
    			HGTRVHPMModelParameterised.ModelParameters.FIXES_APPLIED;

    	final HGTRVHPMModelByHour scenario = new HGTRVHPMModelByHour(modelParams, temperatures);

    	final ScenarioResult result = scenario.runScenario(false);
    	assertNotNull(result);
    	assertEquals("expect the HG-reported result, ie setback increase heat pump electricity demand", 0.45, result.hoursFractionSetbackRaisesDemand(), 0.01);

    	final double powerNoSetback = result.demand().noSetback().heatPumpElectricity();
    	final double powerWithSetback = result.demand().withSetback().heatPumpElectricity();
	    assertEquals(246, powerNoSetback, 1);
	    assertEquals(253, powerWithSetback, 1);

    	final double heatNoSetback = result.demand().noSetback().heatDemand();
    	final double heatWithSetback = result.demand().withSetback().heatDemand();
	    assertEquals(719, heatNoSetback, 1);
	    assertEquals(634, heatWithSetback, 1);

	    // The overall point of the Heat Geek example!
	    assertTrue("electrical power goes UP with B rooms set back", powerNoSetback < powerWithSetback);
	    }
    }
