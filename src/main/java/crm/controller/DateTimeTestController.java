package crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Cloud-ready date/time controller that uses java.time API and standardizes on UTC.
 * This eliminates timezone and clock synchronization issues in distributed cloud environments.
 */
@Controller
@RequestMapping("/date")
public class DateTimeTestController {

    // Use UTC as the standard timezone for all cloud operations
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    
    // Use Clock abstraction for testability and consistency
    private final Clock clock;

    public DateTimeTestController() {
        // Default to UTC clock for cloud environments
        this.clock = Clock.systemUTC();
    }

    // Constructor for testing with custom clock
    public DateTimeTestController(Clock clock) {
        this.clock = clock;
    }

    @GetMapping("/test")
    public String dateTimeTest(Model model) {
        // Use Instant for UTC timestamps (recommended for cloud environments)
        Instant currentInstant = Instant.now(clock);
        
        // Use ZonedDateTime with explicit UTC timezone for clarity
        ZonedDateTime utcDateTime = ZonedDateTime.now(clock);
        
        // LocalDateTime in UTC context
        LocalDateTime utcLocalDateTime = LocalDateTime.now(clock);
        
        // LocalDate in UTC context
        LocalDate utcLocalDate = LocalDate.now(clock);
        
        // Add all time representations to the model
        model.addAttribute("timestamp", currentInstant);
        model.addAttribute("utcDateTime", utcDateTime);
        model.addAttribute("localDateTime", utcLocalDateTime);
        model.addAttribute("localDate", utcLocalDate);
        
        // Add timezone information for display
        model.addAttribute("timezone", UTC_ZONE.getId());
        model.addAttribute("epochMillis", currentInstant.toEpochMilli());
        
        // For backward compatibility, provide ISO-8601 formatted strings
        model.addAttribute("isoDateTime", currentInstant.toString());
        
        return "date/test";
    }

    /**
     * Helper method to convert Instant to ZonedDateTime in a specific timezone.
     * Useful for displaying times in user's local timezone while storing in UTC.
     * 
     * @param instant The UTC instant
     * @param zoneId The target timezone
     * @return ZonedDateTime in the specified timezone
     */
    public ZonedDateTime toZonedDateTime(Instant instant, ZoneId zoneId) {
        return instant.atZone(zoneId);
    }

    /**
     * Helper method to get current time in a specific timezone.
     * Always uses UTC as the source of truth.
     * 
     * @param zoneId The target timezone
     * @return ZonedDateTime in the specified timezone
     */
    public ZonedDateTime getCurrentTimeInZone(ZoneId zoneId) {
        return Instant.now(clock).atZone(zoneId);
    }
}
