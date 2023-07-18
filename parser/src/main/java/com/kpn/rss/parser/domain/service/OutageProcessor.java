package com.kpn.rss.parser.domain.service;

import com.kpn.rss.parser.domain.model.Outage;
import com.kpn.rss.parser.domain.model.OutageStatus;
import com.kpn.rss.parser.domain.model.inbound.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("ReturnOfNull")
@Slf4j
@Service
public class OutageProcessor {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Pattern START_DATE_PATTERN = Pattern.compile("Starttijd: (\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2})");
    private static final Pattern END_DATE_PATTERN = Pattern.compile("Eindtijd: ((\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2})|(?i)onbekend)");
    private static final String UNKNOWN_DATE = "onbekend";

    public Outage createOutageFromItem(final Item item) {
        final String description = item.description();

        final LocalDateTime startDate = this.extractDate(description, START_DATE_PATTERN);
        final LocalDateTime endDate = this.extractDate(description, END_DATE_PATTERN);

        return Outage.builder()
                .title(item.title())
                .postalCodes(item.postalCodes())
                .description(description)
                .startDate(this.formatDate(startDate))
                .endDate(this.formatDate(endDate))
                .status(this.determineStatus(startDate, endDate))
                .build();
    }

    private LocalDateTime extractDate(final String description, final Pattern datePattern) {
        final Matcher matcher = datePattern.matcher(description);
        if (matcher.find()) {
            return parseDate(matcher.group(1));
        }
        return null;
    }

    private static LocalDateTime parseDate(final String dateString) {
        if (UNKNOWN_DATE.equalsIgnoreCase(dateString)) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateString, FORMATTER);
        } catch (final DateTimeParseException e) {
            log.error("Invalid date string {} ", dateString, e);
            return null;
        }
    }

    private String formatDate(final LocalDateTime dateTime) {
        return Optional.ofNullable(dateTime).map(FORMATTER::format).orElse(UNKNOWN_DATE);
    }

    private OutageStatus determineStatus(final LocalDateTime startDate, final LocalDateTime endDate) {
        final LocalDateTime now = LocalDateTime.now();
        if (this.isCurrentOutage(endDate, now)) {
            return OutageStatus.CURRENT;
        }
        if (this.isPlannedOutage(startDate, now)) {
            return OutageStatus.PLANNED;
        }
        return OutageStatus.RESOLVED;
    }

    private boolean isCurrentOutage(final LocalDateTime endDate, final LocalDateTime now) {
        return endDate == null || endDate.isAfter(now);
    }

    private boolean isPlannedOutage(final LocalDateTime startDate, final LocalDateTime now) {
        return startDate != null && startDate.isAfter(now);
    }
}

