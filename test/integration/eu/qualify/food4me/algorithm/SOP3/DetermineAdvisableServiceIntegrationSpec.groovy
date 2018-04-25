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

import eu.qualify.food4me.Property
import eu.qualify.food4me.exampledata.IntegrationTestHelper;
import eu.qualify.food4me.interfaces.Advisable
import eu.qualify.food4me.measurements.MeasurementStatus
import eu.qualify.food4me.measurements.Measurements
import eu.qualify.food4me.measurements.Status
import grails.test.spock.IntegrationSpec

class DetermineAdvisableServiceIntegrationSpec extends IntegrationSpec {
	def determineAdvisableService
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
			List<Advisable> advisables = determineAdvisableService.determineAdvisables( measurementStatus, new Measurements() )
		
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
			List<Advisable> advisables = determineAdvisableService.determineAdvisables( measurementStatus, new Measurements() )
		
		then:
			advisables
			advisables.size() == 1
			advisables.contains(protein)
	}
	
	
	void "test determination without measurements"() {
		given: "no measurements"
			measurementStatus = new MeasurementStatus()
			
		when: "the advisables are determined"
			List<Advisable> advisables = determineAdvisableService.determineAdvisables( measurementStatus, new Measurements() )
		
		then: "nothing is returned"
			!advisables
	}
	
	void "test priority within a single group"() {
		given: "carbohydrate to have the most severe status (very-high)"
			def protein = Property.findByEntity( "Protein" )
			def carbohydrate = Property.findByEntity( "Carbohydrate" )
			
			measurementStatus = new MeasurementStatus()
			measurementStatus.putStatus( protein, new Status( entity: protein, status: Status.STATUS_HIGH, color: Status.Color.AMBER ) )
			measurementStatus.putStatus( carbohydrate, new Status( entity: carbohydrate, status: Status.STATUS_VERY_HIGH, color: Status.Color.RED ) )
			
		when: "the advisables are determined"
			List<Advisable> advisables = determineAdvisableService.determineAdvisables( measurementStatus, new Measurements() )
		
		then: "carbohydrate is selected"
			advisables
			advisables.size() == 1
			advisables.contains(carbohydrate)
	}
	
	void "test same priority within a single group"() {
		given: "two properties with the same status in a single group"
			def protein = Property.findByEntity( "Protein" )
			def carbohydrate = Property.findByEntity( "Carbohydrate" )
			
			measurementStatus = new MeasurementStatus()
			measurementStatus.putStatus( protein, new Status( entity: protein, status: Status.STATUS_HIGH, color: Status.Color.AMBER ) )
			measurementStatus.putStatus( carbohydrate, new Status( entity: carbohydrate, status: Status.STATUS_HIGH, color: Status.Color.AMBER ) )
			
		when: "the advisables are determined"
			List<Advisable> advisables = determineAdvisableService.determineAdvisables( measurementStatus, new Measurements() )
		
		then: "protein is selected as it has a higher rank within the group"
			advisables
			advisables.size() == 1
			advisables.contains(protein)
	}

	
	void "test replacement with gene risk replacement"() {
		given: "a non-gene-risk property protein with highest priority, and a gene-risk property folate with a lower priority"
			def protein = Property.findByEntity( "Protein" )
			def carbohydrate = Property.findByEntity( "Carbohydrate" )
			def folate = Property.findByEntity( "Folate" )
			
			measurementStatus = new MeasurementStatus()
			measurementStatus.putStatus( protein, new Status( entity: protein, status: Status.STATUS_VERY_HIGH, color: Status.Color.RED ) )
			measurementStatus.putStatus( carbohydrate, new Status( entity: carbohydrate, status: Status.STATUS_HIGH, color: Status.Color.AMBER ) )
			measurementStatus.putStatus( folate, new Status( entity: folate, status: Status.STATUS_HIGH, color: Status.Color.AMBER ) )
			
		when: "the advisables are determined"
			List<Advisable> advisables = determineAdvisableService.determineAdvisables( measurementStatus, new Measurements() )
		
		then: "folate is selected due to its gene-risk nature in addition to the protein being selected due to its status"
			advisables
			advisables.size() == 2
			advisables.contains(folate)
			advisables.contains(protein)
	}

}
