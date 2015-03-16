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
