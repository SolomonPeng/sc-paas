package org.solo.paas.job.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.accenture.microservice.data.annotation.AuditChange;
import com.accenture.microservice.data.annotation.AuditDelete;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


/**
 * The persistent class for the ROTATE_CONFIG database table.
 * 
 */
@Entity
@DynamicInsert
@DynamicUpdate
@AuditChange
@AuditDelete
@Data
@EqualsAndHashCode(callSuper=false)
@Table(name="JOB_CYCLE_TRIGGER")
@ToString(exclude = {"cycleRecords"}, callSuper = false)
@JsonIgnoreProperties({"cycleRecords"})
public class CycleTrigger extends AbstractTrigger  {
	private static final long serialVersionUID = 1L;


	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="CURRENT_END_TIME")
	private Date currentEndTime;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="CURRENT_START_TIME")
	private Date currentStartTime;

	@Column(nullable=false)
	private int duration;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="END_TIME", nullable=false)
	private Date endTime;


	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="START_TIME", nullable=false)
	private Date startTime;


	//bi-directional many-to-one association to RotateRecord
	@OneToMany(mappedBy="cycleTrigger", cascade={CascadeType.ALL})
	private List<CycleRecord> cycleRecords;	

	public CycleRecord addCycleRecord(CycleRecord rotateRecord) {
		getCycleRecords().add(rotateRecord);
		rotateRecord.setCycleTrigger(this);
		return rotateRecord;
	}

	public CycleRecord removeCycleRecord(CycleRecord rotateRecord) {
		getCycleRecords().remove(rotateRecord);
		rotateRecord.setCycleTrigger(null);
		return rotateRecord;
	}

}