package org.opengis.cite.ogcapitiles10.apidefinition;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapitiles10.EtsAssert.assertTrue;
import static org.opengis.cite.ogcapitiles10.OgcApiTiles10.OPEN_API_MIME_TYPE;
import static org.opengis.cite.ogcapitiles10.SuiteAttribute.API_MODEL;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.opengis.cite.ogcapitiles10.CommonFixture;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.reprezen.kaizen.oasparser.OpenApi3Parser;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;
import com.reprezen.kaizen.oasparser.val.ValidationResults;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * A.2.3. API Definition Path {root}/api (link)
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class ApiDefinition extends CommonFixture {

    private String response;

    private String apiUrl;

    @BeforeClass(dependsOnMethods = "initCommonFixture")
    public void retrieveApiUrl() {
        Response request = init().baseUri( rootUri.toString() ).accept( JSON ).when().request( GET );
        JsonPath jsonPath = request.jsonPath();

        this.apiUrl = parseApiUrl( jsonPath );
    }

    /**
     * <pre>
     * Abstract Test 5: /ats/core/api-definition-op
     * Test Purpose: Validate that the API Definition document can be retrieved from the expected location.
     * Requirement: /req/core/api-definition-op
     *
     * Test Method
     *
     *  1. Construct a path for each API Definition link on the landing page
     *  2. Issue a HTTP GET request on each path
     *  3. Validate that a document was returned with a status code 200
     *  4. Validate the contents of the returned document using test /ats/core/api-definition-success.
     * </pre>
     */
    @Test(description = "Implements A.2.3. API Definition Path {root}/api (link), Abstract Test 5 (Requirement /req/core/api-definition-op)", groups = "apidefinition", dependsOnGroups = "landingpage")
    public void openapiDocumentRetrieval() {
        if ( apiUrl == null || apiUrl.isEmpty() )
            throw new AssertionError( "Path to the API Definition could not be constructed from the landing page" );
        Response request = init().baseUri( apiUrl ).accept( OPEN_API_MIME_TYPE ).when().request( GET );
        request.then().statusCode( 200 );
        response = request.asString();
    }

    /**
     * <pre>
     * Abstract Test 6: /ats/core/api-definition-success
     * Test Purpose: Validate that the API Definition complies with the required structure and contents.
     * Requirement: /req/core/api-definition-success
     *
     * Test Method: Validate the API Definition document against an appropriate schema document.
     * </pre>
     *
     * @param testContext
     *            never <code>null</code>
     * @throws MalformedURLException
     *             if the apiUrl is malformed
     */
    @Test(description = "Implements A.2.3. API Definition Path {root}/api (link), Abstract Test 6 (Requirement /req/core/api-definition-success)", groups = "apidefinition", dependsOnMethods = "openapiDocumentRetrieval")
    public void apiDefinitionValidation( ITestContext testContext )
                            throws MalformedURLException {
        OpenApi3Parser parser = new OpenApi3Parser();

        OpenApi3 apiModel = parser.parse( response, new URL( apiUrl ), true );
        assertTrue( apiModel.isValid(), createValidationMsg( apiModel ) );

        testContext.getSuite().setAttribute( API_MODEL.getName(), apiModel );
    }

    private String parseApiUrl( JsonPath jsonPath ) {
        for ( Object link : jsonPath.getList( "links" ) ) {
            Map<String, Object> linkMap = (Map<String, Object>) link;
            Object rel = linkMap.get( "rel" );
            Object type = linkMap.get( "type" );
            if ( "service-desc".equals( rel ) && OPEN_API_MIME_TYPE.equals( type ) )
                return (String) linkMap.get( "href" );
        }
        return null;
    }

    private String createValidationMsg( OpenApi3 model ) {
        StringBuilder sb = new StringBuilder();
        sb.append( "Landing Page is not valid. Found following validation items:" );
        if ( !model.isValid() ) {
            for ( ValidationResults.ValidationItem item : model.getValidationItems() ) {
                sb.append( "  - " ).append( item.getSeverity() ).append( ": " ).append( item.getMsg() );

            }
        }
        return sb.toString();
    }

}
