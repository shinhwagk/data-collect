select  (select NAME from v$database) dbname,
       sysdate record_time,
       inst_id,
       value load
from gv$osstat t where stat_name='LOAD'