SELECT   CASE db_stat_name
            WHEN 'parse time elapsed'
               THEN 'soft parse time'
            ELSE db_stat_name
         END db_stat_name,
         CASE db_stat_name
            WHEN 'sql execute elapsed time'
               THEN time_secs - plsql_time
            WHEN 'parse time elapsed'
               THEN time_secs - hard_parse_time
            ELSE time_secs
         END time_secs,
         CASE db_stat_name
            WHEN 'sql execute elapsed time'
               THEN ROUND (100 * (time_secs - plsql_time) / db_time, 2)
            WHEN 'parse time elapsed'
               THEN ROUND (100 * (time_secs - hard_parse_time) / db_time, 2)
            ELSE ROUND (100 * time_secs / db_time, 2)
         END pct_time
    FROM (SELECT stat_name db_stat_name,
                 ROUND ((VALUE / 1000000), 3) time_secs
            FROM SYS.v_$sys_time_model
           WHERE stat_name NOT IN
                    ('DB time',
                     'background elapsed time',
                     'background cpu time',
                     'DB CPU'
                    )),
         (SELECT ROUND ((VALUE / 1000000), 3) db_time
            FROM SYS.v_$sys_time_model
           WHERE stat_name = 'DB time'),
         (SELECT ROUND ((VALUE / 1000000), 3) plsql_time
            FROM SYS.v_$sys_time_model
           WHERE stat_name = 'PL/SQL execution elapsed time'),
         (SELECT ROUND ((VALUE / 1000000), 3) hard_parse_time
            FROM SYS.v_$sys_time_model
           WHERE stat_name = 'hard parse elapsed time')
ORDER BY 2 DESC;