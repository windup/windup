<html>
	<head>
		<title>JBoss Windup: ${archiveReport.relativePathFromRoot}</title>
		<meta http-equiv="Content-Type" content="text/html;charset=utf-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge">
		<link rel="stylesheet" type="text/css" href="windup.css">
		<script type='text/javascript' src='jquery.min.js'></script>
		<script type='text/javascript' src='flot/jquery.flot.min.js'></script>
		<script type='text/javascript' src='flot/jquery.flot.pie.min.js'></script>
		<link rel='icon' type='image/png' href='img/favicon.png'/>
		<script type='text/javascript' src='jquery-collapse/jquery.collapse.js'></script>
		<script type='text/javascript' src='jquery-collapse/jquery.collapse_storage.js'></script>
		<script type='text/javascript' src='jquery-collapse/jquery.collapse_cookie_storage.js'></script>
		<script type='text/javascript' src='windup.js'></script>
	</head>
	
	<body class='windupReport'>
		<div class='windupHeader'><img src='img/windup-logo.png'/></div>
		<div class='windupWhiteBorder'>
			<#include "summary-report.ftl">
		</div>
		
		<div class='windupFooter'>
			<a href='http://redhat.com'><img src='img/rh-logo.png'/></a>
		</div>
	</body>
</html>