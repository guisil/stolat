package stolat.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import stolat.dao.AlbumBirthdayDao;
import stolat.model.AlbumBirthday;
import stolat.model.BirthdayAlbums;

import java.time.*;
import java.util.List;

@RestController
@RequestMapping("/stolat")
@AllArgsConstructor
@Slf4j
public class StolatController {

    private final Clock clock;
    private final AlbumBirthdayDao albumBirthdayDao;

    @GetMapping("/birthdays")
    public BirthdayAlbums birthdayAlbums(
            @RequestParam(value = "from", required = false) MonthDay from,
            @RequestParam(value = "to", required = false) MonthDay to) {

        log.info("Received request for birthday albums between '{}' and '{}'", from, to);

        LocalDateTime now = LocalDateTime.ofInstant(Instant.now(clock), ZoneOffset.UTC);
        MonthDay adjustedFrom = from;
        MonthDay adjustedTo = to;
        if (adjustedFrom == null) {
            adjustedFrom = MonthDay.of(now.getMonth(), now.getDayOfMonth());
        }
        if (adjustedTo == null) {
            adjustedTo = MonthDay.of(now.getMonth(), now.getDayOfMonth());
        }

        List<AlbumBirthday> albumBirthdays = albumBirthdayDao.getAlbumBirthdays(adjustedFrom, adjustedTo);

        log.info("Found {} albums with birthday between {} and {}", albumBirthdays.size(), adjustedFrom, adjustedTo);

        return new BirthdayAlbums(adjustedFrom, adjustedTo, albumBirthdays);
    }
}
