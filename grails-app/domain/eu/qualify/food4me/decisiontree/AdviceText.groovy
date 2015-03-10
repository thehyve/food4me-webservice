package eu.qualify.food4me.decisiontree

import eu.qualify.food4me.Property;

class AdviceText {
	/**
	 * Unique code of the advice.
	 */
	String code

	/**
	 * Text of the advice
	 */
	String text
	
	/**
	 * Language code for this text
	 */
	String language
	
	static belongsTo = [ advice: Advice ]
	
    static constraints = {
		
    }
	
	static mapping = {
		text type: 'text'
	}
}
