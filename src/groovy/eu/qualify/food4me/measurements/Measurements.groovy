/*
 *  Copyright (C) 2015 The Hyve
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.qualify.food4me.measurements

import eu.qualify.food4me.ModifiedProperty
import eu.qualify.food4me.Property
import eu.qualify.food4me.interfaces.Measurable
import groovy.transform.Canonical

 @Canonical
 class Measurements {
	List<Measurement> measurements
	
	public Measurements() {
		measurements = []
	}
	
	List<Measurement> getAll() {
		measurements
	}
	
	void add(Measurement measurement) {
		if( measurement )
			measurements << measurement
	}
	
	void addAll(Collection<Measurement> measurements ) {
		this.measurements.addAll(measurements.findAll())
	}

	public Measurement get( Measurable p ) {
		return measurements.find { it.property == p }
	}
	
	public MeasuredValue getValueFor( Measurable p ) {
		return get(p)?.value
	}
	
	/**
	 * Returns a list of values for the given property, including modified properties
	 * @param p
	 * @return
	 */
	public List<Measurement> getValuesFor( Property p ) {
		return measurements.findAll {
			it?.property?.rootProperty == p 
		}
	}

	/**
	 * Returns a list with properties for which we have measurements 
	 * @param propertyGroup
	 * @return
	 */
	public List<Property> getAllPropertiesForPropertyGroup( String propertyGroup ) {
		measurements.collect {
			if( it?.property?.rootProperty?.propertyGroup != propertyGroup )
				return null
			
			it?.property?.rootProperty
		}.findAll().unique()
	}
	
}
