// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import webit.script.util.FastCharArrayWriter;

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
        FastCharArrayWriter fastCharArrayWriter = new FastCharArrayWriter();
        StreamUtil.copy(in, fastCharArrayWriter, encoding);
        return fastCharArrayWriter.toCharArray();
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
