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
| ROUND                               |                       | TODO               |
| AS                                  |                       | TODO               |
| UNIX.TIMESTAMP                      |                       | TODO               |
| subdate (current_date(), INTERVAL)  |                       | TODO               |
| DATE (TimeStamp)                    | DATE                  | Completed          |
| YEAR (TimeStamp)                    | YEAR                  | Completed          |
| NOW                                 | NOW                   | Completed          |
| HAVING SUM                          |                       | TODO               |
| DISTINCT                            |                       | TODO               |


## Slick Extensions

Although these functions can be easily written in Scala, there is technically no passing parameter function available. As a result, these should be addressed later on if necessary.

| SQL Function                        | Scala Function        | Status             | 
| ----------------------------------- | --------------------- | -------------------| 
| LEFT JOIN                           |                       | TODO               |
| JOIN ON                             |                       | TODO               |
