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
package eu.qualify.food4me

import eu.qualify.food4me.interfaces.Advisable

import eu.qualify.food4me.interfaces.Measurable
import groovy.transform.EqualsAndHashCode


@EqualsAndHashCode(includes="externalId")
class Property implements Measurable, Advisable {
	public static final String PROPERTY_GROUP_GENERIC = "Generic"
	public static final String PROPERTY_GROUP_NUTRIENT = "Nutrient"
	public static final String PROPERTY_GROUP_BIOMARKER = "Biomarker"
	public static final String PROPERTY_GROUP_PHYSICAL = "Physical"
	public static final String PROPERTY_GROUP_SNP = "SNP"
	public static final String PROPERTY_GROUP_FOODGROUP = "Foodgroup"
	
	/**
	 * Group to which this property belongs. Can be nutrient, biomarker, physical, generic
	 */
	String propertyGroup
	
	/**
	 * Name of the entity
	 */
	String entity
	
	/**
	 * External identifier. See http://purl.bioontology.org/ontology/SNOMEDCT/88878007
	 */
	String externalId
	
	/**
	 * Unit required for computations in the application
	 */
	Unit unit
	
    static constraints = {
		unit nullable: true
    }
	
	/**
	 * Returns the root property for this measurable
	 * @return
	 */
	Property getRootProperty() {
		this
	}
	
	public String toString() {
		"" + entity
	}
}
