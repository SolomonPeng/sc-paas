/**
 * 
 */
package org.solo.paas.job.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.accenture.microservice.data.base.AbstractUUIdTable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author song.peng
 *
 */
@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper=false)
public abstract class AbstractRecord extends AbstractUUIdTable {

	private static final long serialVersionUID = 1L;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="START_TIME")
	private Date startTime;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="END_TIME")
	private Date endTime;
	
	@Column(name="STATUS")
	private int status = 0;
	
	@Column(name="PARAMS", length=1000)
	private String params;
	
	@Column(name="RETRY")
	private int retry = 0;
	
	@Column(length=1000)
	private String message;
}
