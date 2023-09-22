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

import org.hd.d.TRVmodel.hg.HGTRVHPMModelParameterised;
import org.hd.d.TRVmodel.hg.HGTRVHPMModelParameterised.DemandWithoutAndWithSetback;

import junit.framework.TestCase;

/**Test the parameterised Heat Geek TRV-with-HP model with soft A-room temperatures. */
public final class TestHGTRVHPModelSoftATemperature extends TestCase
    {
    /**Test bungalow with 'fix' parameters but otherwise original setup. */
    public static void testWithFixParameters()
	    {
    	final HGTRVHPMModelParameterised.ModelParameters fixParams = HGTRVHPMModelParameterised.ModelParameters.FIXES_APPLIED;
    	final DemandWithoutAndWithSetback originalBungalowDemand = HGTRVHPMModelParameterised.computeBungalowDemandW(fixParams);
    	final DemandWithoutAndWithSetback softBungalowDemand = HGTRVHPMModelParameterised.computeSoftATempDemandW(fixParams, true);

    	// Both heat demand and heat-pump electricity demand are expected to be identical to the original
    	// without B rooms set back.
    	assertEquals(originalBungalowDemand.noSetback().heatDemand(), softBungalowDemand.noSetback().heatDemand(), 1.0);
    	assertEquals(originalBungalowDemand.noSetback().heatPumpElectricity(), softBungalowDemand.noSetback().heatPumpElectricity(), 1.0);

//    	// Both heat demand and heat-pump electricity demand are expected to be strictly lower than the original
//    	// when B rooms are set back and A rooms have soft temperature regulation (weather compensation).
//    	assertTrue(originalBungalowDemand.noSetback().heatDemand() > softBungalowDemand.noSetback().heatDemand());
//    	assertTrue(originalBungalowDemand.noSetback().heatPumpElectricity() > softBungalowDemand.noSetback().heatPumpElectricity());
	    }

    }
