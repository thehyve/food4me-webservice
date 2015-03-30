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
package eu.qualify.food4me.algorithm.SOP3

import eu.qualify.food4me.Property
import eu.qualify.food4me.interfaces.Advisable
import eu.qualify.food4me.interfaces.AdvisableDeterminer
import eu.qualify.food4me.interfaces.Measurable
import eu.qualify.food4me.measurements.MeasurementStatus
import eu.qualify.food4me.measurements.Measurements
import eu.qualify.food4me.measurements.Status
import grails.transaction.Transactional

@Transactional
class DetermineAdvisableService implements AdvisableDeterminer {
	// TODO: Improve performance
	@Lazy
	protected List<Property> geneRiskProperties = {[
		Property.findByEntity( "Omega-3 index" ),
		Property.findByEntity( "Saturated fat" ),
		Property.findByEntity( "Total fat" ),
		Property.findByEntity( "Folate" )
	].findAll() }()
	
	@Override
	public List<Advisable> determineAdvisables(MeasurementStatus measurementStatus, Measurements measurements ) {
		List<Advisable> advisables = []
		
		// Get the highest priority property from each of the groups
		for( int group = 1; group <= 3; group++ ) {
			def advisable = getAdvisableFromGroup( measurementStatus, group )
			log.trace "Advisable for group " + group + ": " + advisable
			if( advisable )
				advisables << advisable
		}
		
		// If no properties are found, return immediately
		if( !advisables )
			return advisables
		
		// If non-gene-risk nutrients have a higher colour priority (red) than 
		// gene-risk nutrients, you prioritise 2 red non-gene-risk nutrients 
		// then a third advice will be based on a gene-risk nutrient
		if( !advisables.findAll { isGeneRiskProperty( it ) } ) {
			addGeneRiskPropertyToAdvisables( advisables, measurementStatus )
		}
		
		advisables
	}
	
	/**
	 * Adds a 'gene-risk' advisable to the list of advisables. If there are initially 3
	 * or more advisables, the last one is replaced. 
	 * @param advisables
	 */
	protected void addGeneRiskPropertyToAdvisables( List<Advisable> advisables, MeasurementStatus measurementStatus ) {
		// Retrieve all statusses that have to do with gene risk properties
		// TODO: Improve performance
		def geneRiskStatusses = measurementStatus.all.findAll { isGeneRiskProperty( it.entity ) }
		
		// Find a geneRiskProperty with the highest severity
		if( geneRiskStatusses ) {
			def sortedStatusses = geneRiskStatusses.sort { a, b ->
				if( a.color == b.color ) {
					// With equal color/severity, order by index within the list ascending
					geneRiskProperties.indexOf( a.measurable ) <=> geneRiskProperties.indexOf( b.measurable )
				} else {
					// Order on color/severity descending
					b.color <=> a.color
				}
			}
			
			// Replace the last item with the highest priority gene risk property (if there are three items)
			// or add the item otherwise
			if( advisables.size() >= 3 )
				advisables.pop()
				
			log.trace "  Adding gene risk property " + sortedStatusses[0].entity 
				
			advisables << sortedStatusses[0].entity
		}

	}
		
	/**
	 * Determines whether a given property is a gene risk property	
	 * @param p
	 * @return
	 */
	protected boolean isGeneRiskProperty(Measurable p) {
		geneRiskProperties.contains(p)
	}
	
	/**
	 * Returns the selected advisable from a given group of properties
	 * @param measurementStatus
	 * @param group
	 * @return
	 */
	protected Measurable getAdvisableFromGroup( MeasurementStatus measurementStatus, int group ) {
		// Retrieve the properties within this group
		def groupProperties = getGroupProperties( group )	
		
		// First filter the list based on the given group and the status is not OK
		def filteredList = measurementStatus.all.findAll { it.status != Status.STATUS_OK && it.entity in groupProperties }
		
		if( !filteredList )
			return null
		
		// Order the list on severity, and if severity is equal 
		// on the position in the list of group properties
		def sortedList = filteredList.sort { a, b ->
			if( a.color == b.color ) {
				// With equal color/severity, order by index within the group ascending
				groupProperties.indexOf( a.entity ) <=> groupProperties.indexOf( b.entity )
			} else {
				// Order on color/severity descending
				b.color <=> a.color
			}
		}

		// Return the first item from this groups properties
		sortedList[0].entity
	}
	
	/**
	 * Returns an ordered list with properties within each group
	 * @param group
	 * @return
	 */
	protected List<Measurable> getGroupProperties( int group ) {
		switch( group ) {
			case 1:
				return [
					Property.findByEntity( "Cholesterol" ),
					Property.findByEntity( "Omega-3 index" ),
					Property.findByEntity( "Saturated fat" ),
					Property.findByEntity( "Total fat" ),
					Property.findByEntity( "Monounsaturated fat" ),
					Property.findByEntity( "Polyunsaturated fat" ),
				].findAll()
			case 2:
				return [
					Property.findByEntity( "Carotenoids" ),
					Property.findByEntity( "Folate" ),
					Property.findByEntity( "Fibre" ),
					Property.findByEntity( "Salt" ),
					Property.findByEntity( "B12" ),
					Property.findByEntity( "Riboflavin" ),
					Property.findByEntity( "Thiamin" ),
					Property.findByEntity( "Protein" ),
					Property.findByEntity( "Carbohydrate" ),
				].findAll()
			case 3:
				return [
					Property.findByEntity( "Calcium" ),
					Property.findByEntity( "Iron" ),
					Property.findByEntity( "Vitamin C" ),
					Property.findByEntity( "Vitamin A" ),
				].findAll()
			default:
				throw new Exception( "Only three groups with properties are known.")
		}	

	}
}
