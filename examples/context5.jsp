<%@ taglib uri="http://www.mmbase.org/mmbase-taglib-1.0" prefix="mm" 
%><html>
<head>
  <title>context 5</title>
  <link href="../../css/mmbase.css" rel="stylesheet" type="text/css"/>
</head>
<body>
<h1>context 5</h1>
<mm:import externid="alias" required="true" />
<p>
Perhaps an example with transactions?
</p>
<hr />
<a href='<mm:url page="context.jsp">
         <mm:param name="haj"><mm:write referid="alias" />(again)</mm:param>
	 </mm:url>'>back</a>
</body>
</html>
