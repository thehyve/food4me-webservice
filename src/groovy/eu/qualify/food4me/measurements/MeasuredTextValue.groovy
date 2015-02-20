package eu.qualify.food4me.measurements;

import eu.qualify.food4me.Unit
import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class MeasuredTextValue extends MeasuredValue {
	String value = ""
	
	/**
	 * Returns the type of the value: numeric or text
	 * @return
	 */
	public String getType() {
		"text"  
	}
}
