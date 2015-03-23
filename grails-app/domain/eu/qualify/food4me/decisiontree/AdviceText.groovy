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
	
    static constraints = {
    }
	
	static mapping = {
		text type: 'text'
		code index: 'Code_index'
	}
	
	public static List<String> getLanguages() {
		def criteria = AdviceText.createCriteria()
		
		criteria.listDistinct {
			projections {
				distinct "language"
			}
		}
	}
}
