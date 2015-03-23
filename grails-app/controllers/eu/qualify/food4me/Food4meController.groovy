package eu.qualify.food4me

import eu.qualify.food4me.decisiontree.Advice
import eu.qualify.food4me.decisiontree.AdviceText
import eu.qualify.food4me.interfaces.Advisable
import eu.qualify.food4me.measurements.MeasurementStatus
import eu.qualify.food4me.measurements.Measurements
import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
class Food4meController {
	def computeStatusService
	def derivedMeasurementsService
	def determineAdvisableService
	def generateAdviceService
	
	def parameterBasedParseService
	def jsonSerializeService
	
	def advices() {
		Measurements measurements = parameterBasedParseService.parseMeasurements(params)
		derivedMeasurementsService.deriveMeasurements(measurements)
		
		MeasurementStatus status = computeStatusService.computeStatus(measurements)
		List<Advisable> advisables = determineAdvisableService.determineAdvisables(status, measurements )
		List<Advice> advices = generateAdviceService.generateAdvice( measurements, status, advisables )

		// Determine output language. Defaults to English
		def language = params.language
		if( !AdviceText.isLanguageSupported( language ) )
			language = "en"
		
		// Use content negotiation to output the data
		withFormat {
			html advices: advices, measurements: measurements, translations: AdviceText.getTranslations( advices, language )
			json { jsonSerializeService.serializeAdvices( advices, language ) }
		}
	}
}
