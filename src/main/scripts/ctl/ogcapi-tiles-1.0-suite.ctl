<?xml version="1.0" encoding="UTF-8"?>
<ctl:package xmlns:ctl="http://www.occamlab.com/ctl"
             xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
             xmlns:tns="http://www.opengis.net/cite/ogcapi-tiles-1.0"
             xmlns:saxon="http://saxon.sf.net/"
             xmlns:tec="java:com.occamlab.te.TECore"
             xmlns:tng="java:org.opengis.cite.ogcapitiles10.TestNGController">

  <ctl:function name="tns:run-ets-${ets-code}">
    <ctl:param name="testRunArgs">A Document node containing test run arguments (as XML properties).</ctl:param>
    <ctl:param name="outputDir">The directory in which the test results will be written.</ctl:param>
    <ctl:return>The test results as a Source object (root node).</ctl:return>
    <ctl:description>Runs the OGC API - Tiles (${version}) test suite.</ctl:description>
    <ctl:code>
      <xsl:variable name="controller" select="tng:new($outputDir)" />
      <xsl:copy-of select="tng:doTestRun($controller, $testRunArgs)" />
    </ctl:code>
  </ctl:function>

  <ctl:suite name="tns:ets-${ets-code}-${version}">
    <ctl:title>OGC API - Tiles Conformance Test Suite</ctl:title>
    <ctl:description>Checks OGC API - Tiles implementations for conformance.</ctl:description>
    <ctl:starting-test>tns:Main</ctl:starting-test>
  </ctl:suite>

  <ctl:test name="tns:Main">
    <ctl:assertion>The test subject satisfies all applicable constraints.</ctl:assertion>
    <ctl:code>
      <xsl:variable name="form-data">
        <ctl:form method="POST" width="800" height="600" xmlns="http://www.w3.org/1999/xhtml">
          <h2>OGC API - Tiles Conformance Test Suite</h2>
          <div style="background:#F0F8FF" bgcolor="#F0F8FF">
            <p>The implementation under test (IUT) is checked against the following specifications:</p>
            <ul>
              <li>
                <a href="http://docs.opengeospatial.org/is/17-069r3/17-069r3.html">OGC API - Tiles - Part 1: Core</a>
              </li>
            </ul>
            <p>The following conformance levels are defined:</p>
            <ul>
              <li>Annex A.1: Core - Mandatory tests</li>
              <li>Annex A.1: Core - Conditional tests</li>
              <li>Annex A.3: Tilesets List</li>
              <li>Annex A.4: Dataset Tilesets</li>
              <li>Annex A.5: GeoData Tilesets</li>
              <li>Annex A.8: OpenAPI Specification 3.0</li>              
            </ul>
          </div>
          <fieldset style="background:#ccffff">
            <legend style="font-family: sans-serif; color: #000099;
			                 background-color:#F0F8FF; border-style: solid;
                       border-width: medium; padding:4px">Implementation under test
            </legend>

			<p>
			 The following is a quote from Conformance Class "Core" in Annex A.1 of OGC API - Tiles - Part 1: Core v1.0. "In practice, to test for conformance to the Core conformance class, the user of the test should provide a TileMatrixSet definition, a URL template (that contains the endpoint for tiles) with specific variable names, and a range of valid values for those variables and building the URLs. Once the information has been provided, the user can then execute the test."
			</p>
			
			<p> 
			 <h3 style="margin-bottom: 0.5em">Inputs for testing compliance to Conformance Class "Core"</h3>			 
			</p>
			<p>All of the following inputs are mandatory for testing compliance to Conformance Class "Core". </p>
            
            <p>
              <label for="tilematrixsetdefinitionuri">
                <h4 style="margin-bottom: 0.5em">Tile Matrix Set Definition URI</h4>
                The URI of a registered tile matrix set definition that is supported by the API (e.g. http://www.opengis.net/def/tilematrixset/OGC/1.0/WebMercatorQuad)
              </label>
              <input id="tilematrixsetdefinitionuri" name="tilematrixsetdefinitionuri" size="128" type="text"
                     value="" />
            </p>
            
            <p>
              <label for="urltemplatefortiles">
                <h4 style="margin-bottom: 0.5em">URL template (that contains the endpoint for tiles) with specific variable names tileMatrix, tileRow and tileCol</h4>
              </label>
              <input id="urltemplatefortiles" name="urltemplatefortiles" size="128" type="text"
                     value="" />
            </p> 
  
            
            <p>
              <label for="tilematrix">
                <h4 style="margin-bottom: 0.5em">A valid tileMatrix numerical identifier (e.g. 0)</h4>
              </label>
              <input id="tilematrix" name="tilematrix" size="128" type="text"
                     value="" />
            </p>
            
            <p>
              <label for="mintilerow">
                <h4 style="margin-bottom: 0.5em">The minimum tile row number (minTileRow) for the tile matrix (e.g. 0)</h4>
              </label>
              <input id="mintilerow" name="mintilerow" size="128" type="text"
                     value="" />
            </p>
            
            <p>
              <label for="maxtilerow">
                <h4 style="margin-bottom: 0.5em">The maximum tile row number (maxTileRow) for the tile matrix (e.g. 1)</h4>
              </label>
              <input id="maxtilerow" name="maxtilerow" size="128" type="text"
                     value="" />
            </p>            
            
            <p>
              <label for="mintilecol">
                <h4 style="margin-bottom: 0.5em">The minimum tile column number (minTileCol) for the tile matrix (e.g. 0)</h4>
              </label>
              <input id="mintilecol" name="mintilecol" size="128" type="text"
                     value="" />
            </p>
            
            <p>
              <label for="maxtilecol">
                <h4 style="margin-bottom: 0.5em">The maximum tile column number (maxTileCol) for the tile matrix (e.g. 1)</h4>
              </label>
              <input id="maxtilecol" name="maxtilecol" size="128" type="text"
                     value="" />
            </p>            
            
            <p> 
			 <h3 style="margin-bottom: 0.5em">Inputs for testing compliance to other conformance classes</h3>
			</p>                         
            
            <p>If the API offers a Conformance Class declaration document and a Landing Page, then provide the URL of the Landing Page below.</p>
            
            <p>
              <label for="ogc-api-tiles-uri">
                <h4 style="margin-bottom: 0.5em">Location of the Landing Page</h4>
              </label>
              <input id="ogc-api-tiles-uri" name="ogc-api-tiles-uri" size="128" type="text"
                     value="" />
            </p>            
            
          </fieldset>
          <p>
            <input class="form-button" type="submit" value="Start" />
            |
            <input class="form-button" type="reset" value="Clear" />
          </p>
        </ctl:form>
      </xsl:variable>
      <xsl:variable name="test-run-props">
        <properties version="1.0">
          <entry key="iut">
            <xsl:value-of select="normalize-space($form-data/values/value[@key='ogc-api-tiles-uri'])" />
          </entry>
		  <entry key="tilematrixsetdefinitionuri"><xsl:value-of select="$form-data/values/value[@key='tilematrixsetdefinitionuri']"/></entry>
		  <entry key="urltemplatefortiles"><xsl:value-of select="$form-data/values/value[@key='urltemplatefortiles']"/></entry>
		  <entry key="tilematrix"><xsl:value-of select="$form-data/values/value[@key='tilematrix']"/></entry>
		  <entry key="mintilerow"><xsl:value-of select="$form-data/values/value[@key='mintilerow']"/></entry>
		  <entry key="maxtilerow"><xsl:value-of select="$form-data/values/value[@key='maxtilerow']"/></entry>
		  <entry key="mintilecol"><xsl:value-of select="$form-data/values/value[@key='mintilecol']"/></entry>
		  <entry key="maxtilecol"><xsl:value-of select="$form-data/values/value[@key='maxtilecol']"/></entry>          
        </properties>
      </xsl:variable>
      <xsl:variable name="testRunDir">
        <xsl:value-of select="tec:getTestRunDirectory($te:core)" />
      </xsl:variable>
      <xsl:variable name="test-results">
        <ctl:call-function name="tns:run-ets-${ets-code}">
          <ctl:with-param name="testRunArgs" select="$test-run-props" />
          <ctl:with-param name="outputDir" select="$testRunDir" />
        </ctl:call-function>
      </xsl:variable>
      <xsl:call-template name="tns:testng-report">
        <xsl:with-param name="results" select="$test-results" />
        <xsl:with-param name="outputDir" select="$testRunDir" />
      </xsl:call-template>
      <xsl:variable name="summary-xsl" select="tec:findXMLResource($te:core, '/testng-summary.xsl')" />
      <ctl:message>
        <xsl:value-of select="saxon:transform(saxon:compile-stylesheet($summary-xsl), $test-results)" />
        See detailed test report in the TE_BASE/users/
        <xsl:value-of
                select="concat(substring-after($testRunDir, 'users/'), '/html/')" />
        directory.
      </ctl:message>
      <xsl:if test="xs:integer($test-results/testng-results/@failed) gt 0">
        <xsl:for-each select="$test-results//test-method[@status='FAIL' and not(@is-config='true')]">
          <ctl:message>
            Test method<xsl:value-of select="./@name" />:
            <xsl:value-of select=".//message" />
          </ctl:message>
        </xsl:for-each>
        <ctl:fail />
      </xsl:if>
      <xsl:if test="xs:integer($test-results/testng-results/@skipped) eq xs:integer($test-results/testng-results/@total)">
        <ctl:message>All tests were skipped. One or more preconditions were not satisfied.</ctl:message>
        <xsl:for-each select="$test-results//test-method[@status='FAIL' and @is-config='true']">
          <ctl:message>
            <xsl:value-of select="./@name" />:
            <xsl:value-of select=".//message" />
          </ctl:message>
        </xsl:for-each>
        <ctl:skipped />
      </xsl:if>
    </ctl:code>
  </ctl:test>

  <xsl:template name="tns:testng-report">
    <xsl:param name="results" />
    <xsl:param name="outputDir" />
    <xsl:variable name="stylesheet" select="tec:findXMLResource($te:core, '/testng-report.xsl')" />
    <xsl:variable name="reporter" select="saxon:compile-stylesheet($stylesheet)" />
    <xsl:variable name="report-params" as="node()*">
      <xsl:element name="testNgXslt.outputDir">
        <xsl:value-of select="concat($outputDir, '/html')" />
      </xsl:element>
    </xsl:variable>
    <xsl:copy-of select="saxon:transform($reporter, $results, $report-params)" />
  </xsl:template>
</ctl:package>
