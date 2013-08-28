/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j;

import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;

import java.io.*;

/**
 * Output serializaiton of {@link LoggingEvent} to a file.
 *
 * Implementation refers {@link FileAppender}.
 */
public class ObjectFileAppender extends AppenderSkeleton {

    /** Controls file truncatation. The default value for this variable
     * is <code>true</code>, meaning that by default a
     * <code>FileAppender</code> will append to an existing file and not
     * truncate it.
     *
     * <p>This option is meaningful only if the FileAppender opens the
     * file.
     */
    protected boolean fileAppend = true;

    /**
     The name of the log file. */
    protected String fileName = null;


    protected ObjectOutputStream objStream = null;


    public ObjectFileAppender() {}

    /**
     * Instantiate a <code>ObjectFileAppender</code> and open the file
     * designated by <code>filename</code>. The opened filename will
     * become the output destination for this appender.
     *
     * <p>If the <code>append</code> parameter is true, the file will be
     * appended to. Otherwise, the file designated by
     * <code>filename</code> will be truncated before being opened.
     */
    public ObjectFileAppender(String filename, boolean append) throws IOException {
        setFile(filename, append);
    }


    /**
     * Instantiate a FileAppender and open the file designated by
     * <code>filename</code>. The opened filename will become the output
     * destination for this appender.
     */
    public ObjectFileAppender(String filename) throws IOException {
        this(filename, true);
    }


    /**
     * Returns the value of the <b>File</b> option.
     */
    public String getFile() {
        return fileName;
    }

    /**
     * Returns the value of the <b>Append</b> option.
     */
    public boolean getAppend() {
        return fileAppend;
    }

    /**
     * The <b>File</b> property takes a string value which should be the
     * name of the file to append to.
     *
     * <p><font color="#DD0044"><b>Note that the special values
     * "System.out" or "System.err" are no longer honored.</b></font>
     *
     * <p>Note: Actual opening of the file is made when {@link
     * #activateOptions} is called, not when the options are set.
     */
    public void setFile(String file) {
        String val = file.trim();
        fileName = val;
    }

    /**
     * The <b>Append</b> option takes a boolean value. It is set to
     * <code>true</code> by default. If true, then <code>File</code>
     * will be opened in append mode by {@link #setFile setFile} (see
     * above). Otherwise, {@link #setFile setFile} will open
     * <code>File</code> in truncate mode.
     *
     * <p>Note: Actual opening of the file is made when {@link
     * #activateOptions} is called, not when the options are set.
     */
    public void setAppend(boolean flag) {
        fileAppend = flag;
    }


    protected void closeStream() {
        if(objStream != null) {
            try {
                objStream.close();
            } catch(IOException e) {
                if (e instanceof InterruptedIOException) {
                    Thread.currentThread().interrupt();
                }
                // There is do need to invoke an error handler at this late
                // stage.
                LogLog.error("Could not close " + objStream, e);
            }
        }
    }


    /**
     * <p>Sets and <i>opens</i> the file where the log output will
     * go. The specified file must be writable.
     *
     * <p>If there was already an opened file, then the previous file
     * is closed first.
     *
     * <p><b>Do not use this method directly. To configure a FileAppender
     * or one of its subclasses, set its properties one by one and then
     * call activateOptions.</b>
     */
    public synchronized void setFile(String fileName, boolean append)
            throws IOException {
        LogLog.debug("setFile called: " + fileName + ", " + append);


        closeStream();
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(fileName, append);
        } catch(FileNotFoundException ex) {
            String parentName = new File(fileName).getParent();
            if (parentName != null) {
                File parentDir = new File(parentName);
                if(!parentDir.exists() && parentDir.mkdirs()) {
                    stream = new FileOutputStream(fileName, append);
                } else {
                    throw ex;
                }
            } else {
                throw ex;
            }
        }

        this.objStream = new ObjectOutputStream(stream);
        this.fileName = fileName;
        this.fileAppend = append;

        LogLog.debug("setFile ended");
    }

    @Override
    public void activateOptions() {
        super.activateOptions();

        if(fileName != null) {
            try {
                setFile(fileName, fileAppend);
            } catch(java.io.IOException e) {
                errorHandler.error("setFile("+fileName+","+fileAppend+") call failed.",
                        e, ErrorCode.FILE_OPEN_FAILURE);
            }
        } else {
            //LogLog.error("File option not set for appender ["+name+"].");
            LogLog.warn("File option not set for appender ["+name+"].");
            LogLog.warn("Are you using FileAppender instead of ConsoleAppender?");
        }
    }

    /**
     * This method determines if there is a sense in attempting to append.
     *
     * <p>It checks whether there is a set output target and also if
     * there is a set layout. If these checks fail, then the boolean
     * value <code>false</code> is returned.
     */
    protected boolean checkEntryConditions() {
        if(this.closed) {
            LogLog.warn("Not allowed to write to a closed appender.");
            return false;
        }

        if(this.objStream == null) {
            errorHandler.error("No output stream or file set for the appender named ["+
                    name+"].");
            return false;
        }

        return true;
    }

    @Override
    protected void append(LoggingEvent event) {
        if (checkEntryConditions() && event != null) {
            try {
                event.getLocationInformation();

                event.getNDC();
                event.getThreadName();
                event.getMDCCopy();
                event.getRenderedMessage();
                event.getThrowableStrRep();

                objStream.writeObject(event);
                objStream.flush();
                objStream.reset(); // prevent memory leak
            } catch (IOException e) {
                errorHandler.error("Cannot write to file set for the appender named ["+
                        name+"].");
            }
        }
    }

    @Override
    public void close() {
        if(this.closed)
            return;
        this.closed = true;
        closeStream();
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
