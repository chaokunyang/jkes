```sql
CREATE DATABASE s_inte_test DEFAULT CHARACTER SET 'utf8' DEFAULT COLLATE 'utf8_unicode_ci';
USE s_inte_test;
grant all privileges on *.* to 's_inte_test'@'localhost' identified by '123456'; 
grant all privileges on *.* to 's_inte_test'@'127.0.0.1' identified by '123456'; 
grant all privileges on *.* to 's_inte_test'@'::1' identified by '123456'; 
flush privileges;
grant all privileges on *.* to 's_inte_test'@'yangck-pc' identified by '123456';
flush privileges;
```