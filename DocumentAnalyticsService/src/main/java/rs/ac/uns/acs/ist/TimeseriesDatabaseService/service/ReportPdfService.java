package rs.ac.uns.acs.ist.TimeseriesDatabaseService.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.ist.TimeseriesDatabaseService.model.DocumentAccess;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ReportPdfService {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss").withZone(ZoneId.of("Europe/Belgrade"));

    private static final Font TITLE_FONT  = new Font(Font.HELVETICA, 16, Font.BOLD,  new Color(220, 220, 220));
    private static final Font HEADER_FONT = new Font(Font.HELVETICA,  9, Font.BOLD,  new Color(180, 180, 180));
    private static final Font CELL_FONT   = new Font(Font.HELVETICA,  8, Font.NORMAL, new Color(200, 200, 200));
    private static final Font LABEL_FONT  = new Font(Font.HELVETICA,  9, Font.BOLD,  new Color(150, 150, 150));
    private static final Font VALUE_FONT  = new Font(Font.HELVETICA, 20, Font.BOLD,  new Color(230, 230, 230));
    private static final Font SECTION_FONT= new Font(Font.HELVETICA, 10, Font.BOLD,  new Color(160, 160, 160));

    private static final Color BG_PAGE    = new Color(18,  18,  18);
    private static final Color BG_TABLE   = new Color(30,  30,  30);
    private static final Color BG_HEADER  = new Color(40,  40,  40);
    private static final Color BG_ROW_ALT = new Color(25,  25,  25);
    private static final Color BORDER_CLR = new Color(55,  55,  55);
    private static final Color ACCENT     = new Color(99, 102, 241);

    public byte[] generateOverallReport(List<DocumentAccess> accesses, Instant from, Instant to,
                                        Map<String, String> docNames, Map<String, String> userNames) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 30, 30, 40, 30);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new PageFooter());
            doc.open();

            addBackground(writer, doc);
            addTitle(doc, "Izveštaj pristupa dokumentima", from, to);

            long uniqueUsers = accesses.stream().map(DocumentAccess::getUser_id).filter(u -> u != null).distinct().count();
            long uniqueDocs  = accesses.stream().map(DocumentAccess::getDocument_id).filter(d -> d != null).distinct().count();
            addStatCards(doc, writer,
                    new String[]{"Ukupno pristupa", "Aktivnih korisnika", "Dokumenata pregledano"},
                    new long[]{accesses.size(), uniqueUsers, uniqueDocs});

            doc.add(Chunk.NEWLINE);
            addSectionTitle(doc, "Detalji pristupa");
            addAccessTable(doc, accesses, docNames, userNames, true);

            doc.add(Chunk.NEWLINE);
            addTopSection(doc, accesses, docNames, userNames);

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    public byte[] generateDocumentReport(List<DocumentAccess> accesses, String documentId, String documentName,
                                          Instant from, Instant to, Map<String, String> userNames) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 30, 30, 40, 30);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new PageFooter());
            doc.open();

            addBackground(writer, doc);
            addTitle(doc, "Izveštaj po dokumentu: " + documentName, from, to);

            long uniqueUsers = accesses.stream().map(DocumentAccess::getUser_id).filter(u -> u != null).distinct().count();
            addStatCards(doc, writer,
                    new String[]{"Ukupno pristupa", "Jedinstvenih korisnika"},
                    new long[]{accesses.size(), uniqueUsers});

            doc.add(Chunk.NEWLINE);
            addSectionTitle(doc, "Detalji pristupa");
            addAccessTable(doc, accesses, Map.of(), userNames, false);

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    public byte[] generateUserReport(List<DocumentAccess> accesses, String userId, String userName,
                                      Instant from, Instant to, Map<String, String> docNames) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 30, 30, 40, 30);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new PageFooter());
            doc.open();

            addBackground(writer, doc);
            addTitle(doc, "Izveštaj po korisniku: " + userName, from, to);

            long uniqueDocs = accesses.stream().map(DocumentAccess::getDocument_id).filter(d -> d != null).distinct().count();
            addStatCards(doc, writer,
                    new String[]{"Ukupno pristupa", "Jedinstvenih dokumenata"},
                    new long[]{accesses.size(), uniqueDocs});

            doc.add(Chunk.NEWLINE);
            addSectionTitle(doc, "Detalji pristupa");
            addAccessTable(doc, accesses, docNames, Map.of(), true);

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void addBackground(PdfWriter writer, Document doc) {
        PdfContentByte canvas = writer.getDirectContentUnder();
        canvas.setColorFill(BG_PAGE);
        canvas.rectangle(0, 0, doc.getPageSize().getWidth(), doc.getPageSize().getHeight());
        canvas.fill();
    }

    private void addTitle(Document doc, String title, Instant from, Instant to) throws DocumentException {
        Paragraph p = new Paragraph(title, TITLE_FONT);
        p.setSpacingAfter(4);
        doc.add(p);

        String range = FMT.format(from) + "  —  " + FMT.format(to);
        Paragraph sub = new Paragraph(range, new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(120, 120, 120)));
        sub.setSpacingAfter(16);
        doc.add(sub);
    }

    private void addStatCards(Document doc, PdfWriter writer, String[] labels, long[] values) throws DocumentException {
        int cols = labels.length;
        PdfPTable table = new PdfPTable(cols);
        table.setWidthPercentage(100);
        table.setSpacingAfter(8);

        for (int i = 0; i < cols; i++) {
            PdfPCell cell = new PdfPCell();
            cell.setBackgroundColor(BG_TABLE);
            cell.setBorderColor(BORDER_CLR);
            cell.setBorderWidth(1f);
            cell.setPadding(12);

            Paragraph label = new Paragraph(labels[i], LABEL_FONT);
            label.setSpacingAfter(4);
            Paragraph value = new Paragraph(String.valueOf(values[i]), VALUE_FONT);

            cell.addElement(label);
            cell.addElement(value);
            table.addCell(cell);
        }
        doc.add(table);
    }

    private void addSectionTitle(Document doc, String title) throws DocumentException {
        Paragraph p = new Paragraph(title.toUpperCase(), SECTION_FONT);
        p.setSpacingBefore(4);
        p.setSpacingAfter(6);
        doc.add(p);
    }

    private void addAccessTable(Document doc, List<DocumentAccess> accesses,
                                 Map<String, String> docNames, Map<String, String> userNames,
                                 boolean showDoc) throws DocumentException {
        boolean showUser = !userNames.isEmpty() || accesses.stream().anyMatch(a -> a.getUser_id() != null);
        int cols = 2 + (showDoc ? 1 : 0) + (showUser ? 1 : 0);
        PdfPTable table = new PdfPTable(cols);
        table.setWidthPercentage(100);

        String[] headers = buildHeaders(showDoc, showUser);
        for (String h : headers) {
            PdfPCell hCell = new PdfPCell(new Phrase(h, HEADER_FONT));
            hCell.setBackgroundColor(BG_HEADER);
            hCell.setBorderColor(BORDER_CLR);
            hCell.setPadding(7);
            table.addCell(hCell);
        }

        boolean alt = false;
        for (DocumentAccess a : accesses) {
            Color rowBg = alt ? BG_ROW_ALT : BG_TABLE;
            alt = !alt;

            if (showUser) addCell(table, resolveUser(a.getUser_id(), userNames), rowBg);
            if (showDoc)  addCell(table, resolveDoc(a.getDocument_id(), docNames), rowBg);
            addCell(table, a.getCreated() != null ? FMT.format(a.getCreated()) : "—", rowBg);
            addAccentCell(table, a.getAction_type() != null ? a.getAction_type() : "—", rowBg);
        }

        doc.add(table);
    }

    private String[] buildHeaders(boolean showDoc, boolean showUser) {
        java.util.List<String> h = new java.util.ArrayList<>();
        if (showUser) h.add("Korisnik");
        if (showDoc)  h.add("Dokument");
        h.add("Datum i vreme");
        h.add("Tip akcije");
        return h.toArray(new String[0]);
    }

    private void addCell(PdfPTable table, String text, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, CELL_FONT));
        cell.setBackgroundColor(bg);
        cell.setBorderColor(BORDER_CLR);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addAccentCell(PdfPTable table, String text, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, 8, Font.BOLD, ACCENT)));
        cell.setBackgroundColor(bg);
        cell.setBorderColor(BORDER_CLR);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addTopSection(Document doc, List<DocumentAccess> accesses,
                                Map<String, String> docNames, Map<String, String> userNames) throws DocumentException {
        Map<String, Long> byDoc = accesses.stream()
                .filter(a -> a.getDocument_id() != null)
                .collect(java.util.stream.Collectors.groupingBy(DocumentAccess::getDocument_id, java.util.stream.Collectors.counting()));
        Map<String, Long> byUser = accesses.stream()
                .filter(a -> a.getUser_id() != null)
                .collect(java.util.stream.Collectors.groupingBy(DocumentAccess::getUser_id, java.util.stream.Collectors.counting()));

        PdfPTable wrapper = new PdfPTable(2);
        wrapper.setWidthPercentage(100);

        wrapper.addCell(buildTopCell("Najaktivniji dokumenti", byDoc, docNames, true));
        wrapper.addCell(buildTopCell("Najaktivniji korisnici", byUser, userNames, false));

        doc.add(wrapper);
    }

    private PdfPCell buildTopCell(String title, Map<String, Long> counts,
                                   Map<String, String> names, boolean isDoc) throws DocumentException {
        PdfPCell outer = new PdfPCell();
        outer.setBorder(Rectangle.NO_BORDER);
        outer.setPaddingRight(isDoc ? 6 : 0);
        outer.setPaddingLeft(isDoc ? 0 : 6);

        PdfPTable inner = new PdfPTable(2);
        inner.setWidthPercentage(100);
        inner.setWidths(new float[]{4f, 1f});

        PdfPCell titleCell = new PdfPCell(new Phrase(title.toUpperCase(), SECTION_FONT));
        titleCell.setColspan(2);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setPaddingBottom(6);
        inner.addCell(titleCell);

        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> {
                    String label = names.getOrDefault(e.getKey(), e.getKey());
                    PdfPCell nameCell = new PdfPCell(new Phrase(label, CELL_FONT));
                    nameCell.setBackgroundColor(BG_TABLE);
                    nameCell.setBorderColor(BORDER_CLR);
                    nameCell.setPadding(5);

                    PdfPCell countCell = new PdfPCell(new Phrase(String.valueOf(e.getValue()),
                            new Font(Font.HELVETICA, 8, Font.BOLD, ACCENT)));
                    countCell.setBackgroundColor(BG_TABLE);
                    countCell.setBorderColor(BORDER_CLR);
                    countCell.setPadding(5);
                    countCell.setHorizontalAlignment(Element.ALIGN_CENTER);

                    inner.addCell(nameCell);
                    inner.addCell(countCell);
                });

        outer.addElement(inner);
        return outer;
    }

    private String resolveDoc(String id, Map<String, String> docNames) {
        if (id == null) return "—";
        return docNames.getOrDefault(id, id);
    }

    private String resolveUser(String id, Map<String, String> userNames) {
        if (id == null) return "—";
        return userNames.getOrDefault(id, id);
    }

    private static class PageFooter extends PdfPageEventHelper {
        private static final Font FOOTER_FONT = new Font(Font.HELVETICA, 7, Font.NORMAL, new Color(80, 80, 80));

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            String text = "Stranica " + writer.getPageNumber() +
                    "   •   Generisano: " + DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm")
                    .withZone(ZoneId.of("Europe/Belgrade")).format(Instant.now());
            Phrase footer = new Phrase(text, FOOTER_FONT);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer,
                    document.getPageSize().getWidth() / 2, 15, 0);
        }
    }
}
