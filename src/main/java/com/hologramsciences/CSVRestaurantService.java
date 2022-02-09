package com.hologramsciences;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVRecord;

import io.atlassian.fugue.Option;

public class CSVRestaurantService {
    private final List<Restaurant> restaurantList;

    /**
     * TODO: Implement Me
     *
     * From the CSVRecord which represents a single line from src/main/resources/rest_hours.csv
     * Write a parser to read the line and create an instance of the Restaurant class (Optionally, using the Option class)
     *
     * Example Line:
     *
     *  "Burger Bar","Mon,Tue,Wed,Thu,Sun|11:00-22:00;Fri,Sat|11:00-0:00"
     *
     *  '|'   separates the list of applicable days from the hours span
     *  ';'   separates groups of (list of applicable days, hours span)
     *
     *  So the above line would be parsed as:
     *
     *  Map<DayOfWeek, OpenHours> m = new HashMap<>();
     *  m.put(MONDAY,    new OpenHours(LocalTime.of(11, 0), LocalTime.of(22, 0)));
     *  m.put(TUESDAY,   new OpenHours(LocalTime.of(11, 0), LocalTime.of(22, 0)));
     *  m.put(WEDNESDAY, new OpenHours(LocalTime.of(11, 0), LocalTime.of(22, 0)));
     *  m.put(THURSDAY,  new OpenHours(LocalTime.of(11, 0), LocalTime.of(22, 0)));
     *  m.put(SUNDAY,    new OpenHours(LocalTime.of(11, 0), LocalTime.of(22, 0)));
     *
     *  m.put(FRIDAY,    new OpenHours(LocalTime.of(11, 0), LocalTime.of(0, 0)));
     *  m.put(SATURDAY,  new OpenHours(LocalTime.of(11, 0), LocalTime.of(0, 0)));
     *
     *  Option.some(new Restaurant("Burger Bar", m))
     *
     * This method returns Option.some(parsedRestaurant),
     *       IF the String name, and Map<DayOfWeek, OpenHours> openHours is found in the CSV,
     *         - assume if both columns are in the CSV then they are both parsable.
     *       AND if all values in openHours have !startTime.equals(endTime)
     *
     * This method returns Option.none() when any of the OpenHours for a given restaurant have the same startTime and endDate
     *
     *
     * NOTE, the getDayOfWeek method should be helpful, and the LocalTime should be parsable by LocalDate.parse
     *
     */
    public static Option<Restaurant> parse(final CSVRecord r) {
        if (r.size() == 0 || r.size() < 2) { //this is to validate r.get(0) and r.get(1) name and openhours
            //System.out.println("Error: CSV record is not valid");
            return Option.none();
        }
        Map<DayOfWeek, Restaurant.OpenHours> weeklyOpenHoursMap = parseOpenHour(r.get(1));
        if (weeklyOpenHoursMap == null || weeklyOpenHoursMap.isEmpty()) {
            //System.out.println("Error: Failed to parse the records for weekly data");
            return Option.none();
        }
        return Option.some(new Restaurant(r.get(0), weeklyOpenHoursMap));
    }

    /**
     * TODO: Implement me, This is a useful helper method
     */
    public static Map<DayOfWeek, Restaurant.OpenHours> parseOpenHour(final String openhoursString) {
        if (openhoursString == null || openhoursString.isEmpty()) {
           // System.out.println("Error: Can't parse the csv record, empty string");
            return null;
        }
        final Map<DayOfWeek, Restaurant.OpenHours> weeklyOpenHoursMap = new LinkedHashMap<>();
        final String[] openDays = openhoursString.trim().split(";");
        for (String day : openDays) {
            String[] dayAndHours = day.trim().split("\\|");
            String[] startAndEndTime = dayAndHours[1].trim().split("-");
            LocalTime startTime = LocalTime.parse(startAndEndTime[0]); //parse start time
            LocalTime endTime = LocalTime.parse(startAndEndTime[1]);  //parse end time
            Restaurant.OpenHours openHours = new Restaurant.OpenHours(startTime, endTime);
            if (!openHours.getStartTime().equals(openHours.getEndTime())) {
                String[] multiDaysHours = dayAndHours[0].split(",");
                for (String dayHour : multiDaysHours) {
                    Option<DayOfWeek> dayOfWeek = getDayOfWeek(dayHour); //to return the whole dayOfWeek String. from "Mon" to "MONDAY"
                    if (dayOfWeek.isDefined()) {
                        DayOfWeek ofWeek = dayOfWeek.get();
                        weeklyOpenHoursMap.put(ofWeek, openHours);
                    }
                }
            }
        }
        return weeklyOpenHoursMap;
    }

    public CSVRestaurantService() throws IOException {
        this.restaurantList = ResourceLoader.parseOptionCSV("rest_hours.csv", CSVRestaurantService::parse);
    }

    public List<Restaurant> getAllRestaurants() {
        return restaurantList;
    }

    /**
     *
     *  TODO: Implement me
     *
     *  A restaurant is considered open when the OpenHours for the dayOfWeek has:
     *
     *  startTime < localTime   && localTime < endTime
     *
     *  If the open hours are 16:00-20:00  Then
     *
     *  15:59 open = false
     *  16:00 open = false
     *  16:01 open = true
     *  20:00 open = false
     *
     *
     *  If the startTime endTime spans midnight, then consider an endTime up until 5:00 to be part of same DayOfWeek as the startTime
     *
     *  SATURDAY, OpenHours are: 20:00-04:00    SUNDAY, OpenHours are: 10:00-14:00
     *
     *  (SATURDAY, 03:00) => open = false
     *  (SUNDAY, 03:00)   => open = true
     *  (SUNDAY, 05:00)   => open = false
     *
     */
    public List<Restaurant> getOpenRestaurants(final DayOfWeek dayOfWeek, final LocalTime localTime) {
        final List<Restaurant> restaurants = getAllRestaurants();
        if (restaurants == null || restaurants.isEmpty()) {
            return Collections.emptyList();
        }
        return restaurants.stream().filter(restaurant -> isRestaurantOpen(dayOfWeek, localTime, restaurant)).collect(Collectors.toList());
    }

    private boolean isRestaurantOpen(final DayOfWeek dayOfWeek, final LocalTime localTime, final Restaurant restaurant) {
        if (!restaurant.getOpenHoursMap().containsKey(dayOfWeek)) {
            //System.out.println("Error: restaurant is not open");
            return false;
        }
        Restaurant.OpenHours openHours = restaurant.getOpenHoursMap().get(dayOfWeek);

        if (localTime.equals(LocalTime.MIDNIGHT) || localTime.isAfter(LocalTime.MIDNIGHT)) {
            Restaurant.OpenHours openHoursLastDay = restaurant.getOpenHoursMap().get(dayOfWeek.minus(1));
            if (openHoursLastDay != null && openHoursLastDay.spansMidnight()) {
                //System.out.println("Spans midnight");
                if (openHoursLastDay.getEndTime().isAfter(localTime)) {
                    return true;
                }
            }
        }
        return openHours.getStartTime().isBefore(localTime) && openHours.getEndTime().isAfter(localTime);
    }

    public List<Restaurant> getOpenRestaurantsForLocalDateTime(final LocalDateTime localDateTime) {
        return getOpenRestaurants(localDateTime.getDayOfWeek(), localDateTime.toLocalTime());
    }

    public static Option<DayOfWeek> getDayOfWeek(final String s) {

        if (s.equals("Mon")) {
            return Option.some(DayOfWeek.MONDAY);
        } else if (s.equals("Tue")) {
            return Option.some(DayOfWeek.TUESDAY);
        } else if (s.equals("Wed")) {
            return Option.some(DayOfWeek.WEDNESDAY);
        } else if (s.equals("Thu")) {
            return Option.some(DayOfWeek.THURSDAY);
         } else if (s.equals("Fri")) {
            return Option.some(DayOfWeek.FRIDAY);
        } else if (s.equals("Sat")) {
            return Option.some(DayOfWeek.SATURDAY);
        } else if (s.equals("Sun")) {
            return Option.some(DayOfWeek.SUNDAY);
        } else {
            return Option.none();
        }
    }

    public static <S, T> Function<S, Stream<T>> toStreamFunc(final Function<S, Option<T>> function) {
        return s -> function.apply(s).fold(() -> Stream.empty(), t -> Stream.of(t));
    }

    /**
     * NOTE: Useful for generating the data.sql file in src/main/resources/
     */
    public static void main (final String [] args) throws IOException {
        final CSVRestaurantService csvRestaurantService = new CSVRestaurantService();

        csvRestaurantService.getAllRestaurants().forEach(restaurant -> {

            final String name = restaurant.getName().replaceAll("'", "''");

            System.out.println("INSERT INTO restaurants (name) values ('" + name  + "');");

            restaurant.getOpenHoursMap().entrySet().forEach(entry -> {
                final DayOfWeek dayOfWeek = entry.getKey();
                final LocalTime startTime = entry.getValue().getStartTime();
                final LocalTime endTime   = entry.getValue().getEndTime();

                System.out.println("INSERT INTO open_hours (restaurant_id, day_of_week, start_time_minute_of_day, end_time_minute_of_day) select id, '" + dayOfWeek.toString() + "', " + startTime.get(ChronoField.MINUTE_OF_DAY) + ", " + endTime.get(ChronoField.MINUTE_OF_DAY) + " from restaurants where name = '" + name + "';");

            });
        });
    }
}
