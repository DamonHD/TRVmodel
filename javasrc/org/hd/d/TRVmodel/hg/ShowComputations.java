package org.hd.d.TRVmodel.hg;

import java.io.IOException;

import org.hd.d.TRVmodel.data.DDNTemperatureDataCSV;
import org.hd.d.TRVmodel.hg.HGTRVHPMModelByHour.ScenarioResult;
import org.hd.d.TRVmodel.hg.HGTRVHPMModelParameterised.ModelParameters;

/**Show computations from the Heat Geek TRV/HP model. */
public final class ShowComputations
	{
	/**Prevent creation of an instance. */
    private ShowComputations() { }

    /**Print computations on stdout.
     * @throws IOException
     */
	public static void showCalcs() throws IOException
		{
		System.out.println("Show HG TRV/HP model computations.");

		System.out.println(String.format("Hardwired model, electricity demand normal / setback: %.0fW / %.0fW",
			HGTRVHPMModel.HEAT_PUMP_POWER_IN_NO_SETBACK_W,
			HGTRVHPMModel.HEAT_PUMP_POWER_IN_B_SETBACK_W));

    	final HGTRVHPMModelParameterised.ModelParameters defaultParams = new HGTRVHPMModelParameterised.ModelParameters();
		System.out.println(String.format("Parameterised model, all default parameters, electricity demand normal / setback: %.0fW / %.0fW",
    	    HGTRVHPMModelParameterised.computeHPElectricityDemandW(defaultParams, false),
    	    HGTRVHPMModelParameterised.computeHPElectricityDemandW(defaultParams, true)));

		System.out.println(String.format("Parameterised model, fixes applied for doors and CoP temperature, electricity demand normal / setback: %.0fW / %.0fW",
    	    HGTRVHPMModelParameterised.computeHPElectricityDemandW(HGTRVHPMModelParameterised.ModelParameters.FIXES_APPLIED, false),
    	    HGTRVHPMModelParameterised.computeHPElectricityDemandW(HGTRVHPMModelParameterised.ModelParameters.FIXES_APPLIED, true)));

		System.out.println(String.format("Parameterised model, fixes applied and AABB lower-loss arrangement, electricity demand normal / setback: %.0fW / %.0fW",
    	    HGTRVHPMModelParameterised.computeHPElectricityDemandW(HGTRVHPMModelParameterised.ModelParameters.FIXES_AND_AABB, false),
    	    HGTRVHPMModelParameterised.computeHPElectricityDemandW(HGTRVHPMModelParameterised.ModelParameters.FIXES_AND_AABB, true)));


        System.out.println();
        System.out.println("Parameterised model, fixes applied for doors and CoP temperature, external air temperature varied...");
//        final double eat = HGTRVHPMModel.EXTERNAL_AIR_TEMPERATURE_C;
        for(double eat = HGTRVHPMModel.EXTERNAL_AIR_TEMPERATURE_C - 10.0; eat < HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C; eat += 1.0)
	        {
        	final HGTRVHPMModelParameterised.ModelParameters params = new ModelParameters(
        			ModelParameters.FIXED_DOORS_PER_INTERNAL_WALL,
        			ModelParameters.FIXED_CORRECT_COP_FOR_FLOW_TEMPERATURE,
        			ModelParameters.DEFAULT_ARRANGEMENT_ABAB,
        			eat);
        	if(Math.abs(HGTRVHPMModel.EXTERNAL_AIR_TEMPERATURE_C - eat) < 0.1)
	        	{
        		System.out.println(String.format(" *** original external air temperature, ie %.1fC", eat));
	        	}
        	System.out.println(String.format("  %s, electricity demand normal / setback: %.0fW / %.0fW",
    			params.toString(),
        	    HGTRVHPMModelParameterised.computeHPElectricityDemandW(params, false),
        	    HGTRVHPMModelParameterised.computeHPElectricityDemandW(params, true) ));
	        }


        System.out.println("");
        System.out.println("Parameterised model, fixes applied for doors and CoP temperature, external air temperature varied...");
        System.out.println("London (EGLL) 2018 hourly temperatures");
    	final DDNTemperatureDataCSV temperaturesLondon2018 =
    			DDNTemperatureDataCSV.loadDDNTemperatureDataCSV(DDNTemperatureDataCSV.DATA_EGLL_2018);
    	final HGTRVHPMModelByHour scenarioLondon2018 = new HGTRVHPMModelByHour(
    			HGTRVHPMModelParameterised.ModelParameters.FIXES_APPLIED, temperaturesLondon2018);
    	final ScenarioResult resultLondon2018 = scenarioLondon2018.runScenario();
        System.out.println(String.format("Percentage of hours that room setback raises heat pump demand: %.0f%%",
        		100 * resultLondon2018.hoursFractionSetbackRaisesDemand()));
    	final double heatNoSetbackLondon2018 = resultLondon2018.demand().noSetback().heatDemand();
    	final double heatWithSetbackLondon2018 = resultLondon2018.demand().withSetback().heatDemand();
    	System.out.println(String.format("Heat mean demand: with no setback %.0fW, with setback %.0fW; %.0f%% change with setback",
    			heatNoSetbackLondon2018, heatWithSetbackLondon2018, 100*((heatWithSetbackLondon2018/heatNoSetbackLondon2018)-1)));
    	final double powerNoSetbackLondon2018 = resultLondon2018.demand().noSetback().heatPumpElectricity();
    	final double powerWithSetbackLondon2018 = resultLondon2018.demand().withSetback().heatPumpElectricity();
    	System.out.println(String.format("Heat pump mean power: with no setback %.0fW, with setback %.0fW; %.0f%% change with setback",
    			powerNoSetbackLondon2018, powerWithSetbackLondon2018, 100*((powerWithSetbackLondon2018/powerNoSetbackLondon2018)-1)));

        System.out.println("");
        System.out.println("Parameterised model, fixes applied for doors and CoP temperature, external air temperature varied...");
        System.out.println("Glasgow (EGPF) 2018 hourly temperatures");
    	final DDNTemperatureDataCSV temperaturesGlasgow2018 =
    			DDNTemperatureDataCSV.loadDDNTemperatureDataCSV(DDNTemperatureDataCSV.DATA_EGPF_2018);
    	final HGTRVHPMModelByHour scenarioGlasgow2018 = new HGTRVHPMModelByHour(
    			HGTRVHPMModelParameterised.ModelParameters.FIXES_APPLIED, temperaturesGlasgow2018);
    	final ScenarioResult resultGlasgow2018 = scenarioGlasgow2018.runScenario();
        System.out.println(String.format("Percentage of hours that room setback raises heat pump demand: %.0f%%",
        		100 * resultGlasgow2018.hoursFractionSetbackRaisesDemand()));
    	final double heatNoSetbackGlasgow2018 = resultGlasgow2018.demand().noSetback().heatDemand();
    	final double heatWithSetbackGlasgow2018 = resultGlasgow2018.demand().withSetback().heatDemand();
    	System.out.println(String.format("Heat mean demand: with no setback %.0fW, with setback %.0fW; %.0f%% change with setback",
    			heatNoSetbackGlasgow2018, heatWithSetbackGlasgow2018, 100*((heatWithSetbackGlasgow2018/heatNoSetbackGlasgow2018)-1)));
    	final double powerNoSetbackGlasgow2018 = resultGlasgow2018.demand().noSetback().heatPumpElectricity();
    	final double powerWithSetbackGlasgow2018 = resultGlasgow2018.demand().withSetback().heatPumpElectricity();
    	System.out.println(String.format("Heat pump mean power: with no setback %.0fW, with setback %.0fW; %.0f%% change with setback",
    			powerNoSetbackGlasgow2018, powerWithSetbackGlasgow2018, 100*((powerWithSetbackGlasgow2018/powerNoSetbackGlasgow2018)-1)));



		// TODO

		}
	}
