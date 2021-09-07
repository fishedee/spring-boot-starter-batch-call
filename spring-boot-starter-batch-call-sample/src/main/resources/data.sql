create table user(
    id integer not null,
    name varchar(128) not null,
    level integer not null,
    primary key(id)
);

insert into user(id,name,level)values
(10001,'fish',12),
(10002,'cat',34),
(10003,'dog',56);