 SQL Functions in Slick 
==========================

The following functions should be rewritten from SQL to Slick.

## Essential Functions

Essential functions currently cannot be written in Scala but only raw SQL. These functions should be addressed first.

| SQL Function                        | Scala Function           | Status             | 
| ----------------------------------- | ---------------------    | -------------------| 
| REGEXP                              | regexMatch               | Completed          |
| TimeStamp > Now () - Interval       | isWithinPast             | Completed          |
| STDDEV                              | std                      | Completed          |
| ROUND (number, decimals)            | round (number, decimal)  | Completed          |
| UNIX.TIMESTAMP (Timestamp)          | unixTimestamp (timestamp)| Completed          |
| subdate (current_date(), INTERVAL)  | subdate (date, interval) | Completed          |
| DATE (TimeStamp)                    | date                     | Completed          |
| YEAR (TimeStamp)                    | year                     | Completed          |
| NOW                                 | now                      | Completed          |



## Slick Extensions

We already have the version of the function that we need for Vulcan.
However, we may need cases for parameter overloading.

| SQL Function                        | Scala Function           | Status             | 
| ----------------------------------- | ---------------------    | -------------------| 
| subdate (current_date(), DAYS)      | subdate (date, amount)   | Completed          |
| UNIX.TIMESTAMP                      | unixTimestamp            | Completed          |
| UNIX.TIMESTAMP (Date)               |                          | TODO               |
| UNIX.TIMESTAMP (DateTime)           |                          | TODO               |
| ROUND                               | round                    | Completed          |
