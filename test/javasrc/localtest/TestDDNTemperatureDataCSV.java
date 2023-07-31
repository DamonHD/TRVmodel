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

package localtest;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.hd.d.TRVmodel.data.DDNTemperatureDataCSV;

import junit.framework.TestCase;

/**Miscellaneous tests.
 */
public final class TestDDNTemperatureDataCSV extends TestCase
    {
    /**Test parsing of small fragment.
     * @throws IOException
     */
    public static void testParse() throws IOException
	    {
    	// Note degree character has been replaced with '?' here.
    	final String fragment = """
Description:,"Hourly temperatures in Celsius"
Source:,"www.degreedays.net"
Accuracy:,"Estimates were made to account for missing data: the ""% Estimated"" column shows how much each figure was affected (0% is best, 100% is worst)"
Station:,"London, GB (0.45W,51.48N)"
Station ID:,EGLL

Datetime,Timezone,Date,Time,Temp (?C),% Estimated
2018-01-01 00:00,GMT,2018-01-01,00:00,7,0
2018-01-01 01:00,GMT,2018-01-01,01:00,7,0
2018-01-01 02:00,GMT,2018-01-01,02:00,6.3,0
2018-01-01 03:00,GMT,2018-01-01,03:00,6,0
2018-01-01 04:00,GMT,2018-01-01,04:00,6,0
	    			""";
    	final DDNTemperatureDataCSV result = DDNTemperatureDataCSV.parseDDNTemperatureDataCSV(
    			new StringReader(fragment));
    	assertNotNull(result);
    	assertEquals("should be 5 data rows", 5, result.data().size());
    	assertEquals("6.3", result.data().get(2).get(DDNTemperatureDataCSV.INDEX_OF_TEMPERATURE));
	    }

    /**Test loading and parsing of sample (compressed) temperature date file.
     * @throws IOException
     */
    public static void testLoad() throws IOException
	    {
    	final File path = new File(DDNTemperatureDataCSV.DEFAULT_PATH_TO_TEMPERATURE_DATA, "EGLL_CelsiusTemps_2018_extract.csv.gz");
    	final DDNTemperatureDataCSV result = DDNTemperatureDataCSV.loadDDNTemperatureDataCSV(path);
    	assertNotNull(result);
    	assertEquals("should be 8760 data rows (1 non-leap year, hourly)", 8760, result.data().size());
    	assertEquals("6.3", result.data().get(2).get(DDNTemperatureDataCSV.INDEX_OF_TEMPERATURE));
    	}
    }
