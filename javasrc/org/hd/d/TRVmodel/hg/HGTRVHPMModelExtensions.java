package org.hd.d.TRVmodel.hg;

/**Simple static extensions to the recreation of the heat-pump / TRV / energy interactions from Heat Geek's page:
 * <a href="https://www.heatgeek.com/why-not-to-zone-heat-pumps-or-boilers/">https://www.heatgeek.com/why-not-to-zone-heat-pumps-or-boilers/</a>
 * as of ~2023-06.
 */
public final class HGTRVHPMModelExtensions
 	{
	/**Prevent creation of an instance. */
    private HGTRVHPMModelExtensions() { }

	/**Extras available extrapolating from the HG description. */
	/**AFA: approximate floor and ceiling area per room A). */
	public static final double PER_A_FLOOR_AREA_M2 =
		HGTRVHPMModel.INTERNAL_WALL_LENGTH_M * HGTRVHPMModel.INTERNAL_WALL_LENGTH_M;

    /**TFA: approximate total floor area ignoring wall thickness (m^2). */
    public static final double HOME_TOTAL_FLOOR_AREA_M2 =
		HGTRVHPMModelExtensions.PER_A_FLOOR_AREA_M2 * 4;
    /**TRAL approximate total roof area ignoring wall thickness (m^2). */
    public static final double HOME_TOTAL_ROOF_AREA_M2 =
		HOME_TOTAL_FLOOR_AREA_M2;
    /**TEWL: approximate external wall length (ignoring wall thickness (m). */
    public static final double HOME_TOTAL_EXTERNAL_WALL_LENGTH_M =
		HGTRVHPMModel.INTERNAL_WALL_LENGTH_M * 8;
    /**TEWA: approximate external wall area (ignoring wall thickness, windows, doors (m2). */
    public static final double HOME_TOTAL_EXTERNAL_WALL_AREA_M2 =
		HOME_TOTAL_EXTERNAL_WALL_LENGTH_M * HGTRVHPMModel.INTERNAL_WALL_HEIGHT_M;
    /**EWU: effective total external wall U value if only source of heat loss to outside (W/m^2K).
     * This figure is assuming no heat loss through any other route, eg floor or roof,
     * or ventilation losses, or indeed occupancy and appliance gains.
     */
    public static final double HOME_LOSSLESS_FLOOR_AND_ROOF_EXTERNAL_WALL_U_WpM2K =
		HGTRVHPMModel.HOME_HEAT_LOSS_PER_KELVIN_WpK / HOME_TOTAL_EXTERNAL_WALL_AREA_M2;
    /**EWRU: effective total external wall and roof U value if only sources of heat loss to outside (W/m^2K).
     * This figure is assuming no heat loss through any other route, eg floor,
     * or ventilation losses, or indeed occupancy and appliance gains.
     * <p>
     * This assumes roof and external wall roof U values are the same
     * (usually roof is a little lower in reality).
     * <p>
     * English 1970 regs U values for external walls and roof:
     * <ul>
     * <li>1970: 1.6, 1.5</li>
     * <li>1980: 1.0, 0.68</li>
     * <li>1990: 0.60, 0.40</li>
     * </ul>
     * <p>
     * See: https://www.ecomerchant.co.uk/news/a-brief-history-of-building-regulation-u-values-with-examples/
     * <p>
     * Implied value of 0.61 is ~1980s.
     */
    public static final double HOME_LOSSLESS_FLOOR_EXTERNAL_WALL_AND_ROOF_U_WpM2K =
    	HGTRVHPMModel.HOME_HEAT_LOSS_PER_KELVIN_WpK / (HOME_TOTAL_EXTERNAL_WALL_AREA_M2 + HOME_TOTAL_ROOF_AREA_M2);
    /**IFU: internal floor U value (W/m^2K).
     * Wall U-value used for internal floor U value for simplicity, eg given
     * plasterboard then 8 inch joist space then tongue-and-groove boards ~1.4/1.7.
     * <p>
     * Treated as symmetric for simplicity.
     * <p>
     * See: https://www.diydata.com/information/u_values/u_values.php
     */
    public static final double INTERNAL_FLOOR_U_WpM2K =
		HGTRVHPMModel.INTERNAL_WALL_U_WpM2K;
 	}
