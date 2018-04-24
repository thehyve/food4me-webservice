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
package eu.qualify.food4me.reference

import eu.qualify.food4me.Property
import eu.qualify.food4me.measurements.Status

/**
 * Reference value for a given property
 * @author robert
 */
class ReferenceValue {
	/**
	 * Resulting property of this reference 
	 */
	Property subject

	/**
	 * Resulting status of this rule
	 */
	String status
	
	/**
	 * Status color. This color describes the severity of the
	 * status. Red is more severe than Amber and Green is OK
	 */
	Status.Color color
	
	/**
	 * Number of conditions applicable to this reference
	 * This is stored separately (and could be updated using triggers) to 
	 * prevent counting every time
	 */
	int numConditions = 0
	
	static hasMany = [ conditions: ReferenceCondition ]
	
    static constraints = {

    }
	
	/**
	 * Returns the condition for the subject itself, if any  
	 * @return
	 */
	ReferenceCondition getSubjectCondition() {
		conditions.find { it.subject == subject }
	}
	
	/**
	 * Returns a list of properties needed to determine the status for a given property
	 * 
	 * For example, the status of BMI depends on the BMI itself, but also on Gender and Age
	 * @param property
	 * @return
	 */
	static List getConditionProperties(Property p) {
		// TODO: Find out why I can't retrieve all referenceconditions with it.referenceValue.subject == p
		def referenceValues = ReferenceValue.findAllBySubject(p)
		
		if( !referenceValues )
			return []
			
		def criteria = ReferenceCondition.createCriteria()
		def referenceConditions = criteria.list {
			referenceValue {
				'in'( 'id', referenceValues*.id )
			}
		}
		
		referenceConditions.collect { it.subject }.unique() as List
	}

	/**
	 * Counts the number of subjects for which the database contains a reference.
	 * @return
	 */
	static int getSubjectCount() {
		createCriteria().list {
			projections {
				countDistinct("subject")
			}
		}[0]
	}
}
