/**
 * 
 */
package org.solo.paas.job.task;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.quartz.Job;
import org.solo.paas.job.base.Constant;
import org.solo.paas.job.domain.TaskRecord;
import org.solo.paas.job.domain.TaskTrigger;
import org.solo.paas.job.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.accenture.microservice.core.util.CoreUtils;
import com.accenture.microservice.core.util.DateUtils;
import com.accenture.microservice.core.vo.ResponseResult;
import com.google.common.collect.Maps;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * @author song.peng
 *
 */
@Slf4j
public abstract class AbstractJobTask implements Job {

	@Autowired
	TaskService taskService;

	/*
	*//** 根据定时表达式获取两次执行之间间隔(秒)
	 * @param cron
	 * @return
	 *//*
	public long getCronDuration(String cron) {
		CronSequenceGenerator csg = new CronSequenceGenerator(cron);
		Date today = new Date();
		long dur = DateUtils.dateDiff(csg.next(today),today);
		return dur;
	}*/

	
	/**调用微服务同步数据执行任务
	 * @param rec
	 * @param paramsMap
	 * @param callFun
	 * @return
	 */
	public TaskRecord callService(@NonNull TaskRecord record,Map<String,Object> paramsMap,Function<Map<String,Object>,ResponseResult<List<Map<String,Object>>>> callFun) {	
		record.setParams(CoreUtils.toString(paramsMap));
		if(!record.isNew()) {
			record.setRetry(record.getRetry()+1);
		}
		try {
			ResponseResult<List<Map<String,Object>>> res = callFun.apply(paramsMap);			
			if(res.isSuccess()) {
				List<Map<String,Object>> result = res.getResponse();
				record.setStatus(1);
				record.setRowCount(CollectionUtils.isEmpty(result)?Integer.valueOf(0):Integer.valueOf(result.size()));
				record.setMessage(String.format(Constant.MSG_SUCCESS_PATTERN, record.getRowCount()));
			}else {
				record.setStatus(0);
				if(record.getRetry()>=Constant.RETRY_THRESHOLD) {
					record.setStatus(-1);
				}
				record.setMessage(String.format(Constant.MSG_FAULT_PATTERN, res.getMessage()));
			}
		}catch(Exception ex) {
			record.setStatus(0);
			String errmsg = ex.getMessage();
			if(errmsg.length()>1000) {
				errmsg = errmsg.substring(0, 1000);
			}
			record.setMessage(errmsg);
		}		
		record.setEndTime(new Date());
		return record;
	}
	
	/**生成基本参数对象
	 * @param lastupdatestart
	 * @param lastupdateend
	 * @param pagesize
	 * @return
	 */
	protected Map<String,Object> buildParams(Date lastupdatestart,Date lastupdateend, Integer pagesize){
		Map<String,Object> params = Maps.newHashMap();
		params.put("lastupdatestart", lastupdatestart);
		params.put("lastupdateend", lastupdateend);
		params.put("pagesize", pagesize);
		return params;
	}
	
		
	/**通用execute方法(分页)
	 * @param groupName
	 * @param callFun
	 */
	protected void executeTask(String groupName,Integer pageSize,Function<Map<String,Object>,ResponseResult<List<Map<String,Object>>>> callFun) {
		log.info("Task {} Start!",getClass().getSimpleName());
		TaskRecord record = findLegacyOrNewByGroupName(groupName);
		if(ObjectUtils.isEmpty(record)) {
			return;
		}
		record.setStartTime(new Date());
		Map<String,Object> paramsMap = buildParams(record.getLastStartTime(),record.getStartTime(),pageSize);
		record = callService(record,paramsMap,callFun);
		taskService.saveRecordOne(record);
		log.info("Task {} End!",getClass().getSimpleName());
	}
	
	
	private TaskRecord findLegacyOrNewByGroupName(String groupName) {
		TaskTrigger trigger = taskService.findTriggerByGroupAndClassName(groupName,getClass().getName());
		Assert.notNull(trigger,"无此任务配置记录");
		TaskRecord record = taskService.findRecordFirstLegacy(trigger);
		Date lastStartTime = taskService.findRecordLastSuccessStartTime(trigger);
		if(!ObjectUtils.isEmpty(record)) {
			if(record.getStatus()==-1) {
				log.error("Task {} fault over threshold!",getClass().getSimpleName());
				trigger.setStatus(0);
				trigger.setRemark(DateUtils.getDate()+"重试失败次数超过限制关闭");
				taskService.saveTriggerOne(trigger);
				return null;
			}
		}else {
			record = new TaskRecord();
			record.setTaskTrigger(trigger);
		}
		record.setLastStartTime(lastStartTime);
		return record;
	}
	
}
