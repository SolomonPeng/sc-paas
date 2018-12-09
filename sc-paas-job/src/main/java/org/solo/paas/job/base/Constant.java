package org.solo.paas.job.base;

public class Constant {

	public static final String MK_IS_COMPLETE = "isCompleted";
	public static final int RETRY_THRESHOLD = 5;
	public static final Integer PAGE_SIZE = Integer.valueOf(500);
	public static final String MSG_SUCCESS_PATTERN = "处理成功,共更新数据%d条";
	public static final String MSG_FAULT_PATTERN = "处理失败,异常信息:%s";
	//public static final String LOG_TASK_START_PATTERN = "Task {} Start!";
	//public static final String LOG_TASK_FATAL_OVERTHRESHOLD_PATTEN = "Task {} fault over threshold!";
	//public static final String LOG_TASK_END_PATTERN = "Task {} End!";
	public static final String GROUP_MDM = "icp-isp-mdm";
	public static final String GROUP_ERP = "icp-isp-erp";
	public static final String GROUP_CMS = "icp-isp-cms";
	
	public static final String MSG_EMPTY_COLLECTION = "输入集合对象为空";
	public static final String MSG_NULL_PARAM = "输入参数对象为空";
	public static final String MSG_HASID_POST = "新增对象不可带已有ID";
	public static final String MSG_NOID_PUT = "修改对象必须带已有ID";
	public static final String MSG_SUCCESS = "处理成功";
}
