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

	changeSet(author: "robert (generated)", id: "1424706558711-1") {
		createTable(tableName: "advice") {
			column(name: "id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "advicePK")
			}

			column(name: "version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "code", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "num_conditions", type: "int4") {
				constraints(nullable: "false")
			}

			column(name: "position", type: "int4") {
				constraints(nullable: "false")
			}

			column(name: "subject_id", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "text", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "robert (generated)", id: "1424706558711-2") {
		createTable(tableName: "advice_condition") {
			column(name: "id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "advice_conditPK")
			}

			column(name: "version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "advice_id", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "status", type: "varchar(255)")

			column(name: "subject_id", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "value", type: "varchar(255)")
		}
	}

	changeSet(author: "robert (generated)", id: "1424706558711-3") {
		addForeignKeyConstraint(baseColumnNames: "subject_id", baseTableName: "advice", constraintName: "FK_a4r5vt8wr059kyik6havu0ua", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "property", referencesUniqueColumn: "false")
	}

	changeSet(author: "robert (generated)", id: "1424706558711-4") {
		addForeignKeyConstraint(baseColumnNames: "advice_id", baseTableName: "advice_condition", constraintName: "FK_om525oqj8djllcav53dvudxi7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "advice", referencesUniqueColumn: "false")
	}

	changeSet(author: "robert (generated)", id: "1424706558711-5") {
		addForeignKeyConstraint(baseColumnNames: "subject_id", baseTableName: "advice_condition", constraintName: "FK_luxjrtayqon7n57movwo7i8cs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "property", referencesUniqueColumn: "false")
	}
}
