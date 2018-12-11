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

import com.modak.botworks.bots.common.BotCommon;
import com.modak.utils.JDBCConnectionManager;
import com.modak.utils.MessageUtils;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OnDemandJDBCBot class extends basebot which is used to execute the jdbc queries and return respective results.
 * The difference between this bot and JDBCBot is that its connection object is created on the fly i.e whenever a new message comes to bot.
 * The connection details will be sent in the incoming message to the bot
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2017-11-02
 */
public class OnDemandJDBCBot extends BaseBot {

    private final Logger logger = LogManager.getLogger(this.getClass().getSimpleName());

    @Override
    public void initBot(HashMap<String, Object> props) throws Exception {
        super.initBot(props);
    }

    @Override
    protected String processBotLogic(HashMap<String, Object> inputDataMap, String templateProcessedOutput) throws Exception {

        if (templateProcessedOutput == null) {
            throw new NullPointerException(BotCommon.OUTPUT_NULL);
        }
        if (inputDataMap != null && inputDataMap.get(BotCommon.ONDEMAND_CONFIG) instanceof HashMap) {
            JDBCConnectionManager connectionManager = new JDBCConnectionManager();
            Connection con = null;
            try {
                HashMap<String, Object> map = MessageUtils.getMap(inputDataMap, BotCommon.ONDEMAND_CONFIG);
                connectionManager.configureHikariDataSource(map);
                HikariDataSource hikariCP = connectionManager.getHikariDataSource();
                con = hikariCP.getConnection();
                List<Map<String, Object>> queryOutputList = executeSelectQuery(templateProcessedOutput, con);
                con.close();
                return queryOutputList != null ? new JSONArray(queryOutputList).toString() : null;
            } catch (Exception e) {
                logger.error(BotCommon.EXECUTION_ERROR, e);
                throw new Exception(BotCommon.EXECUTION_DESC + e.getMessage());
            } finally {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
                if (connectionManager != null) {
                    connectionManager.shutdownHikariDatasource();
                }
            }
        } else {
            logger.error(BotCommon.INVALID_INPUT_DATAMAP);
            throw new Exception(BotCommon.INVALID_INPUT_DATAMAP);
        }
    }

    /**
     * Executes select statement query
     *
     * @param query to be executed
     * @return the query result ListOf Map objects
     * @throws Exception if it fails to execute query
     */
    public List<Map<String, Object>> executeSelectQuery(String query, Connection con) throws Exception {
        QueryRunner queryRunner = new QueryRunner();
        List<Map<String, Object>> outputsMap = null;
        try {
            outputsMap = queryRunner.query(con, query, new MapListHandler());
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(BotCommon.QUERY_EXECUTION_ERROR + e.getMessage());
        }
        return outputsMap;
    }


}
