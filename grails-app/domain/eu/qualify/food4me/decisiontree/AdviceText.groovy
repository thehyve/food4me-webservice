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
package eu.qualify.food4me.decisiontree


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
		if( !advices )
			return [:]
		
		// Find the texts for the advices given. Create a map of the texts
		// with the code being the key
		def texts = [:]
		AdviceText.findAllByCodeInListAndLanguage( advices*.code, language ).each {
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
	
	/**
	 * Returns a list of distinct languages available for the given code
	 * @return
	 */
	public static List<String> getLanguagesForAdvice( String adviceCode ) {
		if( !adviceCode )
			return []
			
		def criteria = AdviceText.createCriteria()
		
		criteria.listDistinct {
			eq("code", adviceCode)
			projections {
				distinct "language"
			}
		}
	}

	/**
	 * Returns a list of distinct languages available for the given code
	 * @return
	 */
	public static List<String> getLanguagesForAdvice( Advice advice ) {
		getLanguagesForAdvice(advice?.code)
	}
	
	public static boolean isLanguageSupported( String language ) {
		if( !language )
			return false
			
		language.toLowerCase() in getLanguages()
	}
}
