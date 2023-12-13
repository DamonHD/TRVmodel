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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**Wraps degreedays.net hourly temperature CSV data to make it clear what it is; data cannot be null.
 * Note that neither rows nor fields can be null, but may be empty.
 * <p>
 * Data sample:
<pre>
Description:,"Hourly temperatures in Celsius"
Source:,"www.degreedays.net"
Accuracy:,"Estimates were made to account for missing data: the ""% Estimated"" column shows how much each figure was affected (0% is best, 100% is worst)"
Station:,"London, GB (0.45W,51.48N)"
Station ID:,EGLL

Datetime,Timezone,Date,Time,Temp (<B0>C),% Estimated
2018-01-01 00:00,GMT,2018-01-01,00:00,7,0
2018-01-01 01:00,GMT,2018-01-01,01:00,7,0
2018-01-01 02:00,GMT,2018-01-01,02:00,6.3,0
2018-01-01 03:00,GMT,2018-01-01,03:00,6,0
</pre>
 */
public record DDNTemperatureDataCSV(List<List<String>> data)
    {
	/**Construct instance. */
	public DDNTemperatureDataCSV { Objects.requireNonNull(data); }

	/**If true, attempt to minimise memory consumption when parsing and loading EOUDATACSV data. */
	private static final boolean OPTIMISE_MEMORY_IN_CSV_PARSE = true;

	/**Charset for degreedays.net hourly temperature data CSV format (ISO-Latin-1 for degree symbol 0xB0). */
	public static final Charset TEMPDATACSV_CHARSET = StandardCharsets.ISO_8859_1;

	/**Immutable regex pattern used to split CSV lines; never null.
	 * This is basically just a simple ","
	 * which with split() should preserve empty fields.
	 */
	public static final Pattern delimCSV = Pattern.compile(",");

	/**Index of key temperature field (C) in each data row; positive. */
	public static final int INDEX_OF_TEMPERATURE = 4;

	/**Default path to temperature data directory from project root; non-null. */
    public static final File DEFAULT_PATH_TO_TEMPERATURE_DATA = new File("data/temperature");

    /**Sample data set in default directory: EGLL (LHR/Heathrow) for 2018 as ~median HDD (12) from 2008 to 2022. */
    public static final File DATA_EGLL_2018 = new File(DEFAULT_PATH_TO_TEMPERATURE_DATA, "EGLL_CelsiusTemps_2018_extract.csv.gz");

    /**Sample data set in default directory: EGPF (GLA/Glasgow) for 2018. */
    public static final File DATA_EGPF_2018 = new File(DEFAULT_PATH_TO_TEMPERATURE_DATA, "EGPF_CelsiusTemps_2018_extract.csv.gz");

	/**Default path to 201X decade temperature data directory from project root; non-null. */
    public static final File PATH_TO_201X_TEMPERATURE_DATA = new File(DEFAULT_PATH_TO_TEMPERATURE_DATA, "201X");
    /**Tail of filename for 201X temperature data files; non-null. */
    public static final String FILE_TAIL_FOR_201X_TEMPERATURE_FILE = "_CelsiusTemps_201X_extract.csv.gz";

    /**Expected number of records in each of the 201X data files. */
    public static final int RECORD_COUNT_201X_TEMPERATURE_DATA = 87648;

    /**Data descriptors for 201X temperature dataset; non-null. */
    public static final List<HourlyTemperatureDataDescriptor> DESCRIPTORS_201X_DATASET =
		Collections.unmodifiableList(Arrays.asList(new HourlyTemperatureDataDescriptor[] {
			new HourlyTemperatureDataDescriptor("EGAA", "Belfast", RECORD_COUNT_201X_TEMPERATURE_DATA),
			new HourlyTemperatureDataDescriptor("EGCC", "Manchester", RECORD_COUNT_201X_TEMPERATURE_DATA),
			new HourlyTemperatureDataDescriptor("EGFF", "Cardiff", RECORD_COUNT_201X_TEMPERATURE_DATA),
			new HourlyTemperatureDataDescriptor("EGLL", "London", RECORD_COUNT_201X_TEMPERATURE_DATA),
			new HourlyTemperatureDataDescriptor("EGNT", "Newcastle", RECORD_COUNT_201X_TEMPERATURE_DATA),
			new HourlyTemperatureDataDescriptor("EGPF", "Glasgow", RECORD_COUNT_201X_TEMPERATURE_DATA),
			new HourlyTemperatureDataDescriptor("EGPH", "Edinburgh", RECORD_COUNT_201X_TEMPERATURE_DATA)
		}));

	/**Parse degreedays.net hourly temperature CSV file/stream; never null but may be empty.
     * Parses CSV as List (by row) of List (of String fields),
     * omitting empty and comment (starting with '#') rows.
     * <p>
     * This <em>does not</em> validate the content.
     * </p>
     * The outer and inner Lists implement RandomAccess.
     * <p>
     * This buffers its input for efficiency if not already a BufferedReader.
     *
     * @param r  stream to read from, not closed by this routine; never null
     * @return a non-null but possibly-empty in-order immutable List of rows,
     *    each of which is a non-null but possibly-empty in-order List of fields
     * @throws IOException  if there is an I/O problem or the data is malformed
     */
    public static DDNTemperatureDataCSV parseDDNTemperatureDataCSV(final Reader r)
        throws IOException
        {
        if(null == r) { throw new IllegalArgumentException(); }

        // Wrap a buffered reader around the input if not already so.
        final BufferedReader br = (r instanceof BufferedReader) ? (BufferedReader)r :
        	new BufferedReader(r, 8192);

        // Initially-empty result...
        // As of 2023-06-08, largest non-daily-cadence data CSV is 203 lines.
        final ArrayList<List<String>> result = new ArrayList<>(256);

        String row;
        while(null != (row = br.readLine()))
            {
        	// Skip empty rows.
        	if("".equals(row)) { continue; }
        	// Skip anything other than data rows starting with a date.
        	if(!row.startsWith("2")) { continue; }
            final String fields[] = delimCSV.split(row);
            if(fields.length < 1) { continue; }

            if(fields[0].isEmpty())
                { throw new IOException("unexpected empty date"); }

            // Memory micro-optimisation.
            // Where possible, share duplicate values from the previous row.
            if(OPTIMISE_MEMORY_IN_CSV_PARSE && !result.isEmpty())
	            {
	            final List<String> prevRow = result.get(result.size() - 1);
	            if(fields.length == prevRow.size())
		            {
		            for(int i = fields.length; --i >= 0; )
			            {
		            	final String fi = fields[i];
                        // Else if this matches the item from the previous row, reuse it.
			            final String pi = prevRow.get(i);
						if(fi.equals(pi)) { fields[i] = pi; }
			            }
		            }
	            }

            // Package up row data (and make it unmodifiable).
            result.add(Collections.unmodifiableList(Arrays.asList(fields)));
            }

        result.trimToSize(); // Free resources...
        return(new DDNTemperatureDataCSV(Collections.unmodifiableList(result))); // Make outer list unmodifiable...
        }

	/**Load from GZIPped file degreedays.net hourly temperature data; never null but may be empty.
	 * @throws IOException  if file not present or unreadable/unparseable.
	 */
	public static DDNTemperatureDataCSV loadDDNTemperatureDataCSV(final File gzippedCSV)
	    throws IOException
	    {
		if(null == gzippedCSV) { throw new IllegalArgumentException(); }
		try(final Reader r = new InputStreamReader(
				new GZIPInputStream(
					new BufferedInputStream(
						new FileInputStream(gzippedCSV))), TEMPDATACSV_CHARSET))
		    { return(parseDDNTemperatureDataCSV(r)); }
	    }
	}
