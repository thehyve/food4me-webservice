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
					<li>
						<strong>${advice.code}</strong>: 
							<g:lines string="${translations[advice.code]}" />
					</li>
				</g:each>
			</ul>
		</div>
		
		<h1>Provided measurements</h1>
		<div class="measurements">
			<ul>
				<g:each in="${measurements?.all}" var="measurement">
					<g:if test="${!measurement.derived}">
						<li>
							<span class='property'>${measurement.property}</span>
							<span class='value'>${measurement.value}</span>
							
							<% def propertyStatus = status.getStatus(measurement.property) %>
							<g:if test="${propertyStatus}">
								<span class="status color_${propertyStatus.color}">${propertyStatus.status}</span>
							</g:if>
						</li>
					</g:if>
				</g:each>
			</ul>
		</div>
		
		<h1>Derived measurements</h1>
		<div class="measurements">
			<ul>
				<g:each in="${measurements?.all}" var="measurement">
					<g:if test="${measurement.derived}">
						<li>
							<span class='property'>${measurement.property}</span>
							<span class='value'>${measurement.value}</span>
							
							<% def propertyStatus = status.getStatus(measurement.property) %>
							<g:if test="${propertyStatus}">
								<span class="status color_${propertyStatus.color}">${propertyStatus.status}</span>
							</g:if>
						</li>
					</g:if>
				</g:each>
			</ul>
		</div>		
	</body>
</html>
