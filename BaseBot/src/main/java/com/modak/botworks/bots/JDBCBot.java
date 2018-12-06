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

package com.modak.botworks.bots;

import com.modak.utils.MessageUtils;
import com.modak.botworks.bots.common.BotCommon;
import com.modak.utils.JDBCConnectionManager;
import com.modak.utils.JSONUtils;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBCBot class extends basebot which is used to execute the jdbc queries and return respective results.
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2017-11-02
 */
public class JDBCBot extends BaseBot {

    private final Logger logger = LogManager.getLogger(this.getClass().getSimpleName());
    private JDBCConnectionManager connectionManager = null;

    /**
     * This is used to init jdbcConnection manager which is used to get connection object for executing a query
     *
     * @param configuration This HashMap contains the necessary information for initializing jdbcConnection manager
     * @throws Exception If the Bot cannot be initialized.
     */
    @Override
    public void initBot(HashMap<String, Object> configuration) throws Exception {
        connectionManager = new JDBCConnectionManager();
        if (configuration != null && configuration.get(BotCommon.DATABASE_CONFIG) instanceof HashMap) {
            HashMap<String, Object> map = MessageUtils.getMap(configuration, BotCommon.DATABASE_CONFIG);
            connectionManager.configureHikariDataSource(map);
        }
        else {
            logger.error(BotCommon.INVALID_ARG);
            throw new BotException(BotCommon.INVALID_ARG);
        }
        super.initBot(configuration);
    }

    /**
     * Execute the jdbc query based on query_type
     *
     * @param dataMap           This hashmap contains a query_type based on type it executes a selectQuery or
     *                          updateQuery
     * @param appTemplateOutput query to be executed
     * @return query output as json string
     * @throws Exception failed to execute query
     */
    @Override
    protected String processBotLogic(HashMap<String, Object> dataMap, String appTemplateOutput) throws Exception {
        if (appTemplateOutput == null) {
            throw new NullPointerException(BotCommon.QUERY_NULL);
        }
        String queryType = MessageUtils.getString(dataMap, BotCommon.QUERY_TYPE);
        if (queryType != null) {
            queryType = queryType.trim();
            if (queryType.equalsIgnoreCase(BotCommon.QUERY_TYPE_SELECT)) {
                List<Map<String, Object>> queryOutputList = executeSelectQuery(appTemplateOutput);
                return JSONUtils.object2JsonString(queryOutputList);
            }
            else if (queryType.equalsIgnoreCase(BotCommon.QUERY_TYPE_CREATE) || queryType
                .equalsIgnoreCase(BotCommon.QUERY_TYPE_UPDATE) || queryType
                .equalsIgnoreCase(BotCommon.QUERY_TYPE_INSERT) || queryType
                .equalsIgnoreCase(BotCommon.QUERY_TYPE_DELETE)) {
                HashMap<String, Object> queryOutputMap = executeUpdateQuery(appTemplateOutput);
                return JSONUtils.object2JsonString(queryOutputMap);
            }
            else {
                throw new BotException(BotCommon.QUERY_TYPE_INVALID);
            }
        }
        else {
            throw new NullPointerException(BotCommon.QUERY_TYPE_NULL);
        }
    }

    /**
     * Executes select statement query
     *
     * @param query to be executed
     * @return the query result has map of list
     * @throws Exception if it fails to execute query
     */
    public List<Map<String, Object>> executeSelectQuery(String query) throws Exception {
        QueryRunner queryRunner = new QueryRunner();
        HikariDataSource hikariCP = connectionManager.getHikariDataSource();
        Connection con = null;
        List<Map<String, Object>> outputsMap;
        try {
            con = hikariCP.getConnection();
            outputsMap = queryRunner.query(con, query, new MapListHandler());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new BotException(BotCommon.QUERY_EXECUTION_ERROR + e.getMessage());
        }
        finally {
            try {
                if (con != null) {
                    con.close();
                }
            }
            catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return outputsMap;
    }

    /**
     * Executes update, delete, create or insert statements
     *
     * @return updated number of rows if it is an update, delete, or insert statement. 'create' statement returns 1 by
     * default.
     * @throws Exception if it fails to execute query
     */
    public HashMap<String, Object> executeUpdateQuery(String query) throws Exception {
        QueryRunner queryRunner = new QueryRunner();
        HikariDataSource hikariCP = connectionManager.getHikariDataSource();
        Connection con = null;
        HashMap<String, Object> affected_rows_count_Map = new HashMap<>();
        try {
            con = hikariCP.getConnection();
            int affected_rows = queryRunner.update(con, query);
            affected_rows_count_Map.put(BotCommon.AFFECTED_ROWS_NUMBER, affected_rows);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new BotException(BotCommon.QUERY_EXECUTION_ERROR + e);
        }
        finally {
            try {
                if (con != null) {
                    con.close();
                }
            }
            catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return affected_rows_count_Map;
    }


    /**
     * Shutdowns the connection manager
     *
     * @throws Exception fails to shutdown connection manager
     */
    @Override
    public void shutdown() throws Exception {
        super.shutdown();
        if (connectionManager != null) {
            connectionManager.shutdownHikariDatasource();
        }
    }


}
