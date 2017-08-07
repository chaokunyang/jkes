```sql
CREATE DATABASE jkes DEFAULT CHARACTER SET 'utf8' DEFAULT COLLATE 'utf8_unicode_ci';
USE jkes;
grant all privileges on *.* to 'jkes'@'localhost' identified by '123456'; 
grant all privileges on *.* to 'jkes'@'127.0.0.1' identified by '123456'; 
grant all privileges on *.* to 'jkes'@'::1' identified by '123456'; 
flush privileges;
grant all privileges on *.* to 'jkes'@'yangck-pc' identified by '123456';
flush privileges;
```