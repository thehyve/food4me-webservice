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
package eu.qualify.food4me

import eu.qualify.food4me.interfaces.Measurable
import groovy.transform.EqualsAndHashCode


@EqualsAndHashCode
class ModifiedProperty implements Measurable {
	Property property
	String modifier
	
	/**
	 * Returns the root property for this measurable
	 * @return
	 */
	Property getRootProperty() {
		property
	}
	
	public String toString() {
		String description = property.toString()
		
		if( modifier )
			description += " [" + modifier + "]"
		 
		description
	}
	
	public enum Modifier {
		// Modifiers with respect to nutrient intake
		INTAKE_MEAT_FISH("Meat and fish"),
		INTAKE_DAIRY("Dairy"),
		INTAKE_SOUP_SAUCES("Soup and sauces"),
		INTAKE_SWEETS_SNACKS("Sweets and snacks"),
		INTAKE_FATS_SPREADS("Fats and spreads"),
		INTAKE_POTATOES_RICE_PASTA("Potatoes, rice and pasta"),
		INTAKE_EGGS("Eggs"),
		
		INTAKE_DIETARY("From food"),
		INTAKE_SUPPLEMENTS("From supplements"),
		
		// Modifiers used for food groups that contribute to nutrient intake
		FIRST_CONTRIBUTING_FOOD_GROUP("First contributing food group"),
		SECOND_CONTRIBUTING_FOOD_GROUP("Second contributing food group"),
		
		// Special modifiers used for salt intake
		SALT_ON_TABLE( "Add at the table" ),
		SALT_WHEN_COOKING( "Add when cooking" )
		
		private final String value
		
		Modifier(String value) {
			this.value = value
		}
		
		String getId(){
			value
		}
		
		/**
		 * Checks whether this enum contains an item with the given string as value
		 * @param test
		 * @return
		 */
		public static boolean contains(String test) {
			for (Modifier m : Modifier.values()) {
				if (m.id == test) {
					return true;
				}
			}
		
			return false;
		}
	}
}
