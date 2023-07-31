package org.hd.d.TRVmodel.hg;

import java.util.Objects;

import org.hd.d.TRVmodel.data.DDNTemperatureDataCSV;
import org.hd.d.TRVmodel.hg.HGTRVHPMModelParameterised.DemandWithoutAndWithSetback;

/**Drives the parameterised HG model variant(s) with hourly temperature data.
 */
public record HGTRVHPMModelByHour(HGTRVHPMModelParameterised model, DDNTemperatureDataCSV temperatures)
 	{
	public HGTRVHPMModelByHour
		{
		Objects.requireNonNull(model);
		Objects.requireNonNull(temperatures);
		}

	/**Temperature below which space heating is required, CIBSE typical UK threshold. */
	public static final double DEFAULT_BASE_HEATING_TEMPERATURE_C = 15.5;

	/**Results of running a model against a temperature set.
	 * @param  fractionSetbackRaisesDemand  fraction of the hours in which setting back B rooms
	 *     causes electricity demand by the heat pump to rise [0.0,1.0]
	 * @param demand heat and electrical mean demand; not null
	 *
	 */
	public record ScenarioResult(double fractionSetbackRaisesDemand, DemandWithoutAndWithSetback demand)
		{
		public ScenarioResult
			{
			if(!Double.isFinite(fractionSetbackRaisesDemand)) { throw new IllegalArgumentException(); }
			if(fractionSetbackRaisesDemand < 0) { throw new IllegalArgumentException(); }
			if(fractionSetbackRaisesDemand > 1) { throw new IllegalArgumentException(); }
			Objects.requireNonNull(demand);
			}
		}

	/**Run scenario on model and temperature; never null.
	 */
	public static ScenarioResult runScenario()
		{
throw new RuntimeException("NOT IMPLEMENTED");
		}
 	}
