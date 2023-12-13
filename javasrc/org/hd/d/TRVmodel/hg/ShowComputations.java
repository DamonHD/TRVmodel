package org.hd.d.TRVmodel.hg;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.hd.d.TRVmodel.data.DDNTemperatureDataCSV;
import org.hd.d.TRVmodel.data.HourlyTemperatureDataDescriptor;
import org.hd.d.TRVmodel.hg.HGTRVHPMModelByHour.ScenarioResult;
import org.hd.d.TRVmodel.hg.HGTRVHPMModelParameterised.DemandWithoutAndWithSetback;
import org.hd.d.TRVmodel.hg.HGTRVHPMModelParameterised.ModelParameters;

/**Show computations from the Heat Geek TRV/HP model. */
public final class ShowComputations
	{
	/**Prevent creation of an instance. */
    private ShowComputations() { }

    /**Print key computations/results on stdout.
     * @throws IOException
     */
	public static void showCalcs() throws IOException
		{
		System.out.println("Show HG TRV/HP model computations.");
		System.out.println("Date/time: " + new Date());

		System.out.println();
		System.out.println(String.format("Hardwired model, electricity demand normal / setback: %.0fW / %.0fW",
			HGTRVHPMModel.HEAT_PUMP_POWER_IN_NO_SETBACK_W,
			HGTRVHPMModel.HEAT_PUMP_POWER_IN_B_SETBACK_W));

    	final HGTRVHPMModelParameterised.ModelParameters defaultParams = new HGTRVHPMModelParameterised.ModelParameters();
		System.out.println(String.format("Parameterised model, all default parameters, electricity demand normal / setback: %.0fW / %.0fW",
    	    HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(defaultParams, false),
    	    HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(defaultParams, true)));

		System.out.println(String.format("Parameterised model, fixes applied for doors and CoP temperature, electricity demand normal / setback: %.0fW / %.0fW",
    	    HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(HGTRVHPMModelParameterised.ModelParameters.FIXES_APPLIED, false),
    	    HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(HGTRVHPMModelParameterised.ModelParameters.FIXES_APPLIED, true)));

		System.out.println(String.format("Parameterised model, fixes applied and AABB lower-loss arrangement, electricity demand normal / setback: %.0fW / %.0fW",
    	    HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(HGTRVHPMModelParameterised.ModelParameters.FIXES_AND_AABB, false),
    	    HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(HGTRVHPMModelParameterised.ModelParameters.FIXES_AND_AABB, true)));


        System.out.println();
        System.out.println("Parameterised model, fixes applied for doors and CoP temperature, external air temperature varied...");
        for(double eat = HGTRVHPMModel.EXTERNAL_AIR_TEMPERATURE_C - 10.0; eat < HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C; eat += 1.0)
	        {
        	final HGTRVHPMModelParameterised.ModelParameters params = new ModelParameters(
        			ModelParameters.FIXED_DOORS_PER_INTERNAL_WALL,
        			ModelParameters.FIXED_CORRECT_COP_FOR_FLOW_TEMPERATURE,
        			ModelParameters.DEFAULT_ARRANGEMENT_ABAB,
        			eat);
        	if(Math.abs(HGTRVHPMModel.EXTERNAL_AIR_TEMPERATURE_C - eat) < 0.1)
	        	{
        		System.out.println(String.format(" *** original external air temperature, ie %.1fC", eat));
	        	}
        	System.out.println(String.format("  %s, electricity demand normal / setback: %.0fW / %.0fW",
    			params.toString(),
        	    HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(params, false),
        	    HGTRVHPMModelParameterised.computeBungalowHPElectricityDemandW(params, true) ));
	        }


        System.out.println("");
        System.out.println("Parameterised model, fixes applied for doors and CoP temperature, external air temperature varied...");
        System.out.println("London (EGLL) 2018 hourly temperatures");
    	final DDNTemperatureDataCSV temperaturesLondon2018 =
    			DDNTemperatureDataCSV.loadDDNTemperatureDataCSV(DDNTemperatureDataCSV.DATA_EGLL_2018);
    	final HGTRVHPMModelByHour scenarioLondon2018 = new HGTRVHPMModelByHour(
    			HGTRVHPMModelParameterised.ModelParameters.FIXES_APPLIED, temperaturesLondon2018);
    	final ScenarioResult resultLondon2018 = scenarioLondon2018.runScenario(false, false, null);
        System.out.println(String.format("Percentage of hours that room setback raises heat pump demand: %.0f%%",
        		100 * resultLondon2018.hoursFractionSetbackRaisesDemand()));
    	final double heatNoSetbackLondon2018 = resultLondon2018.demand().noSetback().heatDemand();
    	final double heatWithSetbackLondon2018 = resultLondon2018.demand().withSetback().heatDemand();
    	System.out.println(String.format("Heat mean demand: with no setback %.0fW, with setback %.0fW; %.0f%% change with setback",
    			heatNoSetbackLondon2018, heatWithSetbackLondon2018, 100*((heatWithSetbackLondon2018/heatNoSetbackLondon2018)-1)));
    	final double powerNoSetbackLondon2018 = resultLondon2018.demand().noSetback().heatPumpElectricity();
    	final double powerWithSetbackLondon2018 = resultLondon2018.demand().withSetback().heatPumpElectricity();
    	System.out.println(String.format("Heat pump mean power: with no setback %.0fW, with setback %.0fW; %.0f%% change with setback",
    			powerNoSetbackLondon2018, powerWithSetbackLondon2018, 100*((powerWithSetbackLondon2018/powerNoSetbackLondon2018)-1)));

        System.out.println("");
        System.out.println("Parameterised model, fixes applied for doors and CoP temperature, external air temperature varied...");
        System.out.println("Glasgow (EGPF) 2018 hourly temperatures");
    	final DDNTemperatureDataCSV temperaturesGlasgow2018 =
    			DDNTemperatureDataCSV.loadDDNTemperatureDataCSV(DDNTemperatureDataCSV.DATA_EGPF_2018);
    	final HGTRVHPMModelByHour scenarioGlasgow2018 = new HGTRVHPMModelByHour(
    			HGTRVHPMModelParameterised.ModelParameters.FIXES_APPLIED, temperaturesGlasgow2018);
    	final ScenarioResult resultGlasgow2018 = scenarioGlasgow2018.runScenario(false, false, null);
        System.out.println(String.format("Percentage of hours that room setback raises heat pump demand: %.0f%%",
        		100 * resultGlasgow2018.hoursFractionSetbackRaisesDemand()));
    	final double heatNoSetbackGlasgow2018 = resultGlasgow2018.demand().noSetback().heatDemand();
    	final double heatWithSetbackGlasgow2018 = resultGlasgow2018.demand().withSetback().heatDemand();
    	System.out.println(String.format("Heat mean demand: with no setback %.0fW, with setback %.0fW; %.0f%% change with setback",
    			heatNoSetbackGlasgow2018, heatWithSetbackGlasgow2018, 100*((heatWithSetbackGlasgow2018/heatNoSetbackGlasgow2018)-1)));
    	final double powerNoSetbackGlasgow2018 = resultGlasgow2018.demand().noSetback().heatPumpElectricity();
    	final double powerWithSetbackGlasgow2018 = resultGlasgow2018.demand().withSetback().heatPumpElectricity();
    	System.out.println(String.format("Heat pump mean power: with no setback %.0fW, with setback %.0fW; %.0f%% change with setback",
    			powerNoSetbackGlasgow2018, powerWithSetbackGlasgow2018, 100*((powerWithSetbackGlasgow2018/powerNoSetbackGlasgow2018)-1)));


    	// Bungalow vs detached, original conditions.
        System.out.println("");
        System.out.println("Bungalow (original) vs detached:");
		final DemandWithoutAndWithSetback bungalowDemandW = HGTRVHPMModelParameterised.computeBungalowDemandW(HGTRVHPMModelParameterised.ModelParameters.FIXES_APPLIED);
		System.out.println(String.format("Parameterised bungalow model, fixes applied for doors and CoP temperature, electricity demand normal / setback: %.0fW / %.0fW",
    	    bungalowDemandW.noSetback().heatPumpElectricity(),
    	    bungalowDemandW.withSetback().heatPumpElectricity()));
		final DemandWithoutAndWithSetback detachedDemandW = HGTRVHPMModelParameterised.computeDetachedDemandW(HGTRVHPMModelParameterised.ModelParameters.FIXES_APPLIED, false);
		System.out.println(String.format("Parameterised detached model, fixes applied for doors and CoP temperature, electricity demand normal / setback: %.0fW / %.0fW",
    	    detachedDemandW.noSetback().heatPumpElectricity(),
    	    detachedDemandW.withSetback().heatPumpElectricity()));


		// 7 major towns/cities x 10Y, bungalow and detached (ABAB and AABB as sensitivity measure).
        System.out.println("");
        System.out.println(String.format("7 towns/cites, 10Y hourly data (201x, %d hours), bungalow and detached, two room heating patterns:", DDNTemperatureDataCSV.RECORD_COUNT_201X_TEMPERATURE_DATA));
		for(final HourlyTemperatureDataDescriptor htdd : DDNTemperatureDataCSV.DESCRIPTORS_201X_DATASET)
			{
			System.out.println(String.format("%s (weather station at %s):", htdd.conurbation(), htdd.station()));
	    	// Load temperature data for this station.
			final DDNTemperatureDataCSV temperatures201X =
    			DDNTemperatureDataCSV.loadDDNTemperatureDataCSV(new File(DDNTemperatureDataCSV.PATH_TO_201X_TEMPERATURE_DATA,
					htdd.station() + DDNTemperatureDataCSV.FILE_TAIL_FOR_201X_TEMPERATURE_FILE));
            if(DDNTemperatureDataCSV.RECORD_COUNT_201X_TEMPERATURE_DATA != temperatures201X.data().size())
            	{ throw new IOException("bad record count"); }
			for(final boolean detached : new boolean[]{false, true})
				{
		        final String archetype = detached ? "detached" : "bungalow";
				System.out.println("  Archetype " + archetype);
				for(final boolean abab : new boolean[]{true, false})
					{
			        final String layout = abab ? "ABAB" : "AABB";
					System.out.println("    Layout " + layout);
			    	final HGTRVHPMModelParameterised.ModelParameters modelParameters = new HGTRVHPMModelParameterised.ModelParameters(
							ModelParameters.FIXED_DOORS_PER_INTERNAL_WALL,
							ModelParameters.FIXED_CORRECT_COP_FOR_FLOW_TEMPERATURE,
							abab,
							ModelParameters.DEFAULT_EXTERNAL_AIR_TEMPERATURE_C);
					final HGTRVHPMModelByHour scenario201X = new HGTRVHPMModelByHour(
		    			modelParameters,
		    			temperatures201X);
			    	System.out.println("      Scenario base model parameters: " + modelParameters);
			    	final ScenarioResult result201X = scenario201X.runScenario(detached, false, null);
			    	final double heatNoSetback201X = result201X.demand().noSetback().heatDemand();
			    	final double heatWithSetback201X = result201X.demand().withSetback().heatDemand();
			    	System.out.println(String.format("      Heat mean demand: with no setback %.0fW, with setback %.0fW; %.0f%% change with setback",
			    			heatNoSetback201X, heatWithSetback201X, 100*((heatWithSetback201X/heatNoSetback201X)-1)));
			    	final double powerNoSetback201X = result201X.demand().noSetback().heatPumpElectricity();
			    	final double powerWithSetback201X = result201X.demand().withSetback().heatPumpElectricity();
			    	System.out.println(String.format("      Heat pump mean power: with no setback %.0fW, with setback %.0fW; %.0f%% change with setback",
			    			powerNoSetback201X, powerWithSetback201X, 100*((powerWithSetback201X/powerNoSetback201X)-1)));
			        System.out.println(String.format("      Percentage of hours that room setback raises heat pump demand: %.0f%%",
			        		100f * result201X.hoursFractionSetbackRaisesDemand()));
					}
				}
			}


        // Pure weather-compensated comparison.
        System.out.println("");
        for(final boolean bungalow : new boolean[] {true, false})
        	{
        	final String archetype = bungalow ? "bungalow" : "detached";

	        System.out.println(String.format("Original conditions, %s demand, with 'stiff' A-room temperature regulation:", archetype));
	    	final DemandWithoutAndWithSetback originalBungalowDemand = HGTRVHPMModelParameterised.computeDetachedDemandW(HGTRVHPMModelParameterised.ModelParameters.FIXES_APPLIED, bungalow);
	    	final double heatNoSetbackBungalowStiff = originalBungalowDemand.noSetback().heatDemand();
	    	final double heatWithSetbackBungalowStiff = originalBungalowDemand.withSetback().heatDemand();
	    	System.out.println(String.format("  Heat mean demand: with no setback %.0fW, with setback %.0fW; %.0f%% change with setback",
	    			heatNoSetbackBungalowStiff, heatWithSetbackBungalowStiff, 100*((heatWithSetbackBungalowStiff/heatNoSetbackBungalowStiff)-1)));
	    	final double powerNoSetbackBungalowStiff = originalBungalowDemand.noSetback().heatPumpElectricity();
	    	final double powerWithSetbackBungalowStiff = originalBungalowDemand.withSetback().heatPumpElectricity();
	    	System.out.println(String.format("  Heat pump mean power: with no setback %.0fW, with setback %.0fW; %.0f%% change with setback",
	    			powerNoSetbackBungalowStiff, powerWithSetbackBungalowStiff, 100*((powerWithSetbackBungalowStiff/powerNoSetbackBungalowStiff)-1)));

	    	final double equilibriumTemperatureSoftBungalow[] = new double[1];
	    	final DemandWithoutAndWithSetback softBungalowDemand = HGTRVHPMModelParameterised.computeSoftATempDemandW(HGTRVHPMModelParameterised.ModelParameters.FIXES_APPLIED, bungalow, equilibriumTemperatureSoftBungalow);
	        System.out.println(String.format("Original conditions, %s demand, with 'soft' A-room temperature regulation at %.1fC:", archetype, equilibriumTemperatureSoftBungalow[0]));
	    	final double heatNoSetbackBungalowSoft = softBungalowDemand.noSetback().heatDemand();
	    	final double heatWithSetbackBungalowSoft = softBungalowDemand.withSetback().heatDemand();
	    	System.out.println(String.format("  Heat mean demand: with no setback %.0fW, with setback %.0fW; %.0f%% change with setback",
	    			heatNoSetbackBungalowSoft, heatWithSetbackBungalowSoft, 100*((heatWithSetbackBungalowSoft/heatNoSetbackBungalowSoft)-1)));
	    	final double powerNoSetbackBungalowSoft = softBungalowDemand.noSetback().heatPumpElectricity();
	    	final double powerWithSetbackBungalowSoft = softBungalowDemand.withSetback().heatPumpElectricity();
	    	System.out.println(String.format("  Heat pump mean power: with no setback %.0fW, with setback %.0fW; %.0f%% change with setback",
	    			powerNoSetbackBungalowSoft, powerWithSetbackBungalowSoft, 100*((powerWithSetbackBungalowSoft/powerNoSetbackBungalowSoft)-1)));
        	}


        System.out.println("");
        System.out.println("Parameterised model, bungalow, soft regulation, fixes applied for doors and CoP temperature, external air temperature varied...");
        System.out.println("London (EGLL) 2018 hourly temperatures");
		for(final boolean abab : new boolean[]{true, false})
			{
	        final String layout = abab ? "ABAB" : "AABB";
			System.out.println("Layout " + layout);
	    	final HGTRVHPMModelParameterised.ModelParameters modelParameters = new HGTRVHPMModelParameterised.ModelParameters(
					ModelParameters.FIXED_DOORS_PER_INTERNAL_WALL,
					ModelParameters.FIXED_CORRECT_COP_FOR_FLOW_TEMPERATURE,
					abab,
					ModelParameters.DEFAULT_EXTERNAL_AIR_TEMPERATURE_C);
	    	final double equilibriumTemperatureMinLondon2018Soft[] = new double[1];
	    	final DDNTemperatureDataCSV temperaturesLondon2018Soft =
	    			DDNTemperatureDataCSV.loadDDNTemperatureDataCSV(DDNTemperatureDataCSV.DATA_EGLL_2018);
	    	final HGTRVHPMModelByHour scenarioLondon2018Soft = new HGTRVHPMModelByHour(
	    			modelParameters, temperaturesLondon2018Soft);
	    	final ScenarioResult resultLondon2018Soft = scenarioLondon2018Soft.runScenario(false, true, equilibriumTemperatureMinLondon2018Soft);
	        System.out.println(String.format("  Minimum A-room temperature %.1fC", equilibriumTemperatureMinLondon2018Soft[0]));
	        System.out.println(String.format("  Percentage of hours that room setback raises heat pump demand: %.1f%%",
	        		100 * resultLondon2018Soft.hoursFractionSetbackRaisesDemand()));
	    	final double heatNoSetbackLondon2018Soft = resultLondon2018Soft.demand().noSetback().heatDemand();
	    	final double heatWithSetbackLondon2018Soft = resultLondon2018Soft.demand().withSetback().heatDemand();
	    	System.out.println(String.format("  Heat mean demand: with no setback %.0fW, with setback %.0fW; %.1f%% change with setback",
	    			heatNoSetbackLondon2018Soft, heatWithSetbackLondon2018Soft, 100*((heatWithSetbackLondon2018Soft/heatNoSetbackLondon2018Soft)-1)));
	    	final double powerNoSetbackLondon2018Soft = resultLondon2018Soft.demand().noSetback().heatPumpElectricity();
	    	final double powerWithSetbackLondon2018Soft = resultLondon2018Soft.demand().withSetback().heatPumpElectricity();
	    	System.out.println(String.format("  Heat pump mean power: with no setback %.0fW, with setback %.0fW; %.1f%% change with setback",
	    			powerNoSetbackLondon2018Soft, powerWithSetbackLondon2018Soft, 100*((powerWithSetbackLondon2018Soft/powerNoSetbackLondon2018Soft)-1)));
			}


        System.out.println();
        System.out.println("Parameterised model, bungalow, soft A temperature regulation, fixes applied for doors and CoP temperature, external air temperature varied...");
        for(double eat = HGTRVHPMModel.EXTERNAL_AIR_TEMPERATURE_C - 10.0; eat < HGTRVHPMModel.SETBACK_ROOM_TEMPERATURE_C; eat += 1.0)
	        {
        	final HGTRVHPMModelParameterised.ModelParameters params = new ModelParameters(
        			ModelParameters.FIXED_DOORS_PER_INTERNAL_WALL,
        			ModelParameters.FIXED_CORRECT_COP_FOR_FLOW_TEMPERATURE,
        			ModelParameters.DEFAULT_ARRANGEMENT_ABAB,
        			eat);
        	if(Math.abs(HGTRVHPMModel.EXTERNAL_AIR_TEMPERATURE_C - eat) < 0.1)
	        	{
        		System.out.println(String.format(" *** original external air temperature, ie %.1fC", eat));
	        	}
        	final double equilibriumTemperature[] = new double[1];
        	HGTRVHPMModelParameterised.computeSoftATempDemandW(params, true, equilibriumTemperature);
        	final double sag = HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C - equilibriumTemperature[0];
        	System.out.println(String.format("  temperature sag %.1fK @ %.1fC",
        			sag, eat));
	        }
		}


	/**Generate the main summary model results table for 7 places, 10 years, in (X)HTML5; non-null.
	 * Attempts to remain at least slightly human readable.
	 * <p>
	 * The table class at least should be fixed up manually.
	 */
	public static String generateHTMLMainSummaryTable(final boolean stiff) throws IOException
		{
		// Vertically split as location, heat demand delta on setback, ABAB hp demand change, AABB demand change.
		// Each by bungalow/detached.
		final StringBuilder result = new StringBuilder();
		result.append("<table style=\"border:1px solid\" class=\"yourTableStyle\">\n");
		result.append(String.format("""
			<caption>\
			%s mode: summary of mean power change with selected-room setback of\s\
			(1) %s temperature regulation in A rooms\s\
			(2) whole-home heat demand and of\s\
			(3) heat-pump electrical demand in high ABAB and low AABB internal loss room setback arrangements\s\
			(4) for 1- and 2- storey (bungalow and detached) archetypes, \s\
			for %d UK locations.\s\
			Based on hourly temperature data for the ten years 201X.\s\
			When B rooms are set back overall home heat demand does fall,\s\
			but in the ABAB layout that maximises internal losses,\s\
			heat-pump electricity demand rises, in all scenarios,\s\
			especially in the detached house cases.\
			</caption>
			""",
			    stiff ? "Stiff" : "Soft",
			    stiff ? "stiff" : "soft",
				DDNTemperatureDataCSV.DESCRIPTORS_201X_DATASET.size()));
		result.append("""
			<thead><tr>\
			<th>Location (Weather Station)</th>\
			<th>Archetype</th>\
			<th>Home heat demand delta</th>\
			<th>ABAB heat-pump demand delta</th>\
			<th>AABB heat-pump demand delta</th>\
			</tr></thead>
			""");

		result.append("<tbody>\n");
		for(final HourlyTemperatureDataDescriptor htdd : DDNTemperatureDataCSV.DESCRIPTORS_201X_DATASET)
			{
	    	// Load temperature data for this station.
			final DDNTemperatureDataCSV temperatures201X =
				DDNTemperatureDataCSV.loadDDNTemperatureDataCSV(new File(DDNTemperatureDataCSV.PATH_TO_201X_TEMPERATURE_DATA,
					htdd.station() + DDNTemperatureDataCSV.FILE_TAIL_FOR_201X_TEMPERATURE_FILE));
	        if(DDNTemperatureDataCSV.RECORD_COUNT_201X_TEMPERATURE_DATA != temperatures201X.data().size())
	        	{ throw new IOException("bad record count"); }
			for(final boolean detached : new boolean[]{false, true})
				{
				result.append("<tr>");
				if(!detached)
				    { result.append(String.format("<td rowspan=\"2\">%s (%s)</td>", htdd.conurbation(), htdd.station())); }

		        final String archetype = detached ? "detached" : "bungalow";
		        result.append(String.format("<td>%s</td>", archetype));
				for(final boolean abab : new boolean[]{true, false})
					{
			    	final HGTRVHPMModelParameterised.ModelParameters modelParameters = new HGTRVHPMModelParameterised.ModelParameters(
							ModelParameters.FIXED_DOORS_PER_INTERNAL_WALL,
							ModelParameters.FIXED_CORRECT_COP_FOR_FLOW_TEMPERATURE,
							abab,
							ModelParameters.DEFAULT_EXTERNAL_AIR_TEMPERATURE_C);
					final HGTRVHPMModelByHour scenario201X = new HGTRVHPMModelByHour(
		    			modelParameters,
		    			temperatures201X);
			    	final ScenarioResult result201X = scenario201X.runScenario(detached, !stiff, null);
			    	final double heatNoSetback201X = result201X.demand().noSetback().heatDemand();
			    	final double heatWithSetback201X = result201X.demand().withSetback().heatDemand();
			    	// Overall home heat demand is not affected by archetype or room setback layout, so only show once.
			    	final double heatDelta201X = 100*((heatWithSetback201X/heatNoSetback201X)-1);
			    	if(!detached && abab)
			            { result.append(String.format("<td rowspan=\"2\" style=\"text-align:right\">%.1f%%</td>", heatDelta201X)); }
			    	// Heat-pump power demand delta.
			    	final double powerNoSetback201X = result201X.demand().noSetback().heatPumpElectricity();
			    	final double powerWithSetback201X = result201X.demand().withSetback().heatPumpElectricity();
			    	final double powerDelta201X = 100*((powerWithSetback201X/powerNoSetback201X)-1);
			    	result.append(String.format("<td style=\"text-align:right\">%.1f%%</td>", powerDelta201X));
					}
				result.append("</tr>\n");
				}
			}
		result.append("</tbody>\n");
		result.append("</table>");
		return(result.toString());
		}

	/**Generate the main summary model results table for 7 places, 10 years, in LaTeX non-null.
	 * Attempts to remain at least slightly human readable.
	 * <p>
	 * Some details may need to be fixed up manually, including the label.
	 */
	public static String generateLaTeXMainSummaryTable(final boolean stiff) throws IOException
		{
		final StringBuilder result = new StringBuilder();
		result.append("\\begin{table}[H]\n");
	    result.append(String.format("""
	        \\caption{\
			%s mode: summary of mean power change with selected-room setback of\s\
			(1) %s temperature regulation in A rooms\s\
			(2) whole-home heat demand and of\s\
			(3) heat-pump electrical demand in high ABAB and low AABB internal loss room setback arrangements\s\
			(4) for 1- and 2- storey (bungalow and detached) archetypes, \s\
			for %d UK locations.\s\
			Based on hourly temperature data for the ten years 201X.\s\
			When B rooms are set back overall home heat demand does fall,\s\
			but in the ABAB layout that maximises internal losses,\s\
			heat-pump electricity demand rises, in all scenarios,\s\
			especially in the detached house cases.\
			\\label{t-summary}}\
			""",
			    stiff ? "Stiff" : "Soft",
			    stiff ? "stiff" : "soft",
				DDNTemperatureDataCSV.DESCRIPTORS_201X_DATASET.size()));
        result.append("\\begin{adjustwidth}{-\\extralength}{0cm}\n"
        		+ "                \\newcolumntype{C}{>{\\centering\\arraybackslash}X}\n"
        		+ "                \\begin{tabularx}{\\fulllength}{CCCCC}\n"
        		+ "                        \\toprule\n"
        		+ "");
        result.append(""
        		+ "\\textbf{Location (Weather Station)} & "
        		+ "\\textbf{Archetype} & "
        		+ "\\textbf{Home heat demand delta} & "
        		+ "\\textbf{ABAB heat-pump demand delta} & "
        		+ "\\textbf{AABB heat-pump demand delta}"
        		+ "\\\\\n"
        		+ "");

		for(final HourlyTemperatureDataDescriptor htdd : DDNTemperatureDataCSV.DESCRIPTORS_201X_DATASET)
			{
	    	// Load temperature data for this station.
			final DDNTemperatureDataCSV temperatures201X =
				DDNTemperatureDataCSV.loadDDNTemperatureDataCSV(new File(DDNTemperatureDataCSV.PATH_TO_201X_TEMPERATURE_DATA,
					htdd.station() + DDNTemperatureDataCSV.FILE_TAIL_FOR_201X_TEMPERATURE_FILE));
	        if(DDNTemperatureDataCSV.RECORD_COUNT_201X_TEMPERATURE_DATA != temperatures201X.data().size())
	        	{ throw new IOException("bad record count"); }
			for(final boolean detached : new boolean[]{false, true})
				{
//				result.append("<tr>");
				if(!detached)
				    { result.append(String.format("\\midrule\\multirow[m]{2}{*}{%s (%s)}", htdd.conurbation(), htdd.station())); }
				result.append(" & ");

		        final String archetype = detached ? "detached" : "bungalow";
		        result.append(String.format("%s & ", archetype));
				for(final boolean abab : new boolean[]{true, false})
					{
			    	final HGTRVHPMModelParameterised.ModelParameters modelParameters = new HGTRVHPMModelParameterised.ModelParameters(
							ModelParameters.FIXED_DOORS_PER_INTERNAL_WALL,
							ModelParameters.FIXED_CORRECT_COP_FOR_FLOW_TEMPERATURE,
							abab,
							ModelParameters.DEFAULT_EXTERNAL_AIR_TEMPERATURE_C);
					final HGTRVHPMModelByHour scenario201X = new HGTRVHPMModelByHour(
		    			modelParameters,
		    			temperatures201X);
			    	final ScenarioResult result201X = scenario201X.runScenario(detached, !stiff, null);
			    	final double heatNoSetback201X = result201X.demand().noSetback().heatDemand();
			    	final double heatWithSetback201X = result201X.demand().withSetback().heatDemand();
			    	// Overall home heat demand is not affected by archetype or room setback layout, so only show once.
			    	final double heatDelta201X = 100*((heatWithSetback201X/heatNoSetback201X)-1);
			    	if(!detached && abab)
			            { result.append(String.format("\\multirow[m]{2}{*}{%.1f\\%%}", heatDelta201X)); }
					result.append(" & ");
			    	// Heat-pump power demand delta.
			    	final double powerNoSetback201X = result201X.demand().noSetback().heatPumpElectricity();
			    	final double powerWithSetback201X = result201X.demand().withSetback().heatPumpElectricity();
			    	final double powerDelta201X = 100*((powerWithSetback201X/powerNoSetback201X)-1);
			    	result.append(String.format("%.1f\\%% ", powerDelta201X));
					}
				result.append("\\\\\n");
				}
			}

        result.append("                        \\bottomrule\n"
        		+ "                \\end{tabularx}\n"
        		+ "        \\end{adjustwidth}\n"
        		+ "");
		result.append("\\end{table}");
		return(result.toString());
		}


	/**Generate the temperature sag model results table for 7 places, 10 years, in (X)HTML5; non-null.
	 * Attempts to remain at least slightly human readable.
	 * <p>
	 * The table class at least should be fixed up manually.
	 *
	 * @return HTML table for sag data; non-null, non-empty
	 */
	public static String generateHTMLSagTable()
		{
		try {
			// Vertically split as location, ABAB sag, AABB sag.
			// Each by bungalow/detached.
			final StringBuilder result = new StringBuilder();
			result.append("<table style=\"border:1px solid\" class=\"yourTableStyle\">\n");
			result.append(String.format("""
				<caption>\
				Soft mode: summary of maximum A-room temperature sag with selected-room setback of\s\
				(1) soft temperature regulation in A rooms\s\
				(2) maximum (ie worst-case) temperature dip in high ABAB and low AABB internal loss room setback arrangements\s\
				(4) for 1- and 2- storey (bungalow and detached) archetypes, \s\
				for %d UK locations.\s\
				Based on hourly temperature data for the ten years 201X.\s\
				When B rooms are set back with soft-mode regulation overall home heat demand and heat-pump does fall,\s\
				and the maximum temperature sag in A rooms is shown.\
				</caption>
				""",
					DDNTemperatureDataCSV.DESCRIPTORS_201X_DATASET.size()));
			result.append("""
				<thead><tr>\
				<th>Location (Weather Station)</th><th>Archetype</th>\
				<th>ABAB worst-case sag</th>\
				<th>AABB worst-case sag</th>\
				</tr></thead>
				""");

			result.append("<tbody>\n");
			for(final HourlyTemperatureDataDescriptor htdd : DDNTemperatureDataCSV.DESCRIPTORS_201X_DATASET)
				{
				// Load temperature data for this station.
				final DDNTemperatureDataCSV temperatures201X =
					DDNTemperatureDataCSV.loadDDNTemperatureDataCSV(new File(DDNTemperatureDataCSV.PATH_TO_201X_TEMPERATURE_DATA,
						htdd.station() + DDNTemperatureDataCSV.FILE_TAIL_FOR_201X_TEMPERATURE_FILE));
			    if(DDNTemperatureDataCSV.RECORD_COUNT_201X_TEMPERATURE_DATA != temperatures201X.data().size())
			    	{ throw new IOException("bad record count"); }
				for(final boolean detached : new boolean[]{false, true})
					{
					result.append("<tr>");
					if(!detached)
				    	{ result.append(String.format("<td rowspan=\"2\">%s (%s)</td>", htdd.conurbation(), htdd.station())); }

			        final String archetype = detached ? "detached" : "bungalow";
			        result.append(String.format("<td>%s</td>", archetype));
					for(final boolean abab : new boolean[]{true, false})
						{
				        final String layout = abab ? "ABAB" : "AABB";
				    	final HGTRVHPMModelParameterised.ModelParameters modelParameters = new HGTRVHPMModelParameterised.ModelParameters(
								ModelParameters.FIXED_DOORS_PER_INTERNAL_WALL,
								ModelParameters.FIXED_CORRECT_COP_FOR_FLOW_TEMPERATURE,
								abab,
								ModelParameters.DEFAULT_EXTERNAL_AIR_TEMPERATURE_C);
						final HGTRVHPMModelByHour scenario201X = new HGTRVHPMModelByHour(
			    			modelParameters,
			    			temperatures201X);
						final double equilibriumTemperatureMin[] = new double[1];
				    	final ScenarioResult result201X = scenario201X.runScenario(detached, true, equilibriumTemperatureMin);
				    	final double sag = HGTRVHPMModel.NORMAL_ROOM_TEMPERATURE_C - equilibriumTemperatureMin[0];
				    	result.append(String.format("<td style=\"text-align:right\">%.1fK</td>", sag));
						}
					result.append("</tr>\n");
					}
				}
			result.append("</tbody>\n");
			result.append("</table>");
			return(result.toString());
		    }
		catch (final IOException e)
		    { throw new RuntimeException("should not happen", e); }
		}

	}
