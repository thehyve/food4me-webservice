databaseChangeLog = {

	changeSet(author: "robert (generated)", id: "1426000518904-1") {
		modifyDataType(columnName: "text", newDataType: "text", tableName: "advice")
	}

	changeSet(author: "robert (generated)", id: "1426000518904-2") {
		dropNotNullConstraint(columnDataType: "text", columnName: "text", tableName: "advice")
	}
}
