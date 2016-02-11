<?xml version="1.0" encoding="UTF-8"?>
<!--
	Licensed to the Apache Software Foundation (ASF) under one
	or more contributor license agreements.  See the NOTICE file
	distributed with this work for additional information
	regarding copyright ownership.  The ASF licenses this file
	to you under the Apache License, Version 2.0 (the
	"License"); you may not use this file except in compliance
	with the License.  You may obtain a copy of the License at
	
	http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an
	"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
	KIND, either express or implied.  See the License for the
	specific language governing permissions and limitations
	under the License.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0" xmlns:d="http://docbook.org/ns/docbook">


    <xsl:import href="urn:docbkx:stylesheet"/>
    <xsl:import href="highlight.xsl"/>
    <xsl:include href="common-customizations.xsl"/>

    <!--<xsl:param name="highlight.source" select="1"/>-->
    <xsl:param name="html.stylesheet" select="'css/doc.css'"/>
    <xsl:param name="chunker.output.encoding">UTF-8</xsl:param>

    <!-- Only chapters start a new page -->
    <xsl:param name="chunk.section.depth">0</xsl:param>

    <!-- Don't add any embedded styles -->
    <xsl:param name="css.decoration">0</xsl:param>

    <xsl:param name="ignore.image.scaling">1</xsl:param>

    <xsl:param name="use.id.as.filename">1</xsl:param>

    <xsl:param name="navig.showtitles">1</xsl:param>

    <!--
        BODY > HEAD Customization
        Customized template for generate meta tags with framework version in head of
        page with documentation
    -->
    <xsl:template name="head.content">
        <xsl:param name="node" select="."/>
        <xsl:param name="title">
            <xsl:apply-templates select="$node" mode="object.title.markup.textonly"/>
        </xsl:param>

        <title>
            <xsl:copy-of select="$title"/>
        </title>

        <xsl:if test="$html.base != ''">
            <base href="{$html.base}"/>
        </xsl:if>

        <!-- Insert links to CSS files or insert literal style elements -->
        <xsl:call-template name="generate.css"/>

        <xsl:if test="$html.stylesheet != ''">
            <xsl:call-template name="output.html.stylesheets">
                <xsl:with-param name="stylesheets" select="normalize-space($html.stylesheet)"/>
            </xsl:call-template>
        </xsl:if>

        <xsl:if test="$link.mailto.url != ''">
            <link rev="made" href="{$link.mailto.url}"/>
        </xsl:if>

        <meta name="keywords" content="Bootique ${bootique.version.major} documentation"/>
        <meta name="description" content="User documentation for Bootique Framework version ${linkrest.version.major}"/>

        <xsl:if test="$generate.meta.abstract != 0">
            <xsl:variable name="info" select="(d:articleinfo
                                      |d:bookinfo
                                      |d:prefaceinfo
                                      |d:chapterinfo
                                      |d:appendixinfo
                                      |d:sectioninfo
                                      |d:sect1info
                                      |d:sect2info
                                      |d:sect3info
                                      |d:sect4info
                                      |d:sect5info
                                      |d:referenceinfo
                                      |d:refentryinfo
                                      |d:partinfo
                                      |d:info
                                      |d:docinfo)[1]"/>
            <xsl:if test="$info and $info/d:abstract">
                <meta name="description">
                    <xsl:attribute name="content">
                        <xsl:for-each select="$info/d:abstract[1]/*">
                            <xsl:value-of select="normalize-space(.)"/>
                            <xsl:if test="position() &lt; last()">
                                <xsl:text> </xsl:text>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:attribute>
                </meta>
            </xsl:if>
        </xsl:if>

        <xsl:if test="($draft.mode = 'yes' or
                ($draft.mode = 'maybe' and
                ancestor-or-self::*[@status][1]/@status = 'draft'))
                and $draft.watermark.image != ''">
            <style type="text/css"><xsl:text>
body { background-image: url('</xsl:text>
                <xsl:value-of select="$draft.watermark.image"/><xsl:text>');
       background-repeat: no-repeat;
       background-position: top left;
       /* The following properties make the watermark "fixed" on the page. */
       /* I think that's just a bit too distracting for the reader... */
       /* background-attachment: fixed; */
       /* background-position: center center; */
     }</xsl:text>
            </style>
        </xsl:if>
        <xsl:apply-templates select="." mode="head.keywords.content"/>
    </xsl:template>

    <!-- GoogleAnalytics script for web publishing -->
    <xsl:template name="user.head.content">
        <script type="text/javascript">
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-73654436-1', 'auto');
  ga('send', 'pageview');
        </script>
    </xsl:template>

    <!--
        TOP NAVIGATION
    -->
    <xsl:template name="header.navigation">
        <xsl:param name="prev" select="/d:foo"/>
        <xsl:param name="next" select="/d:foo"/>
        <xsl:param name="nav.context"/>

        <xsl:variable name="home" select="/*[1]"/>
        <xsl:variable name="up" select="parent::*"/>

        <xsl:variable name="row1" select="$navig.showtitles != 0"/>
        <xsl:variable name="row2" select="count($prev) &gt; 0
                                    or (count($up) &gt; 0
                                        and generate-id($up) != generate-id($home)
                                        and $navig.showtitles != 0)
                                    or count($next) &gt; 0"/>

        <xsl:if test="$suppress.navigation = '0' and $suppress.header.navigation = '0'">
            <div class="navheader">
                <xsl:if test="$row1 or $row2">
                    <table width="100%" summary="Navigation header">
                      
                        <!-- Add LinkRest version info -->
                        <xsl:if test="$row1">
                            <tr>
                                <th class="versioninfo">v.${bootique.version.major} (${pom.version})</th>
                                <th align="center">
                                    <xsl:apply-templates select="." mode="object.title.markup"/>
                                </th>
                                <th></th>
                            </tr>
                        </xsl:if>

                        <xsl:if test="$row2">

                            <tr>
                                <td width="20%" align="{$direction.align.start}">
                                    <xsl:if test="count($prev)>0">
                                        <a accesskey="p">
                                            <xsl:attribute name="href">
                                                <xsl:call-template name="href.target">
                                                    <xsl:with-param name="object" select="$prev"/>
                                                </xsl:call-template>
                                            </xsl:attribute>
                                        </a>
                                    </xsl:if>
                                    <xsl:text>&#160;</xsl:text>
                                </td>

                                <!-- Make parent caption as link -->
                                <th width="60%" align="center">
                                    <xsl:choose>
                                        <xsl:when test="count($up) > 0
                                  and generate-id($up) != generate-id($home)
                                  and $navig.showtitles != 0">
                                            <a accesskey="u">
                                                <xsl:attribute name="href">
                                                    <xsl:call-template name="href.target">
                                                        <xsl:with-param name="object" select="$up"/>
                                                    </xsl:call-template>
                                                </xsl:attribute>
                                                <xsl:apply-templates select="$up" mode="object.title.markup"/>
                                            </a>
                                        </xsl:when>
                                        <xsl:otherwise>&#160;</xsl:otherwise>
                                    </xsl:choose>
                                </th>


                                <td width="20%" align="{$direction.align.end}">
                                    <xsl:text>&#160;</xsl:text>
                                    <xsl:if test="count($next)>0">
                                        <a accesskey="n">
                                            <xsl:attribute name="href">
                                                <xsl:call-template name="href.target">
                                                    <xsl:with-param name="object" select="$next"/>
                                                </xsl:call-template>
                                            </xsl:attribute>
                                        </a>
                                    </xsl:if>
                                </td>
                            </tr>

                        </xsl:if>

                    </table>
                </xsl:if>
                <xsl:if test="$header.rule != 0">
                    <hr/>
                </xsl:if>
            </div>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
