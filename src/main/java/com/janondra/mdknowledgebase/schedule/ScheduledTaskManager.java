package com.janondra.mdknowledgebase.schedule;

import com.janondra.mdknowledgebase.document.model.DailyMailTarget;
import com.janondra.mdknowledgebase.document.model.DocumentContent;
import com.janondra.mdknowledgebase.document.service.DocumentEnrichmentService;
import com.janondra.mdknowledgebase.document.service.DocumentService;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.MINUTES;

@Component
public class ScheduledTaskManager {

    private final DocumentService documentService;
    private final DocumentEnrichmentService documentEnrichmentService;
    private final EmailService emailService;
    private final Parser mdParser;
    private final HtmlRenderer htmlRenderer;

    public ScheduledTaskManager(
        DocumentService documentService,
        DocumentEnrichmentService documentEnrichmentService,
        EmailService emailService,
        Parser mdParser,
        HtmlRenderer htmlRenderer
    ) {
        this.documentService = documentService;
        this.documentEnrichmentService = documentEnrichmentService;
        this.emailService = emailService;
        this.mdParser = mdParser;
        this.htmlRenderer = htmlRenderer;
    }

    @Scheduled(cron = "0 5 * * * *")
    public void generateAndSaveMissingDocumentQuestions() {
        for (DocumentContent documentContent : documentService.getDocumentsWithoutQuestions()) {
            documentEnrichmentService.generateAndSaveQuestions(documentContent.id(), documentContent.content());
        }
    }

    @Scheduled(cron = "5 0,15,30,45 * * * *")
    public void generateAndSendDailyMailToUsers() {
        OffsetDateTime now = OffsetDateTime.now(UTC).truncatedTo(MINUTES);

        List<DailyMailTarget> dailyMailTargets = documentService.getDailyMailTargets(now);

        for (DailyMailTarget target : dailyMailTargets) {
            StringBuilder sb = new StringBuilder();

            sb.append("# Heutiges Thema: **");
            sb.append(target.fileName());
            sb.append("**\n");

            for (String question : target.questions()) {
                sb.append("## ");
                sb.append(question);
                sb.append("\n");
            }

            sb.append("---\n");

            sb.append(target.content());

            String html = htmlRenderer.render(
                mdParser.parse(sb.toString())
            );

            emailService.sendEmailWithHtml(
                target.email(),
                "Fragen des Tages",
                html
            );
        }
    }

}
