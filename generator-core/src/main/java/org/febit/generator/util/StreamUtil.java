/**
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.generator.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import org.febit.wit.util.CharArrayWriter;

/**
 *
 * @author zqq90
 */
public class StreamUtil {

    private final static int ioBufferSize = 1024;

    public static void copy(InputStream input, Writer output, String encoding) throws IOException {
        copy(new InputStreamReader(input, encoding), output);
    }

    public static char[] readChars(InputStream in) throws IOException {
        return readChars(in, "UTF-8");
    }

    public static char[] readChars(InputStream in, String encoding) throws IOException {
        CharArrayWriter fastCharArrayWriter = new CharArrayWriter();
        StreamUtil.copy(in, fastCharArrayWriter, encoding);
        return fastCharArrayWriter.toArray();
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[ioBufferSize];
        int count = 0;
        int read;
        while (true) {
            read = input.read(buffer, 0, ioBufferSize);
            if (read == -1) {
                break;
            }
            output.write(buffer, 0, read);
            count += read;
        }
        return count;
    }

    public static int copy(Reader input, Writer output) throws IOException {
        char[] buffer = new char[ioBufferSize];
        int count = 0;
        int read;
        while ((read = input.read(buffer, 0, ioBufferSize)) >= 0) {
            output.write(buffer, 0, read);
            count += read;
        }
        output.flush();
        return count;
    }

    public static void close(Writer writer) {
        if (writer != null) {
            try {
                writer.flush();
            } catch (IOException ignore) {
            }
            try {
                writer.close();
            } catch (IOException ignore) {
            }
        }
    }
    
    public static void close(OutputStream out) {
        if (out != null) {
            try {
                out.flush();
            } catch (IOException ignore) {
            }
            try {
                out.close();
            } catch (IOException ignore) {
            }
        }
    }

    public static void close(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException ignore) {
            }
        }
    }
}
