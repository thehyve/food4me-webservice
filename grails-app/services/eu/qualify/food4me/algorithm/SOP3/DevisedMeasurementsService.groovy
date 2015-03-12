package eu.qualify.food4me.algorithm.SOP3

import eu.qualify.food4me.ModifiedProperty
import eu.qualify.food4me.Property
import eu.qualify.food4me.measurements.MeasuredNumericValue
import eu.qualify.food4me.measurements.MeasuredTextValue
import eu.qualify.food4me.measurements.Measurement
import eu.qualify.food4me.measurements.Measurements
import grails.transaction.Transactional

@Transactional
class DevisedMeasurementsService {

	List<String> foodModifiers = [
		ModifiedProperty.Modifier.INTAKE_DAIRY,
		ModifiedProperty.Modifier.INTAKE_EGGS,
		ModifiedProperty.Modifier.INTAKE_FATS_SPREADS,
		ModifiedProperty.Modifier.INTAKE_MEAT_FISH,
		ModifiedProperty.Modifier.INTAKE_POTATOES_RICE_PASTA,
		ModifiedProperty.Modifier.INTAKE_SOUP_SAUCES,
		ModifiedProperty.Modifier.INTAKE_SWEETS_SNACKS,
	]
	
	/**
	 * Devise detailed measurements from the given values
	 * @param measurements
	 */
	public void deviseMeasurements(Measurements measurements) {
		// Return if no values are given
		if( !measurements )
			return;
	
		// Determine a list of nutrients, for which we have to devise measurements
		List<Property> nutrients = measurements.getAllPropertiesForPropertyGroup( Property.PROPERTY_GROUP_NUTRIENT )
			
		// For each nutrient, compute some devised measurements
		nutrients.each { nutrient ->
			def nutrientMeasurements = measurements.getValuesFor(nutrient)
			measurements.addAll deviseMeasurementsForNutrient( nutrient, nutrientMeasurements )
		}
		
		// Compute omega3 index and total carotenoids
		computeOmega3Index(measurements)
		computeTotalCarotenoids(measurements)
			
	}
	
	/**
	 * Devise detailed measurements for the given nutrient
	 * 
	 * Devised measurements are:
	 *   a total value for the given nutrient, 
	 *   the amount of intake from food
	 *   the most contributing food groups
	 * @param nutrient
	 * @param nutrientMeasurements
	 * @return
	 */
	protected List<Measurement> deviseMeasurementsForNutrient( Property nutrient, List<Measurement> nutrientMeasurements ) {
		List<Measurement> measurements = []
		
		if( !nutrientMeasurements ) {
			log.warn "No measurements given for " + nutrient + ", so no devised measurements are computed."
			return []
		} 
		
		// We can only devise measurements for this nutrient if all given measurements have the same unit
		if( nutrientMeasurements.find { it.measuredValue.unit && it.measuredValue.unit != nutrient.unit } ) {
			log.warn "One or more of the measurements given for " + nutrient + " have not the expected unit and cannot devise measurements." + 
				" Currently, we cannot convert between units, so all values should be given in " + nutrient.unit
			return []
		}
		
		// First compute the total intake. Only do this if there is no total already stored 
		def total = computeTotalForNutrient( nutrient, nutrientMeasurements )
		if( total )
			measurements.add(total)
			
		// Also compute the total intake from food.
		def fromFood = computeIntakeFromFoodForNutrient( nutrient, nutrientMeasurements )
		if( fromFood )
			measurements.add(fromFood)

		// Compute the most contributing food groups.
		measurements.addAll computeContributingFoodGroupsForNutrient( nutrient, nutrientMeasurements )

		measurements
	}	
	
	protected Measurement computeTotalForNutrient( Property nutrient, List<Measurement> nutrientMeasurements ) {
		if( !nutrientMeasurements.find { it.property == nutrient } ) {
			// Create a new measurement with the total for all nutrient measurements
			def total = nutrientMeasurements.collect { it.measuredValue.type == "numeric" ? it.measuredValue.value : 0 }.sum()
			return new Measurement( property: nutrient, value: new MeasuredNumericValue( unit: nutrient.unit, value: total ) )
		} else {
			log.info "A total value for " + nutrient + " is already provided, so will not be computed."
		}
	}
	
	
	protected Measurement computeIntakeFromFoodForNutrient( Property nutrient, List<Measurement> nutrientMeasurements ) {
		def fromFoodProperty = new ModifiedProperty( property: nutrient, modifier: ModifiedProperty.Modifier.INTAKE_DIETARY )
		if( !nutrientMeasurements.find { it.property == fromFoodProperty } ) {
			// Create a new measurement with the total intake from food
			def totalFromFood = nutrientMeasurements.collect {
				if( it.property instanceof ModifiedProperty && it.property.modifier in foodModifiers ) {
					it.measuredValue.type == "numeric" ? it.measuredValue.value : 0
				} else {
					0
				}
			}.sum()
			
			return new Measurement(
				property: fromFoodProperty,
				value: new MeasuredNumericValue( unit: nutrient.unit, value: totalFromFood )
			)
		} else {
			log.info "A total value for " + nutrient + " from food is already provided, so will not be computed."
		}
	}

	
	protected Measurement computeContributingFoodGroupsForNutrient( Property nutrient, List<Measurement> nutrientMeasurements ) {
		List<Measurement> measurements = []
		
		// Compute the most contributing food groups. To do that,
		// use only the measurements for this nutrient, that have a modifier, are numeric and are not from supplements
		// after that, sort descending on value
		def sortedMeasurements = nutrientMeasurements
			.findAll { it.property instanceof ModifiedProperty && it.value?.type == "numeric" && it.property.modifier != ModifiedProperty.Modifier.INTAKE_SUPPLEMENTS }
			.sort { a, b ->
				// Sort descending on numeric value
				b.value.value <=> a.value.value
			}
			
		if( sortedMeasurements.size() >= 1 ) {
			measurements << new Measurement(
				property: new ModifiedProperty( property: nutrient, modifier: ModifiedProperty.Modifier.FIRST_CONTRIBUTING_FOOD_GROUP ),
				value: new MeasuredTextValue( value: sortedMeasurements[0].property.modifier )
			)
		}

		if( sortedMeasurements.size() >= 2 ) {
			measurements << new Measurement(
				property: new ModifiedProperty( property: nutrient, modifier: ModifiedProperty.Modifier.FIRST_CONTRIBUTING_FOOD_GROUP ),
				value: new MeasuredTextValue( value: sortedMeasurements[1].property.modifier )
			)
		}

		measurements
	}

	/**
	 * Compute the total carotenoids as used within the food4me algorithms.
	 * 
	 * Total carotenoids = alpha-carotene + beta-carotene + lutein + zeaxanthin + beta-cryptoxanthin + lycopene
	 * 
	 * @param measurements
	 * @return
	 */
	protected computeTotalCarotenoids(Measurements measurements) {
		def totalCarotenoids = Property.findByEntity( "Carotenoids" )
		
		if( !totalCarotenoids ) {
			log.warn "No property found for total carotenoids. Cannot store the value for this index."
			return
		}

		if( measurements.findValueFor( totalCarotenoids ) ) {
			log.info "A value is already given for total carotenoids. No need to compute it."
			return
		}
		
		def inputs = [ "Alpha-carotene", "Beta-carotene", "Lutein", "Zeaxanthin", "Beta-cryptoxanthin", "Lycopene" ]
		def inputProperties = inputs.collect { Property.findByEntity(it) }
		
		if( inputProperties.findAll().size() < inputs.size() ) {
			log.warn "Not all properties to compute the total carotenoids could be found. The needed properties are: " + inputs
			return
		}
		
		
		def inputMeasurements = inputProperties.collect { measurements.getValueFor(it) }
		
		if( inputMeasurements.findAll().size() < inputProperties.size() ) {
			log.warn "Not enough information to compute the total carotenoids. Required properties are " + inputs
			return
		}
		
		def totalCarotenoidsValue = inputMeasurements.collect { it.value.value }.sum()
		
		measurements.add new Measurement(
			property: totalCarotenoids,
			value: new MeasuredNumericValue( unit: totalCarotenoids.unit, value: totalCarotenoidsValue )
		)
	}	
	
	/**
	 * Compute the omega 3 index as used within the food4me algorithms.
	 *
	 * This excorporates the three omega-3 fatty acids, Eicosapentaenoic acid (EPA), docosapentaenoate acid (DPA)
	 * and Docosahexaenoate acid (DHA).
	 *
	 * Omega-3 index = 1.4473+0.8303*(EPA+DPA+DHA)
	 *
	 * @param measurements
	 * @return
	 */
	protected computeOmega3Index(Measurements measurements) {
		def n3Index = Property.findByEntity( "Omega-3 index" )
		
		if( !n3Index ) {
			log.warn "No property found for omega-3 index. Cannot store the value for this index."
			return
		}

		if( measurements.findValueFor( n3Index ) ) {
			log.info "A value is already given for the omega-3 index. No need to compute it."
			return
		}

		def dpa = Property.findByEntity( "Docosapentaenoic acid" )
		def epa = Property.findByEntity( "Eicosapentanoic acid" )
		def dha = Property.findByEntity( "Docosahexaenoic acid" )
		
		def dpaMeasurement = measurements.getValueFor( dpa )
		def epaMeasurement = measurements.getValueFor( epa )
		def dhaMeasurement = measurements.getValueFor( dha )
		
		if( !dpaMeasurement || !epaMeasurement || !dhaMeasurement ) {
			log.warn "Not enough information to compute the omega-3 index. Required properties are " + [ dpa, epa, dha ]
			return
		}
		
		def n3IndexValue = 1.4473 + 0.8303 * ( dpaMeasurement.value.value + epaMeasurement.value.value + dhaMeasurement.value.value )
		
		measurements.add new Measurement(
			property: n3Index,
			value: new MeasuredNumericValue( unit: n3Index.unit, value: n3IndexValue )
		)
	}

}
