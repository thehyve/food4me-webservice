package eu.qualify.food4me

import eu.qualify.food4me.interfaces.Measurable
import groovy.transform.EqualsAndHashCode


@EqualsAndHashCode
class ModifiedProperty implements Measurable {
	Property property
	String modifier
	
	public String toString() {
		String description = property.toString()
		
		if( modifier )
			description += " [" + modifier + "]"
		 
		description
	}
	
	public enum Modifier {
		// Modifiers with respect to intake
		INTAKE_DIETARY("Dietary"),
		INTAKE_SUPPLEMENT("Supplements")
		
		private final String value
		
		Modifier(String value) {
			this.value = value
		}
		
		String getId(){
			value
		}
	}
}
