package eu.qualify.food4me.measurements

import eu.qualify.food4me.interfaces.Measurable
import groovy.transform.Canonical


@Canonical
class Measurement {
	Measurable property
	MeasuredValue value
	
	/**
	 * Boolean specifying whether this measurement is 
	 * provided by the user or derived by the application
	 */
	boolean derived = false
	
	public String toString() {
		"Measurement for " + property + ": " + value
	} 
}
