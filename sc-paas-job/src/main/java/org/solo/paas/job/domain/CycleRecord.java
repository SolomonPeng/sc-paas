package org.solo.paas.job.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.accenture.microservice.data.annotation.AuditChange;
import com.accenture.microservice.data.annotation.AuditDelete;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * The persistent class for the ROTATE_RECORD database table.
 * 
 */
@Entity
@DynamicInsert
@DynamicUpdate
@AuditChange
@AuditDelete
@Data
@EqualsAndHashCode(callSuper=false)
@Table(name="JOB_CYCLE_RECORD")
public class CycleRecord extends AbstractRecord  {
	
	private static final long serialVersionUID = 1L;

	//bi-directional many-to-one association to RotateConfig
	@ManyToOne
	@JoinColumn(name="TRIGGER_ID", nullable=false)
	private CycleTrigger cycleTrigger;

	@Transient
	private Date currentStartTime;
	
	@Transient
	private Date currentEndTime;
	
	@Transient
	private Integer rowCount=Integer.valueOf(0);
}