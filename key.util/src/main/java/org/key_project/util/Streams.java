package org.key_project.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Streams {

    private Streams() {
        throw new Error("do not instantiate");
    }

    public static String toString(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[2048];
        int count;
        while ((count = is.read(buf)) >= 0) {
            baos.write(buf, 0, count);
        }
        return new String(baos.toByteArray());
    }

}
