package org.opengis.cite.ogcapitiles10;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.logging.Level;

import org.opengis.cite.ogcapitiles10.util.ClientUtils;
import org.opengis.cite.ogcapitiles10.util.TestSuiteLogger;
import org.opengis.cite.ogcapitiles10.util.URIUtils;
import org.testng.ISuite;
import org.testng.ISuiteListener;

import jakarta.ws.rs.client.Client;

/**
 * A listener that performs various tasks before and after a test suite is run, usually
 * concerned with maintaining a shared test suite fixture. Since this listener is loaded
 * using the ServiceLoader mechanism, its methods will be called before those of other
 * suite listeners listed in the test suite definition and before any annotated
 * configuration methods.
 *
 * Attributes set on an ISuite instance are not inherited by constituent test group
 * contexts (ITestContext). However, suite attributes are still accessible from lower
 * contexts.
 *
 * @see org.testng.ISuite ISuite interface
 */
public class SuiteFixtureListener implements ISuiteListener {

	@Override
	public void onStart(ISuite suite) {
		processSuiteParameters(suite);
		registerClientComponent(suite);
	}

	@Override
	public void onFinish(ISuite suite) {
		if (null != System.getProperty("deleteSubjectOnFinish")) {
			deleteTempFiles(suite);
			System.getProperties().remove("deleteSubjectOnFinish");
		}
	}

	/**
	 * Processes test suite arguments and sets suite attributes accordingly. The entity
	 * referenced by the {@link TestRunArg#IUT iut} argument is retrieved and written to a
	 * File that is set as the value of the suite attribute
	 * {@link SuiteAttribute#TEST_SUBJ_FILE testSubjectFile}.
	 * @param suite An ISuite object representing a TestNG test suite.
	 */
	void processSuiteParameters(ISuite suite) {
		Map<String, String> params = suite.getXmlSuite().getParameters();

		TestSuiteLogger.log(Level.CONFIG, "Suite parameters\n" + params.toString());
		String iutParam = params.get(TestRunArg.IUT.toString());
		if ((null == iutParam) || iutParam.isEmpty()) {
			// throw new IllegalArgumentException("1Required test run parameter not found:
			// " + TestRunArg.IUT.toString());
			// do nothing
		}
		else {
			URI iutRef = URI.create(iutParam.trim());
			suite.setAttribute(SuiteAttribute.IUT.getName(), iutRef);
			File entityFile = null;
			try {
				entityFile = URIUtils.dereferenceURI(iutRef);
			}
			catch (IOException iox) {
				throw new RuntimeException("Failed to dereference resource located at " + iutRef, iox);
			}
			TestSuiteLogger.log(Level.FINE, String.format("Wrote test subject to file: %s (%d bytes)",
					entityFile.getAbsolutePath(), entityFile.length()));
			suite.setAttribute(SuiteAttribute.TEST_SUBJ_FILE.getName(), entityFile);
		}

		String tileMatrixSetDefinitionURI = null;

		if (params.containsKey("tilematrixsetdefinitionuri")) {
			tileMatrixSetDefinitionURI = params.get("tilematrixsetdefinitionuri");
			if (tileMatrixSetDefinitionURI != null) {
				suite.setAttribute(SuiteAttribute.TILE_MATRIX_SET_DEFINITION_URI.getName(), tileMatrixSetDefinitionURI);
			}
		}

		String urlTemplateForTiles = null;

		if (params.containsKey("urltemplatefortiles")) {
			urlTemplateForTiles = params.get("urltemplatefortiles");
			if (urlTemplateForTiles != null) {
				suite.setAttribute(SuiteAttribute.URL_TEMPLATE_FOR_TILES.getName(), urlTemplateForTiles);
			}
		}

		String tileMatrix = null;

		if (params.containsKey("tilematrix")) {
			tileMatrix = params.get("tilematrix");
			if (tileMatrix != null) {
				suite.setAttribute(SuiteAttribute.TILE_MATRIX.getName(), tileMatrix);
			}
		}

		String minTileRow = null;

		if (params.containsKey("mintilerow")) {
			minTileRow = params.get("mintilerow");
			if (minTileRow != null) {
				suite.setAttribute(SuiteAttribute.MINIMUM_TILE_ROW.getName(), minTileRow);
			}
		}

		String maxTileRow = null;

		if (params.containsKey("maxtilerow")) {
			maxTileRow = params.get("maxtilerow");
			if (maxTileRow != null) {
				suite.setAttribute(SuiteAttribute.MAXIMUM_TILE_ROW.getName(), maxTileRow);
			}
		}

		String minTileCol = null;

		if (params.containsKey("mintilecol")) {
			minTileCol = params.get("mintilecol");
			if (minTileCol != null) {
				suite.setAttribute(SuiteAttribute.MINIMUM_TILE_COLUMN.getName(), minTileCol);
			}
		}

		String maxTileCol = null;

		if (params.containsKey("maxtilecol")) {
			maxTileCol = params.get("maxtilecol");
			if (maxTileCol != null) {
				suite.setAttribute(SuiteAttribute.MAXIMUM_TILE_COLUMN.getName(), maxTileCol);
			}
		}

		String noOfCollections = params.get(TestRunArg.NOOFCOLLECTIONS.toString());
		try {
			if (noOfCollections != null) {
				int noOfCollectionsInt = Integer.parseInt(noOfCollections);
				suite.setAttribute(SuiteAttribute.NO_OF_COLLECTIONS.getName(), noOfCollectionsInt);
			}
		}
		catch (NumberFormatException e) {
			TestSuiteLogger.log(Level.WARNING,
					String.format("Could not parse parameter %s: %s. Expected is a valid integer",
							TestRunArg.NOOFCOLLECTIONS.toString(), noOfCollections));
		}
	}

	/**
	 * A client component is added to the suite fixture as the value of the
	 * {@link SuiteAttribute#CLIENT} attribute; it may be subsequently accessed via the
	 * {@link org.testng.ITestContext#getSuite()} method.
	 * @param suite The test suite instance.
	 */
	void registerClientComponent(ISuite suite) {
		Client client = ClientUtils.buildClient();
		if (null != client) {
			suite.setAttribute(SuiteAttribute.CLIENT.getName(), client);
		}
	}

	/**
	 * Deletes temporary files created during the test run if TestSuiteLogger is enabled
	 * at the INFO level or higher (they are left intact at the CONFIG level or lower).
	 * @param suite The test suite.
	 */
	void deleteTempFiles(ISuite suite) {
		if (TestSuiteLogger.isLoggable(Level.CONFIG)) {
			return;
		}
		File testSubjFile = (File) suite.getAttribute(SuiteAttribute.TEST_SUBJ_FILE.getName());
		if (testSubjFile.exists()) {
			testSubjFile.delete();
		}
	}

}
