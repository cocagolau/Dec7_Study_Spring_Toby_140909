
create user spring;
# select * from user where user = 'spring';
# delete from user where user = 'spring';

grant all privileges on springbook.* to spring@localhost identified by 'book';
grant all privileges on springbooktest.* to spring@localhost identified by 'book';

flush privileges;