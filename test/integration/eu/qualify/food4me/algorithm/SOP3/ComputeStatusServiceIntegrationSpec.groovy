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
import eu.qualify.food4me.Unit
import eu.qualify.food4me.exampledata.IntegrationTestHelper
import eu.qualify.food4me.measurements.MeasuredNumericValue
import eu.qualify.food4me.measurements.MeasuredTextValue
import eu.qualify.food4me.measurements.Measurement
import eu.qualify.food4me.measurements.MeasurementStatus
import eu.qualify.food4me.measurements.Measurements
import eu.qualify.food4me.measurements.Status
import grails.test.spock.IntegrationSpec

class ComputeStatusServiceIntegrationSpec extends IntegrationSpec {
	def computeStatusService
	Measurements measurements
	
	// Setup is done in bootstrap
	def setup() {
		// Make sure the database is empty
		IntegrationTestHelper.cleanUp()
		IntegrationTestHelper.bootStrap()
	}

	def cleanup() {
	}

	void "test basic status computation"() {
		given:
			def protein = Property.findByEntity( "Protein" )
			def carbohydrate = Property.findByEntity( "Carbohydrate" )
			def age = Property.findByEntity( "Age" )
			def fibre = Property.findByEntity( "Fibre" )
			measurements = new Measurements()
			measurements.add( new Measurement( property: protein, value: new MeasuredNumericValue( value: 0.6, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: carbohydrate, value: new MeasuredNumericValue( value: 50, unit: Unit.findByCode( "% energy intake" ) ) ) )
			measurements.add( new Measurement( property: age, value: new MeasuredNumericValue( value: 35, unit: Unit.findByCode( "years" ) ) ) )
			
		when:
			MeasurementStatus statuses = computeStatusService.computeStatus( measurements )
		
		then:
			statuses
			statuses.getStatus( protein ).status == Status.STATUS_LOW
			statuses.getStatus( carbohydrate ).status == Status.STATUS_OK
			statuses.getStatus( age ).status == Status.STATUS_UNKNOWN // No reference values for age	
			!statuses.getStatus( fibre )
	}
	
	void "test all statuses"() {
		given:
			def protein = Property.findByEntity( "Protein" )
			MeasurementStatus statuses
		
		when:
			measurements = new Measurements()
			measurements.add( new Measurement( property: protein, value: new MeasuredNumericValue( value: 0.3, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			statuses = computeStatusService.computeStatus( measurements )
		
		then:
			statuses
			statuses.getStatus( protein ).status == Status.STATUS_VERY_LOW
			
		when:
			measurements = new Measurements()
			measurements.add( new Measurement( property: protein, value: new MeasuredNumericValue( value: 0.55, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			statuses = computeStatusService.computeStatus( measurements )
		
		then:
			statuses
			statuses.getStatus( protein ).status == Status.STATUS_LOW
			
		when:
			measurements = new Measurements()
			measurements.add( new Measurement( property: protein, value: new MeasuredNumericValue( value: 1, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			statuses = computeStatusService.computeStatus( measurements )
		
		then:
			statuses
			statuses.getStatus( protein ).status == Status.STATUS_OK

		when:
			measurements = new Measurements()
			measurements.add( new Measurement( property: protein, value: new MeasuredNumericValue( value: 10, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			statuses = computeStatusService.computeStatus( measurements )
		
		then:
			statuses
			statuses.getStatus( protein ).status == Status.STATUS_HIGH

	}
	
	void "test complex computation"() {
		given:
			def fibre = Property.findByEntity( "Fibre" )
			def age = Property.findByEntity( "Age" )
			def gender = Property.findByEntity( "Gender" )
			MeasurementStatus statuses
			
		when:
			measurements = new Measurements()
			measurements.add( new Measurement( property: fibre, value: new MeasuredNumericValue( value: 30, unit: fibre.unit ) ) )
			measurements.add( new Measurement( property: gender, value: new MeasuredTextValue( value: "Male" ) ) )
			measurements.add( new Measurement( property: age, value: new MeasuredNumericValue( value: 35, unit: age.unit ) ) )

			statuses = computeStatusService.computeStatus( measurements )
		
		then:
			statuses
			statuses.getStatus( fibre ).status == Status.STATUS_LOW
			
		when:
			measurements = new Measurements()
			measurements.add( new Measurement( property: fibre, value: new MeasuredNumericValue( value: 30, unit: fibre.unit ) ) )
			measurements.add( new Measurement( property: gender, value: new MeasuredTextValue( value: "Female" ) ) )
			measurements.add( new Measurement( property: age, value: new MeasuredNumericValue( value: 35, unit: age.unit ) ) )

			statuses = computeStatusService.computeStatus( measurements )
		
		then:
			statuses
			statuses.getStatus( fibre ).status == Status.STATUS_OK

		when:
			measurements = new Measurements()
			measurements.add( new Measurement( property: fibre, value: new MeasuredNumericValue( value: 30, unit: fibre.unit ) ) )
			measurements.add( new Measurement( property: gender, value: new MeasuredTextValue( value: "unknown" ) ) )
			measurements.add( new Measurement( property: age, value: new MeasuredNumericValue( value: 35, unit: age.unit ) ) )

			statuses = computeStatusService.computeStatus( measurements )
		
		then:
			statuses
			statuses.getStatus( fibre ).status == Status.STATUS_UNKNOWN
	}

	void "test status computation for modified properties"() {
		given:
			def protein = Property.findByEntity( "Protein" )
			def proteinFromFood = new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_DIETARY.id );
			def proteinFromMeat = new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_MEAT_FISH.id );
			def proteinSupplements = new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_SUPPLEMENTS.id );
			
			measurements = new Measurements()
			measurements.add( new Measurement( property: protein, value: new MeasuredNumericValue( value: 0.7, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: proteinFromMeat, value: new MeasuredNumericValue( value: 0.2, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: proteinFromFood, value: new MeasuredNumericValue( value: 0.4, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: proteinSupplements, value: new MeasuredNumericValue( value: 0.3, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			
		when:
			MeasurementStatus statuses = computeStatusService.computeStatus( measurements )
		
		then:
			statuses
			statuses.getStatus( protein ).status == Status.STATUS_OK
			statuses.getStatus( proteinFromFood ).status == Status.STATUS_VERY_LOW
			statuses.getStatus( proteinSupplements ).status == Status.STATUS_YES
			!statuses.getStatus( proteinFromMeat )
	}
	
	void "test boundary values"() {
		
	}
}
