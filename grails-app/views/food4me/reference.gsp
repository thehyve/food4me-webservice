<html>
	<head>
		<meta name="layout" content="main" />
		<title>Food4me reference</title>
	</head>
	<body>
		<h1>Food4me reference</h1>
		
		<table id="references">
			<thead>
				<tr>
					<th>Property</th>
					<th>Status</th>
					<th>Condition(s)</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>${reference.subject}</td>
					<td><span class="status color_${reference.color}">${reference.status}</span></td>
					<td>
						<g:if test="${reference.conditions}">
							<ul>
								<g:each var="condition" in="${reference.conditions}">
									<li>${condition.subject} ${condition.conditionDescription}</li>
								</g:each>
							</ul>
						</g:if>
						<g:else>
							Always valid
						</g:else>
					</td>
				</tr>
			</tbody>
		</table>
		
	</body>
</html>
