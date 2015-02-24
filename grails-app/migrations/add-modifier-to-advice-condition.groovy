databaseChangeLog = {

	changeSet(author: "robert (generated)", id: "1424767372089-1") {
		addColumn(tableName: "advice_condition") {
			column(name: "modifier", type: "varchar(255)")
		}
	}
}
