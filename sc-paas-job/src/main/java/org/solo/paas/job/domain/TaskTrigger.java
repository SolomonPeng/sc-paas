package org.solo.paas.job.domain;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.accenture.microservice.data.annotation.AuditChange;
import com.accenture.microservice.data.annotation.AuditDelete;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


/**
 * The persistent class for the JOB_TRIGGER database table.
 * 
 */
@Entity
@DynamicInsert
@DynamicUpdate
@AuditChange
@AuditDelete
@Data
@EqualsAndHashCode(callSuper=false)
@Table(name="JOB_TASK_TRIGGER")
@ToString(exclude = {"taskRecords"}, callSuper = false)
@JsonIgnoreProperties({"taskRecords"})
public class TaskTrigger extends AbstractTrigger  {
	private static final long serialVersionUID = 1L;
	
	//bi-directional many-to-one association to Record
	@OneToMany(mappedBy="taskTrigger",cascade=CascadeType.ALL)
	private List<TaskRecord> taskRecords;

}