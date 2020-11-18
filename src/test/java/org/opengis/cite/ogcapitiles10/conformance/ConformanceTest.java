package org.opengis.cite.ogcapitiles10.conformance;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.opengis.cite.ogcapitiles10.conformance.RequirementClass.CORE;


import java.io.InputStream;
import java.util.List;

import org.opengis.cite.ogcapitiles10.conformance.Conformance;
import org.opengis.cite.ogcapitiles10.conformance.RequirementClass;
import org.testng.annotations.Test;

import io.restassured.path.json.JsonPath;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class ConformanceTest {

    @Test
    public void testParseAndValidateRequirementClasses() {
        Conformance conformanceOperationTest = new Conformance();
        InputStream json = ConformanceTest.class.getResourceAsStream( "req-classes.json" );
        JsonPath jsonPath = new JsonPath( json );
        List<RequirementClass> requirementClasses = conformanceOperationTest.parseAndValidateRequirementClasses( jsonPath );

        assertThat( requirementClasses.size(), is( 5 ) );
        assertThat( requirementClasses, hasItems( CORE) );
    }

    @Test(expectedExceptions = AssertionError.class)
    public void testParseAndValidateRequirementClasses_invalidConformsTo() {
        Conformance conformanceOperationTest = new Conformance();
        InputStream json = ConformanceTest.class.getResourceAsStream( "req-classes_invalidConformsTo.json" );
        JsonPath jsonPath = new JsonPath( json );
        conformanceOperationTest.parseAndValidateRequirementClasses( jsonPath );
    }

    @Test(expectedExceptions = AssertionError.class)
    public void testParseAndValidateRequirementClasses_invalidElementDataType() {
        Conformance conformanceOperationTest = new Conformance();
        InputStream json = ConformanceTest.class.getResourceAsStream( "req-classes_invalidElementDataType.json" );
        JsonPath jsonPath = new JsonPath( json );
        conformanceOperationTest.parseAndValidateRequirementClasses( jsonPath );
    }

}