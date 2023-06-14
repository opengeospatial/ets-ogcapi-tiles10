package org.opengis.cite.ogcapitiles10.conformance;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapitiles10.EtsAssert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opengis.cite.ogcapitiles10.CommonFixture;
import org.opengis.cite.ogcapitiles10.openapi3.TestPoint;
import org.opengis.cite.ogcapitiles10.openapi3.UriBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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
public class Tile extends CommonFixture {

	private JsonPath response;
	private String tileMatrixTemplateString = "tileMatrix";
	private String tileRowTemplateString = "tileRow";
	private String tileColTemplateString = "tileCol";	

	/**
	 * <pre>
	 * Implements Abstract test A.2
	 * Addresses Requirement 1: /req/core/tc-op
	 * </pre>
	 */
	//@Test(description = "Implements Abstract test A.2, Requirement 1: /req/core/tc-op")
	public void validateTilesAreAvailable() throws Exception {
		
		
		Response request = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/");
		request.then().statusCode(200);
		response = request.jsonPath();

		List<Object> links = response.getList("links");

		
		String resultString = "";

		for (Object linkObj : links) {
			Map<String, Object> link = (Map<String, Object>) linkObj;
			Object linkType = link.get("rel");
			
			ObjectMapper mapper = new ObjectMapper();
		
				if (link.get("rel").toString().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-")) {
					
					
					resultString = processTilesResponse(link.get("href").toString(),false,false);
				}

			}
		
		System.out.println("validateTilesAreAvailable "+resultString.length());
		Assert.assertTrue(resultString.length()==0, resultString);
		

	}
	
	/**
	 * <pre>
	 * Implements Abstract test A.6: /conf/core/tc-success
	 * Addresses Requirement 5: /req/core/tc-success
	 * </pre>
	 */
	//@Test(description = "Implements Abstract test A.6, Requirement 5: /req/core/tc-success")
	public void validateSuccessfulTilesExecution() throws Exception {
		
		
		Response request = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/");
		request.then().statusCode(200);
		response = request.jsonPath();

		List<Object> links = response.getList("links");

		String resultString = "";

		for (Object linkObj : links) {
			Map<String, Object> link = (Map<String, Object>) linkObj;
			Object linkType = link.get("rel");
			
			ObjectMapper mapper = new ObjectMapper();
		
				if (link.get("rel").toString().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-")) {
	
					
					resultString = processTilesResponse(link.get("href").toString(),true,false);
				}

			}
		
		System.out.println("validateSuccessfulTilesExecution "+resultString.length());
		Assert.assertTrue(resultString.length()==0, resultString);
		

	}	
	
	/**
	 * <pre>
	 * Implements Abstract test A.7: /conf/core/tc-error
	 * Addresses Requirement 6: /req/core/tc-error
	 * </pre>
	 */
	//@Test(description = "Implements Abstract test A.7: /conf/core/tc-error, Requirement 6: /req/core/tc-error")
	public void validateTilesErrorConditions() throws Exception {
		
		
		Response request = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/");
		request.then().statusCode(200);
		response = request.jsonPath();

		List<Object> links = response.getList("links");

		String resultString = "";

		for (Object linkObj : links) {
			Map<String, Object> link = (Map<String, Object>) linkObj;
			Object linkType = link.get("rel");
			
			ObjectMapper mapper = new ObjectMapper();
		
				if (link.get("rel").toString().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-")) {

					
					resultString = processTilesResponse(link.get("href").toString(),true,true);
				}

			}
		
		System.out.println("validateSuccessfulTilesExecution "+resultString.length());
		Assert.assertTrue(resultString.length()==0, resultString);
		

	}		
	
	/**
	 * <pre>
	 * Implements Abstract test A.3
	 * Addresses Requirement 2: /req/core/tc-tilematrix-definition
	 * </pre>
	 */
	@Test(description = "Implements Abstract test A.3, Requirement 2: /req/core/tc-tilematrix-definition")
	public void validateTileMatrixDefinitionIsAvailable() throws Exception {
		
		
		Response request = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/");
		request.then().statusCode(200);
		response = request.jsonPath();

		List<Object> links = response.getList("links");

		
		boolean foundTemplates = false;

		for (int t=0; t<links.size(); t++) {
			Object linkObj = links.get(t);
			Map<String, Object> link = (Map<String, Object>) linkObj;
			Object linkType = link.get("rel");
			
			ObjectMapper mapper = new ObjectMapper();
		
				if (link.get("rel").toString().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-")) {
					
					
					foundTemplates = findTemplateDefinition(link.get("href").toString(),this.tileMatrixTemplateString);
					
					t = links.size(); //limit to one tile-enabled collection
				}

			}
		
		//========================	
		
		if(foundTemplates==false) {
		
			Response request2 = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/collections");
			request2.then().statusCode(200);
			response = request2.jsonPath();
	
			List<Object> collectionsList = response.getList("collections");
	
	
			for (Object collectionObj : collectionsList) {
				HashMap collection = (HashMap) collectionObj;
				System.out.println("CHK1 "+collection.get("id"));
				ArrayList collectionLinks = (ArrayList) collection.get("links");
				for(int q=0; q<collectionLinks.size(); q++)
				{
					HashMap linkItem = (HashMap) collectionLinks.get(q);
					if(linkItem.get("rel").toString().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-"))
					{
					
						
						  TestPoint testPoint = new TestPoint(rootUri.toString(), "/collections/"+URLEncoder.encode(collection.get("id").toString(), StandardCharsets.UTF_8.toString()), null);
				      	  String testPointUri = new UriBuilder(testPoint).buildUrl();
				      	  Response inResponse = init().baseUri(testPointUri).accept(JSON).when().request(GET);
				      	  JsonPath jsonPath = inResponse.jsonPath(); 
				      	  
				      	 List<Object> linksList = jsonPath.getList("links");
				      	 System.out.println("CHK2 "+collection.get("id")+" size="+linksList.getClass());
				      	 
				      	 
				      	 for(int k=0; k<linksList.size(); k++)				      
				      	 {
				      		 HashMap linkObj = (HashMap) linksList.get(k);
				      		
				      		 if (linkObj.get("rel").toString().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-")) {
								
				      			 System.out.println("CHK3 "+linkObj.get("rel"));
								 if(linkObj.get("href").toString().startsWith("http://") || linkObj.get("href").toString().startsWith("https://")) {
								  //foundTemplates = findTemplateDefinition(linkObj.get("href").toString(),this.tileMatrixTemplateString);
									 System.out.println("CHK4a "+linkObj.get("href").toString());
								     parseNestedResource(linkObj.get("href").toString());
								 }
								 else {  //relative URLs
									  //foundTemplates = findTemplateDefinition(linkObj.get("href").toString(),this.tileMatrixTemplateString);
									 System.out.println("CHK4b "+linkObj.get("href").toString());
									 String newURL = rootUri.getScheme()+"://"+rootUri.getHost()+linkObj.get("href").toString();
									 System.out.println("XHK "+newURL);
									 parseNestedResource(newURL);
								 }
								 System.out.println("CHK5 "+k);
								 k = linksList.size(); //limit to one tileset-enabled collection
							}
				      	 }
				      	 
				      	q = collectionLinks.size(); //limit to one tileset-enabled collection		
					}
				}
				
				
			
			
			} //for each collection
		}
		
		
		//========================
		
		Assert.assertTrue(foundTemplates, this.tileMatrixTemplateString+" definition could not be found");
		

	}
	
	private void parseNestedResource(String urlString)
	{
		//https://maps.gnosis.earth/ogcapi/collections/swim:d100_airspace:class_c/tiles?f=json
		Response request = init().baseUri(urlString).accept(JSON).when().request(GET);
		request.then().statusCode(200);
		response = request.jsonPath();

		List<Object> tilesets = response.getList("tilesets");
		System.out.println("CHK "+tilesets.getClass());
		
	}
	
	
	/**
	 * <pre>
	 * Implements Abstract test A.4
	 * Addresses Requirement 3: /req/core/tc-tilerow-definition
	 * </pre>
	 */
	//@Test(description = "Implements Abstract test A.4, Requirement 3: /req/core/tc-tilerow-definition")
	public void validateTileRowDefinitionIsAvailable() throws Exception {
		
		
		Response request = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/");
		request.then().statusCode(200);
		response = request.jsonPath();

		List<Object> links = response.getList("links");

		
		boolean foundTemplates = false;

		for (Object linkObj : links) {
			Map<String, Object> link = (Map<String, Object>) linkObj;
			Object linkType = link.get("rel");
			
			ObjectMapper mapper = new ObjectMapper();
		
				if (link.get("rel").toString().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-")) {
					
					foundTemplates = findTemplateDefinition(link.get("href").toString(),this.tileRowTemplateString);
				}

			}
		
		Assert.assertTrue(foundTemplates, this.tileRowTemplateString+" definition could not be found");
		

	}
	
	/**
	 * <pre>
	 * Implements Abstract test A.5
	 * Addresses Requirement 4: /req/core/tc-tilecol-definition
	 * </pre>
	 */
	//@Test(description = "Implements Abstract test A.5, Requirement 4: /req/core/tc-tilecol-definition")
	public void validateTileColDefinitionIsAvailable() throws Exception {
		
		
		Response request = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/");
		request.then().statusCode(200);
		response = request.jsonPath();

		List<Object> links = response.getList("links");

		
		boolean foundTemplates = false;

		for (Object linkObj : links) {
			Map<String, Object> link = (Map<String, Object>) linkObj;
			Object linkType = link.get("rel");
			
			ObjectMapper mapper = new ObjectMapper();
		
				if (link.get("rel").toString().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-")) {
					
					foundTemplates = findTemplateDefinition(link.get("href").toString(),this.tileColTemplateString);
				}

			}
		
		Assert.assertTrue(foundTemplates, this.tileColTemplateString+" definition could not be found");
		

	}	
	
	private boolean findTemplateDefinition(String urlString, String definitionTemplate)
	{
		boolean foundTemplates = false;
		
		Response request = init().baseUri(urlString).accept(JSON).when().request(GET);
		request.then().statusCode(200);
		response = request.jsonPath();

		List<Object> tilesets = response.getList("tilesets");
		
		for (Object tilesetObj : tilesets) {
			Map<String, Object> tileset = (Map<String, Object>) tilesetObj;
			
			ArrayList linksList = (ArrayList) tileset.get("links");
			
			for(int i=0; i<linksList.size(); i++)
			{
				
				HashMap links = (HashMap) linksList.get(i);
				
		
					try {
					  if(links.get("href").toString().contains("{"+definitionTemplate+"}"))
					  {
						  foundTemplates = true;
					  }
					}
					catch(Exception ee)
					{
						ee.printStackTrace();
					}
				
			}
			
		}
		
		return foundTemplates;
	}
	
	private String processTilesResponse(String urlString, boolean testURL, boolean checkErrorResponse)
	{
		StringBuffer errorMessages = new StringBuffer();
		
		
		boolean foundTemplates = false;
		
		Response request = init().baseUri(urlString).accept(JSON).when().request(GET);
		request.then().statusCode(200);
		response = request.jsonPath();

		List<Object> tilesets = response.getList("tilesets");
		
		for (Object tilesetObj : tilesets) {
			Map<String, Object> tileset = (Map<String, Object>) tilesetObj;
			
			String tileMatrixSetId = tileset.get("tileMatrixSetId").toString();
			System.out.println("WW "+tileMatrixSetId);			
			
			ArrayList linksList = (ArrayList) tileset.get("links");
			
			
			ArrayList tileMatrixSetLimitsList = (ArrayList) tileset.get("tileMatrixSetLimits");
			
			String tileMatrix = "";
			String maxTileRow = "";
			String minTileCol = "";
			
			for(int i=0; i<Math.min(tileMatrixSetLimitsList.size(),1); i++)
			{
				HashMap tileMatrixSetLimits = (HashMap) tileMatrixSetLimitsList.get(i);
				tileMatrix = tileMatrixSetLimits.get("tileMatrix").toString();
				maxTileRow = tileMatrixSetLimits.get("maxTileRow").toString();
				minTileCol = tileMatrixSetLimits.get("minTileCol").toString();
			}
			
			for(int i=0; i<linksList.size(); i++)
			{
				
				HashMap links = (HashMap) linksList.get(i);
				
				
					try {
					  if(links.get("href").toString().contains("{"+this.tileMatrixTemplateString+"}") 
							  && links.get("href").toString().contains("{"+this.tileRowTemplateString+"}") 
							  && links.get("href").toString().contains("{"+this.tileColTemplateString+"}")) 
					  {
						  if(testURL) {
		
							  if(checkErrorResponse==false){
								  String newURL = links.get("href").toString().
										  replace("{"+this.tileMatrixTemplateString+"}", tileMatrix).
										  replace("{"+this.tileRowTemplateString+"}", maxTileRow).
										  replace("{"+this.tileColTemplateString+"}", minTileCol);								  
								  URL urlStr = new URL(newURL);
								  HttpURLConnection httpConn = (HttpURLConnection) urlStr.openConnection();
								   
								  int responseCode = httpConn.getResponseCode();
								
								  if(responseCode!=200)
								  {
									  errorMessages.append("Expected status code 200 but received "+responseCode+" . ");
								  }
						    }
							 else if(checkErrorResponse==true) {
								  String newURL = links.get("href").toString().
										  replace("{"+this.tileMatrixTemplateString+"}", tileMatrix).
										  replace("{"+this.tileRowTemplateString+"}", ""+(Integer.parseInt(maxTileRow)+1)).
										  replace("{"+this.tileColTemplateString+"}", minTileCol);
								  URL urlStr = new URL(newURL);
								  HttpURLConnection httpConn = (HttpURLConnection) urlStr.openConnection();
								   
								  int responseCode = httpConn.getResponseCode();
								
								  if(responseCode!=404 && responseCode!=400)
								  {
									  errorMessages.append("Expected status code 404 or 400 but received "+responseCode+" . ");
								  }  
								
							 }
							  
						  }
						  foundTemplates = true;
					  }
					}
					catch(Exception ee)
					{
						ee.printStackTrace();
					}
				
			}
			

			
		}
		
		if(foundTemplates==false) errorMessages.append("No URL templates were found.");
		
		return errorMessages.toString();
	}

	private String parseTilesetMetadata(String urlString)
	{

		String tileSetMetadata = null;
		
		try {
			tileSetMetadata = readJSONObjectFromURL(new URL(urlString));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
		return tileSetMetadata;
	}
	
    public String readJSONObjectFromURL(URL requestURL) throws IOException {

        HttpURLConnection urlConnection = (HttpURLConnection) requestURL.openConnection();
        urlConnection.setRequestProperty("Accept","application/json");
        InputStream is = urlConnection.getInputStream();
        try ( Scanner scanner = new Scanner(is,
                StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter("\\A");

            return (scanner.hasNext() ? scanner.next() : "");
        }

    }
}