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
package eu.qualify.food4me.importer

import java.nio.charset.StandardCharsets

import eu.qualify.food4me.exampledata.IntegrationTestHelper
import grails.test.mixin.*
import grails.test.spock.IntegrationSpec

/**
 * Test for importService.
 * 
 * This is an integration test instead of a unit test because the import service make use of the 
 * CSV plugin, which doesn't add the toCsvReader methods in unit tests
 */
class ImportServiceIntegrationSpec extends IntegrationSpec {
	def importService
	
    def setup() {
		// Make sure the database is empty
		IntegrationTestHelper.cleanUp()
    }

    def cleanup() {
    }

	/**
	 * 
	 * @param datastructure 2-dimensional matrix with data
	 * @return
	 */
	protected InputStream getInputStreamFromImportDatastructure( datastructure ) {
		def separator = "\t"
		def lineSeparator = "\n"
		String text = datastructure.collect { it.join( separator ) }.join( lineSeparator )
		
		new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))
	}
}
