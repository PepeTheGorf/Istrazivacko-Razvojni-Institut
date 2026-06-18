package org.example.projectrealizationservice.repository;

import org.example.projectrealizationservice.model.Task;
import org.example.projectrealizationservice.model.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {

    List<TaskAssignment> findByTask_Id(Long taskId);

    List<TaskAssignment> findByAssigneeId(Long assigneeId);
    
    Boolean existsByTaskAndAssigneeId(Task task, Long assigneeId);
}
