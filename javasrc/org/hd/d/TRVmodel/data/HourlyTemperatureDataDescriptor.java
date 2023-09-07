/*
Copyright (c) 2023, Damon Hart-Davis

Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package org.hd.d.TRVmodel.data;

import java.util.Objects;

/**A descriptor for an hourly temperature data file.
 * Contains the weather station (airport) code (non-null),
 * the name of the nearest major city/town (non-null)
 * and the expected number of records (non-negative) as a simple guard against corruption.
 * <p>
 * Can be used to describe the source of DDNTemperatureDataCSV items.
 *
 * @param station  location of weather station, eg at ICAO airport; never null (nor empty)
 * @param conurbation  nearby large town or city; never null (nor empty)
 * @param records  expected record count
 */
public record HourlyTemperatureDataDescriptor(String station, String conurbation, int records)
	{
	public HourlyTemperatureDataDescriptor
	    {
		Objects.requireNonNull(station);
		Objects.requireNonNull(conurbation);
		if(records < 0) { throw new IllegalArgumentException(); }
		}
	}
