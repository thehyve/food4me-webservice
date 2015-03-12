package eu.qualify.food4me.measurements

import eu.qualify.food4me.interfaces.Measurable

class Status {
	// Statusses used for nutrients etc
	static final String STATUS_VERY_LOW = "Very low"
	static final String STATUS_LOW = "Low"
	static final String STATUS_OK = "OK"
	static final String STATUS_HIGH = "High"
	static final String STATUS_VERY_HIGH = "Very high"
	
	// Statusses used for nutrient intake from supplements
	static final String STATUS_NO = "No"
	static final String STATUS_YES = "Yes"
	
	// Statusses used for SNPS
	static final String STATUS_RISK = "Risk allele"
	static final String STATUS_NON_RISK = "Non-risk allele"

	// Other statusses
	static final String STATUS_UNKNOWN = "Unknown"

	MeasuredValue value
	Measurable entity
	
	String status = STATUS_UNKNOWN
	Color color
	
	/**
	 * A status which is unknown should result in a boolean false
	 * @return
	 */
	public boolean asBoolean() {
		return status && status != STATUS_UNKNOWN
	}
	
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
		
		static Color fromString(String value) {
			value.toUpperCase() as Color
		}
	}
}
