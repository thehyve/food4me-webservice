/*
 *  Copyright (C) 2015 The Hyve
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
