<%@ taglib uri="http://www.mmbase.org/mmbase-taglib-1.0" prefix="mm"
%><html>
<head>
 <title>A simple editor</title>
 <link href="../../css/mmbase.css" rel="stylesheet" type="text/css"/>
</head>
<body>
<%@ include file="menu.jsp"%>

<mm:cloud method="http">
<mm:context> <!-- the explicit use of context tag is only necessary in orion 1.5.2 -->

<h1>Simple editor</h1>
  <h2>Creating a form</h2>
  <p>
    A form can easily be made with the use of the `fieldlist' and the
    `fieldinfo' tag. With the `fieldlist' tag a list of all the fields
    of this node can be generated, with the sub-tag `fieldinfo' then
    we show the name of the field, and create a form entry for it.
  </p>
   <h2>Handling a form</h2>
   <p>
     To handle a form that was created with the `fieldinfo' tag, you
     should also use the fieldinfo tag.
   </p>

<%@include file="codesamples/edit.jsp.1" %>


<hr />
<p>
It was implemented like this:
</p>
<pre><mm:formatter format="escapexml"><mm:include page="codesamples/edit.jsp.1" /></mm:formatter></pre>

</mm:context>
</mm:cloud>
</body>
</html>
