# Coding Project


### Fork the project

Fork this repository, and update the README.md link to Gitpod below to use your Github fork public link.

[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/shridhar-hitnalli/hologram-coding-project)

### Overview of the coding assignment

This is a project to test your algorithms, Java, and SQL.  You will be writing some utility functions, parsing a csv file, writing some logic, reading a SQL database (H2) schema, and writing some SQL.

## Algorithms

Read over this file: src/main/java/com/hologramsciences/Algorithms.java

1. Implement cartesianProductForLists

2. Implement countNumWaysMakeChange

## CSVRestaurantService

Read over this file:

src/main/resource/rest_hours.csv

1. Implement parse

2. Implement getOpenRestaurants(final DayOfWeek dayOfWeek, final LocalTime localTime)


## SQLRestaurantService

Read over these files:

Schema: src/main/resource/schema.sql
Data:   src/main/resource/data.sql

1. Implement getOpenRestaurants
2. Implement getRestaurantsWithMenuOfSizeGreaterThanOrEqualTo

## JooqRestaurantService (Bonus)

As a bonus you can show your skills with learning a new library.

Jooq (https://www.jooq.org) is a Java library that makes writing SQL easier and more typesafe.
You will re-implement the methods in SQLRestaurantService, but using the Jooq DSL (Domain specific language.

1. Implement getOpenRestaurants
2. Implement getRestaurantsWithMenuOfSizeGreaterThanOrEqualTo

## Testing

You can run the tests using Intellij/Eclipse or run this from the command line:

```
mvn test
```

## Submitting your solution

Make sure to update README.md link to open Gitpod to use your Github fork public link.

Reply to email with a link to your Github fork.
