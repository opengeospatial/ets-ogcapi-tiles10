package org.opengis.cite.ogcapitiles10.tileset;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapitiles10.SuiteAttribute.API_MODEL;
import static org.opengis.cite.ogcapitiles10.SuiteAttribute.IUT;
import static org.opengis.cite.ogcapitiles10.SuiteAttribute.REQUIREMENTCLASSES;
import static org.opengis.cite.ogcapitiles10.conformance.RequirementClass.CORE;
import static org.opengis.cite.ogcapitiles10.openapi3.OpenApiUtils.retrieveTestPointsForConformance;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengis.cite.ogcapitiles10.CommonFixture;
import org.opengis.cite.ogcapitiles10.openapi3.TestPoint;
import org.opengis.cite.ogcapitiles10.openapi3.UriBuilder;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.reprezen.kaizen.oasparser.model3.MediaType;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * Updated at the OSGeo Virtual Community Sprint 2020 by ghobona
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class TileSet extends CommonFixture {

	@DataProvider(name = "tilesetUris")
	public Object[][] tilesetUris(ITestContext testContext) {
		OpenApi3 apiModel = (OpenApi3) testContext.getSuite().getAttribute(API_MODEL.getName());
		URI iut = (URI) testContext.getSuite().getAttribute(IUT.getName());

		TestPoint tp = new TestPoint(rootUri.toString(), "/tileMatrixSets", null);

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
	 * Partly addresses Requirement 24 /req/tileset/tmxslink If the tiles are available in
	 * a tile matrix set different from WebMercatorQuad, the content of the response to a
	 * successful execution to a tileset request SHALL provide the necessary information
	 * to formulate a tile request.
	 * @param testPoint the test point to test, never <code>null</code>
	 */
	@Test(description = "Implements Abstract Test 24 and Requirement 24 /req/tileset/tmxslink TileSet Path {root}/tileMatrixSets,",
			groups = "tileMatrixSets", dataProvider = "tilesetUris")
	public void validateTilesetResponse(TestPoint testPoint) {

		Response request = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/tileMatrixSets");
		request.then().statusCode(200);
		JsonPath response = request.jsonPath();

		List<Object> tileMatrixSets = response.getList("tileMatrixSets");

		boolean providesInformationToFormulateTileRequest = false;
		String[] knownTMS = { "WebMercatorQuad", "WorldCRS84Quad", "WorldMercatorWGS84Quad", "UTM31WGS84Quad",
				"UPSArcticWGS84Quad", "UPSAntarcticWGS84Quad", "EuropeanETRS89_LAEAQuad", "CanadianNAD83_LCC",
				"GNOSISGlobalGrid", "CDBGlobalGrid" }; // This will eventually be pulled
														// in from the OGC Definitions
														// Server

		for (Object tileMatrixSetObj : tileMatrixSets) {

			HashMap tileMatrixSet = (HashMap) tileMatrixSetObj;
			ArrayList aList = (ArrayList) tileMatrixSet.get("links");

			for (int i = 0; i < aList.size(); i++) {
				HashMap link = (HashMap) aList.get(i);

				if (link.get("rel").toString().equals("item")
						&& link.get("type").toString().equals("application/json")) {

					for (int k = 0; k < knownTMS.length; k++) {
						if (link.get("href").toString().contains(knownTMS[k])) {
							providesInformationToFormulateTileRequest = true;
						}
					}
					if (providesInformationToFormulateTileRequest == false) {

						providesInformationToFormulateTileRequest = checkIfTileMatrixSetIsDefined(
								link.get("href").toString());
					}
				}

			}

		}

		assertTrue(providesInformationToFormulateTileRequest,
				"If the tiles are available in a tile matrix set different from WebMercatorQuad, the content of the response to a successful execution to a tileset request SHALL provide the necessary information to formulate a tile request.");

	}

	public boolean checkIfTileMatrixSetIsDefined(String href) {
		boolean providesInformationToFormulateTileRequest = false;

		Response request = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, href);
		request.then().statusCode(200);
		JsonPath response = request.jsonPath();

		if (response.getList("tileMatrices").size() > 0)
			providesInformationToFormulateTileRequest = true;

		return providesInformationToFormulateTileRequest;
	}

}