package org.opengis.cite.ogcapitiles10.landingpage;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapitiles10.EtsAssert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opengis.cite.ogcapitiles10.CommonFixture;
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
				"The landing page must include at least links with relation types "+
						"http://www.opengis.net/def/rel/ogc/1.0/tilesets-vector, " +
						"http://www.opengis.net/def/rel/ogc/1.0/tilesets-map or "+
						"http://www.opengis.net/def/rel/ogc/1.0/tilesets-coverage"
				
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