/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.server_tic_tac_toe;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.derby.jdbc.ClientDriver;



/**
 *
 * @author LENOVO
 */
public class DOA {
    private static Connection con; 
    static{
        try {
            DriverManager.registerDriver(new ClientDriver());
             con=DriverManager.getConnection("jdbc:derby://localhost:1527/UserDetails", "root", "root");
        } catch (SQLException ex) {
            System.getLogger(DOA.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    
}
