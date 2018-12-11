/*
   Copyright 2018 modakanalytics.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.modak.compression;

import java.nio.charset.StandardCharsets;

/**
 * This Utility class contains methods to compress bot messages which are of String and BytArray messages.
 * This is taken as a part of ModakUtils utility JAR, presented here for
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2018-03-06
 */

public interface DataCompressor {

    /**
     * This method does the compression of String data using GZIP
     *
     * @param toBeCompressedData - data to be compressed
     * @return - compressed byte[] for the given String data
     */
    public byte[] compressString(String toBeCompressedData);


    /**
     * This method takes compressed data in bytes and returns the uncompressed String data
     *
     * @param compressedBytes - compressed bytes which need to be uncompressed
     * @return - uncompressed string format for the given compressedBytes
     */
    public String uncompressToString(byte[] compressedBytes);


    /**
     * This method does the compression for the given byte[] data and returns compressed byte[]
     *
     * @param toBeCompressedData - data to be compressed
     * @return - byte[] compressed bytes in byte[]
     */
    public byte[] compressByteArray(byte[] toBeCompressedData);

    /**
     * This method takes compressed data in bytes and returns the uncompressed bytes in byte[]
     *
     * @param compressedBytes - compressed bytes which need to be uncompressed
     * @return - uncompressed bytes for the given bytes
     */
    public byte[] uncompressToByteArray(byte[] compressedBytes);

}
