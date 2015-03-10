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
