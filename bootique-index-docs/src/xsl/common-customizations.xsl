<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:param name="keep.relative.image.uris" select="1"/>
	<xsl:param name="toc.section.depth">1</xsl:param>

	<!-- disable TOC for each part -->
	<xsl:template name="generate.part.toc" />

</xsl:stylesheet>
