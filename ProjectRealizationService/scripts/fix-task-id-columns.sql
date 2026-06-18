-- Fixes schema after migrating task IDs from Neo4j element IDs (varchar) to bigint.
-- Old task_id values (e.g. "4:uuid:93") cannot be cast to bigint, so child rows are cleared.
-- Safe for dev: parent tasks/projects/workflows are kept; re-create assignments/criteria via the app.

BEGIN;

TRUNCATE TABLE
    acceptance_criteria,
    problem_reports,
    task_resource_assignments,
    task_assignments
RESTART IDENTITY CASCADE;

DROP TABLE IF EXISTS project_tasks;

ALTER TABLE acceptance_criteria
    ALTER COLUMN task_id TYPE bigint USING task_id::bigint;

ALTER TABLE problem_reports
    ALTER COLUMN task_id TYPE bigint USING task_id::bigint;

ALTER TABLE task_assignments
    ALTER COLUMN task_id TYPE bigint USING task_id::bigint;

ALTER TABLE task_resource_assignments
    ALTER COLUMN task_id TYPE bigint USING task_id::bigint;

COMMIT;
