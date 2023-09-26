package org.hd.d.TRVmodel.hg;

import java.util.List;
import java.util.Objects;

import org.hd.d.TRVmodel.data.DDNTemperatureDataCSV;
import org.hd.d.TRVmodel.hg.HGTRVHPMModelParameterised.DemandWithoutAndWithSetback;
import org.hd.d.TRVmodel.hg.HGTRVHPMModelParameterised.HeatAndElectricityDemand;

/**Drives the parameterised HG model variant(s) with hourly external temperature data.
 * This clones the model parameters, replacing the external temperature for each hour.
 *
 * @param modelParameters  input parameters to the model; never null
 * @param temperatures  hourly temperature records; never null nor empty
 */
public record HGTRVHPMModelByHour(HGTRVHPMModelParameterised.ModelParameters modelParameters, DDNTemperatureDataCSV temperatures)
 	{
	public HGTRVHPMModelByHour
		{
		Objects.requireNonNull(modelParameters);
		Objects.requireNonNull(temperatures);
		if(temperatures.data().isEmpty()) { throw new IllegalArgumentException(); }
		}

	/**Temperature below which space heating is required, CIBSE typical UK threshold. */
	public static final double DEFAULT_BASE_HEATING_TEMPERATURE_C = 15.5;

	/**Results of running a model against a temperature set.
	 * @param  hoursFractionSetbackRaisesDemand  fraction of the hours in which setting back B rooms
	 *     causes electricity demand by the heat pump to rise [0.0,1.0]
	 * @param demand heat and electrical mean demand; not null
	 */
	public record ScenarioResult(double hoursFractionSetbackRaisesDemand, DemandWithoutAndWithSetback demand)
		{
		public ScenarioResult
			{
			if(!Double.isFinite(hoursFractionSetbackRaisesDemand)) { throw new IllegalArgumentException(); }
			if(hoursFractionSetbackRaisesDemand < 0) { throw new IllegalArgumentException(); }
			if(hoursFractionSetbackRaisesDemand > 1) { throw new IllegalArgumentException(); }
			Objects.requireNonNull(demand);
			}
		}

	/**Run scenario on model and temperature data; never null.
	 * @param modelDetached  iff true, model detached house
	 *     (else model original 4-room bungalow)
	 * @param softATemp  iff true run with soft temperature regulation
	 *     (pure weather compensation) in A rooms,
	 *     else run with original 'still' regulation
     * @param equilibriumTemperatureMin  if not null and not zero length,
     *     used to return the minimum A-room equilibrium temperature
	 */
	public ScenarioResult runScenario(final boolean modelDetached,
			final boolean softATemp,
			final double[] equilibriumTemperatureMin)
		{
		final int hourCount = temperatures.data().size();
//		assert(hourCount > 0);

		// Running totals.
		int hoursSetbackRaisesDemand = 0;
		// Cumulative Wh.
		double heatDemandNSB = 0;
		double heatPumpElectricityNSB = 0;
		double heatDemandSB = 0;
		double heatPumpElectricitySB = 0;

		if(null != equilibriumTemperatureMin)
		    { equilibriumTemperatureMin[0] = HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C; }
		final double equilibriumTemperature[] = new double[1];

		for(final List<String> row : temperatures.data())
			{
			final double temperature = Double.parseDouble(row.get(DDNTemperatureDataCSV.INDEX_OF_TEMPERATURE));

			// Assume no heat required above standard HDD base temperature.
			if(temperature >= DEFAULT_BASE_HEATING_TEMPERATURE_C) { continue; }

			final HGTRVHPMModelParameterised.ModelParameters updateModelParameters =
					modelParameters.cloneWithAdjustedExternalTemperature(temperature);

		    equilibriumTemperature[0] = HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C;
	    	final DemandWithoutAndWithSetback power = softATemp ?
				HGTRVHPMModelParameterised.computeSoftATempDemandW(updateModelParameters, !modelDetached, equilibriumTemperature) :
    			HGTRVHPMModelParameterised.computeDetachedDemandW(updateModelParameters, !modelDetached);
			if((null != equilibriumTemperatureMin) &&
					(equilibriumTemperature[0] < equilibriumTemperatureMin[0]))
				{ equilibriumTemperatureMin[0] = equilibriumTemperature[0]; }

	    	heatDemandNSB += power.noSetback().heatDemand();
	    	heatPumpElectricityNSB += power.noSetback().heatPumpElectricity();

	    	heatDemandSB += power.withSetback().heatDemand();
	    	heatPumpElectricitySB += power.withSetback().heatPumpElectricity();

            if(power.withSetback().heatPumpElectricity() > power.noSetback().heatPumpElectricity())
            	{ ++hoursSetbackRaisesDemand; }
			}

		final double hoursFractionSetbackRaisesDemand = hoursSetbackRaisesDemand / (double) hourCount;

		final DemandWithoutAndWithSetback demand = new DemandWithoutAndWithSetback(
        		new HeatAndElectricityDemand(heatDemandNSB / hourCount, heatPumpElectricityNSB / hourCount),
        		new HeatAndElectricityDemand(heatDemandSB / hourCount, heatPumpElectricitySB / hourCount));

		return(new ScenarioResult(hoursFractionSetbackRaisesDemand, demand));
		}
 	}
