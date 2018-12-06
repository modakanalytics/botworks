package com.modak.compression;

import java.nio.charset.StandardCharsets;

public class NullCompressor implements DataCompressor {

    @Override
    public byte[] compressString(String toBeCompressedData) {
        return toBeCompressedData.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String uncompressToString(byte[] compressedBytes) {
        return new String(compressedBytes);
    }

    @Override
    public byte[] compressByteArray(byte[] toBeCompressedData) {
        return toBeCompressedData;
    }

    @Override
    public byte[] uncompressToByteArray(byte[] compressedBytes) {
        return compressedBytes;
    }

}
