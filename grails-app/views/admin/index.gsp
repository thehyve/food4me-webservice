<html>
	<head>
		<meta name="layout" content="main" />
		<title>Food4me Administration panel</title>
	</head>
	<body>
		<h1>Administration tasks</h1>
		<div id="adminPanel">
			<p>The database contains:</p>
			<ul>
				<li>${numProperties} properties and ${numUnits} units</li>
				<li>${numReferences} reference values for ${numReferenceSubjects} properties</li>
				<li>${numAdvices} advices for ${numAdviceSubjects} properties</li>
				<li>
					<g:if test="${languages}">
						(partial) translations in ${languages?.join(", ")}
					</g:if>
					<g:else>
						no translations
					</g:else>
				</li>
			</ul>
			
			<p>Import directory</p>
			<ul>
				<li>${importDirectory}</li>
			</ul>
						
			<p>Application version</p>
			<ul>
				<li>${appVersion}</li>
			</ul>			
			
		</div>
		
		<p>
			This page allows the administrator to perform certain tasks. Please use with care!
		</p>
		
		<br />
		
		<g:form action="clearAll">
			<input type="submit" value="Clear the database" />
		</g:form>
		
		<g:form action="importAll">
			<input type="submit" value="Import all data" />
		</g:form>
		
		<g:form action="importReferenceData">
			<input type="submit" value="Import reference data only" />
		</g:form>
		
		<g:form action="importDecisionTrees">
			<input type="submit" value="Import decision trees" />
		</g:form>
		
		<g:form action="importTranslations">
			<input type="submit" value="Import advice translations" />
		</g:form>	
	</body>
</html>
