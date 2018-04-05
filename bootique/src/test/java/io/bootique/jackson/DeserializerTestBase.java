package io.bootique.jackson;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import io.bootique.config.PolymorphicConfiguration;
import io.bootique.config.TypesFactory;
import io.bootique.log.DefaultBootLogger;

import java.io.IOException;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public abstract class DeserializerTestBase {

    private TypesFactory<PolymorphicConfiguration> typesFactory = new TypesFactory<>(
            getClass().getClassLoader(),
            PolymorphicConfiguration.class,
            new DefaultBootLogger(true));

    private JacksonService jacksonService = new DefaultJacksonService(typesFactory.getTypes());

    protected <T> T deserialize(Class<T> type, String yml) throws IOException {
        YAMLParser parser = new YAMLFactory().createParser(yml);
        return jacksonService.newObjectMapper().readValue(parser, type);
    }

    protected ObjectMapper createMapper() {
        return jacksonService.newObjectMapper();
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

        protected Year year;
        protected Month month;
        protected YearMonth yearMonth;
        protected MonthDay monthDay;
        protected Period period;
        protected OffsetTime offsetTime;
        protected OffsetDateTime offsetDateTime;
        protected ZonedDateTime zonedDateTime;
        protected ZoneOffset zoneOffset;


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

        public Period getPeriod() {
            return period;
        }

        public void setPeriod(Period period) {
            this.period = period;
        }

        public OffsetTime getOffsetTime() {
            return offsetTime;
        }

        public void setOffsetTime(OffsetTime offsetTime) {
            this.offsetTime = offsetTime;
        }

        public OffsetDateTime getOffsetDateTime() {
            return offsetDateTime;
        }

        public void setOffsetDateTime(OffsetDateTime offsetDateTime) {
            this.offsetDateTime = offsetDateTime;
        }

        public ZonedDateTime getZonedDateTime() {
            return zonedDateTime;
        }

        public void setZonedDateTime(ZonedDateTime zonedDateTime) {
            this.zonedDateTime = zonedDateTime;
        }

        public ZoneOffset getZoneOffset() {
            return zoneOffset;
        }

        public void setZoneOffset(ZoneOffset zoneOffset) {
            this.zoneOffset = zoneOffset;
        }

    }
}
