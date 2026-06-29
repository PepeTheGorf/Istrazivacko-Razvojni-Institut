package org.example.projectrealizationservice.validation;

import org.example.projectrealizationservice.model.Project;
import org.example.projectrealizationservice.model.Task;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class TaskDateValidator {

    public void validate(Task task, Project project, Task parentTask) {
        OffsetDateTime startDate = task.getStartDate();
        OffsetDateTime endDate = task.getEndDate();

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new RuntimeException("Datum pocetka zadatka ne moze biti posle datuma zavrsetka.");
        }

        if (project != null) {
            validateWithinProjectBounds(startDate, endDate, project);
        }

        if (parentTask != null) {
            validateWithinParentBounds(startDate, endDate, parentTask);
        }
    }

    private void validateWithinProjectBounds(OffsetDateTime startDate, OffsetDateTime endDate, Project project) {
        OffsetDateTime projectStart = project.getStartDate();
        OffsetDateTime projectEnd = project.getEndDate();

        if (projectStart != null && startDate != null && startDate.isBefore(projectStart)) {
            throw new RuntimeException("Zadatak ne moze poceti pre pocetka projekta.");
        }
        if (projectEnd != null && endDate != null && endDate.isAfter(projectEnd)) {
            throw new RuntimeException("Zadatak ne moze zavrsiti nakon kraja projekta.");
        }
        if (projectEnd != null && startDate != null && startDate.isAfter(projectEnd)) {
            throw new RuntimeException("Zadatak ne moze poceti nakon kraja projekta.");
        }
    }

    private void validateWithinParentBounds(OffsetDateTime startDate, OffsetDateTime endDate, Task parentTask) {
        OffsetDateTime parentStart = parentTask.getStartDate();
        OffsetDateTime parentEnd = parentTask.getEndDate();

        if (parentStart != null && startDate != null && startDate.isBefore(parentStart)) {
            throw new RuntimeException("Podzadatak mora poceti nakon pocetka parent zadatka.");
        }
        if (parentEnd != null && endDate != null && endDate.isAfter(parentEnd)) {
            throw new RuntimeException("Podzadatak mora zavrsiti pre kraja parent zadatka.");
        }
    }
}
