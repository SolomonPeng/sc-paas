/**
 * 
 */
package org.solo.paas.job.api;

import org.solo.paas.job.scheduler.TriggerScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.accenture.microservice.app.annotation.OpenApi;
import com.accenture.microservice.app.base.BaseApi;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author song.peng
 *
 */
@OpenApi
@RequestMapping("/scheduler")
@Api(tags="scheduler",description = "定时计划")
public class SchedulerApi extends BaseApi {

	@Autowired
	TriggerScheduler triggerScheduler;
	
	@ApiOperation("即时刷新Trigger Job定时配置")
    @PostMapping("/refresh/trigger")
	public void refreshTrigger() {
		triggerScheduler.refreshTrigger();
	}
}
