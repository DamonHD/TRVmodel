package org.hd.d.TRVmodel.hg;

import org.hd.d.TRVmodel.hg.HGTRVHPMModelParameterised.ModelParameters;

/**Show computations from the Heat Geek TRV/HP model. */
public final class ShowComputations
	{
	/**Prevent creation of an instance. */
    private ShowComputations() { }

    /**Print computations on stdout. */
	public static void showCalcs()
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
        for(double eat = HGTRVHPMModel.EXTERNAL_AIR_TEMPERATURE_C; eat < HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C; eat += 1.0)
	        {
        	final HGTRVHPMModelParameterised.ModelParameters params = new ModelParameters(
        			ModelParameters.FIXED_DOORS_PER_INTERNAL_WALL,
        			ModelParameters.FIXED_CORRECT_COP_FOR_FLOW_TEMPERATURE,
        			false,
        			eat);
        	System.out.println(String.format("  %s, electricity demand normal / setback: %.0fW / %.0fW",
    			params.toString(),
        	    HGTRVHPMModelParameterised.computeHPElectricityDemandW(params, false),
        	    HGTRVHPMModelParameterised.computeHPElectricityDemandW(params, true) ));
	        }



		// TODO

		}
	}
