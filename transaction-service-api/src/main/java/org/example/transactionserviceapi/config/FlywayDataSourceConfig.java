package org.example.transactionserviceapi.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayDataSourceConfig {
    @Bean
    @Qualifier("flywayDs0")
    public DataSource flywayDs0() {
        return create("jdbc:postgresql://postgres-ds0:5432/wallet_ds0");
    }

    @Bean
    @Qualifier("flywayDs1")
    public DataSource flywayDs1() {
        return create("jdbc:postgresql://postgres-ds1:5432/wallet_ds1");
    }

    private DataSource create(String url) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername("wallet_user");
        ds.setPassword("wallet_pass");
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setMaximumPoolSize(2);
        return ds;
    }
}
