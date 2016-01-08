SELECT   wait_class,
         total_waits,
         ROUND (100 * (total_waits / sum_waits), 2) pct_waits,
         ROUND ((time_waited / 100), 2) time_waited_secs,
         ROUND (100 * (time_waited / sum_time), 2) pct_time
    FROM (SELECT wait_class,
                 total_waits,
                 time_waited
            FROM v$system_wait_class
           WHERE wait_class != 'Idle'),
         (SELECT SUM (total_waits) sum_waits,
                 SUM (time_waited) sum_time
            FROM v$system_wait_class
           WHERE wait_class != 'Idle')
ORDER BY 5 DESC;