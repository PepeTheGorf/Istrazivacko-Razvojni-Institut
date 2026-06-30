package org.example.documentmanagementservice.repository;

import org.example.documentmanagementservice.model.Dokument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DokumentRepository extends JpaRepository<Dokument, UUID> {

    Optional<Dokument> findByVectorDocumentId(String vectorDocumentId);

    List<Dokument> findByProjektId(UUID projektId);

    @Query(value = """
            SELECT DISTINCT d.* FROM dokumenti d
            LEFT JOIN prava_pristupa pp_dok
                ON pp_dok.dokument_id = d.id AND pp_dok.korisnik_id = :korisnikId
            LEFT JOIN prava_pristupa pp_proj
                ON pp_proj.projekat_id = d.projekt_id AND pp_proj.korisnik_id = :korisnikId
            WHERE
                (d.author_id = :korisnikId AND COALESCE(pp_dok.nivo, '') != 'ZABRANA')
                OR (pp_dok.id IS NOT NULL AND pp_dok.nivo != 'ZABRANA')
                OR (pp_proj.id IS NOT NULL AND pp_proj.nivo != 'ZABRANA' AND pp_dok.id IS NULL)
            """, nativeQuery = true)
    List<Dokument> findDokumentiZaKorisnika(@Param("korisnikId") UUID korisnikId);

    @Query(value = """
            SELECT * FROM (
                SELECT DISTINCT d.*,
                    CASE WHEN CAST(:naslov AS text) IS NULL THEN 0 ELSE similarity(d.naslov, CAST(:naslov AS text)) END AS _sim
                FROM dokumenti d
                LEFT JOIN dokument_tag dt ON dt.dokument_id = d.id
                LEFT JOIN tag t ON t.id = dt.tag_id
                WHERE
                    (CAST(:naslov AS text) IS NULL OR similarity(d.naslov, CAST(:naslov AS text)) > 0.2 OR d.naslov ILIKE '%' || CAST(:naslov AS text) || '%')
                    AND (CAST(:autor AS text) IS NULL OR d.author_name ILIKE '%' || CAST(:autor AS text) || '%')
                    AND (CAST(:tipDokumentaId AS uuid) IS NULL OR d.tip_dokumenta_id = CAST(:tipDokumentaId AS uuid))
                    AND (CAST(:projektId AS uuid) IS NULL OR d.projekt_id = CAST(:projektId AS uuid))
                    AND (CAST(:dateFrom AS timestamptz) IS NULL OR d.created_at >= CAST(:dateFrom AS timestamptz))
                    AND (CAST(:dateTo AS timestamptz) IS NULL OR d.created_at <= CAST(:dateTo AS timestamptz))
                    AND (CAST(:tag AS text) IS NULL OR similarity(t.naziv, CAST(:tag AS text)) > 0.2 OR t.naziv ILIKE '%' || CAST(:tag AS text) || '%')
            ) sub
            ORDER BY _sim DESC, created_at DESC
            """,
            nativeQuery = true)
    List<Dokument> searchDokumenti(
            @Param("naslov") String naslov,
            @Param("autor") String autor,
            @Param("tipDokumentaId") UUID tipDokumentaId,
            @Param("projektId") UUID projektId,
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo,
            @Param("tag") String tag
    );
}
