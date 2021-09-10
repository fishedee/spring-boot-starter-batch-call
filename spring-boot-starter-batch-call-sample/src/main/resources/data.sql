drop table if exists user;
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

drop table if exists car;
create table car(
     id integer not null,
     driverId integer not null,
     name varchar(128) not null,
     color varchar(128) not null,
     primary key(id)
);

insert into car(id,driverId,name,color)values
(20001,10001,'车1','red'),
(20002,10002,'车2','green'),
(20003,10002,'车3','blue');

drop table if exists category;
create table category(
    id integer not null,
    name varchar(128) not null,
    parentId integer not null,
    primary key(id)
);

insert into category(id,name,parentId)values
(30001,'分类1',0),
(30002,'分类2',0),
(30003,'分类3',30001),
(30004,'分类4',30001),
(30005,'分类5',30003),
(30006,'分类6',30002),
(30007,'分类7',30006),
(30008,'分类8',30007);