SELECT *
  FROM (SELECT   sql_text,
                 sql_id,
                 elapsed_time,
                 cpu_time,
                 user_io_wait_time
            FROM SYS.v_$sqlarea
        ORDER BY 5 DESC)
 WHERE ROWNUM < 6;