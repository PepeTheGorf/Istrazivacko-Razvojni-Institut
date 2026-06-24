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

    @Query("SELECT AVG(f.rating) FROM SectionFeedback f WHERE f.section.usedPromptVersion.id = :versionId")
    Double getAverageRatingByVersionId(@Param("versionId") Long versionId);

    @Query("SELECT f.comment FROM SectionFeedback f WHERE f.section.usedPromptVersion.id = :versionId AND f.comment IS NOT NULL AND f.comment != ''")
    List<String> findAllCommentsByVersionId(@Param("versionId") Long versionId);
    
    @Query("SELECT COUNT(f) FROM SectionFeedback f WHERE f.section.usedPromptVersion.id = :versionId")
    Integer countFeedbackByVersionId(@Param("versionId") Long versionId);

}