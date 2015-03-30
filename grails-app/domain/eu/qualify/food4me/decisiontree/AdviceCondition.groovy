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

import eu.qualify.food4me.Property
import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
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
