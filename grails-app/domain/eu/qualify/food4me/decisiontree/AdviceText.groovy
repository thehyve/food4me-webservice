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
	 * @return mapping of code to text
	 */
	static Map<String, String> getTranslations( List<Advice> advices, String language) {
		if( !advices )
			return [:]
		
		// Find the texts for the advices given. Create a map of the texts
		// with the code being the key
		findAllByCodeInListAndLanguage( advices*.code, language ).collectEntries {
			[(it.code): it.text]
		}
	}
	
	/**
	 * Returns a list of distinct languages in the database
	 * @return
	 */
	static List<String> getLanguages() {
		createCriteria().listDistinct {
			projections {
				distinct "language"
			}
		}
	}
	
	/**
	 * Returns a list of distinct languages available for the given code
	 * @return
	 */
	static List<String> getLanguagesForAdvice( String adviceCode ) {
		if( !adviceCode )
			return []
			
		createCriteria().listDistinct {
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
	static List<String> getLanguagesForAdvice( Advice advice ) {
		getLanguagesForAdvice(advice?.code)
	}

	static boolean isLanguageSupported(String language ) {
		language?.toLowerCase() in getLanguages()
	}
}
