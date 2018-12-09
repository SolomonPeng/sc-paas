/**
 * 
 */
package org.solo.paas.job.task.mdm;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.solo.paas.job.base.Constant;
import org.solo.paas.job.feign.MdmFeign;
import org.solo.paas.job.task.AbstractJobTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author song.peng
 *
 */
@Component
public class VendorSyncTask extends AbstractJobTask {

	@Autowired
	MdmFeign mdmFeign;
	
	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		executeTask(Constant.GROUP_MDM,Constant.PAGE_SIZE,x->mdmFeign.vendorSync(x));
	}


}
