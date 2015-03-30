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

	changeSet(author: "robert (generated)", id: "1424689802472-1") {
		createTable(tableName: "property") {
			column(name: "id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "propertyPK")
			}

			column(name: "version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "entity", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "external_id", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "property_group", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "unit_id", type: "int8")
		}
	}

	changeSet(author: "robert (generated)", id: "1424689802472-2") {
		createTable(tableName: "reference_condition") {
			column(name: "id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "reference_conPK")
			}

			column(name: "version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "high", type: "numeric(19, 2)")

			column(name: "low", type: "numeric(19, 2)")

			column(name: "reference_value_id", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "subject_id", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "value", type: "varchar(255)")
		}
	}

	changeSet(author: "robert (generated)", id: "1424689802472-3") {
		createTable(tableName: "reference_value") {
			column(name: "id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "reference_valPK")
			}

			column(name: "version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "color", type: "int4") {
				constraints(nullable: "false")
			}

			column(name: "num_conditions", type: "int4") {
				constraints(nullable: "false")
			}

			column(name: "status", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "subject_id", type: "int8") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "robert (generated)", id: "1424689802472-4") {
		createTable(tableName: "unit") {
			column(name: "id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "unitPK")
			}

			column(name: "version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "code", type: "varchar(255)")

			column(name: "external_id", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "robert (generated)", id: "1424689802472-9") {
		createSequence(sequenceName: "hibernate_sequence")
	}

	changeSet(author: "robert (generated)", id: "1424689802472-5") {
		addForeignKeyConstraint(baseColumnNames: "unit_id", baseTableName: "property", constraintName: "FK_i85q0go98qy6ituoafeceppk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "unit", referencesUniqueColumn: "false")
	}

	changeSet(author: "robert (generated)", id: "1424689802472-6") {
		addForeignKeyConstraint(baseColumnNames: "reference_value_id", baseTableName: "reference_condition", constraintName: "FK_tqxd16ux82coojvuurnc6xm09", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "reference_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "robert (generated)", id: "1424689802472-7") {
		addForeignKeyConstraint(baseColumnNames: "subject_id", baseTableName: "reference_condition", constraintName: "FK_shuqqijrdd815mbmvqtodbbaq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "property", referencesUniqueColumn: "false")
	}

	changeSet(author: "robert (generated)", id: "1424689802472-8") {
		addForeignKeyConstraint(baseColumnNames: "subject_id", baseTableName: "reference_value", constraintName: "FK_ho18l91utioiu5rffexavwi2j", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "property", referencesUniqueColumn: "false")
	}
	
	// Please note, creating the trigger is currently only supported on postgres
	include file: 'trigger-on-reference-conditions.groovy'

	include file: 'initial-advices.groovy'
	
	// Please note, creating the trigger is currently only supported on postgres
	include file: 'trigger-on-advice-conditions.groovy'

	include file: 'add-modifier-to-advice-condition.groovy'

	include file: 'advice-text-large-than-255.groovy'

	include file: 'advice-translations.groovy'

	include file: 'advice-text-nullable.groovy'

	include file: 'advice-text-separate-from-advices.groovy'

	include file: 'authorization.groovy'

	include file: 'reference-type.groovy'
}
