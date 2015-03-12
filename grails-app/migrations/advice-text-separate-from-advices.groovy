databaseChangeLog = {

	changeSet(author: "robert (generated)", id: "1426015404449-1") {
		dropForeignKeyConstraint(baseTableName: "advice_text", baseTableSchemaName: "public", constraintName: "FK_nifwnyxkbetnn485dy8x54cs0")
	}

	changeSet(author: "robert (generated)", id: "1426015404449-2") {
		createIndex(indexName: "Code_index", tableName: "advice_text") {
			column(name: "code")
		}
	}

	changeSet(author: "robert (generated)", id: "1426015404449-3") {
		dropColumn(columnName: "text", tableName: "advice")
	}

	changeSet(author: "robert (generated)", id: "1426015404449-4") {
		dropColumn(columnName: "advice_id", tableName: "advice_text")
	}
}
