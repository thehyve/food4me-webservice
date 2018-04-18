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
import eu.qualify.food4me.exampledata.IntegrationTestHelper
import eu.qualify.food4me.interfaces.Advisable
import eu.qualify.food4me.measurements.MeasurementStatus
import eu.qualify.food4me.measurements.Measurements
import eu.qualify.food4me.measurements.Status
import grails.test.spock.IntegrationSpec

class AllLowOrHighAdvisableServiceIntegrationSpec extends IntegrationSpec {
	def allLowOrHighAdvisableService
	Measurements measurements
	MeasurementStatus measurementStatus
	
	// Setup is done in bootstrap
	def setup() {
		// Make sure the database is empty
		IntegrationTestHelper.cleanUp()
		IntegrationTestHelper.bootStrap()
	}

	def cleanup() {
	}

	void "test basic determination"() {
		given:
			def protein = Property.findByEntity( "Protein" )
			def carbohydrate = Property.findByEntity( "Carbohydrate" )
			def vitaminA = Property.findByEntity( "Vitamin A" )
			def cholesterol = Property.findByEntity( "Cholesterol" )
			
			measurementStatus = new MeasurementStatus()
			measurementStatus.putStatus( protein, new Status( entity: protein, status: Status.STATUS_OK, color: Status.Color.GREEN ) )
			measurementStatus.putStatus( carbohydrate, new Status( entity: carbohydrate, status: Status.STATUS_HIGH, color: Status.Color.AMBER ) )
			measurementStatus.putStatus( vitaminA, new Status( entity: vitaminA, status: Status.STATUS_VERY_HIGH, color: Status.Color.RED ) )
			measurementStatus.putStatus( cholesterol, new Status( entity: cholesterol, status: Status.STATUS_LOW, color: Status.Color.AMBER ) )
			
		when: "the advisables are determined"
			List<Advisable> advisables = allLowOrHighAdvisableService.determineAdvisables( measurementStatus, new Measurements() )
		
		then:
			advisables
			advisables.size() == 3
			advisables.contains(carbohydrate)	// As protein has status OK, it is not taken into account
			advisables.contains(vitaminA)
			advisables.contains(cholesterol)
	}
	
	void "test with little nutrients"() {
		given:
			def protein = Property.findByEntity( "Protein" )
			def cholesterol = Property.findByEntity( "Cholesterol" )
			
			measurementStatus = new MeasurementStatus()
			measurementStatus.putStatus( protein, new Status( entity: protein, status: Status.STATUS_VERY_LOW, color: Status.Color.RED ) )
			measurementStatus.putStatus( cholesterol, new Status( entity: cholesterol, status: Status.STATUS_OK, color: Status.Color.GREEN ) )
			
		when: "the advisables are determined"
			List<Advisable> advisables = allLowOrHighAdvisableService.determineAdvisables( measurementStatus, new Measurements() )
		
		then:
			advisables
			advisables.size() == 1
			advisables.contains(protein)
	}
	
	
	void "test determination without measurements"() {
		given: "no measurements"
			measurementStatus = new MeasurementStatus()
			
		when: "the advisables are determined"
			List<Advisable> advisables = allLowOrHighAdvisableService.determineAdvisables( measurementStatus, new Measurements() )
		
		then: "nothing is returned"
			!advisables
	}
	
	void "test different statuses"() {
		given: "multiple measurements with different statuses"
			def protein = Property.findByEntity( "Protein" )
			def carbohydrate = Property.findByEntity( "Carbohydrate" )
			def fibre = Property.findByEntity( "Fibre" )
			def folate = Property.findByEntity( "Folate" )
			def cholesterol = Property.findByEntity( "Cholesterol" )
			
			def age = Property.findByEntity( "Age" )
			def geneFADS1 = Property.findByEntity("FADS1")
			def geneFTO = Property.findByEntity("FTO")
			
			measurementStatus = new MeasurementStatus()
			measurementStatus.putStatus( protein, new Status( entity: protein, status: Status.STATUS_HIGH, color: Status.Color.AMBER ) )
			measurementStatus.putStatus( carbohydrate, new Status( entity: carbohydrate, status: Status.STATUS_VERY_HIGH, color: Status.Color.RED ) )
			measurementStatus.putStatus( fibre, new Status( entity: fibre, status: Status.STATUS_VERY_LOW, color: Status.Color.RED ) )
			measurementStatus.putStatus( folate, new Status( entity: folate, status: Status.STATUS_LOW, color: Status.Color.GREEN ) )
			measurementStatus.putStatus( cholesterol, new Status( entity: cholesterol, status: Status.STATUS_OK, color: Status.Color.RED ) )
			measurementStatus.putStatus( age, new Status( entity: age, status: Status.STATUS_UNKNOWN, color: Status.Color.RED ) )
			
			measurementStatus.putStatus( geneFADS1, new Status( entity: geneFADS1, status: Status.STATUS_RISK, color: Status.Color.RED ) )
			measurementStatus.putStatus( geneFTO, new Status( entity: geneFTO, status: Status.STATUS_NON_RISK, color: Status.Color.RED ) )
			
		when: "the advisables are determined"
			List<Advisable> advisables = allLowOrHighAdvisableService.determineAdvisables( measurementStatus, new Measurements() )
		
		then: "carbohydrate is selected"
			advisables
			advisables.size() == 4
			advisables.contains(carbohydrate)
			advisables.contains(protein)
			advisables.contains(fibre)
			advisables.contains(folate)
	}
	
	void "test modified properties"() {
		given: "multiple statuses, including some statuses for modified properties"
			def protein = Property.findByEntity( "Protein" )
			def carbohydrate = Property.findByEntity( "Carbohydrate" )
			def folate = Property.findByEntity( "Folate" )
			
			def modifiedProtein = new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_DIETARY.id )
			def modifiedCarbohydrate = new ModifiedProperty( property: carbohydrate, modifier: ModifiedProperty.Modifier.INTAKE_SUPPLEMENTS.id )
			def modifiedFolate = new ModifiedProperty( property: folate, modifier: ModifiedProperty.Modifier.INTAKE_FATS_SPREADS.id )
			
			measurementStatus = new MeasurementStatus()
			measurementStatus.putStatus( protein, new Status( entity: protein, status: Status.STATUS_HIGH, color: Status.Color.AMBER ) )
			measurementStatus.putStatus( carbohydrate, new Status( entity: carbohydrate, status: Status.STATUS_VERY_HIGH, color: Status.Color.RED ) )
			measurementStatus.putStatus( folate, new Status( entity: folate, status: Status.STATUS_OK, color: Status.Color.GREEN ) )

			measurementStatus.putStatus( modifiedProtein, new Status( entity: modifiedProtein, status: Status.STATUS_HIGH, color: Status.Color.AMBER ) )
			measurementStatus.putStatus( modifiedCarbohydrate, new Status( entity: modifiedCarbohydrate, status: Status.STATUS_VERY_HIGH, color: Status.Color.RED ) )
			measurementStatus.putStatus( modifiedFolate, new Status( entity: modifiedFolate, status: Status.STATUS_HIGH, color: Status.Color.GREEN ) )

		when: "the advisables are determined"
			List<Advisable> advisables = allLowOrHighAdvisableService.determineAdvisables( measurementStatus, new Measurements() )
		
		then: "only normal properties are selected"
			advisables
			advisables.size() == 2
			advisables.contains(carbohydrate)
			advisables.contains(protein)
	}
}
