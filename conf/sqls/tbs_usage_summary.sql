select
       total.tablespace_name,
       round(total.GB, 2) as Total_GB,
       round(total.GB - free.GB, 2) as Used_GB,
       round(free.GB,2) as free_GB,
       round((1 - free.GB / total.GB) * 100, 2) as Used_Pct
  from (select tablespace_name, sum(bytes) / 1024 / 1024 /1024 as GB
          from dba_free_space
         group by tablespace_name) free,
       (select tablespace_name, sum(bytes) / 1024 / 1024 /1024 as GB
          from dba_data_files
         group by tablespace_name) total
where free.tablespace_name = total.tablespace_name
order by Total_GB desc