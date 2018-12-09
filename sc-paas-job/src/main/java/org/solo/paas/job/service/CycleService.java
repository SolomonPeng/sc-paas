/**
 * 
 */
package org.solo.paas.job.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.solo.paas.job.base.Constant;
import org.solo.paas.job.domain.CycleRecord;
import org.solo.paas.job.domain.CycleTrigger;
import org.solo.paas.job.domain.QCycleRecord;
import org.solo.paas.job.domain.QCycleTrigger;
import org.solo.paas.job.repository.CycleRecordRepository;
import org.solo.paas.job.repository.CycleTriggerRepository;
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
import com.accenture.microservice.core.util.DateUtils;
import com.accenture.microservice.core.util.NumberUtils;
import com.accenture.microservice.core.vo.ResponseResult;
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
@CacheConfig(cacheNames = "paas-job-cycle")
public class CycleService extends AbstractJobService {

	@Autowired
	CycleTriggerRepository triggerRepository;
	@Autowired
	CycleRecordRepository recordRepository;
	
	public CycleTrigger transTriggerObject(@NonNull Map<String,Object> map) {
		return transObject(map,triggerRepository);
	}
	
	private Predicate transTriggerPredicates(@NonNull Map<String,Object> map) {
		QCycleTrigger q = QCycleTrigger.cycleTrigger;
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
	public CycleTrigger findTriggerById(String id) {
		return triggerRepository.findOne(QCycleTrigger.cycleTrigger.id.eq(id));
	}
	
	public List<CycleTrigger> findTriggerByIds(List<String> ids){
		return Lists.newArrayList(triggerRepository.findAll(QCycleTrigger.cycleTrigger.id.in(ids)));
	}
	
	public List<CycleTrigger> findTriggerByParams(@NonNull Map<String,Object> paramsMap){
		Predicate predicate = transTriggerPredicates(paramsMap);
		Sort sort = transSort(paramsMap);
		return Lists.newArrayList(triggerRepository.findAll(predicate,sort));
	}
	
	public List<CycleTrigger> findTriggerAll(){
		return triggerRepository.findAll();
	}
	
	public Page<CycleTrigger> findTriggerByParamsPage(@NonNull Map<String,Object> paramsMap,final int page, final int size){
		Sort sort = transSort(paramsMap);
		PageRequest pageable = new PageRequest(page, size, sort);
		return triggerRepository.findAll(transTriggerPredicates(paramsMap), pageable);
	}
	
	@Transactional
	@CachePut(key="T(String).valueOf('trigger').concat('-').concat(#trigger.id)", condition="!#trigger.isNew()")
	public CycleTrigger saveTriggerOne(CycleTrigger trigger) {
		return triggerRepository.save(trigger);
	}
	
	@Transactional
	public List<CycleTrigger> saveTriggerList(@NonNull List<CycleTrigger> triggers){
		List<CycleTrigger> list = Lists.newArrayList();
		for(CycleTrigger trigger : triggers) {
			ResponseResult<CycleTrigger> validate = DataUtils.validateEntity(trigger);
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
		List<CycleTrigger> list = findTriggerByIds(ids);
		if(!CollectionUtils.isEmpty(list)) {
			triggerRepository.delete(list);
		}		
	}
	
	
	public CycleRecord transRecordObject(@NonNull Map<String,Object> map) {
		return transObject(map,recordRepository);
	}
	
	@Cacheable(key="T(String).valueOf('record').concat('-').concat(#id)")
	public CycleRecord findRecordById(@NonNull String id) {
		return recordRepository.findOne(id);
	}
	
	public List<CycleRecord> findRecordByIds(List<String> ids){
		return Lists.newArrayList(recordRepository.findAll(QCycleRecord.cycleRecord.id.in(ids)));
	}
	
	private Predicate transRecordPredicates(@NonNull Map<String,Object> map) {
		QCycleRecord q = QCycleRecord.cycleRecord;
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
			predicates.add(q.cycleTrigger.id.eq(map.get("triggerId").toString()));
		}
		if(!ObjectUtils.isEmpty(map.get("triggerGroup"))) {
			predicates.add(q.cycleTrigger.group.eq(map.get("triggerGroup").toString()));
		}
		if(!ObjectUtils.isEmpty(map.get("triggerClassName"))) {
			predicates.add(q.cycleTrigger.className.eq(map.get("triggerClassName").toString()));
		}
    	return ExpressionUtils.allOf(predicates);
	}	
	
	public List<CycleRecord> findRecordByParams(Map<String,Object> paramsMap){
		Predicate predicate = transRecordPredicates(paramsMap);
		Sort sort = transSort(paramsMap);
		return Lists.newArrayList(recordRepository.findAll(predicate,sort));
	}
	
	public Page<CycleRecord> findRecordByParamsPage(@NonNull Map<String,Object> paramsMap,final int page, final int size){
		Sort sort = transSort(paramsMap);
		PageRequest pageable = new PageRequest(page, size, sort);
		return recordRepository.findAll(transRecordPredicates(paramsMap), pageable);
	}
	
	
	@Transactional
	@CachePut(key="T(String).valueOf('record').concat('-').concat(#record.id)", condition="!#record.isNew()")
	public CycleRecord saveRecordOne(@NonNull CycleRecord record) {
		return recordRepository.save(record);		
	}
	
	@Transactional
	public List<CycleRecord> saveRecordList(@NonNull List<CycleRecord> records){
		List<CycleRecord> list = Lists.newArrayList();
		for(CycleRecord record : records) {
			ResponseResult<CycleRecord> validate = DataUtils.validateEntity(record);
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
		List<CycleRecord> list = findRecordByIds(ids);
		if(!CollectionUtils.isEmpty(list)) {
			recordRepository.delete(list);
		}		
	}
	
	
	
	/**根据group和className查找一个CycleTrigger
	 * @param group
	 * @param className
	 * @return
	 */
	public CycleTrigger findTriggerByGroupAndClassName(@NonNull String group,@NonNull String className) {
		QCycleTrigger q = QCycleTrigger.cycleTrigger;
		List<Predicate> predicates = Lists.newArrayList();
		predicates.add(q.group.eq(group));
		predicates.add(q.className.eq(className));		
		return jpaQueryFactory.selectFrom(q).where(ExpressionUtils.allOf(predicates)).fetchFirst();		
	}
		
	/**根据cycleTrigger配置好的开始结束时间段和间隔,以及上次执行的轮询时间段,生成当前轮询时间段
	 * @param cycleTrigger
	 * @return
	 */
	public CycleTrigger generateTriggerCurrentDuratuon(@NonNull CycleTrigger cycleTrigger) {
		Date currentStartTime = cycleTrigger.getCurrentEndTime();
		if(ObjectUtils.isEmpty(currentStartTime)) {
			currentStartTime = cycleTrigger.getStartTime();
		}
		Assert.notNull(currentStartTime,"currentStartTime is null");
		Date currentEndTime = DateUtils.addDay(currentStartTime, cycleTrigger.getDuration());
		if(currentEndTime.after(cycleTrigger.getEndTime())) {
			return null;
		}
		cycleTrigger.setCurrentStartTime(currentStartTime);
		cycleTrigger.setCurrentEndTime(currentEndTime);
		return cycleTrigger;
	}
	
	/**根据trigger查找上一次运行失败的记录包括status<1的所有记录
	 * @param cycleTrigger
	 * @return
	 */
	public CycleRecord findRecordFirstLegacy(@NonNull CycleTrigger cycleTrigger){
		QCycleRecord q = QCycleRecord.cycleRecord;
		List<Predicate> predicates = Lists.newArrayList();		
		predicates.add(q.cycleTrigger.eq(cycleTrigger));
		predicates.add(q.status.lt(Integer.valueOf(1)));
		//recordRepository.findOne(ExpressionUtils.allOf(predicates));
		return jpaQueryFactory.selectFrom(q).where(ExpressionUtils.allOf(predicates)).fetchFirst();
	}
	
	/**根据trigger查找上一次成功的记录(where status=1 order by startTime desc)的startTime
	 * @param cycleTrigger
	 * @return
	 */
	public Date findRecordLastSuccessStartTime(@NonNull CycleTrigger cycleTrigger) {
		QCycleRecord q = QCycleRecord.cycleRecord;
		List<Predicate> predicates = Lists.newArrayList();		
		predicates.add(q.cycleTrigger.eq(cycleTrigger));
		predicates.add(q.status.eq(Integer.valueOf(1)));
		CycleRecord rec = jpaQueryFactory.selectFrom(q).where(ExpressionUtils.allOf(predicates)).orderBy(q.startTime.desc()).fetchFirst();
		if(ObjectUtils.isEmpty(rec)) {
			return null;
		}
		return rec.getStartTime();
	}
}
