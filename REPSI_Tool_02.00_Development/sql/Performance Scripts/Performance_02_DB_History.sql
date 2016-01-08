SELECT   end_time,
         VALUE
    FROM SYS.v_$sysmetric_history
   WHERE metric_name IN
                      ('Database CPU Time Ratio')
ORDER BY 1