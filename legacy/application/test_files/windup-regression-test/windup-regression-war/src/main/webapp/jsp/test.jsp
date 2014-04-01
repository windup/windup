	<%@ page import="net.sf.hibernate" %>
	<%@ page import="ibm.com" %>
	<%@ page import="something.oracle.com" %>
	<%@ page import="something.bea.com" %>
	<%@ page import="something.sql.oracle" %>

    <html>
      <head><title>Hello World Test</title></head>

    <body bgcolor=#ffffff>
    <center>
    <h1> <font color=#DB1260> Hello World Test </font></h1>
    <font color=navy>

    <%

        out.print("Java-generated Hello World");
    %>

    </font>
    <p> This is not Java!
    <p><i>Middle stuff on page</i>
    <p>
    <font color=navy>

    <%
         for (int i = 1; i<=3; i++) {
    %>
            <h2>This is HTML in a Java loop! <%= i %> </h2>
    <%
         }
    %>

    </font>
    </center>
    </body>
    </html>