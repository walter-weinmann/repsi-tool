SELECT   CASE metric_name
            WHEN 'SQL Service Response Time'
               THEN 'SQL Service Response Time (secs)'
            WHEN 'Response Time Per Txn'
               THEN 'Response Time Per Txn (secs)'
            ELSE metric_name
         END metric_name,
         CASE metric_name
            WHEN 'SQL Service Response Time'
               THEN ROUND ((minval / 100), 2)
            WHEN 'Response Time Per Txn'
               THEN ROUND ((minval / 100), 2)
            ELSE minval
         END mininum,
         CASE metric_name
            WHEN 'SQL Service Response Time'
               THEN ROUND ((maxval / 100), 2)
            WHEN 'Response Time Per Txn'
               THEN ROUND ((maxval / 100), 2)
            ELSE maxval
         END maximum,
         CASE metric_name
            WHEN 'SQL Service Response Time'
               THEN ROUND ((average / 100), 2)
            WHEN 'Response Time Per Txn'
               THEN ROUND ((average / 100), 2)
            ELSE average
         END average
    FROM SYS.v_$sysmetric_summary
   WHERE metric_name IN
            ('CPU Usage Per Sec',
             'CPU Usage Per Txn',
             'Database CPU Time Ratio',
             'Database Wait Time Ratio',
             'Executions Per Sec',
             'Executions Per Txn',
             'Response Time Per Txn',
             'SQL Service Response Time',
             'User Transaction Per Sec'
            )
ORDER BY 1