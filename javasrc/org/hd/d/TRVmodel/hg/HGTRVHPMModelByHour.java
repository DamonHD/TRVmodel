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
 	}
