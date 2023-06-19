package org.hd.d.TRVmodel.hg;

/**
 * Recreation of the heat-pump / TRV / energy interactions from Heat Geek's page:
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
 * In this model there are 4 rooms in house in a square in the pattern:
 * <table border="1">
 * <tr><td>A</td><td>B</td></tr>
 * <tr><td>B</td><td>A</td></tr>
 * </table>
 * <p>
 * Where the <em>A</em> rooms are kept at the 'normal' 21&deg;C living temperature,
 * and the <em>B</em> rooms are kept at the same temperature as A or at a lower 18&deg;C setback.
 * These temperatures (especially those in the B rooms) are regulated by TRVs
 * controlling the radiator heat emitters in each room.
 * <p>
 * (Note that this arrangement maximises internal heat transfers between rooms
 * when Bs are at setback temperature; swap any horizontal or vertical pair to minimise instead.)
 * <p>
 * The rooms can be considered to be in a horizontal plane,
 * with the area outside the rooms being at exterior temperature.
 * <p>
 * The external walls are of different construction to
 * the internal walls between the A and B rooms.
 * Heat flow through these walls is modelled.
 * <p>
 * Heat loss through ceilings and floors and all other routes is ignored.
 * <p>
 * This model looks to the total input electric energy input demand
 * to a heat pump to maintain the specified steady state,
 * taking into account implied flow water temperatures
 * and the heat-pump CoP to reach those flow temperatures.
 * <p>
 * <blockquote>
 * To keep the maths simple, each room has a 500 W heat loss at design outside temperature of -3&deg;C or 2 kW in total with a room temperature of 21&deg;C. They are also each fitted with a mean water-to-air temperature DT25 [MW-AT DT25] radiator, meaning that each radiator will output 500 W, when its average surface temperature is 25&deg;C above the room temperature.
 * </blockquote>
 * <p>
 * For step 1 the home heat loss is verified to be 83.3W/K as stated, with testHLWpK().
 * <p>
 * For step 2 the B rooms are lowered to the setback temperature (18&deg;C).
 * The mean home temperature is verified to be 19.5&deg;C as stated, with testTMeanWhenSetback().
 * <p>
 * ...
 * <p>
 * (The <q>Let's say those rooms with a setback temperature of 18&deg;C only drop to 19°C.</q>
 * seems spurious: the room is either at 18&deg;C or it is not.)
 * <p>
 * ...
 * <p>
 * (Not clear why only one door is allowed for in each pair of walls:
 * having one door per wall would increase the effect being described here,
 * and is not consistent with the other text.)
 * <p>
 */
public final class HGTRVHPMModel
 	{
	/**Prevent creation of an instance. */
    private HGTRVHPMModel() { }

    /**tExt: external air temperature, ie design temperature on cold winter day, (Celsius). */
    public static final double EXTERNAL_AIR_TEMPERATURE_C = -3;
    /**tInt: 'normal' room temperature (Celsius). */
    public static final double NORMAL_ROOM_TEMPERATURE_C = 21;
    /**HLDT: design temperature (cold winter day) for heat loss calculations (Kelvin). */
    public static final double HOME_HEAT_LOSS_DESIGN_TEMPERATURE_DELTA_K =
		(NORMAL_ROOM_TEMPERATURE_C - EXTERNAL_AIR_TEMPERATURE_C);
    /**HLW: (flow temperature, step 1) heat loss with all rooms at normal internal temperature (W). */
    public static final double HOME_HEAT_LOSS_AT_NORMAL_ROOM_TEMPERATURE_W = 2000;

    /**HLpK: (flow temperature, step 1) heat loss per Kelvin (W/K). */
    public static final double HOME_HEAT_LOSS_PER_KELVIN_WpK =
		HOME_HEAT_LOSS_AT_NORMAL_ROOM_TEMPERATURE_W / HOME_HEAT_LOSS_DESIGN_TEMPERATURE_DELTA_K;

    /**tIntSetback: setback/unused room temperature (Celsius). */
    public static final double SETBACK_ROOM_TEMPERATURE_C = 18;
    /**tMeanWhenSetback: (flow temperature, step 2) mean home temperature when B rooms setback (Celcius). */
    public static final double MEAN_HOME_TEMPERATURE_WITH_SETBACK_C =
		(NORMAL_ROOM_TEMPERATURE_C + SETBACK_ROOM_TEMPERATURE_C) / 2;
    /**HLsbW: (flow temperature, step 3) heat loss with B rooms setback (W). */
    public static final double HOME_HEAT_LOSS_B_SETBACK_W = HOME_HEAT_LOSS_PER_KELVIN_WpK *
    		(MEAN_HOME_TEMPERATURE_WITH_SETBACK_C - EXTERNAL_AIR_TEMPERATURE_C);
    /**HLfall: (flow temperature, step 3) reduction in home heat loss with B set back. */
    public static final double HOME_HEAT_LOSS_FALL_B_SETBACK =
		(HOME_HEAT_LOSS_AT_NORMAL_ROOM_TEMPERATURE_W - HOME_HEAT_LOSS_B_SETBACK_W) /
			HOME_HEAT_LOSS_AT_NORMAL_ROOM_TEMPERATURE_W;

    /**IWA: (heat loss 1.0) internal wall area of each internal intra-room wall (m^2). */
    public static final double INTERNAL_WALL_AREA_PER_WALL_M2 = 2.3 * 4;
    /**IDA: (heat loss 1.0) internal door area per door - one in each internal wall (m^2).
     * Note: resultr
     */
    public static final double INTERNAL_DOOR_AREA_PER_WALL_M2 = 2;
    /**IWU: (heat loss 1.0) internal wall U value (W/m^2K). */
    public static final double INTERNAL_WALL_U_WpM2K = 2;
    /**IWU: (heat loss 1.0) internal door U value (W/m^2K). */
    public static final double INTERNAL_DOOR_U_WpM2K = 8;
    /**IWAab: (heat loss 1.1) internal wall area between each A and adjoining B rooms (m^2). */
    public static final double INTERNAL_WALL_AREA_FROM_EACH_A_TO_B_ROOMS_M2 = 2 * INTERNAL_WALL_AREA_PER_WALL_M2;
    /**IWAabmd: (heat loss 1.2) internal wall area between each A and adjoining B rooms minus one door (m^2).
     * Note: inconsistent with text that says one door per internal wall rather than per pair.
     */
    public static final double INTERNAL_WALL_AREA_FROM_EACH_A_TO_B_ROOMS_MINUS_DOOR_M2 =
		INTERNAL_WALL_AREA_FROM_EACH_A_TO_B_ROOMS_M2 - INTERNAL_DOOR_AREA_PER_WALL_M2;
    /**IWAabHL: (heat load 1.3) internal wall (minus door) heat loss per Kelvin (W/K). */
    public static final double INTERNAL_WALL_MINUS_DOOR_HEAT_LOSS_PER_KELVIN_WpK =
		INTERNAL_WALL_AREA_FROM_EACH_A_TO_B_ROOMS_MINUS_DOOR_M2 * INTERNAL_WALL_U_WpM2K;
    /**IWAabHLW: (heat load 1.4) internal wall (minus door) heat loss (WK). */
    public static final double INTERNAL_WALL_MINUS_DOOR_HEAT_LOSS_W =
		INTERNAL_WALL_MINUS_DOOR_HEAT_LOSS_PER_KELVIN_WpK *
    		(NORMAL_ROOM_TEMPERATURE_C - SETBACK_ROOM_TEMPERATURE_C);
    /**IDAabHL: (heat load 1.5) internal door heat loss per door per Kelvin (W/K). */
    public static final double INTERNAL_DOOR_HEAT_LOSS_PER_KELVIN_WpK =
		INTERNAL_DOOR_AREA_PER_WALL_M2 * INTERNAL_DOOR_U_WpM2K;
    /**IDAabHLW: (heat load 1.6) internal door heat loss per door (W). */
    public static final double INTERNAL_DOOR_HEAT_LOSS_W =
		INTERNAL_DOOR_HEAT_LOSS_PER_KELVIN_WpK *
			(NORMAL_ROOM_TEMPERATURE_C - SETBACK_ROOM_TEMPERATURE_C);
    /**IDWAabHLW: (heat load 1.6) internal wall and door heat loss per A room (W). */
    public static final double INTERNAL_WALL_AND_DOOR_HEAT_LOSS_PER_A_ROOM_W =
    		INTERNAL_WALL_MINUS_DOOR_HEAT_LOSS_W + INTERNAL_DOOR_HEAT_LOSS_W;


 	}
