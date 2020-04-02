package me.infinityz.stats.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.bukkit.Bukkit;

import net.md_5.bungee.api.ChatColor;

/**
 * MySQL
 */
public class MySQL {

    public String username, password, database, host;
    public int port;
    public Connection con;
    
    public MySQL(String username, String password, String database, String host, int port){
        this.username = username;
        this.password = password;
        this.database = database;
        this.host = host;
        this.port = port;
    }
    
    public MySQL(String username, String password, String database, String host){
        this(username, password, database, host, 3306);
    }
    
    public void connect() throws Exception{
        if (!isConnected()) {            
            final Properties properties = new Properties();
            properties.setProperty("user", username);
            properties.setProperty("password", password);
            properties.setProperty("autoReconnect", "true");
            properties.setProperty("verifyServerCertificate", "false");
            properties.setProperty("useSSL", "false");
            properties.setProperty("requireSSL", "false");
            con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, properties);
            update("CREATE TABLE IF NOT EXISTS SurvivalUniversePlayers (UUID VARCHAR(100) NOT NULL PRIMARY KEY, allies TEXT)");
            update("CREATE TABLE IF NOT EXISTS SU_BLOCK_LOG (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, UUID VARCHAR(100) NOT NULL, blockType VARCHAR(32) NOT NULL, eventType VARCHAR(32) NOT NULL, location VARCHAR(64) NOT NULL, cancelled BOOLEAN NOT NULL, date VARCHAR(64) NOT NULL)");
        }
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConnected() {
        return con != null;
    }

    public void update(final String qry) {
        if (isConnected()) {
            try {
                con.createStatement().executeUpdate(qry);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public ResultSet getResult(final String qry) {
        if (isConnected()) {
            try {
                return con.createStatement().executeQuery(qry);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getFirstString(final ResultSet rs, final int l, final String re, final int t) {
        try {
            while (rs.next()) {
                if (rs.getString(l).equalsIgnoreCase(re)) {
                    return rs.getString(t);
                }
            }
        } catch (Exception ex) {
        }
        return null;
    }

    public int getFirstInt(final ResultSet rs, final int l, final String re, final int t) {
        try {
            while (rs.next()) {
                if (rs.getString(l).equalsIgnoreCase(re)) {
                    return rs.getInt(t);
                }
            }
        } catch (Exception ex) {
        }
        return 0;
    }

    public Connection getConnection() {
        return con;
    }

    public void closeRessources(final ResultSet rs, final PreparedStatement st) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ex) {
            }
        }
        if (st != null) {
            try {
                st.close();
            } catch (SQLException ex2) {
            }
        }
    }

    public void close(final PreparedStatement st, final ResultSet rs) {
        try {
            if (st != null) {
                st.close();
            }
            if (rs != null) {
                rs.close();
            }
        } catch (Exception ex) {
        }
    }
    public  void executeUpdate(final String statement) {
        try {
            final PreparedStatement st = con.prepareStatement(statement);
            st.executeUpdate();
            close(st, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void executeUpdate(final PreparedStatement statement) {
        try {
            statement.executeUpdate();
            close(statement, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResultSet executeQuery(final String statement) {
        try {
            final PreparedStatement st = con.prepareStatement(statement);
            return st.executeQuery();
        } catch (Exception ex) {
            return null;
        }
    }

    public ResultSet executeQuery(final PreparedStatement statement) {
        try {
            return statement.executeQuery();
        } catch (Exception ex) {
            return null;
        }
    }

    public ResultSet query(final String query) throws SQLException {
        final Statement stmt = con.createStatement();
        try {
            stmt.executeQuery(query);
            return stmt.getResultSet();
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + e.getMessage());
            return null;
        }
    }


    
}