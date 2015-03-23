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
	
	/**
	 * Returns a map of translations in a given language
	 * @param advices
	 * @param language
	 * @return
	 */
	public static Map getTranslations( List<Advice> advices, String language) {
		// Find the texts for the advices given. Create a map of the texts
		// with the code being the key
		def texts = [:]
		AdviceText.findByCodeInListAndLanguage( advices*.code, language ).each {
			texts[ it.code ] = it.text
		}
		
		texts
	}
	
	/**
	 * Returns a list of distinct languages in the database
	 * @return
	 */
	public static List<String> getLanguages() {
		def criteria = AdviceText.createCriteria()
		
		criteria.listDistinct {
			projections {
				distinct "language"
			}
		}
	}
	
	public static boolean isLanguageSupported( String language ) {
		if( !language )
			return false
			
		language.toLowerCase() in getLanguages()
	}
}
