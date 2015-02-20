import eu.qualify.food4me.Property
import eu.qualify.food4me.Unit
import eu.qualify.food4me.measurements.Status
import eu.qualify.food4me.reference.ReferenceCondition
import eu.qualify.food4me.reference.ReferenceValue
import grails.util.Environment

class BootStrap {

    def init = { servletContext ->
		if( Environment.getCurrent().name in [ "env", "test" ] )
			initializeReferences()
    }
	
	def initializeReferences() {
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

		// Initialize a few simple references
		if( ReferenceValue.count == 0 ) {
			def proteinVeryLow = new ReferenceValue(subject: protein, status: Status.STATUS_VERY_LOW )
				proteinVeryLow.addToConditions( subject: protein, high: 0.52 )
				proteinVeryLow.save(failOnError: true)
				
			def proteinLow = new ReferenceValue(subject: protein, status: Status.STATUS_LOW )
				proteinLow.addToConditions( new ReferenceCondition( subject: protein, low: 0.52, high: 0.66 ) )
				proteinLow.save(failOnError: true)
	
			def proteinOK = new ReferenceValue(subject: protein, status: Status.STATUS_OK )
				proteinOK.addToConditions( new ReferenceCondition( subject: protein, low: 0.66, high: 2.4 ) )
				proteinOK.save(failOnError: true)
	
			def proteinHigh = new ReferenceValue(subject: protein, status: Status.STATUS_HIGH )
				proteinHigh.addToConditions( new ReferenceCondition( subject: protein, low: 2.4 ) )
				proteinHigh.save(failOnError: true)
				
			def carbohydrateVeryLow = new ReferenceValue(subject: carbohydrate, status: Status.STATUS_VERY_LOW )
				carbohydrateVeryLow.addToConditions( new ReferenceCondition( subject: carbohydrate, high: 40 ) )
				carbohydrateVeryLow.save(failOnError: true)
				
			def carbohydrateLow = new ReferenceValue(subject: carbohydrate, status: Status.STATUS_LOW )
				carbohydrateLow.addToConditions( new ReferenceCondition( subject: carbohydrate, low: 40, high: 45 ) )
				carbohydrateLow.save(failOnError: true)
	
			def carbohydrateOK = new ReferenceValue(subject: carbohydrate, status: Status.STATUS_OK )
				carbohydrateOK.addToConditions( new ReferenceCondition( subject: carbohydrate, low: 45, high: 65 ) )
				carbohydrateOK.save(failOnError: true)
	
			def carbohydrateHigh = new ReferenceValue(subject: carbohydrate, status: Status.STATUS_HIGH )
				carbohydrateHigh.addToConditions( new ReferenceCondition( subject: carbohydrate, low: 65, high: 70 ) )
				carbohydrateHigh.save(failOnError: true)
				
			def carbohydrateVeryHigh = new ReferenceValue(subject: carbohydrate, status: Status.STATUS_VERY_HIGH )
				carbohydrateVeryHigh.addToConditions( new ReferenceCondition( subject: carbohydrate, low: 70 ) )
				carbohydrateVeryHigh.save(failOnError: true)
	
			def fibreVeryLow1 = new ReferenceValue(subject: fibre, status: Status.STATUS_VERY_LOW )
				fibreVeryLow1.addToConditions( new ReferenceCondition( subject: fibre, high: 28 ) )
				fibreVeryLow1.addToConditions( new ReferenceCondition( subject: gender, value: "male") )
				fibreVeryLow1.addToConditions( new ReferenceCondition( subject: age, low: 17, high: 50 ) )
				fibreVeryLow1.save(failOnError: true)
				
			def fibreLow1 = new ReferenceValue(subject: fibre, status: Status.STATUS_LOW )
				fibreLow1.addToConditions( new ReferenceCondition( subject: fibre, low: 28, high: 38 ) )
				fibreLow1.addToConditions( new ReferenceCondition( subject: gender, value: "male") )
				fibreLow1.addToConditions( new ReferenceCondition( subject: age, low: 17, high: 50 ) )
				fibreLow1.save(failOnError: true)
	
			def fibreOK1 = new ReferenceValue(subject: fibre, status: Status.STATUS_OK )
				fibreOK1.addToConditions( new ReferenceCondition( subject: fibre, low: 38 ) )
				fibreOK1.addToConditions( new ReferenceCondition( subject: gender, value: "male") )
				fibreOK1.addToConditions( new ReferenceCondition( subject: age, low: 17, high: 50 ) )
				fibreOK1.save(failOnError: true)
	
			def fibreVeryLow2 = new ReferenceValue(subject: fibre, status: Status.STATUS_VERY_LOW )
				fibreVeryLow2.addToConditions( new ReferenceCondition( subject: fibre, high: 20 ) )
				fibreVeryLow2.addToConditions( new ReferenceCondition( subject: gender, value: "male") )
				fibreVeryLow2.addToConditions( new ReferenceCondition( subject: age, low: 50 ) )
				fibreVeryLow2.save(failOnError: true)
				
			def fibreLow2 = new ReferenceValue(subject: fibre, status: Status.STATUS_LOW )
				fibreLow2.addToConditions( new ReferenceCondition( subject: fibre, low: 20, high: 30 ) )
				fibreLow2.addToConditions( new ReferenceCondition( subject: gender, value: "male") )
				fibreLow2.addToConditions( new ReferenceCondition( subject: age, low: 50 ) )
				fibreLow2.save(failOnError: true)
	
			def fibreOK2 = new ReferenceValue(subject: fibre, status: Status.STATUS_OK )
				fibreOK2.addToConditions( new ReferenceCondition( subject: fibre, low: 30 ) )
				fibreOK2.addToConditions( new ReferenceCondition( subject: gender, value: "male") )
				fibreOK2.addToConditions( new ReferenceCondition( subject: age, low: 50 ) )
				fibreOK2.save(failOnError: true)
	
			def fibreVeryLow3 = new ReferenceValue(subject: fibre, status: Status.STATUS_VERY_LOW )
				fibreVeryLow3.addToConditions( new ReferenceCondition( subject: fibre, high: 15 ) )
				fibreVeryLow3.addToConditions( new ReferenceCondition( subject: gender, value: "female") )
				fibreVeryLow3.addToConditions( new ReferenceCondition( subject: age, low: 17, high: 50 ) )
				fibreVeryLow3.save(failOnError: true)
				
			def fibreLow3 = new ReferenceValue(subject: fibre, status: Status.STATUS_LOW )
				fibreLow3.addToConditions( new ReferenceCondition( subject: fibre, low: 15, high: 25 ) )
				fibreLow3.addToConditions( new ReferenceCondition( subject: gender, value: "female") )
				fibreLow3.addToConditions( new ReferenceCondition( subject: age, low: 17, high: 50 ) )
				fibreLow3.save(failOnError: true)
	
			def fibreOK3 = new ReferenceValue(subject: fibre, status: Status.STATUS_OK )
				fibreOK3.addToConditions( new ReferenceCondition( subject: fibre, low: 25 ) )
				fibreOK3.addToConditions( new ReferenceCondition( subject: gender, value: "female") )
				fibreOK3.addToConditions( new ReferenceCondition( subject: age, low: 17, high: 50 ) )
				fibreOK3.save(failOnError: true)
	
			def fibreVeryLow4 = new ReferenceValue(subject: fibre, status: Status.STATUS_VERY_LOW )
				fibreVeryLow4.addToConditions( new ReferenceCondition( subject: fibre, high: 14 ) )
				fibreVeryLow4.addToConditions( new ReferenceCondition( subject: gender, value: "female") )
				fibreVeryLow4.addToConditions( new ReferenceCondition( subject: age, low: 50 ) )
				fibreVeryLow4.save(failOnError: true)
				
			def fibreLow4 = new ReferenceValue(subject: fibre, status: Status.STATUS_LOW )
				fibreLow4.addToConditions( new ReferenceCondition( subject: fibre, low: 14, high: 21 ) )
				fibreLow4.addToConditions( new ReferenceCondition( subject: gender, value: "female") )
				fibreLow4.addToConditions( new ReferenceCondition( subject: age, low: 50 ) )
				fibreLow4.save(failOnError: true)
	
			def fibreOK4 = new ReferenceValue(subject: fibre, status: Status.STATUS_OK )
				fibreOK4.addToConditions( new ReferenceCondition( subject: fibre, low: 21 ) )
				fibreOK4.addToConditions( new ReferenceCondition( subject: gender, value: "female") )
				fibreOK4.addToConditions( new ReferenceCondition( subject: age, low: 50 ) )
				fibreOK4.save(failOnError: true)
		}
	}
	
    def destroy = {
    }
}
