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

package com.modak.botworks.messagebus;

import com.modak.compression.NullCompressor;
import com.modak.encryption.EncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * This Utility class contains methods to do compression-encryption and decryption-uncompression of kafka messages. For
 * this a combination of Symmetric and Asymmetric encryption is used. This is taken as a part of ModakUtils utility JAR,
 * presented here for
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2018-03-06
 */

public class BotsMessageEncryptionUtils {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private static NullCompressor nullCompressor = new NullCompressor();

    /**
     * This method takes compressed-encrypted bot message (in byte[]) and a private key and then does decryption and
     * then uncompress the byte[] bot data and returns the string of the bot message
     *
     * @param encyptedBytes - compressed-encrypted bot message in byte[]
     * @param privateKey - private key used in Asymmetric encryption
     * @return - bot message in string which is decrypted and uncompressed
     */
    public static String decryptAndUncompressString(byte[] encyptedBytes, PrivateKey privateKey) throws Exception {
        if (encyptedBytes != null && privateKey != null) {
            byte[] decryptedBytes = EncryptionUtils.decryptLargeByteArrayUsingRSA(encyptedBytes, privateKey);
            return nullCompressor.uncompressToString(decryptedBytes);
        }
        return null;
    }

    /**
     * This method takes bot message (byte[]) and a public key and then does compression and then encrypts the byte[]
     * and returns the byte[] of the bot message
     *
     * @param botMessage - Bot message in byte[]
     * @param publicKey - public key used in Asymmetric encryption
     * @return - byte[] compressed and encrypted data
     */
    public static byte[] compressAndEncryptByteArray(byte[] botMessage, PublicKey publicKey) throws Exception {
        if (botMessage != null && publicKey != null) {
            byte[] compressedBytes = nullCompressor.compressByteArray(botMessage);
            return EncryptionUtils.encryptLargeByteArrayUsingRSA(compressedBytes, publicKey);
        }
        return null;
    }

    /**
     * This method takes compressed-encrypted bot message (in byte[]) and a private key and then does decryption and
     * then uncompress the byte[] bot data and returns the byte[] of the data
     *
     * @param encyptedBytes - compressed-encrypted bot message in byte[]
     * @param privateKey - private key used in Asymmetric encryption
     * @return - bot message in byte[] which is decrypted and uncompressed
     */
    public static byte[] decryptAndUncompressByteArray(byte[] encyptedBytes, PrivateKey privateKey) throws Exception {
        if (encyptedBytes != null && privateKey != null) {
            byte[] decryptedBytes = EncryptionUtils.decryptLargeByteArrayUsingRSA(encyptedBytes, privateKey);
            return nullCompressor.uncompressToByteArray(decryptedBytes);
        }
        return null;
    }

}
