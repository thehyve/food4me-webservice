package eu.qualify.food4me.decisiontree

import eu.qualify.food4me.ModifiedProperty
import eu.qualify.food4me.Property
import eu.qualify.food4me.interfaces.Measurable
import groovy.transform.EqualsAndHashCode

/**
 * Reference value for a given property
 * @author robert
 */
@EqualsAndHashCode(includes="code")
class Advice {
	/**
	 * Unique code of the advice.
	 */
	String code
	
	/**
	 * Property that this advice is about. 
	 */
	Property subject

	/**
	 * Text of the advice
	 */
	String text
	
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
		text type: 'text'
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
}
