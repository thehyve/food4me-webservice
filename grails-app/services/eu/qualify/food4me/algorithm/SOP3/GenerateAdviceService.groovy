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
import eu.qualify.food4me.decisiontree.Advice
import eu.qualify.food4me.interfaces.AdviceGenerator
import eu.qualify.food4me.interfaces.Advisable
import eu.qualify.food4me.interfaces.Measurable
import eu.qualify.food4me.measurements.MeasuredTextValue
import eu.qualify.food4me.measurements.MeasuredValue
import eu.qualify.food4me.measurements.MeasurementStatus
import eu.qualify.food4me.measurements.Measurements
import eu.qualify.food4me.measurements.Status
import grails.transaction.Transactional

@Transactional
class GenerateAdviceService implements AdviceGenerator {

	@Override
	List<Advice> generateAdvice(Measurements measurements,
								MeasurementStatus measurementStatus, List<Advisable> advisables) {
		
		advisables.collectMany { advisable ->
			log.info "Generating advice for " + advisable
			generateAdviceFor( advisable, measurements, measurementStatus )
		}
	}

	/**
	 * Generates a list of advices for a given property, based on the measurements	
	 * @param advisable
	 * @param measurements
	 * @param measurementStatus
	 * @return
	 */
	List<Advice> generateAdviceFor( Advisable advisable, Measurements measurements,
		MeasurementStatus measurementStatus) {

		List<Advice> advices = []
		
		// First determine the conditions applicable for the given property
		// Most probably that includes the property value itself, as well as other
		// related properties
		List<Measurable> properties = Advice.getConditionProperties(advisable as Property)
		
		// If no properties are found, no advices are known for this property. Returning immediately
		if( !properties ) {
			log.warn "No advices are known for ${advisable}. Please check the database"
			return advices
		}
		
		log.trace "  The following properties are needed to determine an advice for " + advisable + ": " + properties
			
		// Create a query that includes all values and retrieve the id and status
		def hql = "SELECT advice.id FROM Advice as advice INNER JOIN advice.conditions as condition"
		hql += " WHERE advice.subject = :referenceProperty "
		
		def (whereClause, hqlParams) = generateWhereClause( properties, measurementStatus, measurements )
		if( whereClause ) {
			hql += " AND ( " + whereClause.join( " OR " ) + " )"
		}
			
		hql += " GROUP BY advice.id HAVING COUNT(*) = advice.numConditions"
		
		hqlParams[ "referenceProperty" ] = advisable
		
		def adviceIds = Advice.executeQuery( hql, hqlParams )
		
		if( adviceIds.size() == 0 ) {
			log.warn "No advices can be determined for ${advisable}. Retrieval parameters are " + hqlParams
			return advices
		}
		
		log.trace "" + adviceIds?.size() + " advices have been returned for property " + advisable
		
		// Return the advices 
		adviceIds.collect { Advice.get(it) }
	}
	
	protected static def generateWhereClause(List<Measurable> properties, MeasurementStatus measurementStatus, Measurements measurements ) {
		List<String> whereClause = []
		def whereParams = [:]
		int index = 0;
		
		properties.each { Measurable property ->
			MeasuredValue measuredValue = measurements.getValueFor( property )
			Status status = measurementStatus.getStatus( property )
			
			log.trace "Adding advice where clause for " + property + ": " + measuredValue + " / " + status
			
			// If no value is measured and no status is found, we cannot
			// filter on this property. Skipping immediately
			if( !measuredValue && !status ) {
				log.warn "No value provided for " + property + ", which may be needed to generate this advice"
				return
			}
			
			// Modified property are handled differently from default properties
			String condition = "( "
			if( property instanceof ModifiedProperty ) {
				condition += " condition.subject = :property" + index + " AND condition.modifier = :modifier" + index + " "
				whereParams[ "property" + index ] = property.property
				whereParams[ "modifier" + index ] = property.modifier
			} else {
				condition += " condition.subject = :property" + index + " AND condition.modifier IS NULL "
				whereParams[ "property" + index ] = property
			}
			
			condition += " AND "
			
			// Check if the status is the right one. Only do so if the status is
			// determined and the status is known
			if( status && status.status != Status.STATUS_UNKNOWN ) {
				condition += "( condition.status IS NULL OR condition.status = :status" + index + " )"
				whereParams[ "status" + index ] = status.status
			} else { 
				condition += "condition.status IS NULL"
			}
			
			condition += " AND "
			
			// Check if the value is correct. That can only be done for text values
			if( measuredValue instanceof MeasuredTextValue ) {
				condition += "( condition.value IS NULL OR lower(condition.value) = lower(:value" + index + ") )"
				whereParams[ "value" + index ] = measuredValue.value
			} else {
				condition += "condition.value IS NULL"
			}
			 
			whereClause << condition + " )"
			
			index++
		}
		
		[ whereClause, whereParams ]
	}
}
