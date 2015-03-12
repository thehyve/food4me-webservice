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
