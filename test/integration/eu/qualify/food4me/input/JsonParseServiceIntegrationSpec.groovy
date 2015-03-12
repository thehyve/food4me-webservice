package eu.qualify.food4me.input

import eu.qualify.food4me.ModifiedProperty
import eu.qualify.food4me.Property
import eu.qualify.food4me.Unit
import eu.qualify.food4me.exampledata.IntegrationTestHelper
import eu.qualify.food4me.measurements.MeasuredNumericValue
import eu.qualify.food4me.measurements.MeasuredTextValue
import eu.qualify.food4me.measurements.Measurement
import eu.qualify.food4me.measurements.Measurements
import grails.test.mixin.TestFor
import grails.test.spock.IntegrationSpec

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(JsonParseService)
class JsonParseServiceIntegrationSpec extends IntegrationSpec {
	def jsonParseService
	
	def setup() {
		// Make sure the database is empty
		IntegrationTestHelper.cleanUp()

		// Initialize units
		def years = Unit.findByCode( "yr" ) ?: new Unit( name: "years", externalId: "258707000", code: "yr" )
			years.save(failOnError: true)
		def gPerKgBodyWeight = Unit.findByCode( "g/kg bw" ) ?: new Unit( name: "g/kg body weight", externalId: "228919004", code: "g/kg bw" )
			gPerKgBodyWeight.save(failOnError: true)
		def grams = Unit.findByCode( "g" ) ?: new Unit( name: "g", externalId: "258682000", code: "g" )
			grams.save(failOnError: true)
		def percentageEnergyIntake = Unit.findByCode( "% energy intake" ) ?: new Unit( name: "% of total energy intake", externalId: "288493004", code: "% energy intake" )
			percentageEnergyIntake.save(failOnError: true)

		// Initialize properties needed
		def age = Property.findByEntity( "Age" ) ?: new Property(propertyGroup: Property.PROPERTY_GROUP_GENERIC, entity: "Age", externalId: "397669002", unit: years)
			age.save(failOnError: true)
		def gender = Property.findByEntity( "Gender" ) ?: new Property(propertyGroup: Property.PROPERTY_GROUP_GENERIC, entity: "Gender", externalId: "263495000" )
			gender.save(failOnError: true)
		def protein = Property.findByEntity( "Protein" ) ?: new Property(propertyGroup: Property.PROPERTY_GROUP_NUTRIENT, entity: "Protein", externalId: "88878007", unit: gPerKgBodyWeight)
			protein.save(failOnError: true)
		def carbohydrate = Property.findByEntity( "Carbohydrate" ) ?: new Property(propertyGroup: Property.PROPERTY_GROUP_NUTRIENT, entity: "Carbohydrate", externalId: "2331003", unit: percentageEnergyIntake)
			carbohydrate.save(failOnError: true)
		def fibre = Property.findByEntity( "Fibre" ) ?: new Property(propertyGroup: Property.PROPERTY_GROUP_NUTRIENT, entity: "Fibre", externalId: "400293002", unit: grams)
			fibre.save(failOnError: true)    
	}

    def cleanup() {
    }

    void "test parsing generic measurements"() {
		given: "a set of measurements serialized as JSON"
			def input = [
				nutrients: [
					protein: [
						(ModifiedProperty.Modifier.INTAKE_MEAT_FISH.id): [ value: 2, unit: "g/kg bw" ],
						(ModifiedProperty.Modifier.INTAKE_EGGS.id): [ value: 1, unit: "g/kg bw" ],
						(ModifiedProperty.Modifier.INTAKE_POTATOES_RICE_PASTA.id): [ value: 1, unit: "g/kg bw" ],
						(ModifiedProperty.Modifier.INTAKE_SUPPLEMENTS.id): [ value: 3, unit: "g/kg bw" ]
					],
				],
				generic: [
					age: [ value: 31, unit: "yr" ],
					gender: [ value: "Male" ]
				]
			]
			def json = input.encodeAsJSON()
		
		and: "the properties and units stored in the database"
			def protein = Property.findByEntity( "Protein" )
			def age = Property.findByEntity( "Age" )
			def gender = Property.findByEntity( "Gender" )
			def year = Unit.findByCode( "yr" )
			def gPerKgBW = Unit.findByCode( "g/kg bw" )
			assert protein
			assert age
			assert gender
			assert year
			assert gPerKgBW
			
		when:
			Measurements measurements = service.parseMeasurements( json.toString() )
			
		then:
			measurements
			measurements.all.size() == 6
			measurements.getValueFor( age ) == new MeasuredNumericValue( value: 31, unit: year ) 
			measurements.getValueFor( gender ) == new MeasuredTextValue( value: "Male" )
			measurements.getValueFor( new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_MEAT_FISH.id ) ) == new MeasuredNumericValue( value: 2, unit: gPerKgBW )
			measurements.getValueFor( new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_EGGS.id ) ) == new MeasuredNumericValue( value: 1, unit: gPerKgBW )
			measurements.getValueFor( new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_POTATOES_RICE_PASTA.id ) ) == new MeasuredNumericValue( value: 1, unit: gPerKgBW )
			measurements.getValueFor( new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_SUPPLEMENTS.id ) ) == new MeasuredNumericValue( value: 3, unit: gPerKgBW )
    }
}
