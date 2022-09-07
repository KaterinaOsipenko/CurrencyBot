CREATE TABLE users (
                      chat_id serial
                          primary key,
                      first_name    varchar(255),
                      last_name     varchar(255),
                      registerDate timestamp,
                      username      varchar(255)
);