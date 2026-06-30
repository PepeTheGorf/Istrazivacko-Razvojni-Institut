CREATE OR REPLACE FUNCTION add_task_phase_transition(
    p_task_id BIGINT,
    p_old_phase_id BIGINT,
    p_new_phase_id BIGINT,
    p_workflow_id BIGINT,
    p_assignee_id BIGINT,
    p_transitioned_at TIMESTAMPTZ DEFAULT NOW(),
    duration BIGINT
)
RETURNS VOID AS $$
DECLARE
    v_transition_condition_id BIGINT;
    v_task_assignment_id BIGINT;
BEGIN
    SELECT tc.id INTO v_transition_condition_id FROM transition_conditions tc
    WHERE tc.workflow_id = p_workflow_id AND tc.from_phase_id = p_old_phase_id AND tc.to_phase_id = p_new_phase_id
    ORDER BY tc.id
    LIMIT 1;

    IF v_transition_condition_id IS NULL THEN
        RAISE EXCEPTION 'Nije pronadjena tranzicija za zadatak %', p_task_id;
    END IF;

    SELECT ta.id INTO v_task_assignment_id FROM task_assignments ta
    WHERE ta.task_id = p_task_id AND ta.assignee_id = p_assignee_id
    LIMIT 1;

    IF v_task_assignment_id IS NULL THEN
        RAISE EXCEPTION 'Greska. Ne postoje trazeni podaci za zadatak % i korisnika %', p_task_id, p_assignee_id;
    END IF;

    INSERT INTO task_phase_transitions (task_assignment_id, transition_condition_id, transitioned_at, duration)
	VALUES (
        v_task_assignment_id,
        v_transition_condition_id,
        COALESCE(p_transitioned_at, NOW()),
        duration
    );
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION validate_task_dates()
RETURNS TRIGGER AS $$
DECLARE
    v_project_start TIMESTAMPTZ;
    v_project_end TIMESTAMPTZ;
    v_parent_start TIMESTAMPTZ;
    v_parent_end TIMESTAMPTZ;
BEGIN
    IF NEW.start_date > NEW.end_date THEN
        RAISE EXCEPTION 'Datum pocetka zadatka ne moze biti posle datuma zavrsetka.';
    END IF;

    SELECT p.start_date, p.end_date INTO v_project_start, v_project_end FROM projects p WHERE p.id = NEW.project_id;

    IF NEW.start_date < v_project_start THEN
        RAISE EXCEPTION 'Zadatak ne moze poceti pre pocetka projekta.';
    END IF;

    IF NEW.end_date > v_project_end THEN
        RAISE EXCEPTION 'Zadatak ne moze zavrsiti nakon kraja projekta.';
    END IF;

    IF NEW.start_date > v_project_end THEN
        RAISE EXCEPTION 'Zadatak ne moze poceti nakon kraja projekta.';
    END IF;

    SELECT t.start_date, t.end_date INTO v_parent_start, v_parent_end FROM tasks t WHERE t.id = NEW.parent_task_id;

    IF NEW.start_date < v_parent_start THEN
        RAISE EXCEPTION'Podzadatak mora poceti nakon pocetka parent zadatka.';
    END IF;

    IF NEW.end_date > v_parent_end THEN
        RAISE EXCEPTION 'Podzadatak mora zavrsiti pre kraja parent zadatka.';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER trg_validate_task_dates
BEFORE INSERT OR UPDATE OF start_date, end_date, project_id, parent_task_id ON tasks
FOR EACH ROW
EXECUTE FUNCTION validate_task_dates();
