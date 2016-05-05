package io.zeymo.network.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created By Zeymo at 14/11/11 14:31
 */
public abstract class EnhanceInputStream extends InputStream{

    public void readFully(byte[] b, int off, int len)
            throws IOException {
        int n = 0;
        while (n < len) {
            int nread = read(b, off+ n, len- n);
            if (nread < 0) {
                throw new EOFException("End of file reached before reading fully.");
            }
            n += nread;
        }
    }
}
