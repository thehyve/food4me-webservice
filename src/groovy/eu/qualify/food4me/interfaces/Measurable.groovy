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
package eu.qualify.food4me.interfaces

import eu.qualify.food4me.Property

interface Measurable {
	/**
	 * Returns the root property for this measurable
	 * @return
	 */
	Property getRootProperty()
	
	/**
	 * Returns the property to be used to retrieve references for this property
	 * 
	 * Can be used on modified properties to use the references of the root property
	 * @return
	 */
	Property getReferenceProperty()
}
