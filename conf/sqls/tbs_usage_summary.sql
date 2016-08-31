select (select NAME from v$database) dbname,
       sysdate record_time,
       total.tablespace_name,
       round(total.MB, 2) as Total_MB,
       round(total.MB - free.MB, 2) as Used_MB,
       round(free.MB,2) as free_MB,
       round((1 - free.MB / total.MB) * 100, 2) as Used_Pct
  from (select tablespace_name, sum(bytes) / 1024 / 1024 as MB
          from dba_free_space
         group by tablespace_name) free,
       (select tablespace_name, sum(bytes) / 1024 / 1024 as MB
          from dba_data_files
         group by tablespace_name) total
where free.tablespace_name = total.tablespace_name