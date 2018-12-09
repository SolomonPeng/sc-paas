/**
 * 
 */
package org.solo.paas.job.feign;

import java.util.List;
import java.util.Map;

import org.solo.paas.job.config.FeignConfig;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.accenture.microservice.core.vo.ResponseResult;

/**
 * @author song.peng
 *
 */
@FeignClient(name = "paas-cms",configuration = {FeignConfig.class})
@RequestMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public interface CmsFeign {

	@PostMapping("${api.cms.contract-base.sync}")
	public ResponseResult<List<Map<String,Object>>> contractBaseSync(@RequestBody Map<String,Object> paramMap);
}
