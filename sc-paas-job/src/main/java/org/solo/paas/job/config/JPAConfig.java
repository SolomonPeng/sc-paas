/**
 * 
 */
package org.solo.paas.job.config;

import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.querydsl.jpa.HQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;

/**
 * @author song.peng
 *
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass=true)
@EnableJpaRepositories(basePackages= {"com.accenture.microservice.data.repository","org.solo.paas.job.repository"})
@ComponentScan(basePackages="com.accenture.microservice.data.component",lazyInit=true)
public class JPAConfig {
	
	@Autowired
	DataSource dataSource;
	
	@Bean
	public DefaultPersistenceUnitManager persistenceUnitManager() throws SQLException{
		DefaultPersistenceUnitManager persistenceUnitManager = new DefaultPersistenceUnitManager();
        persistenceUnitManager.setDefaultPersistenceUnitName("SC-PAAS-JOB");
        persistenceUnitManager.setDefaultDataSource(dataSource);
        persistenceUnitManager.setPackagesToScan("com.accenture.microservice.data.domain","org.solo.paas.job.domain");
        return persistenceUnitManager;
	}	
    
	@Bean
	public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
	    return new JPAQueryFactory(new HQLTemplates(), entityManager);
	}
}
