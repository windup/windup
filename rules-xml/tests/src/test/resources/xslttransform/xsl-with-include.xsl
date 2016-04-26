<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:pom="http://maven.apache.org/POM/4.0.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:include href="../simpleXSLT.xsl"/>

    <xsl:template match="/">

        <html>
            <head>
                <title>Sample with an include</title>
            </head>
            <body>
                The include was successful
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
