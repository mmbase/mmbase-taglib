<%@ taglib uri="http://www.mmbase.org/mmbase-taglib-1.0" prefix="mm" %>
<mm:cloud name="mmbase">

<html>

<head>
  <title>Node manager info</title>
  <link href="style.css" rel="stylesheet" type="text/css" media="screen"  />
</head>

<body>

<%@ include file="menu.jsp"%>

<h1>Nodemanager info</h1>

<p>This example shows how to retrieve information from the MMBase node managers (which descrive the objecttypes). </p>

<%@include file="codesamples/nodemanagerinfo.jsp.1" %>


<hr />
<p>
It was implemented like this:
</p>
<pre><mm:formatter format="escapexml"><mm:include page="codesamples/nodemanagerinfo.jsp.1" /></mm:formatter></pre>

</body>

</html>

</mm:cloud>
