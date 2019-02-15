<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:output method="html" omit-xml-declaration="yes" indent="yes"/>
    
    <xsl:template match="standard">
        <xsl:variable name="susceptibleGTorG" select="metadata/susceptibleGTorG"/>
        <xsl:variable name="resistentLTorL" select="metadata/resistentLTorL"/>
        <h1><xsl:value-of select="metadata/name"/> </h1><h1> Version <xsl:value-of select="metadata/version"/></h1>
        <h2>Valid from <xsl:value-of select="metadata/validFrom"/></h2>
        <hr/>
        <xsl:for-each select="breakpoints">
            <xsl:for-each select="familyBreakPoint">
                <h3><xsl:value-of select="bacteriaFamily"/></h3>
                
                <table border="1" style="width:100%">
                    <tr>
                        <th>Antibiotic Family</th>
                        <th>Antibiotic</th>
                        <th>Case</th>
                        <th>Disk Content</th>
                        <th>Susceptible (<xsl:value-of select="$susceptibleGTorG"/> )</th>
                        <th>Resistant (<xsl:value-of select="$resistentLTorL"/> )</th>
                        <th>Notes</th>
                    </tr>     
                
                <xsl:for-each select="breakpoint">
                    <tr><td><xsl:value-of select="antibiotic/family"/></td>
                    <td><xsl:value-of select="antibiotic/name"/></td>
                    <td><xsl:value-of select="case"/></td>
                    <td><xsl:value-of select="antibiotic/diskContent"/></td>
                    <td><xsl:value-of select="susceptible"/></td>
                    <td><xsl:value-of select="resistant"/></td>
                    <td><xsl:value-of select="comments"/></td>
                    </tr>
                </xsl:for-each>
                </table>
                <hr/>
            </xsl:for-each>
            
        </xsl:for-each>
        
        
    </xsl:template>
    
    
    
</xsl:stylesheet>
