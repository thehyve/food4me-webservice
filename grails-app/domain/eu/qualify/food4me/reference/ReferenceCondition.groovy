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
package eu.qualify.food4me.reference

import java.math.BigDecimal;

import eu.qualify.food4me.Property;

class ReferenceCondition {
	public static final TYPE_TEXT = 'text'
	public static final TYPE_NUMERIC = 'numeric'
	
	/**
	 * Property this condition applies to 
	 */
	Property subject
	
	/**
	 * Lowest value for this property to result in this status
	 */
	BigDecimal low

	/**
	 * Highest value for this property to result in this status
	 */
	BigDecimal high
	
	/**
	 * String value that the property should have to result in this status
	 */
	String value

	/**
	 * Condition type determines whether a low/high or value is given
	 */
	String conditionType
	
	static belongsTo = [referenceValue: ReferenceValue]
	
    static constraints = {
		low nullable: true
		high nullable: true
		value nullable: true
    }
	
	public String toString() {
		"Reference condition: " + subject + " " + getConditionDescription()
	}
	
	public String getConditionDescription() {
		if( conditionType == TYPE_TEXT ) {
			return "== " + value
		} else {
			if( low != null && high != null ) {
				return "" + low + " - " + high
			} else if( low != null ) {
				return "> " + low
			} else {
				return "<= " + high
			}
		}
	}
}
