import eu.qualify.food4me.exampledata.ExampleData
import grails.util.Environment

class BootStrap {

    def init = { servletContext ->
		if( Environment.getCurrent().name == "dev" ) {
			println "Bootstrapping environment " + Environment.getCurrent().name
			ExampleData.initializeGenericData()
			ExampleData.initializeReferences()
			ExampleData.initializeAdvices()
		}
    }
		
	def destroy = {
    }
}
