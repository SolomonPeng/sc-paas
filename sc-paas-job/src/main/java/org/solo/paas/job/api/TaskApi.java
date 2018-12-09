/**
 * 
 */
package org.solo.paas.job.api;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.solo.paas.job.base.Constant;
import org.solo.paas.job.domain.TaskRecord;
import org.solo.paas.job.domain.TaskTrigger;
import org.solo.paas.job.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
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
@RequestMapping("/task")
@Api(tags="task",description = "任务处理api")
public class TaskApi extends BaseApi {

	@Autowired
	TaskService taskService;
	
	@ApiOperation("根据id查询任务触发器")
    @GetMapping("/trigger/{ids}")
    public Object getTriggerById(@PathVariable String[] ids) {
		Assert.notEmpty(ids, Constant.MSG_EMPTY_COLLECTION);
		if (ids.length <= 1) {
			return taskService.findTriggerById(ids[0]);
		}else {
			return taskService.findTriggerByIds(Arrays.asList(ids));
		}
    }
	
	@ApiOperation("根据id查询任务记录")
    @GetMapping("/record/{ids}")
    public Object getRecordById(@PathVariable String[] ids) {
		Assert.notEmpty(ids, Constant.MSG_EMPTY_COLLECTION);
		if (ids.length <= 1) {
			return taskService.findRecordById(ids[0]);
		}else {
			return taskService.findRecordByIds(Arrays.asList(ids));
		}
    }

    @ApiOperation("根据字段参数查询任务触发器")
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
    public List<TaskTrigger> listTrigger(@RequestParam @ApiIgnore Map<String, Object> map) {
    	return taskService.findTriggerByParams(map);
    }
    
    @ApiOperation("根据字段参数查询任务记录")
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
    public List<TaskRecord> listRecord(@RequestParam @ApiIgnore Map<String, Object> map) {
    	return taskService.findRecordByParams(map);
    }

    @ApiOperation("根据字段参数查询任务触发器[分页]")
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
    public Page<TaskTrigger> pageTrigger(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
                                @RequestParam @ApiIgnore Map<String, Object> map) {
        return taskService.findTriggerByParamsPage(map, page, size);
    }
    
    @ApiOperation("根据字段参数查询任务记录[分页]")
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
    public Page<TaskRecord> pageRecord(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
                                @RequestParam @ApiIgnore Map<String, Object> map) {
        return taskService.findRecordByParamsPage(map, page, size);
    }

    @ApiOperation("新增一个任务触发器")
    @PostMapping("/trigger/")
    public TaskTrigger insertTrigger(@RequestBody Map<String, Object> map) {
    	TaskTrigger trigger = taskService.transTriggerObject(map);
    	Assert.isTrue(trigger.isNew(), Constant.MSG_HASID_POST);
    	return taskService.saveTriggerOne(trigger);
    }
    
    @ApiOperation("新增一个任务记录")
    @PostMapping("/record/")
    public TaskRecord insertRecord(@RequestBody Map<String, Object> map) {
    	TaskRecord record = taskService.transRecordObject(map);
    	Assert.isTrue(record.isNew(), Constant.MSG_HASID_POST);
    	return taskService.saveRecordOne(record);
    }

    @ApiOperation("批量新增任务触发器")
    @PostMapping("/trigger/list")
    public List<TaskTrigger> insertTriggerList(@RequestBody List<Map<String, Object>> list) {
        Assert.notEmpty(list, Constant.MSG_EMPTY_COLLECTION);
        List<TaskTrigger> triggers = Lists.newArrayList();
        for(Map<String, Object> item : list) {
        	TaskTrigger trigger = taskService.transTriggerObject(item);
        	Assert.isTrue(trigger.isNew(), Constant.MSG_HASID_POST);
        	triggers.add(trigger);
        }
    	return taskService.saveTriggerList(triggers);
    }
    
    @ApiOperation("批量新增任务记录")
    @PostMapping("/record/list")
    public List<TaskRecord> insertRecordList(@RequestBody List<Map<String, Object>> list) {
        Assert.notEmpty(list, Constant.MSG_EMPTY_COLLECTION);
        List<TaskRecord> records = Lists.newArrayList();
        for(Map<String, Object> item : list) {
        	TaskRecord record = taskService.transRecordObject(item);
        	Assert.isTrue(record.isNew(), Constant.MSG_HASID_POST);
        	records.add(record);
        }
    	return taskService.saveRecordList(records);
    }
    
    @ApiOperation("修改一个任务触发器")
    @PutMapping("/trigger/")
    public TaskTrigger updateTrigger(@RequestBody Map<String, Object> map) {
    	TaskTrigger trigger = taskService.transTriggerObject(map);
    	Assert.isTrue(!trigger.isNew(), Constant.MSG_NOID_PUT);
    	return taskService.saveTriggerOne(trigger);
    }
    
    @ApiOperation("修改一个任务记录")
    @PutMapping("/record/")
    public TaskRecord updateRecord(@RequestBody Map<String, Object> map) {
    	TaskRecord record = taskService.transRecordObject(map);
    	Assert.isTrue(!record.isNew(), Constant.MSG_NOID_PUT);
    	return taskService.saveRecordOne(record);
    }

    @ApiOperation("批量修改任务触发器")
    @PutMapping("/trigger/list")
    public List<TaskTrigger> updateTriggerList(@RequestBody List<Map<String, Object>> list) {
        Assert.notEmpty(list, Constant.MSG_EMPTY_COLLECTION);
        List<TaskTrigger> triggers = Lists.newArrayList();
        for(Map<String, Object> item : list) {
        	TaskTrigger trigger = taskService.transTriggerObject(item);
        	Assert.isTrue(!trigger.isNew(), Constant.MSG_NOID_PUT);
        	triggers.add(trigger);
        }
    	return taskService.saveTriggerList(triggers);
    }
    
    @ApiOperation("批量修改任务记录")
    @PutMapping("/record/list")
    public List<TaskRecord> updateRecordList(@RequestBody List<Map<String, Object>> list) {
        Assert.notEmpty(list, Constant.MSG_EMPTY_COLLECTION);
        List<TaskRecord> records = Lists.newArrayList();
        for(Map<String, Object> item : list) {
        	TaskRecord record = taskService.transRecordObject(item);
        	Assert.isTrue(!record.isNew(), Constant.MSG_NOID_PUT);
        	records.add(record);
        }
    	return taskService.saveRecordList(records);
    }

    @ApiOperation("根据id删除任务触发器")
    @DeleteMapping("/trigger/{ids}")
    public String deleteTriggerById(@PathVariable("ids") String[] ids) {
        if (ids.length <= 1) {
        	taskService.deleteTriggerById(ids[0]);
        } else {
        	taskService.deleteTriggerList(Arrays.asList(ids));
        }
        return Constant.MSG_SUCCESS;
    }
    
    @ApiOperation("根据id删除任务记录")
    @DeleteMapping("/record/{ids}")
    public String deleteRecordById(@PathVariable("ids") String[] ids) {
        if (ids.length <= 1) {
        	taskService.deleteRecordById(ids[0]);
        } else {
        	taskService.deleteRecordList(Arrays.asList(ids));
        }
        return Constant.MSG_SUCCESS;
    }
}
