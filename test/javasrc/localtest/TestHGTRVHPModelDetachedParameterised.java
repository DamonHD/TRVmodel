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

import org.hd.d.TRVmodel.data.DDNTemperatureDataCSV;
import org.hd.d.TRVmodel.hg.HGTRVHPMModel;
import org.hd.d.TRVmodel.hg.HGTRVHPMModelByHour;
import org.hd.d.TRVmodel.hg.HGTRVHPMModelByHour.ScenarioResult;
import org.hd.d.TRVmodel.hg.HGTRVHPMModelParameterised;
import org.hd.d.TRVmodel.hg.HGTRVHPMModelParameterised.DemandWithoutAndWithSetback;
import org.hd.d.TRVmodel.hg.HGTRVHPMModelParameterised.ModelParameters;

import junit.framework.TestCase;

/**Test the parameterised Heat Geek TRV-with-HP model, detached house version. */
public final class TestHGTRVHPModelDetachedParameterised extends TestCase
    {
    /**Sanity-test detached-as-bungalow against bungalow, all defaults. */
    public static void testOriginalVsDetachedAsBungalowWithDefaultParameters()
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
    public static void testOriginalVsDetachedAsBungalowWithFixedParameters()
	    {
    	final HGTRVHPMModelParameterised.ModelParameters fixedParams = HGTRVHPMModelParameterised.ModelParameters.FIXES_APPLIED;
    	final DemandWithoutAndWithSetback bungalowDemandW = HGTRVHPMModelParameterised.computeBungalowDemandW(fixedParams);
    	final DemandWithoutAndWithSetback detachedDemandW = HGTRVHPMModelParameterised.computeDetachedDemandW(fixedParams, true);
	    assertEquals(bungalowDemandW.noSetback().heatDemand(), detachedDemandW.noSetback().heatDemand(), 1);
	    assertEquals(bungalowDemandW.withSetback().heatDemand(), detachedDemandW.withSetback().heatDemand(), 1);
	    assertEquals(bungalowDemandW.noSetback().heatPumpElectricity(), detachedDemandW.noSetback().heatPumpElectricity(), 1);
	    assertEquals(bungalowDemandW.withSetback().heatPumpElectricity(), detachedDemandW.withSetback().heatPumpElectricity(), 1);
	    }

    /**Sanity-test detached-as-bungalow against bungalow, with fixes and a range of external temperatures. */
    public static void testOriginalVsDetachedAsBungalowWithFixedParametersAndVariousExtTemps()
	    {
        for(double eat = HGTRVHPMModel.EXTERNAL_AIR_TEMPERATURE_C - 10.0; eat < HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C; eat += 1.0)
	        {
	    	final HGTRVHPMModelParameterised.ModelParameters params = new ModelParameters(
	    			ModelParameters.FIXED_DOORS_PER_INTERNAL_WALL,
	    			ModelParameters.FIXED_CORRECT_COP_FOR_FLOW_TEMPERATURE,
	    			ModelParameters.DEFAULT_ARRANGEMENT_ABAB,
	    			eat);
	    	final DemandWithoutAndWithSetback bungalowDemandW = HGTRVHPMModelParameterised.computeBungalowDemandW(params);
	    	final DemandWithoutAndWithSetback detachedDemandW = HGTRVHPMModelParameterised.computeDetachedDemandW(params, true);
		    assertEquals(bungalowDemandW.noSetback().heatDemand(), detachedDemandW.noSetback().heatDemand(), 1);
		    assertEquals(bungalowDemandW.withSetback().heatDemand(), detachedDemandW.withSetback().heatDemand(), 1);
		    assertEquals(bungalowDemandW.noSetback().heatPumpElectricity(), detachedDemandW.noSetback().heatPumpElectricity(), 1);
		    assertEquals(bungalowDemandW.withSetback().heatPumpElectricity(), detachedDemandW.withSetback().heatPumpElectricity(), 1);
	        }
	    }

    /**Sanity-test detached-as-bungalow against bungalow, with fixes and a range of external temperatures, AABB arrangement. */
    public static void testOriginalVsDetachedAsBungalowWithFixedParametersAndVariousExtTempsAABB()
	    {
        for(double eat = HGTRVHPMModel.EXTERNAL_AIR_TEMPERATURE_C - 10.0; eat < HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C; eat += 1.0)
	        {
	    	final HGTRVHPMModelParameterised.ModelParameters params = new ModelParameters(
	    			ModelParameters.FIXED_DOORS_PER_INTERNAL_WALL,
	    			ModelParameters.FIXED_CORRECT_COP_FOR_FLOW_TEMPERATURE,
	    			!ModelParameters.DEFAULT_ARRANGEMENT_ABAB,
	    			eat);
	    	final DemandWithoutAndWithSetback bungalowDemandW = HGTRVHPMModelParameterised.computeBungalowDemandW(params);
	    	final DemandWithoutAndWithSetback detachedDemandW = HGTRVHPMModelParameterised.computeDetachedDemandW(params, true);
		    assertEquals(bungalowDemandW.noSetback().heatDemand(), detachedDemandW.noSetback().heatDemand(), 1);
		    assertEquals(bungalowDemandW.withSetback().heatDemand(), detachedDemandW.withSetback().heatDemand(), 1);
		    assertEquals(bungalowDemandW.noSetback().heatPumpElectricity(), detachedDemandW.noSetback().heatPumpElectricity(), 1);
		    assertEquals(bungalowDemandW.withSetback().heatPumpElectricity(), detachedDemandW.withSetback().heatPumpElectricity(), 1);
	        }
	    }

    /**Sanity test detached-as-detached against bungalow (both with fixes).
     * The detached house should always lose more heat and need more heat-pump electricity.
     */
    public static void testOriginalVsDetached()
	    {
	    for(double eat = HGTRVHPMModel.EXTERNAL_AIR_TEMPERATURE_C - 10.0; eat < HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C; eat += 1.0)
	        {
	    	final HGTRVHPMModelParameterised.ModelParameters params = new ModelParameters(
	    			ModelParameters.FIXED_DOORS_PER_INTERNAL_WALL,
	    			ModelParameters.FIXED_CORRECT_COP_FOR_FLOW_TEMPERATURE,
	    			ModelParameters.DEFAULT_ARRANGEMENT_ABAB,
	    			eat);
	    	final DemandWithoutAndWithSetback bungalowDemandW = HGTRVHPMModelParameterised.computeBungalowDemandW(params);
	    	final DemandWithoutAndWithSetback detachedDemandW = HGTRVHPMModelParameterised.computeDetachedDemandW(params, false);
		    assertTrue(bungalowDemandW.noSetback().heatDemand() < detachedDemandW.noSetback().heatDemand());
		    assertTrue(bungalowDemandW.withSetback().heatDemand() < detachedDemandW.withSetback().heatDemand());
		    assertTrue(bungalowDemandW.noSetback().heatPumpElectricity() < detachedDemandW.noSetback().heatPumpElectricity());
		    assertTrue(bungalowDemandW.withSetback().heatPumpElectricity() < detachedDemandW.withSetback().heatPumpElectricity());
	        }
	    }

    /**Check one test scenario of number for detached (with fixes).
     * @throws IOException
     */
    public static void testDetachedLondon2018() throws IOException
	    {
    	final DDNTemperatureDataCSV temperaturesLondon2018 =
    			DDNTemperatureDataCSV.loadDDNTemperatureDataCSV(DDNTemperatureDataCSV.DATA_EGLL_2018);
    	final HGTRVHPMModelByHour scenarioLondon2018 = new HGTRVHPMModelByHour(
    			HGTRVHPMModelParameterised.ModelParameters.FIXES_APPLIED, temperaturesLondon2018);
    	final ScenarioResult resultLondon2018 = scenarioLondon2018.runScenario(false, false, null);
//    	Percentage of hours that room setback raises heat pump demand: 45%
    	assertEquals(0.45, resultLondon2018.hoursFractionSetbackRaisesDemand(), 0.01);
//    	Heat mean demand: with no setback 719W, with setback 634W; -12% change with setback
    	final double heatNoSetbackLondon2018 = resultLondon2018.demand().noSetback().heatDemand();
    	final double heatWithSetbackLondon2018 = resultLondon2018.demand().withSetback().heatDemand();
    	assertEquals(719, heatNoSetbackLondon2018, 1);
    	assertEquals(634, heatWithSetbackLondon2018, 1);
//    	Heat pump mean power: with no setback 246W, with setback 253W; 3% change with setback
    	final double powerNoSetbackLondon2018 = resultLondon2018.demand().noSetback().heatPumpElectricity();
    	final double powerWithSetbackLondon2018 = resultLondon2018.demand().withSetback().heatPumpElectricity();
    	assertEquals(246, powerNoSetbackLondon2018, 1);
    	assertEquals(253, powerWithSetbackLondon2018, 1);
	    }
    }
