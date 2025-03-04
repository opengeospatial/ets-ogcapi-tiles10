package org.opengis.cite.ogcapitiles10;

import static io.restassured.RestAssured.given;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;

import org.opengis.cite.ogcapitiles10.conformance.RequirementClass;
import org.opengis.cite.ogcapitiles10.util.ClientUtils;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;

/**
 * A supporting base class that sets up a common test fixture. These configuration methods
 * are invoked before those defined in a subclass.
 */
public class CommonFixture {

	private ByteArrayOutputStream requestOutputStream = new ByteArrayOutputStream();

	private ByteArrayOutputStream responseOutputStream = new ByteArrayOutputStream();

	private final String conformance_class_not_implemented = "Skipped because Conformance Class %s is not implemented.";

	protected RequestLoggingFilter requestLoggingFilter;

	protected ResponseLoggingFilter responseLoggingFilter;

	protected URI rootUri;

	protected final String missing_landing_page_error_message = "Skipped because a Landing Page was not provided";

	protected final String missing_api_definition_error_message = "Skipped because the API definition could not be retrieved";

	protected final String dataset_tilesets_conformance_class_not_implemented = String
		.format(conformance_class_not_implemented, RequirementClass.DATASET_TILES.getConformanceClass());

	protected final String geodata_tilesets_conformance_class_not_implemented = String
		.format(conformance_class_not_implemented, RequirementClass.GEODATA_TILESETS.getConformanceClass());

	protected final String tilesets_lists_conformance_class_not_implemented = String
		.format(conformance_class_not_implemented, RequirementClass.TILESETS_LIST.getConformanceClass());

	/**
	 * Initializes the common test fixture with a client component for interacting with
	 * HTTP endpoints.
	 * @param testContext The test context that contains all the information for a test
	 * run, including suite attributes.
	 */
	@BeforeClass
	public void initCommonFixture(ITestContext testContext) {
		initLogging();

		rootUri = (URI) testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());

	}

	@BeforeMethod
	public void clearMessages() {
		initLogging();
	}

	public String getRequest() {
		return requestOutputStream.toString();
	}

	public String getResponse() {
		return responseOutputStream.toString();
	}

	protected RequestSpecification init() {
		return given().filters(requestLoggingFilter, responseLoggingFilter).log().all();
	}

	/**
	 * Obtains the (XML) response entity as a DOM Document. This convenience method wraps
	 * a static method call to facilitate unit testing (Mockito workaround).
	 * @param response A representation of an HTTP response message.
	 * @param targetURI The target URI from which the entity was retrieved (may be null).
	 * @return A Document representing the entity.
	 *
	 * @see ClientUtils#getResponseEntityAsDocument public Document
	 * getResponseEntityAsDocument( ClientResponse response, String targetURI ) { return
	 * ClientUtils.getResponseEntityAsDocument( response, targetURI ); }
	 */

	/**
	 * Builds an HTTP request message that uses the GET method. This convenience method
	 * wraps a static method call to facilitate unit testing (Mockito workaround).
	 * @return A ClientRequest object.
	 *
	 * @see ClientUtils#buildGetRequest public ClientRequest buildGetRequest( URI
	 * endpoint, Map<String, String> qryParams, MediaType... mediaTypes ) { return
	 * ClientUtils.buildGetRequest( endpoint, qryParams, mediaTypes ); }
	 */

	private void initLogging() {
		this.requestOutputStream = new ByteArrayOutputStream();
		this.responseOutputStream = new ByteArrayOutputStream();
		PrintStream requestPrintStream = new PrintStream(requestOutputStream, true);
		PrintStream responsePrintStream = new PrintStream(responseOutputStream, true);
		requestLoggingFilter = new RequestLoggingFilter(requestPrintStream);
		responseLoggingFilter = new ResponseLoggingFilter(responsePrintStream);
	}

	public String formatLinkURI(String scheme, String host, String hrefLink) {
		String newURL = "";
		if (hrefLink.contains(scheme + "://" + host)) {
			newURL = hrefLink;
		}
		else {
			newURL = scheme + "://" + host + hrefLink;
		}

		return newURL;
	}

}