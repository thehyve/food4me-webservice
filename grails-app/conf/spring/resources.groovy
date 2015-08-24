// Place your Spring DSL code here
beans = {
	// Use aliases to configure injected dependencies
	// See http://mrhaki.blogspot.nl/2013/03/grails-goodness-using-spring-bean.html

	// StatusComputer implementation: how to determine the status, based on the input values
	springConfig.addAlias 'statusComputer', 'computeStatusService'

	// AdvisableDeterminer implementation: How to decide on which variables to generate an advice
	springConfig.addAlias 'advisableDeterminer', 'allParametersAdvisableService'

	// AdviceGenerator implementation: Generate advices for the given variables, based on the measurements and their status
	springConfig.addAlias 'adviceGenerator', 'generateAdviceService'

	// Parser implementation: how to interpret the parameters and input data that is being sent
	springConfig.addAlias 'parser', 'parameterBasedParseService'

	// Serializer implementation: how to return the data when the client asks for a JSON representation
	springConfig.addAlias 'serializer', 'structuredSerializationService'
	
}
