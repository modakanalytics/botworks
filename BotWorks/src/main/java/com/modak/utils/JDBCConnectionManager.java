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

package com.modak.utils;

import com.modak.botworks.bots.common.BotCommon;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * This class is used to configure the connectionpool and establish the connection
 *  @author modakanalytics
 *  @version 1.0
 *  @since 2017-11-02
 */

public class JDBCConnectionManager {

    private final Logger logger = LogManager.getLogger(this.getClass().getSimpleName());

    private HikariDataSource hikariDataSource = null;

    public JDBCConnectionManager() {
    }

    /**
     * This is used to get the connectionpool
     *
     * @return returns the connectionpool
     */
    public HikariDataSource getHikariDataSource() {
        return hikariDataSource;
    }

    /**
     * This is used to set the connectionpool
     *
     * @param hikariDataSource connectionpool object required to set the connection
     */
    public void setHikariDataSource(HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
    }

    /**
     * This is used to configure the pool is the input is a file by changing it to hashMap
     *
     * @param configFile this file contains the parameters to configure the pool
     * @throws Exception throws exception if the file is not found
     */
    public void configureHikariDataSource(File configFile) throws Exception {
        String jsonString = FileUtils.readFileToString(configFile, Charset.defaultCharset());
        configureHikariDataSource(jsonString);
    }

    /**
     * This is used to configure the pool is the input is a json string by changing it to hashMap
     *
     * @param jsonString this json string contains the parameters to configure the pool
     * @throws Exception throws exception if the string is not found
     */

    public void configureHikariDataSource(String jsonString) throws Exception {
        HashMap<String, Object> connection_profile_map = JSONUtils.jsonToMap(jsonString);
        configureHikariDataSource(connection_profile_map);
    }

    /**
     * This is used to configure the connection pool by taking input as hashMap
     *
     * @param jdbc_connection_map this hashmap contains the data for configuring the connection pool
     * @throws Exception throws exception if the map is null
     */
    public void configureHikariDataSource(HashMap<String, Object> jdbc_connection_map) throws Exception {

        checkValidInputs(jdbc_connection_map);

        String password = MessageUtils.getString(jdbc_connection_map, BotCommon.PASSWORD);
        Class.forName(MessageUtils.getString(jdbc_connection_map, BotCommon.JDBC_DRIVER));
        // setup the connection pool
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(MessageUtils.getString(jdbc_connection_map, BotCommon.JDBC_URL));
        hikariConfig.setUsername(MessageUtils.getString(jdbc_connection_map, BotCommon.USER_NAME));
        hikariConfig.setPassword(password);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("autoReconnect", true);

        hikariConfig.setMinimumIdle(
            Integer.parseInt(MessageUtils.getString(jdbc_connection_map, BotCommon.MIN_CONNECTION_PER_PARTITION)));
        hikariConfig.setMaximumPoolSize(
            Integer.parseInt(MessageUtils.getString(jdbc_connection_map, BotCommon.MAX_CONNECTION_PER_PARTITION)));

        String leakDetectionThresholdVal = MessageUtils
            .getString(jdbc_connection_map, BotCommon.LEAK_DETECTION_THRESHOLD);
        if (leakDetectionThresholdVal != null) {
            hikariConfig.setLeakDetectionThreshold(Long.parseLong(leakDetectionThresholdVal));
        }

        hikariDataSource = new HikariDataSource(hikariConfig);
        setHikariDataSource(hikariDataSource);
    }

    /**
     * This class shutdowns the connection pool
     */
    public void shutdownHikariDatasource() {
        try {
            if (hikariDataSource != null) {
                // This method must be called only once when the application stops
                hikariDataSource.close();
                logger.debug("datasource closed successfully");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("failed to close datasource" + e.getMessage());
        }
    }

    /**
     * This class checks whether all the inputs provided are not null and throws exception if it is null
     *
     * @param jdbc_connection_map the map where all the configurations are provided
     */

    private void checkValidInputs(HashMap<String, Object> jdbc_connection_map) {
        if (jdbc_connection_map == null) {
            throw new NullPointerException("connection details map is null");
        }
        String jdbcDriver = MessageUtils.getString(jdbc_connection_map, BotCommon.JDBC_DRIVER);
        if (jdbcDriver == null) {
            throw new NullPointerException(BotCommon.JDBC_DRIVER + "is null");
        }
        String jdbcUrl = MessageUtils.getString(jdbc_connection_map, BotCommon.JDBC_URL);
        if (jdbcUrl == null) {
            throw new NullPointerException(BotCommon.JDBC_URL + "is null");
        }
        String userName = MessageUtils.getString(jdbc_connection_map, BotCommon.USER_NAME);
        if (userName == null) {
            throw new NullPointerException(BotCommon.USER_NAME + "is null");
        }
        String password = MessageUtils.getString(jdbc_connection_map, BotCommon.PASSWORD);
        if (password == null) {
            throw new NullPointerException(BotCommon.PASSWORD + "is null");
        }

        String encryptedPassword = MessageUtils.getString(jdbc_connection_map, BotCommon.ENCRYPTED_PASSWORD);
        if (encryptedPassword != null && encryptedPassword.equalsIgnoreCase(BotCommon.YES)) {
            String encryptionKeyPath = MessageUtils.getString(jdbc_connection_map, BotCommon.PRIVATE_KEY_FILE_PATH);
            if (encryptionKeyPath == null) {
                throw new NullPointerException(BotCommon.PRIVATE_KEY_FILE_PATH + " is null");
            }
        }
        String minConnectionsPerPartition = MessageUtils
            .getString(jdbc_connection_map, BotCommon.MIN_CONNECTION_PER_PARTITION);

        if (minConnectionsPerPartition == null) {
            throw new NullPointerException(BotCommon.MIN_CONNECTION_PER_PARTITION + "is null");
        }
        String maxConnectionsPerPartition = MessageUtils
            .getString(jdbc_connection_map, BotCommon.MIN_CONNECTION_PER_PARTITION);

        if (maxConnectionsPerPartition == null) {
            throw new NullPointerException(BotCommon.MAX_CONNECTION_PER_PARTITION + "is null");
        }
    }
}