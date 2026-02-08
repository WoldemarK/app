package org.example.transactionserviceapi.config;


import lombok.Getter;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.Properties;
import java.util.UUID;

public class UserUuidShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>> {

    private Properties props = new Properties();

    @Override
    public void init(Properties props) {
        this.props = props;
    }


    public Properties getProps() {
        return this.props;
    }

    @Override
    public String getType() {
        return "UUID_HASH_MOD";
    }

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Comparable<?>> shardingValue) {
        UUID userId = (UUID) shardingValue.getValue();
        int shardIndex = Math.abs(userId.hashCode()) % availableTargetNames.size();

        return availableTargetNames.stream()
                .filter(name -> name.endsWith(String.valueOf(shardIndex)))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No shard found for user_uid=%s".formatted(userId)));
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Comparable<?>> shardingValue) {

        return availableTargetNames;


    }
}