SELECT event,
       time_waited,
       owner,
       object_name,
       current_file#,
       current_block#
  FROM SYS.v_$active_session_history a,
       SYS.dba_objects b
 WHERE sql_id = 'cvn54b7yz0s8u'
   AND a.current_obj# = b.object_id
   AND time_waited <> 0;