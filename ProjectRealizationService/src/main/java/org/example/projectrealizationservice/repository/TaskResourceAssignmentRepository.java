package org.example.projectrealizationservice.repository;

import org.example.projectrealizationservice.model.TaskResourceAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskResourceAssignmentRepository extends JpaRepository<TaskResourceAssignment, Long> {

    List<TaskResourceAssignment> findByTask_Id(Long taskId);
}
