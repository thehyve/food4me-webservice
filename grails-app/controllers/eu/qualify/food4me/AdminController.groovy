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
package eu.qualify.food4me

import eu.qualify.food4me.decisiontree.Advice
import eu.qualify.food4me.decisiontree.AdviceCondition
import eu.qualify.food4me.decisiontree.AdviceText
import eu.qualify.food4me.exampledata.ExampleData
import eu.qualify.food4me.reference.ReferenceCondition
import eu.qualify.food4me.reference.ReferenceValue
import grails.plugin.springsecurity.annotation.Secured

@Secured(['ROLE_ADMIN'])
class AdminController {
	static allowedMethods = [
		clearAll: [ 'POST', 'DELETE' ],
		importAll: 'POST',
		importReferenceData: 'POST',
		importDecisionTrees: 'POST',
		importTranslations: 'POST'
	]
	
	def importService
	def grailsApplication
	
	def index() {
		[ 
			importDirectory: importService.defaultImportDirectory,
			appName: grailsApplication.metadata['app.name'],
			appVersion: grailsApplication.metadata['app.version'],
			
			numProperties: Property.count,
			numUnits: Unit.count,
			
			numReferences: ReferenceValue.count,
			numReferenceSubjects: ReferenceValue.subjectCount,
			
			numAdvices: Advice.count,
			numAdviceSubjects: Advice.subjectCount,
			
			languages: AdviceText.languages
		]
	}
	
	def consistencyCheck() {
		// Do we have all translations for available languages
		def advices = Advice.list()
		
		def missingCodes = [:]
		AdviceText.getLanguages().each { language ->
			// Determine the existing codes for this language
			def languageCodes = AdviceText.createCriteria().listDistinct() {
				eq( 'language', language )
				projections {
					distinct 'code'
				}
			}
			
			// List the missing advice codes
			missingCodes[ language ] = advices.findAll { !languageCodes.contains( it.code ) } 
		}
		
		
		// Do we have references for all decision tree arguments
		def statusAdviceConditions = AdviceCondition.findAllByStatusIsNotNull()
		def referenceProperties = ReferenceValue.createCriteria().listDistinct() {
			projections {
				distinct 'subject'
			}
		}
		
		def missingReferences = statusAdviceConditions.findAll { !referenceProperties.contains( it.subject ) }
		
		[ missingCodes: missingCodes, missingReferences: missingReferences]
	}
	
    def clearAll() {
		ReferenceCondition.executeUpdate( "DELETE FROM ReferenceCondition")
		ReferenceValue.executeUpdate( "DELETE FROM ReferenceValue")
		
		AdviceText.executeUpdate( "DELETE FROM AdviceText")
		AdviceCondition.executeUpdate( "DELETE FROM AdviceCondition")
		Advice.executeUpdate( "DELETE FROM Advice")
		
		Property.executeUpdate( "DELETE FROM Property")
		Unit.executeUpdate( "DELETE FROM Unit")
		
		flash.message = "Cleared all database tables. Import your data again."
		redirect action: "index"
	}
	
	def importAll() { 
		importService.loadAll()
		
		flash.message = "All data is imported from the default directory"
		redirect action: "index"
	}
	
	def importReferenceData() {
		importService.loadUnitsFromDirectory( )
		importService.loadPropertiesFromDirectory( )
		importService.loadReferencesFromDirectory( )
		
		flash.message = "Reference data (units, properties and references) are imported from the default directory"
		redirect action: "index"
	}

	def importDecisionTrees() {
		importService.batchSize = 50
		importService.loadDecisionTreesFromDirectory()
		
		flash.message = "Decision trees have been imported from the default directory"
		redirect action: "index"
	}
	
	def importTranslations() {
		importService.loadAdviceTextsFromDirectory( )
		flash.message = "Advice text translations have been imported from the default directory"
		redirect action: "index"
	}
	
	
	def importExampleData() {
		// Add example data to the database
		ExampleData.initializeGenericData()
		log.info "Generic example data was loaded"
		
		ExampleData.initializeReferences()
		log.info "Example reference data was loaded"
		
		ExampleData.initializeAdvices()
		log.info "Example advice data was loaded"
		
		flash.message = "Example data has been loaded into the database"
		redirect action: "index"
	}
	 
}
