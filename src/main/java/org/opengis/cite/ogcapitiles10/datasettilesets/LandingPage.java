package org.opengis.cite.ogcapitiles10.datasettilesets;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapitiles10.EtsAssert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opengis.cite.ogcapitiles10.CommonFixture;
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
public class LandingPage extends CommonFixture {

	private JsonPath response;

	/**
	 * <pre>
	 * Implements Abstract test A.10: /conf/dataset-tilesets/landingpage
	 * Addresses Requirement 11: /req/dataset-tilesets/landingpage
	 * </pre>
	 */
	@Test(description = "Implements Abstract test A.10, addresses Requirement 11", groups = "landingpage")
	public void landingPageRetrieval() {
		
		if(rootUri==null)
		{
			throw new SkipException(missing_landing_page_error_message);
		}
	
		Response request = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/");
		request.then().statusCode(200);
		response = request.jsonPath();	
		
		List<Object> links = response.getList("links");
		
		boolean hasTilesets = false;

		for (Object link : links) {
			Map<String, Object> linkMap = (Map<String, Object>) link;
			Object linkType = linkMap.get("rel");
		
			if(linkType.toString().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-vector") || 
					linkType.toString().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-map") || 
					linkType.toString().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-coverage")) 
				hasTilesets = true;			
		}
		assertTrue(hasTilesets,
				"Requirement 11 states that if the API has a mechanism for exposing root resources (e.g., a landing page), the API SHALL advertise at least one URI to retrieve a tilesets list provided by this service with a link having a rel value: http://www.opengis.net/def/rel/ogc/1.0/tilesets-vector, http://www.opengis.net/def/rel/ogc/1.0/tilesets-map or http://www.opengis.net/def/rel/ogc/1.0/tilesets-coverage. However, the landing page did not have any such links."
				
				);

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