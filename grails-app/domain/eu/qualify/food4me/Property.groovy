package eu.qualify.food4me

import eu.qualify.food4me.interfaces.Advisable
import eu.qualify.food4me.interfaces.Measurable
import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes="externalId")
class Property implements Measurable, Advisable {
	public static final String PROPERTY_GROUP_GENERIC = "Generic"
	public static final String PROPERTY_GROUP_NUTRIENT = "Nutrient"
	public static final String PROPERTY_GROUP_BIOMARKER = "Biomarker"
	public static final String PROPERTY_GROUP_PHYSICAL = "Physical"
	public static final String PROPERTY_GROUP_SNP = "SNP"
	
	/**
	 * Group to which this property belongs. Can be nutrient, biomarker, physical, generic
	 */
	String propertyGroup
	
	/**
	 * Name of the entity
	 */
	String entity
	
	/**
	 * External identifier. See http://purl.bioontology.org/ontology/SNOMEDCT/88878007
	 */
	String externalId
	
	/**
	 * Unit required for computations in the application
	 */
	Unit unit
	
    static constraints = {
		unit nullable: true
    }
	
	public String toString() {
		String description = "" + entity
		
		if( propertyGroup && propertyGroup != PROPERTY_GROUP_GENERIC ) 
			description += " " + propertyGroup 
		
		if( externalId )
			description += " (" + externalId + ")"
			
		description
	}
}
