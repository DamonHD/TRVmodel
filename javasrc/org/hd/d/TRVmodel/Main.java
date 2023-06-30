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
        }

    /**Accepts command-line arguments.
     *
     * Accepts the following commands:
     * <ul>
     * </ul>
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
