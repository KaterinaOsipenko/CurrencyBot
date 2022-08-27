package com.spring.currency.bot.model;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity(name = "Users")
@Getter
@Setter
@ToString
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long chatId;

    private String username;

    private String firstName;

    private String lastName;

    private Timestamp registerDate;
}
