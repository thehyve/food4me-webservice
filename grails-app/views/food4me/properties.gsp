<html>
	<head>
		<meta name="layout" content="main" />
		<title>Food4me properties</title>
	</head>
	<body>
		<h1>Food4me properties</h1>
		
		<table id="references">
			<thead>
				<tr>
					<th>Group</th>
					<th>Property</th>
					<th>Unit</th>
					<th>Field names</th>
				</tr>
			</thead>
			<tbody>
				<g:each in="${properties}" var="property">
					<tr>
						<td class="propertyGroup">
							${property.propertyGroup}
						</td>
						<td class="property">
							${property.entity}
						</td>
						<td>
							${property.unit?.code}
						</td>
						<td>
							<ul>
								<li>${property.propertyGroup.toLowerCase()}.${property.entity.toLowerCase()}</li>
								<g:each in="${propertyModifiers[property]}" var="modifier">
									<li>${property.propertyGroup.toLowerCase()}.${property.entity.toLowerCase()}.${modifier.id.toLowerCase()}</li>
								</g:each>
							</ul>
						</td>
					</tr>
				</g:each>
			</tbody>
		</table>
		
	</body>
</html>
