SELECT metric_name,
       VALUE
  FROM SYS.v_$sysmetric
 WHERE metric_name IN ('Database CPU Time Ratio', 'Database Wait Time Ratio')
   AND intsize_csec = (SELECT MAX (intsize_csec)
                         FROM SYS.v_$sysmetric)