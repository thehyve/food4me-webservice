package eu.qualify.food4me.reference

import eu.qualify.food4me.Property


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
	 * Number of conditions applicable to this reference
	 * This is stored separately (and could be updated using triggers) to 
	 * prevent counting every time
	 */
	int numConditions = 0
	
	static hasMany = [ conditions: ReferenceCondition ]
	
    static constraints = {

    }
	
	/**
	 * Returns a list of properties needed to determine the status for a given property
	 * 
	 * For example, the status of BMI depends on the BMI itself, but also on Gender and Age
	 * @param property
	 * @return
	 */
	public static List getConditionProperties(Property p) {
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
}
