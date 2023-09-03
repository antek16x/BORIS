package org.boris.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "postgresEntityManager",
        transactionManagerRef = "postgresTransactionManager",
        basePackages = {
                "org.axonframework.eventhandling.tokenstore",
                "org.boris.repository"
        }
)
@Profile("prod")
@EntityScan("org.boris.entity")
@SuppressWarnings("unused")
public class PostgresConfiguration {

    @Primary
    @Bean(name = "postgresHikariConfig")
    @ConfigurationProperties(prefix = "spring.datasource.postgres")
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    @Primary
    @Bean(name = "postgresDataSource")
    public DataSource postgresDataSource(@Qualifier("postgresHikariConfig") HikariConfig hikariConfig) {
        return new HikariDataSource(hikariConfig);
    }

    @Primary
    @Bean(name = "postgresEntityManager")
    public LocalContainerEntityManagerFactoryBean postgresEntityManager(
            @Qualifier("postgresDataSource") DataSource postgresDataSource,
            @Value("${spring.jpa.postgres.database-platform}") String dialect,
            @Value("${spring.jpa.postgres.show-sql}") String showSQL,
            @Value("${spring.jpa.postgres.format-sql}") String formatSQL,
            @Value("${spring.jpa.postgres.generate-ddl}") String generateDDL,
            @Value("${spring.jpa.postgres.hibernate.hbm2ddl.auto}") String hbm2DDLAuto
    ) {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        JpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactoryBean.setDataSource(postgresDataSource);
        entityManagerFactoryBean.setPackagesToScan(
                "org.axonframework.eventhandling.tokenstore",
                "org.boris.entity"
        );
        entityManagerFactoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        entityManagerFactoryBean.setJpaProperties(JpaProperties.jpaProperties(formatSQL, showSQL, hbm2DDLAuto, dialect, generateDDL));

        return entityManagerFactoryBean;
    }

    @Primary
    @Bean(name = "postgresTransactionManager")
    public PlatformTransactionManager postgresTransactionManager(@Qualifier("postgresEntityManager") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
