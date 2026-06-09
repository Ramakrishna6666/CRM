package crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Cloud-native date/time controller using java.time API.
 * All timestamps are standardized to UTC to ensure consistency across distributed cloud environments.
 */
@Controller
@RequestMapping("/date")
public class DateTimeTestController {

    private final Clock clock;

    /**
     * Constructor with Clock injection for testability and cloud compatibility.
     * Uses UTC timezone to avoid timezone inconsistencies in distributed systems.
     */
    public DateTimeTestController() {
        // Use UTC clock for cloud-native consistency
        this.clock = Clock.systemUTC();
    }

    /**
     * Constructor for testing with custom clock.
     * 
     * @param clock Custom clock instance
     */
    public DateTimeTestController(Clock clock) {
        this.clock = clock;
    }

    @GetMapping("/test")
    public String dateTimeTest(Model model) {
        // Use java.time API with UTC standardization for cloud environments
        Instant now = Instant.now(clock);
        
        // All timestamps in UTC for consistency across regions and containers
        model.addAttribute("timestamp", now);
        model.addAttribute("utcDateTime", ZonedDateTime.ofInstant(now, ZoneOffset.UTC));
        model.addAttribute("localDateTime", LocalDateTime.ofInstant(now, ZoneOffset.UTC));
        model.addAttribute("localDate", LocalDate.ofInstant(now, ZoneOffset.UTC));
        
        // Include ISO-8601 formatted strings for API responses and logging
        model.addAttribute("iso8601Timestamp", now.toString());
        model.addAttribute("epochMillis", now.toEpochMilli());
        
        return "date/test";
    }

}
