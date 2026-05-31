package org.example.projectrealizationservice.repository.sql;

import org.example.projectrealizationservice.model.sql.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {

    List<TaskAssignment> findByTaskId(String taskId);

    List<TaskAssignment> findByAssigneeId(Long assigneeId);
}
