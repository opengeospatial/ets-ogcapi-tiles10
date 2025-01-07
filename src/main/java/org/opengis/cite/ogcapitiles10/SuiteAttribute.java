package org.opengis.cite.ogcapitiles10;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;

import com.reprezen.kaizen.oasparser.model3.OpenApi3;

import jakarta.ws.rs.client.Client;

/**
 * An enumerated type defining ISuite attributes that may be set to constitute a shared
 * test fixture.
 */
@SuppressWarnings("rawtypes")
public enum SuiteAttribute {

	/**
	 * A client component for interacting with HTTP endpoints.
	 */
	CLIENT("httpClient", Client.class),

	/**
	 * The root URL.
	 */
	IUT("instanceUnderTest", URI.class),

	/**
	 * A File containing the test subject or a description of it.
	 */
	TEST_SUBJ_FILE("testSubjectFile", File.class),

	/**
	 * The number of collections to test.
	 */
	NO_OF_COLLECTIONS("noOfCollections", Integer.class),

	/**
	 * Parsed OpenApi3 document resource /api; Added during execution.
	 */
	API_MODEL("apiModel", OpenApi3.class),

	/**
	 * Requirement classes parsed from /conformance; Added during execution.
	 */
	REQUIREMENTCLASSES("requirementclasses", List.class),

	/**
	 * Parsed collections from resource /collections; Added during execution.
	 */
	COLLECTIONS("collections", List.class),

	/**
	 * Collection names assigned to a feature id parsed from resource
	 * /collections/{name}/items; Added during execution.
	 */
	FEATUREIDS("featureIds", Map.class),

	/**
	 * The URL Template For Tiles.
	 */
	URL_TEMPLATE_FOR_TILES("urlTemplateForTiles", String.class),

	/**
	 * The range Of Valid Values
	 */
	RANGE_OF_VALID_VALUES("rangeOfValidValues", String.class),

	// ,,maxTileRow,minTileCol,maxTileCol

	/**
	 * Tile Matrix
	 */
	TILE_MATRIX("tileMatrix", String.class),

	/**
	 * Minimum Tile Row
	 */
	MINIMUM_TILE_ROW("minTileRow", String.class),

	/**
	 * Maximum Tile Row
	 */
	MAXIMUM_TILE_ROW("maxTileRow", String.class),

	/**
	 * Minimum Tile Column
	 */
	MINIMUM_TILE_COLUMN("minTileCol", String.class),

	/**
	 * Maximum Tile Column
	 */
	MAXIMUM_TILE_COLUMN("maxTileCol", String.class),

	/**
	 * The URI of the TileMatrixSet definition.
	 */
	TILE_MATRIX_SET_DEFINITION_URI("tileMatrixSetDefinitionURI", URI.class);

	private final Class attrType;

	private final String attrName;

	SuiteAttribute(String attrName, Class attrType) {
		this.attrName = attrName;
		this.attrType = attrType;
	}

	public Class getType() {
		return attrType;
	}

	public String getName() {
		return attrName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(attrName);
		sb.append('(').append(attrType.getName()).append(')');
		return sb.toString();
	}

}
