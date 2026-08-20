package crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Controller
@RequestMapping("/date")
public class DateTimeTestController {

    @GetMapping("/test")
    public String dateTimeTest(Model model) {
        // Migrated from java.util.Date to java.time.Instant for UTC-based time handling
        model.addAttribute("standardDate", Instant.now());
        // Using UTC timezone explicitly to avoid timezone inconsistencies in cloud environments
        model.addAttribute("localDateTime", LocalDateTime.now(ZoneOffset.UTC));
        model.addAttribute("localDate", LocalDate.now(ZoneOffset.UTC));
        model.addAttribute("timestamp", Instant.now());
        return "date/test";
    }

}
