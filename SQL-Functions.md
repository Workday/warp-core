 SQL Functions in Slick 
==========================

The following functions should be rewritten from SQL to Slick.

## Essential Functions

Essential functions currently cannot be written in Scala but only raw SQL. These functions should be addressed first.

| SQL Function                        | Scala Function        | Status             | 
| ----------------------------------- | --------------------- | -------------------| 
| REGEXP                              | regexMatch            | Completed          |
| TimeStamp > Now () - Interval       | isWithinPast          | Completed          |
| STDDEV                              | std                   | Completed          |
| ROUND                               | ROUND                 | Completed          |
| AS                                  |                       | TODO               |
| UNIX.TIMESTAMP (Timestamp)          | UNIX.TIMESTAMP        | Completed          |
| subdate (current_date(), INTERVAL)  | subdate               | Completed          |
| DATE (TimeStamp)                    | DATE                  | Completed          |
| YEAR (TimeStamp)                    | YEAR                  | Completed          |
| NOW                                 | NOW                   | Completed          |
| HAVING                              |                       | TODO               |



## Slick Extensions

These functions can be easily implemented in SCALA or we already have the version of the function that we need for Vulcan.
However, we may need cases for parameter overloading.

| SQL Function                        | Scala Function        | Status             | 
| ----------------------------------- | --------------------- | -------------------| 
| LEFT JOIN                           |                       | TODO               |
| JOIN ON                             |                       | TODO               |
| subdate (current_date(), DAYS)      |                       | TODO               |
| UNIX.TIMESTAMP                      |                       | TODO               |
| UNIX.TIMESTAMP (Date)               |                       | TODO               |
| UNIX.TIMESTAMP (DateTime)           |                       | TODO               |
