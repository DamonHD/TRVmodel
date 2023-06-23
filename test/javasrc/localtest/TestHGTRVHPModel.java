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

    /**Verify heat loss fall/reduction when B rooms set back (flow temperature, step 3).
     * Note: published value in page is 6.3%, computed value is 6.25%.
     */
    public static void testHLFallWhenSetback()
	    {
	    assertEquals(0.063, HGTRVHPMModel.HOME_HEAT_LOSS_FALL_B_SETBACK, 0.05);
        }

    /**Verify internal heat loss through walls and door per A room.
     * Note: text is inconsistent about whether one door per wall or per pair of walls.
     */
    public static void testIWLoss()
	    {
	    assertEquals(98.4, HGTRVHPMModel.INTERNAL_WALL_MINUS_DOOR_HEAT_LOSS_W, 0.05);
	    assertEquals(48, HGTRVHPMModel.INTERNAL_DOOR_HEAT_LOSS_W, 0.05);
	    assertEquals(146.4, HGTRVHPMModel.INTERNAL_WALL_AND_DOOR_HEAT_LOSS_PER_A_ROOM_W, 0.05);
	    }

    /**Verify radiator output power in each room when B rooms are setback. */
    public static void testRadPowerSetback()
    	{
	    assertEquals(646, HGTRVHPMModel.RADIATOR_POWER_IN_A_ROOMS_WHEN_B_SETBACK_W, 0.5);
	    assertEquals(291, HGTRVHPMModel.RADIATOR_POWER_IN_B_ROOMS_WHEN_B_SETBACK_W, 0.5);
    	}

    /**Verify A radiators delta-T (MW-AT) uplift multiplier and new value, and MW-AT, when B rooms are setback. */
    public static void testARadsDT()
	    {
	    assertEquals(1.22, HGTRVHPMModel.RADIATOR_DT_UPLIFT_IN_A_ROOMS_WHEN_B_SETBACK_MULTIPLIER, 0.005);
	    assertEquals(30.5, HGTRVHPMModel.RADIATOR_DT_IN_A_ROOMS_WHEN_B_SETBACK_K, 0.05);
	    assertEquals(51.5, HGTRVHPMModel.RADIATOR_MW_IN_A_ROOMS_WHEN_B_SETBACK_C, 0.05);
	    }

    /**Verify claimed heat-pump power in in normal and setback cases. */
    public static void testHPPowerIn()
		{
	    assertEquals(769, HGTRVHPMModel.HEAT_PUMP_POWER_IN_NO_SETBACK_W, 0.5);
	    assertEquals(815, HGTRVHPMModel.HEAT_PUMP_POWER_IN_B_SETBACK_W, 0.5);
	    // The overall point of this example!
	    assertTrue("electrical power goes UP with Bs set back", HGTRVHPMModel.HEAT_PUMP_POWER_IN_NO_SETBACK_W < HGTRVHPMModel.HEAT_PUMP_POWER_IN_B_SETBACK_W);
		}

    }
