<html>
	<head>
		<meta name="layout" content="main" />
		<title>Food4me units</title>
	</head>
	<body>
		<h1>Food4me units</h1>
		
		<table id="references">
			<thead>
				<tr>
					<th>Unit</th>
					<th>Code</th>
					<th>External ID</th>
				</tr>
			</thead>
			<tbody>
				<g:each in="${units}" var="unit">
					<tr>
						<td class="unitname">
							${unit.name}
						</td>
						<td class="unitCode">
							${unit.code}
						</td>
						<td>
							${unit.externalId}
						</td>
					</tr>
				</g:each>
			</tbody>
		</table>
		
	</body>
</html>
