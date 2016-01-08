SELECT   TO_CHAR (a.end_time, 'DD-MON-YYYY HH:MI:SS') end_time,
         b.wait_class,
         ROUND ((a.time_waited / 100), 2) time_waited
    FROM SYS.v_$waitclassmetric_history a,
         SYS.v_$system_wait_class b
   WHERE a.wait_class# = b.wait_class# AND b.wait_class != 'Idle'
ORDER BY 1,
         2;