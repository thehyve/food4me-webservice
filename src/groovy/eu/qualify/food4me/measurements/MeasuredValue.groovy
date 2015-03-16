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
	
	public String toString() {
		value + ( unit ? " " + unit.code : "" )
	}
	
	/**
	 * Factory method to create a measuredValue object
	 */
	public static MeasuredValue fromValue( def value ) {
		if( value instanceof String ) {
			new MeasuredTextValue( value: value )
		} else if( value instanceof Number ) {
			new MeasuredNumericValue( value: value )
		}
	}
}
