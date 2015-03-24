<html>
	<head>
		<meta name="layout" content="main" />
		<title>Food4me advices</title>
	</head>
	<body>
		<h1>Generate food4me advice</h1>
		
		<g:form action="advices" name="generate_advices" method="get">
			<fieldset>
				<legend>Nutrients</legend>
				
				<table>
					<thead>
						<th></th>
						<g:each in="${nutrientModifiers}" var="modifier">
							<th title="${modifier.id}">${modifier.id}</th>
						</g:each>
					</thead>
					<tbody>
						<g:each in="${nutrients}" var="nutrient">
							<tr>
								<td>
									<label>
										${nutrient.entity}
										<g:if test="${nutrient.unit}">
											(${nutrient.unit.code})
										</g:if>
									</label>
								</td>
								<g:each in="${nutrientModifiers}" var="modifier">
									<td>
										<input id="property_${nutrient.id}" type="text" name="nutrient.${nutrient.entity}.${modifier.id}" />
									</td>
								</g:each>
							</tr>
						</g:each>
					</tbody>
				</table>
			</fieldset>
		
		
			<g:each in="${properties}" var="propertygroup">
				<fieldset>
					<legend>${propertygroup.key}</legend>
					<ul>
						<g:each in="${propertygroup.value}" var="property">
							<li>
								<label for="property_${property.id}">
									${property.entity}
									<g:if test="${property.unit}">
										(${property.unit.code})
									</g:if>
								</label>
								<input id="property_${property.id}" type="text" name="${conversionMap[propertygroup.key]}.${property.entity}" />
							</li>
						</g:each>
					</ul>
				</fieldset>
			</g:each>
			
			<g:submitButton name="Retrieve advice" />
		</g:form>
	</body>
</html>
