package eu.qualify.food4me.measurements;

import eu.qualify.food4me.Unit
import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class MeasuredNumericValue extends MeasuredValue {
	BigDecimal value = 0
	
	/**
	 * Returns the type of the value: numeric or text
	 * @return
	 */
	public String getType() {
		"numeric"
	}
}
