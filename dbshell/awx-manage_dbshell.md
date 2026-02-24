# AAP Postgres SQL queries for performance

## UI / API slowness diagnosis

#### Use profile_sql to capture slow queries

Login to a Controller node, and run: 

```
awx-manage profile_sql --threshold 2 --minutes 5
```

This will capture queries taking longer than 2 seconds to complete, and profile for 5 minutes. During this time, try to duplicate the UI / API slowness previously observed, to capture the problematic queries. 

## Database investigation

#### Access the database shell

Login to a Controller node, and run:

```
awx-manage dbshell
```

From the dbshell environment, you can run SQL queries against the database. 

#### Check the database size:

```
SELECT current_database(), pg_size_pretty(pg_database_size(current_database()));
```

#### Size of each table (sorted):

```
SELECT
  schema_name,
  relname,
  pg_size_pretty(table_size) AS size,
  table_size
FROM (
       SELECT
         pg_catalog.pg_namespace.nspname           AS schema_name,
         relname,
         pg_relation_size(pg_catalog.pg_class.oid) AS table_size

       FROM pg_catalog.pg_class
         JOIN pg_catalog.pg_namespace ON relnamespace = pg_catalog.pg_namespace.oid
     ) t
WHERE schema_name NOT LIKE 'pg_%'
ORDER BY table_size DESC;
```

#### Ratio of live and dead tuples:

```
SELECT
  relname,
  n_live_tup,
  n_dead_tup,
  CASE n_dead_tup WHEN 0 THEN 0 ELSE round(n_dead_tup*100/(n_live_tup+n_dead_tup) ,2) END AS ratio
FROM
  pg_stat_user_tables
ORDER BY
  n_dead_tup DESC;
```

#### Find valuable size information for each table in database:

```
SELECT table_name, row_estimate
    , pg_size_pretty(total_bytes) AS total
    , pg_size_pretty(index_bytes) AS INDEX
    , pg_size_pretty(toast_bytes) AS toast
    , pg_size_pretty(table_bytes) AS TABLE
FROM (
  SELECT *, total_bytes-index_bytes-COALESCE(toast_bytes,0) AS table_bytes FROM (
      SELECT c.oid,nspname AS table_schema
              , relname AS table_name
              , c.reltuples AS row_estimate
              , pg_total_relation_size(c.oid) AS total_bytes
              , pg_indexes_size(c.oid) AS index_bytes
              , pg_total_relation_size(reltoastrelid) AS toast_bytes
          FROM pg_class c
          LEFT JOIN pg_namespace n ON n.oid = c.relnamespace
          WHERE relkind = 'r' 
          ORDER BY row_estimate desc
  ) a
) a WHERE table_schema = 'public';
```

#### Find Top 100 hosts by ansible_facts size:

```
SELECT 
  i.name inventory_name, 
  i.total_hosts, 
  i.hosts_with_active_failures, 
  h.name host_name, 
  h.id host_id, 
  pg_column_size(h.ansible_facts) ansible_facts_size 
FROM
  main_inventory i INNER JOIN main_host h ON i.id = h.inventory_id 
ORDER BY 
  pg_column_size(ansible_facts) DESC 
LIMIT 100;
```

#### Get postgres settings involved with tuning:

```
SELECT
  s.name,
  s.setting,
  s.unit
FROM
  pg_settings AS s
WHERE
  s.name IN ('max_connections', 'shared_buffers', 'work_mem', 'maintenance_work_mem', 'autovacuum');
```

#### Get targeted index info about a table, e.g., main_host

```
SELECT
    i.indexrelname as index_name,
    i.idx_scan as times_used,
    i.idx_tup_read as tuples_read,
    i.idx_tup_fetch as tuples_fetched,
    pg_size_pretty(pg_relation_size(i.indexrelid)) as index_size,
    pg_size_pretty(pg_relation_size(i.relid)) as table_size,
    CASE
        WHEN i.idx_scan = 0 THEN 'UNUSED'
        WHEN i.idx_scan < 100 THEN 'Low usage'
        WHEN i.idx_scan < 1000 THEN 'Medium usage'
        ELSE 'High usage'
    END as usage_status,
    round(100.0 * i.idx_scan / NULLIF(
        (SELECT sum(idx_scan) FROM pg_stat_user_indexes WHERE relname = 'main_host'), 0
    ), 2) as pct_of_total_scans
FROM pg_stat_user_indexes i
WHERE i.relname = 'main_host'
ORDER BY i.idx_scan DESC; 
```
