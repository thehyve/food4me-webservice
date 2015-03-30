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
package eu.qualify.food4me.measurements

import eu.qualify.food4me.Property
import grails.test.mixin.*
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@Mock(Property)
class StatusSpec extends Specification {

    def setup() {
		// Initialize properties needed
		def age = Property.findByEntity( "Age" ) ?: new Property(propertyGroup: Property.PROPERTY_GROUP_GENERIC, entity: "Age", externalId: "397669002")
			age.save(failOnError: true)
    }

    def cleanup() {
    }

    void "test as boolean"() {
		given:
			Status status
			
		when: "status is empty"
			status = new Status()
		
		then: "status should be false"
			assert !status
			
		when: "status is filled but unknown"
			status = new Status(entity: Property.findByEntity( "Age" ), value: new MeasuredNumericValue( value: 50 ), status: Status.STATUS_UNKNOWN, color: Status.Color.RED )
		
		then: "status should be false"
			assert !status

		when: "status is filled and not unknown"
			status = new Status(entity: Property.findByEntity( "Age" ), value: new MeasuredNumericValue( value: 50 ), status: Status.STATUS_OK, color: Status.Color.RED )
		
		then: "status should be true"
			assert status
			
		when: "status is empty but not unknown"
			status = new Status(status: Status.STATUS_OK)
		
		then: "status should be true"
			assert status

    }
}
