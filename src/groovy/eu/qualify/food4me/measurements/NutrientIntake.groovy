package eu.qualify.food4me.measurements

import eu.qualify.food4me.FoodGroup

class NutrientIntake extends Measurement {
	
	Map<FoodGroup,MeasuredValue> intake
	
	public MeasuredValue getMeasuredValue() {
		if( !intake )
			return null;
		
		def total = new MeasuredNumericValue()
		intake.values().each { it ->
			if( !total.unit ) 
				total.unit = intake.unit
				
			if( total.unit != intake.unit ) {
				throw Exception( "Cannot compute total nutrient intake when using different units. All nutrient intakes should be provided in the same unit")
			}
			
			total.value += it.value
		}
		
		total
	}
	
	public MeasuredValue getTotal() { getMeasuredValue() }
}
