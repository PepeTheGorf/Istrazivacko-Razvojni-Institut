package org.example.projectrealizationservice.repository.sql.smartdocs;

import org.example.projectrealizationservice.model.sql.smartdocs.SectionFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SectionFeedbackRepository extends JpaRepository<SectionFeedback, Long> {

    @Query("SELECT AVG(f.rating) FROM SectionFeedback f "+
            "JOIN f.section s " +
            "JOIN s.document d " +
            "WHERE d.template.id = :templateId")
    Double getAverageRatingByTemplateId(@Param("templateId") Long templateId);

    @Query("SELECT d.template.id, AVG(f.rating) FROM SectionFeedback f " +
           "JOIN f.section s JOIN s.document d GROUP BY d.template.id")
    List<Object[]> findAllAverageRatings();

}