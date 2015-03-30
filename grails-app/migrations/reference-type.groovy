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
databaseChangeLog = {

	changeSet(author: "robert (generated)", id: "1427116095832-1") {
		addColumn(tableName: "reference_condition") {
			column(name: "condition_type", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "robert (generated)", id: "1427116095832-2") {
		modifyDataType(columnName: "account_expired", newDataType: "boolean", tableName: "users")
	}

	changeSet(author: "robert (generated)", id: "1427116095832-3") {
		modifyDataType(columnName: "account_locked", newDataType: "boolean", tableName: "users")
	}

	changeSet(author: "robert (generated)", id: "1427116095832-4") {
		modifyDataType(columnName: "enabled", newDataType: "boolean", tableName: "users")
	}

	changeSet(author: "robert (generated)", id: "1427116095832-5") {
		modifyDataType(columnName: "password_expired", newDataType: "boolean", tableName: "users")
	}
}
