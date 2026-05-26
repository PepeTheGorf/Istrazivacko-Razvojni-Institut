package org.example.projectrealizationservice.repository.sql;

import org.example.projectrealizationservice.model.sql.TaskResourceAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskResourceAssignmentRepository extends JpaRepository<TaskResourceAssignment, Long> {

    List<TaskResourceAssignment> findByTaskId(String taskId);
}
