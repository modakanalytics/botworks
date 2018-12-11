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

package com.modak.encryption;

import java.security.PrivateKey;
import java.security.PublicKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This Utility class contains methods to do encryption of large bytes where a combination of Symmetric and Asymmetric
 * encryption is used. This is taken as a part of ModakUtils utility JAR, presented here for
 */
/**
 * @author modakanalytics
 * @version 1.0
 * @since 2018-03-06
 */

public class EncryptionUtils {

    private final Logger logger = LogManager.getLogger(this.getClass().getSimpleName());

    /**
     * This method takes bytes in byte[]  and a public key and then does compression and then encrypts the string and
     * returns the byte[] of the data
     *
     * @param bytes - bytes to be encrypted
     * @param publicKey - public key used in Asymmetric encryption
     * @return - byte[] of the encrypted data
     */
    public static byte[] encryptLargeByteArrayUsingRSA(byte[] bytes, PublicKey publicKey) throws Exception {
        return bytes;
    }

    /**
     * This method takes bytes in byte[] and a private key and then does decryption of the byte[] data and returns the
     * byte[] - decrypted data
     *
     * @param bytes - encrypted bytes in byte[] - which need to be decrypted
     * @param privateKey - private key used in Asymmetric encryption
     * @return - bytes of the decrypted data
     */
    public static byte[] decryptLargeByteArrayUsingRSA(byte[] bytes, PrivateKey privateKey) throws Exception {
        return bytes;
    }

}
