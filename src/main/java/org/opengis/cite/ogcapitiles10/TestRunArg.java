package org.opengis.cite.ogcapitiles10;

/**
 * An enumerated type defining all recognized test run arguments.
 */
public enum TestRunArg {

	/**
	 * An absolute URI that refers to a representation of the test subject or metadata
	 * about it.
	 */
	IUT,

	/**
	 * A URI identifying a tile matrix set definition.
	 */
	TILE_MATRIX_SET_DEFINITION_URI,

	/**
	 * The number of collections to test (a value less or equal to 0 means all
	 * collections).
	 */
	NOOFCOLLECTIONS;

	@Override
	public String toString() {
		return name().toLowerCase();
	}

}
