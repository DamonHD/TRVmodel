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

import java.io.IOException;
import java.io.StringReader;

import org.hd.d.TRVmodel.data.DDNTemperatureDataCSV;
import org.hd.d.TRVmodel.hg.HGTRVHPMModelParameterised;

import junit.framework.TestCase;

/**Test the parameterised Heat Geek TRV-with-HP model against hourly temperature data.
 */
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
    	final DDNTemperatureDataCSV temperatures = DDNTemperatureDataCSV.parseDDNTemperatureDataCSV(new StringReader(fragment));
    	final HGTRVHPMModelParameterised.ModelParameters defaultParams = new HGTRVHPMModelParameterised.ModelParameters();




//    	final double powerNoSetback = HGTRVHPMModelParameterised.computeHPElectricityDemandW(defaultParams, false);
//    	final double powerWithSetback = HGTRVHPMModelParameterised.computeHPElectricityDemandW(defaultParams, true);
//	    assertEquals(HGTRVHPMModel.HEAT_PUMP_POWER_IN_NO_SETBACK_W, powerNoSetback, 1);
//	    assertEquals(HGTRVHPMModel.HEAT_PUMP_POWER_IN_B_SETBACK_W, powerWithSetback, 1);
//
//	    // The overall point of this Heat Geek example!
//	    assertTrue("electrical power goes UP with B rooms set back", powerNoSetback < powerWithSetback);
	    }
    }
