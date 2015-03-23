package eu.qualify.food4me.input

import java.util.List;

import eu.qualify.food4me.ModifiedProperty
import eu.qualify.food4me.Property
import eu.qualify.food4me.Unit
import eu.qualify.food4me.interfaces.Advisable
import eu.qualify.food4me.interfaces.Measurable
import eu.qualify.food4me.interfaces.Parser
import eu.qualify.food4me.measurements.MeasuredValue
import eu.qualify.food4me.measurements.Measurement
import eu.qualify.food4me.measurements.MeasurementStatus
import eu.qualify.food4me.measurements.Measurements
import grails.converters.JSON
import grails.transaction.Transactional

class JsonParseService implements Parser {
	
	protected conversionMap = [
		generic: Property.PROPERTY_GROUP_GENERIC,
		nutrients: Property.PROPERTY_GROUP_NUTRIENT,
		biomarkers: Property.PROPERTY_GROUP_BIOMARKER,
		snps: Property.PROPERTY_GROUP_SNP,
		physical: Property.PROPERTY_GROUP_PHYSICAL,
		foodgroups: Property.PROPERTY_GROUP_FOODGROUP
	]

	@Override
	public Measurements parseMeasurements(def input) {
		def output = new Measurements()
		def measurementJSON = input.toString()
		def parsed
		
		try {
			parsed = JSON.parse(measurementJSON)
		} catch( Exception e ) {
			log.error "No proper JSON is provided to describe the measurements: " + measurementJSON
			return output
		}

		// Loop through each group of measurements		
		parsed.each { groupName, data ->
			if( !conversionMap[ groupName ] ) {
				log.warn "Group ${groupName} is not supported within this webservice. Skipping all measurements in the group"
				return
			}
			
			data.each { propertyName, valueData ->
				// Find the given property
				def property = Property.findByEntityIlikeAndPropertyGroup( propertyName, conversionMap[ groupName ] )
				
				if( !property ) {
					log.warn "Cannot find property ${propertyName} in group ${groupName}. Skipping this measurement"
					return
				}

				// Nutrients are treated differently, as the intake is split into groups
				if( groupName.toLowerCase() == "nutrients" ) {
					output.addAll parseNutrient( valueData, property )
				} else {
					output.add parseMeasurement( property, property.unit, valueData)
				}
			}
		}
		
		output
	}
	
	/**
	 * Parse a single measurement
	 * @param measurable	Property to measure
	 * @param unit			Unit that is expected 
	 * @param data			Map with keys value and unit
	 * @return
	 */
	protected Measurement parseMeasurement( Measurable measurable, Unit unit, def data ) {
		// Check if the value is the correct format { value: ..., unit: "" }
		if( !(data instanceof Map) || !data.containsKey( "value" ) ) {
			log.warn "The value specified for ${measurable} is invalid. Please specify a value and its unit."
			return
		}
			
		// If the unit doesn't match the required unit, don't use the measurement
		if( unit && unit.code != data.unit ) {
			log.warn "The required unit for ${measurable} is ${unit}. The specified unit is ${data.unit}. Currently, we are not capable of converting between units."
			return
		}
		
		// Create a value and measurement object
		log.info "Parsing measurement for ${measurable}: ${data.value} ${data.unit}"
		def measuredValue = MeasuredValue.fromValue( data.value )
		measuredValue.unit = unit
		
		new Measurement(property: measurable, value: measuredValue )
	}
		
	protected List<Measurement> parseNutrient( def valueData, Measurable measurable ) {
		List<Measurement> measurements = []
		
		// The data is split up into foodgroups. The allowed foodgroups are specified as 
		// a value in the ModifiedProperty.Modifier enum
		def allowedFoodGroups
		valueData.each { modifier, groupData ->
			// Check if this modifier is supported. If not, skip this measurement
			if( !ModifiedProperty.Modifier.contains( modifier ) ) {
				log.warn "The group " + modifier + " to be imported for " + measurable + " is not supported. " + 
					"Please note that the totals for this nutrient may not be accurate as this measurement is not used." 
				return
			}
			
			// Parse the measurement itself
			measurements << parseMeasurement( new ModifiedProperty( property: measurable, modifier: modifier ), measurable.unit, groupData)
		}

		measurements
	}
	
	@Override
	public MeasurementStatus parseStatus(def input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Advisable> parseEntityList(def input) {
		// TODO Auto-generated method stub
		return null;
	}

}
