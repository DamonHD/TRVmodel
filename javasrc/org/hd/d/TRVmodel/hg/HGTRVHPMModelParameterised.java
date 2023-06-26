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
    		boolean roomsAlternatingABAB
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
    			true
    			);
	    	}

        /**Default doors per internal wall: matches the 0.5 in calcs on the original page. */
    	public static final double DEFAULT_DOORS_PER_INTERNAL_WALL = 0.5;
    	/**Default room arrangement ABAB (vs AABB) alternating as original calcs. */
    	public static final boolean DEFAULT_ARRANGEMENT_ABAB = true;
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
    	if(params.doorsPerInternalWall != ModelParameters.DEFAULT_DOORS_PER_INTERNAL_WALL) { throw new UnsupportedOperationException("NOT IMPLEMENTED YET"); }
    	if(params.roomsAlternatingABAB != ModelParameters.DEFAULT_ARRANGEMENT_ABAB) { throw new UnsupportedOperationException("NOT IMPLEMENTED YET"); }


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
        final double IDWAabHLW =
    		IWAabHLW + IDAabHLW;

        // HEAT LOSS 2
        // radWAbs: (Heat Loss 2.0) radiator output in each A room when B setback (W).
        // (RADIATOR_POWER_IN_A_ROOMS_WHEN_B_SETBACK_W)
        final double radWAbs =
    		HGTRVHPMModel.RADIATOR_POWER_WITH_HOME_AT_NORMAL_ROOM_TEMPERATURE_W + IDWAabHLW;
        // radWBbs: (Heat Loss 2.0) radiator output in each B room when B setback (W).
        // (RADIATOR_POWER_IN_B_ROOMS_WHEN_B_SETBACK_W_
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
        		HGTRVHPMModel.RADIATOR_MWATDT_AT_NORMAL_ROOM_TEMPERATURE_W * radAbsdTmult;
        // radAbsMW: (Heat Loss 2.5) radiator mean water temperature in each A room when B setback (C).
        // (RADIATOR_MW_IN_A_ROOMS_WHEN_B_SETBACK_C)
        final double radAbsMW =
    		HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C + radAbsdT;




//        /**CoPA2W46p0: (Heat Pump Efficiency) suggested HP CoP at 46.0C flow temperature. */
//        public static final double COP_AT_46p0C = 2.6;
//        /**CoPA2W51p5: (Heat Pump Efficiency) suggested HP CoP at 51.5C flow temperature. */
//        public static final double COP_AT_51p5C = 2.3;
//        /**CoPDropPerK: (Heat Pump Efficiency) suggested approximate fall in CoP per K rise in flow temperature. */
//        public static final double COP_DROP_pK = 0.025;
//        /**HPinWnsb: (Heat Pump Efficiency) heat-pump electrical power in when B not setback (W).
//         * Note that flow and mean temperatures seem to be being mixed here.
//         */
//        public static final double HEAT_PUMP_POWER_IN_NO_SETBACK_W =
//    		HOME_HEAT_LOSS_AT_NORMAL_ROOM_TEMPERATURE_W / COP_AT_46p0C;
//        /**HPinWsb: (Heat Pump Efficiency) heat-pump electrical power in when B is setback (W).
//         * Note that flow and mean temperatures seem to be being mixed here.
//         */
//        public static final double HEAT_PUMP_POWER_IN_B_SETBACK_W =
//    		HOME_HEAT_LOSS_B_SETBACK_W / COP_AT_51p5C;






    	// TODO






    	return(withBSetback ? HGTRVHPMModel.HEAT_PUMP_POWER_IN_B_SETBACK_W : HGTRVHPMModel.HEAT_PUMP_POWER_IN_NO_SETBACK_W);
	    }

 	}
