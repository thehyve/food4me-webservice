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
package eu.qualify.food4me

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes="externalId")
class Unit {
	/**
	 * Human readable name for this unit
	 */
	String name

	/**
	 * External identifier to identify this unit
	 * See http://bioportal.bioontology.org/ontologies/SNOMEDCT
	 */
	String externalId
	
	/**
	 * Code used to describe this unit. 
	 * See http://www.hl7.de/download/documents/ucum/ucumdata.html, column Common Synonym
	 */
	String code	
	
	static constraints = {
		code nullable: true
	}
	
	
	public String toString() {
		return "Unit " + name+ " (" + externalId + ")"
	}
}
