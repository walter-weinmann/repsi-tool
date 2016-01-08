SELECT   sess_id,
         username,
         program,
         wait_event,
         sess_time,
         ROUND (100 * (sess_time / total_time), 2) pct_time_waited
    FROM (SELECT   a.session_id sess_id,
                   DECODE (session_type,
                           'background', session_type,
                           c.username
                          ) username,
                   a.program program,
                   b.NAME wait_event,
                   SUM (a.time_waited) sess_time
              FROM SYS.v_$active_session_history a,
                   SYS.v_$event_name b,
                   SYS.dba_users c
             WHERE a.event# = b.event#
               AND a.user_id = c.user_id
               AND sample_time > '12-OKT-06 00:00:00'
               AND sample_time < '31-DEZ-06 12:00:00'
               AND b.wait_class = 'User I/O'
          GROUP BY a.session_id,
                   DECODE (session_type,
                           'background', session_type,
                           c.username
                          ),
                   a.program,
                   b.NAME),
         (SELECT SUM (a.time_waited) total_time
            FROM SYS.v_$active_session_history a,
                 SYS.v_$event_name b
           WHERE a.event# = b.event#
             AND sample_time > '12-OKT-06 00:00:00'
             AND sample_time < '31-DEZ-06 12:00:00'
             AND b.wait_class = 'User I/O')
ORDER BY 6 DESC;