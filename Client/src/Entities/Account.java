package Entities;

import java.io.*;
/**
 * Класс, представляющий аккаунт пользователя
 */
public class Account implements Serializable{
    public String login;
    public String password;

    public Account(String login, String password) {
        this.login = login;
        this.password = password;
    }

}
