package org.boris;

import org.boris.config.JpaProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@TestConfiguration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "testEntityManager",
        transactionManagerRef = "testTransactionManager",
        basePackages = {
                "org.axonframework.eventhandling.tokenstore",
                "org.boris.repository"
        }
)
@EntityScan("org.boris.entity")
@Profile("test")
@SuppressWarnings("unused")
public class DBTestConfiguration {

    @Primary
    @Bean(name = "testDataSource")
    public DataSource testDataSource(
            @Value("${spring.datasource.driverClassName}") String className,
            @Value("${spring.datasource.jdbc-url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password
    ) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName(className);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setUrl(url);

        return dataSource;
    }

    @Primary
    @Bean(name = "testEntityManager")
    public LocalContainerEntityManagerFactoryBean testEntityManager(
            @Qualifier("testDataSource") DataSource testDataSource,
            @Value("${spring.jpa.database-platform}") String dialect,
            @Value("${spring.jpa.show-sql}") String showSQL,
            @Value("${spring.jpa.format-sql}") String formatSQL,
            @Value("${spring.jpa.generate-ddl}") String generateDDL,
            @Value("${spring.jpa.hibernate.hbm2ddl.auto}") String hbm2DDLAuto
    ) {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        JpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactoryBean.setDataSource(testDataSource);
        entityManagerFactoryBean.setPackagesToScan(
                "org.axonframework.eventhandling.tokenstore",
                "org.boris.entity"
        );
        entityManagerFactoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        entityManagerFactoryBean.setJpaProperties(JpaProperties.jpaProperties(formatSQL, showSQL, hbm2DDLAuto, dialect, generateDDL));

        return entityManagerFactoryBean;
    }

    @Primary
    @Bean(name = "testTransactionManager")
    public PlatformTransactionManager testTransactionManager(@Qualifier("testEntityManager") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
