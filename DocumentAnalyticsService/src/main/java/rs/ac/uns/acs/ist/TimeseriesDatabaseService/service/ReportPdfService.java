package rs.ac.uns.acs.ist.TimeseriesDatabaseService.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.ist.TimeseriesDatabaseService.model.DocumentAccess;
import rs.ac.uns.acs.ist.TimeseriesDatabaseService.model.DocumentChange;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportPdfService {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm").withZone(ZoneId.of("Europe/Belgrade"));

    private static final Color INK       = new Color(30, 30, 30);
    private static final Color INK_MUTED = new Color(110, 110, 110);
    private static final Color ACCENT    = new Color(79, 70, 229);
    private static final Color SUCCESS   = new Color(22, 163, 74);
    private static final Color DANGER    = new Color(220, 38, 38);
    private static final Color WHITE     = new Color(255, 255, 255);
    private static final Color BG_HEAD   = new Color(242, 242, 246);
    private static final Color BG_ALT    = new Color(250, 250, 253);
    private static final Color BORDER    = new Color(218, 218, 226);
    private static final Color RULE      = new Color(200, 200, 210);

    private static final Font F_TITLE   = new Font(Font.HELVETICA, 14, Font.BOLD,   INK);
    private static final Font F_SUB     = new Font(Font.HELVETICA,  8, Font.NORMAL, INK_MUTED);
    private static final Font F_SECTION = new Font(Font.HELVETICA,  7, Font.BOLD,   INK_MUTED);
    private static final Font F_STAT_L  = new Font(Font.HELVETICA,  7, Font.NORMAL, INK_MUTED);
    private static final Font F_STAT_V  = new Font(Font.HELVETICA, 11, Font.BOLD,   INK);
    private static final Font F_THEAD   = new Font(Font.HELVETICA,  7, Font.BOLD,   INK_MUTED);
    private static final Font F_CELL    = new Font(Font.HELVETICA,  7, Font.NORMAL, INK);
    private static final Font F_ACCENT  = new Font(Font.HELVETICA,  7, Font.BOLD,   ACCENT);

    public byte[] generateOverallReport(List<DocumentAccess> accesses, Instant from, Instant to,
                                        Map<String, String> docNames, Map<String, String> userNames) {
        return buildPdf(doc -> {
            addHeader(doc, "Izveštaj pristupa dokumentima", from, to,
                      labels("Ukupno pristupa", "Aktivnih korisnika", "Dokumenata pregledano"),
                      values(accesses.size(),
                             distinct(accesses, DocumentAccess::getUser_id),
                             distinct(accesses, DocumentAccess::getDocument_id)));
            addTopSection(doc, groupBy(accesses, DocumentAccess::getDocument_id),
                              groupBy(accesses, DocumentAccess::getUser_id), docNames, userNames);
            addSection(doc, "Detalji pristupa");
            addTable(doc, accesses, docNames, userNames, true, true);
        });
    }

    public byte[] generateDocumentReport(List<DocumentAccess> accesses, String documentId, String documentName,
                                         Instant from, Instant to, Map<String, String> userNames) {
        return buildPdf(doc -> {
            addHeader(doc, "Pristup dokumentu: " + documentName, from, to,
                      labels("Ukupno pristupa", "Jedinstvenih korisnika"),
                      values(accesses.size(), distinct(accesses, DocumentAccess::getUser_id)));
            addSection(doc, "Detalji pristupa");
            addTable(doc, accesses, Map.of(), userNames, false, true);
        });
    }

    public byte[] generateUserReport(List<DocumentAccess> accesses, String userId, String userName,
                                     Instant from, Instant to, Map<String, String> docNames) {
        return buildPdf(doc -> {
            addHeader(doc, "Pristupi korisnika: " + userName, from, to,
                      labels("Ukupno pristupa", "Jedinstvenih dokumenata"),
                      values(accesses.size(), distinct(accesses, DocumentAccess::getDocument_id)));
            addSection(doc, "Detalji pristupa");
            addTable(doc, accesses, docNames, Map.of(), true, false);
        });
    }

    public byte[] generateOverallChangeReport(List<DocumentChange> changes, Instant from, Instant to,
                                              Map<String, String> docNames, Map<String, String> userNames) {
        return buildPdf(doc -> {
            addHeader(doc, "Izveštaj izmena dokumenata", from, to,
                      labels("Ukupno izmena", "Izmena sadržaja", "Izmena metapodataka", "Aktivnih korisnika"),
                      values(changes.size(),
                             countWhere1(changes, DocumentChange::getIs_content_change),
                             countWhere1(changes, DocumentChange::getIs_metadata_change),
                             distinct(changes, DocumentChange::getUser_id)));
            addTopSection(doc, groupBy(changes, DocumentChange::getDocument_id),
                              groupBy(changes, DocumentChange::getUser_id), docNames, userNames);
            addSection(doc, "Detalji izmena");
            addChangeTable(doc, changes, docNames, userNames, true, true);
        });
    }

    public byte[] generateDocumentChangeReport(List<DocumentChange> changes, String documentName,
                                               Instant from, Instant to, Map<String, String> userNames) {
        return buildPdf(doc -> {
            addHeader(doc, "Izmene dokumenta: " + documentName, from, to,
                      labels("Ukupno izmena", "Izmena sadržaja", "Izmena metapodataka", "Korisnika"),
                      values(changes.size(),
                             countWhere1(changes, DocumentChange::getIs_content_change),
                             countWhere1(changes, DocumentChange::getIs_metadata_change),
                             distinct(changes, DocumentChange::getUser_id)));
            addSection(doc, "Detalji izmena");
            addChangeTable(doc, changes, Map.of(), userNames, false, true);
        });
    }

    public byte[] generateUserChangeReport(List<DocumentChange> changes, String userName,
                                           Instant from, Instant to, Map<String, String> docNames) {
        return buildPdf(doc -> {
            addHeader(doc, "Izmene korisnika: " + userName, from, to,
                      labels("Ukupno izmena", "Izmena sadržaja", "Izmena metapodataka", "Dokumenata"),
                      values(changes.size(),
                             countWhere1(changes, DocumentChange::getIs_content_change),
                             countWhere1(changes, DocumentChange::getIs_metadata_change),
                             distinct(changes, DocumentChange::getDocument_id)));
            addSection(doc, "Detalji izmena");
            addChangeTable(doc, changes, docNames, Map.of(), true, false);
        });
    }

    @FunctionalInterface
    private interface PdfBody {
        void write(Document doc) throws Exception;
    }

    private byte[] buildPdf(PdfBody body) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 48, 48, 48, 40);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            doc.open();
            body.write(doc);
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

   private void addHeader(Document doc, String title, Instant from, Instant to,
                           String[] lbls, long[] vals) throws DocumentException {
        Paragraph p = new Paragraph(title, F_TITLE);
        p.setSpacingAfter(2);
        doc.add(p);

        Paragraph range = new Paragraph(FMT.format(from) + "  –  " + FMT.format(to), F_SUB);
        range.setSpacingAfter(10);
        doc.add(range);

        addRule(doc);

        PdfPTable stats = new PdfPTable(lbls.length * 2);
        float[] widths = new float[lbls.length * 2];
        for (int i = 0; i < lbls.length; i++) {
            widths[i * 2]     = 2.5f; 
            widths[i * 2 + 1] = 1f;   
        }
        stats.setWidths(widths);
        stats.setWidthPercentage(100);
        stats.setSpacingAfter(14);

        for (int i = 0; i < lbls.length; i++) {
            PdfPCell lbl = new PdfPCell(new Phrase(lbls[i], F_STAT_L));
            lbl.setBorder(Rectangle.NO_BORDER);
            lbl.setPaddingTop(4);
            lbl.setPaddingBottom(4);
            stats.addCell(lbl);

            PdfPCell val = new PdfPCell(new Phrase(String.valueOf(vals[i]), F_STAT_V));
            val.setBorder(Rectangle.NO_BORDER);
            val.setPaddingTop(2);
            val.setPaddingBottom(4);
            stats.addCell(val);
        }
        doc.add(stats);
    }

    private void addRule(Document doc) throws DocumentException {
        PdfPTable rule = new PdfPTable(1);
        rule.setWidthPercentage(100);
        rule.setSpacingAfter(8);
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(RULE);
        c.setBorderWidth(0.5f);
        c.setFixedHeight(1f);
        c.setPadding(0);
        rule.addCell(c);
        doc.add(rule);
    }

    private void addSection(Document doc, String title) throws DocumentException {
        Paragraph p = new Paragraph(title.toUpperCase(), F_SECTION);
        p.setSpacingBefore(8);
        p.setSpacingAfter(4);
        doc.add(p);
    }

    private void addTopSection(Document doc, Map<String, Long> byDoc, Map<String, Long> byUser,
                                Map<String, String> docNames, Map<String, String> userNames) throws DocumentException {
        addSection(doc, "Najaktivniji dokumenti");
        addBarChart(doc, byDoc, docNames);
        addSection(doc, "Najaktivniji korisnici");
        addBarChart(doc, byUser, userNames);
    }

    private void addBarChart(Document doc, Map<String, Long> counts, Map<String, String> names) throws DocumentException {
        List<Map.Entry<String, Long>> top = counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());
        if (top.isEmpty()) return;

        long max = top.get(0).getValue();
        PdfPTable chart = new PdfPTable(new float[]{2.8f, 6f, 0.6f});
        chart.setWidthPercentage(100);
        chart.setSpacingAfter(8);

        for (Map.Entry<String, Long> e : top) {
            PdfPCell label = new PdfPCell(new Phrase(truncate(names.getOrDefault(e.getKey(), e.getKey()), 32), F_CELL));
            label.setBorder(Rectangle.NO_BORDER);
            label.setVerticalAlignment(Element.ALIGN_MIDDLE);
            label.setPaddingTop(3);
            label.setPaddingBottom(3);
            label.setPaddingRight(6);
            chart.addCell(label);

            float fraction = max > 0 ? (float) e.getValue() / max : 0f;
            float filled = Math.max(fraction * 85f, 1.5f);
            float empty = 100f - filled;

            PdfPTable barWrap = new PdfPTable(2);
            barWrap.setWidths(new float[]{filled, empty});
            barWrap.setWidthPercentage(100);

            PdfPCell bar = new PdfPCell();
            bar.setBackgroundColor(ACCENT);
            bar.setBorder(Rectangle.NO_BORDER);
            bar.setFixedHeight(11f);
            barWrap.addCell(bar);

            PdfPCell spacer = new PdfPCell();
            spacer.setBorder(Rectangle.NO_BORDER);
            barWrap.addCell(spacer);

            PdfPCell barCell = new PdfPCell(barWrap);
            barCell.setBorder(Rectangle.NO_BORDER);
            barCell.setPaddingTop(3);
            barCell.setPaddingBottom(3);
            chart.addCell(barCell);

            PdfPCell val = new PdfPCell(new Phrase(String.valueOf(e.getValue()), F_ACCENT));
            val.setBorder(Rectangle.NO_BORDER);
            val.setVerticalAlignment(Element.ALIGN_MIDDLE);
            val.setHorizontalAlignment(Element.ALIGN_LEFT);
            val.setPaddingLeft(5);
            val.setPaddingTop(3);
            val.setPaddingBottom(3);
            chart.addCell(val);
        }
        doc.add(chart);
    }

    private String truncate(String value, int len) {
        if (value == null) return "—";
        return value.length() > len ? value.substring(0, len) + "…" : value;
    }

    private void addTopTable(Document doc, Map<String, Long> counts, Map<String, String> names) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{6f, 1f});
        t.setWidthPercentage(100);
        t.setSpacingAfter(4);

        t.addCell(headCell("Naziv"));
        t.addCell(headCell("Broj"));

        boolean alt = false;
        for (Map.Entry<String, Long> e : counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList())) {
            Color bg = (alt = !alt) ? BG_ALT : WHITE;
            t.addCell(dataCell(names.getOrDefault(e.getKey(), e.getKey()), bg));
            PdfPCell cnt = new PdfPCell(new Phrase(String.valueOf(e.getValue()), F_ACCENT));
            cnt.setBackgroundColor(bg);
            cnt.setBorderColor(BORDER);
            cnt.setBorderWidth(0.5f);
            cnt.setPadding(4);
            cnt.setHorizontalAlignment(Element.ALIGN_CENTER);
            t.addCell(cnt);
        }
        doc.add(t);
    }

    private void addTable(Document doc, List<DocumentAccess> rows,
                          Map<String, String> docNames, Map<String, String> userNames,
                          boolean showDoc, boolean showUser) throws DocumentException {
        java.util.List<String> hdrs = new java.util.ArrayList<>();
        if (showUser) hdrs.add("Korisnik");
        if (showDoc)  hdrs.add("Dokument");
        hdrs.add("Datum i vreme");
        hdrs.add("Tip akcije");

        PdfPTable t = newTable(hdrs.size());
        hdrs.forEach(h -> t.addCell(headCell(h)));

        boolean alt = false;
        for (DocumentAccess a : rows) {
            Color bg = (alt = !alt) ? BG_ALT : WHITE;
            if (showUser) t.addCell(dataCell(resolve(a.getUser_id(), userNames), bg));
            if (showDoc)  t.addCell(dataCell(resolve(a.getDocument_id(), docNames), bg));
            t.addCell(dataCell(a.getCreated() != null ? FMT.format(a.getCreated()) : "—", bg));
            t.addCell(accentCell(a.getAction_type() != null ? a.getAction_type() : "—", bg));
        }
        doc.add(t);
    }

    private void addChangeTable(Document doc, List<DocumentChange> rows,
                                Map<String, String> docNames, Map<String, String> userNames,
                                boolean showDoc, boolean showUser) throws DocumentException {
        java.util.List<String> hdrs = new java.util.ArrayList<>();
        if (showUser) hdrs.add("Korisnik");
        if (showDoc)  hdrs.add("Dokument");
        hdrs.add("Tip izmene");
        hdrs.add("Sadržaj");
        hdrs.add("Metapodaci");

        PdfPTable t = newTable(hdrs.size());
        hdrs.forEach(h -> t.addCell(headCell(h)));

        boolean alt = false;
        for (DocumentChange c : rows) {
            Color bg = (alt = !alt) ? BG_ALT : WHITE;
            if (showUser) t.addCell(dataCell(resolve(c.getUser_id(), userNames), bg));
            if (showDoc)  t.addCell(dataCell(resolve(c.getDocument_id(), docNames), bg));
            t.addCell(accentCell(c.getChange_type() != null ? c.getChange_type() : "—", bg));
            t.addCell(boolCell(c.getIs_content_change(), bg));
            t.addCell(boolCell(c.getIs_metadata_change(), bg));
        }
        doc.add(t);
    }

    private PdfPTable newTable(int cols) {
        PdfPTable t = new PdfPTable(cols);
        t.setWidthPercentage(100);
        t.setSpacingAfter(8);
        return t;
    }

    private PdfPCell headCell(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, F_THEAD));
        c.setBackgroundColor(BG_HEAD);
        c.setBorderColor(BORDER);
        c.setBorderWidth(0.5f);
        c.setPadding(5);
        return c;
    }

    private PdfPCell dataCell(String text, Color bg) {
        PdfPCell c = new PdfPCell(new Phrase(text, F_CELL));
        c.setBackgroundColor(bg);
        c.setBorderColor(BORDER);
        c.setBorderWidth(0.5f);
        c.setPadding(4);
        return c;
    }

    private PdfPCell accentCell(String text, Color bg) {
        PdfPCell c = new PdfPCell(new Phrase(text, F_ACCENT));
        c.setBackgroundColor(bg);
        c.setBorderColor(BORDER);
        c.setBorderWidth(0.5f);
        c.setPadding(4);
        return c;
    }

    private PdfPCell boolCell(Integer value, Color bg) {
        boolean yes = Integer.valueOf(1).equals(value);
        Font f = new Font(Font.HELVETICA, 7, Font.BOLD, yes ? SUCCESS : DANGER);
        PdfPCell c = new PdfPCell(new Phrase(yes ? "DA" : "NE", f));
        c.setBackgroundColor(bg);
        c.setBorderColor(BORDER);
        c.setBorderWidth(0.5f);
        c.setPadding(4);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        return c;
    }

    private <T> long distinct(List<T> list, java.util.function.Function<T, String> fn) {
        return list.stream().map(fn).filter(v -> v != null).distinct().count();
    }

    private <T> long countWhere1(List<T> list, java.util.function.Function<T, Integer> fn) {
        return list.stream().filter(item -> Integer.valueOf(1).equals(fn.apply(item))).count();
    }

    private <T> Map<String, Long> groupBy(List<T> list, java.util.function.Function<T, String> fn) {
        return list.stream().filter(item -> fn.apply(item) != null)
                .collect(Collectors.groupingBy(fn, Collectors.counting()));
    }

    private String resolve(String id, Map<String, String> names) {
        if (id == null) return "—";
        return names.getOrDefault(id, id);
    }

    private String[] labels(String... s) { return s; }
    private long[] values(long... v) { return v; }

}
