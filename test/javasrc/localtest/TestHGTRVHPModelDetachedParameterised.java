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

import org.hd.d.TRVmodel.hg.HGTRVHPMModel;
import org.hd.d.TRVmodel.hg.HGTRVHPMModelParameterised;
import org.hd.d.TRVmodel.hg.HGTRVHPMModelParameterised.DemandWithoutAndWithSetback;

import junit.framework.TestCase;

/**Test the parameterised Heat Geek TRV-with-HP model, detached house version. */
public final class TestHGTRVHPModelDetachedParameterised extends TestCase
    {
    /**Sanity-test detached-as-bungalow against bungalow, all defaults. */
    public static void testOriginalVsDetachedWithDefaultParameters()
	    {
    	final HGTRVHPMModelParameterised.ModelParameters defaultParams = new HGTRVHPMModelParameterised.ModelParameters();
    	final DemandWithoutAndWithSetback bungalowDemandW = HGTRVHPMModelParameterised.computeBungalowDemandW(defaultParams);
	    assertEquals(HGTRVHPMModel.HOME_HEAT_LOSS_AT_NORMAL_ROOM_TEMPERATURE_W, bungalowDemandW.noSetback().heatDemand(), 1);
	    assertEquals(HGTRVHPMModel.HOME_HEAT_LOSS_B_SETBACK_W, bungalowDemandW.withSetback().heatDemand(), 1);
	    assertEquals(HGTRVHPMModel.HEAT_PUMP_POWER_IN_NO_SETBACK_W, bungalowDemandW.noSetback().heatPumpElectricity(), 1);
	    assertEquals(HGTRVHPMModel.HEAT_PUMP_POWER_IN_B_SETBACK_W, bungalowDemandW.withSetback().heatPumpElectricity(), 1);

    	final DemandWithoutAndWithSetback detachedDemandW = HGTRVHPMModelParameterised.computeDetachedDemandW(defaultParams, true);
	    assertEquals(HGTRVHPMModel.HOME_HEAT_LOSS_AT_NORMAL_ROOM_TEMPERATURE_W, detachedDemandW.noSetback().heatDemand(), 1);
	    assertEquals(HGTRVHPMModel.HOME_HEAT_LOSS_B_SETBACK_W, detachedDemandW.withSetback().heatDemand(), 1);
	    assertEquals(HGTRVHPMModel.HEAT_PUMP_POWER_IN_NO_SETBACK_W, detachedDemandW.noSetback().heatPumpElectricity(), 1);
	    assertEquals(HGTRVHPMModel.HEAT_PUMP_POWER_IN_B_SETBACK_W, detachedDemandW.withSetback().heatPumpElectricity(), 1);
	    }

    /**Sanity-test detached-as-bungalow against bungalow, with fixes. */
    public static void testOriginalVsDetachedWithFixedParameters()
	    {
    	final HGTRVHPMModelParameterised.ModelParameters fixedParams = HGTRVHPMModelParameterised.ModelParameters.FIXES_APPLIED;
    	final DemandWithoutAndWithSetback bungalowDemandW = HGTRVHPMModelParameterised.computeBungalowDemandW(fixedParams);
    	final DemandWithoutAndWithSetback detachedDemandW = HGTRVHPMModelParameterised.computeDetachedDemandW(fixedParams, true);
	    assertEquals(bungalowDemandW.noSetback().heatDemand(), detachedDemandW.noSetback().heatDemand(), 1);
	    assertEquals(bungalowDemandW.withSetback().heatDemand(), detachedDemandW.withSetback().heatDemand(), 1);
	    assertEquals(bungalowDemandW.noSetback().heatPumpElectricity(), detachedDemandW.noSetback().heatPumpElectricity(), 1);
	    assertEquals(bungalowDemandW.withSetback().heatPumpElectricity(), detachedDemandW.withSetback().heatPumpElectricity(), 1);
	    }

    }
