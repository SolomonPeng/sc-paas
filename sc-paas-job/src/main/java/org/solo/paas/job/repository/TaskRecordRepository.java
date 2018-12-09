/**
 * 
 */
package org.solo.paas.job.repository;

import org.solo.paas.job.domain.TaskRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * @author song.peng
 *
 */
@Repository
public interface TaskRecordRepository extends JpaRepository<TaskRecord, String>,QueryDslPredicateExecutor<TaskRecord> {
	//List<Record> findByTrigger(Trigger trigger);
}
