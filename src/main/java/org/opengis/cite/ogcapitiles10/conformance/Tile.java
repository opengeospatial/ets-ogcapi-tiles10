package org.opengis.cite.ogcapitiles10.conformance;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapitiles10.EtsAssert.assertTrue;
import static org.opengis.cite.ogcapitiles10.SuiteAttribute.MAXIMUM_TILE_COLUMN;
import static org.opengis.cite.ogcapitiles10.SuiteAttribute.MAXIMUM_TILE_ROW;
import static org.opengis.cite.ogcapitiles10.SuiteAttribute.MINIMUM_TILE_COLUMN;
import static org.opengis.cite.ogcapitiles10.SuiteAttribute.MINIMUM_TILE_ROW;
import static org.opengis.cite.ogcapitiles10.SuiteAttribute.TILE_MATRIX;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.cite.ogcapitiles10.CommonFixture;
import org.opengis.cite.ogcapitiles10.util.TestSuiteLogger;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import no.ecc.vectortile.VectorTileDecoder;
import no.ecc.vectortile.VectorTileDecoder.Feature;
import no.ecc.vectortile.VectorTileDecoder.FeatureIterable;

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

	private String tileMatrixString;

	private String minTileRowString;

	private String maxTileRowString;

	private String minTileColString;

	private String maxTileColString;

	@BeforeClass
	public void setUp(ITestContext testContext) {
		tileMatrixString = testContext.getSuite().getAttribute(TILE_MATRIX.getName()).toString();
		minTileRowString = testContext.getSuite().getAttribute(MINIMUM_TILE_ROW.getName()).toString();
		maxTileRowString = testContext.getSuite().getAttribute(MAXIMUM_TILE_ROW.getName()).toString();
		minTileColString = testContext.getSuite().getAttribute(MINIMUM_TILE_COLUMN.getName()).toString();
		maxTileColString = testContext.getSuite().getAttribute(MAXIMUM_TILE_COLUMN.getName()).toString();
	}

	/**
	 * <pre>
	 * Implements Abstract test A.2
	 * Addresses Requirement 1: /req/core/tc-op
	 * </pre>
	 */
	@Test(description = "Implements Abstract test A.2, Requirement 1: /req/core/tc-op")
	public void validateTilesAreAvailable() throws Exception {

		if (rootUri == null) {
			throw new SkipException(missing_landing_page_error_message);
		}

		Response request = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/");

		request.then().statusCode(200);

		response = request.jsonPath();

		List<Object> links = response.getList("links");

		String resultString = "No tiles resource was found";

		for (Object linkObj : links) {

			Map<?, ?> link = (Map<?, ?>) linkObj;
			Object linkType = link.get("rel");

			if (link.get("rel").toString().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-")) {

				resultString = processTilesResponse(link.get("href").toString(), false, false);

			}

		}

		if (resultString.contains("No tiles resource was found")) { // if the tilesets are
																	// not accessible from
																	// the landing page,
																	// then we check at
																	// the collections
																	// level

			resultString = processNestedTilesResponse();
		}

		Assert.assertTrue(resultString.length() == 0, resultString);

	}

	/**
	 * <pre>
	 * Implements Abstract test A.6: /conf/core/tc-success
	 * Addresses Requirement 5: /req/core/tc-success
	 * </pre>
	 */
	@Test(description = "Implements Abstract test A.6, Requirement 5: /req/core/tc-success")
	public void validateSuccessfulTilesExecutionFollowingLinks() throws Exception {
		if (rootUri == null) {
			throw new SkipException(missing_landing_page_error_message);
		}
		String resultString = processTilesResponse(findTileSetsUriString(), true, false);
		Assert.assertTrue(resultString.length() == 0, resultString);
	}

	/**
	 * <pre>
	 * Implements Abstract test A.7: /conf/core/tc-error
	 * Addresses Requirement 6: /req/core/tc-error
	 * </pre>
	 */
	@Test(description = "Implements Abstract test A.7: /conf/core/tc-error, Requirement 6: /req/core/tc-error")
	public void validateTilesErrorConditions() throws Exception {
		if (rootUri == null) {
			throw new SkipException(missing_landing_page_error_message);
		}
		String resultString = processTilesResponse(findTileSetsUriString(), true, true);
		Assert.assertTrue(resultString.length() == 0, resultString);
	}

	/**
	 * <pre>
	 * Implements Abstract test A.3
	 * Addresses Requirement 2: /req/core/tc-tilematrix-definition
	 * </pre>
	 */
	@Test(description = "Implements Abstract test A.3, Requirement 2: /req/core/tc-tilematrix-definition")
	public void validateTileMatrixDefinitionIsAvailable() throws Exception {

		if (rootUri == null) {
			throw new SkipException(missing_landing_page_error_message);
		}

		Response request = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/");
		request.then().statusCode(200);
		response = request.jsonPath();

		List<Object> links = response.getList("links");

		boolean foundTemplates = false;

		for (int t = 0; t < links.size(); t++) {
			Object linkObj = links.get(t);
			Map<String, Object> link = (Map<String, Object>) linkObj;
			Object linkType = link.get("rel");

			if (link.get("rel").toString().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-")) {

				foundTemplates = findTemplateDefinition(link.get("href").toString(), this.tileMatrixTemplateString);

				t = links.size(); // limit to one tile-enabled collection
			}

		}

		if (foundTemplates == false) {

			foundTemplates = findNestedTemplateDefinition(tileMatrixTemplateString);
		}

		Assert.assertTrue(foundTemplates, this.tileMatrixTemplateString + " definition could not be found");

	}

	private String processNestedTilesResponse() {
		StringBuffer errorMessages = new StringBuffer();

		Response request2 = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/collections");
		request2.then().statusCode(200);
		response = request2.jsonPath();

		List<Object> collectionsList = response.getList("collections");

		boolean foundTilesetsLink = false;
		boolean nestedTilesAreAvailable = false;

		for (Object collectionObj : collectionsList) {
			HashMap<?, ?> collection = (HashMap<?, ?>) collectionObj;

			ArrayList<?> collectionLinks = (ArrayList<?>) collection.get("links");

			for (int q = 0; q < collectionLinks.size(); q++) {

				HashMap<?, ?> linkItem = (HashMap<?, ?>) collectionLinks.get(q);
				if (linkItem.get("rel").toString().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-")
						&& foundTilesetsLink == false) {

					String newURL = formatLinkURI(rootUri.getScheme(), rootUri.getHost(),
							linkItem.get("href").toString());

					Response tilesRequest = init().baseUri(newURL).accept(JSON).when().request(GET);
					tilesRequest.then().statusCode(200);
					JsonPath tilesResponse = tilesRequest.jsonPath();
					List<Object> tilesetsList = tilesResponse.getList("tilesets");
					for (int r = 0; r < tilesetsList.size(); r++) {
						HashMap<?, ?> tileset = (HashMap<?, ?>) tilesetsList.get(r);
						ArrayList<?> tilesetLinksList = (ArrayList<?>) tileset.get("links");
						for (int p = 0; p < tilesetLinksList.size(); p++) {
							HashMap<?, ?> tilesetLink = (HashMap<?, ?>) tilesetLinksList.get(p);
							if (tilesetLink.containsKey("rel") && tilesetLink.containsKey("type")) {
								if (tilesetLink.get("rel").toString().equals("self")
										&& tilesetLink.get("type").toString().equals("application/json")) {
									String newURL2 = formatLinkURI(rootUri.getScheme(), rootUri.getHost(),
											tilesetLink.get("href").toString());

									Response innerTilesRequest = init().baseUri(newURL2)
										.accept(JSON)
										.when()
										.request(GET);
									innerTilesRequest.then().statusCode(200);
									JsonPath innerTilesResponse = innerTilesRequest.jsonPath();
									List<Object> innerTilesLinks = innerTilesResponse.getList("links");

									for (int x = 0; x < innerTilesLinks.size(); x++) {
										HashMap<?, ?> innerTileLink = (HashMap<?, ?>) innerTilesLinks.get(x);
										nestedTilesAreAvailable = true;

									}
								}
							}
						}

					}

					foundTilesetsLink = true;
				}
			}

		} // for each collection

		errorMessages.append(nestedTilesAreAvailable ? "" : "No tiles resource found in collections");

		return errorMessages.toString();
	}

	private boolean findNestedTemplateDefinition(String definitionTemplate) {
		boolean foundTemplates = false;

		Response request2 = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/collections");
		request2.then().statusCode(200);
		response = request2.jsonPath();

		List<Object> collectionsList = response.getList("collections");

		boolean foundTilesetsLink = false;

		for (Object collectionObj : collectionsList) {
			HashMap<?, ?> collection = (HashMap<?, ?>) collectionObj;

			ArrayList<?> collectionLinks = (ArrayList<?>) collection.get("links");

			for (int q = 0; q < collectionLinks.size(); q++) {
				HashMap<?, ?> linkItem = (HashMap<?, ?>) collectionLinks.get(q);
				if (linkItem.get("rel").toString().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-")
						&& foundTilesetsLink == false) {

					String newURL = formatLinkURI(rootUri.getScheme(), rootUri.getHost(),
							linkItem.get("href").toString());

					Response tilesRequest = init().baseUri(newURL).accept(JSON).when().request(GET);
					tilesRequest.then().statusCode(200);
					JsonPath tilesResponse = tilesRequest.jsonPath();
					List<Object> tilesetsList = tilesResponse.getList("tilesets");
					for (int r = 0; r < tilesetsList.size(); r++) {
						HashMap<?, ?> tileset = (HashMap<?, ?>) tilesetsList.get(r);
						ArrayList<?> tilesetLinksList = (ArrayList<?>) tileset.get("links");
						for (int p = 0; p < tilesetLinksList.size(); p++) {
							HashMap<?, ?> tilesetLink = (HashMap<?, ?>) tilesetLinksList.get(p);
							if (tilesetLink.containsKey("rel") && tilesetLink.containsKey("type")) {
								if (tilesetLink.get("rel").toString().equals("self")
										&& tilesetLink.get("type").toString().equals("application/json")) {
									String newURL2 = formatLinkURI(rootUri.getScheme(), rootUri.getHost(),
											tilesetLink.get("href").toString());

									Response innerTilesRequest = init().baseUri(newURL2)
										.accept(JSON)
										.when()
										.request(GET);
									innerTilesRequest.then().statusCode(200);
									JsonPath innerTilesResponse = innerTilesRequest.jsonPath();
									List<Object> innerTilesLinks = innerTilesResponse.getList("links");

									for (int x = 0; x < innerTilesLinks.size(); x++) {
										HashMap<?, ?> innerTileLink = (HashMap<?, ?>) innerTilesLinks.get(x);
										if (innerTileLink.get("href")
											.toString()
											.contains("{" + definitionTemplate + "}")) {
											foundTemplates = true;
										}

									}
								}
							}
						}

					}

					foundTilesetsLink = true;
				}
			}

		} // for each collection
		return foundTemplates;
	}

	/**
	 * <pre>
	 * Implements Abstract test A.4
	 * Addresses Requirement 3: /req/core/tc-tilerow-definition
	 * </pre>
	 */
	@Test(description = "Implements Abstract test A.4, Requirement 3: /req/core/tc-tilerow-definition")
	public void validateTileRowDefinitionIsAvailable() throws Exception {

		if (rootUri == null) {
			throw new SkipException(missing_landing_page_error_message);
		}

		Response request = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/");
		request.then().statusCode(200);
		response = request.jsonPath();

		List<Object> links = response.getList("links");

		boolean foundTemplates = false;

		for (int t = 0; t < links.size(); t++) {
			Object linkObj = links.get(t);
			Map<?, ?> link = (Map<?, ?>) linkObj;
			Object linkType = link.get("rel");

			if (linkType.toString().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-")) {

				foundTemplates = findTemplateDefinition(link.get("href").toString(), this.tileRowTemplateString);

				t = links.size(); // limit to one tile-enabled collection
			}

		}

		// ========================

		if (foundTemplates == false) {

			foundTemplates = findNestedTemplateDefinition(tileRowTemplateString);
		}

		// ========================

		Assert.assertTrue(foundTemplates, this.tileRowTemplateString + " definition could not be found");

	}

	/**
	 * <pre>
	 * Implements Abstract test A.5
	 * Addresses Requirement 4: /req/core/tc-tilecol-definition
	 * </pre>
	 */
	@Test(description = "Implements Abstract test A.5, Requirement 4: /req/core/tc-tilecol-definition")
	public void validateTileColDefinitionIsAvailable() throws Exception {

		if (rootUri == null) {
			throw new SkipException(missing_landing_page_error_message);
		}

		Response request = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/");
		request.then().statusCode(200);
		response = request.jsonPath();

		List<Object> links = response.getList("links");

		boolean foundTemplates = false;

		for (int t = 0; t < links.size(); t++) {
			Object linkObj = links.get(t);
			Map<?, ?> link = (Map<?, ?>) linkObj;
			Object linkType = link.get("rel");

			if (linkType.toString().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-")) {

				foundTemplates = findTemplateDefinition(link.get("href").toString(), this.tileColTemplateString);

				t = links.size(); // limit to one tile-enabled collection
			}

		}

		// ========================

		if (foundTemplates == false) {

			foundTemplates = findNestedTemplateDefinition(tileColTemplateString);
		}

		// ========================

		Assert.assertTrue(foundTemplates, this.tileColTemplateString + " definition could not be found");

	}

	private boolean findTemplateDefinition(String urlString, String definitionTemplate) {
		boolean foundTemplates = false;
		urlString = checkUrlString(urlString);
		Response request = init().baseUri(urlString).accept(JSON).when().request(GET);
		request.then().statusCode(200);
		response = request.jsonPath();

		List<Object> tilesets = response.getList("tilesets");

		for (Object tilesetObj : tilesets) {
			Map<?, ?> tileset = (Map<?, ?>) tilesetObj;

			ArrayList<?> linksList = (ArrayList<?>) tileset.get("links");

			for (int i = 0; i < linksList.size(); i++) {

				HashMap<?, ?> links = (HashMap<?, ?>) linksList.get(i);

				try {
					if (links.get("href").toString().contains("{" + definitionTemplate + "}")) {
						foundTemplates = true;
					}
				}
				catch (Exception ee) {
					ee.printStackTrace();
				}

			}

		}

		return foundTemplates;
	}

	private String processTilesResponse(String urlString, boolean testURL, boolean checkErrorResponse) {
		StringBuffer errorMessages = new StringBuffer();

		boolean foundTemplates = false;

		urlString = checkUrlString(urlString);

		Response request = init().baseUri(urlString).accept(JSON).when().request(GET);
		request.then().statusCode(200);
		response = request.jsonPath();

		List<Object> tilesets = response.getList("tilesets");

		for (Object tilesetObj : tilesets) {
			Map<?, ?> tileset = (Map<?, ?>) tilesetObj;

			ArrayList<?> linksList = (ArrayList<?>) tileset.get("links");

			ArrayList<?> tileMatrixSetLimitsList = (ArrayList<?>) tileset.get("tileMatrixSetLimits");

			String tileMatrix = tileMatrixString;
			String maxTileRow = maxTileRowString;
			String minTileCol = minTileColString;

			if (tileMatrixSetLimitsList != null && !(tileMatrixSetLimitsList.size() == 0)) {

				for (int i = 0; i < Math.min(tileMatrixSetLimitsList.size(), 1); i++) {
					HashMap<?, ?> tileMatrixSetLimits = (HashMap<?, ?>) tileMatrixSetLimitsList.get(i);
					tileMatrix = tileMatrixSetLimits.get("tileMatrix").toString();
					maxTileRow = tileMatrixSetLimits.get("maxTileRow").toString();
					minTileCol = tileMatrixSetLimits.get("minTileCol").toString();
				}
			}

			for (int i = 0; i < linksList.size(); i++) {

				HashMap<?, ?> links = (HashMap<?, ?>) linksList.get(i);

				request = init().baseUri(checkUrlString((String) links.get("href"))).accept(JSON).when().request(GET);
				request.then().statusCode(200);
				response = request.jsonPath();

				ArrayList<?> tilesetsLinksList = (ArrayList<?>) response.get("links");

				if (tileMatrixSetLimitsList == null) {
					tileMatrixSetLimitsList = (ArrayList<?>) response.get("tileMatrixSetLimits");

					if (tileMatrixSetLimitsList != null && !(tileMatrixSetLimitsList.size() == 0)) {

						for (int j = 0; j < Math.min(tileMatrixSetLimitsList.size(), 1); j++) {
							HashMap<?, ?> tileMatrixSetLimits = (HashMap<?, ?>) tileMatrixSetLimitsList.get(j);
							tileMatrix = tileMatrixSetLimits.get("tileMatrix").toString();
							maxTileRow = tileMatrixSetLimits.get("maxTileRow").toString();
							minTileCol = tileMatrixSetLimits.get("minTileCol").toString();
						}
					}
				}

				if (tilesetsLinksList == null) {
					continue;
				}

				for (Object tilesetsLink : tilesetsLinksList) {

					HashMap<?, ?> link = (HashMap<?, ?>) tilesetsLink;

					try {
						if (link.get("href").toString().contains("{" + this.tileMatrixTemplateString + "}")
								&& link.get("href").toString().contains("{" + this.tileRowTemplateString + "}")
								&& link.get("href").toString().contains("{" + this.tileColTemplateString + "}")) {
							if (testURL) {

								if (checkErrorResponse == false) {
									String newURL = link.get("href")
										.toString()
										.replace("{" + this.tileMatrixTemplateString + "}", tileMatrix)
										.replace("{" + this.tileRowTemplateString + "}", maxTileRow)
										.replace("{" + this.tileColTemplateString + "}", minTileCol);

									newURL = checkUrlString(newURL);

									URL urlStr = new URI(newURL).toURL();
									HttpURLConnection httpConn = (HttpURLConnection) urlStr.openConnection();

									int responseCode = httpConn.getResponseCode();

									if (responseCode != 200 && responseCode != 204) {
										errorMessages.append("Failed to check URL " + urlStr + ": ");
										errorMessages.append(
												"Expected status code 200 or 204 but received " + responseCode + " . ");
									}

									// https://github.com/opengeospatial/ets-ogcapi-tiles10/issues/32
									// check response for mapbox vector tiles in
									// webmercator format
									if (newURL.toLowerCase().contains("webmercatorquad")
											&& newURL.toLowerCase().contains("mvt")) {
										double maxTileRowDouble = Double.valueOf(maxTileRow);
										double minTileColDouble = Double.valueOf(minTileCol);
										double tileMatrixDouble = Double.valueOf(tileMatrix);

										Coordinate topleft = webMercatorPixel2deg(maxTileRowDouble, minTileColDouble,
												tileMatrixDouble, 0, 0);
										Coordinate bottomRight = webMercatorPixel2deg(maxTileRowDouble,
												minTileColDouble, tileMatrixDouble, 4096, 4096);

										Envelope bbox = new Envelope(topleft, bottomRight);

										VectorTileDecoder decoder = new VectorTileDecoder();

										FeatureIterable features = decoder.decode(toBytes((httpConn.getInputStream())));
										if (features != null) {
											features.forEach(f -> checkFeature(f, bbox, tileMatrixDouble,
													tileMatrixDouble, tileMatrixDouble));
										}
									}
								}
								else if (checkErrorResponse == true) {
									String newURL = link.get("href")
										.toString()
										.replace("{" + this.tileMatrixTemplateString + "}", tileMatrix)
										.replace("{" + this.tileRowTemplateString + "}",
												"" + (Integer.parseInt(maxTileRow) + 1))
										.replace("{" + this.tileColTemplateString + "}", minTileCol);

									newURL = checkUrlString(newURL);

									URL urlStr = new URI(newURL).toURL();
									HttpURLConnection httpConn = (HttpURLConnection) urlStr.openConnection();

									int responseCode = httpConn.getResponseCode();

									if (responseCode != 404 && responseCode != 400) {
										errorMessages.append("Failed to check URL " + urlStr + ": ");
										errorMessages.append(
												"Expected status code 404 or 400 but received " + responseCode + ". ");
									}

								}

							}
							foundTemplates = true;
							break;
						}
					}
					catch (Exception ee) {
						TestSuiteLogger.log(Level.SEVERE, ee.getMessage());
					}
				}
			}
		}
		if (foundTemplates == false)
			errorMessages.append("No URL templates were found.");

		return errorMessages.toString();
	}

	private String checkUrlString(String urlString) {
		try {
			URI getTilesURI = new URI(urlString);

			// check if URL is relative
			if (!getTilesURI.isAbsolute()) {
				String rootUriString = rootUri.toString().replace(rootUri.getPath(), "");
				if (rootUriString.endsWith("/") && urlString.startsWith("/")) {
					urlString = rootUriString.substring(0, rootUriString.length() - 1) + urlString;
				}
				else if (rootUriString.endsWith("/") && !urlString.startsWith("/")
						|| (!rootUriString.endsWith("/") && urlString.startsWith("/"))) {
					urlString = rootUriString + urlString;
				}
				else if (!rootUriString.endsWith("/") && !urlString.startsWith("/")) {
					urlString = rootUriString + "/" + urlString;
				}
			}

		}
		catch (URISyntaxException e) {
			TestSuiteLogger.log(Level.WARNING, "Could not construct absolute URL.", e);
		}
		return urlString;
	}

	private String findTileSetsUriString() {
		String tileSetsUriString = null;
		tileSetsUriString = findTileSetsUriStringInRoot();
		if (tileSetsUriString == null) {
			tileSetsUriString = findTileSetsUriStringInCollections();
		}
		return tileSetsUriString;
	}

	private String findTileSetsUriStringInRoot() {
		String tileSetsUriString = null;
		Response request = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/");
		request.then().statusCode(200);
		response = request.jsonPath();
		List<Object> links = response.getList("links");
		tileSetsUriString = findTileSetsUriStringInLinks(links);
		if (tileSetsUriString != null) {
			return tileSetsUriString;
		}
		return tileSetsUriString;
	}

	private String findTileSetsUriStringInCollections() {
		Response request = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/collections");
		request.then().statusCode(200);
		response = request.jsonPath();
		List<Object> collectionsList = response.getList("collections");
		String tileSetsUriString = null;
		for (Object collectionObj : collectionsList) {
			HashMap<?, ?> collection = (HashMap<?, ?>) collectionObj;
			ArrayList<?> collectionLinks = (ArrayList<?>) collection.get("links");
			tileSetsUriString = findTileSetsUriStringInLinks(collectionLinks);
			if (tileSetsUriString != null) {
				return tileSetsUriString;
			}
		}
		return null;
	}

	private String findTileSetsUriStringInLinks(List<?> links) {
		for (Object linkObj : links) {
			Map<String, Object> link = (Map<String, Object>) linkObj;
			String linkRel = (String) link.get("rel");
			String linkHref = (String) link.get("href");
			if (linkRel.toString().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-")) {
				return linkHref;
			}
		}
		return null;
	}

	private String parseTilesetMetadata(String urlString) {

		String tileSetMetadata = null;

		try {
			tileSetMetadata = readJSONObjectFromURL(new URL(urlString));
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return tileSetMetadata;
	}

	private static Coordinate webMercatorPixel2deg(double xtile, double ytile, double zoom, double xpixel,
			double ypixel) {
		double extent = 4096;
		double n = Math.pow(2.0, zoom);
		xtile = xtile + (xpixel / extent);
		ytile = ytile + ((extent - ypixel) / extent);
		double lon_deg = (xtile / n) * 360.0 - 180.0;
		double lat_rad = Math.atan(Math.sinh(Math.PI * (1 - 2 * ytile / n)));
		double lat_deg = Math.toDegrees(lat_rad);
		return new Coordinate(lon_deg, lat_deg);
	}

	private static byte[] toBytes(InputStream in) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[8192];
		int bytesRead = 0;
		while ((bytesRead = in.read(buf)) != -1) {
			baos.write(buf, 0, bytesRead);
		}
		return baos.toByteArray();
	}

	private static Object checkFeature(Feature f, Envelope bbox, double maxTileRowDouble, double minTileColDouble,
			double tileMatrixDouble) {
		if (f.getGeometry() != null) {
			if (f.getGeometry() instanceof Point) {
				try {
					Point p = (Point) f.getGeometry();
					Coordinate c1 = webMercatorPixel2deg(maxTileRowDouble, minTileColDouble, tileMatrixDouble, p.getX(),
							p.getY());
					assertTrue(bbox.contains(c1), "Coordinate outside tiles extent.");
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if (f.getGeometry() instanceof Polygon) {
				Polygon p = (Polygon) f.getGeometry();
				Coordinate[] coordinates = p.getCoordinates();
				for (Coordinate coordinate : coordinates) {
					Coordinate c1 = webMercatorPixel2deg(maxTileRowDouble, minTileColDouble, tileMatrixDouble,
							coordinate.getX(), coordinate.getY());
					assertTrue(bbox.contains(c1), "Coordinate" + c1 + " outside tiles extent: " + bbox);
				}
			}
		}
		else if (f.getGeometry() instanceof LineString) {
			LineString l = (LineString) f.getGeometry();
			Coordinate[] coordinates = l.getCoordinates();
			for (Coordinate coordinate : coordinates) {
				Coordinate c1 = webMercatorPixel2deg(maxTileRowDouble, minTileColDouble, tileMatrixDouble,
						coordinate.getX(), coordinate.getY());
				assertTrue(bbox.contains(c1), "Coordinate" + c1 + " outside tiles extent: " + bbox);
			}

		}
		return null;
	}

	public String readJSONObjectFromURL(URL requestURL) throws IOException {

		HttpURLConnection urlConnection = (HttpURLConnection) requestURL.openConnection();
		urlConnection.setRequestProperty("Accept", "application/json");
		InputStream is = urlConnection.getInputStream();
		try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.toString())) {
			scanner.useDelimiter("\\A");

			return (scanner.hasNext() ? scanner.next() : "");
		}

	}

}
