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
import eu.qualify.food4me.measurements.MeasurementStatus
import eu.qualify.food4me.measurements.Measurements
import grails.transaction.Transactional

/**
 * This service will return a list of all properties that have a status
 * This will result in a list of advices for all decision trees for which we have enough information
 * @author robert
 *
 */
@Transactional
class AllParametersAdvisableService implements AdvisableDeterminer {
	@Override
	List<Advisable> determineAdvisables(MeasurementStatus measurementStatus, Measurements measurements ) {
		measurementStatus.all.findResults {
			it.entity instanceof Property && it.status ? it.entity : null
		}
	}
}
