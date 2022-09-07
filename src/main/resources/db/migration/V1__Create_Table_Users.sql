CREATE TABLE user (
                      chat_id int(20)
                          primary key,
                      first_name    varchar(255),
                      last_name     varchar(255),
                      registerDate timestamp,
                      username      varchar(255)
);