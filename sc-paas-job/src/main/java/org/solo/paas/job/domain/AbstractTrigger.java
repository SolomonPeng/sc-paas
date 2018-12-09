/**
 * 
 */
package org.solo.paas.job.domain;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

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
public class AbstractTrigger extends AbstractUUIdTable {
	
	private static final long serialVersionUID = 1L;

	@Column(length=20, nullable=false)
	private String cron;

	@Column(name="GROUP_", length=20, nullable=false)
	private String group;

	@Column(name="CLASS_NAME", length=100, nullable=false)
	private String className;
	
	@Column(length=100, nullable=false)
	private String name;

	@Column(name="STATUS", nullable=false)
	private int status;
	
	@Column(length=200)
	private String remark;
}
