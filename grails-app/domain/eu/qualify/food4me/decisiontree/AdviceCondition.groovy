package eu.qualify.food4me.decisiontree

import java.math.BigDecimal;

import eu.qualify.food4me.Property;

class AdviceCondition {
	/**
	 * Property this condition applies to 
	 */
	Property subject
	
	/**
	 * Modifier on the property to distinguish statusses. Can be e.g. 'dietary' for dietary intake, or 'supplement' for supplement intake
	 */
	String modifier
	
	/**
	 * Specific status that the (subject) property should have for the condition to apply
	 */
	String status

	/**
	 * Specific value that the (subject) property should have for the condition to apply
	 */
	String value
	
	static belongsTo = [advice: Advice]
	
    static constraints = {
		modifier nullable: true
		status nullable: true
		value nullable: true
    }
	
	public String toString() {
		def conditions = []
		if( status )
			conditions << "status == " + status
			
		if( value )
			conditions << "value == " + value
		
		"Advice condition: " + subject + "( " + conditions.join( " / " ) + ")"
	}
}
