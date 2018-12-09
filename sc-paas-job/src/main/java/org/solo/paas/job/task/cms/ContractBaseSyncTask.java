/**
 * 
 */
package org.solo.paas.job.task.cms;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.solo.paas.job.base.Constant;
import org.solo.paas.job.feign.CmsFeign;
import org.solo.paas.job.task.AbstractJobTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author song.peng
 *
 */
@Component
public class ContractBaseSyncTask extends AbstractJobTask {

	@Autowired
	CmsFeign cmsFeign;
	
	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		executeTask(Constant.GROUP_CMS,Constant.PAGE_SIZE,x->cmsFeign.contractBaseSync(x));
	}

}
