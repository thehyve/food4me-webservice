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

import eu.qualify.food4me.FoodGroup

class NutrientIntake extends Measurement {
	
	Map<FoodGroup,MeasuredValue> intake
	
	public MeasuredValue getMeasuredValue() {
		if( !intake )
			return null;
		
		def total = new MeasuredNumericValue()
		intake.values().each { it ->
			if( !total.unit ) 
				total.unit = intake.unit
				
			if( total.unit != intake.unit ) {
				throw Exception( "Cannot compute total nutrient intake when using different units. All nutrient intakes should be provided in the same unit")
			}
			
			total.value += it.value
		}
		
		total
	}
	
	public MeasuredValue getTotal() { getMeasuredValue() }
}
