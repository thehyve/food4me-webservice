<html>
	<head>
		<meta name="layout" content="main" />
		<title>Food4me Consistency check</title>
	</head>
	<body>
		<g:if test="${missingReferences}">
			<h1>Missing references</h1>
			<p>
				The database contains decision trees dependent on some properties,
				for which we cannot determine a reference. This is not necessarily 
				problematic, but the system will never generate those advices
			</p>
			<ul>
				<g:each in="${missingReferences}" var="reference">
					<li>${reference}</li>
				</g:each>
			</ul>
		</g:if>

		<g:if test="${missingCodes}">
			<g:each in="${missingCodes}">
				<g:if test="${it.value}">
					<h1>Missing texts for ${it.key}</h1>
					<p>
						The database contains advices for which we miss some texts in <strong>${it.key}</strong>.
					</p>
					<ul>
						<g:each in="${it.value}" var="missingCode">
							<li>${missingCode}</li>
						</g:each>
					</ul>
				</g:if>
			</g:each>
		</g:if>

	</body>
</html>
