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
package eu.qualify.food4me.algorithm.SOP3

import eu.qualify.food4me.ModifiedProperty
import eu.qualify.food4me.Property
import eu.qualify.food4me.interfaces.Measurable
import eu.qualify.food4me.interfaces.StatusComputer
import eu.qualify.food4me.measurements.MeasuredNumericValue
import eu.qualify.food4me.measurements.MeasuredValue
import eu.qualify.food4me.measurements.Measurement
import eu.qualify.food4me.measurements.MeasurementStatus
import eu.qualify.food4me.measurements.Measurements
import eu.qualify.food4me.measurements.Status
import eu.qualify.food4me.reference.ReferenceValue
import grails.transaction.Transactional

@Transactional
class ReferenceService {

	/**
	 * Returns a list of references for the given entities. 
	 * 
	 * If specified, the age and gender from the measurements are used
	 * @param entities
	 * @param measurements
	 * @return
	 */
	public Map<Measurable, List<ReferenceValue>> getReferences( List<Measurable> entities, Measurements measurements ) {
		def age = Property.findByEntity( 'Age' )
		def gender = Property.findByEntity( 'Gender' )
		
		if( !age || !gender ) {
			log.error "No age or gender is present in the database, but they are required to retrieve references"
			return null
		}
		
		// Create a new measurements map, with only gender and age, if they exist
		def filteredMeasurements = new Measurements()
		filteredMeasurements.add(measurements.get( age ) )
		filteredMeasurements.add(measurements.get( gender ) )
		
		Map references = [:]
		
		// Now for all entities, retrieve the references
		entities.each { measurable ->
			log.info "Retrieving reference for property " + measurable
			
			// First determine whether we already retrieved references for this property
			// That could happen if both a property and a modified property are given
			if( references.containsKey( measurable ) ) {
				log.info "Reference for property " + measurable + " was already retrieved. Skipping for now."
				return
			}
			
			// We only have references for normal (non-modified properties)
			// However, for dietary intake, we use the same references as for the property itself
			def referenceProperty = measurable.referenceProperty
			if( !referenceProperty ) {
				log.info "We only have references for non-modified properties. There is no reference for " + measurable
				return
			}
				
			// Now determine the conditions applicable for the given property
			// Most probably that includes the property value itself, but it could
			// be dependent on age or gender as well
			def properties = ReferenceValue.getConditionProperties( referenceProperty )
			
			// If no properties are found, no reference values are known. Returning immediately
			if( !properties ) {
				log.warn "No references apply for any value of ${referenceProperty}, although the client asked for it. This may indicate missing information in the database."
				return
			}
			
			// Create a query that includes all values but and retrieve the id and status
			def hql = "SELECT reference FROM ReferenceValue as reference INNER JOIN reference.conditions as condition"
			hql += " WHERE reference.subject = :referenceProperty "
			
			// Determine the whereclause for all properties, except for the property itself. That way
			// we only retrieve the references given the age and gender. However, if later on the references
			// will be dependent on other properties as well, these are easily included
			// because the value for this property could be determined by another property (e.g. a ModifiedProperty)
			def (whereClause, hqlParams) = generateWhereClause( properties - referenceProperty, measurements )
			if( whereClause ) {
				hql += " AND ( " + whereClause.join( " OR " ) + " )"
			}
			
			if( properties.size() > 1 ) {
				// If more than one property is needed to find the reference, they must be included.
				// For that reason, we should check whether the number of conditions matches all but one
				hql += " GROUP BY reference HAVING COUNT(*) = ( reference.numConditions - 1 )"
			} else { 
				// Otherwise, filtering is only done on the subject condition itself. That means that
				// all references for this property should be returned
				hql += " GROUP BY reference"
			}
			
			hqlParams[ "referenceProperty" ] = referenceProperty
			
			log.trace "Retrieving reference with HQL: " + hql + " / " + hqlParams
			
			// Retrieve the reference values
			references[measurable] = ReferenceValue.executeQuery( hql, hqlParams )?.sort { a, b ->
				// Order the references based on their subject condition.
				// We want it in the logical order from low to high. Hoever, if low = null it should be first
				// but if high = null it should be last.
				def aCondition = a.subjectCondition
				def bCondition = b.subjectCondition
				
				if( !aCondition && !b ) {
					return 0
				}
				
				if( !aCondition )
					return -1
				
				if( !bCondition )
					return 1
					
				
				// If the values for low are equal (probably both NULL), check the high velus
				if( aCondition.low == bCondition.low ) {
					// If one of the high values is NULL, put that one last
					if( aCondition.high == null )
						return 1
					
					if( bCondition.high == null )
						return -1
						
					return aCondition.high <=> bCondition.high
				} else {
					// If the lows are not equal, but one of them is NULL, put that one first
					if( aCondition.low == null )
						return -1
					
					if( bCondition.low == null )
						return 1
						
					return aCondition.low <=> bCondition.low
				}
			}
		} 
		
		references
	}
	
	/**
	 * Determines the status for some referenceproperty
	 * @param valueProperty		Property to retrieve the value for
	 * @param referenceProperty	Property to determine the reference
	 * @param measurements		Set of measurements used as input
	 * @return
	 */
	protected Status determineStatusForProperty( Measurable valueProperty, Property referenceProperty, Measurements measurements ) {
		def status = new Status( entity: valueProperty )

		// First determine the conditions applicable for the given property
		// Most probably that includes the property value itself, but it could
		// be dependent on age or gender as well
		def properties = ReferenceValue.getConditionProperties( referenceProperty )
		
		// If no properties are found, no reference values are known. Returning immediately
		if( !properties ) {
			log.warn "No references apply for any value of ${referenceProperty}. Please check the database"
			status.status = Status.STATUS_UNKNOWN
			return status
		}
		
		// Create a query that includes all values and retrieve the id and status
		def hql = "SELECT reference.id, reference.status, reference.color FROM ReferenceValue as reference INNER JOIN reference.conditions as condition"
		hql += " WHERE reference.subject = :referenceProperty "
		
		// First determine the whereclause for all properties, except for the referenceproperty
		// because the value for this property could be determined by another property (e.g. a ModifiedProperty)
		def (whereClause, hqlParams) = generateWhereClause( properties - referenceProperty, measurements )
		extendWhereClauses( whereClause, hqlParams, referenceProperty, measurements.getValueFor( valueProperty ), whereClause.size() + 1 )
		
		if( whereClause ) {
			hql += " AND ( " + whereClause.join( " OR " ) + " )"
		}
			
		hql += " GROUP BY reference.id, reference.status, reference.color HAVING COUNT(*) = reference.numConditions"
		
		hqlParams[ "referenceProperty" ] = referenceProperty
		
		def statuses = ReferenceValue.executeQuery( hql, hqlParams )
		
		if( statuses.size() == 0 ) {
			log.warn "No references apply for ${referenceProperty}. Retrieval parameters are " + hqlParams
			status.status = Status.STATUS_UNKNOWN
			return status
		}
			
		if( statuses.size() > 1 ) {
			log.warn "Multiple references apply for ${referenceProperty}. Retrieval parameters are " + hqlParams
			
			if( log.traceEnabled ) {
				log.trace "  HQL: " + hql
				log.trace "  params: " + hqlParams
				statuses.each {
					log.trace "  Reference: " + it 
				}
			}
		}
		
		// Return the first status found
		status.status = statuses[0][1]
		status.color = statuses[0][2]
		return status
	}

	/**
	 * Determines the status for some supplement intake value
	 * @param valueProperty		Property to retrieve the value for
	 * @param referenceProperty	Property to determine the reference
	 * @param measurements		Set of measurements used as input
	 * @return
	 */
	protected Status determineStatusForSupplement( ModifiedProperty valueProperty, Property referenceProperty, Measurements measurements ) {
		def status = new Status( entity: valueProperty )

		// A very simple check: yes or no
		def value = measurements.getValueFor( valueProperty )
		status.value = value
		
		if( value && value.type == "numeric" && value.value > 0 ) {
			status.status = Status.STATUS_YES
			status.color = Status.Color.GREEN
		} else {
			status.status = Status.STATUS_NO
			status.color = Status.Color.RED
		}

		return status
	}
	
	
	protected def generateWhereClause( List<Property> properties, Measurements measurements, int index = 0 ) {
		List<String> whereClause = []
		def whereParams = [:]
		
		properties.each { Property property ->
			MeasuredValue measuredValue = measurements.getValueFor( property )
			extendWhereClauses( whereClause, whereParams, property, measuredValue, index++ )
		}
		
		[ whereClause, whereParams ]
	}
	
	protected void extendWhereClauses( List whereClause, Map whereParams, Property property, MeasuredValue measuredValue, int index = 0 ) {
		// If no value is provided, we cannot filter on this property. That
		// may result in no status being determined for this property. Skipping immediately
		if( !measuredValue ) {
			log.warn "No value provided for " + property + ", which may be needed to determine the status"
			return
		}
		
		String condition = " ( condition.subject = :property" + index + " AND "
		
		// There is a difference between text and numeric values
		if( measuredValue instanceof MeasuredNumericValue )
			condition += "( condition_type = 'numeric' AND ( low IS NULL or low < :value" + index + " ) AND ( high IS NULL OR high >= :value" + index + " ) )"
		 else
			condition += "( condition_type = 'text' AND ( value IS NULL or lower(value) = lower(:value" + index + ") ) )"

		whereClause << condition + " )"
		
		whereParams[ "property" + index ] = property
		whereParams[ "value" + index ] = measuredValue.value
	}

}
