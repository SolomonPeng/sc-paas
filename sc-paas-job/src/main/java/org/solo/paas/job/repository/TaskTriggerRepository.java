/**
 * 
 */
package org.solo.paas.job.repository;

import org.solo.paas.job.domain.TaskTrigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * @author song.peng
 *
 */
@Repository
public interface TaskTriggerRepository extends JpaRepository<TaskTrigger, String>,QueryDslPredicateExecutor<TaskTrigger> {

}
