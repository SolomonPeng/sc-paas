/**
 * 
 */
package org.solo.paas.job.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.solo.paas.job.base.Constant;
import org.solo.paas.job.domain.QTaskRecord;
import org.solo.paas.job.domain.QTaskTrigger;
import org.solo.paas.job.domain.TaskRecord;
import org.solo.paas.job.domain.TaskTrigger;
import org.solo.paas.job.repository.TaskRecordRepository;
import org.solo.paas.job.repository.TaskTriggerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.accenture.microservice.core.util.CoreUtils;
import com.accenture.microservice.core.util.NumberUtils;
import com.accenture.microservice.core.vo.ResponseResult;
import com.accenture.microservice.data.base.AbstractService;
import com.accenture.microservice.data.util.DataUtils;
import com.google.common.collect.Lists;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;

import lombok.NonNull;

/**
 * @author song.peng
 *
 */
@Service
@CacheConfig(cacheNames = "paas-job-task")
public class TaskService extends AbstractService {

	@Autowired
	TaskTriggerRepository triggerRepository;
	@Autowired
	TaskRecordRepository recordRepository;
	
	public TaskTrigger transTriggerObject(@NonNull Map<String,Object> map) {
		return transObject(map,triggerRepository);
	}
	
	private Predicate transTriggerPredicates(@NonNull Map<String,Object> map) {
		QTaskTrigger q = QTaskTrigger.taskTrigger;
    	List<Predicate> predicates = Lists.newArrayList();
    	if(!ObjectUtils.isEmpty(map.get("group"))) {
    		predicates.add(q.group.eq(map.get("group").toString()));
    	}
    	if(!ObjectUtils.isEmpty(map.get("status"))) {
    		predicates.add(q.status.eq(NumberUtils.fromString(map.get("status").toString(), Integer.class)));
    	}
    	if(!ObjectUtils.isEmpty(map.get("className"))) {
    		predicates.add(q.className.eq(map.get("className").toString()));
    	}
    	if(!ObjectUtils.isEmpty(map.get("name"))) {
    		predicates.add(q.name.like(map.get("name").toString()));
    	}    	
    	if(!ObjectUtils.isEmpty(map.get("remark"))) {
    		predicates.add(q.remark.like(map.get("remark").toString()));
    	}
    	return ExpressionUtils.allOf(predicates);
	}
	
	@Cacheable(key="T(String).valueOf('trigger').concat('-').concat(#id)")
	public TaskTrigger findTriggerById(String id) {
		return triggerRepository.findOne(QTaskTrigger.taskTrigger.id.eq(id));
	}
	
	public List<TaskTrigger> findTriggerByIds(List<String> ids){
		return Lists.newArrayList(triggerRepository.findAll(QTaskTrigger.taskTrigger.id.in(ids)));
	}
	
	public List<TaskTrigger> findTriggerByParams(@NonNull Map<String,Object> paramsMap){
		Predicate predicate = transTriggerPredicates(paramsMap);
		Sort sort = transSort(paramsMap);
		return Lists.newArrayList(triggerRepository.findAll(predicate,sort));
	}
	
	public List<TaskTrigger> findTriggerAll(){
		return triggerRepository.findAll();
	}
	
	public Page<TaskTrigger> findTriggerByParamsPage(@NonNull Map<String,Object> paramsMap,final int page, final int size){
		Sort sort = transSort(paramsMap);
		PageRequest pageable = new PageRequest(page, size, sort);
		return triggerRepository.findAll(transTriggerPredicates(paramsMap), pageable);
	}
	
	@Transactional
	@CachePut(key="T(String).valueOf('trigger').concat('-').concat(#trigger.id)", condition="!#trigger.isNew()")
	public TaskTrigger saveTriggerOne(TaskTrigger trigger) {
		return triggerRepository.save(trigger);
	}
	
	@Transactional
	public List<TaskTrigger> saveTriggerList(@NonNull List<TaskTrigger> triggers){
		List<TaskTrigger> list = Lists.newArrayList();
		for(TaskTrigger trigger : triggers) {
			ResponseResult<TaskTrigger> validate = DataUtils.validateEntity(trigger);
			Assert.isTrue(validate.isSuccess(),validate.getMessage());
			list.add(trigger);
		}
		list = triggerRepository.save(list);
		return list;
	}
	
	@Transactional
	@PreAuthorize("hasAuthority('sys_admin')")
	@CacheEvict(key="T(String).valueOf('trigger').concat('-').concat(#id)")
	public void deleteTriggerById(@NonNull String id) {
		triggerRepository.delete(id);
	}
	
	@Transactional
	@PreAuthorize("hasAuthority('sys_admin')")
	public void deleteTriggerList(@NonNull List<String> ids) {
		List<TaskTrigger> list = findTriggerByIds(ids);
		if(!CollectionUtils.isEmpty(list)) {
			triggerRepository.delete(list);
		}		
	}
	
	public TaskRecord transRecordObject(@NonNull Map<String,Object> map) {
		return transObject(map,recordRepository);
	}
	
	@Cacheable(key="T(String).valueOf('record').concat('-').concat(#id)")
	public TaskRecord findRecordById(@NonNull String id) {
		return recordRepository.findOne(id);
	}
	
	public List<TaskRecord> findRecordByIds(List<String> ids){
		return Lists.newArrayList(recordRepository.findAll(QTaskRecord.taskRecord.id.in(ids)));
	}
	
	private Predicate transRecordPredicates(@NonNull Map<String,Object> map) {
		QTaskRecord q = QTaskRecord.taskRecord;
		List<Predicate> predicates = Lists.newArrayList();
		if(!ObjectUtils.isEmpty(map.get("status"))) {
    		predicates.add(q.status.eq(NumberUtils.fromString(CoreUtils.optionalMapString(map, "status"), Integer.class)));
    	}
		if(!ObjectUtils.isEmpty(map.get("retry"))) {
    		predicates.add(q.retry.loe(NumberUtils.fromString(CoreUtils.optionalMapString(map, "retry"), Integer.class)));
    	}
		if(!ObjectUtils.isEmpty(map.get(Constant.MK_IS_COMPLETE))) {
    		predicates.add(q.endTime.isNotNull());
    	}
		if(!ObjectUtils.isEmpty(map.get("triggerId"))) {
			predicates.add(q.taskTrigger.id.eq(map.get("triggerId").toString()));
		}
		if(!ObjectUtils.isEmpty(map.get("triggerGroup"))) {
			predicates.add(q.taskTrigger.group.eq(map.get("triggerGroup").toString()));
		}
		if(!ObjectUtils.isEmpty(map.get("triggerClassName"))) {
			predicates.add(q.taskTrigger.className.eq(map.get("triggerClassName").toString()));
		}
    	return ExpressionUtils.allOf(predicates);
	}	
	
	public List<TaskRecord> findRecordByParams(Map<String,Object> paramsMap){
		Predicate predicate = transRecordPredicates(paramsMap);
		Sort sort = transSort(paramsMap);
		return Lists.newArrayList(recordRepository.findAll(predicate,sort));
	}
	
	public Page<TaskRecord> findRecordByParamsPage(@NonNull Map<String,Object> paramsMap,final int page, final int size){
		Sort sort = transSort(paramsMap);
		PageRequest pageable = new PageRequest(page, size, sort);
		return recordRepository.findAll(transRecordPredicates(paramsMap), pageable);
	}
	
	
	@Transactional
	@CachePut(key="T(String).valueOf('record').concat('-').concat(#record.id)", condition="!#record.isNew()")
	public TaskRecord saveRecordOne(@NonNull TaskRecord record) {
		return recordRepository.save(record);		
	}
	
	@Transactional
	public List<TaskRecord> saveRecordList(@NonNull List<TaskRecord> records){
		List<TaskRecord> list = Lists.newArrayList();
		for(TaskRecord record : records) {
			ResponseResult<TaskRecord> validate = DataUtils.validateEntity(record);
			Assert.isTrue(validate.isSuccess(),validate.getMessage());
			list.add(record);
		}
		list = recordRepository.save(list);
		return list;
	}
	
	@Transactional
	@PreAuthorize("hasAuthority('sys_admin')")
	@CacheEvict(key="T(String).valueOf('record').concat('-').concat(#id)")
	public void deleteRecordById(@NonNull String id) {
		recordRepository.delete(id);
	}
	
	@Transactional
	@PreAuthorize("hasAuthority('sys_admin')")
	public void deleteRecordList(@NonNull List<String> ids) {
		List<TaskRecord> list = findRecordByIds(ids);
		if(!CollectionUtils.isEmpty(list)) {
			recordRepository.delete(list);
		}		
	}
	
	/**根据group和className查找一个TaskTrigger
	 * @param group
	 * @param className
	 * @return
	 */
	public TaskTrigger findTriggerByGroupAndClassName(@NonNull String group,@NonNull String className) {
		QTaskTrigger q = QTaskTrigger.taskTrigger;
		List<Predicate> predicates = Lists.newArrayList();
		predicates.add(q.group.eq(group));
		predicates.add(q.className.eq(className));		
		return this.getJpaQueryFactory().selectFrom(q).where(ExpressionUtils.allOf(predicates)).fetchFirst();		
	}

	/**根据trigger查找上一次运行失败的记录包括status<1的所有记录
	 * @param trigger
	 * @return
	 */
	public TaskRecord findRecordFirstLegacy(@NonNull TaskTrigger trigger){
		QTaskRecord q = QTaskRecord.taskRecord;
		List<Predicate> predicates = Lists.newArrayList();		
		predicates.add(q.taskTrigger.eq(trigger));
		predicates.add(q.status.lt(Integer.valueOf(1)));
		//recordRepository.findOne(ExpressionUtils.allOf(predicates));
		return this.getJpaQueryFactory().selectFrom(q).where(ExpressionUtils.allOf(predicates)).fetchFirst();
	}
	
	/**根据trigger查找上一次成功的记录(where status=1 order by startTime desc)的startTime
	 * @param trigger
	 * @return
	 */
	public Date findRecordLastSuccessStartTime(@NonNull TaskTrigger trigger) {
		QTaskRecord q = QTaskRecord.taskRecord;
		List<Predicate> predicates = Lists.newArrayList();		
		predicates.add(q.taskTrigger.eq(trigger));
		predicates.add(q.status.eq(Integer.valueOf(1)));
		TaskRecord rec = this.getJpaQueryFactory().selectFrom(q).where(ExpressionUtils.allOf(predicates)).orderBy(q.startTime.desc()).fetchFirst();
		if(ObjectUtils.isEmpty(rec)) {
			return null;
		}
		return rec.getStartTime();
	}
}
