package stolat.dao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.FileCopyUtils;
import stolat.model.AlbumBirthday;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.MonthDay;
import java.util.List;
import java.util.function.BiFunction;

import static stolat.dao.StolatDatabaseConstants.*;

@Profile("jdbc")
@Repository
@AllArgsConstructor
@Slf4j
public class JdbcAlbumBirthdayDao implements AlbumBirthdayDao {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void clearAlbumBirthdays() {
        log.info("Clearing intermediate album birthdays");
        jdbcTemplate.update("TRUNCATE TABLE " + BIRTHDAY_TABLE_INTERMEDIATE_FULL_NAME);
        log.info("Clearing album birthdays");
        jdbcTemplate.update("TRUNCATE TABLE " + BIRTHDAY_TABLE_FULL_NAME);
    }

    @Override
    public void populateAlbumBirthdays() {
        try {
            log.info("Populating intermediate album birthdays");
            runScript(DaoConstants.POPULATE_ALBUM_BIRTHDAY_INTERMEDIATE_SCRIPT);
            log.info("Populating album birthdays");
            runScript(DaoConstants.POPULATE_ALBUM_BIRTHDAY_SCRIPT);
        } catch (IOException ex) {
            log.error("Error reading SQL script file for populating the album birthdays", ex);
        }
    }

    private void runScript(String script) throws IOException {
        InputStream sqlFileInputStream =
                new ClassPathResource(script, getClass())
                        .getInputStream();
        String sql =
                FileCopyUtils.copyToString(
                        new InputStreamReader(sqlFileInputStream));
        jdbcTemplate.update(sql);
    }

    @Override
    public List<AlbumBirthday> getAlbumBirthdays(MonthDay from, MonthDay to) {

        String selectBody = getSelectBody();
        String dateConditions = getCombinedConditions(from, to);
        String orderBy = getOrderBy();
        String fullSql = selectBody + dateConditions + orderBy;

        return jdbcTemplate.query(fullSql, new AlbumBirthdayListExtractor());
    }

    private String getSelectBody() {
        return new StringBuilder()
                .append("SELECT ")
                .append("al").append(".").append(ALBUM_MBID_COLUMN).append(",")
                .append("al").append(".").append(ALBUM_NAME_COLUMN).append(",")
                .append("al").append(".").append(ALBUM_ARTIST_DISPLAY_NAME_COLUMN).append(",")
                .append("ar").append(".").append(ARTIST_MBID_COLUMN).append(",")
                .append("ar").append(".").append(ARTIST_NAME_COLUMN).append(",")
                .append("b").append(".").append(ALBUM_YEAR_COLUMN).append(",")
                .append("b").append(".").append(ALBUM_MONTH_COLUMN).append(",")
                .append("b").append(".").append(ALBUM_DAY_COLUMN)
                .append(" FROM ")
                .append(BIRTHDAY_TABLE_FULL_NAME).append(" b")
                .append(" INNER JOIN ").append(ALBUM_TABLE_FULL_NAME).append(" al").append(" ON ")
                .append("b").append(".").append(ALBUM_MBID_COLUMN).append(" = ").append("al").append(".").append(ALBUM_MBID_COLUMN)
                .append(" INNER JOIN ").append(ALBUM_ARTIST_TABLE_FULL_NAME).append(" alar").append(" ON ")
                .append("al").append(".").append(ALBUM_MBID_COLUMN).append(" = ").append("alar").append(".").append(ALBUM_MBID_COLUMN)
                .append(" INNER JOIN ").append(ARTIST_TABLE_FULL_NAME).append(" ar").append(" ON ")
                .append("alar").append(".").append(ARTIST_MBID_COLUMN).append(" = ").append("ar").append(".").append(ARTIST_MBID_COLUMN)
                .toString();
    }

    private String getCombinedConditions(MonthDay from, MonthDay to) {
        final boolean crossEndOfYear = to.isBefore(from);
        final BiFunction<MonthDay, MonthDay, String> conditionGetter;
        if (isRangeInSameMonth(from, to)) {
            conditionGetter = this::getDateConditionsForSameMonth;
        } else {
            conditionGetter = this::getDateConditionsForDifferentMonths;
        }

        if (crossEndOfYear) {
            final MonthDay intermediateTo = MonthDay.of(12, 31);
            final MonthDay intermediateFrom = MonthDay.of(1, 1);
            return " WHERE (" +
                    conditionGetter.apply(from, intermediateTo) +
                    ") OR (" +
                    conditionGetter.apply(intermediateFrom, to) + ")";
        } else {
            return " WHERE " + conditionGetter.apply(from, to);
        }
    }


    private boolean isRangeInSameMonth(MonthDay from, MonthDay to) {
        return from.getMonth().equals(to.getMonth());
    }

    private String getDateConditionsForSameMonth(MonthDay from, MonthDay to) {
        return new StringBuilder().append("b").append(".").append(ALBUM_MONTH_COLUMN).append("=").append(from.getMonthValue())
                .append(" AND ").append("b").append(".").append(ALBUM_DAY_COLUMN).append(">=").append(from.getDayOfMonth())
                .append(" AND ").append("b").append(".").append(ALBUM_DAY_COLUMN).append("<=").append(to.getDayOfMonth())
                .toString();
    }

    private String getDateConditionsForDifferentMonths(MonthDay from, MonthDay to) {
        return new StringBuilder().append("((").append("b").append(".").append(ALBUM_MONTH_COLUMN).append("=").append(from.getMonthValue())
                .append(" AND ").append("b").append(".").append(ALBUM_DAY_COLUMN).append(">=").append(from.getDayOfMonth()).append(")")
                .append(" OR (").append("b").append(".").append(ALBUM_MONTH_COLUMN).append(">").append(from.getMonthValue())
                .append(" AND ").append("b").append(".").append(ALBUM_MONTH_COLUMN).append("<").append(to.getMonthValue()).append(")")
                .append(" OR (").append("b").append(".").append(ALBUM_MONTH_COLUMN).append("=").append(to.getMonthValue())
                .append(" AND ").append("b").append(".").append(ALBUM_DAY_COLUMN).append("<=").append(to.getDayOfMonth()).append("))")
                .toString();
    }

    private String getOrderBy() {
        return new StringBuilder().append(" ORDER BY")
                .append(" ").append("b").append(".").append(ALBUM_MONTH_COLUMN).append(",")
                .append(" ").append("b").append(".").append(ALBUM_DAY_COLUMN).append(",")
                .append(" ").append("al").append(".").append(ALBUM_NAME_COLUMN).append(",")
                .append(" ").append("alar").append(".").append(ARTIST_POSITION_COLUMN)
                .toString();
    }
}
