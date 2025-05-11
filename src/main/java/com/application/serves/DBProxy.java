package com.application.serves;

import com.application.Main;
import com.application.Model.Phrase;
import com.application.Model.User;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

public class DBProxy {
    private static final String URL = "jdbc:sqlite:database.db";
    private static Connection conn;

    public static Connection connect() {
        if (conn != null) {
            return conn;
        }
        try {
            conn = DriverManager.getConnection(URL);
            System.out.println("Соединение с SQLite установлено.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static void createUserTable() throws SQLException {
        conn = connect();
        String sql = "CREATE TABLE IF NOT EXISTS Users (id INT, name VARCHAR(100))";
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate(sql);
            System.out.println("Users table созданна");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addUser(User user) throws SQLException {
        conn = connect();
        if (getUser(user.getId()) != null) {
            Main.log("Этот пользователь уже добавлен");
        } else {
            String sql = String.format("INSERT INTO Users (id, name) VALUES (%d, '%s')", user.getId(), user.getName());
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate(sql);
                System.out.println(user);
            }
        }
    }

    public static User getUser(long id) throws SQLException {
        conn = connect();
        String sql = "SELECT id, name FROM Users WHERE id = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new User(resultSet.getInt("id"), resultSet.getString("name"));
                }
            }
        }
        return null;
    }

    public static List<User> getUsers() throws SQLException {
        conn = connect();
        String sql = "SELECT id, name FROM Users";
        List<User> users = new ArrayList<>();
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(new User(resultSet.getInt("id"), resultSet.getString("name")));
                }
            }
        }
        return users;
    }

    public static void createPhrasesTable() throws SQLException {
        conn = connect();
        String sql = "CREATE TABLE IF NOT EXISTS Phrases (id INTEGER PRIMARY KEY AUTOINCREMENT, text TEXT UNIQUE)";
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate(sql);
            System.out.println("Phrases table создана");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int insertPhrase(String text) throws SQLException {
        conn = connect();
        String sql = "INSERT OR IGNORE INTO Phrases (text) VALUES (?)";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, text);
            statement.executeUpdate();
        }

        sql = "SELECT id FROM Phrases WHERE text = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, text);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.getInt("id");
            }
        }
    }

    public static Phrase getPhrase(int id) throws SQLException {
        conn = connect();
        String sql = "SELECT phrase FROM Phrases WHERE id = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Phrase phrase = new Phrase(
                            resultSet.getString("phrase")
                    );
                    return phrase;
                }
            }
        }
        return null;
    }

    //reduced
    public static List<Phrase> readAllPhrases() {
        List<Phrase> phrases = new ArrayList<>();

        conn = connect();

        String sql = "SELECT id, tag, phrase FROM Phrases";

        try (PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String tag = resultSet.getString("tag");
                String phrase = resultSet.getString("phrase");

                phrases.add(new Phrase(id, phrase));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return phrases;
    }

    public static void createSentPhrasesTable() throws SQLException {
        conn = connect();
        String sql = "CREATE TABLE IF NOT EXISTS sentPhrases (userId INT, phraseId INT)";
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate(sql);
            Main.log("sentPhrases table создана");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean addSentPhrase(User user, Phrase phrase) {
        conn = connect();
        String sql = "INSERT INTO sentPhrases (userId, phraseId) VALUES(?, ?)";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, user.getId());
            statement.setInt(2, phrase.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public static void resetSendedPhrases(User user, List<Phrase> phraseList) {
        if (phraseList == null || phraseList.isEmpty()) return;

        conn = connect();

        StringBuilder sql = new StringBuilder("DELETE FROM sentPhrases WHERE userId = ? AND phraseId IN (");
        for (int i = 0; i < phraseList.size(); i++) {
            sql.append("?");
            if (i < phraseList.size() - 1) {
                sql.append(", ");
            }
        }
        sql.append(")");

        try (PreparedStatement statement = conn.prepareStatement(sql.toString())) {
            statement.setLong(1, user.getId());
            for (int i = 0; i < phraseList.size(); i++) {
                statement.setInt(i + 2, phraseList.get(i).getId());
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static List<Integer> getSentPhraseId(User user) {
        conn = connect();
        List<Integer> ids = new ArrayList<>();
        String query = "SELECT p.id " +
                        "FROM sentPhrases sp " +
                        "JOIN Phrases p ON sp.phraseID = p.id " +
                        "WHERE sp.userId = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, user.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    public static void createBroadcastTable() {
        conn = connect();
        String sql = "CREATE TABLE IF NOT EXISTS broadcasts (date TEXT PRIMARY KEY)";
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate(sql);
            Main.log("broadcasts table создана");
        } catch (SQLException e) {
            Main.log(e.getMessage());
        }
    }

    public static void addBroadcastMark(LocalDate date) {
        conn = connect();
        String sql = "INSERT OR IGNORE INTO broadcasts (date) VALUES(?)";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, date.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            Main.log(e.getMessage());
        }
    }

    public static boolean getBroadcastMark(LocalDate date) throws SQLException {
        conn = connect();
        String sql = "SELECT 1 FROM broadcasts WHERE date = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, date.toString());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            Main.log(e.getMessage());
        }
        return false;
    }

    public static void crateAllTables() throws SQLException {
        createUserTable();
        createPhrasesTable();
        createSentPhrasesTable();
        createBroadcastTable();
    }
}
