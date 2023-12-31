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
import org.hd.d.TRVmodel.hg.HGTRVHPMModelParameterised.ModelParameters;

import junit.framework.TestCase;

/**Test the parameterised Heat Geek TRV-with-HP model. */
public final class TestHGTRVHPModelParameterised extends TestCase
    {
    /**Test with default (as published page) parameters. */
    public static void testWithDefaultParameters()
	    {
    	final HGTRVHPMModelParameterised.ModelParameters defaultParams = new HGTRVHPMModelParameterised.ModelParameters();
    	final double powerNoSetback = HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(defaultParams, false);
    	final double powerWithSetback = HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(defaultParams, true);
	    assertEquals(HGTRVHPMModel.HEAT_PUMP_POWER_IN_NO_SETBACK_W, powerNoSetback, 1);
	    assertEquals(HGTRVHPMModel.HEAT_PUMP_POWER_IN_B_SETBACK_W, powerWithSetback, 1);

	    // The overall point of this Heat Geek example!
	    assertTrue("electrical power goes UP with B rooms set back", powerNoSetback < powerWithSetback);
	    }

    /**Test the CoP computation. */
    public static void testComputeFlowCoP()
	    {
	    assertEquals("must reproduce the original quoted value", HGTRVHPMModel.CoPL, HGTRVHPMModelParameterised.computeFlowCoP(46.0), 0.01);
	    assertEquals("must reproduce the original quoted value", HGTRVHPMModel.CoPH, HGTRVHPMModelParameterised.computeFlowCoP(51.5), 0.01);
	    final double intermediateTempC = 50.0;
	    assertTrue("should interpolate an intermediate value (inverse relationship)", HGTRVHPMModel.CoPL > HGTRVHPMModelParameterised.computeFlowCoP(intermediateTempC));
	    assertTrue("should interpolate an intermediate value (inverse relationship)", HGTRVHPMModel.CoPH < HGTRVHPMModelParameterised.computeFlowCoP(intermediateTempC));
	    final double lowTempC = 35.0;
	    assertTrue("should extrapolate low", HGTRVHPMModel.CoPL < HGTRVHPMModelParameterised.computeFlowCoP(lowTempC));
	    final double highTempC = 60.0;
	    assertTrue("should extrapolate high", HGTRVHPMModel.CoPH > HGTRVHPMModelParameterised.computeFlowCoP(highTempC));
	    }

    /**Test with parameters at default except one full door per internal wall. */
    public static void testOneDoorPerInternalWallCorrection()
	    {
    	final HGTRVHPMModelParameterised.ModelParameters oneDoor = new HGTRVHPMModelParameterised.ModelParameters(
			HGTRVHPMModelParameterised.ModelParameters.FIXED_DOORS_PER_INTERNAL_WALL);
    	final double powerNoSetback = HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(oneDoor, false);
    	final double powerWithSetback = HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(oneDoor, true);
	    assertEquals("doors-per-internal-wall should not affect no-setback case",
    		HGTRVHPMModel.HEAT_PUMP_POWER_IN_NO_SETBACK_W, powerNoSetback, 0.5);
	    assertTrue("doors-per-internal-wall should increase intra-room loss and overall electricity demand",
    		HGTRVHPMModel.HEAT_PUMP_POWER_IN_B_SETBACK_W < powerWithSetback);
	    assertTrue("electrical power goes UP with B rooms set back", powerNoSetback < powerWithSetback);
	    }

    /**Test with parameters at default except CoP correction to use flow rather than radiator mean water temperature. */
    public static void testFlowTemperatureCorrection()
	    {
    	final HGTRVHPMModelParameterised.ModelParameters fixCoP = new HGTRVHPMModelParameterised.ModelParameters(
			HGTRVHPMModelParameterised.ModelParameters.DEFAULT_DOORS_PER_INTERNAL_WALL,
			HGTRVHPMModelParameterised.ModelParameters.FIXED_CORRECT_COP_FOR_FLOW_TEMPERATURE);
    	final double powerNoSetback = HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(fixCoP, false);
    	final double powerWithSetback = HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(fixCoP, true);
	    assertTrue("CoP correction should increase overall electricity demand",
    		HGTRVHPMModel.HEAT_PUMP_POWER_IN_NO_SETBACK_W < powerNoSetback);
	    assertTrue("CoP correction should increase overall electricity demand",
    		HGTRVHPMModel.HEAT_PUMP_POWER_IN_B_SETBACK_W < powerWithSetback);
	    assertTrue("electrical power goes UP with B rooms set back", powerNoSetback < powerWithSetback);
	    }

    /**Test with parameters at default except corrections for CoP and doors. */
    public static void testCorrections()
	    {
    	final HGTRVHPMModelParameterised.ModelParameters corrections = new HGTRVHPMModelParameterised.ModelParameters(
			HGTRVHPMModelParameterised.ModelParameters.FIXED_DOORS_PER_INTERNAL_WALL,
			HGTRVHPMModelParameterised.ModelParameters.FIXED_CORRECT_COP_FOR_FLOW_TEMPERATURE);
    	final double powerNoSetback = HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(corrections, false);
    	final double powerWithSetback = HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(corrections, true);
	    assertTrue("corrections should increase overall electricity demand",
    		HGTRVHPMModel.HEAT_PUMP_POWER_IN_NO_SETBACK_W < powerNoSetback);
	    assertTrue("corrections should increase overall electricity demand",
    		HGTRVHPMModel.HEAT_PUMP_POWER_IN_B_SETBACK_W < powerWithSetback);
	    assertTrue("electrical power goes UP with B rooms set back", powerNoSetback < powerWithSetback);
	    }

    /**Test with corrections for CoP and doors and AABB room arrangement. */
    public static void testCorrectionsAndAABB()
	    {
    	final HGTRVHPMModelParameterised.ModelParameters correctionsAndAABB = HGTRVHPMModelParameterised.ModelParameters.FIXES_AND_AABB;
    	final double powerNoSetback = HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(correctionsAndAABB, false);
    	final double powerWithSetback = HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(correctionsAndAABB, true);
	    assertTrue("corrections even with AABB layout should increase overall electricity demand",
    		HGTRVHPMModel.HEAT_PUMP_POWER_IN_NO_SETBACK_W < powerNoSetback);
	    assertTrue("corrections even with AABB layout should increase overall electricity demand",
    		HGTRVHPMModel.HEAT_PUMP_POWER_IN_B_SETBACK_W < powerWithSetback);
	    assertTrue("electrical power goes UP with B rooms set back", powerNoSetback < powerWithSetback);
	    }

    /**Test without corrections and explicitly with original external air temperature. */
    public static void testVariableExternalAirTemperaturePerOriginal()
	    {
    	final double eat = HGTRVHPMModel.EXTERNAL_AIR_TEMPERATURE_C;
      	final HGTRVHPMModelParameterised.ModelParameters params = new ModelParameters(
      			ModelParameters.DEFAULT_DOORS_PER_INTERNAL_WALL,
      			ModelParameters.DEFAULT_CORRECT_COP_FOR_FLOW_TEMPERATURE,
      			ModelParameters.DEFAULT_ARRANGEMENT_ABAB,
      			eat);
    	final double powerNoSetback = HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(params, false);
    	final double powerWithSetback = HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(params, true);
	    assertEquals(HGTRVHPMModel.HEAT_PUMP_POWER_IN_NO_SETBACK_W, powerNoSetback, 1);
	    assertEquals(HGTRVHPMModel.HEAT_PUMP_POWER_IN_B_SETBACK_W, powerWithSetback, 1);
	    }

    /**Test without corrections that lowering external air temperature raises electricity demand. */
    public static void testVariableExternalAirTemperatureBelowOriginal()
	    {
    	final double eat = HGTRVHPMModel.EXTERNAL_AIR_TEMPERATURE_C - 1.0;
      	final HGTRVHPMModelParameterised.ModelParameters params = new ModelParameters(
      			ModelParameters.DEFAULT_DOORS_PER_INTERNAL_WALL,
      			ModelParameters.DEFAULT_CORRECT_COP_FOR_FLOW_TEMPERATURE,
      			ModelParameters.DEFAULT_ARRANGEMENT_ABAB,
      			eat);
    	final double powerNoSetback = HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(params, false);
    	final double powerWithSetback = HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(params, true);
	    assertTrue("lowering external air temperature should increase overall electricity demand unsetback",
    		HGTRVHPMModel.HEAT_PUMP_POWER_IN_NO_SETBACK_W < powerNoSetback);
	    assertTrue("lowering external air temperature should increase overall electricity demand setback",
    		HGTRVHPMModel.HEAT_PUMP_POWER_IN_B_SETBACK_W < powerWithSetback);
	    }

    /**Test without corrections that raising external air temperature lowers electricity demand. */
    public static void testVariableExternalAirTemperatureAboveOriginal()
	    {
    	final double eat = HGTRVHPMModel.EXTERNAL_AIR_TEMPERATURE_C + 1.0;
      	final HGTRVHPMModelParameterised.ModelParameters params = new ModelParameters(
      			ModelParameters.DEFAULT_DOORS_PER_INTERNAL_WALL,
      			ModelParameters.DEFAULT_CORRECT_COP_FOR_FLOW_TEMPERATURE,
      			ModelParameters.DEFAULT_ARRANGEMENT_ABAB,
      			eat);
    	final double powerNoSetback = HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(params, false);
    	final double powerWithSetback = HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(params, true);
	    assertTrue("raising external air temperature should lower overall electricity demand unsetback",
    		HGTRVHPMModel.HEAT_PUMP_POWER_IN_NO_SETBACK_W > powerNoSetback);
	    assertTrue("raising external air temperature should lower overall electricity demand setback",
    		HGTRVHPMModel.HEAT_PUMP_POWER_IN_B_SETBACK_W > powerWithSetback);
	    }

    /**Test (with corrections) that there is an external temperature above which expected TRV energy-saving behaviour returns. */
    public static void testForEATThresholdForSetbackSavingsBehaviour()
	    {
    	final double thresholdEAT = 10.0;
      	final HGTRVHPMModelParameterised.ModelParameters paramsBelowThreshold = new ModelParameters(
      			HGTRVHPMModelParameterised.ModelParameters.FIXED_DOORS_PER_INTERNAL_WALL,
    			HGTRVHPMModelParameterised.ModelParameters.FIXED_CORRECT_COP_FOR_FLOW_TEMPERATURE,
      			ModelParameters.DEFAULT_ARRANGEMENT_ABAB,
      			thresholdEAT - 1.0);
     	final HGTRVHPMModelParameterised.ModelParameters paramsAboveThreshold = new ModelParameters(
      			HGTRVHPMModelParameterised.ModelParameters.FIXED_DOORS_PER_INTERNAL_WALL,
    			HGTRVHPMModelParameterised.ModelParameters.FIXED_CORRECT_COP_FOR_FLOW_TEMPERATURE,
      			ModelParameters.DEFAULT_ARRANGEMENT_ABAB,
      			thresholdEAT + 1.0);
	    assertTrue("electrical power goes UP with B rooms set back below EAT threshold",
	    		HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(paramsBelowThreshold, false) <
	    		HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(paramsBelowThreshold, true));
	    assertTrue("electrical power goes DOWN with B rooms set back above EAT threshold",
	    		HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(paramsAboveThreshold, false) >
	    		HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(paramsAboveThreshold, true));
	    }
    }
