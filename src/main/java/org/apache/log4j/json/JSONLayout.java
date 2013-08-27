package org.apache.log4j.json;


import org.apache.log4j.Layout;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;

import java.util.Arrays;
import java.util.Set;

/**
 * The output of {@link LoggingEvent} in JSON format.
 */
public class JSONLayout extends Layout {

    private final int DEFAULT_SIZE = 384;
    private final int UPPER_LIMIT = 2048;

    private StringBuilder buf = new StringBuilder(DEFAULT_SIZE);

    boolean locationInfo = false;
    boolean properties = false;

    /**
     * The <b>LocationInfo</b> option takes a boolean value. By default,
     * it is set to false which means there will be no location
     * information output by this layout. If the the option is set to
     * true, then the file name and line number of the statement at the
     * origin of the log statement will be output.
     */
    public void setLocationInfo(boolean flag) {
        locationInfo = flag;
    }

    /**
     * Returns the current value of the <b>LocationInfo</b> option.
     */
    public boolean getLocationInfo() {
        return locationInfo;
    }

    /**
     * Sets whether MDC key-value pairs should be output, default false.
     */
    public void setProperties(final boolean flag) {
        properties = flag;
    }

    /**
     * Gets whether MDC key-value pairs should be output.
     */
    public boolean getProperties() {
        return properties;
    }


    @Override
    public String format(LoggingEvent event) {
        // Reset working buffer. If the buffer is too large, then we need a new
        // one in order to avoid the penalty of creating a large array.
        if(buf.capacity() > UPPER_LIMIT) {
            buf = new StringBuilder(DEFAULT_SIZE);
        } else {
            buf.setLength(0);
        }

        // START: event object
        startObject();

        field("logger").string(event.getLoggerName())
        .sep().field("timestamp").number(event.getTimeStamp())
        .sep().field("level").string(String.valueOf(event.getLevel()))
        .sep().field("thread").string(event.getThreadName())
        .sep().field("message").string(event.getRenderedMessage());


        String ndc = event.getNDC();
        if (ndc != null) {
            sep().field("NDC").string(ndc);
        }

        String[] sr = event.getThrowableStrRep();
        if (sr != null) {
            sep().field("throwable").string(sr);
        }

        if (locationInfo) {
            LocationInfo locationInfo = event.getLocationInformation();
            sep().field("location").startObject()
                    .field("class").string(locationInfo.getClassName())
                    .sep().field("method").string(locationInfo.getMethodName())
                    .sep().field("file").string(locationInfo.getFileName())
                    .sep().field("line").string(locationInfo.getLineNumber())
                    .endObject();
        }

        if (properties) {
            Set keySet = event.getPropertyKeySet();
            if (keySet.size() > 0) {
                sep().field("properties").startObject();
                Object[] keys = keySet.toArray();
                Arrays.sort(keys);
                for (int i = 0, j = 0; i < keys.length; i++) {
                    if (keys[i] == null) continue;
                    if (j > 0) sep();
                    String key = keys[i].toString();
                    Object val = event.getMDC(key);
                    field1(key).string(String.valueOf(val));
                    j++;
                }
                endObject();
            }
        }

        // END: event object
        endObject();

        buf.append("\r\n\r\n");
        return buf.toString();
    }

    @Override
    public boolean ignoresThrowable() {
        return false;
    }

    @Override
    public void activateOptions() {
    }

    private JSONLayout sep() {
        buf.append(",\r\n");
        return this;
    }

    private JSONLayout startObject() {
        buf.append("{\r\n");
        return this;
    }

    private JSONLayout endObject() {
        buf.append("\r\n}");
        return this;
    }

    private JSONLayout field(String name) {
        buf.append("\"").append(name).append("\": ");
        return this;
    }

    private JSONLayout field1(String name) {
        buf.append("\"");
        _string(name);
        buf.append("\": ");
        return this;
    }

    private JSONLayout number(long value) {
        buf.append(value);
        return this;
    }

    private JSONLayout string(String value) {
        if (value == null) {
            buf.append("null");
        } else {
            buf.append('\"');
            _string(value);
            buf.append('\"');
        }

        return this;
    }

    private JSONLayout string(String[] values) {
        buf.append('\"');
        for (int i = 0; i < values.length; i++) {
            if(i > 0) buf.append("\\r\\n");
            _string(values[i]);
        }
        buf.append('\"');
        return this;
    }

    private void _string(String value) {
        for (int i = 0, len = value.length(); i < len; i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\b':
                    buf.append("\\b");
                    break;
                case '\f':
                    buf.append("\\f");
                    break;
                case '\n':
                    buf.append("\\n");
                    break;
                case '\r':
                    buf.append("\\r");
                    break;
                case '\t':
                    buf.append("\\t");
                    break;
                case '"':
                    buf.append("\\\"");
                    break;
                case '/':
                    buf.append("\\/");
                    break;
                case '\\':
                    buf.append("\\\\");
                    break;
                default:
                    buf.append(ch);
            }
        }
    }
}
