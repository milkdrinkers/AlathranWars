package io.github.alathra.alathranwars.utility;

import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for creating {@link TagResolver} for date/time related java classes.
 * </br></br>This class provides methods to create TagResolvers for {@link LocalDateTime}, {@link ZonedDateTime}, {@link Instant}, {@link LocalDate}, {@link LocalTime},
 * and {@link Duration} objects, allowing for easy resolution of date and time components using MiniMessage.
 *
 * @since 4.0.0
 */
public final class TimeTagResolversTowny {
    /**
     * Adds a prefix to each tag in the set. Allows consumers to support multiple time unit tag resolvers in the same context.
     *
     * @param tagPrefix the prefix to add to each tag
     * @param tags      the set of tags to prefix
     * @return a set of tags with the prefix added
     * @since 4.0.0
     */
    private static Set<String> prefix(@TagPattern final String tagPrefix, final Set<String> tags) {
        // Don't iterate and mutate if the prefix is empty
        if (tagPrefix.isEmpty())
            return tags;

        return tags.stream()
            .map(s -> tagPrefix + s)
            .collect(Collectors.toUnmodifiableSet());
    }

    //region Date & Time Tag Resolvers

    private static final Set<String> YEAR_TAGS = Set.of("year", "yyyy");
    private static final Set<String> YEAR_2DIGIT_TAGS = Set.of("yy");

    private static final Set<String> MONTH_NUMBER_TAGS = Set.of("month", "mo");
    private static final Set<String> MONTH_OF_YEAR_TAGS = Set.of("mmo");
    private static final Set<String> MONTH_SHORT_TAGS = Set.of("mmms", "month_short");
    private static final Set<String> MONTH_FULL_TAGS = Set.of("mmmm", "month_full");

    private static final Set<String> DAY_TAGS = Set.of("day", "d");
    private static final Set<String> DAY_OF_MONTH_TAGS = Set.of("day_of_month", "dd");
    private static final Set<String> DAY_OF_YEAR_TAGS = Set.of("day_of_year", "ddd");
    private static final Set<String> DAY_OF_WEEK_NUM_TAGS = Set.of("day_of_week");
    private static final Set<String> DAY_OF_WEEK_SHORT_TAGS = Set.of("e", "day_short");
    private static final Set<String> DAY_OF_WEEK_FULL_TAGS = Set.of("eeee", "day_full");

    private static final Set<String> HOUR_24_TAGS = Set.of("hour", "h24");
    private static final Set<String> HOUR_24_ZERO_TAGS = Set.of("hh24");
    private static final Set<String> HOUR_12_TAGS = Set.of("h");
    private static final Set<String> HOUR_12_ZERO_TAGS = Set.of("hh");
    private static final Set<String> AMPM_TAGS = Set.of("ampm");

    private static final Set<String> MINUTE_TAGS = Set.of("minute", "m", "min");
    private static final Set<String> MINUTE_OF_HOUR_TAGS = Set.of("mm");

    private static final Set<String> SECOND_TAGS = Set.of("second", "s", "sec");
    private static final Set<String> SECOND_OF_MINUTE_TAGS = Set.of("ss");

    private static final Set<String> MILLI_TAGS = Set.of("milli", "ms");
    private static final Set<String> MILLI_3DIGIT_TAGS = Set.of("sss", "mmm");
    private static final Set<String> NANO_TAGS = Set.of("nano", "ns");
    private static final Set<String> NANO_9DIGIT_TAGS = Set.of("nnnnnnnnn");

    private static final Set<String> TIMEZONE_ID_TAGS = Set.of("timezone", "zone_id", "vv");
    private static final Set<String> TIMEZONE_SHORT_TAGS = Set.of("z", "zone_short");
    private static final Set<String> TIMEZONE_FULL_TAGS = Set.of("zzzz", "zone_full");
    private static final Set<String> TIMEZONE_OFFSET_TAGS = Set.of("x", "offset");
    private static final Set<String> TIMEZONE_OFFSET_COLON_TAGS = Set.of("xxx", "offset_colon");

    private static final Set<String> EPOCH_SECOND_TAGS = Set.of("epoch_second", "epoch_s");
    private static final Set<String> EPOCH_MILLI_TAGS = Set.of("epoch_milli", "epoch_ms");
    private static final Set<String> EPOCH_NANO_TAGS = Set.of("epoch_nano", "epoch_ns");

    /**
     * Creates a TagResolver for LocalDateTime components.
     *
     * @param dateTime the LocalDateTime to resolve tags for
     * @return a TagResolver that resolves various date and time components
     * @see #tag(String, LocalDateTime)
     * @since 4.0.0
     */
    @SuppressWarnings("unused")
    public static TagResolver tag(final LocalDateTime dateTime) {
        return tag("", dateTime);
    }

    /**
     * Creates a TagResolver for LocalDateTime components.
     *
     * @param tagPrefix the prefix to add to each tag
     * @param dateTime  the LocalDateTime to resolve tags for
     * @return a TagResolver that resolves various date and time components
     * @since 4.0.0
     */
    @SuppressWarnings("unused")
    public static TagResolver tag(@TagPattern final String tagPrefix, final LocalDateTime dateTime) {
        return TagResolver.resolver(
            TagResolver.resolver(prefix(tagPrefix, YEAR_TAGS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(dateTime.getYear()))),
            TagResolver.resolver(prefix(tagPrefix, YEAR_2DIGIT_TAGS), (args, ctx) -> Tag.preProcessParsed(String.format("%02d", dateTime.getYear() % 100))),

            TagResolver.resolver(prefix(tagPrefix, MONTH_NUMBER_TAGS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(dateTime.getMonthValue()))),
            TagResolver.resolver(prefix(tagPrefix, MONTH_OF_YEAR_TAGS), (args, ctx) -> Tag.preProcessParsed(String.format("%02d", dateTime.getMonthValue()))),
            TagResolver.resolver(prefix(tagPrefix, MONTH_SHORT_TAGS), (args, ctx) -> Tag.preProcessParsed(dateTime.getMonth().toString().substring(0, 3))),
            TagResolver.resolver(prefix(tagPrefix, MONTH_FULL_TAGS), (args, ctx) -> Tag.preProcessParsed(dateTime.getMonth().toString())),

            TagResolver.resolver(prefix(tagPrefix, DAY_TAGS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(dateTime.getDayOfMonth()))),
            TagResolver.resolver(prefix(tagPrefix, DAY_OF_MONTH_TAGS), (args, ctx) -> Tag.preProcessParsed(String.format("%02d", dateTime.getDayOfMonth()))),
            TagResolver.resolver(prefix(tagPrefix, DAY_OF_YEAR_TAGS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(dateTime.getDayOfYear()))),
            TagResolver.resolver(prefix(tagPrefix, DAY_OF_WEEK_NUM_TAGS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(dateTime.getDayOfWeek().getValue()))),
            TagResolver.resolver(prefix(tagPrefix, DAY_OF_WEEK_SHORT_TAGS), (args, ctx) -> Tag.preProcessParsed(dateTime.getDayOfWeek().toString().substring(0, 3))),
            TagResolver.resolver(prefix(tagPrefix, DAY_OF_WEEK_FULL_TAGS), (args, ctx) -> Tag.preProcessParsed(dateTime.getDayOfWeek().toString())),

            TagResolver.resolver(prefix(tagPrefix, HOUR_24_TAGS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(dateTime.getHour()))),
            TagResolver.resolver(prefix(tagPrefix, HOUR_24_ZERO_TAGS), (args, ctx) -> Tag.preProcessParsed(String.format("%02d", dateTime.getHour()))),
            TagResolver.resolver(prefix(tagPrefix, HOUR_12_TAGS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(dateTime.getHour() == 0 ? 12 : dateTime.getHour() > 12 ? dateTime.getHour() - 12 : dateTime.getHour()))),
            TagResolver.resolver(prefix(tagPrefix, HOUR_12_ZERO_TAGS), (args, ctx) -> Tag.preProcessParsed(String.format("%02d", dateTime.getHour() == 0 ? 12 : dateTime.getHour() > 12 ? dateTime.getHour() - 12 : dateTime.getHour()))),
            TagResolver.resolver(prefix(tagPrefix, AMPM_TAGS), (args, ctx) -> Tag.preProcessParsed(dateTime.getHour() < 12 ? "AM" : "PM")),

            TagResolver.resolver(prefix(tagPrefix, MINUTE_TAGS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(dateTime.getMinute()))),
            TagResolver.resolver(prefix(tagPrefix, MINUTE_OF_HOUR_TAGS), (args, ctx) -> Tag.preProcessParsed(String.format("%02d", dateTime.getMinute()))),

            TagResolver.resolver(prefix(tagPrefix, SECOND_TAGS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(dateTime.getSecond()))),
            TagResolver.resolver(prefix(tagPrefix, SECOND_OF_MINUTE_TAGS), (args, ctx) -> Tag.preProcessParsed(String.format("%02d", dateTime.getSecond()))),

            TagResolver.resolver(prefix(tagPrefix, MILLI_TAGS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(dateTime.get(ChronoField.MILLI_OF_SECOND)))),
            TagResolver.resolver(prefix(tagPrefix, MILLI_3DIGIT_TAGS), (args, ctx) -> Tag.preProcessParsed(String.format("%03d", dateTime.get(ChronoField.MILLI_OF_SECOND)))),
            TagResolver.resolver(prefix(tagPrefix, NANO_TAGS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(dateTime.getNano()))),
            TagResolver.resolver(prefix(tagPrefix, NANO_9DIGIT_TAGS), (args, ctx) -> Tag.preProcessParsed(String.format("%09d", dateTime.getNano())))
        );
    }


    /**
     * Creates a TagResolver for ZonedDateTime components.
     *
     * @param dateTime the ZonedDateTime to resolve tags for
     * @return a TagResolver that resolves various date and time components, including timezone information
     * @see #tag(String, ZonedDateTime)
     * @since 4.0.0
     */
    @SuppressWarnings("unused")
    public static TagResolver tag(ZonedDateTime dateTime) {
        return tag("", dateTime);
    }

    /**
     * Creates a TagResolver for ZonedDateTime components.
     *
     * @param tagPrefix the prefix to add to each tag
     * @param dateTime  the ZonedDateTime to resolve tags for
     * @return a TagResolver that resolves various date and time components, including timezone information
     * @since 4.0.0
     */
    @SuppressWarnings("unused")
    public static TagResolver tag(@TagPattern final String tagPrefix, ZonedDateTime dateTime) {
        return TagResolver.resolver(
            tag(tagPrefix, dateTime.toLocalDateTime()),

            TagResolver.resolver(prefix(tagPrefix, TIMEZONE_ID_TAGS), (args, ctx) -> Tag.preProcessParsed(dateTime.getZone().getId())),
            TagResolver.resolver(prefix(tagPrefix, TIMEZONE_SHORT_TAGS), (args, ctx) -> Tag.preProcessParsed(dateTime.format(DateTimeFormatter.ofPattern("z")))),
            TagResolver.resolver(prefix(tagPrefix, TIMEZONE_FULL_TAGS), (args, ctx) -> Tag.preProcessParsed(dateTime.format(DateTimeFormatter.ofPattern("zzzz")))),
            TagResolver.resolver(prefix(tagPrefix, TIMEZONE_OFFSET_TAGS), (args, ctx) -> Tag.preProcessParsed(dateTime.getOffset().toString())),
            TagResolver.resolver(prefix(tagPrefix, TIMEZONE_OFFSET_COLON_TAGS), (args, ctx) -> Tag.preProcessParsed(dateTime.format(DateTimeFormatter.ofPattern("XXX")))),

            TagResolver.resolver(prefix(tagPrefix, EPOCH_SECOND_TAGS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(dateTime.toEpochSecond()))),
            TagResolver.resolver(prefix(tagPrefix, EPOCH_MILLI_TAGS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(dateTime.toInstant().toEpochMilli()))),
            TagResolver.resolver(prefix(tagPrefix, EPOCH_NANO_TAGS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(dateTime.toInstant().toEpochMilli() * 1_000_000 + dateTime.getNano())))
        );
    }

    /**
     * Creates a TagResolver for Instant components.
     *
     * @param instant the Instant to resolve tags for
     * @return a TagResolver that resolves various components of the instant
     * @since 4.0.0
     */
    @SuppressWarnings("unused")
    public static TagResolver tag(Instant instant) {
        return tag("", instant.atZone(ZoneOffset.UTC));
    }

    /**
     * Creates a TagResolver for Instant components.
     *
     * @param tagPrefix the prefix to add to each tag
     * @param instant   the Instant to resolve tags for
     * @return a TagResolver that resolves various components of the instant
     * @since 4.0.0
     */
    @SuppressWarnings("unused")
    public static TagResolver tag(@TagPattern final String tagPrefix, Instant instant) {
        return tag(tagPrefix, instant.atZone(ZoneOffset.UTC));
    }

    /**
     * Creates a TagResolver for Instant components in a specific timezone.
     *
     * @param instant the Instant to resolve tags for
     * @param zone    the ZoneId to use for the Instant
     * @return a TagResolver that resolves various components of the instant in the specified timezone
     * @since 4.0.0
     */
    @SuppressWarnings("unused")
    public static TagResolver tag(Instant instant, ZoneId zone) {
        return tag("", instant.atZone(zone));
    }

    /**
     * Creates a TagResolver for Instant components in a specific timezone.
     *
     * @param tagPrefix the prefix to add to each tag
     * @param instant   the Instant to resolve tags for
     * @param zone      the ZoneId to use for the Instant
     * @return a TagResolver that resolves various components of the instant in the specified timezone
     * @since 4.0.0
     */
    @SuppressWarnings("unused")
    public static TagResolver tag(@TagPattern final String tagPrefix, Instant instant, ZoneId zone) {
        return tag(tagPrefix, instant.atZone(zone));
    }

    /**
     * Creates a TagResolver for LocalDate components.
     *
     * @param date the LocalDate to resolve tags for
     * @return a TagResolver that resolves various components of the date
     * @see #tag(String, LocalDate)
     * @since 4.0.0
     */
    @SuppressWarnings("unused")
    public static TagResolver tag(LocalDate date) {
        return tag("", date.atStartOfDay());
    }

    /**
     * Creates a TagResolver for LocalDate components.
     *
     * @param tagPrefix the prefix to add to each tag
     * @param date      the LocalDate to resolve tags for
     * @return a TagResolver that resolves various components of the date
     * @since 4.0.0
     */
    @SuppressWarnings("unused")
    public static TagResolver tag(@TagPattern final String tagPrefix, LocalDate date) {
        return tag(tagPrefix, date.atStartOfDay());
    }

    /**
     * Creates a TagResolver for LocalTime components with a specific time.
     *
     * @param time the LocalTime to use with the LocalDate
     * @return a TagResolver that resolves various components of the date at the specified time
     * @see #tag(String, LocalTime)
     * @since 4.0.0
     */
    @SuppressWarnings("unused")
    public static TagResolver tag(LocalTime time) {
        return tag("", LocalDate.now().atTime(time));
    }

    /**
     * Creates a TagResolver for LocalTime components with a specific time.
     *
     * @param tagPrefix the prefix to add to each tag
     * @param time      the LocalTime to use with the LocalDate
     * @return a TagResolver that resolves various components of the date at the specified time
     * @since 4.0.0
     */
    @SuppressWarnings("unused")
    public static TagResolver tag(@TagPattern final String tagPrefix, LocalTime time) {
        return tag(tagPrefix, LocalDate.now().atTime(time));
    }

    //endregion

    //region Date & Time Span Tag Resolvers

    private static final Set<String> TAGS_YEARS = Set.of("y", "years", "year");
    private static final Set<String> TAGS_MONTHS = Set.of("mo", "months", "month");
    private static final Set<String> TAGS_DAYS = Set.of("d", "days", "day");
    private static final Set<String> TAGS_HOURS = Set.of("h", "hours", "hour");
    private static final Set<String> TAGS_MINUTES = Set.of("m", "minutes", "minute", "min");
    private static final Set<String> TAGS_SECONDS = Set.of("s", "seconds", "second", "sec");
    private static final Set<String> TAGS_MILLIS = Set.of("ms", "milliseconds", "millisecond", "milli");
    private static final Set<String> TAGS_NANOS = Set.of("ns", "nanoseconds", "nanosecond", "nano");

    private static final Set<String> TAGS_YEARS_ZERO = Set.of("yy");
    private static final Set<String> TAGS_MONTHS_ZERO = Set.of("mmo");
    private static final Set<String> TAGS_DAYS_ZERO = Set.of("dd");
    private static final Set<String> TAGS_HOURS_ZERO = Set.of("hh");
    private static final Set<String> TAGS_MINUTES_ZERO = Set.of("mm");
    private static final Set<String> TAGS_SECONDS_ZERO = Set.of("ss");
    private static final Set<String> TAGS_MILLI_ZERO = Set.of("ms");
    private static final Set<String> TAGS_NANO_ZERO = Set.of("ns");

    private static final Set<String> TAGS_TOTAL_YEARS = Set.of("total_years", "ty");
    private static final Set<String> TAGS_TOTAL_MONTHS = Set.of("total_months", "tmo");
    private static final Set<String> TAGS_TOTAL_DAYS = Set.of("total_days", "td");
    private static final Set<String> TAGS_TOTAL_HOURS = Set.of("total_hours", "th");
    private static final Set<String> TAGS_TOTAL_MINUTES = Set.of("total_minutes", "tm");
    private static final Set<String> TAGS_TOTAL_SECONDS = Set.of("total_seconds", "ts");
    private static final Set<String> TAGS_TOTAL_MILLIS = Set.of("total_millis", "tms");
    private static final Set<String> TAGS_TOTAL_NANOS = Set.of("total_nanos", "tns");

    /**
     * Creates a TagResolver for Duration components.
     *
     * @param duration the Duration to resolve tags for
     * @return a TagResolver that resolves various components of the duration
     * @see #tag(String, Duration)
     * @since 4.0.0
     */
    @SuppressWarnings("unused")
    public static TagResolver tag(final Duration duration) {
        return tag("", duration);
    }

    /**
     * Creates a TagResolver for Duration components.
     *
     * @param tagPrefix the prefix to add to each tag
     * @param duration  the Duration to resolve tags for
     * @return a TagResolver that resolves various components of the duration
     * @since 4.0.0
     */
    @SuppressWarnings("unused")
    public static TagResolver tag(@TagPattern final String tagPrefix, final Duration duration) {
        final Duration dur = duration.abs();

        final long days = dur.toDaysPart();
        final int hours = dur.toHoursPart();
        final int minutes = dur.toMinutesPart();
        final int seconds = dur.toSecondsPart();
        final int millis = dur.toMillisPart();
        final int nanos = dur.toNanosPart();

        final long totalDays = dur.toDays();
        final long totalHours = dur.toHours();
        final long totalMinutes = dur.toMinutes();
        final long totalSeconds = dur.getSeconds();
        final long totalMillis = dur.toMillis();
        final long totalNanos = dur.toNanos();

        return TagResolver.resolver(
            TagResolver.resolver(prefix(tagPrefix, TAGS_DAYS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(days))),
            TagResolver.resolver(prefix(tagPrefix, TAGS_HOURS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(hours))),
            TagResolver.resolver(prefix(tagPrefix, TAGS_MINUTES), (args, ctx) -> Tag.preProcessParsed(String.valueOf(minutes))),
            TagResolver.resolver(prefix(tagPrefix, TAGS_SECONDS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(seconds))),
            TagResolver.resolver(prefix(tagPrefix, TAGS_MILLIS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(millis))),
            TagResolver.resolver(prefix(tagPrefix, TAGS_NANOS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(nanos))),

            TagResolver.resolver(prefix(tagPrefix, TAGS_DAYS_ZERO), (args, ctx) -> Tag.preProcessParsed(String.format("%02d", days))),
            TagResolver.resolver(prefix(tagPrefix, TAGS_HOURS_ZERO), (args, ctx) -> Tag.preProcessParsed(String.format("%02d", hours))),
            TagResolver.resolver(prefix(tagPrefix, TAGS_MINUTES_ZERO), (args, ctx) -> Tag.preProcessParsed(String.format("%02d", minutes))),
            TagResolver.resolver(prefix(tagPrefix, TAGS_SECONDS_ZERO), (args, ctx) -> Tag.preProcessParsed(String.format("%02d", seconds))),
            TagResolver.resolver(prefix(tagPrefix, TAGS_MILLI_ZERO), (args, ctx) -> Tag.preProcessParsed(String.format("%02d", millis))),
            TagResolver.resolver(prefix(tagPrefix, TAGS_NANO_ZERO), (args, ctx) -> Tag.preProcessParsed(String.format("%02d", nanos))),

            TagResolver.resolver(prefix(tagPrefix, TAGS_TOTAL_DAYS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(totalDays))),
            TagResolver.resolver(prefix(tagPrefix, TAGS_TOTAL_HOURS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(totalHours))),
            TagResolver.resolver(prefix(tagPrefix, TAGS_TOTAL_MINUTES), (args, ctx) -> Tag.preProcessParsed(String.valueOf(totalMinutes))),
            TagResolver.resolver(prefix(tagPrefix, TAGS_TOTAL_SECONDS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(totalSeconds))),
            TagResolver.resolver(prefix(tagPrefix, TAGS_TOTAL_MILLIS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(totalMillis))),
            TagResolver.resolver(prefix(tagPrefix, TAGS_TOTAL_NANOS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(totalNanos)))
        );
    }

    /**
     * Creates a TagResolver for Period components.
     *
     * @param period the Period to resolve tags for
     * @return a TagResolver that resolves various components of the period
     * @since 4.0.0
     */
    @SuppressWarnings("unused")
    public static TagResolver tag(final Period period) {
        return tag("", period);
    }

    /**
     * Creates a TagResolver for Period components.
     *
     * @param tagPrefix the prefix to add to each tag
     * @param period    the Period to resolve tags for
     * @return a TagResolver that resolves various components of the period
     * @since 4.0.0
     */
    @SuppressWarnings("unused")
    public static TagResolver tag(@TagPattern final String tagPrefix, final Period period) {
        final Period per = period.normalized();

        final int years = Math.abs(per.getYears());
        final int months = Math.abs(period.getMonths());
        final int days = Math.abs(period.getDays());

        final int totalYears = Math.abs(per.getYears());
        final long totalMonths = Math.abs(period.toTotalMonths());
        final int totalDays = Math.abs(period.getDays() + period.getMonths() * 30 + period.getYears() * 365);

        return TagResolver.resolver(
            TagResolver.resolver(prefix(tagPrefix, TAGS_YEARS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(years))),
            TagResolver.resolver(prefix(tagPrefix, TAGS_MONTHS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(months))),
            TagResolver.resolver(prefix(tagPrefix, TAGS_DAYS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(days))),

            TagResolver.resolver(prefix(tagPrefix, TAGS_YEARS_ZERO), (args, ctx) -> Tag.preProcessParsed(String.format("%02d", years))),
            TagResolver.resolver(prefix(tagPrefix, TAGS_MONTHS_ZERO), (args, ctx) -> Tag.preProcessParsed(String.format("%02d", months))),
            TagResolver.resolver(prefix(tagPrefix, TAGS_DAYS_ZERO), (args, ctx) -> Tag.preProcessParsed(String.format("%02d", days))),

            TagResolver.resolver(prefix(tagPrefix, TAGS_TOTAL_YEARS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(totalYears))),
            TagResolver.resolver(prefix(tagPrefix, TAGS_TOTAL_MONTHS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(totalMonths))),
            TagResolver.resolver(prefix(tagPrefix, TAGS_TOTAL_DAYS), (args, ctx) -> Tag.preProcessParsed(String.valueOf(totalDays)))
        );
    }

    //endregion
}
