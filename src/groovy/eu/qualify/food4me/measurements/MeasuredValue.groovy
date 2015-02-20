package eu.qualify.food4me.measurements;

import eu.qualify.food4me.Unit
import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
abstract class MeasuredValue {
	Unit unit
	
	/**
	 * Returns the type of the value: numeric or text
	 * @return
	 */
	public abstract String getType();
	
	/**
	 * Returns the actual value. If type == text, than this should return String. BigDecimal otherwise
	 * @return
	 */
	public abstract def getValue();
}
