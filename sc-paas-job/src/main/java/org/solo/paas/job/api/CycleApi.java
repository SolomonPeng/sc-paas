/**
 * 
 */
package org.solo.paas.job.api;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.solo.paas.job.base.Constant;
import org.solo.paas.job.domain.CycleRecord;
import org.solo.paas.job.domain.CycleTrigger;
import org.solo.paas.job.service.CycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.accenture.microservice.app.annotation.OpenApi;
import com.accenture.microservice.app.base.BaseApi;
import com.google.common.collect.Lists;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @author song.peng
 *
 */
@OpenApi
@RequestMapping("/cycle")
@Api(tags="cycle",description = "轮询处理api")
public class CycleApi extends BaseApi {

	@Autowired
	CycleService cycleService;
	
	@ApiOperation("根据id查询轮询触发器")
    @GetMapping("/trigger/{ids}")
    public Object getTriggerById(@PathVariable String ids) {
		String[] id = StringUtils.commaDelimitedListToStringArray(ids);
		Assert.notEmpty(id, Constant.MSG_EMPTY_COLLECTION);
		if (id.length <= 1) {
			return cycleService.findTriggerById(id[0]);
		}else {
			return cycleService.findTriggerByIds(Arrays.asList(id));
		}
    }
	
	@ApiOperation("根据id查询轮询记录")
    @GetMapping("/record/{ids}")
    public Object getRecordById(@PathVariable String ids) {
		String[] id = StringUtils.commaDelimitedListToStringArray(ids);
		Assert.notEmpty(id, Constant.MSG_EMPTY_COLLECTION);
		if (id.length <= 1) {
			return cycleService.findRecordById(id[0]);
		}else {
			return cycleService.findRecordByIds(Arrays.asList(id));
		}
    }

    @ApiOperation("根据字段参数查询轮询触发器")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "group", paramType = "query"),
            @ApiImplicitParam(name = "status", paramType = "query"),
            @ApiImplicitParam(name = "className", paramType = "query"),
            @ApiImplicitParam(name = "name", paramType = "query"),
            @ApiImplicitParam(name = "remark", paramType = "query"),
            @ApiImplicitParam(name = "ascs", paramType = "query"),
            @ApiImplicitParam(name = "descs", paramType = "query")
    })
    @GetMapping("/trigger/list")
    public List<CycleTrigger> listTrigger(@RequestParam @ApiIgnore Map<String, Object> map) {
    	return cycleService.findTriggerByParams(map);
    }
    
    @ApiOperation("根据字段参数查询轮询记录")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "retry", paramType = "query"),
            @ApiImplicitParam(name = "status", paramType = "query"),
            @ApiImplicitParam(name = Constant.MK_IS_COMPLETE, paramType = "query"),
            @ApiImplicitParam(name = "triggerId", paramType = "query"),
            @ApiImplicitParam(name = "triggerClassName", paramType = "query"),
            @ApiImplicitParam(name = "triggerGroup", paramType = "query"),
            @ApiImplicitParam(name = "ascs", paramType = "query"),
            @ApiImplicitParam(name = "descs", paramType = "query")
    })
    @GetMapping("/record/list")
    public List<CycleRecord> listRecord(@RequestParam @ApiIgnore Map<String, Object> map) {
    	return cycleService.findRecordByParams(map);
    }

    @ApiOperation("根据字段参数查询轮询触发器[分页]")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "page", paramType = "query"),
            @ApiImplicitParam(name = "size", paramType = "query"),
            @ApiImplicitParam(name = "group", paramType = "query"),
            @ApiImplicitParam(name = "status", paramType = "query"),
            @ApiImplicitParam(name = "className", paramType = "query"),
            @ApiImplicitParam(name = "name", paramType = "query"),
            @ApiImplicitParam(name = "remark", paramType = "query"),
            @ApiImplicitParam(name = "ascs", paramType = "query"),
            @ApiImplicitParam(name = "descs", paramType = "query")
    })
    @GetMapping("/trigger/page")
    public Page<CycleTrigger> pageTrigger(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
                                @RequestParam @ApiIgnore Map<String, Object> map) {
        return cycleService.findTriggerByParamsPage(map, page, size);
    }
    
    @ApiOperation("根据字段参数查询轮询记录[分页]")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "page", paramType = "query"),
            @ApiImplicitParam(name = "size", paramType = "query"),
            @ApiImplicitParam(name = "retry", paramType = "query"),
            @ApiImplicitParam(name = "status", paramType = "query"),
            @ApiImplicitParam(name = Constant.MK_IS_COMPLETE, paramType = "query"),
            @ApiImplicitParam(name = "triggerId", paramType = "query"),
            @ApiImplicitParam(name = "triggerClassName", paramType = "query"),
            @ApiImplicitParam(name = "triggerGroup", paramType = "query"),
            @ApiImplicitParam(name = "ascs", paramType = "query"),
            @ApiImplicitParam(name = "descs", paramType = "query")
    })
    @GetMapping("/record/page")
    public Page<CycleRecord> pageRecord(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
                                @RequestParam @ApiIgnore Map<String, Object> map) {
        return cycleService.findRecordByParamsPage(map, page, size);
    }

    @ApiOperation("新增一个轮询触发器")
    @PostMapping("/trigger/")
    public CycleTrigger insertTrigger(@RequestBody Map<String, Object> map) {
    	CycleTrigger trigger = cycleService.transTriggerObject(map);
    	Assert.isTrue(trigger.isNew(), Constant.MSG_HASID_POST);
    	return cycleService.saveTriggerOne(trigger);
    }
    
    @ApiOperation("新增一个轮询记录")
    @PostMapping("/record/")
    public CycleRecord insertRecord(@RequestBody Map<String, Object> map) {
    	CycleRecord record = cycleService.transRecordObject(map);
    	Assert.isTrue(record.isNew(), Constant.MSG_HASID_POST);
    	return cycleService.saveRecordOne(record);
    }

    @ApiOperation("批量新增轮询触发器")
    @PostMapping("/trigger/list")
    public List<CycleTrigger> insertTriggerList(@RequestBody List<Map<String, Object>> list) {
        Assert.notEmpty(list, Constant.MSG_EMPTY_COLLECTION);
        List<CycleTrigger> triggers = Lists.newArrayList();
        for(Map<String, Object> item : list) {
        	CycleTrigger trigger = cycleService.transTriggerObject(item);
        	Assert.isTrue(trigger.isNew(), Constant.MSG_HASID_POST);
        	triggers.add(trigger);
        }
    	return cycleService.saveTriggerList(triggers);
    }
    
    @ApiOperation("批量新增轮询记录")
    @PostMapping("/record/list")
    public List<CycleRecord> insertRecordList(@RequestBody List<Map<String, Object>> list) {
        Assert.notEmpty(list, Constant.MSG_EMPTY_COLLECTION);
        List<CycleRecord> records = Lists.newArrayList();
        for(Map<String, Object> item : list) {
        	CycleRecord record = cycleService.transRecordObject(item);
        	Assert.isTrue(record.isNew(), Constant.MSG_HASID_POST);
        	records.add(record);
        }
    	return cycleService.saveRecordList(records);
    }
    
    @ApiOperation("修改一个轮询触发器")
    @PutMapping("/trigger/")
    public CycleTrigger updateTrigger(@RequestBody Map<String, Object> map) {
    	CycleTrigger trigger = cycleService.transTriggerObject(map);
    	Assert.isTrue(!trigger.isNew(), Constant.MSG_NOID_PUT);
    	return cycleService.saveTriggerOne(trigger);
    }
    
    @ApiOperation("修改一个轮询记录")
    @PutMapping("/record/")
    public CycleRecord updateRecord(@RequestBody Map<String, Object> map) {
    	CycleRecord record = cycleService.transRecordObject(map);
    	Assert.isTrue(!record.isNew(), Constant.MSG_NOID_PUT);
    	return cycleService.saveRecordOne(record);
    }

    @ApiOperation("批量修改轮询触发器")
    @PutMapping("/trigger/list")
    public List<CycleTrigger> updateTriggerList(@RequestBody List<Map<String, Object>> list) {
        Assert.notEmpty(list, Constant.MSG_EMPTY_COLLECTION);
        List<CycleTrigger> triggers = Lists.newArrayList();
        for(Map<String, Object> item : list) {
        	CycleTrigger trigger = cycleService.transTriggerObject(item);
        	Assert.isTrue(!trigger.isNew(), Constant.MSG_NOID_PUT);
        	triggers.add(trigger);
        }
    	return cycleService.saveTriggerList(triggers);
    }
    
    @ApiOperation("批量修改轮询记录")
    @PutMapping("/record/list")
    public List<CycleRecord> updateRecordList(@RequestBody List<Map<String, Object>> list) {
        Assert.notEmpty(list, Constant.MSG_EMPTY_COLLECTION);
        List<CycleRecord> records = Lists.newArrayList();
        for(Map<String, Object> item : list) {
        	CycleRecord record = cycleService.transRecordObject(item);
        	Assert.isTrue(!record.isNew(), Constant.MSG_NOID_PUT);
        	records.add(record);
        }
    	return cycleService.saveRecordList(records);
    }

    @ApiOperation("根据id删除轮询触发器")
    @DeleteMapping("/trigger/{ids}")
    public String deleteTriggerById(@PathVariable String ids) {
    	String[] id = StringUtils.commaDelimitedListToStringArray(ids);
		Assert.notEmpty(id, Constant.MSG_EMPTY_COLLECTION);
    	if (id.length <= 1) {
        	cycleService.deleteTriggerById(id[0]);
        } else {
        	cycleService.deleteTriggerList(Arrays.asList(id));
        }
        return Constant.MSG_SUCCESS;
    }
    
    @ApiOperation("根据id删除轮询记录")
    @DeleteMapping("/record/{ids}")
    public String deleteRecordById(@PathVariable String ids) {
    	String[] id = StringUtils.commaDelimitedListToStringArray(ids);
		Assert.notEmpty(id, Constant.MSG_EMPTY_COLLECTION);
    	if (id.length <= 1) {
        	cycleService.deleteRecordById(id[0]);
        } else {
        	cycleService.deleteRecordList(Arrays.asList(id));
        }
        return Constant.MSG_SUCCESS;
    }
}
