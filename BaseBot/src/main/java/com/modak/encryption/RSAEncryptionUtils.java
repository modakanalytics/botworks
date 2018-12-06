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


import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used for generating RSA keys i.e., public and private keys and encrypting and decrypting the password
 *  @author modakanalytics
 *  @version 1.0
 *  @since 2017-11-02
 **/
public class RSAEncryptionUtils {

    private final Logger logger = LogManager.getLogger(this.getClass().getSimpleName());

    /**
     * This method gets the privateKey using the file
     *
     * @param filePath the file where the privateKey is provided
     * @return returns the private key
     * @throws Exception when the path provided is null
     */
    public static PrivateKey getPrivateKey(String filePath) throws Exception {
        if (filePath != null) {
            File f = new File(filePath);
            if (f.exists()) {
                byte[] keyBytes = Files.readAllBytes((new File(filePath)).toPath());
                PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                return kf.generatePrivate(spec);
            } else {
                throw new Exception("File not found in location " + filePath);
            }
        } else {
            throw new NullPointerException("null file");
        }
    }

    /**
     * This method gets the publicKey using the file
     *
     * @param filePath the file where the publicKey is provided
     * @return returns the public key
     * @throws Exception when the path provided is null
     */
    public static PublicKey getPublicKey(String filePath) throws Exception {
        if (filePath != null) {
            File f = new File(filePath);
            if (f.exists()) {
                byte[] keyBytes = Files.readAllBytes((new File(filePath)).toPath());
                X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                return kf.generatePublic(spec);
            } else {
                throw new Exception("File not found in location " + filePath);
            }
        } else {
            throw new NullPointerException("null file");
        }
    }
}
