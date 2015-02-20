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
