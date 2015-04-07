<html>
	<head>
		<meta name="layout" content="main" />
		<title>Food4me references</title>
	</head>
	<body>
		<h1>Food4me references</h1>
		
		<div id="advices">
			<ul>
				<g:each in="${measurements.all.findAll { it.property.entity.toLowerCase() in secondaryConditions }}" var="measurement">
					<li>
						<strong>${measurement.property}</strong>: ${measurement.value} 
					</li>
				</g:each>
			</ul>
		</div>
		
		<table id="references">
			<thead>
				<tr>
					<th>Property</th>
					<th>Unit</th>
					<th>References</th>
				</tr>
			</thead>
			<tbody>
				<g:each in="${entities}" var="property">
					<tr>
						<td class="property">
							${property}
						</td>
						<td>
							${property.unit}
						</td>
						<td>
							<g:if test="${references[property]}">
								<g:each in="${references[property]}" var="reference">
									<span class="status color_${reference.color}">
										${reference.status} 
										${reference.subjectCondition?.conditionDescription}
									</span>
								</g:each>
							</g:if>
							<g:else>
								-
							</g:else>
						</td>
					</tr>
				</g:each>
			</tbody>
		</table>
		
	</body>
</html>
