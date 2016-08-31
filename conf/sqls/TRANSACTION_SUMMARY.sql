select  (select NAME from v$database) dbname,
       sysdate record_time,
       instance_number inst_id,
       round(avg(average)) TPS,
       systimestamp aaa
  from DBA_HIST_SYSMETRIC_SUMMARY
 where metric_unit = 'Transactions Per Second'
   and begin_time >= sysdate - 2 / 24
   and begin_time < sysdate
 group by instance_number