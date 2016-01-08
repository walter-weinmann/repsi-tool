SELECT   a.SID,
         b.username,
         a.wait_class,
         a.total_waits,
         ROUND ((a.time_waited / 100), 2) time_waited_secs
    FROM SYS.v_$session_wait_class a,
         SYS.v_$session b
   WHERE b.SID = a.SID AND b.username IS NOT NULL AND a.wait_class != 'Idle'
ORDER BY 5 DESC;