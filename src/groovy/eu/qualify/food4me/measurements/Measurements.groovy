package eu.qualify.food4me.measurements

import eu.qualify.food4me.Property
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
	
	public MeasuredValue getValueFor( Property p ) {
		return measurements.find { it.property == p }?.value
	}

}
