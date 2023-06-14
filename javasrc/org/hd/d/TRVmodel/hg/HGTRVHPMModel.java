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
 */
public final class HGTRVHPMModel
 	{
	/**Prevent creation of an instance. */
    private HGTRVHPMModel() { }

    /**External air temperature, ie design temperature on cold winter day, (degrees C). */
    public static final double EXTERNAL_AIR_TEMPERATURE_C = -3;
    /**'Normal' room temperature (degrees C). */
    public static final double NORMAL_ROOM_TEMPERATURE_C = 21;
    /**Setback/unused room temperature (degrees C). */
    public static final double SETBACK_ROOM_TEMPERATURE_C = 18;
 	}
