package org.opengis.cite.ogcapitiles10.tilesetslist;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapitiles10.SuiteAttribute.API_MODEL;
import static org.opengis.cite.ogcapitiles10.SuiteAttribute.IUT;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengis.cite.ogcapitiles10.CommonFixture;
import org.opengis.cite.ogcapitiles10.openapi3.TestPoint;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.reprezen.kaizen.oasparser.model3.OpenApi3;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class TilesetLlinks extends CommonFixture {

	private JsonPath response;

	@DataProvider(name = "tilesetListsURIs")
	public Object[][] tilesetUris(ITestContext testContext) {
		OpenApi3 apiModel = (OpenApi3) testContext.getSuite().getAttribute(API_MODEL.getName());
		URI iut = (URI) testContext.getSuite().getAttribute(IUT.getName());

		TestPoint tp = new TestPoint(rootUri.toString(), "/collections", null);

		List<TestPoint> testPoints = new ArrayList<TestPoint>();
		testPoints.add(tp);
		Object[][] testPointsData = new Object[1][];
		int i = 0;
		for (TestPoint testPoint : testPoints) {
			testPointsData[i++] = new Object[] { testPoint };
		}
		return testPointsData;
	}

	/**
	 * Implements Abstract test A.9 Addresses Requirement 10
	 * @param testPoint the test point to test<code>null</code>
	 */
	@Test(description = "Implements Abstract test A.9, addresses Requirement 10 (/req/tilesets-list/tileset-links)",
			groups = "tilesetsLists", dataProvider = "tilesetListsURIs")
	public void validateTilesetsListResponse(TestPoint testPoint) {

		if (rootUri == null) {
			throw new SkipException(missing_landing_page_error_message);
		}

		StringBuffer errorMessagesRoot = new StringBuffer();
		StringBuffer errorMessagesCollection = new StringBuffer();

		errorMessagesRoot.append(tilesetsListResponseFromRoot());

		errorMessagesCollection.append(tilesetsListResponseFromCollections());

		assertTrue(errorMessagesRoot.toString().length() == 0 && errorMessagesCollection.toString().length() == 0,
				errorMessagesRoot.toString() + " \n" + errorMessagesCollection.toString());

	}

	private String tilesetsListResponseFromRoot() {
		StringBuffer errorMessages = new StringBuffer();

		Response request = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/");
		request.then().statusCode(200);
		response = request.jsonPath();

		List<Object> links = response.getList("links");

		boolean hasTilesets = false;

		for (Object linkObj : links) {
			Map<String, Object> link = (Map<String, Object>) linkObj;
			Object linkType = link.get("rel");

			if (link.get("rel").toString().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-")) {

				if (link.get("href").toString().contains("f=json")) {
					boolean hasSubsetOfTheTilesetMetadata = checkHasSubsetOfTheTilesetMetadata(
							link.get("href").toString());

					if (hasSubsetOfTheTilesetMetadata == false) { // Test Requirement
																	// 10B
						errorMessages
								.append("One of the tilesets did not have the minimum required subset of metadata ;");
					}

				}

				if (!link.get("href").toString().contains("/tiles")) { // Test
																		// Requirement
																		// 10A
					errorMessages.append(
							"'/tiles' path not found at link relation type " + link.get("href").toString() + " ;");
				}
			}

		}

		return errorMessages.toString();
	}

	private String tilesetsListResponseFromCollections() {

		// Check Tilesets at collections level

		Response request = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/collections");
		request.then().statusCode(200);
		JsonPath response = request.jsonPath();

		List<Object> collections = response.getList("collections");

		StringBuffer errorMessages = new StringBuffer();

		int tilesCollectionLimit = 2; // maximum number of collections with tiles that
										// will be checked
		int tilesCollectionCount = 0;

		for (int a = 0; (a < collections.size() && tilesCollectionCount < tilesCollectionLimit); a++) {

			HashMap collection = (HashMap) collections.get(a);

			ArrayList links = (ArrayList) collection.get("links");

			for (int b = 0; b < links.size(); b++) {
				HashMap link = (HashMap) links.get(b);

				if (link.get("rel").toString().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-")) {

					if (link.get("href").toString().contains("f=json")) {
						boolean hasSubsetOfTheTilesetMetadata = checkHasSubsetOfTheTilesetMetadata(
								link.get("href").toString());

						if (hasSubsetOfTheTilesetMetadata == false) { // Test Requirement
																		// 10B
							errorMessages.append("One of the tilesets in " + collection.get("title")
									+ " did not have the minimum required subset of metadata ;");
						}

						tilesCollectionCount++;
					}

					if (!link.get("href").toString().contains("/tiles")) { // Test
																			// Requirement
																			// 10A
						errorMessages.append("'/tiles' link relation not found in " + collection.get("title") + " ;");
					}
				}

			}

		}

		return errorMessages.toString();

	}

	private boolean checkHasSubsetOfTheTilesetMetadata(String href) {
		boolean hasSubsetOfTheTilesetMetadata = true; // Assume True until proven
														// otherwise

		try {

			// System.out.println("root=" + rootUri.toString());
			// System.out.println("href=" + href);

			String newHref = href;

			if (href.startsWith("/")) {
				String[] hrefSegments = href.split("/");
				String token = rootUri.toString();
				newHref = token.replace("/" + hrefSegments[1], "");
				if (newHref.endsWith("/"))
					newHref = newHref.substring(0, newHref.length() - 1) + href;
				else
					newHref = newHref + href;
			}

			Response request = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, newHref);
			request.then().statusCode(200);
			JsonPath response = request.jsonPath();

			ArrayList tilesets = (ArrayList) response.getList("tilesets");

			for (int b = 0; b < tilesets.size(); b++) {
				HashMap tilesetMap = (HashMap) tilesets.get(b);
				// System.out.println("title="+tilesetMap.get("title"));
				// System.out.println("tileMatrixSetURI="+tilesetMap.get("tileMatrixSetURI"));
				// System.out.println("tileMatrixSetDefinition="+tilesetMap.get("tileMatrixSetDefinition"));

				if (tilesetMap.containsKey("tileMatrixSetURI") == false) {
					hasSubsetOfTheTilesetMetadata = false;
				}
				// System.out.println("hasSubsetOfTheTilesetMetadata=" +
				// hasSubsetOfTheTilesetMetadata);
			}

		}
		catch (Exception ee) {
			ee.printStackTrace();
		}

		return hasSubsetOfTheTilesetMetadata;
	}

}
