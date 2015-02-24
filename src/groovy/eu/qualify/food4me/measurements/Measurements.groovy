package eu.qualify.food4me.measurements

import eu.qualify.food4me.interfaces.Measurable
import groovy.transform.Canonical

 @Canonical
 class Measurements {
	List<Measurement> measurements
	
	public Measurements() {
		measurements = []
	}
	
	List<Measurement> getAll() {
		measurements
	}
	
	void add(Measurement measurement) {
		measurements << measurement
	}
	
	public MeasuredValue getValueFor( Measurable p ) {
		return measurements.find { it.property == p }?.value
	}

}
