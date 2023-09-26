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

import junit.framework.TestCase;

/**Test the parameterised Heat Geek TRV-with-HP model with soft A-room temperatures. */
public final class TestHGTRVHPModelSoftATemperature extends TestCase
    {
    /**Test bungalow with 'fix' parameters but otherwise original setup, HG vs soft A temperatures. */
    public static void testWithFixParametersBungalow()
	    {
    	final HGTRVHPMModelParameterised.ModelParameters fixParams = HGTRVHPMModelParameterised.ModelParameters.FIXES_APPLIED;
    	final DemandWithoutAndWithSetback originalBungalowDemand = HGTRVHPMModelParameterised.computeBungalowDemandW(fixParams);
    	final double equilibriumTemperature[] = new double[1];
    	final DemandWithoutAndWithSetback softBungalowDemand = HGTRVHPMModelParameterised.computeSoftATempDemandW(fixParams, true, equilibriumTemperature);

    	// Both heat demand and heat-pump electricity demand are expected to be identical to the original
    	// without B rooms set back.
    	assertEquals(originalBungalowDemand.noSetback().heatDemand(), softBungalowDemand.noSetback().heatDemand(), 1.0);
    	assertEquals(originalBungalowDemand.noSetback().heatPumpElectricity(), softBungalowDemand.noSetback().heatPumpElectricity(), 1.0);

    	// Both heat demand and heat-pump electricity demand are expected to be strictly lower than the original
    	// when B rooms are set back and A rooms have soft temperature regulation (weather compensation).
    	assertTrue(originalBungalowDemand.withSetback().heatDemand() > softBungalowDemand.withSetback().heatDemand());
    	assertTrue(originalBungalowDemand.withSetback().heatPumpElectricity() > softBungalowDemand.withSetback().heatPumpElectricity());

    	// The A-room equilibrium temperature with B set back
    	// will be lower than the 'normal' temperature and higher than the setback temperature.
    	assertTrue(equilibriumTemperature[0] > HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C);
    	assertTrue(equilibriumTemperature[0] < HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C);
	    }

    /**Test detached with 'fix' parameters but otherwise original setup, HG vs soft A temperatures. */
    public static void testWithFixParametersDetached()
	    {
    	final HGTRVHPMModelParameterised.ModelParameters fixParams = HGTRVHPMModelParameterised.ModelParameters.FIXES_APPLIED;
    	final DemandWithoutAndWithSetback originalDetachedDemand = HGTRVHPMModelParameterised.computeDetachedDemandW(fixParams);
    	final double equilibriumTemperature[] = new double[1];
    	final DemandWithoutAndWithSetback softDetachedDemand = HGTRVHPMModelParameterised.computeSoftATempDemandW(fixParams, false, equilibriumTemperature);

    	// Both heat demand and heat-pump electricity demand are expected to be identical to the original
    	// without B rooms set back.
    	assertEquals(originalDetachedDemand.noSetback().heatDemand(), softDetachedDemand.noSetback().heatDemand(), 1.0);
    	assertEquals(originalDetachedDemand.noSetback().heatPumpElectricity(), softDetachedDemand.noSetback().heatPumpElectricity(), 1.0);

    	// Both heat demand and heat-pump electricity demand are expected to be strictly lower than the original
    	// when B rooms are set back and A rooms have soft temperature regulation (weather compensation).
    	assertTrue(originalDetachedDemand.withSetback().heatDemand() > softDetachedDemand.withSetback().heatDemand());
    	assertTrue(originalDetachedDemand.withSetback().heatPumpElectricity() > softDetachedDemand.withSetback().heatPumpElectricity());

    	// The A-room equilibrium temperature with B set back
    	// will be lower than the 'normal' temperature and higher than the setback temperature.
    	assertTrue(equilibriumTemperature[0] > HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C);
    	assertTrue(equilibriumTemperature[0] < HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C);
	    }

    /**Check one test scenario (London 2018) for bungalow (with fixes) and soft regulation.
     * @throws IOException
     */
    public static void testBungalowLondon2018Soft() throws IOException
	    {
		//Parameterised model, bungalow, soft regulation, fixes applied for doors and CoP temperature, external air temperature varied...
		//London (EGLL) 2018 hourly temperatures
		//Layout ABAB
		//  Minimum A-room temperature 18.2C
		//  Percentage of hours that room setback raises heat pump demand: 0.0%
		//  Heat mean demand: with no setback 719W, with setback 559W; -22.2% change with setback
		//  Heat pump mean power: with no setback 246W, with setback 193W; -21.6% change with setback
    	final HGTRVHPMModelParameterised.ModelParameters modelParameters = HGTRVHPMModelParameterised.ModelParameters.FIXES_APPLIED;
    	final double equilibriumTemperatureMinLondon2018Soft[] = new double[1];
    	final DDNTemperatureDataCSV temperaturesLondon2018Soft =
    			DDNTemperatureDataCSV.loadDDNTemperatureDataCSV(DDNTemperatureDataCSV.DATA_EGLL_2018);
    	final HGTRVHPMModelByHour scenarioLondon2018Soft = new HGTRVHPMModelByHour(
    			modelParameters, temperaturesLondon2018Soft);
    	final ScenarioResult resultLondon2018Soft = scenarioLondon2018Soft.runScenario(false, true, equilibriumTemperatureMinLondon2018Soft);
        assertEquals(18.2, equilibriumTemperatureMinLondon2018Soft[0], 0.1);
        assertEquals(0, resultLondon2018Soft.hoursFractionSetbackRaisesDemand(), 0.001);
        assertEquals(719, resultLondon2018Soft.demand().noSetback().heatDemand(), 1);
        assertEquals(559, resultLondon2018Soft.demand().withSetback().heatDemand(), 1);
        assertEquals(246, resultLondon2018Soft.demand().noSetback().heatPumpElectricity(), 1);
        assertEquals(193, resultLondon2018Soft.demand().withSetback().heatPumpElectricity(), 1);
	    }

    /**Check one test scenario (London 2018) for bungalow AABB (with fixes) and soft regulation.
     * @throws IOException
     */
    public static void testBungalowAABBLondon2018Soft() throws IOException
	    {
		//Parameterised model, bungalow, soft regulation, AABB, fixes applied for doors and CoP temperature, external air temperature varied...
		//London (EGLL) 2018 hourly temperatures
		//Layout AABB
		//  Minimum A-room temperature 18.2C
		//  Percentage of hours that room setback raises heat pump demand: 0.0%
		//  Heat mean demand: with no setback 719W, with setback 561W; -22.0% change with setback
		//  Heat pump mean power: with no setback 246W, with setback 193W; -21.4% change with setback
    	final HGTRVHPMModelParameterised.ModelParameters modelParameters = HGTRVHPMModelParameterised.ModelParameters.FIXES_AND_AABB;
    	final double equilibriumTemperatureMinLondon2018Soft[] = new double[1];
    	final DDNTemperatureDataCSV temperaturesLondon2018Soft =
    			DDNTemperatureDataCSV.loadDDNTemperatureDataCSV(DDNTemperatureDataCSV.DATA_EGLL_2018);
    	final HGTRVHPMModelByHour scenarioLondon2018Soft = new HGTRVHPMModelByHour(
    			modelParameters, temperaturesLondon2018Soft);
    	final ScenarioResult resultLondon2018Soft = scenarioLondon2018Soft.runScenario(false, true, equilibriumTemperatureMinLondon2018Soft);
        assertEquals(18.2, equilibriumTemperatureMinLondon2018Soft[0], 0.1);
        assertEquals(0, resultLondon2018Soft.hoursFractionSetbackRaisesDemand(), 0.001);
        assertEquals(719, resultLondon2018Soft.demand().noSetback().heatDemand(), 1);
        assertEquals(561, resultLondon2018Soft.demand().withSetback().heatDemand(), 1);
        assertEquals(246, resultLondon2018Soft.demand().noSetback().heatPumpElectricity(), 1);
        assertEquals(193, resultLondon2018Soft.demand().withSetback().heatPumpElectricity(), 1);
	    }
    }
