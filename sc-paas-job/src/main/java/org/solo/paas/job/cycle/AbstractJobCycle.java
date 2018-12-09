/**
 * 
 */
package org.solo.paas.job.cycle;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.quartz.Job;
import org.solo.paas.job.base.Constant;
import org.solo.paas.job.domain.CycleRecord;
import org.solo.paas.job.domain.CycleTrigger;
import org.solo.paas.job.service.CycleService;
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
public abstract class AbstractJobCycle implements Job {

	@Autowired
	CycleService cycleService;
	

	/**调用微服务同步数据执行任务
	 * @param rec
	 * @param paramsMap
	 * @param callFun
	 * @return
	 */
	public CycleRecord callService(@NonNull CycleRecord record,Map<String,Object> paramsMap,Function<Map<String,Object>,ResponseResult<List<Map<String,Object>>>> callFun) {	
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
	
	/**通用execute方法(轮询)
	 * @param groupName
	 * @param callFun
	 */
	protected void executeCycle(String groupName,Integer pageSize,Function<Map<String,Object>,ResponseResult<List<Map<String,Object>>>> callFun) {
		CycleRecord record = findLegacyOrNewByGroupName(groupName);
		if(ObjectUtils.isEmpty(record)) {
			return;
		}
		int i = 1;
		while(!ObjectUtils.isEmpty(record)) {
			log.info("Cycle {} of {} Start!",getClass().getSimpleName(),i);
			record.setStartTime(new Date());
			Map<String,Object> paramsMap = buildParams(record.getCurrentStartTime(),record.getCurrentEndTime(),pageSize);
			record = callService(record,paramsMap,callFun);
			if(record.getRowCount().intValue()>0 || record.getStatus()==0) {
				cycleService.saveRecordOne(record);
			}
			log.info("Cycle {} of {} End! Rowcount is {}",getClass().getSimpleName(),Integer.valueOf(i),record.getRowCount());
			i++;
			record = findLegacyOrNewByGroupName(groupName);
		}		
	}

	private CycleRecord findLegacyOrNewByGroupName(String groupName) {
		CycleTrigger cycleTrigger = cycleService.findTriggerByGroupAndClassName(groupName,getClass().getName());
		Assert.notNull(cycleTrigger,"无此轮询配置记录");
		CycleRecord record = cycleService.findRecordFirstLegacy(cycleTrigger);
		if(!ObjectUtils.isEmpty(record)) {
			if(record.getStatus()==-1) {
				log.error("Cycle {} fault over threshold!",getClass().getSimpleName());
				cycleTrigger.setStatus(0);
				cycleTrigger.setRemark(DateUtils.getDate()+"重试失败次数超过限制关闭");
				cycleService.saveTriggerOne(cycleTrigger);
				return null;
			}
		}else {
			record = new CycleRecord();
			record.setCycleTrigger(cycleTrigger);
			cycleTrigger = cycleService.generateTriggerCurrentDuratuon(cycleTrigger);			
			if(ObjectUtils.isEmpty(cycleTrigger)) {
				log.info("Cycle {} all finished!",getClass().getSimpleName());
				return null;
			}
			cycleTrigger = cycleService.saveTriggerOne(cycleTrigger);
		}
		record.setCurrentStartTime(cycleTrigger.getCurrentStartTime());
		record.setCurrentEndTime(cycleTrigger.getCurrentEndTime());
		return record;
	}
}
