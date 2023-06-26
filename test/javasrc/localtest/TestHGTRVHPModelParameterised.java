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

import junit.framework.TestCase;

/**Test the parameterised Heat Geek TRV-with-HP model.
 */
public final class TestHGTRVHPModelParameterised extends TestCase
    {
    /**Verify that test harness is sane... */
    public static void testSanity() { }

    /**Test with default (as published page) parameters. */
    public static void testWithDefaultParameters()
	    {
    	final HGTRVHPMModelParameterised.ModelParameters defaultParams = new HGTRVHPMModelParameterised.ModelParameters();
    	final double powerNoSetback = HGTRVHPMModelParameterised.computeHPElectricityDemandW(defaultParams, false);
    	final double powerWithSetback = HGTRVHPMModelParameterised.computeHPElectricityDemandW(defaultParams, true);
//	    assertEquals(HGTRVHPMModel.HEAT_PUMP_POWER_IN_NO_SETBACK_W, powerNoSetback, 0.5);
//	    assertEquals(HGTRVHPMModel.HEAT_PUMP_POWER_IN_B_SETBACK_W, powerWithSetback, 0.5);
//
//	    // The overall point of this Heat Geek example!
//	    assertTrue("electrical power goes UP with Bs set back", powerNoSetback < powerWithSetback);
	    }
    }
