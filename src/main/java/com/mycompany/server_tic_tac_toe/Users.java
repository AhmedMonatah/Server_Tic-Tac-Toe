/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.server_tic_tac_toe;

/**
 *
 * @author LENOVO
 */
public class Users {
    private String userName;
    private String password;
    private String condfirmPassword;
    
    public Users(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public Users(String userName, String password, String condfirmPassword) {
        this.userName = userName;
        this.password = password;
        this.condfirmPassword = condfirmPassword;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCondfirmPassword(String condfirmPassword) {
        this.condfirmPassword = condfirmPassword;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getCondfirmPassword() {
        return condfirmPassword;
    }
   
}
