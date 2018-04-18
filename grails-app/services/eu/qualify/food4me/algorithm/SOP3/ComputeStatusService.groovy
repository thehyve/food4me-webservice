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
class ComputeStatusService implements StatusComputer {

	@Override
	MeasurementStatus computeStatus(Measurements measurements) {
		// Return if no values are given
		if (!measurements)
			return null

		// Check the status for all properties
		MeasurementStatus measurementStatus = new MeasurementStatus()

		measurementStatus.putAll measurements.all.findResults {
			getStatus(it, measurements)
		}.collectEntries {[(it.entity):it]}

		measurementStatus.putAll measurements.getAllPropertiesForPropertyGroup(Property.PROPERTY_GROUP_NUTRIENT).findResults {
			log.trace "Determine status for supplement intake for $it"
			determineStatusForSupplement(new ModifiedProperty(property: it, modifier: ModifiedProperty.Modifier.INTAKE_SUPPLEMENTS.id), measurements)
		}.collectEntries {[(it.entity):it]}

		return measurementStatus
	}
	
	Status getStatus( Measurement measurement, Measurements measurements ) {
		log.trace "Determine status for " + measurement

		// Lookup the reference property for this property
		def referenceProperty = measurement.property?.referenceProperty
		
		if (referenceProperty) {
			def status = determineStatusForProperty(measurement.property, referenceProperty, measurements)
			status?.value = measurement.value
			return status
		}

		return null
	}
	
	/**
	 * Determines the status for some referenceproperty
	 * @param valueProperty		Property to retrieve the value for
	 * @param referenceProperty	Property to determine the reference
	 * @param measurements		Set of measurements used as input
	 * @return
	 */
	protected static Status determineStatusForProperty(Measurable valueProperty, Property referenceProperty, Measurements measurements ) {
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
	 * @param measurements		Set of measurements used as input
	 * @return
	 */
	protected static Status determineStatusForSupplement(ModifiedProperty valueProperty, Measurements measurements) {
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
	
	
	protected static def generateWhereClause( List<Property> properties, Measurements measurements, int index = 0 ) {
		List<String> whereClause = []
		def whereParams = [:]
		
		properties.each { Property property ->
			MeasuredValue measuredValue = measurements.getValueFor( property )
			extendWhereClauses( whereClause, whereParams, property, measuredValue, index++ )
		}
		
		[ whereClause, whereParams ]
	}
	
	protected static void extendWhereClauses( List whereClause, Map whereParams, Property property, MeasuredValue measuredValue, int index = 0 ) {
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
