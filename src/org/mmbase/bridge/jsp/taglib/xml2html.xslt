<?xml version="1.0"?>
<xsl:stylesheet id="xml2html" 
	version="1.1"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml"
                version="1.0"
                encoding="UTF-8"
                omit-xml-declaration="yes"
                doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
                doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
                indent="yes"
        />


<!-- some configuration -->
<xsl:variable name="extendscolor">blue</xsl:variable>
<xsl:variable name="attrcolor">green</xsl:variable>
<xsl:variable name="reqcolor">red</xsl:variable>

<!-- main entry point -->
<xsl:template match="taglib">
  <xsl:apply-templates select="tagtypes/type" />
  <!-- xsl:apply-templates select="tag|taginterface" mode="file" / -->
  <!-- create a toc file -->
   <!--
  <xsl:document href="{$basedir}toc.html">
    <html>
      <body 
        bgcolor="#FFFFFF" text="#336699" link="#336699" vlink="#336699" alink="#336699">
        <h1>MMBase taglib <xsl:value-of select="/taglib/tlibversion" /> documentation</h1>
          <xsl:for-each select="tagtypes/type">
             <a href="mmbase-taglib-{@name}.html"><xsl:value-of select="description" /></a><br />
          </xsl:for-each>
          <hr />
          All 'tags' in seperate files (these are small jsp's, probably will be possible to add working examples): <br />
          groups: <br />
          <xsl:apply-templates select="taginterface" mode="toc" >
            <xsl:sort select="name" />
            <xsl:with-param name="file"  select="true()" />
          </xsl:apply-templates><br />
          tags: <br />
          <xsl:apply-templates select="tag" mode="toc" >
            <xsl:sort select="name" />
            <xsl:with-param name="file"  select="true()" />
          </xsl:apply-templates><br />
    </body>
    </html>
  </xsl:document>
  -->
</xsl:template>

<!-- The several 'types' of tags -->
<xsl:template match="type[@name = 'all']">
 
    <xsl:apply-templates select="/taglib" mode="main">
       <xsl:with-param name="info" select="info" />
       <xsl:with-param name="type" select="@name" />
    </xsl:apply-templates>
  <!-- 
  <xsl:document href="{$basedir}mmbase-taglib-{@name}.html">
    <xsl:apply-templates select="/taglib" mode="main">
       <xsl:with-param name="info" select="info" />
       <xsl:with-param name="type" select="@name" />
    </xsl:apply-templates>
  </xsl:document>
  -->
</xsl:template>



<xsl:template match="taglib" mode="main">
  <xsl:param name="info" />
  <xsl:param name="type" />
  <html>
    <head>
      <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
      <title>MMBase taglib <xsl:value-of select="tlibversion" /> documentation</title>
      <xsl:if test="@author"><meta name="Author" value="{@author}"/></xsl:if>
    </head>
      <body 
        bgcolor="#FFFFFF" text="#336699" link="#336699" vlink="#336699" alink="#336699">
        <h1>MMBase taglib <xsl:value-of select="tlibversion" /> documentation</h1>
      <table width="100%" cellpadding="5">
        <tr>
          <td width="30"></td>
	    <td>
            <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
          </td>
          <td width="30"></td>
          </tr>
        <tr>
          <td></td>
          <td><xsl:apply-templates select="$info"/></td>
          <td></td>
        </tr>
        <tr>
          <td></td>
          <td>
            <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
          </td>
          <td></td>
        </tr>
        <tr>
          <td></td>
          <td>
            <a name="toc"/>			
            <xsl:apply-templates select="taginterface[contains(type, $type) or $type='all']" mode="toc" >
                <xsl:sort select="name" />
            </xsl:apply-templates><br />
          </td>
          <td></td>
        </tr>
        <tr>
          <td></td>
          <td>
            <a name="toc"/>			
            <xsl:apply-templates select="tag[contains(type, $type) or $type='all']" mode="toc" >
                <xsl:sort select="name" />
            </xsl:apply-templates><br />
          </td>
          <td></td>
        </tr>
        <tr>
          <td></td>
          <td>
            <a href="#docinfo">info about the syntax of this document</a>
          </td>
          <td></td>
        </tr>
        <tr>
          <td></td>
          <td>
            <xsl:apply-templates select="taginterface[contains(type, $type) or $type='all']" mode="full"><xsl:sort select="name" /></xsl:apply-templates>
            <xsl:apply-templates select="tag[contains(type, $type) or $type='all']" mode="full"><xsl:sort select="name" /></xsl:apply-templates>
          </td>
          <td></td>
        </tr>
        <tr>
          <td></td>
          <td>
            <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
          </td>
          <td></td>
        </tr>
         <tr>
          <td></td>
          <td>
            <a name="docinfo"/>
            <p>
            This document lists the current tags implemented for MMBase (version <xsl:value-of
            select="/taglib/tlibversion" />)
	    </p>
            <p>
                Attributes in <font color="{$reqcolor}"><xsl:value-of select="$reqcolor" /></font> are
                required.
	    </p>
              <p>
                <font color="{$extendscolor}"><xsl:value-of select="$extendscolor" /></font> entries
                are no tags, but describe a group of tags. Tags can belong to several groups.
              </p>
	    <p>
                If a tag definition contains a body section this means that the
                tag might do something with the content of the body.
              </p>
          </td>
          <td></td>
        </tr>
      </table>
    </body>
  </html>
</xsl:template>

<xsl:template match="tag|taginterface" mode="toc">
  <a href="#{name}">
   <xsl:if test="name()='taginterface'"><font color="{$extendscolor}"><xsl:value-of select="name" /></font></xsl:if>
   <xsl:if test="name()='tag'"><xsl:value-of select="name" /></xsl:if>
  </a>
  <xsl:if test="position() != last()"> | </xsl:if>
</xsl:template>

<xsl:template match="tag|taginterface" mode="tocext">
  <xsl:param name="testlast">false</xsl:param>
  <xsl:apply-templates select="/taglib/*[name()='tag' or name()='taginterface']/extends[.=current()/name]/parent::*" mode="tocext" />
   <xsl:if test="name()='tag'">
     <a href="#{name}">
     <xsl:value-of select="name" />  
     </a>
     <xsl:if test="(position() != last()) or ($testlast='false')"> | </xsl:if>     
   </xsl:if>
</xsl:template>

<xsl:template match="see">
  <a href="#{.}">
      <xsl:value-of select="." />
  </a>
  <xsl:if test="position() != last()"> | </xsl:if>
</xsl:template>

<!-- Create a file for a tag -->
<xsl:template match="tag|taginterface" mode="file">
  <xsl:document href="{$basedir}{name}.jsp">
  <html>
  <body>
  <h1><xsl:value-of select="name" /></h1>
   <xsl:apply-templates select="." mode="full" />
  </body>
  </html>
  </xsl:document>
</xsl:template>


<xsl:template match="tag|taginterface" mode="full">
  
  <table bgcolor="#eeeeee" width="100%" cellpadding="5">
    <tr>
      <td colspan="2" bgcolor="white" align="right">
        <a name="{name}"/>
        <a href="#toc">toc</a>
      </td>
    </tr>
    <tr>
      <td colspan="2">
        <xsl:if test="name()='tag'"><b>&lt;mm:<xsl:value-of select="name"/>&gt;</b></xsl:if>
        <xsl:if test="name()='taginterface'"><b><font color="{$extendscolor}">`<xsl:value-of select="name"/>' tags</font></b></xsl:if>
        <br />
        <xsl:apply-templates select="info"/>
      </td>
    </tr>
    <xsl:if test="see">
      <tr>
        <td width="100" valign="top">see also</td>
        <td>
          <xsl:apply-templates select="see"  />
        </td>
      </tr>
    </xsl:if>
    <xsl:if test="attribute">
      <tr>
        <td width="100" valign="top">attributes</td>
        <td>
          <ul><xsl:apply-templates select="attribute" mode="full" /></ul>
        </td>
      </tr>
    </xsl:if>
    
    <xsl:apply-templates select="extends" />
    <xsl:if test="xxbodycontent">
      <tr>
        <td width="100" valign="top">body</td>
        <td>
          <xsl:value-of select="bodycontent"/>
          <br />
          <xsl:apply-templates select="bodycontentinfo"/>
        </td>
      </tr>
    </xsl:if>       
	<xsl:if test="name()='taginterface'">
	<tr>
    <td>tags of this type</td><td>
           <xsl:apply-templates
              select="/taglib/*[name()='tag' or name()='taginterface']/extends[.=current()/name]/parent::*" 
              mode="tocext" >
              <xsl:with-param name="testlast">true</xsl:with-param>
              <xsl:sort select="name" />
            </xsl:apply-templates>
     </td>
    </tr>  
    </xsl:if>

    <xsl:apply-templates select="example" />
  </table>
</xsl:template>

<xsl:template match="example">
   <tr>
     <td width="100" valign="top">example</td>
     <td>
        <pre><xsl:value-of select="."/></pre>
     </td>
   </tr>
</xsl:template>

<xsl:template match="extends">
  <tr>
      <td width="100" valign="top"><xsl:if test="/taglib/taginterface/name[.=current()]"><a href="#{current()}"><font color="{$extendscolor}"><xsl:value-of select="." /></font></a></xsl:if> attributes</td>
	  <td>
	  <xsl:if test="/taglib/*[name()='tag' or name()='taginterface']/name[.=current()]/parent::*/attribute">
      <ul>   	   
       <xsl:apply-templates select="/taglib/*[starts-with(name(), 'tag')]/name[.=current()]/parent::*/attribute"  mode="extends" />
      </ul>
      </xsl:if>
	  </td>
   </tr>
   <xsl:apply-templates select="/taglib/*[name()='tag' or name()='taginterface']/name[.=current()]/parent::*/extends"  />
</xsl:template>

<xsl:template match="attribute" mode="extends">
   <li><a href="#{parent::*/name}.{name}"><font color="{$attrcolor}"><xsl:value-of select="name" /></font></a></li>
</xsl:template>

<xsl:template match="attribute" mode="full">
  <li>
    <a name="{parent::*/name}.{name}" />
    <xsl:choose>
      <xsl:when test="requirednote">
        <xsl:if test="requirednote">
          <font color="ff9900"><xsl:apply-templates select="name"/></font>
          <font color="black" size="-1"> (<xsl:value-of select="requirednote"/>)</font>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="required='true'">
            <font color="{$reqcolor}"><xsl:apply-templates select="name"/></font>
          </xsl:when>
          <xsl:otherwise>
            <font color="{$attrcolor}"><xsl:apply-templates select="name"/></font>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>      
    </xsl:choose>
    <br />
    <xsl:if test="info">
      <xsl:apply-templates select="info"/>
    </xsl:if>
    <xsl:if test="possiblevalue">
      <table bgcolor="#99ccff">
        <xsl:apply-templates select="possiblevalue"/>
      </table>
    </xsl:if>
    <xsl:if test="examplevalue">
      <table bgcolor="#99ffff">
        <xsl:apply-templates select="examplevalue"/>
      </table>
    </xsl:if>
  </li>
</xsl:template>

<xsl:template match="possiblevalue">
  <tr>
    <td valign="top"><b><xsl:value-of select="value"/></b></td>
    <td valign="top"><xsl:apply-templates select="info"/></td>
  </tr>
</xsl:template>

<xsl:template match="examplevalue">
  <tr>
    <td valign="top"><b><xsl:value-of select="value"/></b></td>
    <td valign="top"><xsl:apply-templates select="info"/></td>
  </tr>
</xsl:template>

<xsl:template match="info">
  <xsl:apply-templates select="p|text()|em|a" />  
</xsl:template>

<xsl:template match="p|text()|a">
  <xsl:copy-of select="." />
</xsl:template>

<xsl:template match="em">
	<xsl:copy>
   	  <xsl:apply-templates />
	</xsl:copy>
</xsl:template>

</xsl:stylesheet>
