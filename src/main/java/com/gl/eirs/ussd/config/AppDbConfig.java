package com.gl.eirs.ussd.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {"com.gl.eirs.ussd.repository.app"},
        entityManagerFactoryRef = "springEntityManagerFactory",
        transactionManagerRef = "springTransactionManager")
@EntityScan({"com.gl.eirs.ussd.entity.app"})
public class AppDbConfig {
    @Bean
    public CommandLineRunner springDbConnectionCheck(DbConnectionChecker dbConnectionChecker) {
        return args -> dbConnectionChecker.checkSpringDbConnection(appDataSource());
    }

    @Primary
    @Bean(name = "springEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean appEntityManagerFactory(
            @Qualifier("springDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(dataSource)
                .packages("com.gl.eirs.ussd.entity.app")
                .persistenceUnit("app") // CHANGE TO CEIR
                .properties(jpaProperties())
                .build();

    }

    @Primary
    @Bean(name = "springTransactionManager")
    public PlatformTransactionManager springTransactionManager(
            @Qualifier("springEntityManagerFactory") LocalContainerEntityManagerFactoryBean appEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(appEntityManagerFactory.getObject()));
    }

    // DataSource Configs
    @Primary
    @Bean(name = "springDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource appDataSource() {
        return DataSourceBuilder.create().build();
    }


    protected Map<String, Object> jpaProperties() {
        Map<String, Object> props = new HashMap<>();
//        props.put("hibernate.physical_naming_strategy", SpringPhysicalNamingStrategy.class.getName());
        props.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());
        return props;
    }

    public  Connection getConnection() {
        DataSource dataSource = appDataSource();
        try (Connection connection = dataSource.getConnection()) {
//            logger.info("Database connection for " + dbName + " is successful!");
            return connection;
        } catch (SQLException e) {
//            logger.error("alert1601: DB connection failed for " + dbName + " while getting DB configuration value");
            e.printStackTrace();
            return null;
        }
    }

}
