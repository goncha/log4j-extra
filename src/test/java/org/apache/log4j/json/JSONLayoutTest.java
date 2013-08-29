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
    }

}
