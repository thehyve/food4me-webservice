package eu.qualify.food4me.measurements

import eu.qualify.food4me.interfaces.Measurable

class Status {
	static final String STATUS_VERY_LOW = "Very low"
	static final String STATUS_LOW = "Low"
	static final String STATUS_OK = "OK"
	static final String STATUS_HIGH = "High"
	static final String STATUS_VERY_HIGH = "Very high"
	static final String STATUS_UNKNOWN = "Unknown"

	String status = STATUS_UNKNOWN
	MeasuredValue value
	Measurable entity
	
	/**
	 * Color that is used to show on screen and determine the severity
	 * of the status
	 * @author robert
	 *
	 */
	public enum Color { 
		GREEN(10), AMBER(20), RED(30)
		
		private final Integer value
		
		Color(Integer value) {
			this.value = value
		}
		
		Integer getId(){
			value
		}
	}
}
