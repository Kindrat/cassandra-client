CREATE KEYSPACE test WITH REPLICATION = {
'class' : 'SimpleStrategy',
'replication_factor' : 1
};;
CREATE TABLE test.data (
counter int PRIMARY KEY,
time timestamp,
data text);
