package eu.qualify.food4me

import eu.qualify.food4me.measurements.Status
import eu.qualify.food4me.reference.ReferenceCondition
import eu.qualify.food4me.reference.ReferenceValue

class HomeController {
	def importService
	
    def importData() { 
		importService.loadUnitsFromDirectory( )
		importService.loadPropertiesFromDirectory( )
		importService.loadReferencesFromDirectory( )
		render "Food4me reference data imported"
	}

	def importAll() { 
		importService.loadAll()
		render "Food4me all data imported"
	}

	def importDecisionTrees() {
		importService.batchSize = 50
		def start = System.currentTimeMillis()
		importService.loadDecisionTreesFromDirectory()
		log.info "Importing decision trees took " + ( ( System.currentTimeMillis() - start ) / 1000 ) + " seconds"
		render "Food4me decision trees imported"
	}
	 
}
