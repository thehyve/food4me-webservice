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

	changeSet(author: "robert (generated)", id: "1425997968861-1") {
		createTable(tableName: "advice_text") {
			column(name: "id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "advice_textPK")
			}

			column(name: "version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "advice_id", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "code", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "language", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "text", type: "text") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "robert (generated)", id: "1425997968861-2") {
		addForeignKeyConstraint(baseColumnNames: "advice_id", baseTableName: "advice_text", constraintName: "FK_nifwnyxkbetnn485dy8x54cs0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "advice", referencesUniqueColumn: "false")
	}
}
