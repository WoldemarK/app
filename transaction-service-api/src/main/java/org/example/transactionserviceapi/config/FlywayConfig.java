package org.example.transactionserviceapi.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {
    @Bean
    public Flyway flyway(@Qualifier("flywayDs0") DataSource ds0,
                         @Qualifier("flywayDs1") DataSource ds1) {
        migrate(ds0);
        migrate(ds1);
        return Flyway.configure()
                .dataSource(ds0)
                .load();
    }

    private void migrate(DataSource dataSource) {
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load()
                .migrate();
    }
}
