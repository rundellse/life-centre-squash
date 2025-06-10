package org.rundellse.squashleague.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.rundellse.squashleague.model.Player;
import org.rundellse.squashleague.persistence.SeasonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

@Service
public class PdfService {

    private static final Logger LOG = LoggerFactory.getLogger(PdfService.class.getName());

    @Autowired
    private SeasonRepository seasonRepository;

    @Autowired
    private TableService tableService;

    @Value("${squash.league.pdf.path:/pdf}")
    String pdfPath;


    public Resource getCurrentSeasonPdfResource() {
        Map<Integer, List<Player>> divisions = tableService.getCurrentDivisions();

        String path = assembleSeasonPdfFile(divisions);
        File filePath = new File(path);
        Resource resource;
        try {
            LOG.trace("Attempting to create resource for path: {}", path);
            resource = new UrlResource(filePath.toURI());
        } catch (MalformedURLException e) {
            LOG.error("URL exception while attempting to assemble PDF. Attempted Path: {}", path, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return resource;
    }

    public String assembleSeasonPdfFile(Map<Integer, List<Player>> divisions) {
        try (PDDocument pdDocument = new PDDocument()) {
            PDPage pdPage = new PDPage();
            pdDocument.addPage(pdPage);

            PDPageContentStream contentStream = new PDPageContentStream(pdDocument, pdPage);
            PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            float fontSize = 12;
            float margin = 40;
            float yStart = pdPage.getMediaBox().getHeight() - margin;
            float tableWidth = pdPage.getMediaBox().getWidth() - 2 * margin;
            float yPosition = yStart;

            // Table headers
            String[] headers = {"Letter", "Name", "Phone", "Email"};
            float[] colWidths = {50, 150, 120, 180};

            contentStream.setFont(font, fontSize);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.setLeading(20);

            for (int divisionNum : divisions.keySet()) {
                // Title
                contentStream.setFont(font, 16);
                contentStream.showText("Division " + divisionNum + " Players");
                contentStream.newLine();
                contentStream.setFont(font, fontSize);

                // Header row
                for (int i = 0; i < headers.length; i++) {
                    contentStream.showText(headers[i]);
                    contentStream.newLineAtOffset(colWidths[i], 0);
                }
                // Move to next line after headers
                contentStream.newLineAtOffset(-1 * (colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3]), -20);

                // Player rows
                char letter = 'A';
                for (Player player : divisions.get(divisionNum)) {
                    String[] row = {
                            String.valueOf(letter++),
                            player.getName(),
                            player.getPhoneNumber(),
                            player.getEmail()
                    };
                    for (int i = 0; i < row.length; i++) {
                        contentStream.showText(row[i] != null ? row[i] : "");
                        contentStream.newLineAtOffset(colWidths[i], 0);
                    }
                    // Move to next line after row
                    contentStream.newLineAtOffset(-1 * (colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3]), -20);
                }
            }
            contentStream.endText();
            contentStream.close();

            System.out.println("pdfPath: " + pdfPath);
            String filePath = pdfPath + "/_players.pdf";
            System.out.println("filePath: " + filePath);
            pdDocument.save(filePath);

            LOG.debug("PDF generated with file name: {}", filePath);
            return filePath;
        } catch (IOException e) {
            LOG.error("Exception while attempting to assemble PDF", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
