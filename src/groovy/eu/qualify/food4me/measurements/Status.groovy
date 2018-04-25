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
	boolean asBoolean() {
		return status && status != STATUS_UNKNOWN
	}

	String toString() {
		return "" + entity + " = " + status
	}
	
	/**
	 * Color that is used to show on screen and determine the severity
	 * of the status
	 * @author robert
	 *
	 */
	enum Color {
		GREEN(10), AMBER(20), RED(30)
		
		private final int value
		
		Color(int value) {
			this.value = value
		}

		int getId() {
			value
		}

		static Color fromString(String value) {
			value.toUpperCase() as Color
		}
	}
}
