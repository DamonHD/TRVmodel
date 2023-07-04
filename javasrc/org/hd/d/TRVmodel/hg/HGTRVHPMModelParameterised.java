package org.hd.d.TRVmodel.hg;

import java.util.Objects;

/**Parameterised recreation of the heat-pump / TRV / energy interactions from Heat Geek's page:
 * <a href="https://www.heatgeek.com/why-not-to-zone-heat-pumps-or-boilers/">https://www.heatgeek.com/why-not-to-zone-heat-pumps-or-boilers/</a>
 * as of ~2023-06.
 * <p>
 * This is examining if turning down TRVs in unused rooms saves energy or not,
 * as is commonly assumed eg with gas-fired heating.
 * <p>
 * Many thanks to Heat Geek for making this analysis and explanation available.
 * <p>
 * This is a simple model, mirroring the page above.
 * <p>
 * This model is steady-state, ie in temperature equilibrium.
 */
public final class HGTRVHPMModelParameterised
 	{
	/**Prevent creation of an instance. */
    private HGTRVHPMModelParameterised() { }

    /**Parameters for this version of the model.
     * Defaults should yield the same
     *
     * @param  doorsPerInternalWall  was 0.5 in the original page calcs (but 1 in the text);
     *     expected to be in range [0,1], must be finite and positive
     */
    public record ModelParameters(
    		double doorsPerInternalWall,
    		boolean correctCoPForFlowVsMW,
    		boolean roomsAlternatingABAB,
    		double externalAirTemperatureC
    		)
	    {
    	public ModelParameters
			{
    		// Sanity-check parameters.
    		if(!Double.isFinite(doorsPerInternalWall)) { throw new IllegalArgumentException(); }
    		if(doorsPerInternalWall < 0) { throw new IllegalArgumentException(); }
			}

    	/**All-defaults parameter set the should produce original model results. */
    	public ModelParameters()
	    	{
	    	this(
    			DEFAULT_DOORS_PER_INTERNAL_WALL,
    			DEFAULT_CORRECT_COP_FOR_FLOW_TEMPERATURE,
    			DEFAULT_ARRANGEMENT_ABAB,
    			DEFAULT_EXTERNAL_AIR_TEMPERATURE_C
    			);
	    	}

    	/**Allow doors per internal wall to be set, all else defaults. */
    	public ModelParameters(final double doorsPerInternalWall, final boolean correctCoPForFlowVsMW)
    	    { this(doorsPerInternalWall, correctCoPForFlowVsMW, DEFAULT_ARRANGEMENT_ABAB, DEFAULT_EXTERNAL_AIR_TEMPERATURE_C); }

    	/**Allow doors per internal wall to be set, all else defaults. */
    	public ModelParameters(final double doorsPerInternalWall)
    	    { this(doorsPerInternalWall, DEFAULT_CORRECT_COP_FOR_FLOW_TEMPERATURE); }

        /**Default doors per internal wall: matches the 0.5 in calcs on the original page. */
    	public static final double DEFAULT_DOORS_PER_INTERNAL_WALL = 0.5;
    	/**Default correction for CoP for flow rather than radiator mean water temperature. */
    	public static final boolean DEFAULT_CORRECT_COP_FOR_FLOW_TEMPERATURE = false;
    	/**Default room arrangement ABAB (vs AABB) alternating as original calcs. */
    	public static final boolean DEFAULT_ARRANGEMENT_ABAB = true;
    	/**Default external temperature (C). */
    	public static final double DEFAULT_EXTERNAL_AIR_TEMPERATURE_C = HGTRVHPMModel.EXTERNAL_AIR_TEMPERATURE_C;

        /**Fixed doors per internal wall: matches the 1 in text on the original page. */
    	public static final double FIXED_DOORS_PER_INTERNAL_WALL = 1.0;
    	/**Fixed correction for CoP for flow rather than radiator mean water temperature. */
    	public static final boolean FIXED_CORRECT_COP_FOR_FLOW_TEMPERATURE = true;
        /**Version of parameters with fixes applied but no other changes from defaults. */
    	public static final ModelParameters FIXES_APPLIED =
			new ModelParameters(
    			ModelParameters.FIXED_DOORS_PER_INTERNAL_WALL,
    			ModelParameters.FIXED_CORRECT_COP_FOR_FLOW_TEMPERATURE);

    	/**AABB minimal-loss room arrangement. */
    	public static final ModelParameters FIXES_AND_AABB =
			new ModelParameters(
    			ModelParameters.FIXED_DOORS_PER_INTERNAL_WALL,
    			ModelParameters.FIXED_CORRECT_COP_FOR_FLOW_TEMPERATURE,
    			false,
    			DEFAULT_EXTERNAL_AIR_TEMPERATURE_C);
	    }

    /**Estimate the CoP for a given flow temperature (C) given the two supplied data points.
     * This does a simple linear fit, which is not perfect but probably adequate.
     * <p>
     * Note this is for flow temperature, given the sample manufacturer's data,
     * though the original text uses this interchangeably with mid temperature.
     */
    public static double computeFlowCoP(final double flowC)
	    {
    	final double lowerTempC = 46.0;
    	final double upperTempC = 51.5;
    	final double tempDeltaK = upperTempC - lowerTempC;
    	final double CoPDelta = HGTRVHPMModel.COP_AT_51p5C - HGTRVHPMModel.COP_AT_46p0C;

    	final double CoP = HGTRVHPMModel.COP_AT_46p0C +
    			((flowC - lowerTempC) * (CoPDelta / tempDeltaK));

    	return(CoP);
	    }

    /**Compute the heat-pump electricity demand (W); zero or more.
     * The calculation uses constants from HGTRVHPMModel as far as possible,
     * substituting in parameters and new calculation where needed.
     *
     * @param params  the variable model parameters
     * @param withBSetback  if true, with B rooms set back, else all at same temperature
     * @return  demand in watts, finite and non-negative
     */
    public static double computeHPElectricityDemandW(final ModelParameters params, final boolean withBSetback)
	    {
    	Objects.requireNonNull(params);

    	// Parameterisation not yet fully handled...
    	if(params.externalAirTemperatureC != ModelParameters.DEFAULT_EXTERNAL_AIR_TEMPERATURE_C) { throw new UnsupportedOperationException("NOT IMPLEMENTED YET"); }


    	// Do not allow model to be run with potentially implausible parameters.
    	if(params.externalAirTemperatureC >= HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C)
    	    { throw new UnsupportedOperationException("model may not work when outside is warmer than setback rooms"); }




///**tExt: external air temperature, ie design temperature on cold winter day, (Celsius). */
//public static final double EXTERNAL_AIR_TEMPERATURE_C = -3;
///**tInt: 'normal' room temperature (Celsius). */
//public static final double NORMAL_ROOM_TEMPERATURE_C = 21;
///**HLDT: design temperature (cold winter day) for heat loss calculations (Kelvin). */
//public static final double HOME_HEAT_LOSS_DESIGN_TEMPERATURE_DELTA_K =
//	(NORMAL_ROOM_TEMPERATURE_C - EXTERNAL_AIR_TEMPERATURE_C);
///**hlW: (Flow Temperature, step 1) heat loss with all rooms at normal internal temperature (W). */
//public static final double HOME_HEAT_LOSS_AT_NORMAL_ROOM_TEMPERATURE_W = 2000;
///**radW: pre-setback radiator output (W). */
//public static final double RADIATOR_POWER_WITH_HOME_AT_NORMAL_ROOM_TEMPERATURE_W = 500;
///**radMWATdT: no-setback Mean Water to Air Temperature delta T (K). */
//public static final double RADIATOR_MWATDT_AT_NORMAL_ROOM_TEMPERATURE_K = 25;


///**HLpK: (Flow Temperature, step 1) heat loss per Kelvin (W/K). */
//public static final double HOME_HEAT_LOSS_PER_KELVIN_WpK =
//	HOME_HEAT_LOSS_AT_NORMAL_ROOM_TEMPERATURE_W / HOME_HEAT_LOSS_DESIGN_TEMPERATURE_DELTA_K;
//
///**tIntSetback: setback/unused room temperature (Celsius). */
//public static final double SETBACK_ROOM_TEMPERATURE_C = 18;
///**tMeanWhenSetback: (Flow Temperature, step 2) mean home temperature when B rooms setback (Celcius). */
//public static final double MEAN_HOME_TEMPERATURE_WITH_SETBACK_C =
//	(NORMAL_ROOM_TEMPERATURE_C + SETBACK_ROOM_TEMPERATURE_C) / 2;
///**HLsbW: (Flow Temperature, step 3) heat loss with B rooms setback (W). */
//public static final double HOME_HEAT_LOSS_B_SETBACK_W = HOME_HEAT_LOSS_PER_KELVIN_WpK *
//		(MEAN_HOME_TEMPERATURE_WITH_SETBACK_C - EXTERNAL_AIR_TEMPERATURE_C);
///**HLfall: (Flow Temperature, step 3) reduction in home heat loss with B set back. */
//public static final double HOME_HEAT_LOSS_FALL_B_SETBACK =
//	(HOME_HEAT_LOSS_AT_NORMAL_ROOM_TEMPERATURE_W - HOME_HEAT_LOSS_B_SETBACK_W) /
//		HOME_HEAT_LOSS_AT_NORMAL_ROOM_TEMPERATURE_W;



    	// Whole home heat loss with no setback (all rooms same temperature) and given external air temperature (W).
        final double HHL = (HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C - params.externalAirTemperatureC()) *
        		HGTRVHPMModel.HOME_HEAT_LOSS_PER_KELVIN_WpK;
    	// Whole home heat loss with B rooms setback and given external air temperature (W).
        final double HHLsb = (HGTRVHPMModel.MEAN_HOME_TEMPERATURE_WITH_SETBACK_C - params.externalAirTemperatureC()) *
        		HGTRVHPMModel.HOME_HEAT_LOSS_PER_KELVIN_WpK;


        // HEAT LOSS 1
    	// IWAabmd: (Heat Loss 1.2) internal wall area between each A and adjoining B rooms minus appropriate amount of door (m^2).
    	// (INTERNAL_WALL_AREA_FROM_EACH_A_TO_B_ROOMS_MINUS_DOOR_M2)
    	final double IWAabmd =
			HGTRVHPMModel.INTERNAL_WALL_AREA_FROM_EACH_A_TO_B_ROOMS_M2
			- (2 * params.doorsPerInternalWall() * HGTRVHPMModel.INTERNAL_DOOR_AREA_PER_DOOR_M2);
    	// IWAabHL: (Heat Loss 1.3) internal wall (minus door) heat loss per Kelvin (W/K).
    	// (INTERNAL_WALL_MINUS_DOOR_HEAT_LOSS_PER_KELVIN_WpK)
        final double IWAabHL =
    		IWAabmd * HGTRVHPMModel.INTERNAL_WALL_U_WpM2K;
        // IWAabHLW: (Heat Loss 1.4) internal wall (minus door) heat loss (WK).
        // (INTERNAL_WALL_MINUS_DOOR_HEAT_LOSS_W)
        final double IWAabHLW =
    		IWAabHL *
    			(HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C - HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C);
        // IDAabHL: (Heat Loss 1.5) internal door heat loss per door per Kelvin (W/K).
        // (INTERNAL_DOOR_HEAT_LOSS_PER_KELVIN_WpK)
        final double IDAabHL =
    		HGTRVHPMModel.INTERNAL_DOOR_AREA_PER_DOOR_M2 * HGTRVHPMModel.INTERNAL_DOOR_U_WpM2K;
        // IDAabHLW: (Heat Loss 1.6) internal door heat loss per door (W).
        // (INTERNAL_DOOR_HEAT_LOSS_W)
        final double IDAabHLW =
    		IDAabHL *
    		    (HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C - HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C);
        // IDWAabHLW: (Heat Loss 1.7) internal wall and door heat loss per A room (W).
        // (INTERNAL_WALL_AND_DOOR_HEAT_LOSS_PER_A_ROOM_W)
        // In the original ABAB arrangement there are two walls from each A room into B rooms.
        // In the alternate AABB arrangement there is one wall from each A room into a B room.
        // Thus AABB has half the internal heat loss of ABAB.
        final double IDWAabHLW = ((params.roomsAlternatingABAB) ? 1 : 0.5) *
    		IWAabHLW + (2 * params.doorsPerInternalWall() * IDAabHLW);

        // HEAT LOSS 2
        // radWAbs: (Heat Loss 2.0) radiator output in each A room when B setback (W).
        // (RADIATOR_POWER_IN_A_ROOMS_WHEN_B_SETBACK_W)
        final double radWAbs =
    		HGTRVHPMModel.RADIATOR_POWER_WITH_HOME_AT_NORMAL_ROOM_TEMPERATURE_W + IDWAabHLW;
        // radWBbs: (Heat Loss 2.0) radiator output in each B room when B setback (W).
        // (RADIATOR_POWER_IN_B_ROOMS_WHEN_B_SETBACK_W)
// TODO: why unused: radWBbs
        final double radWBbs =
    		(HGTRVHPMModel.HOME_HEAT_LOSS_B_SETBACK_W - 2*radWAbs) / 2;
        // radWAmult: (Heat Loss 2.1) radiator output increase multiplier in each A room when B setback.
        // (RADIATOR_POWER_UPLIFT_IN_A_ROOMS_WHEN_B_SETBACK_MULTIPLIER)
        final double radWAmult =
    		radWAbs / HGTRVHPMModel.RADIATOR_POWER_WITH_HOME_AT_NORMAL_ROOM_TEMPERATURE_W;
        // radAbsdTmult: (Heat Loss 2.3) radiator MW-AT delta-T increase multiplier in each A room when B setback.
        // (RADIATOR_DT_UPLIFT_IN_A_ROOMS_WHEN_B_SETBACK_MULTIPLIER)
        final double radAbsdTmult =
    		Math.pow(radWAmult, HGTRVHPMModel.RADIATOR_EXP_POWER_TO_DT);
        // radAbsdT: (Heat Loss 2.4) radiator MW-AT delta-T in each A room when B setback (K).
        // (RADIATOR_DT_IN_A_ROOMS_WHEN_B_SETBACK_K)
        final double radAbsdT =
    		HGTRVHPMModel.RADIATOR_MWATDT_AT_NORMAL_ROOM_TEMPERATURE_K * radAbsdTmult;
        // radAbsMW: (Heat Loss 2.5) radiator mean water temperature in each A room when B setback (C).
        // (RADIATOR_MW_IN_A_ROOMS_WHEN_B_SETBACK_C)
        final double radAbsMW =
    		HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C + radAbsdT;

        // Normal (no setback) mean water temperature (C).
        final double radAMW =
            HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C + HGTRVHPMModel.RADIATOR_MWATDT_AT_NORMAL_ROOM_TEMPERATURE_K;

        // Assumed delta between MW and flow temperature (5K system delta).
        final double flowMWDelta = 2.5;
        final double CoPCorrectionK = params.correctCoPForFlowVsMW ? flowMWDelta : 0;

        // HPinWnsb: (Heat Pump Efficiency) heat-pump electrical power in when B not setback (W).
        // (HEAT_PUMP_POWER_IN_NO_SETBACK_W)
        // Note that flow and mean temperatures seem to be being mixed here.
        final double HPinWnsb =
    		HGTRVHPMModel.HOME_HEAT_LOSS_AT_NORMAL_ROOM_TEMPERATURE_W / computeFlowCoP(radAMW + CoPCorrectionK);
        // HPinWsb: (Heat Pump Efficiency) heat-pump electrical power in when B is setback (W).
        // (HEAT_PUMP_POWER_IN_B_SETBACK_W)
        // Note that flow and mean temperatures seem to be being mixed here.
        final double HPinWsb =
    		HGTRVHPMModel.HOME_HEAT_LOSS_B_SETBACK_W / computeFlowCoP(radAbsMW + CoPCorrectionK);

    	return(withBSetback ? HPinWsb : HPinWnsb);
	    }
 	}
