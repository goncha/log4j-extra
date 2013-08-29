package org.apache.log4j.json;


import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class JSONLayoutTest extends LayoutTest {

    /**
     * Construct new instance of JSONLayoutTest.
     *
     * @param testName test name.
     */
    public JSONLayoutTest(String testName) {
        super(testName, "text/plain", false, null, null);
    }

    /**
     * Clear MDC and NDC before test.
     */
    public void setUp() {
        NDC.clear();
        if (MDC.getContext() != null) {
            MDC.getContext().clear();
        }
    }

    /**
     * Clear MDC and NDC after test.
     */
    public void tearDown() {
        setUp();
    }

    /**
     * @{inheritDoc}
     */
    protected Layout createLayout() {
        return new JSONLayout();
    }


    private void checkEventObject(final JSONObject object, final LoggingEvent event) {
        assertEquals(event.getLoggerName(), object.get("logger"));
        assertEquals(event.getTimeStamp(), object.get("timestamp"));
        assertEquals(event.getLevel().toString(), object.get("level"));
        assertEquals(event.getThreadName(), object.get("thread"));
    }

    public void testFormat() throws Exception {
        Logger logger = Logger.getLogger("org.apache.log4j.json.JSONLayoutTest");
        LoggingEvent event = new LoggingEvent("org.apache.log4j.Logger", logger, Level.INFO, "Hello, world", null);
        JSONLayout layout = new JSONLayout();
        String result = layout.format(event);
        JSONObject parsedResult = (JSONObject) JSONValue.parse(result);
        checkEventObject(parsedResult, event);

        assertEquals(5, parsedResult.keySet().size());
        assertNull(parsedResult.get("throwable"));
    }

    public void testFormatWithException() throws Exception {
        Logger logger = Logger.getLogger("org.apache.log4j.json.JSONLayoutTest");
        Exception ex = new IllegalArgumentException("required argument");
        LoggingEvent event = new LoggingEvent("org.apache.log4j.Logger", logger, Level.INFO, "Hello, world", ex);
        JSONLayout layout = new JSONLayout();
        String result = layout.format(event);
        JSONObject parsedResult = (JSONObject) JSONValue.parse(result);
        checkEventObject(parsedResult, event);

        assertNotNull(parsedResult.get("throwable"));
        assertTrue(parsedResult.get("throwable").toString().indexOf("required argument") >= 0);
    }

    public void testFormatWithNDC() throws Exception {
        Logger logger = Logger.getLogger("org.apache.log4j.json.JSONLayoutTest");

        NDC.push("NDC goes here");
        LoggingEvent event = new LoggingEvent("org.apache.log4j.Logger", logger, Level.INFO, "Hello, world", null);
        JSONLayout layout = new JSONLayout();
        String result = layout.format(event);
        NDC.pop();

        JSONObject parsedResult = (JSONObject) JSONValue.parse(result);
        checkEventObject(parsedResult, event);

        assertNotNull(parsedResult.get("NDC"));
        assertEquals("NDC goes here", parsedResult.get("NDC"));
    }

    public void testFormatWithMDC() throws Exception {
        Logger logger = Logger.getLogger("org.apache.log4j.json.JSONLayoutTest");

        MDC.put("mdc", "MDC goes here");
        LoggingEvent event = new LoggingEvent("org.apache.log4j.Logger", logger, Level.INFO, "Hello, world", null);
        JSONLayout layout = new JSONLayout();
        layout.setProperties(true);
        String result = layout.format(event);
        MDC.clear();

        JSONObject parsedResult = (JSONObject) JSONValue.parse(result);
        checkEventObject(parsedResult, event);

        assertNotNull(parsedResult.get("properties"));
        assertEquals("MDC goes here", ((JSONObject) parsedResult.get("properties")).get("mdc"));
    }

    public void testFormatWithoutMDC() throws Exception {
        Logger logger = Logger.getLogger("org.apache.log4j.json.JSONLayoutTest");

        MDC.put("mdc", "MDC goes here");
        LoggingEvent event = new LoggingEvent("org.apache.log4j.Logger", logger, Level.INFO, "Hello, world", null);
        JSONLayout layout = new JSONLayout();
        layout.setProperties(false);
        String result = layout.format(event);
        MDC.clear();

        JSONObject parsedResult = (JSONObject) JSONValue.parse(result);
        checkEventObject(parsedResult, event);

        assertNull(parsedResult.get("properties"));
    }

    public void testFormatWithLocationInfo() {
        Logger logger = Logger.getLogger("org.apache.log4j.json.JSONLayoutTest");
        LoggingEvent event = new LoggingEvent("org.apache.log4j.Logger", logger, Level.INFO, "Hello, world", null);
        JSONLayout layout = new JSONLayout();
        layout.setLocationInfo(true);
        String result = layout.format(event);

        JSONObject parsedResult = (JSONObject) JSONValue.parse(result);
        checkEventObject(parsedResult, event);

        JSONObject location = (JSONObject) parsedResult.get("location");
        assertNotNull(location);
    }

    public void testFormatWithoutLocationInfo() {
        Logger logger = Logger.getLogger("org.apache.log4j.json.JSONLayoutTest");
        LoggingEvent event = new LoggingEvent("org.apache.log4j.Logger", logger, Level.INFO, "Hello, world", null);
        JSONLayout layout = new JSONLayout();
        layout.setLocationInfo(false);
        String result = layout.format(event);

        JSONObject parsedResult = (JSONObject) JSONValue.parse(result);
        checkEventObject(parsedResult, event);

        JSONObject location = (JSONObject) parsedResult.get("location");
        assertNull(location);
    }

    public void testGetSetLocationInfo() {
        JSONLayout layout = new JSONLayout();
        assertFalse(layout.getLocationInfo());
        layout.setLocationInfo(true);
        assertTrue(layout.getLocationInfo());
        layout.setLocationInfo(false);
        assertFalse(layout.getLocationInfo());
    }

    public void testGetSetProperties() {
        JSONLayout layout = new JSONLayout();
        assertFalse(layout.getProperties());
        layout.setProperties(true);
        assertTrue(layout.getProperties());
        layout.setProperties(false);
        assertFalse(layout.getProperties());
    }

}
