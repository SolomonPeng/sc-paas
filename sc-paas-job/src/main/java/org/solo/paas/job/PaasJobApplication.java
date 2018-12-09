/**
 * 
 */
package org.solo.paas.job;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;


/**
 * @author song.peng
 *
 */
@EnableFeignClients
@SpringCloudApplication
public class PaasJobApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaasJobApplication.class, args);
	}

	/*@Bean
    Request.Options feignOptions() {
        return new Request.Options(*//**connectTimeoutMillis**//*10 * 1000, *//** readTimeoutMillis **//*3600 * 3 * 1000);
    }*/

}
