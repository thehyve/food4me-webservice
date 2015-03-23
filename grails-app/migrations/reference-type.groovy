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
