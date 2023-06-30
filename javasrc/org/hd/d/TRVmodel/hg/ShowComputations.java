package org.hd.d.TRVmodel.hg;

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



		// TODO

		}
	}
