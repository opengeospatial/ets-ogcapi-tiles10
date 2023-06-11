package org.opengis.cite.ogcapitiles10.conformance;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

	/**
	 * <pre>
	 * Implements Abstract test A.2
	 * Addresses Requirement 1: /req/core/tc-op
	 * </pre>
	 */
	@Test(description = "Implements Abstract test A.2")
	public void validateTilesAreAvailable() {
		
		System.out.println("CHK 1");
		
		Response request = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET, "/");
		request.then().statusCode(200);
		response = request.jsonPath();

		List<Object> links = response.getList("links");

		
		boolean hasTilesets = false;

		for (Object linkObj : links) {
			Map<String, Object> link = (Map<String, Object>) linkObj;
			Object linkType = link.get("rel");
		
				if (link.get("rel").toString().startsWith("http://www.opengis.net/def/rel/ogc/1.0/tilesets-")) {
					parseTilesetMetadata(link.get("href").toString());
				}

			}

	
		
		System.out.println("CHK 10 "+hasTilesets);
		
		

	}

	private void parseTilesetMetadata(String urlString)
	{
		System.out.println("CHK 11 "+urlString);
		String tileSetMetadata = null;
		
		try {
			tileSetMetadata = readJSONObjectFromURL(new URL(urlString));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("CHK 11 "+tileSetMetadata);
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