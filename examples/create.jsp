<%@ taglib uri="http://www.mmbase.org/mmbase-taglib-1.0" prefix="mm" %>
<mm:cloud name="mmbase" method="http">
	<mm:import externid ="nodemanager"/>
	<mm:import externid ="fieldname" />
	<mm:import externid ="create" />

<html>

<head>
  <title>Taglib examples</title>
  <link href="style.css" rel="stylesheet" type="text/css" media="screen"  />
</head>

<body>

<%@ include file="menu.jsp"%>

<h1>Create</h1>

<mm:present referid="nodemanager">
  <mm:present referid="create">
    <mm:createnode type="${nodemanager}">
      <mm:fieldlist type="edit">
		<mm:fieldinfo type="useinput"/>
      </mm:fieldlist>
    </mm:createnode>
  </mm:present>
</mm:present>

<table>
  <tr>
    <td valign="top">
      <mm:listnodes type="typedef">
        <a href="create.jsp?nodemanager=<mm:field name="name" />"><mm:field name="name" /></a><br>
      </mm:listnodes>
    </td>
    <td valign="top">
      <mm:present referid="nodemanager">
        <form>
          <input type="hidden" name="nodemanager" value='<mm:write referid="nodemanager"/>'>
          <input type="hidden" name="create" value="true">
          <table>
            <mm:fieldlist nodetype="${nodemanager}" type="edit">
              <tr>
                <td valign="top">
                  <mm:fieldinfo type="name"/>
                </td>
                <td valign="top">
                  <mm:fieldinfo type="input"/>
                </td>
              </tr>
            </mm:fieldlist>
          </table>
          <input type="submit" value="create"/>
        </form>
      </mm:present>
    </td>
  </tr>
</table>

</body>

</html>

</mm:cloud>
