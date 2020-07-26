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
        log.info("Clearing album birthdays");
        jdbcTemplate.update("TRUNCATE TABLE " + BIRTHDAY_TABLE_FULL_NAME);
    }

    @Override
    public void populateAlbumBirthdays() {
        log.info("Populating album birthdays");
        try {
            InputStream sqlFileInputStream =
                    new ClassPathResource(DaoConstants.POPULATE_ALBUM_BIRTHDAY_SCRIPT, getClass())
                            .getInputStream();
            String sql =
                    FileCopyUtils.copyToString(
                            new InputStreamReader(sqlFileInputStream));
            jdbcTemplate.update(sql);
        } catch (IOException ex) {
            log.error("Error reading SQL script file for populating the album birthdays", ex);
        }
    }

    @Override
    public List<AlbumBirthday> getAlbumBirthdays(MonthDay from, MonthDay to) {

        String selectBody = getSelectBody();
        String dateConditions = getCombinedConditions(from, to);
        String orderBy = getOrderBy();
        String fullSql = selectBody + dateConditions + orderBy;

        return jdbcTemplate.query(fullSql, new AlbumBirthdayRowMapper());
    }

    private String getSelectBody() {
        return new StringBuilder()
                .append("SELECT * FROM ")
                .append(BIRTHDAY_TABLE_FULL_NAME)
                .append(" WHERE ").append(ALBUM_MBID_COLUMN).append(" IN ")
                .append("(SELECT ").append(ALBUM_MBID_COLUMN).append(" FROM ").append(ALBUM_TABLE_FULL_NAME).append(")")
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

        if (to.isBefore(from)) {
            final MonthDay intermediateTo = MonthDay.of(12, 31);
            final MonthDay intermediateFrom = MonthDay.of(1, 1);
            return " AND (" +
                    conditionGetter.apply(from, intermediateTo) +
                    ") OR (" +
                    conditionGetter.apply(intermediateFrom, to) + ")";
        } else {
            return " AND " + conditionGetter.apply(from, to);
        }
    }


    private boolean isRangeInSameMonth(MonthDay from, MonthDay to) {
        return from.getMonth().equals(to.getMonth());
    }

    private String getDateConditionsForSameMonth(MonthDay from, MonthDay to) {
        return new StringBuilder().append(ALBUM_MONTH_COLUMN).append("=").append(from.getMonthValue())
                .append(" AND ").append(ALBUM_DAY_COLUMN).append(">=").append(from.getDayOfMonth())
                .append(" AND ").append(ALBUM_DAY_COLUMN).append("<=").append(to.getDayOfMonth())
                .toString();
    }

    private String getDateConditionsForDifferentMonths(MonthDay from, MonthDay to) {
        return new StringBuilder().append("((").append(ALBUM_MONTH_COLUMN).append("=").append(from.getMonthValue())
                .append(" AND ").append(ALBUM_DAY_COLUMN).append(">=").append(from.getDayOfMonth()).append(")")
                .append(" OR (").append(ALBUM_MONTH_COLUMN).append(">").append(from.getMonthValue())
                .append(" AND ").append(ALBUM_MONTH_COLUMN).append("<").append(to.getMonthValue()).append(")")
                .append(" OR (").append(ALBUM_MONTH_COLUMN).append("=").append(to.getMonthValue())
                .append(" AND ").append(ALBUM_DAY_COLUMN).append("<=").append(to.getDayOfMonth()).append("))")
                .toString();
    }

    private String getOrderBy() {
        return new StringBuilder().append(" ORDER BY")
                .append(" ").append(ALBUM_MONTH_COLUMN).append(",")
                .append(" ").append(ALBUM_DAY_COLUMN).append(",")
                .append(" ").append(ARTIST_NAME_COLUMN)
                .toString();
    }
}
