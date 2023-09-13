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

package org.hd.d.TRVmodel;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.hd.d.TRVmodel.hg.ShowComputations;

/**Main (command-line) entry-point for the data handler.
 */
public final class Main
    {
    /**Print a summary of command options to stderr. */
    private static void printOptions()
        {
        System.err.println("Commands/options");
        System.err.println("  -help");
        System.err.println("    This summary/help.");
        System.err.println("  -hg");
        System.err.println("    Show Heat Geek TRV/HP model and variants.");
        System.err.println("  -htmltable XXX");
        System.err.println("    Write HTML table XXX to out.html for debugging.");
        System.err.println("    XXX can be one of: main, summary");
        }

    /**Default name of output file for debugging HTML table generation; non-null. */
    public static final File DEFAULT_OUTPUT_NAME = new File("out.html");

    /**Accepts command-line arguments.
     * See {@link #printOptions()}.
     */
    public static void main(final String[] args)
        {
        if((args.length < 1) || "-help".equals(args[0]))
            {
            printOptions();
            return; // Not an error.
            }

        // Command is first argument.
        final String command = args[0];

        try
            {
        	if("-hg".equals(args[0]))
    			{
        		ShowComputations.showCalcs();
        		System.exit(0);
    			}

        	if("-htmltable".equals(args[0]) && (args.length > 1))
    			{
        		final String tableHTML;

        		switch(args[1])
        		{
        		case "summary": tableHTML = ShowComputations.generateHTMLMainSummaryTable(); break;
        		default:
        			System.err.println("unknown table: " + args[1]);
            		System.exit(1);
            		return; // Should be unreachable.
        		}

        		// Write HTML bare, as UTF-8 (though should only be 7-bit ASCII).
        		// Expects to overwrite any existing file.
        		try (Writer w = new FileWriter(DEFAULT_OUTPUT_NAME, StandardCharsets.UTF_8))
	        		{ w.write(tableHTML); }

        		System.exit(0);
    			}

            }
        catch(final Throwable e)
            {
            System.err.println("FAILED command: " + command);
            e.printStackTrace();
            System.exit(1);
            }

        // Unrecognised/unhandled command.
        System.err.println("Unrecognised or unhandled command: " + command);
        printOptions();
        System.exit(1);
        }
    }
