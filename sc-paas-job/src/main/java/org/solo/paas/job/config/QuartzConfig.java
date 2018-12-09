/**
 * 
 */
package org.solo.paas.job.config;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.quartz.Scheduler;
import org.solo.paas.job.custom.CustomJobFactory;
import org.solo.paas.job.scheduler.TriggerScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * @author song.peng
 *
 */
@Configuration
public class QuartzConfig {

	@Autowired
	DataSource dataSource;
	
	@Bean
	public CustomJobFactory jobFactory() {
		CustomJobFactory customJobFactory = new CustomJobFactory();
		return customJobFactory;
	}
	
	@Bean
	@ConfigurationProperties(prefix = "org.quartz")
    public Properties quartzProperties() throws IOException {
		Properties properties = new Properties();
        return properties;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setOverwriteExistingJobs(true);
        schedulerFactoryBean.setDataSource(dataSource);
        schedulerFactoryBean.setQuartzProperties(quartzProperties());
        schedulerFactoryBean.setJobFactory(jobFactory());
        return schedulerFactoryBean;
    }

    @Bean
    public Scheduler scheduler() throws IOException {
        return schedulerFactoryBean().getScheduler();
    }
    
    @Bean
    public TriggerScheduler triggerScheduler() {
    	return new TriggerScheduler();
    }
}
