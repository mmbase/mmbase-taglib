<mm:formatter escape="none">
  <mm:node />
  <mm:xslt>
    <xsl:import href="file:xslt/2xhtml.xslt" />
    <xsl:template match="field[@name='title']" >
      <h1><font color="blue">
        <xsl:value-of select="." />
      </font></h1>
    </xsl:template>
  </mm:xslt>
</mm:formatter>

