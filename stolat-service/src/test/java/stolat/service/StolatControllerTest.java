package stolat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import stolat.dao.AlbumBirthdayDao;
import stolat.model.Album;
import stolat.model.AlbumBirthday;
import stolat.model.BirthdayAlbums;

import java.time.*;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(MockitoExtension.class)
class StolatControllerTest {

    private static final LocalDateTime FIXED_DATE_TIME =
            LocalDateTime.of(2020, 3, 28, 12, 21);
    private static final Instant FIXED_INSTANT = FIXED_DATE_TIME.toInstant(ZoneOffset.UTC);

    private MockMvc mvc;

    private JacksonTester<BirthdayAlbums> birthdayAlbumsJacksonTester;

    @Mock
    private Clock mockClock;

    @Mock
    private AlbumBirthdayDao mockAlbumBirthdayDao;

    @InjectMocks
    private StolatController controller;

    @BeforeEach
    void setUp() {
        given(mockClock.instant()).willReturn(FIXED_INSTANT);

        var objectMapper = new ObjectMapper();
        JacksonTester.initFields(this, objectMapper);
        var messageConverter = new MappingJackson2HttpMessageConverter(objectMapper);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(messageConverter)
                .build();
    }

    @Test
    void shouldGetBirthdayAlbumsBetweenFromDateAndToDateWhenBothDatesArePresent() throws Exception {

        var firstBirthday =
                new AlbumBirthday(
                        new Album(
                                UUID.randomUUID(), "Some Album",
                                UUID.randomUUID(), "Some Artist"),
                        2000, 3, 28);
        var secondBirthday =
                new AlbumBirthday(
                        new Album(
                                UUID.randomUUID(), "Some Other Album",
                                UUID.randomUUID(), "Some Other Artists"),
                        1983, 4, 2);
        var from = MonthDay.of(3, 25);
        var to = MonthDay.of(4, 5);
        var albumBirthdays = List.of(firstBirthday, secondBirthday);
        var expected = new BirthdayAlbums(from, to, albumBirthdays);

        given(mockAlbumBirthdayDao.getAlbumBirthdays(from, to))
                .willReturn(albumBirthdays);

        MockHttpServletResponse response =
                mvc.perform(get("/stolat/birthdays")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        var contentAsString = response.getContentAsString();
        var json = birthdayAlbumsJacksonTester.write(expected).getJson();
        assertThat(contentAsString)
                .isEqualTo(json);
    }

    @Test
    void shouldGetBirthdayAlbumsBetweenTodayAndToDateWhenFromDateIsMissing() throws Exception {

        var firstBirthday =
                new AlbumBirthday(
                        new Album(
                                UUID.randomUUID(), "Some Album",
                                UUID.randomUUID(), "Some Artist"),
                        2000, 3, 28);
        var today = MonthDay.of(
                FIXED_DATE_TIME.get(ChronoField.MONTH_OF_YEAR),
                FIXED_DATE_TIME.get(ChronoField.DAY_OF_MONTH));
        var to = MonthDay.of(4, 5);
        var albumBirthdays = List.of(firstBirthday);
        var expected = new BirthdayAlbums(today, to, albumBirthdays);

        given(mockAlbumBirthdayDao.getAlbumBirthdays(today, to))
                .willReturn(albumBirthdays);

        MockHttpServletResponse response =
                mvc.perform(get("/stolat/birthdays")
                        .param("to", to.toString())
                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        var contentAsString = response.getContentAsString();
        var json = birthdayAlbumsJacksonTester.write(expected).getJson();
        assertThat(contentAsString)
                .isEqualTo(json);
    }

    @Test
    void shouldGetBirthdayAlbumsBetweenFromDateAndTodayWhenToDateIsMissing() throws Exception {

        var firstBirthday =
                new AlbumBirthday(
                        new Album(
                                UUID.randomUUID(), "Some Album",
                                UUID.randomUUID(), "Some Artist"),
                        2000, 3, 28);
        var from = MonthDay.of(2, 20);
        var today = MonthDay.of(
                FIXED_DATE_TIME.get(ChronoField.MONTH_OF_YEAR),
                FIXED_DATE_TIME.get(ChronoField.DAY_OF_MONTH));
        var albumBirthdays = List.of(firstBirthday);
        var expected = new BirthdayAlbums(from, today, albumBirthdays);

        given(mockAlbumBirthdayDao.getAlbumBirthdays(from, today))
                .willReturn(albumBirthdays);

        MockHttpServletResponse response =
                mvc.perform(get("/stolat/birthdays")
                        .param("from", from.toString())
                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        var contentAsString = response.getContentAsString();
        var json = birthdayAlbumsJacksonTester.write(expected).getJson();
        assertThat(contentAsString)
                .isEqualTo(json);
    }

    @Test
    void shouldGetBirthdayAlbumsOfTodayWhenBothDatesAreMissing() throws Exception {
        var firstBirthday =
                new AlbumBirthday(
                        new Album(
                                UUID.randomUUID(), "Some Album",
                                UUID.randomUUID(), "Some Artist"),
                        2000, 3, 28);
        var today = MonthDay.of(
                FIXED_DATE_TIME.get(ChronoField.MONTH_OF_YEAR),
                FIXED_DATE_TIME.get(ChronoField.DAY_OF_MONTH));
        var albumBirthdays = List.of(firstBirthday);
        var expected = new BirthdayAlbums(today, today, albumBirthdays);

        given(mockAlbumBirthdayDao.getAlbumBirthdays(today, today))
                .willReturn(albumBirthdays);

        MockHttpServletResponse response =
                mvc.perform(get("/stolat/birthdays")
                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        var contentAsString = response.getContentAsString();
        var json = birthdayAlbumsJacksonTester.write(expected).getJson();
        assertThat(contentAsString)
                .isEqualTo(json);
    }
}