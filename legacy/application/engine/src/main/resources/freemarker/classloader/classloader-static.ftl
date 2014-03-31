<html>
	<head>
		<meta http-equiv="X-UA-Compatible" content="IE=edge">
		
		<script type='text/javascript' src='${relativePath}jquery.min.js'></script>
		<script type='text/javascript' src='${relativePath}jquery-ui/jquery.ui.widget.js'></script>
		<script type='text/javascript' src='${relativePath}snippet/jquery.snippet.min.js'></script>
		<script type='text/javascript' src='${relativePath}snippet/jquery.snippet.java-manifest.js'></script>
		<script type='text/javascript' src='${relativePath}sausage/jquery.sausage.min.js'></script>

		<link rel='stylesheet' type='text/css' href='${relativePath}snippet/jquery.snippet.min.css' />
		<link rel='stylesheet' type='text/css' href='${relativePath}windup.css' />

		<link rel='stylesheet' type='text/css' href='${relativePath}sausage/sausage.css' />
		<title>Classloader Report</title>
	</head>
	<body class='sourceReport'>
		<div class="windupHeader"><img src="${relativePath}img/windup-logo.png"></div>
		
		<#include "classloader-report.ftl">
		
		<script type='text/javascript' src='${relativePath}windup.js'></script>
		<div class='windupFooter'>
			<a href='http://redhat.com'><img src='${relativePath}img/rh-logo.png'/></a>
		</div>

	</body>
</html>