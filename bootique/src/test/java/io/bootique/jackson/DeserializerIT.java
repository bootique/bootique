package io.bootique.jackson;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import io.bootique.config.PolymorphicConfiguration;
import io.bootique.config.TypesFactory;
import io.bootique.log.DefaultBootLogger;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;

import static org.junit.Assert.assertNotNull;

public class DeserializerIT {
    private TypesFactory<PolymorphicConfiguration> typesFactory = new TypesFactory<>(
            getClass().getClassLoader(),
            PolymorphicConfiguration.class,
            new DefaultBootLogger(true));

    protected ObjectMapper mapper = new DefaultJacksonService(typesFactory.getTypes()).newObjectMapper();

    protected <T> T readValue(Class<T> type, ObjectMapper mapper, String yml) throws IOException {
        YAMLParser parser = new YAMLFactory().createParser(yml);
        JsonNode node = mapper.readTree(parser);
        assertNotNull(node);

        return mapper.readValue(new TreeTraversingParser(node), type);
    }

    protected static class Bean1 {
        protected String a;
        protected Bean2 c;

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public Bean2 getC() {
            return c;
        }

        public void setC(Bean2 c) {
            this.c = c;
        }
    }

    protected static class Bean2 {

        protected LocalDate localDate;
        protected LocalDateTime localDateTime;
        protected LocalTime localTime;
        protected Duration duration;
        protected Year year;
        protected Month month;
        protected YearMonth yearMonth;
        protected MonthDay monthDay;
        protected Instant instant;

        public LocalDate getLocalDate() {
            return localDate;
        }

        public void setLocalDate(LocalDate localDate) {
            this.localDate = localDate;
        }

        public LocalDateTime getLocalDateTime() {
            return localDateTime;
        }

        public void setLocalDateTime(LocalDateTime localDateTime) {
            this.localDateTime = localDateTime;
        }

        public LocalTime getLocalTime() {
            return localTime;
        }

        public void setLocalTime(LocalTime localTime) {
            this.localTime = localTime;
        }

        public Duration getDuration() {
            return duration;
        }

        public void setDuration(Duration duration) {
            this.duration = duration;
        }

        public Year getYear() {
            return year;
        }

        public void setYear(Year year) {
            this.year = year;
        }

        public Month getMonth() {
            return month;
        }

        public void setMonth(Month month) {
            this.month = month;
        }

        public YearMonth getYearMonth() {
            return yearMonth;
        }

        public void setYearMonth(YearMonth yearMonth) {
            this.yearMonth = yearMonth;
        }

        public MonthDay getMonthDay() {
            return monthDay;
        }

        public void setMonthDay(MonthDay monthDay) {
            this.monthDay = monthDay;
        }

        public Instant getInstant() {
            return instant;
        }

        public void setInstant(Instant instant) {
            this.instant = instant;
        }
    }
}
