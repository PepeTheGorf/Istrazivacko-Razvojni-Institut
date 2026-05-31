package org.example.projectrealizationservice.repository.sql;

import org.example.projectrealizationservice.model.sql.ProjectTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Long> {

    Optional<ProjectTask> findByTaskId(String taskId);

    List<ProjectTask> findByProjectId(Long projectId);
}
