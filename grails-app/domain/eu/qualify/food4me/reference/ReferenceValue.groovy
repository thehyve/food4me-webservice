package eu.qualify.food4me.reference

import java.util.List;

import eu.qualify.food4me.Property;
import eu.qualify.food4me.measurements.Status;

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
	public ReferenceCondition getSubjectCondition() {
		conditions.find { it.subject == subject }
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
	
	/**
	 * Counts the number of subjects for which the database contains a reference.
	 * @return
	 */
	public static int getSubjectCount() {
		def criteria = ReferenceValue.createCriteria()
		def referenceSubjectCount = criteria.list {
			projections {
				countDistinct("subject")
			}
		}
		
		referenceSubjectCount[0]
	}
}
