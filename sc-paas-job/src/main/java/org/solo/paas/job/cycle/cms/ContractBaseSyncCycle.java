/**
 * 
 */
package org.solo.paas.job.cycle.cms;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.solo.paas.job.base.Constant;
import org.solo.paas.job.cycle.AbstractJobCycle;
import org.solo.paas.job.feign.CmsFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author song.peng
 *
 */
@Component
public class ContractBaseSyncCycle extends AbstractJobCycle {

	@Autowired
	CmsFeign cmsFeign;
	
	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		executeCycle(Constant.GROUP_CMS,Constant.PAGE_SIZE,x->cmsFeign.contractBaseSync(x));
	}

}
