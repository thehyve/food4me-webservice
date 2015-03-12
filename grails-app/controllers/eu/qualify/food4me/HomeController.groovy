package eu.qualify.food4me

import eu.qualify.food4me.measurements.Status
import eu.qualify.food4me.reference.ReferenceCondition
import eu.qualify.food4me.reference.ReferenceValue

class HomeController {
	def importService
	
    def importData() { 
		importService.loadAll()
		render "Food4me imported"
	}
	
	 
}
