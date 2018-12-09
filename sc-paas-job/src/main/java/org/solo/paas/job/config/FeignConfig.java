/**
 * 
 */
package org.solo.paas.job.config;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * @author song.peng
 *
 */
@Configuration
public class FeignConfig {

	/*@Bean
    @ConfigurationProperties(prefix = "security.oauth2.client")
    public ClientCredentialsResourceDetails clientCredentialsResourceDetails() {
        return new ClientCredentialsResourceDetails();
    }

    @Bean
    public RequestInterceptor oauth2FeignRequestInterceptor(){
        return new OAuth2FeignRequestInterceptor(new DefaultOAuth2ClientContext(), clientCredentialsResourceDetails());
    }

    @Bean
    public OAuth2RestTemplate clientCredentialsRestTemplate() {
        return new OAuth2RestTemplate(clientCredentialsResourceDetails());
    }*/
	
	@Bean
    public RequestInterceptor headerInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if(!ObjectUtils.isEmpty(attributes)) {
                	HttpServletRequest request = attributes.getRequest();
                    if(!ObjectUtils.isEmpty(request)) {
                    	Enumeration<String> headerNames = request.getHeaderNames();
                        if (headerNames != null) {
                            while (headerNames.hasMoreElements()) {
                                String name = headerNames.nextElement();
                                String values = request.getHeader(name);
                                requestTemplate.header(name, values);
                            }
                        }
                    }  
                }
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();        		
        		if(!ObjectUtils.isEmpty(authentication)) {
        			OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
                    requestTemplate.header("Authorization", "Bearer " + details.getTokenValue());
        		}
            }
        };
    }
}
