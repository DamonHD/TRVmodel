package org.hd.d.TRVmodel.hg;

import java.util.Objects;

import org.hd.d.TRVmodel.data.DDNTemperatureDataCSV;

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
 	}
