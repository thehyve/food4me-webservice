package eu.qualify.food4me.reference

import eu.qualify.food4me.Property

class ReferenceCondition {
	/**
	 * Property this condition applies to 
	 */
	Property subject
	
	/**
	 * Lowest value for this property to result in this status
	 */
	BigDecimal low

	/**
	 * Highest value for this property to result in this status
	 */
	BigDecimal high
	
	/**
	 * String value that the property should have to result in this status
	 */
	String value
	
	static belongsTo = [referenceValue: ReferenceValue]
	
    static constraints = {
		low nullable: true
		high nullable: true
		value nullable: true
    }
	
	public String toString() {
		def conditions = []
		if( low )
			conditions << "> " + low
			
		if( high )
			conditions << "<= " + high
		
		if( value )
			conditions << "== " + value
			
		"Reference condition: " + subject + "( " + conditions.join( " / " ) + ")"
	}
}
