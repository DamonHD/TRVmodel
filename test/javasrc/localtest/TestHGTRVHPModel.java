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

import org.hd.d.TRVmodel.hg.HGTRVHPMModel;

import junit.framework.TestCase;

/**Test the Heat Geek TRV-with-HP model.
 */
public final class TestHGTRVHPModel extends TestCase
    {
    /**Verify that test harness is sane... */
    public static void testSanity() { }

    /**Verify computed home heat loss per K (flow temperature, step 1). */
    public static void testHLWpK()
	    {
	    assertEquals(83.3, HGTRVHPMModel.HOME_HEAT_LOSS_PER_KELVIN_WpK, 0.05);
        }

    /**Verify computed mean home temperature when B rooms set back (flow temperature, step 2). */
    public static void testTMeanWhenSetback()
	    {
	    assertEquals(19.5, HGTRVHPMModel.MEAN_HOME_TEMPERATURE_WITH_SETBACK_C, 0.05);
        }

    /**Verify heat loss when B rooms set back (flow temperature, step 3).
     * Note: published value in page is 1874W, computed value is 1875.0W.
     */
    public static void testHLWhenSetback()
	    {
	    assertEquals(1874, HGTRVHPMModel.HOME_HEAT_LOSS_B_SETBACK_W, 1);
        }
    }
