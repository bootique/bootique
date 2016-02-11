<?xml version="1.0" encoding="ASCII"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:d="http://docbook.org/ns/docbook"
                xmlns:xslthl="http://xslthl.sf.net" xmlns="http://www.w3.org/1999/xhtml" exclude-result-prefixes="xslthl d" version="1.0">

    <xsl:import href="urn:docbkx:stylesheet/highlight.xsl"/>
    <!-- <xsl:import href="urn:/highlighting/common.xsl"/> -->
    <xsl:template match="xslthl:keyword" mode="xslthl">
        <span class="hl-keyword">
            <xsl:apply-templates mode="xslthl"/>
        </span>
    </xsl:template>
    <xsl:template match="xslthl:string" mode="xslthl">
        <span class="hl-string">
            <xsl:apply-templates mode="xslthl"/>
        </span>
    </xsl:template>
    <xsl:template match="xslthl:comment" mode="xslthl">
        <span class="hl-comment">
            <xsl:apply-templates mode="xslthl"/>
        </span>
    </xsl:template>
    <xsl:template match="xslthl:directive" mode="xslthl">
        <span class="hl-directive">
            <xsl:apply-templates mode="xslthl"/>
        </span>
    </xsl:template>
    <xsl:template match="xslthl:tag" mode="xslthl">
        <span class="hl-tag">
            <xsl:apply-templates mode="xslthl"/>
        </span>
    </xsl:template>
    <xsl:template match="xslthl:attribute" mode="xslthl">
        <span class="hl-attribute">
            <xsl:apply-templates mode="xslthl"/>
        </span>
    </xsl:template>
    <xsl:template match="xslthl:value" mode="xslthl">
        <span class="hl-value">
            <xsl:apply-templates mode="xslthl"/>
        </span>
    </xsl:template>
    <xsl:template match="xslthl:html" mode="xslthl">
        <strong>
            <span>
                <xsl:apply-templates mode="xslthl"/>
            </span>
        </strong>
    </xsl:template>
    <xsl:template match="xslthl:xslt" mode="xslthl">
        <span>
            <xsl:apply-templates mode="xslthl"/>
        </span>
    </xsl:template>
    <!-- Not emitted since XSLTHL 2.0 -->
    <xsl:template match="xslthl:section" mode="xslthl">
        <span>
            <xsl:apply-templates mode="xslthl"/>
        </span>
    </xsl:template>
    <xsl:template match="xslthl:number" mode="xslthl">
        <span class="hl-number">
            <xsl:apply-templates mode="xslthl"/>
        </span>
    </xsl:template>
    <xsl:template match="xslthl:annotation" mode="xslthl">
        <span class="hl-annotation">
            <xsl:apply-templates mode="xslthl"/>
        </span>
    </xsl:template>
    <!-- Not sure which element will be in final XSLTHL 2.0 -->
    <xsl:template match="xslthl:doccomment|xslthl:doctype" mode="xslthl">
        <strong class="hl-tag">
            <xsl:apply-templates mode="xslthl"/>
        </strong>
    </xsl:template>
</xsl:stylesheet>