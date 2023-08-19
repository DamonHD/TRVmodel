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
     * @param correctCoPForFlowVsMW  if true then correct CoP flow temperature
     *     (original page implies false)
     * @param roomsAlternatingABAB  if true then arrange rooms to maximise internal heat transfer/loss
     *     (original page implies true)
     * @param externalAirTemperatureC  external air temperature, degrees C;
     *     (original page has -3&deg;C)
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


    	/**Clone but replace external temperature value; all other parameters unchanged. */
    	public ModelParameters cloneWithAdjustedExternalTemperature(final double newExternalTemperature)
	    	{
	    	if(!Double.isFinite(newExternalTemperature)) { throw new IllegalArgumentException(); }
	    	return(new ModelParameters(
	    			doorsPerInternalWall,
	        		correctCoPForFlowVsMW,
	        		roomsAlternatingABAB,
	        		newExternalTemperature));
	    	}


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

    /**Heat demand and the electricity input to the heat pump to meet it (W).
     * @param heatDemand  raw heat demand to maintain home temperature; finite, non-negative
     * @param heatPumpElectricity  electricity into the heat pump to maintain home temperature; finite, non-negative
     */
    public record HeatAndElectricityDemand(double heatDemand, double heatPumpElectricity)
	    {
	    public HeatAndElectricityDemand
		    {
    		// Sanity-check parameters.
    		if(!Double.isFinite(heatDemand)) { throw new IllegalArgumentException(); }
    		if(heatDemand < 0) { throw new IllegalArgumentException(); }
    		if(!Double.isFinite(heatPumpElectricity)) { throw new IllegalArgumentException(); }
    		if(heatPumpElectricity < 0) { throw new IllegalArgumentException(); }
		    }
	    }

    /**Home heat and electricity demand with and without setback in B rooms; both non-null. */
    public record DemandWithoutAndWithSetback(HeatAndElectricityDemand noSetback, HeatAndElectricityDemand withSetback)
	    {
    	public DemandWithoutAndWithSetback
	    	{
    		Objects.requireNonNull(noSetback);
    		Objects.requireNonNull(withSetback);
	    	}
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

    /**Assumed delta between MW and flow temperature with 5K system delta (K). */
    public static final double flowMWDelta_K = 2.5;

    /**Compute the original HG 4-room 'bungalow' heat-pump electricity demand (W); zero or more.
     * The calculation uses constants from HGTRVHPMModel as far as possible,
     * substituting in parameters and new calculation where needed.
     *
     * @param params  the variable model parameters
     * @param withBSetback  if true, with B rooms set back, else all at same temperature
     * @return  demand in watts, finite and non-negative
     */
    public static double computeBungalowHPElectricityDemandW(final ModelParameters params, final boolean withBSetback)
	    {
    	Objects.requireNonNull(params);
    	return(withBSetback ?
    			computeBungalowDemandW(params).withSetback.heatPumpElectricity :
    			computeBungalowDemandW(params).noSetback.heatPumpElectricity);
	    }

    /**Compute the original HG 4-room 'bungalow' raw heat and heat-pump electricity demand with and without setback (W).
     * The calculation uses constants from HGTRVHPMModel as far as possible,
     * substituting in parameters and new calculation where needed.
     *
     * @param params  the variable model parameters
     * @return  demand in watts, finite and non-negative
     */
    public static DemandWithoutAndWithSetback computeBungalowDemandW(final ModelParameters params)
	    {
    	Objects.requireNonNull(params);


    	// Do not allow model to be run with potentially implausible parameters.
    	if(params.externalAirTemperatureC >= HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C)
    	    { throw new UnsupportedOperationException("model may not work when outside is warmer than setback rooms"); }


    	// HHLnsb: whole home heat loss with no setback (all rooms same temperature) and given external air temperature (W).
        final double HHLnsb = (HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C - params.externalAirTemperatureC()) *
        		HGTRVHPMModel.HOME_HEAT_LOSS_PER_KELVIN_WpK;
    	// HHLsb: whole home heat loss with B rooms setback and given external air temperature (W).
        final double HHLsb = (HGTRVHPMModel.MEAN_HOME_TEMPERATURE_WITH_SETBACK_C - params.externalAirTemperatureC()) *
        		HGTRVHPMModel.HOME_HEAT_LOSS_PER_KELVIN_WpK;
		// radWnbs: (Flow Temperature, step 1) pre-setback radiator output based on variable external air temperature (W).
        // (Was: RADIATOR_POWER_WITH_HOME_AT_NORMAL_ROOM_TEMPERATURE_W.)
		final double radWnbs = HHLnsb / 4;
//System.out.println(String.format("radWnbs = %f @ %fC", radWnbs,params.externalAirTemperatureC));


        // HEAT LOSS 1
    	// IWAabmd: (Heat Loss 1.2) internal wall area between each A and adjoining B rooms minus appropriate amount of door (m^2).
    	// (INTERNAL_WALL_AREA_FROM_EACH_A_TO_B_ROOMS_MINUS_DOOR_M2)
    	final double IWAabmd =
			HGTRVHPMModel.INTERNAL_WALL_AREA_FROM_EACH_A_TO_B_ROOM_M2
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
        	//HGTRVHPMModel.RADIATOR_POWER_WITH_HOME_AT_NORMAL_ROOM_TEMPERATURE_W + IDWAabHLW;
    		radWnbs + IDWAabHLW;
        // radWBbs: (Heat Loss 2.0) radiator output in each B room when B setback (W).
        // (Was: RADIATOR_POWER_IN_B_ROOMS_WHEN_B_SETBACK_W)
// TODO: why unused: radWBbs
        final double radWBbs =
    		(HHLsb - 2*radWAbs) / 2;
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
//System.out.println(String.format("radAbsMW = %.1f", radAbsMW));

        // Extension to heat loss 2 to allow for varying external temperatures.
        // Compute, for when B rooms are not set back:
        //   * the needed power for each A radiator
        //   * thus the implied temperature
        //   * thus the CoP
        //   * thus the electricity demand
        //
        // Replaces the simple calc;
//     final double radAMW =
//         HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C + HGTRVHPMModel.RADIATOR_MWATDT_AT_NORMAL_ROOM_TEMPERATURE_K;
        //
        // radWAmult: (Heat Loss 2.1) radiator output (possibly < 1) multiplier in each A room when B is not setback.
        // (RADIATOR_POWER_UPLIFT_IN_A_ROOMS_WHEN_B_SETBACK_MULTIPLIER)
        final double radWAnbsmult =
    		radWnbs / HGTRVHPMModel.RADIATOR_POWER_WITH_HOME_AT_NORMAL_ROOM_TEMPERATURE_W;
//System.out.println(String.format("radWAnbsmult = %f", radWAnbsmult));

		// radAnbsdTmult: radiator MW-AT delta-T multiplier in each A room when B NOT setback.
		final double radAnbsdTmult =
			Math.pow(radWAnbsmult, HGTRVHPMModel.RADIATOR_EXP_POWER_TO_DT);
		// radAnbsdT: radiator MW-AT delta-T in each A room when B NOT setback (K).
		final double radAnbsdT =
			HGTRVHPMModel.RADIATOR_MWATDT_AT_NORMAL_ROOM_TEMPERATURE_K * radAnbsdTmult;
		// radAnbsMW: radiator mean water temperature in each A room when B NOT setback (C).
		final double radAnbsMW =
			HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C + radAnbsdT;
//System.out.println(String.format("radAnbsMW = %.1f", radAnbsMW));


        // Normal (no setback) mean water temperature (C).
        final double radAMW = radAnbsMW;
        // Was:
//            HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C + HGTRVHPMModel.RADIATOR_MWATDT_AT_NORMAL_ROOM_TEMPERATURE_K;
//System.out.println(String.format("radAMW = %f", radAMW));


        final double CoPCorrectionK = params.correctCoPForFlowVsMW ? flowMWDelta_K : 0;

		// HPinWnsb: (Heat Pump Efficiency) heat-pump electrical power in when B not setback (W).
        // (HEAT_PUMP_POWER_IN_NO_SETBACK_W)
        // Note that flow and mean temperatures seem to be being mixed here in the HG page.
        final double CoPnsb = computeFlowCoP(radAMW + CoPCorrectionK);
//System.out.println(String.format("CoPnsb = %f", CoPnsb));
        final double HPinWnsb =
    		HHLnsb / CoPnsb;

		// HPinWsb: (Heat Pump Efficiency) heat-pump electrical power in when B is setback (W).
        // (HEAT_PUMP_POWER_IN_B_SETBACK_W)
        // Note that flow and mean temperatures seem to be being mixed here in the HG page.
        final double CoPsb = computeFlowCoP(radAbsMW + CoPCorrectionK);
//System.out.println(String.format("CoPsb = %f", CoPsb));
        final double HPinWsb =
    		HHLsb / CoPsb;

        final HeatAndElectricityDemand noSetback = new HeatAndElectricityDemand(HHLnsb, HPinWnsb);
        final HeatAndElectricityDemand withSetback = new HeatAndElectricityDemand(HHLsb, HPinWsb);

        // Return everything at once.
    	return(new DemandWithoutAndWithSetback(noSetback, withSetback));
	    }


    /**Compute 4-room 'detatched' raw heat and heat-pump electricity demand with and without setback (W).
     * The calculation uses constants from HGTRVHPMModel as far as possible,
     * substituting in parameters and new calculation where needed.
     *
     * @param params  the variable model parameters
     * @param keepAsBungalow  if true, compute as bungalow to cross-check with original calculation
     * @return  demand in watts, finite and non-negative
     */
    public static DemandWithoutAndWithSetback computeDetachedDemandW(final ModelParameters params,
    		final boolean keepAsBungalow)
	    {
    	Objects.requireNonNull(params);


    	// Do not allow model to be run with potentially implausible parameters.
    	if(params.externalAirTemperatureC >= HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C)
    	    { throw new UnsupportedOperationException("model may not work when outside is warmer than setback rooms"); }

        // FIXME: do not allow model to be run *except* in bungalow-like mode yet.
    	if(!keepAsBungalow)
	    	{ throw new RuntimeException("NOT IMPLEMENTED"); }

    	
    	// Roof area: as for bungalow.
    	final double roofAreaM2 = HGTRVHPMModel.HOME_TOTAL_ROOF_AREA_M2;

    	// External wall area: as for bungalow in bungalow mode, else double.
    	final double extWallAreaM2 = (keepAsBungalow ? 1 : 2) *
    			HGTRVHPMModel.HOME_TOTAL_EXTERNAL_WALL_AREA_M2;

        // Wall heat loss per K temperature differential between inside and out.
    	final double homeHeatLossPerK = (roofAreaM2 + extWallAreaM2) *
    			HGTRVHPMModel.HOME_LOSSLESS_FLOOR_EXTERNAL_WALL_AND_ROOF_U_WpM2K;



    	
    	
    	throw new RuntimeException("NOT IMPLEMENTED");

	    }
 	}
