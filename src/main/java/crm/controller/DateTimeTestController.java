package crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * DateTimeTestController - Cloud-ready date/time handling
 * 
 * This controller demonstrates timezone-agnostic date/time handling for cloud environments.
 * All date/time operations use UTC to ensure consistency across distributed cloud deployments.
 * 
 * For scheduled operations in cloud environments, consider using:
 * - Azure Service Bus Scheduled Messages for distributed task scheduling
 * - Azure Functions with Timer Triggers for cron-like scheduling
 * - Spring @Scheduled with externalized cron expressions
 */
@Controller
@RequestMapping("/date")
public class DateTimeTestController {

    // UTC Zone ID for timezone-agnostic operations
    private static final ZoneId UTC_ZONE = ZoneOffset.UTC;

    @GetMapping("/test")
    public String dateTimeTest(Model model) {
        // Use Instant for timezone-agnostic timestamp
        Instant now = Instant.now();
        
        // Convert to UTC-based LocalDateTime and LocalDate for display
        // This ensures consistent behavior across all cloud regions and containers
        LocalDateTime utcDateTime = LocalDateTime.ofInstant(now, UTC_ZONE);
        LocalDate utcDate = LocalDate.ofInstant(now, UTC_ZONE);
        
        // Legacy Date object - converted from Instant for backward compatibility
        Date standardDate = Date.from(now);
        
        model.addAttribute("standardDate", standardDate);
        model.addAttribute("localDateTime", utcDateTime);
        model.addAttribute("localDate", utcDate);
        model.addAttribute("timestamp", now);
        model.addAttribute("timezone", "UTC");
        
        return "date/test";
    }

}
