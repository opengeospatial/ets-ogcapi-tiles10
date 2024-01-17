package org.opengis.cite.ogcapitiles10.geodatatilesets;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapitiles10.EtsAssert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opengis.cite.ogcapitiles10.CommonFixture;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * Updated at the OGC API - Tiles Sprint 2020 by ghobona
 *
 * A.2.2. Landing Page {root}/
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GeospatialDataResource extends CommonFixture {

	private JsonPath response;

	/**
	 * <pre>
	 * Implements Abstract test A.12: /conf/geodata-tilesets/desc-links
	 * Addresses Requirement 13: /req/geodata-tilesets/desc-links
	 * </pre>
	 */
	@Test(description = "Implements Abstract test A.12, addresses Requirement 13", groups = "geodata")
	public void geospatialResourceTilesetsLinksCheck() {
	
		
		
		if(rootUri==null)
		{
			throw new SkipException(missing_landing_page_error_message);
		}
	
	
		
		Response request = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/collections");
		request.then().statusCode(200);
		response = request.jsonPath();
		
		
		
		String resultString = "";
		
	
		
		resultString = processNestedTilesetCheck();
		
		
		
		if(resultString.length()>0) {
			Assert.fail("Requirement 13 states that if the Web API based server has a mechanism for geospatial data resources to expose links to geospatial resource aspects (e.g., feature items, metadata…​), the API implementation SHALL include at least one of three link with the href pointing to tilesets list for geospatial data resources and with rel: http://www.opengis.net/def/rel/ogc/1.0/tilesets-vector, http://www.opengis.net/def/rel/ogc/1.0/tilesets-map and http://www.opengis.net/def/rel/ogc/1.0/tilesets-coverage. However, none were found.");
		}
	}	
	
	
	/**
	 * <pre>
	 * Implements Abstract test A.13: /conf/geodata-tilesets/operation
	 * Addresses Requirement 14: /req/geodata-tilesets/operation
	 * </pre>
	 */
	@Test(description = "Implements Abstract test A.13, addresses Requirement 14", groups = "geodata")
	public void geospatialResourceTilesetsRetrieval() {
		
		if(rootUri==null)
		{
			throw new SkipException(missing_landing_page_error_message);
		}
	
		
		Response request = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/collections");
		request.then().statusCode(200);
		response = request.jsonPath();
		
		
		
		String resultString = "";
		
		
		resultString = processNestedTilesResponse();
		
		
		Assert.assertTrue(resultString.length()==0, "Requirement 14 states that the geospatial data resource SHALL have an associated list of at least one tileset accessible at …​/tiles supporting an HTTP GET operation. However, "+resultString);
		
	}
	
	private String processNestedTilesetCheck()
	{
		StringBuffer errorMessages = new StringBuffer();
		
		Response request2 = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/collections");
		request2.then().statusCode(200);
		response = request2.jsonPath();

	
		
		List<Object> collectionsList = response.getList("collections");

		boolean foundTilesetsLink = false;
	

	
		
		for (Object collectionObj : collectionsList) {
			HashMap collection = (HashMap) collectionObj;
			
		
			
			ArrayList collectionLinks = (ArrayList) collection.get("links");
			
			
			
			
			
			for(int q=0; q<collectionLinks.size(); q++)
			{
				
				
				
				HashMap linkItem = (HashMap) collectionLinks.get(q);
			
				if(linkItem.get("rel").toString().trim().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-") &&
						foundTilesetsLink ==false
						)
				{
				
				
					
					String newURL = formatLinkURI(rootUri.getScheme(),rootUri.getHost(),linkItem.get("href").toString()); 
					
			
					
					
					Response tilesRequest = init().baseUri(newURL).accept(JSON).when().request(GET);
					tilesRequest.then().statusCode(200);
					JsonPath tilesResponse = tilesRequest.jsonPath();
					List<Object> tilesetsList = tilesResponse.getList("tilesets");
					
					
			
				
					
			      	 
			      	foundTilesetsLink = true;		
				}
			}
			
			
		
		
		} //for each collection		
		
		errorMessages.append(foundTilesetsLink?"":"No tilesets links found in the geospatial data resources.");
		
		return errorMessages.toString();
	}	
	
	
	
	private String processNestedTilesResponse()
	{
		StringBuffer errorMessages = new StringBuffer();
		
		Response request2 = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/collections");
		request2.then().statusCode(200);
		response = request2.jsonPath();

		
		List<Object> collectionsList = response.getList("collections");

		boolean foundTilesetsLink = false;
	
		
		for (Object collectionObj : collectionsList) {
			HashMap collection = (HashMap) collectionObj;
			
			ArrayList collectionLinks = (ArrayList) collection.get("links");
			
			
			for(int q=0; q<collectionLinks.size(); q++)
			{
				
				
				
				HashMap linkItem = (HashMap) collectionLinks.get(q);
				
				if(linkItem.get("rel").toString().trim().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-") && 
						(linkItem.get("href").toString().endsWith("/tiles") || linkItem.get("href").toString().contains("/tiles?")) && 
						foundTilesetsLink ==false
						)
				{
				
					
					String newURL = formatLinkURI(rootUri.getScheme(),rootUri.getHost(),linkItem.get("href").toString()); 
					
					
					Response tilesRequest = init().baseUri(newURL).accept(JSON).when().request(GET);
					tilesRequest.then().statusCode(200);
					JsonPath tilesResponse = tilesRequest.jsonPath();
					List<Object> tilesetsList = tilesResponse.getList("tilesets");
					
					
			      	 
			      	foundTilesetsLink = true;		
				}
			}
			
			
		
		
		} //for each collection		
		
		errorMessages.append(foundTilesetsLink?"":"No tilesets link with an href ending with '.../tiles' was found in the geospatial data resources.");
		
		return errorMessages.toString();
	}	

	private Set<String> collectLinkTypes(List<Object> links) {
		Set<String> linkTypes = new HashSet<>();
		for (Object link : links) {
			Map<String, Object> linkMap = (Map<String, Object>) link;
			Object linkType = linkMap.get("rel");
			linkTypes.add((String) linkType);
		}
		return linkTypes;
	}

}