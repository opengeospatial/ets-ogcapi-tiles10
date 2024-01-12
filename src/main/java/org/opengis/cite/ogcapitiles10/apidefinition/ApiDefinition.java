package org.opengis.cite.ogcapitiles10.apidefinition;

import com.reprezen.kaizen.oasparser.OpenApi3Parser;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;
import com.reprezen.kaizen.oasparser.val.ValidationResults;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.opengis.cite.ogcapitiles10.CommonFixture;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapitiles10.EtsAssert.assertTrue;
import static org.opengis.cite.ogcapitiles10.OgcApiTiles10.OPEN_API_MIME_TYPE;
import static org.opengis.cite.ogcapitiles10.SuiteAttribute.API_MODEL;
import static org.opengis.cite.ogcapitiles10.util.JsonUtils.parseAsListOfMaps;

/**
 * A.2.3. API Definition Path {root}/api (link)
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class ApiDefinition extends CommonFixture {

	private String response;

	private String apiUrl = null;

	@BeforeClass(dependsOnMethods = "initCommonFixture")
	public void retrieveApiUrl() {
		

		if(rootUri!=null)
		{
		
			Response request = init().baseUri(rootUri.toString()).accept(JSON).when().request(GET);
			JsonPath jsonPath = request.jsonPath();
	
			this.apiUrl = parseApiUrl(jsonPath);
			
		}

	}

	@BeforeClass(dependsOnMethods = "retrieveApiUrl")
	public void openapiDocumentRetrieval() {
		
	
		
		if (apiUrl != null)
		{		
		
			Response request = init().baseUri(apiUrl).accept(OPEN_API_MIME_TYPE).when().request(GET);
		
			request.then().statusCode(200);
		
			response = request.asString();
		}
		
	
	}

	/**
	 * Implements Abstract test A.23: /conf/oas30/completeness
	 * Partly addresses Requirement 22: /req/oas30/completeness
	 * @param testContext never <code>null</code>
	 * @throws MalformedURLException if the apiUrl is malformed
	 */
	@Test(description = "Implements Abstract test A.23, Requirement 22: /req/oas30/completeness",
			groups = "apidefinition")
	public void apiDefinitionValidation(ITestContext testContext) throws MalformedURLException {
		
		
		
		OpenApi3Parser parser = new OpenApi3Parser();

		if(apiUrl == null || apiUrl.isEmpty())
		{
			throw new SkipException(missing_api_definition_error_message);
		}
		
		OpenApi3 apiModel = parser.parse(response, new URL(apiUrl), true);
		assertTrue(apiModel.isValid(), createValidationMsg(apiModel));

		testContext.getSuite().setAttribute(API_MODEL.getName(), apiModel);
		
		
	}

	private String parseApiUrl(JsonPath jsonPath) {
		for (Object link : parseAsListOfMaps("links", jsonPath)) {
			Map<String, Object> linkMap = (Map<String, Object>) link;
			Object rel = linkMap.get("rel");
			Object type = linkMap.get("type");
			if ("service-desc".equals(rel) && OPEN_API_MIME_TYPE.equals(type)) {
				String url = (String) linkMap.get("href");
				if (!url.startsWith("http")) {
					String path = url;
					if (null != rootUri.getScheme() && !rootUri.getScheme().isEmpty())
						url = rootUri.getScheme() + ":";
					if (null != rootUri.getAuthority() && !rootUri.getAuthority().isEmpty())
						url = url + "//" + rootUri.getAuthority();
					url = url + path;
					if (null != rootUri.getQuery() && !rootUri.getQuery().isEmpty())
						url = url + "?" + rootUri.getQuery();
					if (null != rootUri.getFragment() && !rootUri.getFragment().isEmpty())
						url = url + "#" + rootUri.getFragment();
				}
				return url;
			}
		}
		return null;
	}

	private String createValidationMsg(OpenApi3 model) {
		StringBuilder sb = new StringBuilder();
		sb.append("Landing Page is not valid. Found following validation items:");
		if (!model.isValid()) {
			for (ValidationResults.ValidationItem item : model.getValidationItems()) {
				sb.append("  - ").append(item.getSeverity()).append(": ").append(item.getMsg());

			}
		}
		return sb.toString();
	}

}
