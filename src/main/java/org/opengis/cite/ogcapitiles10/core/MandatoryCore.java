package org.opengis.cite.ogcapitiles10.core;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapitiles10.EtsAssert.assertTrue;
import static org.opengis.cite.ogcapitiles10.SuiteAttribute.API_MODEL;
import static org.opengis.cite.ogcapitiles10.SuiteAttribute.TILE_MATRIX_SET_DEFINITION_URI;
import static org.opengis.cite.ogcapitiles10.SuiteAttribute.URL_TEMPLATE_FOR_TILES;
import static org.opengis.cite.ogcapitiles10.SuiteAttribute.TILE_MATRIX;
import static org.opengis.cite.ogcapitiles10.SuiteAttribute.MINIMUM_TILE_ROW;
import static org.opengis.cite.ogcapitiles10.SuiteAttribute.MAXIMUM_TILE_ROW;
import static org.opengis.cite.ogcapitiles10.SuiteAttribute.MINIMUM_TILE_COLUMN;
import static org.opengis.cite.ogcapitiles10.SuiteAttribute.MAXIMUM_TILE_COLUMN;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opengis.cite.ogcapitiles10.CommonFixture;
import org.opengis.cite.ogcapitiles10.SuiteAttribute;
import org.opengis.cite.ogcapitiles10.openapi3.TestPoint;
import org.opengis.cite.ogcapitiles10.openapi3.UriBuilder;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Updated at the OGC API - Tiles Sprint 2020 by ghobona
 *
 * A.2.2. Landing Page {root}/
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class MandatoryCore extends CommonFixture {

	private JsonPath response;
	private String tileMatrixTemplateString = "tileMatrix";
	private String tileRowTemplateString = "tileRow";
	private String tileColTemplateString = "tileCol";	

	
	/**
	 * <pre>
	 * Implements Abstract test A.6
	 * Addresses Requirement 5: /req/core/tc-success
	 * </pre>
	 */
	@Test(description = "Implements Abstract test A.6, Requirement 5: /req/core/tc-success")
	public void verifyMinimalConformance(ITestContext testContext) throws Exception {
		
		Map<String, String> params = testContext.getSuite().getXmlSuite().getParameters();	
		
		checkInputs(params);		
		
		String urlTemplate = testContext.getSuite().getAttribute(URL_TEMPLATE_FOR_TILES.getName()).toString();
		String tileMatrixSetDefinitionURI = testContext.getSuite().getAttribute(TILE_MATRIX_SET_DEFINITION_URI.getName()).toString();		

		String tileMatrixString = testContext.getSuite().getAttribute(TILE_MATRIX.getName()).toString();
		String minTileRowString = testContext.getSuite().getAttribute(MINIMUM_TILE_ROW.getName()).toString();
		String maxTileRowString = testContext.getSuite().getAttribute(MAXIMUM_TILE_ROW.getName()).toString();
		String minTileColString = testContext.getSuite().getAttribute(MINIMUM_TILE_COLUMN.getName()).toString();
		String maxTileColString = testContext.getSuite().getAttribute(MAXIMUM_TILE_COLUMN.getName()).toString();
		
		int tileMatrix = Integer.parseInt(tileMatrixString);
		int minTileRow = Integer.parseInt(minTileRowString);
		int maxTileRow = Integer.parseInt(maxTileRowString);
		int minTileCol = Integer.parseInt(minTileColString);
		int maxTileCol = Integer.parseInt(maxTileColString);
		
		FileReader fis = null;
		boolean foundRegisteredTileMatrixSetDefinition = false;
		boolean tileMatrixSetDefinitionInUrlTemplate = false;
		try {
			
			List<List<String>> records = new ArrayList<>();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/org/opengis/cite/ogcapitiles10/tilematrixsetdefinitions.csv")))) {
			    String line;
			    while ((line = br.readLine()) != null) {
			        String[] values = line.split(",");
			        records.add(Arrays.asList(values));
			    }
			}
			
			for(int i=0; i < records.size(); i++) {
		
				if(tileMatrixSetDefinitionURI.toLowerCase().equals(records.get(i).get(1).toLowerCase())) {
					foundRegisteredTileMatrixSetDefinition = true;					
					
					tileMatrixSetDefinitionInUrlTemplate = urlTemplate.contains("/tiles/"+records.get(i).get(0)) | urlTemplate.contains("/tiles/{tileMatrixSetId}");
				
				}
			}
			
			
		}
		catch(Exception ex)
		{
	
			ex.printStackTrace();
		}
		
		Assert.assertTrue(foundRegisteredTileMatrixSetDefinition,"The tile matrix set definition is unknown. Please use the URI of a registered tile matrix set definition. The URIs can be found at https://defs.opengis.net/vocprez/object?uri=http%3A//www.opengis.net/def/tms");
		Assert.assertTrue(tileMatrixSetDefinitionInUrlTemplate,"Neither the user-provided tile matrix set definition nor its variable ({tileMatrixSetId}) was found in the url template");
		


		boolean allRequestsSuccessful = false;
		
		try {
		  allRequestsSuccessful = getTile(tileMatrix,minTileRow,maxTileRow,minTileCol, maxTileCol,urlTemplate);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		Assert.assertTrue(allRequestsSuccessful, "A successful execution of the operation with content responds with a HTTP status code 200, but the respose code was not 200.");
			

		
	}
	
	
	private boolean getTile(int tileMatrix,int minTileRow,int maxTileRow,int minTileCol,int maxTileCol, String urlTemplate) throws Exception {
		
		boolean allRequestsSuccessful = true;
		
		
		String urlString = null;
		
		
		for(int r = minTileRow; r <= maxTileRow; r++ ) {
			
			for(int c = minTileCol; c <= maxTileCol; c++ ) {
		
				urlString = urlTemplate.replace("{tileMatrix}", tileMatrix+"").replace("{tileRow}", r+"").replace("{tileCol}", c+"");		
		
				
				  URL urlStr = new URL(urlString);
				  HttpURLConnection httpConn = (HttpURLConnection) urlStr.openConnection();
				   
				  int responseCode = httpConn.getResponseCode();
				
				  if(responseCode!=200)
				  {
					  allRequestsSuccessful = false;
			
				  } 
				  
	
				
			}
		}
		
		return allRequestsSuccessful;
	}
	
	private void checkInputs(Map<String, String> params)
	{
	
		
		if(!params.containsKey("tilematrixsetdefinitionuri") || params.get("tilematrixsetdefinitionuri").isEmpty()) {		
			Assert.fail("A tile matrix set definition uri was not found in the test inputs.");
		}
		if(!params.containsKey("urltemplatefortiles") || params.get("urltemplatefortiles").isEmpty()) {		
			Assert.fail("A url template was not found in the test inputs.");
		}		
		if(!params.containsKey("tilematrix") || params.get("tilematrix").isEmpty()) {		
			Assert.fail("A tileMatrix was not found in the test inputs.");
		}		
        if (!params.containsKey("mintilerow") || params.get("mintilerow").isEmpty()) {
        	Assert.fail("A minimum tile row number was not found in the test suite inputs.");
        }
        if (!params.containsKey("maxtilerow") || params.get("maxtilerow").isEmpty()) {
        	Assert.fail("A maximum tile row number was not found in the test suite inputs.");
        }
        if (!params.containsKey("mintilecol") || params.get("mintilecol").isEmpty()) {
        	Assert.fail("A minimum tile column number was not found in the test suite inputs.");
        }
        if (!params.containsKey("maxtilecol") || params.get("maxtilecol").isEmpty()) {
        	Assert.fail("A maximum tile column number was not found in the test suite inputs.");
        }
        
  

        
        
	}
	
	
	/**
	 * <pre>
	 * Implements Abstract test A.7
	 * Addresses Requirement 6: /req/core/tc-error
	 * </pre>
	 */
	@Test(description = "Implements Abstract test A.7, Requirement 6: /req/core/tc-error")
	public void verifyErrorResponses(ITestContext testContext) throws Exception {
		
		Map<String, String> params = testContext.getSuite().getXmlSuite().getParameters();	
		
		checkInputs(params);
		
		String urlTemplate = testContext.getSuite().getAttribute(URL_TEMPLATE_FOR_TILES.getName()).toString();
		int tileMatrix = Integer.parseInt(testContext.getSuite().getAttribute(TILE_MATRIX.getName()).toString());
		int minTileRow = Integer.parseInt(testContext.getSuite().getAttribute(MINIMUM_TILE_ROW.getName()).toString());
		int maxTileRow = Integer.parseInt(testContext.getSuite().getAttribute(MAXIMUM_TILE_ROW.getName()).toString());
		int minTileCol = Integer.parseInt(testContext.getSuite().getAttribute(MINIMUM_TILE_COLUMN.getName()).toString());
		int maxTileCol = Integer.parseInt(testContext.getSuite().getAttribute(MAXIMUM_TILE_COLUMN.getName()).toString());	
	
		boolean allRequestsSuccessful = true;
		
		
		String urlString = null;
		
		int invalidTileRow = maxTileRow + 100;
		int invalidTileCol = maxTileCol + 100;
		
		urlString = urlTemplate.replace("{tileMatrix}", tileMatrix+"").replace("{tileRow}", invalidTileRow+"").replace("{tileCol}", invalidTileCol+"");		
		
		
		URL urlStr = new URL(urlString);
		HttpURLConnection httpConn = (HttpURLConnection) urlStr.openConnection();
		   
		int responseCode = httpConn.getResponseCode();
				
		Assert.assertTrue(responseCode==404 || responseCode==400, "If the path parameter values tileMatrix, tileRow, tileCol for a tile request are out-of-range, the HTTP response must be status code 404 or a 400. However, the response code was "+responseCode);
			

		
	}	
	
}