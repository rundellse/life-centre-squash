package org.rundellse.squashleague.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.rundellse.squashleague.model.Player;
import org.rundellse.squashleague.model.Season;
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

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class PdfService {

    private static final Logger LOG = LoggerFactory.getLogger(PdfService.class.getName());

    public static final PDFont BASE_FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    public static final PDFont BOLD_FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    public static final float FONT_SIZE = 8;
    public static final float MARGIN = 40;

    public static final String SIGN_UP_MESSAGE = "If you would like to join the squash leagues please add your name, contact details and ability level/division below:";
    public static final String SCORING_TEXT_1 = "SCORING: 3-0 6pts - 1pt | 3-1 5pts - 2pts | 3-2 4pts - 3pts | Unfinished Games 1 point each + 1 point for each game won.";
    public static final String SCORING_TEXT_2 = "If a player fails to play any of their league matches in a given cycle their name will appear in red for the following league cycle.";
    public static final String SCORING_TEXT_3 = "If a player in red fails to play any of their league matches they will be removed from the leagues.";
    public static final String END_DATE_TEXT = "ALL MATCHES TO BE COMPLETED BY %s";


    @Autowired
    private SeasonRepository seasonRepository;

    @Autowired
    private TableService tableService;

    @Value("${squash.league.pdf.path:/pdf}")
    String pdfPath;


    public Resource getCurrentSeasonPdfResource() {
        Map<Integer, List<Player>> divisions = tableService.getCurrentDivisions();
        Season season = seasonRepository.findSeasonForDate(LocalDate.now());
        if (season == null) {
            LOG.error("Not able to find season for Current date, please ensure a Season has been created with an end-date in the future.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        String path = assembleSeasonPdfFile(divisions, season.getEndDate().format(DateTimeFormatter.ofPattern("EEEE, d MMMM")).toUpperCase());

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

    private String assembleSeasonPdfFile(Map<Integer, List<Player>> divisions, String endDate) {
        try (PDDocument pdDocument = new PDDocument()) {

            // Draw the PDF, yPosition keeps track of how far down the page the drawing has occurred.
            PDPage pdPage = new PDPage(PDRectangle.A4);
            pdDocument.addPage(pdPage);
            PDPageContentStream contentStream = new PDPageContentStream(pdDocument, pdPage);
            float pageHeight = pdPage.getMediaBox().getHeight();
            float tableWidth = pdPage.getMediaBox().getWidth() - 2 * MARGIN;
            float yPosition = pageHeight - (MARGIN * 1.5f);

            contentStream.setFont(BASE_FONT, FONT_SIZE);

            yPosition = drawSheetIntro(endDate, contentStream, yPosition, tableWidth, pdPage);
            contentStream = drawDivisionTables(divisions, yPosition, pdPage, pdDocument, contentStream, pageHeight);
            contentStream.close();

            LOG.trace("pdfPath: {}", pdfPath);
            String filePath = pdfPath + "/_players.pdf";
            LOG.trace("filePath: {}", filePath);
            pdDocument.save(filePath);

            LOG.debug("PDF generated with file name: {}", filePath);
            return filePath;
        } catch (IOException e) {
            LOG.error("Exception while attempting to assemble PDF", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static PDPageContentStream drawDivisionTables(Map<Integer, List<Player>> divisions, float yPosition, PDPage pdPage, PDDocument pdDocument, PDPageContentStream contentStream, float pageHeight) throws IOException {
        // Draw division tables
        yPosition -= 15;
        for (int divisionNum : divisions.keySet()) {
            List<Player> division = divisions.get(divisionNum);
            float divisionTableHeightWithTitle = getHeightOfTable(divisions.size()) + 70;

            // If division table would go beyond end of page, create a new page and bind new content stream to it
            if (yPosition - divisionTableHeightWithTitle < (0 + MARGIN)) {
                pdPage = new PDPage(PDRectangle.A4);
                pdDocument.addPage(pdPage);
                contentStream.close();
                contentStream = new PDPageContentStream(pdDocument, pdPage);
                contentStream.setFont(BASE_FONT, FONT_SIZE);
                yPosition = pageHeight - MARGIN;
            }

            drawDivisionTable(pdPage, division, divisionNum, contentStream, yPosition);
            yPosition -= divisionTableHeightWithTitle;
        }
        return contentStream;
    }

    private static float drawSheetIntro(String endDate, PDPageContentStream contentStream, float yPosition, float tableWidth, PDPage pdPage) throws IOException {
        // Top text
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(SIGN_UP_MESSAGE);
        contentStream.endText();
        yPosition -= 20;

        // Top / Sign-up box
        int topBoxHeight = 260;
        contentStream.addRect(MARGIN, yPosition - topBoxHeight, tableWidth, topBoxHeight);
        contentStream.setStrokingColor(Color.BLACK);
        contentStream.stroke();
        yPosition -= topBoxHeight + 20;

        // Scoring text
        contentStream.setLeading(11);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(SCORING_TEXT_1);
        contentStream.newLine();
        contentStream.showText(SCORING_TEXT_2);
        contentStream.newLine();
        contentStream.showText(SCORING_TEXT_3);
        contentStream.endText();
        yPosition -= 35;

        // End-date text
        yPosition -= 35;
        contentStream.beginText();
        contentStream.setFont(BOLD_FONT, FONT_SIZE);
        String endDateText = String.format(END_DATE_TEXT, endDate);
        float endDateTextWidth = BOLD_FONT.getStringWidth(endDateText) / 1000 * FONT_SIZE;
        contentStream.setNonStrokingColor(Color.RED);
        contentStream.newLineAtOffset((pdPage.getMediaBox().getWidth() - endDateTextWidth) / 2, yPosition);
        contentStream.showText(endDateText);
        contentStream.setFont(BASE_FONT, FONT_SIZE);
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.endText();
        yPosition -= 15;
        return yPosition;
    }

    private static void drawDivisionTable(PDPage pdPage, List<Player> division, int divisionNum, PDPageContentStream contentStream, float yPosition) throws IOException {
        float tableWidth = pdPage.getMediaBox().getWidth() - (MARGIN * 2);
        float tableMidPoint = tableWidth / 100 * 50;
        float divisionTitlePadding = 10;
        int divisionSize = division.size();
        float gameColWidth = tableMidPoint / divisionSize;
        float[] playerDetailsColWidths = {
                // Should add up to 50, or 100 - midpoint %age above
                tableWidth / 100 * 3,
                tableWidth / 100 * 35,
                tableWidth / 100 * 12
        };

        drawTableLines(contentStream, yPosition, divisionTitlePadding, tableWidth, divisionSize, playerDetailsColWidths, gameColWidth);
        writeTableContent(contentStream, division, divisionNum, divisionSize, yPosition, tableMidPoint, gameColWidth, tableWidth, playerDetailsColWidths);
    }

    private static void drawTableLines(PDPageContentStream contentStream,
                                       float yPosition, float divisionTitlePadding, float tableWidth,
                                       int divisionSize, float[] playerDetailsColWidths, float gameColWidth) throws IOException {

        float lineWidth = 1.3f;
        tableWidth += lineWidth; //
        contentStream.setLineWidth(lineWidth);

        // Draw columns
        float xPosition = MARGIN + (lineWidth / 2);
        float tableBottomY = yPosition - getHeightOfTable(divisionSize);
        // I can't work out why there's manual changes required here, but that's what needed to line up lines to text.
        float lineTopYPosition = yPosition - 10;
        float lineBottomYPosition = tableBottomY - 8;
        contentStream.moveTo(xPosition, lineTopYPosition);
        contentStream.lineTo(xPosition, lineBottomYPosition);
        contentStream.stroke();

        // Letter | Player Details
        xPosition += playerDetailsColWidths[0];
        contentStream.moveTo(xPosition, lineTopYPosition);
        contentStream.lineTo(xPosition, lineBottomYPosition);
        contentStream.stroke();

        // Player Details | Games (skipping Phone number index)
        xPosition += playerDetailsColWidths[1] + playerDetailsColWidths[2];
        contentStream.moveTo(xPosition, lineTopYPosition);
        contentStream.lineTo(xPosition, lineBottomYPosition);
        contentStream.stroke();

        float gamCellHeight = 20;
        for (int i = 0; i < divisionSize; i++) {
            // Fill self-game rectangles
            contentStream.addRect(xPosition, (lineTopYPosition - 12 - (gamCellHeight * (i + 1))), gameColWidth, gamCellHeight);
            contentStream.fill();

            xPosition += gameColWidth;
            contentStream.moveTo(xPosition, lineTopYPosition);
            contentStream.lineTo(xPosition, lineBottomYPosition);
            contentStream.stroke();
        }

        // Draw rows
        yPosition -= divisionTitlePadding;
        contentStream.moveTo(MARGIN, yPosition);
        contentStream.lineTo(MARGIN + tableWidth, yPosition);
        contentStream.stroke();

        yPosition -= 12;
        contentStream.moveTo(MARGIN, yPosition);
        contentStream.lineTo(MARGIN + tableWidth, yPosition);
        contentStream.stroke();

        for (int i = 0; i < divisionSize; i++) {
            yPosition -= gamCellHeight;
            contentStream.moveTo(MARGIN, yPosition);
            contentStream.lineTo(MARGIN + tableWidth, yPosition);
            contentStream.stroke();
        }
    }

    private static void writeTableContent(PDPageContentStream contentStream,
                                          List<Player> division, int divisionNum, int divisionSize,
                                          float yPosition, float tableMidPoint,
                                          float gameColWidth, float tableWidth, float[] playerDetailsColWidths) throws IOException {
        contentStream.setFont(BASE_FONT, FONT_SIZE);
        contentStream.beginText();

        // Division Title
        contentStream.newLineAtOffset(MARGIN, yPosition);
        writeDivisionName(contentStream, divisionNum);

        // Header row
        int headerXOffset = 18;
        contentStream.newLineAtOffset(tableMidPoint + headerXOffset, -20);
        char headerChar = 'A';
        for (int i = 0; i < divisionSize; i++) {
            contentStream.showText(String.valueOf(headerChar++));
            contentStream.newLineAtOffset(gameColWidth, 0);
        }
        // Move to next line after headers
        float playerLineXOffset = 5;
        contentStream.newLineAtOffset(-tableWidth - headerXOffset + playerLineXOffset, -10);

        // Player rows
        char letter = 'A';
        for (Player player : division) {
            String[] row = {
                    String.valueOf(letter++),
                    player.getName(),
                    player.getPhoneNumber().substring(0, 5) + " " + player.getPhoneNumber().substring(5),
            };
            for (int i = 0; i < row.length; i++) {
                contentStream.setFont(BOLD_FONT, FONT_SIZE);
                contentStream.showText(row[i] != null ? row[i] : "");
                contentStream.newLineAtOffset(playerDetailsColWidths[i], 0);
            }
            contentStream.setFont(BASE_FONT, FONT_SIZE);
            contentStream.setNonStrokingColor(Color.BLUE);
            contentStream.newLineAtOffset(-1 * (playerDetailsColWidths[1] + playerDetailsColWidths[2]), -10);
            contentStream.showText(player.getEmail());
            contentStream.setNonStrokingColor(Color.BLACK);

            // Move to next line after row
            contentStream.newLineAtOffset(-1 * (playerDetailsColWidths[0]), -10);
        }
        contentStream.endText();
    }

    private static void writeDivisionName(PDPageContentStream contentStream, int divisionNum) throws IOException {
        contentStream.setFont(BOLD_FONT, FONT_SIZE);
        contentStream.setNonStrokingColor(Color.RED);

        if (divisionNum == 0) {
            contentStream.showText("PREMIER DIVISION");
        } else {
            contentStream.showText("DIVISION " + divisionNum);
        }

        contentStream.setFont(BASE_FONT, FONT_SIZE);
        contentStream.setNonStrokingColor(Color.BLACK);
    }

    private static float getHeightOfTable(int divisionSize) {
        return (divisionSize * 20) + 14;
    }
}
