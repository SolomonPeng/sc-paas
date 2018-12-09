/**
 * 
 */
package org.solo.paas.job.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.accenture.microservice.app.annotation.OpenApi;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author song.peng
 *
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

	@Value("${swagger.enabled:true}")
	private boolean enableSwagger;
	
	@Bean
    public Docket createRestApi() {
		ParameterBuilder authPar = new ParameterBuilder();  
        List<Parameter> pars = new ArrayList<Parameter>();    
        authPar.name("Authorization").description("access token")
        .modelRef(new ModelRef("string")).parameterType("header")   
        .required(false).build(); 
        pars.add(authPar.build());  
		
		return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .enable(enableSwagger)
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(OpenApi.class))
                .paths(PathSelectors.any())                
                .build().globalOperationParameters(pars);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("ICP-ISP-ERP")
                .description("广东移动投资项目一体化ICP接口服务平台定时任务调度")
                .termsOfServiceUrl("http://www.gmcc.com/")
                .version("1.0")
                .build();
    }

}
