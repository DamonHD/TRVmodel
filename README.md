# TRVmodel

TRV energy modelling in home heating.

[![DOI](https://zenodo.org/badge/653210925.svg)](https://zenodo.org/doi/10.5281/zenodo.10116280)

[Also DOI:10.15126/900901](https://doi.org/10.15126/900901)

[Supporting MDPI Sustainability paper DOI:10.3390/su16114710 "To Zone or Not to Zone When Upgrading a Wet Heating System from Gas to Heat Pump for Maximum Climate Impact: A UK View"](https://www.mdpi.com/2071-1050/16/11/4710)


## Abstract

This model explores the interaction between wet/hydronic central heating systems typical of the UK,
and that will generally have to be retrofitted from gas-fired to heat pump by 2050,
and TRVs (Thermostatic Radiator Valves) in those systems to micro-zone for comfort and efficiency.

Heat-pump designers/installers have been concerned that while TRVs and zoning can lower
heat demand, they may raise electricity demand (and thus carbon footprint) for the heating system.

This model looks at various plausible UK scenarios at up to 1h resolution over 10 years,
and indicates that the problem can indeed exist with very 'tight' temperature regulation,
eg using "load compensation".  But this "bad setback effect" goes away with pure
"weather compensation", at the cost of looser temperature regulation.


## Implementation

This model is implemented in Java and expected behaviour is verified with JUnit tests.

Firstly, a flat largely-compile time model replicates one scenario from "Heat Geek".

Then a parameterised version (cross checked with the initial version) allows
checking against different external temperatures and an additional building archetype.

This extended model is then checked against a decade of hourly temperature data
at seven representative locations around the UK.

A copy of the output of a full calculation run is checked in.

Run in Eclipse 2.3.1300.v20230302-0300 on a MacBook Air M1 macOS Sonoma 14.1.2 with Java GraalVM 19.


## Acknowledgements

Thanks to Heat Geek for the clear worked example and the thought experiment!

Thanks to https://www.degreedays.net/ for temperature data to support modelling!


## Locations

https://github.com/DamonHD/TRVmodel
https://doi.org/10.5281/zenodo.10116280
https://doi.org/10.15126/900901
