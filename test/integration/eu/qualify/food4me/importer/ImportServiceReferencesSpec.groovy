package eu.qualify.food4me.importer

import eu.qualify.food4me.Property
import eu.qualify.food4me.measurements.Status
import eu.qualify.food4me.reference.ReferenceCondition
import eu.qualify.food4me.reference.ReferenceValue
import grails.test.mixin.*

/**
 * Test for importService.
 * 
 * This is an integration test instead of a unit test because the import service make use of the 
 * CSV plugin, which doesn't add the toCsvReader methods in unit tests
 */
class ImportServiceReferencesSpec extends ImportServiceIntegrationSpec {

	void "test importing generic references"() {
		given: "a list of references to import, in the expected format"
			// Format: name, group, unit, age lower, age higher, gender, very low (color and upper boundary), low (color and upper boundary), ok (color and upper boundary), high (color and upper boundary), very high (color), 
			def referencesToImport = [
				[ "headerline", "to", "be", "ignored" ],
				[ "secondline", "also", "ignored" ],
				[ "Carbohydrate", "Nutrient", "% of total energy", "", "", "", "Red", "40", "Amber", "45", "Green", "65", "Amber", "70", "Red" ],
				[ "Vitamin C", "Nutrient", "mg", "10", "20", "Male", "", "", "Red", "75", "Green", "2000", "Red" ]
			]
			InputStream is = getInputStreamFromImportDatastructure(referencesToImport)
			
			assert ReferenceValue.count == 0
		
		and: "the corresponding properties in the database"
			def carbohydrate = new Property( entity: "Carbohydrate", propertyGroup: "Nutrient", externalId: "2331003" )
			def vitaminC = new Property( entity: "Vitamin C", propertyGroup: "Nutrient", externalId: "286586005" )
			def age = new Property( entity: "Age", propertyGroup: "Generic", externalId: "age" )
			def gender = new Property( entity: "Gender", propertyGroup: "Generic", externalId: "gender" )
			carbohydrate.save()
			vitaminC.save()
			age.save()
			gender.save()
			
		when: "importing the references"
			importService.loadGenericReferences(is)
		
		then: "references for both nutrients are imported"
			ReferenceValue.count == 8
			ReferenceValue.countBySubject( carbohydrate ) == 5
			ReferenceValue.countBySubject( vitaminC ) == 3
			
		and: "the reference values and colors for carbohydrate are properly stored"
			ReferenceValue verylow = ReferenceValue.findBySubjectAndStatus( carbohydrate, "Very low" )
			verylow.color == Status.Color.RED
			verylow.conditions?.size() == 1
			
			verylow.conditions[0].subject == carbohydrate
			verylow.conditions[0].conditionType == ReferenceCondition.TYPE_NUMERIC
			!verylow.conditions[0].low
			verylow.conditions[0].high == 40
			!verylow.conditions[0].value
			
			ReferenceValue low = ReferenceValue.findBySubjectAndStatus( carbohydrate, "Low" )
			low.color == Status.Color.AMBER
			low.conditions?.size() == 1
			
			low.conditions[0].subject == carbohydrate
			low.conditions[0].conditionType == ReferenceCondition.TYPE_NUMERIC
			low.conditions[0].low == 40
			low.conditions[0].high == 45
			!low.conditions[0].value

			ReferenceValue ok = ReferenceValue.findBySubjectAndStatus( carbohydrate, "OK" )
			ok.color == Status.Color.GREEN
			ok.conditions?.size() == 1
			
			ok.conditions[0].subject == carbohydrate
			ok.conditions[0].conditionType == ReferenceCondition.TYPE_NUMERIC
			ok.conditions[0].low == 45
			ok.conditions[0].high == 65
			!ok.conditions[0].value

			ReferenceValue high = ReferenceValue.findBySubjectAndStatus( carbohydrate, "High" )
			high.color == Status.Color.AMBER
			high.conditions?.size() == 1
			
			high.conditions[0].subject == carbohydrate
			high.conditions[0].conditionType == ReferenceCondition.TYPE_NUMERIC
			high.conditions[0].low == 65
			high.conditions[0].high == 70
			!high.conditions[0].value
			
			ReferenceValue veryhigh = ReferenceValue.findBySubjectAndStatus( carbohydrate, "Very high" )
			veryhigh.color == Status.Color.RED
			veryhigh.conditions?.size() == 1
			
			veryhigh.conditions[0].subject == carbohydrate
			veryhigh.conditions[0].conditionType == ReferenceCondition.TYPE_NUMERIC
			veryhigh.conditions[0].low == 70
			!veryhigh.conditions[0].high
			!veryhigh.conditions[0].value
			
		and: "the reference values and colors for vitamin C are properly stored"
			def vlow = ReferenceValue.findBySubjectAndStatus( vitaminC, "Low" )
			vlow.color == Status.Color.RED
			vlow.conditions?.size() == 3
			
			def ageCondition = vlow.conditions.find { it.subject == age }
			ageCondition.conditionType == ReferenceCondition.TYPE_NUMERIC
			ageCondition.low == 10
			ageCondition.high == 20
			
			def genderCondition = vlow.conditions.find { it.subject == gender }
			!genderCondition.low
			!genderCondition.high
			genderCondition.conditionType == ReferenceCondition.TYPE_TEXT
			genderCondition.value == "Male"
			
			def vitaminCCondition = vlow.conditions.find { it.subject == vitaminC }
			!vitaminCCondition.low
			vitaminCCondition.high == 75
			!vitaminCCondition.value
	
			def vok = ReferenceValue.findBySubjectAndStatus( vitaminC, "OK" )
			vok.color == Status.Color.GREEN
			vok.conditions?.size() == 3
			
			def vitaminCConditionOK = vok.conditions.find { it.subject == vitaminC }
			vitaminCConditionOK.low == 75
			vitaminCConditionOK.high == 2000
			!vitaminCConditionOK.value
	
			def vhigh = ReferenceValue.findBySubjectAndStatus( vitaminC, "High" )
			vhigh.color == Status.Color.RED
			vhigh.conditions?.size() == 3
			
			def vitaminCConditionHigh = vhigh.conditions.find { it.subject == vitaminC }
			vitaminCConditionHigh.low == 2000
			!vitaminCConditionHigh.high
			!vitaminCConditionHigh.value
			
		and: "Vitamin C is never very low or very high"
			!ReferenceValue.findAllBySubjectAndStatus( vitaminC, "Very low" )
			!ReferenceValue.findAllBySubjectAndStatus( vitaminC, "Very high" )
	}
	
	void "test importing generic references with improper lines"() {
		given: "a list of references to import, in the expected format"
			// Format: name, group, unit, age lower, age higher, gender, very low (color and upper boundary), low (color and upper boundary), ok (color and upper boundary), high (color and upper boundary), very high (color),
			def referencesToImport = [
				[ "headerline", "to", "be", "ignored" ],
				[ "secondline", "also", "ignored" ],
				[],
				[ "Carbohydrate", "Nutrient", "% of total energy", "", "", "", "Red", "40", "Amber", "45", "Green", "65", "Amber", "70", "Red" ],
				[ "abc" ],
				[ "Vitamin C", "Biomarker", "", "", "", "", "", "", "Red", "90", "Green" ],
				[ "Vitamin C", "Nutrient", "mg", "10", "20", "Male" ]
			]
			InputStream is = getInputStreamFromImportDatastructure(referencesToImport)
			
			assert ReferenceValue.count == 0
		
		and: "the corresponding properties in the database"
			def carbohydrate = new Property( entity: "Carbohydrate", propertyGroup: "Nutrient", externalId: "2331003" )
			def vitaminC = new Property( entity: "Vitamin C", propertyGroup: "Nutrient", externalId: "286586005" )
			def age = new Property( entity: "Age", propertyGroup: "Generic", externalId: "age" )
			def gender = new Property( entity: "Gender", propertyGroup: "Generic", externalId: "gender" )
			carbohydrate.save()
			vitaminC.save()
			age.save()
			gender.save()
			
		when: "importing the references"
			importService.loadGenericReferences(is)
		
		then: "references for carbohydrate are imported and improper lines are discarded"
			ReferenceValue.count == 5
			ReferenceValue.countBySubject( carbohydrate ) == 5
	}
	
	void "test importing generic references without gender and age"() {
		given: "a list of references to import, in the expected format"
			// Format: name, group, unit, age lower, age higher, gender, very low (color and upper boundary), low (color and upper boundary), ok (color and upper boundary), high (color and upper boundary), very high (color),
			def referencesToImport = [
				[ "headerline", "to", "be", "ignored" ],
				[ "secondline", "also", "ignored" ],
				[ "Carbohydrate", "Nutrient", "% of total energy", "", "", "", "Red", "40", "Amber", "45", "Green", "65", "Amber", "70", "Red" ],
				[ "Vitamin C", "Nutrient", "mg", "10", "20", "Male", "", "", "Red", "75", "Green", "2000", "Red" ]
			]
			InputStream is = getInputStreamFromImportDatastructure(referencesToImport)
			
			assert ReferenceValue.count == 0
		
		and: "the corresponding properties in the database"
			def carbohydrate = new Property( entity: "Carbohydrate", propertyGroup: "Nutrient", externalId: "2331003" )
			def vitaminC = new Property( entity: "Vitamin C", propertyGroup: "Nutrient", externalId: "286586005" )
			carbohydrate.save()
			vitaminC.save()
			
		and: "No gender and age in the database"
			assert !Property.findByEntity( "Gender" )
			assert !Property.findByEntity( "Age" )
			
		when: "importing the references"
			importService.loadGenericReferences(is)
		
		then: "references for carbohydarte are imported"
			ReferenceValue.count == 5
			ReferenceValue.countBySubject( carbohydrate ) == 5
			
		and: "the reference values and colors for vitamin C are discarded"
			ReferenceValue.countBySubject( vitaminC ) == 0

	}

	void "test importing SNP references"() {
		given: "a list of SNP references to import, in the expected format"
			def referencesToImport = [
				[ "Gene/SNP", "Risk allele", "", "", "Non-risk allele", "" ],
				[ "FADS1", "CC", "", "", "CT", "TC" ],
				[ "FTO", "AA", "TA", "AT", "TT" ]
			]
			InputStream is = getInputStreamFromImportDatastructure(referencesToImport)
			
			assert ReferenceValue.count == 0
		
		and: "the corresponding SNPs in the database"
			def fads1 = new Property( entity: "FADS1", propertyGroup: "SNP", externalId: "rs174546" )
			def fto = new Property( entity: "FTO", propertyGroup: "SNP", externalId: "rs9939609" )
			fads1.save()
			fto.save()
			
		when: "importing the references"
			importService.loadSNPReferences(is)
		
		then: "references for both SNPs are imported"
			ReferenceValue.count == 7
			ReferenceValue.countBySubject( fads1 ) == 3
			ReferenceValue.countBySubject( fto ) == 4
			
		and: "the reference values for FADS1 are properly stored"
			def fads1Risk = ReferenceValue.findAllBySubjectAndStatus( fads1, "Risk allele" )
			fads1Risk?.size() == 1
			fads1Risk[0].subject == fads1
			fads1Risk[0].conditions.size() == 1
			
			fads1Risk[0].conditions[0].subject == fads1
			!fads1Risk[0].conditions[0].low
			!fads1Risk[0].conditions[0].high
			fads1Risk[0].conditions[0].value == "CC"
			
			def fads1NonRisk = ReferenceValue.findAllBySubjectAndStatus( fads1, "Non-risk allele" )
			fads1NonRisk?.size() == 2
			fads1NonRisk.findAll { it.conditions.size() == 1 }.size() == 2
			fads1NonRisk.find { it.conditions[0].value == "CT" }
			fads1NonRisk.find { it.conditions[0].value == "TC" }
			
		and: "the reference values for FTO are properly stored"
			def ftoRisk = ReferenceValue.findAllBySubjectAndStatus( fto, "Risk allele" )
			ftoRisk?.size() == 3
			ftoRisk.findAll { it.conditions.size() == 1 }.size() == 3
			ftoRisk.find { it.conditions[0].value == "AA" }
			ftoRisk.find { it.conditions[0].value == "AT" }
			ftoRisk.find { it.conditions[0].value == "TA" }
			
			def ftoNonRisk = ReferenceValue.findAllBySubjectAndStatus( fto, "Non-risk allele" )
			ftoNonRisk?.size() == 1
			ftoNonRisk[0].conditions.size() == 1
			ftoNonRisk[0].conditions[0].value == "TT"
	}

	void "test importing SNP references with improper lines"() {
		given: "a list of SNP references to import, in the expected format"
			// Please note: the header line has only 5 columns, whereas the FADS1 line has 6 columns. The last one will be disregarded
			def referencesToImport = [
				[ "Gene/SNP", "Risk allele", "", "", "Non-risk allele" ],
				[],
				[ "Non-Existent", "AA" ],
				[ "FADS1", "CC", "", "", "CT", "TC" ],
				[ "", "", "", "", "CA" ],
				[ "FTO", "AA", "TA", "AT", "TT" ]
			]
			InputStream is = getInputStreamFromImportDatastructure(referencesToImport)
			
			assert ReferenceValue.count == 0
		
		and: "the corresponding SNPs in the database"
			def fads1 = new Property( entity: "FADS1", propertyGroup: "SNP", externalId: "rs174546" )
			def fto = new Property( entity: "FTO", propertyGroup: "SNP", externalId: "rs9939609" )
			fads1.save()
			fto.save()
			
		when: "importing the references"
			importService.loadSNPReferences(is)
		
		then: "references for both SNPs are imported"
			ReferenceValue.count == 6
			ReferenceValue.countBySubject( fads1 ) == 2
			ReferenceValue.countBySubject( fto ) == 4
	}
	
	
}
