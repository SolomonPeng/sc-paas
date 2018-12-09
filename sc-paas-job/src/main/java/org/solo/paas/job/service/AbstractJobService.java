/**
 * 
 */
package org.solo.paas.job.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.util.StringUtils;

import com.accenture.microservice.core.util.CoreUtils;
import com.accenture.microservice.data.base.AbstractService;
import com.querydsl.jpa.JPQLQueryFactory;

/**
 * @author song.peng
 *
 */
public abstract class AbstractJobService extends AbstractService {

	@Autowired
	protected JPQLQueryFactory jpaQueryFactory;
	
	Sort transSort(String ascs,String descs) {
		Sort sort = new Sort(Direction.DESC, "createDate");
    	if(StringUtils.hasText(ascs)) {
    		String[] arrAscs = StringUtils.delimitedListToStringArray(ascs.trim(), "-");
    		for(String asc:arrAscs) {
    			sort.and(new Sort(Direction.ASC,asc));
    		}
    	}
    	if(StringUtils.hasText(descs)) {
    		String[] arrDescss = StringUtils.delimitedListToStringArray(descs.trim(), "-");
    		for(String desc:arrDescss) {
    			sort.and(new Sort(Direction.DESC,desc));
    		}
    	}
    	return sort;
	}
	
	Sort transSort(Map<String,Object> paramsMap) {
		return transSort(CoreUtils.optionalMapString(paramsMap, "ascs"),CoreUtils.optionalMapString(paramsMap, "descs"));
	}
}
