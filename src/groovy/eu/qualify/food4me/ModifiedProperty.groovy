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

	/**
	 * Returns the property to be used to retrieve references for this property
	 *
	 * Can be used on modified properties to use the references of the root property
	 * @return
	 */
	Property getReferenceProperty() {
		// Only use the root property for the intake from food, not on other modifiers
		modifier == Modifier.INTAKE_DIETARY.id ? rootProperty : null
	}
	
	String toString() {
		modifier ? "${property} [${modifier}]" : property.toString()
	}
	
	/**
	 * Returns a list of allowed modifiers for the given property
	 */
	static List<Modifier> getAllowedModifiers(Property property) {
		List<Modifier> modifiers = []
		
		if(property.propertyGroup == Property.PROPERTY_GROUP_NUTRIENT) {
			// Modifiers that can be specified on nutrients
			modifiers += [ 
				Modifier.INTAKE_MEAT_FISH,
				Modifier.INTAKE_DAIRY, 
				Modifier.INTAKE_SOUP_SAUCES, 
				Modifier.INTAKE_SWEETS_SNACKS,
				Modifier.INTAKE_FATS_SPREADS,
				Modifier.INTAKE_POTATOES_RICE_PASTA,
				Modifier.INTAKE_EGGS,
				Modifier.INTAKE_DIETARY,
				Modifier.INTAKE_SUPPLEMENTS ]
			
			// Special modifiers on salt
			if( property.entity?.toLowerCase() == "salt" ) {
				modifiers += [ Modifier.SALT_ON_TABLE, Modifier.SALT_WHEN_COOKING ]
			}
		}
		
		return modifiers
	}

	static enum Modifier {
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

		final String id

		Modifier(String value) {
			this.id = value
		}

		/**
		 * Checks whether this enum contains an item with the given string as value
		 * @param test
		 * @return
		 */
		static boolean contains(String test) {
			values().any { it.id == test }
		}
	}
}
