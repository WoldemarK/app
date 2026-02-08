package org.example.transactionserviceapi.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class ShardingSphereConfig {

    @Bean
    public DataSource shardingDataSource() throws Exception {

        Map<String, DataSource> dataSourceMap = new HashMap<>();

        dataSourceMap.put("ds0", createDataSource(
                "jdbc:postgresql://postgres-ds0:5432/wallet_ds0",
                "wallet_user",
                "wallet_pass"));

        dataSourceMap.put("ds1", createDataSource(
                "jdbc:postgresql://postgres-ds1:5432/wallet_ds1",
                "wallet_user",
                "wallet_pass"));


        ShardingTableRuleConfiguration userTableRule = new ShardingTableRuleConfiguration
                ("user", "ds${0..1}.user_${0..1}");


        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(userTableRule);

        Properties props = new Properties();
        shardingRuleConfig.getShardingAlgorithms().put("uuid_hash_mod",
                new AlgorithmConfiguration(UserUuidShardingAlgorithm.class.getName(), props));

        userTableRule.setTableShardingStrategy(
                new StandardShardingStrategyConfiguration("user_id", "uuid_hash_mod"));

        return ShardingSphereDataSourceFactory.createDataSource(
                dataSourceMap,
                Collections.singleton(shardingRuleConfig),
                new Properties()
        );
    }


    private DataSource createDataSource(String url, String username, String password) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName("org.postgresql.Driver");
        return ds;
    }
}


