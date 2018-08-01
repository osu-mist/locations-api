package edu.oregonstate.mist.locations

import edu.oregonstate.mist.locations.db.DAOException
import edu.oregonstate.mist.locations.db.ExtensionDAO
import org.junit.Test

class ExtensionDAOTest {

    ExtensionDAO extensionDAO = new ExtensionDAO(["extensionThreshold": "1"], new Cache([:]))

    // Threshold is satisfied
    @Test
    void testParseExtensionData() {
        String testData = """\
        <response>
            <item key="0">
                <org_nid>test</org_nid>
                <GeoLocation>test</GeoLocation>
                <GroupName>test</GroupName>
                <StreetAddress>test</StreetAddress>
                <City>test</City>
                <State>test</State>
                <ZIPCode>test</ZIPCode>
                <BusinessHours>test</BusinessHours>
                <fax>test</fax>
                <tel/>
                <GUID>test</GUID>
                <county>test</county>
                <location_url>test</location_url>
                <field_program_category></field_program_category>
            </item>
        </response>
    """.stripIndent()
        extensionDAO.parseExtensionData(testData)
    }

    // Threshold is not satisfied
    @Test(expected = DAOException.class)
    void testUnderThreshold() {
        extensionDAO.parseExtensionData("<response></response>")
    }
}
