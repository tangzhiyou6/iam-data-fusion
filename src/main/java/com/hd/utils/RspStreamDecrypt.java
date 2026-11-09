package com.hd.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface RspStreamDecrypt {
    void decryptStream(InputStream in, OutputStream out) throws IOException;
}
