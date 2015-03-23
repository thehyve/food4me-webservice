package eu.qualify.food4me.interfaces

import eu.qualify.food4me.measurements.MeasurementStatus
import eu.qualify.food4me.measurements.Measurements

interface Parser {
	/**
	 * Parses the input to retrieve measurements from it
	 * @param params
	 * @return
	 */
	Measurements parseMeasurements(def input)

	/**
	 * Parses the input to determine the status of several measurements
	 * @param params
	 * @return
	 */
	MeasurementStatus parseStatus(def input)
		
	/**
	 * Parses the input to retrieve a list of entities to give advice on
	 * @param params
	 * @return
	 */
	List<Advisable> parseEntityList(def input);

}
