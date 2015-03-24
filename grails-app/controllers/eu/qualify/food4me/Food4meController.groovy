package eu.qualify.food4me

import eu.qualify.food4me.decisiontree.Advice
import eu.qualify.food4me.decisiontree.AdviceText
import eu.qualify.food4me.interfaces.Advisable
import eu.qualify.food4me.measurements.MeasurementStatus
import eu.qualify.food4me.measurements.Measurements
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
class Food4meController {
	def computeStatusService
	def derivedMeasurementsService
	def determineAdvisableService
	def generateAdviceService
	
	def parameterBasedParseService
	def structuredSerializationService
	
	def form() {
		// Find all properties grouped by propertygroup
		def groupedProperties = [:]
		def nutrients = []
		Property.list( sort: 'entity' ).each {
			if( it.propertyGroup == Property.PROPERTY_GROUP_NUTRIENT ) {
				nutrients << it
			} else {
				if(!groupedProperties[it.propertyGroup])
					groupedProperties[it.propertyGroup] = []
					
				groupedProperties[it.propertyGroup] << it
			}
		}
		
		// Determine the modifiers to allow the user to enter through the form
		def nutrientModifiers = [
			ModifiedProperty.Modifier.INTAKE_MEAT_FISH,
			ModifiedProperty.Modifier.INTAKE_SOUP_SAUCES,
			ModifiedProperty.Modifier.INTAKE_SWEETS_SNACKS,
			ModifiedProperty.Modifier.INTAKE_FATS_SPREADS,
			ModifiedProperty.Modifier.INTAKE_POTATOES_RICE_PASTA,
			ModifiedProperty.Modifier.INTAKE_EGGS,
			ModifiedProperty.Modifier.INTAKE_SUPPLEMENTS,
		]
		
		// Convert names of the property groups into names of the parameter (groups) in the URL
		def conversionMap = [
			(Property.PROPERTY_GROUP_GENERIC): 'generic',
			(Property.PROPERTY_GROUP_NUTRIENT): 'nutrient',
			(Property.PROPERTY_GROUP_BIOMARKER): 'biomarker',
			(Property.PROPERTY_GROUP_SNP): 'snp',
			(Property.PROPERTY_GROUP_PHYSICAL): 'physical',
			(Property.PROPERTY_GROUP_FOODGROUP): 'foodgroup'
		]
		
		[
			nutrients: nutrients,
			nutrientModifiers: nutrientModifiers, 
			properties: groupedProperties, 
			conversionMap: conversionMap
		]
	}
	
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
			json { render structuredSerializationService.serializeAdvices( advices, language ) as JSON }
		}
	}
}
