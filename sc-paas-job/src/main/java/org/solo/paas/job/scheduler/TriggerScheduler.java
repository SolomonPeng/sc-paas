package org.solo.paas.job.scheduler;

import java.util.List;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.solo.paas.job.domain.AbstractTrigger;
import org.solo.paas.job.domain.CycleTrigger;
import org.solo.paas.job.domain.TaskTrigger;
import org.solo.paas.job.service.CycleService;
import org.solo.paas.job.service.TaskService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TriggerScheduler implements InitializingBean{

	@Autowired
    Scheduler scheduler;
	@Autowired
	TaskService taskTriggerService;
	@Autowired
	CycleService cycleTriggerService;
	
	@SuppressWarnings("unchecked")
	@Scheduled(cron = "${org.quartz.scheduler.refreshCron}")
	public void refreshTrigger() {
		try {
			// 查询出数据库中所有的定时任务
			List<TaskTrigger> taskList = taskTriggerService.findTriggerAll();
			List<CycleTrigger> cycleList = cycleTriggerService.findTriggerAll();
			List<AbstractTrigger> jobList = Lists.newArrayList();
			jobList.addAll(taskList);
			jobList.addAll(cycleList);
			if (!CollectionUtils.isEmpty(jobList)) {
				for (AbstractTrigger scheduleJob : jobList) {
					Integer status = scheduleJob.getStatus(); // 该任务触发器目前的状态
					TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.getClassName(), scheduleJob.getGroup());
					CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
					// 说明本条任务还没有添加到quartz中
					if (ObjectUtils.isEmpty(trigger)) {
						if (status.equals(Integer.valueOf(0))) { // 如果是禁用，则不用创建触发器
							continue;
						}
						JobDetail jobDetail = null;
						try {
							// 创建JobDetail（数据库中job_name存的任务全路径，这里就可以动态的把任务注入到JobDetail中）
							jobDetail = JobBuilder
									.newJob((Class<? extends Job>) Class.forName(scheduleJob.getClassName()))
									.withIdentity(scheduleJob.getClassName(), scheduleJob.getGroup()).build();

							// 表达式调度构建器
							CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
									.cronSchedule(scheduleJob.getCron());
							/// 设置定时任务的时间触发规则
							trigger = TriggerBuilder.newTrigger()
									.withIdentity(scheduleJob.getClassName(), scheduleJob.getGroup())
									.withSchedule(scheduleBuilder).build();
							// 把trigger和jobDetail注入到调度器
							scheduler.scheduleJob(jobDetail, trigger);
						} catch (ClassNotFoundException e) {
							log.error("找不到类名:",e);
						}

					} else { // 说明查出来的这条任务，已经设置到quartz中了
						// Trigger已存在，先判断是否需要删除，如果不需要，再判定是否时间有变化
						if (status.equals(Integer.valueOf(0))) { // 如果是禁用，从quartz中删除这条任务
							JobKey jobKey = JobKey.jobKey(scheduleJob.getClassName(), scheduleJob.getGroup());
							scheduler.deleteJob(jobKey);
							continue;
						}
						String searchCron = scheduleJob.getCron(); // 获取数据库的
						String currentCron = trigger.getCronExpression();
						if (!searchCron.equals(currentCron)) { // 说明该任务有变化，需要更新quartz中的对应的记录
							// 表达式调度构建器
							CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(searchCron);

							// 按新的cronExpression表达式重新构建trigger
							trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder)
									.build();

							// 按新的trigger重新设置job执行
							scheduler.rescheduleJob(triggerKey, trigger);
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("定时任务每日刷新触发器任务异常:", e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		refreshTrigger();
	}
}
