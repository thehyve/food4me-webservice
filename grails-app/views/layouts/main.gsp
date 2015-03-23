<!DOCTYPE html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
		<title><g:layoutTitle default="QualiFY Food4me configuration"/></title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		
		<link rel="apple-touch-icon" sizes="57x57" href="${assetPath(src: 'icon/apple-icon-57x57.png')}">
		<link rel="apple-touch-icon" sizes="60x60" href="${assetPath(src: 'icon/apple-icon-60x60.png')}">
		<link rel="apple-touch-icon" sizes="72x72" href="${assetPath(src: 'icon/apple-icon-72x72.png')}">
		<link rel="apple-touch-icon" sizes="76x76" href="${assetPath(src: 'icon/apple-icon-76x76.png')}">
		<link rel="apple-touch-icon" sizes="114x114" href="${assetPath(src: 'icon/apple-icon-114x114.png')}">
		<link rel="apple-touch-icon" sizes="120x120" href="${assetPath(src: 'icon/apple-icon-120x120.png')}">
		<link rel="apple-touch-icon" sizes="144x144" href="${assetPath(src: 'icon/apple-icon-144x144.png')}">
		<link rel="apple-touch-icon" sizes="152x152" href="${assetPath(src: 'icon/apple-icon-152x152.png')}">
		<link rel="apple-touch-icon" sizes="180x180" href="${assetPath(src: 'icon/apple-icon-180x180.png')}">
		<link rel="icon" type="image/png" sizes="192x192"  href="${assetPath(src: 'icon/android-icon-192x192.png')}">
		<link rel="icon" type="image/png" sizes="32x32" href="${assetPath(src: 'icon/favicon-32x32.png')}">
		<link rel="icon" type="image/png" sizes="96x96" href="${assetPath(src: 'icon/favicon-96x96.png')}">
		<link rel="icon" type="image/png" sizes="16x16" href="${assetPath(src: 'icon/favicon-16x16.png')}">
		<link rel="manifest" href="${assetPath(src: 'icon/manifest.json')}">
		<meta name="msapplication-TileColor" content="#ffffff">
		<meta name="msapplication-TileImage" content="${assetPath(src: '/ms-icon-144x144.png')}">
		<meta name="theme-color" content="#ffffff">		
		
  		<asset:stylesheet src="application.css"/>
		<asset:javascript src="application.js"/>
		<g:layoutHead/>
	</head>
	<body>
		<div id="qualifyLogo" role="banner"><a href="http://grails.org"><asset:image src="qualify-logo.png" alt="Qualify"/></a></div>
		<div id="mainbody">
			<g:if test="${flash.message}">
				<div class="message">
					${flash.message}
				</div>
			</g:if>
		
			<g:layoutBody/>
		</div>
		<div class="footer" role="contentinfo"></div>
		<div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt" default="Loading&hellip;"/></div>
	</body>
</html>
