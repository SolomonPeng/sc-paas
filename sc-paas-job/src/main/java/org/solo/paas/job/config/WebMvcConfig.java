/**
 * 
 */
package org.solo.paas.job.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.accenture.microservice.app.annotation.support.ApiRequestMappingHandlerAdapter;

/**
 * @author song.peng
 *
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

	/**添加OpenApi的处理
	 * @return
	 */
	@Bean
	ApiRequestMappingHandlerAdapter apiRequestMappingHandlerAdapter() {
		return new ApiRequestMappingHandlerAdapter();
	}
		
}
