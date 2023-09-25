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
     *     else arrange so as to minimise heat loss
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

    /**Radiator mean water temperature in each A room when B setback (C).
     * @param HHLsb  whole home heat loss with B rooms setback and given external air temperature (W)
     * @param radWnsb  pre-setback radiator output based on variable external air temperature (W)
     * @param IWFAabHLW  internal wall/floor heat loss/transfer per A room (W)
     * @return (radAsbMW) mean water temperature in each A room when B setback (C)
     */
	public static double sbAMW(final double HHLsb, final double radWnsb, final double IWAabHLW)
		{
		// radWAsb: (Heat Loss 2.0) radiator output in each A room when B setback (W).
        // (RADIATOR_POWER_IN_A_ROOMS_WHEN_B_SETBACK_W)
        final double radWAsb =
        	//HGTRVHPMModel.RADIATOR_POWER_WITH_HOME_AT_NORMAL_ROOM_TEMPERATURE_W + IDWAabHLW;
    		radWnsb + IWAabHLW;
        // radWBbs: (Heat Loss 2.0) radiator output in each B room when B setback (W).
        // (Was: RADIATOR_POWER_IN_B_ROOMS_WHEN_B_SETBACK_W)
// TODO: why unused: radWBsb
//        final double radWBsb =
//    		(HHLsb - 2*radWAsb) / 2;
        // radWAmult: (Heat Loss 2.1) radiator output increase multiplier in each A room when B setback.
        // (RADIATOR_POWER_UPLIFT_IN_A_ROOMS_WHEN_B_SETBACK_MULTIPLIER)
        final double radWAmult =
    		radWAsb / HGTRVHPMModel.RADIATOR_POWER_WITH_HOME_AT_NORMAL_ROOM_TEMPERATURE_W;
        // radAsbdTmult: (Heat Loss 2.3) radiator MW-AT delta-T increase multiplier in each A room when B setback.
        // (RADIATOR_DT_UPLIFT_IN_A_ROOMS_WHEN_B_SETBACK_MULTIPLIER)
        final double radAsbdTmult =
    		Math.pow(radWAmult, HGTRVHPMModel.RADIATOR_EXP_POWER_TO_DT);
        // radAsbdT: (Heat Loss 2.4) radiator MW-AT delta-T in each A room when B setback (K).
        // (RADIATOR_DT_IN_A_ROOMS_WHEN_B_SETBACK_K)
        final double radAsbdT =
    		HGTRVHPMModel.RADIATOR_MWATDT_AT_NORMAL_ROOM_TEMPERATURE_K * radAsbdTmult;
        // radAsbMW: (Heat Loss 2.5) radiator mean water temperature in each A room when B setback (C).
        // (RADIATOR_MW_IN_A_ROOMS_WHEN_B_SETBACK_C)
        final double radAsbMW =
    		HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C + radAsbdT;
//System.out.println(String.format("radAbsMW = %.1f", radAbsMW));
		return(radAsbMW);
		}

    /**Internal wall heat loss/transfer per A room (HEAT LOSS 1) with A at 'normal' temperature (W).
     * This allows an A room temperature other than the initial 'normal' 21C,
     * though it must still be above the B room setback temperature.
     *
     * @param params  the model parameters; never null
     * @param tempA  the A room temperature in C; no lower than B (and finite)
     */
	private static double iwHeatLossPerA(final ModelParameters params)
		{
		return(iwHeatLossPerA(params, HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C));
		}

    /**Internal wall heat loss/transfer per A room (HEAT LOSS 1) (W). */
	private static double iwHeatLossPerA(final ModelParameters params, final double tempA)
	    {
		Objects.requireNonNull(params);
		if(!(tempA >= HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C)) { throw new IllegalArgumentException(); }

		// IWAabmd: (Heat Loss 1.2) internal wall area between each A and adjoining B rooms minus appropriate amount of door (m^2).
    	// (INTERNAL_WALL_AREA_FROM_EACH_A_TO_B_ROOMS_MINUS_DOOR_M2)
    	final double IWAabmd =
			HGTRVHPMModel.INTERNAL_WALL_AREA_FROM_EACH_A_TO_B_ROOM_M2
			- (2 * params.doorsPerInternalWall() * HGTRVHPMModel.INTERNAL_DOOR_AREA_PER_DOOR_M2);
    	// IWAabHL: (Heat Loss 1.3) internal wall (minus door) heat loss per Kelvin (W/K).
    	// (INTERNAL_WALL_MINUS_DOOR_HEAT_LOSS_PER_KELVIN_WpK)
        final double IWAabHL =
    		IWAabmd * HGTRVHPMModel.INTERNAL_WALL_U_WpM2K;
        // IWAabHLW: (Heat Loss 1.4) internal wall (minus door) heat loss (W).
        // (INTERNAL_WALL_MINUS_DOOR_HEAT_LOSS_W)
        final double IWAabHLW =
    		IWAabHL *
    			(tempA - HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C);
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

		return(IDWAabHLW);
	    }

    /**Internal floor/ceiling heat loss/transfer per A room (W) for 2-storey detached.
     * Only applies when a B room is directly above or below.
     * <p>
     * This is only for a 2-storey detached, so in an ABAB arrangement (with BABA below)
     * each A room will lose each <em>either</em> to a B room below or above (not both).
     * Heat loss in each direction (up or down) is treated as the same.
     * <p>
     * Assumes that A room is at 'normal' temperature and B room is at setback temperature.
     *
     * @param params  model parameters; non-null
     */
	private static double ifHeatLossPerA2Storey(final ModelParameters params)
		{
		return(ifHeatLossPerA2Storey(params, HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C));
		}

    /**Internal floor/ceiling heat loss/transfer per A room (W) for 2-storey detached.
     * Only applies when a B room is directly above or below.
     * <p>
     * This is only for a 2-storey detached, so in an ABAB arrangement (with BABA below)
     * each A room will lose each <em>either</em> to a B room below or above (not both).
     * Heat loss in each direction (up or down) is treated as the same.
     *
     * @param params  model parameters; non-null
     * @param tempA  temperature of A room, must be no lower than B room setback.
     */
	private static double ifHeatLossPerA2Storey(final ModelParameters params, final double tempA)
		{
		if(!(tempA >= HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C)) { throw new IllegalArgumentException("A must not be colder than B"); }

		if(!params.roomsAlternatingABAB) { return(0); }

    	// IFAabHL: internal floor/ceiling heat loss per Kelvin (W/K).
        final double IFAabHL =
    		HGTRVHPMModelExtensions.PER_A_FLOOR_AREA_M2 * HGTRVHPMModelExtensions.INTERNAL_FLOOR_U_WpM2K;

        // IFAabHLW: internal floor/ceiling heat loss per A room (W).
        final double IFAabHLW =
    		IFAabHL *
    			(tempA - HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C);

		return(IFAabHLW);
		}

	/**Compute radiator mean water temperature in each A room when B is NOT set back (C).
	 * Extension to heat loss 2 to allow for varying external temperatures.
	 * <p>
	 * This is in fact the MW temperature for all room radiators when there are no setbacks.
	 *
	 * @param radWnsb  pre-setback radiator output based on variable external air temperature (W)
	 * @return (radAnsbMW) radiator mean water temperature in each A room when B is NOT set back (C)
	 */
	public static double nsbAMW(final double radWnsb)
		{
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
        // radWAnsbmult: (Heat Loss 2.1) radiator output (possibly < 1) multiplier in each A room when B is not setback.
        // (RADIATOR_POWER_UPLIFT_IN_A_ROOMS_WHEN_B_SETBACK_MULTIPLIER)
        final double radWAnsbmult =
    		radWnsb / HGTRVHPMModel.RADIATOR_POWER_WITH_HOME_AT_NORMAL_ROOM_TEMPERATURE_W;
//System.out.println(String.format("radWAnsbmult = %f", radWAnsbmult));

		// radAnsbdTmult: radiator MW-AT delta-T multiplier in each A room when B NOT setback.
		final double radAnsbdTmult =
			Math.pow(radWAnsbmult, HGTRVHPMModel.RADIATOR_EXP_POWER_TO_DT);
		// radAnsbdT: radiator MW-AT delta-T in each A room when B NOT setback (K).
		final double radAnsbdT =
			HGTRVHPMModel.RADIATOR_MWATDT_AT_NORMAL_ROOM_TEMPERATURE_K * radAnsbdTmult;
		// radAnsbMW: radiator mean water temperature in each A room when B NOT setback (C).
		final double radAnsbMW =
			HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C + radAnsbdT;
//System.out.println(String.format("radAnbsMW = %.1f", radAnbsMW));
		return(radAnsbMW);
		}

    /**Compute the original HG 4-room 'bungalow' raw heat and heat-pump electricity demand with and without B-room setback (W).
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
		final double radWnsb = HHLnsb / 4;


        // HEAT LOSS 1
		// Internal wall heat loss/transfer per A room (W).
    	final double IDWAabHLW = iwHeatLossPerA(params);


        // HEAT LOSS 2
        // radAsbMW: (Heat Loss 2.5) radiator mean water temperature in each A room when B setback (C).
        final double radAsbMW = sbAMW(HHLsb, radWnsb, IDWAabHLW);
		// Extension to heat loss 2 to allow for varying external temperatures.
        final double radAnsbMW = nsbAMW(radWnsb);


        final double CoPCorrectionK = params.correctCoPForFlowVsMW ? flowMWDelta_K : 0;

		// HPinWnsb: (Heat Pump Efficiency) heat-pump electrical power in when B not setback (W).
        // (HEAT_PUMP_POWER_IN_NO_SETBACK_W)
        // Note that flow and mean temperatures seem to be being mixed here in the HG page.
        final double CoPnsb = computeFlowCoP(radAnsbMW + CoPCorrectionK);
//System.out.println(String.format("CoPnsb = %f", CoPnsb));
        final double HPinWnsb =
    		HHLnsb / CoPnsb;

		// HPinWsb: (Heat Pump Efficiency) heat-pump electrical power in when B is setback (W).
        // (HEAT_PUMP_POWER_IN_B_SETBACK_W)
        // Note that flow and mean temperatures seem to be being mixed here in the HG page.
        final double CoPsb = computeFlowCoP(radAsbMW + CoPCorrectionK);
//System.out.println(String.format("CoPsb = %f", CoPsb));
        final double HPinWsb =
    		HHLsb / CoPsb;


        final HeatAndElectricityDemand noSetback = new HeatAndElectricityDemand(HHLnsb, HPinWnsb);
        final HeatAndElectricityDemand withSetback = new HeatAndElectricityDemand(HHLsb, HPinWsb);

        // Return everything at once.
    	return(new DemandWithoutAndWithSetback(noSetback, withSetback));
	    }

    /**Compute 8-room 'detached' raw heat and heat-pump electricity demand with and without B-room setback (W).
     * The calculation uses constants from HGTRVHPMModel as far as possible,
     * substituting in parameters and new calculation where needed.
     *
     * @param params  the variable model parameters
     * @return  demand in watts, finite and non-negative
     */
    public static DemandWithoutAndWithSetback computeDetachedDemandW(final ModelParameters params)
    	{ return(computeDetachedDemandW(params, false)); }

    /**Compute 8-room 'detached' raw heat and heat-pump electricity demand with and without B-room setback (W).
     * The calculation uses constants from HGTRVHPMModel as far as possible,
     * substituting in parameters and new calculation where needed.
     *
     * @param params  the variable model parameters
     * @param keepAsBungalow  if true, compute as 4-room bungalow to cross-check with original calculation
     * @return  demand in watts, finite and non-negative
     */
    public static DemandWithoutAndWithSetback computeDetachedDemandW(final ModelParameters params,
    		final boolean keepAsBungalow)
	    {
    	Objects.requireNonNull(params);

    	// Do not allow model to be run with potentially implausible parameters.
    	if(params.externalAirTemperatureC >= HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C)
    	    { throw new UnsupportedOperationException("model may not work when outside is warmer than setback rooms"); }


    	// Roof area: as for bungalow.
    	final double roofAreaM2 = HGTRVHPMModelExtensions.HOME_TOTAL_ROOF_AREA_M2;

    	// Number of rooms.
    	final int numRooms = keepAsBungalow ? 4 : 8;

    	// External wall area: as for bungalow in bungalow mode, else double.
    	final double extWallAreaM2 = (keepAsBungalow ? 1 : 2) *
    			HGTRVHPMModelExtensions.HOME_TOTAL_EXTERNAL_WALL_AREA_M2;

        // Wall heat loss per K temperature differential between inside and out.
    	final double homeHeatLossPerK = (roofAreaM2 + extWallAreaM2) *
    			HGTRVHPMModelExtensions.HOME_LOSSLESS_FLOOR_EXTERNAL_WALL_AND_ROOF_U_WpM2K;

    	// DHHLnsb: whole home heat loss with no setback (all rooms same temperature) and given external air temperature (W).
        final double DHHLnsb = (HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C - params.externalAirTemperatureC()) *
        		homeHeatLossPerK;
    	// HHLsb: whole home heat loss with B rooms setback and given external air temperature (W).
        final double DHHLsb = (HGTRVHPMModel.MEAN_HOME_TEMPERATURE_WITH_SETBACK_C - params.externalAirTemperatureC()) *
        		homeHeatLossPerK;
        // DradWnsb: pre-setback radiator output based on variable external air temperature (W).
        // (Was: RADIATOR_POWER_WITH_HOME_AT_NORMAL_ROOM_TEMPERATURE_W.)
		final double DradWnsb = DHHLnsb / numRooms;
//System.out.println(String.format("DradWnbs = %f", DradWnsb));

		// HEAT LOSS 1
		// Internal wall heat loss/transfer per A room (W).
    	final double DIWAabHLW = iwHeatLossPerA(params);
		// Internal floor/ceiling heat loss/transfer per A room (W).
    	// None if a bungalow or if AABB arrangement on both floors,
    	// ie no A and B share a ceiling/floor.
    	final double DIFAabHLW =
			(keepAsBungalow || !params.roomsAlternatingABAB) ? 0 :
				ifHeatLossPerA2Storey(params);
        // All internal heat losses per A room (W).
    	final double DIFWAabHLW = DIWAabHLW + DIFAabHLW;


        // HEAT LOSS 2
        // DradAsbMW: (Heat Loss 2.5) radiator mean water temperature in each A room when B setback (C).
        final double DradAsbMW = sbAMW(DHHLsb, DradWnsb, DIFWAabHLW);
		// Extension to heat loss 2 to allow for varying external temperatures.
		// MW temperature for all room radiators with no setbacks.
        final double DradAnsbMW = nsbAMW(DradWnsb);


        final double CoPCorrectionK = params.correctCoPForFlowVsMW ? flowMWDelta_K : 0;

		// HPinWnsb: (Heat Pump Efficiency) heat-pump electrical power in when B not setback (W).
        // (HEAT_PUMP_POWER_IN_NO_SETBACK_W)
        // Note that flow and mean temperatures seem to be being mixed here in the HG page.
        final double DCoPnsb = computeFlowCoP(DradAnsbMW + CoPCorrectionK);
//System.out.println(String.format("CoPnsb = %f", CoPnsb));
        final double DHPinWnsb =
    		DHHLnsb / DCoPnsb;

		// HPinWsb: (Heat Pump Efficiency) heat-pump electrical power in when B is setback (W).
        // (HEAT_PUMP_POWER_IN_B_SETBACK_W)
        // Note that flow and mean temperatures seem to be being mixed here in the HG page.
        final double DCoPsb = computeFlowCoP(DradAsbMW + CoPCorrectionK);
//System.out.println(String.format("CoPsb = %f", CoPsb));
        final double DHPinWsb =
    		DHHLsb / DCoPsb;


        final HeatAndElectricityDemand noSetback = new HeatAndElectricityDemand(DHHLnsb, DHPinWnsb);
        final HeatAndElectricityDemand withSetback = new HeatAndElectricityDemand(DHHLsb, DHPinWsb);

        // Return everything at once.
    	return(new DemandWithoutAndWithSetback(noSetback, withSetback));
	    }








    /**Compute raw heat and heat-pump electricity demand with and without setback, for 'soft' A temperature regulation (W).
     * This lets the A room temperature droop during B-room setback,
     * and does not raise the flow temperature.
     * <p>
     * This assumes that:
     * <ul>
     * <li>B room temperatures (and A room temperatures) are 'normal' (21C) without setback.</li>
     * <li>B room temperatures are the expected 18C when set back.</li>
     * <li>Flow temperature is fixed during setback at the temperature
     *     that maintained A and B rooms at 'normal' temperature without setback,
     *     ie as if the flow temperature is entirely driven by external temperature
     *     and thus weather compensation.</li>
     * </ul>
     *
     * @param params  the variable model parameters
     * @param bungalow  if true, compute as 4-room bungalow, else as 8-room detached
     * @return  demand in watts, finite and non-negative
     */
    public static DemandWithoutAndWithSetback computeSoftATempDemandW(final ModelParameters params,
    		final boolean bungalow)
	    {
    	Objects.requireNonNull(params);

    	// Do not allow model to be run with potentially implausible parameters.
    	if(params.externalAirTemperatureC >= HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C)
    	    { throw new UnsupportedOperationException("model may not work when outside is warmer than setback rooms"); }


    	// Roof area: as for bungalow.
    	final double roofAreaM2 = HGTRVHPMModelExtensions.HOME_TOTAL_ROOF_AREA_M2;

    	// Number of rooms.
    	final int numRooms = bungalow ? 4 : 8;

    	// External wall area: as for bungalow in bungalow mode, else double.
    	final double extWallAreaM2 = (bungalow ? 1 : 2) *
    			HGTRVHPMModelExtensions.HOME_TOTAL_EXTERNAL_WALL_AREA_M2;

        // Wall heat loss per K temperature differential between inside and out.
    	final double homeHeatLossPerK = (roofAreaM2 + extWallAreaM2) *
    			HGTRVHPMModelExtensions.HOME_LOSSLESS_FLOOR_EXTERNAL_WALL_AND_ROOF_U_WpM2K;

    	// DHHLnsb: whole home heat loss with no setback (all rooms same temperature) and given external air temperature (W).
        final double DHHLnsb = (HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C - params.externalAirTemperatureC()) *
        		homeHeatLossPerK;

        // DradWnsb: pre-setback radiator output based on variable external air temperature (W).
        // (Was: RADIATOR_POWER_WITH_HOME_AT_NORMAL_ROOM_TEMPERATURE_W.)
		final double DradWnsb = DHHLnsb / numRooms;
//System.out.println(String.format("DradWnbs = %f", DradWnsb));

		// Extension to heat loss 2 to allow for varying external temperatures.
		// MW temperature for all room radiators with no setbacks.
        final double DradAnsbMW = nsbAMW(DradWnsb);
System.out.println(String.format("DradAnsbMW = %.1f", DradAnsbMW));

        final double CoPCorrectionK = params.correctCoPForFlowVsMW ? flowMWDelta_K : 0;

		// HPinWnsb: (Heat Pump Efficiency) heat-pump electrical power in when B not setback (W).
        // (HEAT_PUMP_POWER_IN_NO_SETBACK_W)
        // Note that flow and mean temperatures seem to be being mixed here in the HG page.
        final double DCoPnsb = computeFlowCoP(DradAnsbMW + CoPCorrectionK);
//System.out.println(String.format("CoPnsb = %f", CoPnsb));
        final double DHPinWnsb =
    		DHHLnsb / DCoPnsb;


        // A-room temperature step in K.
        final double tempStepK = 0.1;

        // Try all A room temperatures from setback up to and just above 'normal'
        // to find XXX
        for(double tempA = HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C;
        		tempA <= HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C + tempStepK;
        		tempA += tempStepK)
	        {
        	System.out.println(String.format("tempA = %.1fC", tempA));

//        	// Compute mean house temperature and thus whole home heat demand for setback as before.
//        	final double VHmt = (tempA + HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C) / 2;
//        	final double VHHLsb = (VHmt - params.externalAirTemperatureC()) *
//            		homeHeatLossPerK;
//        	System.out.println(String.format("  VHHLsb = %.1fW", VHHLsb));

        	// Compute losses to outside for A and B rooms separately when B setback.
        	final double VAHLsb = (tempA - params.externalAirTemperatureC()) *
            		(homeHeatLossPerK / 2);
        	final double VBHLsb = (HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C - params.externalAirTemperatureC()) *
            		(homeHeatLossPerK / 2);
        	final double VHHLsb = VAHLsb+VBHLsb;
System.out.println(String.format("  VHHLsb = %.1fW", VHHLsb));

			// Losses to outside for each A room.
			final double VAHLo = VAHLsb / (numRooms / 2);

    		// HEAT LOSS 1
    		// Internal wall heat loss/transfer per A room (W).
        	final double VIWAabHLW = iwHeatLossPerA(params, tempA);
    		// Internal floor/ceiling heat loss/transfer per A room (W).
        	// None if a bungalow or if AABB arrangement on both floors,
        	// ie no A and B share a ceiling/floor.
        	final double VIFAabHLW =
    			(bungalow || !params.roomsAlternatingABAB) ? 0 :
    				ifHeatLossPerA2Storey(params, tempA);
            // All internal heat losses per A room (W).
        	final double VIFWAabHLW = VIWAabHLW + VIFAabHLW;
System.out.println(String.format("  VIFWAabHLW = %.1fW", VIFWAabHLW));

			// Total heat losses from each A room.
            final double VAHLW = VIFWAabHLW + VAHLo;
System.out.println(String.format("  VAHLW = %.1fW", VAHLW));


            // Input power from radiator to each A room given:
            //   * A room temperature
            //   * same (weather-compensated) MW/flow temperature as without setbacks
			final double VradAsbdT = DradAnsbMW - tempA;
System.out.println(String.format("  VradAsbdT = %.1fK", VradAsbdT));





//// (RADIATOR_POWER_UPLIFT_IN_A_ROOMS_WHEN_B_SETBACK_MULTIPLIER)
//final double radWAmult =
//	radWAsb / HGTRVHPMModel.RADIATOR_POWER_WITH_HOME_AT_NORMAL_ROOM_TEMPERATURE_W;
//// radAsbdTmult: (Heat Loss 2.3) radiator MW-AT delta-T increase multiplier in each A room when B setback.
//// (RADIATOR_DT_UPLIFT_IN_A_ROOMS_WHEN_B_SETBACK_MULTIPLIER)
//final double radAsbdTmult =
//	Math.pow(radWAmult, HGTRVHPMModel.RADIATOR_EXP_POWER_TO_DT);
//// radAsbdT: (Heat Loss 2.4) radiator MW-AT delta-T in each A room when B setback (K).
//// (RADIATOR_DT_IN_A_ROOMS_WHEN_B_SETBACK_K)
//final double radAsbdT =
//	HGTRVHPMModel.RADIATOR_MWATDT_AT_NORMAL_ROOM_TEMPERATURE_K * radAsbdTmult;




	        }


        // FIXME








    	// HHLsb: whole home heat loss with B rooms setback and given external air temperature (W).
        final double DHHLsb = (HGTRVHPMModel.MEAN_HOME_TEMPERATURE_WITH_SETBACK_C - params.externalAirTemperatureC()) *
        		homeHeatLossPerK;

		// HEAT LOSS 1
		// Internal wall heat loss/transfer per A room (W).
    	final double DIWAabHLW = iwHeatLossPerA(params);
		// Internal floor/ceiling heat loss/transfer per A room (W).
    	// None if a bungalow or if AABB arrangement on both floors,
    	// ie no A and B share a ceiling/floor.
    	final double DIFAabHLW =
			(bungalow || !params.roomsAlternatingABAB) ? 0 :
				ifHeatLossPerA2Storey(params);
        // All internal heat losses per A room (W).
    	final double DIFWAabHLW = DIWAabHLW + DIFAabHLW;


        // HEAT LOSS 2
        // DradAsbMW: (Heat Loss 2.5) radiator mean water temperature in each A room when B setback (C).
        final double DradAsbMW = sbAMW(DHHLsb, DradWnsb, DIFWAabHLW);

		// HPinWsb: (Heat Pump Efficiency) heat-pump electrical power in when B is setback (W).
        // (HEAT_PUMP_POWER_IN_B_SETBACK_W)
        // Note that flow and mean temperatures seem to be being mixed here in the HG page.
        final double DCoPsb = computeFlowCoP(DradAsbMW + CoPCorrectionK);
//System.out.println(String.format("CoPsb = %f", CoPsb));
        final double DHPinWsb =
    		DHHLsb / DCoPsb;


        final HeatAndElectricityDemand noSetback = new HeatAndElectricityDemand(DHHLnsb, DHPinWnsb);
        final HeatAndElectricityDemand withSetback = new HeatAndElectricityDemand(DHHLsb, DHPinWsb);

        // Return everything at once.
    	return(new DemandWithoutAndWithSetback(noSetback, withSetback));
	    }

 	}
