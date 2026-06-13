package crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Cloud-native date/time controller using java.time API standardized on UTC
 * to eliminate timezone inconsistencies in distributed cloud environments.
 */
@Controller
@RequestMapping("/date")
public class DateTimeTestController {

    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    @GetMapping("/test")
    public String dateTimeTest(Model model) {
        // Use Instant for UTC timestamps (cloud-native best practice)
        Instant currentInstant = Instant.now();
        
        // Use ZonedDateTime with explicit UTC timezone for cloud consistency
        ZonedDateTime utcDateTime = ZonedDateTime.now(UTC_ZONE);
        
        // LocalDateTime and LocalDate for timezone-agnostic operations
        LocalDateTime localDateTime = LocalDateTime.now(UTC_ZONE);
        LocalDate localDate = LocalDate.now(UTC_ZONE);
        
        // Add cloud-native time attributes (all standardized on UTC)
        model.addAttribute("timestamp", currentInstant);
        model.addAttribute("utcDateTime", utcDateTime);
        model.addAttribute("localDateTime", localDateTime);
        model.addAttribute("localDate", localDate);
        
        // Add formatted timestamps for display
        model.addAttribute("timestampFormatted", currentInstant.toString());
        model.addAttribute("utcDateTimeFormatted", utcDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        
        // Add epoch milliseconds for inter-service communication
        model.addAttribute("epochMillis", currentInstant.toEpochMilli());
        
        return "date/test";
    }

}
