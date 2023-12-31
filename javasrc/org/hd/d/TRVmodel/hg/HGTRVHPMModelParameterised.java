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
 * <p>
 * Note that the calculations are all stateless,
 * so could be memoised/cached for any particular set of input parameters.
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
    	/**Construct instance. */
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

    	/**Allow doors per internal wall to be set, all else defaults.
    	 * @param doorsPerInternalWall  mean number of doors per internal wall (non-negative)
         * @param correctCoPForFlowVsMW  if true, correct for flow vs mean radiator issue
    	 */
    	public ModelParameters(final double doorsPerInternalWall, final boolean correctCoPForFlowVsMW)
    	    { this(doorsPerInternalWall, correctCoPForFlowVsMW, DEFAULT_ARRANGEMENT_ABAB, DEFAULT_EXTERNAL_AIR_TEMPERATURE_C); }

    	/**Allow doors per internal wall to be set, all else defaults.
    	 * @param doorsPerInternalWall  mean number of doors per internal wall (non-negative)
    	 */
    	public ModelParameters(final double doorsPerInternalWall)
    	    { this(doorsPerInternalWall, DEFAULT_CORRECT_COP_FOR_FLOW_TEMPERATURE); }


    	/**Clone but replace external temperature value; all other parameters unchanged.
    	 * @param newExternalTemperature  the temperature to use (C)
    	 * @return adjusted model parameters; not null
    	 */
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
    	/**Construct instance. */
	    public HeatAndElectricityDemand
		    {
    		// Sanity-check parameters.
    		if(!Double.isFinite(heatDemand)) { throw new IllegalArgumentException(); }
    		if(heatDemand < 0) { throw new IllegalArgumentException(); }
    		if(!Double.isFinite(heatPumpElectricity)) { throw new IllegalArgumentException(); }
    		if(heatPumpElectricity < 0) { throw new IllegalArgumentException(); }
		    }
	    }

    /**Home heat and electricity demand with and without setback in B rooms; both non-null.
     * @param noSetback  non-set-back values; non-null
     * @param withSetback  set-back values; non-null
     */
    public record DemandWithoutAndWithSetback(HeatAndElectricityDemand noSetback, HeatAndElectricityDemand withSetback)
	    {
    	/**Construct instance. */
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
     *
     * @param flowC  flow temperature (from heat pump to radiator) in C
     * @return CoP
     */
    public static double computeFlowCoP(final double flowC)
	    {
    	final double tempDeltaK = HGTRVHPMModel.CoPHt - HGTRVHPMModel.CoPLt;
    	final double CoPDelta = HGTRVHPMModel.CoPH - HGTRVHPMModel.CoPL;

    	final double CoP = HGTRVHPMModel.CoPL +
    			((flowC - HGTRVHPMModel.CoPLt) * (CoPDelta / tempDeltaK));

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
     * @return demand in watts, finite and non-negative
     */
    public static double computeBungalowHPElectricityDemandW(final ModelParameters params, final boolean withBSetback)
	    {
    	Objects.requireNonNull(params);
    	return(withBSetback ?
    			computeBungalowDemandW(params).withSetback.heatPumpElectricity :
    			computeBungalowDemandW(params).noSetback.heatPumpElectricity);
	    }

    /**Radiator mean water temperature in each A room when B setback (C) for 'stiff' regulation.
     * @param HHLsb  whole home heat loss with B rooms setback and given external air temperature (W)
     * @param radWnsb  pre-setback radiator output based on variable external air temperature (W)
     * @param IWFAabHLW  internal wall/floor heat loss/transfer per A room (W)
     * @return (radAMWsb) mean water temperature in each A room when B setback (C)
     */
	public static double sbAMW(final double HHLsb, final double radWnsb, final double IWFAabHLW)
		{
		// radWAsb: (Heat Loss 2.0) radiator output in each A room when B setback (W).
        // (RADIATOR_POWER_IN_A_ROOMS_WHEN_B_SETBACK_W)
        final double radWAsb =
        	//HGTRVHPMModel.RADIATOR_POWER_WITH_HOME_AT_NORMAL_ROOM_TEMPERATURE_W + IDWAabHLW;
    		radWnsb + IWFAabHLW;
        // radWBsb: (Heat Loss 2.0) radiator output in each B room when B setback (W).
        // (Was: RADIATOR_POWER_IN_B_ROOMS_WHEN_B_SETBACK_W)
        // radWAmultsb: (Heat Loss 2.1) radiator output increase multiplier in each A room when B setback.
        // (RADIATOR_POWER_UPLIFT_IN_A_ROOMS_WHEN_B_SETBACK_MULTIPLIER)
        final double radWAmultsb =
    		radWAsb / HGTRVHPMModel.RADIATOR_POWER_WITH_HOME_AT_NORMAL_ROOM_TEMPERATURE_W;
        // radAdTmultsb: (Heat Loss 2.3) radiator MW-AT delta-T increase multiplier in each A room when B setback.
        // (RADIATOR_DT_UPLIFT_IN_A_ROOMS_WHEN_B_SETBACK_MULTIPLIER)
        final double radAdTmultsb =
    		Math.pow(radWAmultsb, HGTRVHPMModel.RADIATOR_EXP_POWER_TO_DT);
        // radAdTsb: (Heat Loss 2.4) radiator MW-AT delta-T in each A room when B setback (K).
        // (RADIATOR_DT_IN_A_ROOMS_WHEN_B_SETBACK_K)
        final double radAdTsb =
    		HGTRVHPMModel.RADIATOR_MWATDT_AT_NORMAL_ROOM_TEMPERATURE_K * radAdTmultsb;
        // radAMWsb: (Heat Loss 2.5) radiator mean water temperature in each A room when B setback (C).
        // (RADIATOR_MW_IN_A_ROOMS_WHEN_B_SETBACK_C)
        final double radAMWsb =
    		HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C + radAdTsb;
//System.out.println(String.format("radAMWsb = %.1f", radAMWsb));
		return(radAMWsb);
		}

    /**Internal wall heat loss/transfer per A room (HEAT LOSS 1) with A at 'normal' temperature (W).     *
     * @param params  the model parameters; never null
     * @return heat loss power per A rooms (W)
     */
	private static double iwHeatLossPerA(final ModelParameters params)
		{
		return(iwHeatLossPerA(params, HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C));
		}

    /**Internal wall heat loss/transfer per A room (HEAT LOSS 1) (W).
     * This allows an A room temperature other than the initial 'normal' 21C,
     * though it must still be above the B room setback temperature.
     *
     * @param params  the model parameters; never null
     * @param tempA  the A room temperature in C; no lower than B (and finite)
     * @return heat loss power per A rooms (W)
     */
	private static double iwHeatLossPerA(final ModelParameters params, final double tempA)
	    {
		Objects.requireNonNull(params);
		if(!(tempA >= HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C)) { throw new IllegalArgumentException("A must be warmer than B"); }

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
    		    (tempA - HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C);
        // IDWAabHLW: (Heat Loss 1.7) internal wall and door heat loss per A room (W).
        // (INTERNAL_WALL_AND_DOOR_HEAT_LOSS_PER_A_ROOM_W)
        // In the original ABAB arrangement there are two walls from each A room into B rooms.
        // In the alternate AABB arrangement there is one wall from each A room into a B room.
        // Thus AABB has half the internal heat loss of ABAB.
        final double IDWAabHLW = ((params.roomsAlternatingABAB()) ? 1 : 0.5) *
			(IWAabHLW + (2 * params.doorsPerInternalWall() * IDAabHLW));

// DHD20231127: bug present in V0.9.4 ie missing brackets around wall and door terms // ((params.roomsAlternatingABAB()) ? 1 : 0.5) * /*(*/ IWAabHLW + (2 * params.doorsPerInternalWall() * IDAabHLW) /*)*/;

		return(IDWAabHLW);
	    }

    /**Internal floor/ceiling heat loss/transfer per A room (W) for 2-storey detached with stiff regulation.
     * Only applies when a B room is directly above or below.
     * <p>
     * This is only for a 2-storey detached, so in an ABAB arrangement (with BABA below)
     * each A room will lose each <em>either</em> to a B room below or above (not both).
     * Heat loss in each direction (up or down) is treated as the same.
     * <p>
     * Assumes that A room is at 'normal' temperature and B room is at setback temperature.
     *
     * @param params  model parameters; non-null
     * @return heat loss power per A rooms (W)
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
     * @return heat loss power per A rooms (W)
     */
	private static double ifHeatLossPerA2Storey(final ModelParameters params, final double tempA)
		{
		if(!(tempA >= HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C)) { throw new IllegalArgumentException("A must be warmer than B"); }

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
	 * This works for stiff and soft regulation cases therefore.
	 *
	 * @param radWnsb  pre-setback radiator output based on variable external air temperature (W)
	 * @return (radAnsbMW) radiator mean water temperature in each A room when B is NOT set back (C)
	 */
	public static double nsbAMW(final double radWnsb)
		{
		// Extension to heat loss 2 to allow for varying external temperatures.
        // Compute, for when B rooms are not set back:
        //   * the needed power for each A radiator
        //   * thus the implied radiator mean water temperature
		//
		// This then allows computing:
        //   * the CoP
        //   * the heat-pump electricity demand
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
//System.out.println(String.format("radAnsbMW = %.1f", radAnsbMW));
		return(radAnsbMW);
		}

    /**Compute the original HG 4-room 'bungalow' raw heat and heat-pump electricity demand with and without B-room setback (W) with 'stiff' regulation.
     * The calculation uses constants from HGTRVHPMModel as far as possible,
     * substituting in parameters and new calculation where needed.
     *
     * @param params  the variable model parameters
     * @return demand in watts, finite and non-negative
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
        // radAMWsb: (Heat Loss 2.5) radiator mean water temperature in each A room when B setback (C).
        final double radAMWsb = sbAMW(HHLsb, radWnsb, IDWAabHLW);
		// Extension to heat loss 2 to allow for varying external temperatures.
        final double radAMWnsb = nsbAMW(radWnsb);

        // Compute correction for across-radiator delta-T if needed.
        final double CoPCorrectionK = params.correctCoPForFlowVsMW() ? flowMWDelta_K : 0;

		// HPinWnsb: (Heat Pump Efficiency) heat-pump electrical power in when B not setback (W).
        // (HEAT_PUMP_POWER_IN_NO_SETBACK_W)
        // Note that flow and mean temperatures seem to be being mixed here in the HG page.
        final double CoPnsb = computeFlowCoP(radAMWnsb + CoPCorrectionK);
//System.out.println(String.format("CoPnsb = %f", CoPnsb));
        final double HPinWnsb =
    		HHLnsb / CoPnsb;

		// HPinWsb: (Heat Pump Efficiency) heat-pump electrical power in when B is setback (W).
        // (HEAT_PUMP_POWER_IN_B_SETBACK_W)
        // Note that flow and mean temperatures seem to be being mixed here in the HG page.
        final double CoPsb = computeFlowCoP(radAMWsb + CoPCorrectionK);
//System.out.println(String.format("CoPsb = %f", CoPsb));
        final double HPinWsb =
    		HHLsb / CoPsb;


        final HeatAndElectricityDemand noSetback = new HeatAndElectricityDemand(HHLnsb, HPinWnsb);
        final HeatAndElectricityDemand withSetback = new HeatAndElectricityDemand(HHLsb, HPinWsb);

        // Return everything at once.
    	return(new DemandWithoutAndWithSetback(noSetback, withSetback));
	    }

    /**Compute 8-room detached 2-storey house raw heat and heat-pump electricity demand with and without B-room setback with stiff regulation (W).
     * The calculation uses constants from HGTRVHPMModel as far as possible,
     * substituting in parameters and new calculation where needed.
     *
     * @param params  the variable model parameters
     * @return demand in watts, finite and non-negative
     */
    public static DemandWithoutAndWithSetback computeDetachedDemandW(final ModelParameters params)
    	{ return(computeDetachedDemandW(params, false)); }

    /**Compute 8-room detached 2-storey house raw heat and heat-pump electricity demand with and without B-room setback with stiff regulation (W).
     * The calculation uses constants from HGTRVHPMModel as far as possible,
     * substituting in parameters and new calculation where needed.
     *
     * @param params  the variable model parameters
     * @param asBungalow  if true, compute as 4-room bungalow to cross-check with original calculation
     * @return demand in watts, finite and non-negative
     */
    public static DemandWithoutAndWithSetback computeDetachedDemandW(final ModelParameters params,
    		final boolean asBungalow)
	    {
    	Objects.requireNonNull(params);

    	// Do not allow model to be run with potentially implausible parameters.
    	if(params.externalAirTemperatureC >= HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C)
    	    { throw new UnsupportedOperationException("model may not work when outside is warmer than setback rooms"); }


    	// Roof area: as for bungalow.
    	final double roofAreaM2 = HGTRVHPMModelExtensions.HOME_TOTAL_ROOF_AREA_M2;

    	// Number of rooms.
    	final int numRooms = asBungalow ? 4 : 8;

    	// External wall area: as for bungalow in bungalow mode, else double.
    	final double extWallAreaM2 = (asBungalow ? 1 : 2) *
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
//System.out.println(String.format("DradWnsb = %f", DradWnsb));

		// HEAT LOSS 1
		// Internal wall heat loss/transfer per A room (W).
    	final double DIWAabHLW = iwHeatLossPerA(params);
		// Internal floor/ceiling heat loss/transfer per A room (W).
    	// None if a bungalow or if AABB arrangement on both floors,
    	// ie no A and B share a ceiling/floor.
    	final double DIFAabHLW =
			(asBungalow || !params.roomsAlternatingABAB) ? 0 :
				ifHeatLossPerA2Storey(params);
        // All internal heat losses per A room (W).
    	final double DIFWAabHLW = DIWAabHLW + DIFAabHLW;


        // HEAT LOSS 2
        // DradAMWsb: (Heat Loss 2.5) radiator mean water temperature in each A room when B setback (C).
        final double DradAMWsb = sbAMW(DHHLsb, DradWnsb, DIFWAabHLW);
		// Extension to heat loss 2 to allow for varying external temperatures.
		// MW temperature for all room radiators with no setbacks.
        final double DradAMWnsb = nsbAMW(DradWnsb);

        // Compute correction for across-radiator delta-T if needed.
        final double CoPCorrectionK = params.correctCoPForFlowVsMW() ? flowMWDelta_K : 0;

		// HPinWnsb: (Heat Pump Efficiency) heat-pump electrical power in when B not setback (W).
        // (HEAT_PUMP_POWER_IN_NO_SETBACK_W)
        // Note that flow and mean temperatures seem to be being mixed here in the HG page.
        final double DCoPnsb = computeFlowCoP(DradAMWnsb + CoPCorrectionK);
//System.out.println(String.format("CoPnsb = %f", CoPnsb));
        final double DHPinWnsb =
    		DHHLnsb / DCoPnsb;

		// HPinWsb: (Heat Pump Efficiency) heat-pump electrical power in when B is setback (W).
        // (HEAT_PUMP_POWER_IN_B_SETBACK_W)
        // Note that flow and mean temperatures seem to be being mixed here in the HG page.
        final double DCoPsb = computeFlowCoP(DradAMWsb + CoPCorrectionK);
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
     * <li>Flow temperature is pinned during setback to the temperature
     *     that maintained A and B rooms at 'normal' temperature without setback,
     *     ie as if the flow temperature is entirely driven by external temperature
     *     and thus weather compensation.</li>
     * </ul>
     * <p>
     * Note: this does not allow for the temperature delta across the radiator rising,
     * and thus the mean water (MW) temperature dropping further from the flow temperature,
     * as more heat is drawn by an A room below 'normal' temperature.
     * <p>
     * This shows the change in heat and electrical demand from a house with no rooms set back
     * to when B rooms are set back.
     *
     * @param params  the variable model parameters
     * @param bungalow  if true, compute as 4-room bungalow, else as 2-storey 8-room detached
     * @param equilibriumTemperature  if not null and not zero length,
     *     used to return the A-room equilibrium temperature
     * @return demand in watts, finite and non-negative
     */
    public static DemandWithoutAndWithSetback computeSoftATempDemandW(final ModelParameters params,
    		final boolean bungalow,
    		final double[] equilibriumTemperature)
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
//System.out.println(String.format("DHHLnsb = %.1f", DHHLnsb));

        // DradWnsb: pre-setback radiator output based on variable external air temperature (W).
		final double DradWnsb = DHHLnsb / numRooms;
//System.out.println(String.format("DradWnsb = %f", DradWnsb));

		// Extension to heat loss 2 to allow for varying external temperatures.
		// MW temperature for all room radiators with no setbacks.
        final double DradAMWnsb = nsbAMW(DradWnsb);
//System.out.println(String.format("DradAnsbMW = %.1f", DradAnsbMW));
        // Delta between radiator mean water (MW) and A room air with no setbacks.
		final double DradAdTnsb = DradAMWnsb - HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C;
//System.out.println(String.format("DradAdTsb = %.1fK", DradAdTsb));

        // Compute correction for across-radiator delta-T if needed.
        final double CoPCorrectionK = params.correctCoPForFlowVsMW() ? flowMWDelta_K : 0;

		// HPinWnsb: (Heat Pump Efficiency) heat-pump electrical power in when B not setback (W).
        // (HEAT_PUMP_POWER_IN_NO_SETBACK_W)
        // Note that flow and mean temperatures seem to be being mixed here in the HG page.
        final double DCoPnsb = computeFlowCoP(DradAMWnsb + CoPCorrectionK);
        final double VCoPsb = DCoPnsb;
//System.out.println(String.format("DCoPnsb = VCoPsb = %f", DCoPnsb));
        final double DHPinWnsb =
    		DHHLnsb / DCoPnsb;
//System.out.println(String.format("DHPinWnsb = %f", DHPinWnsb));

    	// Compute losses to outside for all B rooms when B setback.
    	final double VBHLsb = (HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C - params.externalAirTemperatureC()) *
        		(homeHeatLossPerK / 2);
//System.out.println(String.format("VBHLsb = %.1fW (%.1fW per B room))", VBHLsb, VBHLsb / (numRooms / 2)));


        // A-room temperature step in K.
        final double tempStepK = 0.01;

        // Try all A room temperatures from setback up to and just above 'normal'
        // to find the minimum A room temperature where heat gains equal or exceed losses
        // and the whole house heat loss at that point.
        double VequilibriumTempA = 0;
        double VequilibriumHHLsb = 0;
        for(double tempA = HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C;
        		tempA <= HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C + tempStepK;
        		tempA += tempStepK)
	        {
//System.out.println(String.format("tempA = %.1fC", tempA));

        	// Compute losses to outside for all A rooms when B setback.
        	final double VAHLsb = (tempA - params.externalAirTemperatureC()) *
            		(homeHeatLossPerK / 2);
//System.out.println(String.format("  VAHLsb = %.1fW", VAHLsb));
        	final double VHHLsb = VAHLsb+VBHLsb;
//System.out.println(String.format("  VHHLsb = %.1fW", VHHLsb));

			// Losses to outside for each A room.
			final double VAHLo = VAHLsb / (numRooms / 2);
//System.out.println(String.format("  VAHLo = %.1fW", VAHLo));

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
//System.out.println(String.format("  VIFWAabHLW = %.1fW", VIFWAabHLW));

			// Total heat losses from each A room.
            final double VAHLW = VIFWAabHLW + VAHLo;
//System.out.println(String.format("  VAHLW = %.1fW", VAHLW));

            // Input power from radiator to each A room given:
            //   * the A room temperature
            //   * same (weather-compensated) MW/flow temperature as without setbacks
            //
            // Delta between radiator mean water (MW) and A room air.
			final double VradAdTsb = DradAMWnsb - tempA;
			assert((VradAdTsb > DradAdTnsb) || (tempA >= HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C)) :
				"When room is cooler than 'normal', delta must be higher.";
//System.out.println(String.format("  VradAdTsb = %.1fK", VradAdT));
            // Ratio to original non-setback delta.
            final double VradAdTmultsb = VradAdTsb / DradAdTnsb;
//System.out.println(String.format("  VradAdTmultsb = %.2f", VradAdTmultsb));
            final double dtToWexp = 1 / HGTRVHPMModel.RADIATOR_EXP_POWER_TO_DT;
			// Power output from rad in A room.
            final double VradWAmultsb =
        		VradAdTmultsb * Math.pow(VradAdTmultsb, dtToWexp);
//System.out.println(String.format("  VradWAmultsb = %.2f", VradWAmult));
			// Power output from rad in A room (with B set back).
			final double VradWAsb =
				VradWAmultsb * DradWnsb;
//System.out.println(String.format("  VradWAsb = %.1fW", VradWAsb));
            // When room is cooler than 'normal', radiator output must be higher.
			assert((VradWAsb > DradWnsb) || (tempA >= HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C)) :
				"When room is cooler than 'normal', radiator output must be higher.";

            // Compute the error in each A-room heat gains and losses (+ve means excess heat in).
            final double VAHLerrW =
        		VradWAsb - VAHLW;
//System.out.println(String.format("  VAHLerrW = %.1fW", VAHLerrW));

            // Stop when A-room losses exceed gains
			// thus returning (conservative, near) equilibrium values
            // from the previous step.
            //
            // As the A room trial temperature rises with each loop iteration,
            // losses will rise.
			if(VAHLerrW < 0)
				{ break; }

			// Record temperature and home heat loss
			// when room A temperature below equilibrium point.
			VequilibriumTempA = tempA;
			VequilibriumHHLsb = VHHLsb;
	        }

        if(VequilibriumHHLsb <= 0)
            { throw new RuntimeException("Failed to find solution"); }

        // Return equilibrium temperature if possible.
        if((null != equilibriumTemperature) && (0 != equilibriumTemperature.length))
        	{ equilibriumTemperature[0] = VequilibriumTempA; }

//System.out.println(String.format("VequilibriumTempA = %.1fC @ external %.1fC", VequilibriumTempA, params.externalAirTemperatureC()));

        // Compute electrical energy in given non-setback flow temperature CoP.
        final double VHPinWsb =
    		VequilibriumHHLsb / VCoPsb;
//System.out.println(String.format("VHPinWsb = %.1fW", VHPinWsb));
//System.out.println(String.format("DHPinWnsb = %.1fW", DHPinWnsb));

        final HeatAndElectricityDemand noSetback = new HeatAndElectricityDemand(DHHLnsb, DHPinWnsb);
        final HeatAndElectricityDemand withSetback = new HeatAndElectricityDemand(VequilibriumHHLsb, VHPinWsb);

        // Return everything at once.
    	return(new DemandWithoutAndWithSetback(noSetback, withSetback));
	    }

 	}
