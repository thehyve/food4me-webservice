<html>
	<head>
		<meta name="layout" content="main" />
		<title>Food4me advices</title>
	</head>
	<body>
		<h1>Food4me advices (${advices.size()})</h1>
		
		<div id="advices">
			<ul>
				<g:each in="${advices}" var="advice">
					<li><strong>${advice.code}</strong>: ${translations[advice.code]}</li>
				</g:each>
			</ul>
		</div>
		
		<h1>Provided measurements</h1>
		<div id="measurements">
			<ul>
				<g:each in="${measurements?.all}" var="measurement">
					<g:if test="${!measurement.derived}">
						<li>${measurement.property}: ${measurement.value}</li>
					</g:if>
				</g:each>
			</ul>
		</div>
	</body>
</html>
