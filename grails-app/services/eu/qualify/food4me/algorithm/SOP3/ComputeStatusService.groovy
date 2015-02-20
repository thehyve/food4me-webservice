package eu.qualify.food4me.algorithm.SOP3

import eu.qualify.food4me.Property
import eu.qualify.food4me.interfaces.StatusComputer
import eu.qualify.food4me.measurements.MeasuredNumericValue
import eu.qualify.food4me.measurements.MeasuredValue
import eu.qualify.food4me.measurements.Measurement
import eu.qualify.food4me.measurements.MeasurementStatus
import eu.qualify.food4me.measurements.Measurements
import eu.qualify.food4me.measurements.NutrientIntake
import eu.qualify.food4me.measurements.Status
import eu.qualify.food4me.reference.ReferenceValue
import grails.transaction.Transactional

@Transactional
class ComputeStatusService implements StatusComputer {

	@Override
	public MeasurementStatus computeStatus(Measurements measurements) {
		// Return if no values are given
		if( !measurements )
			return null;
	
		MeasurementStatus measurementStatus = new MeasurementStatus()
			
		// Check the status for all nutrients
		measurements.all.each { measurement ->
			Status status = getStatus( measurement, measurements )
			measurementStatus.addStatus( status.entity, status )
		}
			
		return measurementStatus;
	}
	
	public Status getStatus( NutrientIntake nutrientIntake, Measurements measurements ) {
		MeasuredValue total = nutrientIntake.total
		new Status( entity: nutrientIntake.property, value: total, getStatusText( measurement.property, measurements ) ) 
	}

	public Status getStatus( Measurement measurement, Measurements measurements ) {
		MeasuredValue value = measurement.value
		new Status( entity: measurement.property, value: value, status: getStatusText( measurement.property, measurements ) )
	}
	
	protected String getStatusText( Property property, Measurements measurements ) {
		// First determine the conditions applicable for the given property
		// Most probably that includes the property value itself, but it could 
		// be dependent on age or gender as well
		def properties = ReferenceValue.getConditionProperties( property )
		
		// If no properties are found, no reference values are known. Returning immediately
		if( !properties ) {
			log.warn "No references apply for any value of ${property}. Please check the database"
			println "No references apply for any value of ${property}. Please check the database"
			return Status.STATUS_UNKNOWN
		}
		
		// Create a query that includes all values and retrieve the id and status 
		def hql = "SELECT reference.id, reference.status FROM ReferenceValue as reference INNER JOIN reference.conditions as condition"
		hql += " WHERE reference.subject = :referenceProperty "
		
		def (whereClause, hqlParams) = generateWhereClause( properties, measurements )
		if( whereClause ) {
			hql += " AND ( " + whereClause.join( " OR " ) + " )"
		}
			
		hql += " GROUP BY reference.id, reference.status HAVING COUNT(*) = reference.numConditions"
		
		hqlParams[ "referenceProperty" ] = property
		
		println "Executing HQL: " + hql
		
		def statuses = ReferenceValue.executeQuery( hql, hqlParams )
		
		if( statuses.size() == 0 ) {
			log.warn "No references apply for ${property}. Retrieval parameters are " + hqlParams
			println "No references apply for ${property}. Retrieval parameters are " + hqlParams
			return Status.STATUS_UNKNOWN
		}
			
		if( statuses.size() > 1 ) { 
			log.warn "Multiple references apply for ${property}. Retrieval parameters are " + hqlParams
			println "Multiple references apply for ${property}. Retrieval parameters are " + hqlParams
			
		}
		
		// Return the first status found
		statuses[0][1]
	}
	
	protected def generateWhereClause( List<Property> properties, Measurements measurements ) {
		List<String> whereClause = []
		def whereParams = [:]
		int index = 0;
		
		properties.each { Property property ->
			MeasuredValue measuredValue = measurements.getValueFor( property )
			
			String condition = " ( condition.subject = :property" + index + " AND "
			
			// There is a difference between text and numeric values
			if( measuredValue instanceof MeasuredNumericValue )
				condition += "( ( low IS NULL or low < :value" + index + " ) AND ( high IS NULL OR high >= :value" + index + " ) )"
			 else
			 	condition += "( value IS NULL or value = :value" + index + " )"
			 
			whereClause << condition + " )"
			
			whereParams[ "property" + index ] = property
			whereParams[ "value" + index ] = measuredValue.value  
			
			index++
		}
		
		[ whereClause, whereParams ]
	}
}
