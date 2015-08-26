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

import eu.qualify.food4me.ModifiedProperty
import eu.qualify.food4me.Property
import eu.qualify.food4me.interfaces.Measurable
import groovy.transform.EqualsAndHashCode

import java.util.List;
import java.util.Map;


/**
 * Reference value for a given property
 * @author robert
 */
@EqualsAndHashCode(includes="code")
class Advice {
	/**
	 * Code of the advice.
	 */
	String code
	
	/**
	 * Property that this advice is about. 
	 */
	Property subject

	/**
	 * Number of conditions applicable to this advice
	 * This is stored separately (and could be updated using triggers) to 
	 * prevent counting every time
	 */
	int numConditions = 0
	
	/**
	 * (optional) position of this advice, if multiple advices apply
	 */
	int position = 0
	
	static hasMany = [ conditions: AdviceCondition ]
	
    static constraints = {
    }
	
	static mapping = {
	}
	
	public String toString() {
		return "Advice " + code + " on " + subject
	}
	
	/**
	 * Returns a list of properties needed to determine which advice for a given property should be given.
	 * 
	 * That is, it returns all properties needed to walk through the decision tree. Please note that this
	 * could also include modified properties, e.g. Omega3 intake from supplements.
	 *  
	 * For example, the advice for cholesterol could depend on the cholesterol status, as well 
	 * as on the BMI and fat intake.
	 *  
	 * @param property
	 * @return
	 */
	public static List<Measurable> getConditionProperties(Property p) {
		// TODO: Find out why I can't retrieve all conditions with it.advice.subject == p
		def advices = Advice.findAllBySubject(p)
		
		if( !advices )
			return []
			
		def criteria = AdviceCondition.createCriteria()
		def adviceConditions = criteria.list {
			advice {
				'in'( 'id', advices*.id )
			}
		}
		
		adviceConditions.collect {
			// If the advice condition related to a modified property, return a modified property object
			if( it.modifier ) {
				new ModifiedProperty( property: it.subject, modifier: it.modifier )
			} else {
				it.subject
			} 
		}.unique() as List
	}
	
	public static int getSubjectCount() {
		def criteria = Advice.createCriteria()
		def adviceSubjectCount = criteria.list {
			projections {
				countDistinct("subject")
			}
		}
		
		adviceSubjectCount[0]
	}
	
	public String getTranslation( String language = "en" ) {
		AdviceText.findByCodeAndLanguage( this.code, language )?.text
	}

}
