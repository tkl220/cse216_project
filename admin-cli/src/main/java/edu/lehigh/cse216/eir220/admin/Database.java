package edu.lehigh.cse216.eir220.admin;

import com.sendgrid.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class Database {
    /**
     * The connection to the database.  When there is no connection, it should
     * be null.  Otherwise, there is a valid open connection
     */
    private Connection mConnection;

    /**
     * A prepared statement for getting all data in the database
     */
    private PreparedStatement mSelectAll;
    /**
     * A prepared statement for getting all data in the Message database
     */
    private PreparedStatement mSelectAllMsg;
    /**
     * A prepared statement for getting all data in the UserId database
     */
    private PreparedStatement mSelectAllUserId;

    /**
     * A prepared statement for getting one row from the database
     */
    private PreparedStatement mSelectOne;
    /**
     * A prepared statement for getting one row from the Message database
     */
    private PreparedStatement mSelectOneMsg;
    /**
     * A prepared statement for getting one row from the UserId database
     */
    private PreparedStatement mSelectOneUserId;

    /**
     * A prepared statement for deleting a row from the database
     */
    private PreparedStatement mDeleteOne;

    /**
     * A prepared statement for deleting a row from the message database
     */
    private PreparedStatement mDeleteOneMsg;
    /**
     * A prepared statement for deleting a row from the Userid database
     */
    private PreparedStatement mDeleteOneUserId;
    /**
     * A prepared statement for inserting into the database
     */
    private PreparedStatement mInsertOne;
    /**
     * A prepared statement for inserting into the Message database
     */
    private PreparedStatement mInsertOneMsg;
    /**
     * A prepared statement for inserting into the UserId database
     */
    private PreparedStatement mInsertOneUserId;

    /**
     * A prepared statement for updating a single row in the database
     */
    private PreparedStatement mUpdateOne;
    /**
     * A prepared statement for updating a single row in the Message database
     */
    private PreparedStatement mUpdateOneMsg;
    /**
     * A prepared statement for updating a single row in the UserId database
     */
    private PreparedStatement mUpdateOneUserId;

    /**
     * A prepared statement for creating the table in our database
     */
    private PreparedStatement mCreateTable;

    /**
     * A prepared statement for dropping the table in our database
     */
    private PreparedStatement mDropTable;

    /**
     * A prepared statement for creating the message table in our database
     */
    private PreparedStatement mMessageTable;

    /**
     * A prepared statement for dropping the message table in our database
     */
    private PreparedStatement mDropMessageTable;
    /**
     * A prepared statement for creating the UserIDtable in our database
     */
    private PreparedStatement mUserIdTable;
    /**
     * A prepared statement for creating the CommentsTable in our database
     */
    private PreparedStatement mCommentTable;
    private PreparedStatement mDropCommentTable;

    /**
     * A prepared statement for dropping the userId in our database
     */
    private PreparedStatement mDropUserIdTable;
    private PreparedStatement mSalt;
    private PreparedStatement mHash;
    private PreparedStatement mSelectOneUserIdName;
    private PreparedStatement mUpVoteTable;
    private PreparedStatement mDropUpVoteTable;
    private PreparedStatement mDownVoteTable;
    private PreparedStatement mDropDownVoteTable;
    private PreparedStatement mInsertOneComment;


    /**
     * RowData is like a struct in C: we use it to hold data, and we allow 
     * direct access to its fields.  In the context of this Database, RowData 
     * represents the data we'd see in a row.
     * 
     * We make RowData a static class of Database because we don't really want
     * to encourage users to think of RowData as being anything other than an
     * abstract representation of a row of the database.  RowData and the 
     * Database are tightly coupled: if one changes, the other should too.
     */
    public static class RowData {
        /**
         * The ID of this row of the database
         */
        int mId;
        /**
         * The subject stored in this row
         */
        String mSubject;
        /**
         * The message stored in this row
         */
        String mMessage;
        /**
         * The message stored in this row
         */
        int mVote;
        /**
         * The message stored in this row
         */
        String mUserId;
        /**
         * The message stored in this row
         */
        String mEmail;
        /**
         * The message stored in this row
         */
        String mCreated;

        /**
         * Construct a RowData object by providing values for its fields
         */
        public RowData(int id, String subject, String message, int vote, String userId, String email, String created) {
            mId = id;
            mSubject = subject;
            mMessage = message;
            mVote = vote;
            mUserId = userId;
            mEmail = email;
            mCreated = created;
        }
    }

    /**
     * The Database constructor is private: we only create Database objects 
     * through the getDatabase() method.
     */
    private Database() {
    }

    /**
     * Get a fully-configured connection to the database
     * 
     * @return A Database object, or null if we cannot connect properly
     */
    static Database getDatabase() {
        // Create an un-configured Database object

        Database db = new Database();
        // Give the Database object a connection, fail if we cannot get one
        try {
            Class.forName("org.postgresql.Driver");
            URI dbUri = new URI("voxkaockulhndl:0a50f2bf7a854777c38177f775ed6c9056da0b3d592fce95f9e63d88d41124ee@ec2-54-83-50-145.compute-1.amazonaws.com:5432/dk6p6uidak96f");
            String username = "voxkaockulhndl";
            String password = "0a50f2bf7a854777c38177f775ed6c9056da0b3d592fce95f9e63d88d41124ee";
            String dbUrl = "jdbc:postgresql://ec2-54-83-50-145.compute-1.amazonaws.com:5432/dk6p6uidak96f?sslmode=require";
            System.out.println("username: " + username + " password: " + password + " bdbUrl: " + dbUrl);
            Connection conn = DriverManager.getConnection(dbUrl, username, password);
            if (conn == null) {
                System.err.println("Error: DriverManager.getConnection() returned a null object");
                //return null;
            }
            db.mConnection = conn;
        } catch (SQLException e) {
            System.err.println("Error: DriverManager.getConnection() threw a SQLException");
            e.printStackTrace();
            //return null;
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Unable to find postgresql driver");
            //return null;
        } catch (URISyntaxException s) {
            System.out.println("URI Syntax Error");
            //return null;
        }

        // Attempt to create all of our prepared statements.  If any of these 
        // fail, the whole getDatabase() call should failq
        try {
            // NB: we can easily get ourselves in trouble here by typing the
            //     SQL incorrectly.  We really should have things like "tblData"
            //     as constants, and then build the strings for the statements
            //     from those constants.

            // Note: no "IF NOT EXISTS" or "IF EXISTS" checks on table 
            // creation/deletion, so multiple executions will cause an exception
            db.mCreateTable = db.mConnection.prepareStatement(
                    "CREATE TABLE tblData (id SERIAL PRIMARY KEY, subject VARCHAR(50) "
                    + "NOT NULL, message VARCHAR(500) NOT NULL)");
            db.mDropTable = db.mConnection.prepareStatement("DROP TABLE tblData");
            db.mMessageTable = db.mConnection.prepareStatement(
                "CREATE TABLE messageTable(idM SERIAL PRIMARY KEY, message VARCHAR(500) NOT NULL, vote INT NOT NULL, userId VARCHAR(255) NOT NULL, driveURL VARCHAR(255), created TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            db.mDropMessageTable = db.mConnection.prepareStatement("DROP TABLE messageTable");
            db.mUserIdTable = db.mConnection.prepareStatement(
                "CREATE TABLE userIdTable(idU SERIAL PRIMARY KEY, userId VARCHAR(255) NOT NULL UNIQUE, realName VARCHAR(255), email VARCHAR(255),  status VARCHAR(500), salt BYTEA, password VARCHAR(255), logonStatus INT)");
            db.mDropUserIdTable = db.mConnection.prepareStatement("DROP TABLE userIdTable");
            db.mCommentTable = db.mConnection.prepareStatement(
                    "CREATE TABLE commentTable(idC SERIAL PRIMARY KEY, comment VARCHAR(255),  idM INTEGER, userId VARCHAR(255) NOT NULL, driveURL VARCHAR(255), created TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            db.mDropCommentTable = db.mConnection.prepareStatement("DROP TABLE commentTable");
            db.mUpVoteTable = db.mConnection.prepareStatement(
                    "CREATE TABLE upVoteTable(idV SERIAL PRIMARY KEY, userId VARCHAR(255) NOT NULL, idM INT)");
            db.mDropUpVoteTable = db.mConnection.prepareStatement("DROP TABLE upVoteTable");
            db.mDownVoteTable = db.mConnection.prepareStatement(
                    "CREATE TABLE downVoteTable(idV SERIAL PRIMARY KEY, userId VARCHAR(255) NOT NULL, idM INT)");
            db.mDropDownVoteTable = db.mConnection.prepareStatement("DROP TABLE downVoteTable");
            // Standard CRUD operations
            db.mDeleteOne = db.mConnection.prepareStatement("DELETE FROM tblData WHERE id = ?");
            db.mInsertOne = db.mConnection.prepareStatement("INSERT INTO tblData VALUES (default, ?, ?)");
            db.mSelectAll = db.mConnection.prepareStatement("SELECT id, subject FROM tblData");
            db.mSelectOne = db.mConnection.prepareStatement("SELECT * from tblData WHERE id=?");
            db.mUpdateOne = db.mConnection.prepareStatement("UPDATE tblData SET message = ? WHERE id = ?");
/////////////////
            db.mDeleteOneMsg = db.mConnection.prepareStatement("DELETE FROM messageTable WHERE idM = ?");
            db.mInsertOneMsg = db.mConnection.prepareStatement("INSERT INTO messageTable VALUES (default, ?, ?, ?)");
            db.mSelectAllMsg = db.mConnection.prepareStatement("SELECT idM, message, vote, userId, created FROM messageTable");
            db.mSelectOneMsg = db.mConnection.prepareStatement("SELECT * from messageTable WHERE idM = ?");
            db.mUpdateOneMsg = db.mConnection.prepareStatement("UPDATE messageTable SET message = ?, vote = ? WHERE idM = ?");
            db.mSalt = db.mConnection.prepareStatement("UPDATE userIdTable SET salt = ? WHERE idU = ?");
            db.mHash = db.mConnection.prepareStatement("UPDATE userIdTable SET password = ? WHERE idU = ?");
////////////////////////////
            db.mSelectOneUserIdName = db.mConnection.prepareStatement("SELECT * from userIdTable WHERE userId = ?");
            db.mDeleteOneUserId = db.mConnection.prepareStatement("DELETE FROM userIdTable WHERE idU = ?");
            db.mInsertOneUserId = db.mConnection.prepareStatement("INSERT INTO userIdTable VALUES (default, ?, ?, ?, ?, ?, ?, ?)");
            db.mSelectAllUserId = db.mConnection.prepareStatement("SELECT idU, userId, realName, email, status, salt, password FROM userIdTable");
            db.mSelectOneUserId = db.mConnection.prepareStatement("SELECT * from userIdTable WHERE idU = ?");
            db.mUpdateOneUserId = db.mConnection.prepareStatement("UPDATE userIdTable SET userId = ? WHERE idU = ?");
 //////////////
            db.mInsertOneComment = db.mConnection.prepareStatement("INSERT INTO commentTable VALUES (default, ?, ?, ?, ?)");

        } catch (SQLException e) {
            System.err.println("Error creating prepared statement");
            e.printStackTrace();
            db.disconnect();
            return null;
        }
        return db;
    }

    /**
     * Close the current connection to the database, if one exists.
     * 
     * NB: The connection will always be null after this call, even if an 
     *     error occurred during the closing operation.
     * 
     * @return True if the connection was cleanly closed, false otherwise
     */
    boolean disconnect() {
        if (mConnection == null) {
            System.err.println("Unable to close connection: Connection was null");
            return false;
        }
        try {
            mConnection.close();
        } catch (SQLException e) {
            System.err.println("Error: Connection.close() threw a SQLException");
            e.printStackTrace();
            mConnection = null;
            return false;
        }
        mConnection = null;
        return true;
    }

    private static String get_SHA_256_SecurePassword(String passwordToHash, byte[] salt)
    {
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] bytes = md.digest(passwordToHash.getBytes());
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    void sendMail(String email, int id) throws IOException, NoSuchAlgorithmException {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String pass = new SecureRandom().ints(8, 0, chars.length()).mapToObj(i -> "" + chars.charAt(i)).collect(Collectors.joining());
        byte[] salt = saltIt(id);
        String hash = get_SHA_256_SecurePassword(pass, salt);
        try {
            //System.out.println("hash: " + hash);
            mHash.setString(1, hash);
            mHash.setInt(2, id);
            mHash.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Email from = new Email("tkl220@lehigh.edu");
        String subject = "Welcome to Walrus";
        Email to = new Email(email);
        Content content = new Content("text/plain", "Something something something walrus is great, this is your password btw: " + pass);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid("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");
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException ex) {
            throw ex;
        }
    }

    byte[] saltIt(int id) throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);

        try {
            mSalt.setBytes(1, salt);
            mSalt.setInt(2, id);
            mSalt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return salt;
    }



    /**
     * Insert a row into the database
     * 
     * @param subject The subject for this new row
     * @param message The message body for this new row
     * 
     * @return The number of rows that were inserted
     */
    int insertRow(String subject, String message) {
        int count = 0;
        try {
            mInsertOne.setString(1, subject);
            mInsertOne.setString(2, message);
            count += mInsertOne.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    UserRow selectOneUserIdName(String id) {
        UserRow res = null;
        try {
            mSelectOneUserIdName.setString(1, id);
            ResultSet rs = mSelectOneUserIdName.executeQuery();
            if (rs.next()) {
                res = new UserRow(rs.getInt("idU"), rs.getString("userId"), rs.getString("realName"), rs.getString("email"), rs.getBytes("salt"), rs.getString("password"), rs.getString("status"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Insert a row into the database
     *
     * @param message The message body for this new row
     * 
     * @return The number of rows that were inserted
     */
    int insertRowMsg(int vote, String message, String userId) {
        int count = 0;
        try {
            mInsertOneMsg.setString(1, message);
            mInsertOneMsg.setString(3, userId);
            mInsertOneMsg.setInt(2, vote);
            count += mInsertOneMsg.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    int insertRowComment(String comment, String userId, int idM) {
        int count = 0;
        try {
            mInsertOneComment.setString(1, comment);
            mInsertOneComment.setString(3, userId);
            mInsertOneComment.setInt(2, idM);
            count += mInsertOneComment.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Insert a row into the database
     * 
     * UserId The subject for this new row
     * 
     * @return The number of rows that were inserted
     */
    int insertRowUserId(String userId, String realName, String email, String status, byte[] salt, String hash) {
        int count = 0;
        try {
            mInsertOneUserId.setString(1, userId);
            mInsertOneUserId.setString(2, realName);
            mInsertOneUserId.setString(3, email);
            mInsertOneUserId.setString(4, status);
            mInsertOneUserId.setBytes( 5, salt);
            mInsertOneUserId.setString(6, hash);
            mInsertOneUserId.setInt(   7, 0);
            count += mInsertOneUserId.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Query the database for a list of all subjects and their IDs
     * 
     * @return All rows, as an ArrayList
     */
    ArrayList<RowData> selectAll() {
        ArrayList<RowData> res = new ArrayList<RowData>();
        try {
            ResultSet rs = mSelectAll.executeQuery();
            while (rs.next()) {
                res.add(new RowData(rs.getInt("id"), rs.getString("subject"), null, 0, null, null, null));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Query the database for a list of all subjects and their IDs
     * 
     * @return All rows, as an ArrayList
     */
    ArrayList<RowData> selectAllMsg() {
        ArrayList<RowData> res = new ArrayList<RowData>();
        try {
            ResultSet rs = mSelectAllMsg.executeQuery();
            while (rs.next()) {
                res.add(new RowData(rs.getInt("idM"), null, rs.getString("message"), rs.getInt("vote"), rs.getString("userId"), null, null));
            }
            System.out.println("please don't be null: " + rs);
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Query the database for a list of all subjects and their IDs
     * 
     * @return All rows, as an ArrayList
     */
    ArrayList<UserRow> selectAllUserId() {
        ArrayList<UserRow> res = new ArrayList<UserRow>();
        try {
            ResultSet rs = mSelectAllUserId.executeQuery();
            while (rs.next()) {
                res.add(new UserRow(rs.getInt("idU"), rs.getString("userId"), rs.getString("realName"), rs.getString("email"), rs.getBytes("salt"), rs.getString("password"), rs.getString("status")));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get all data for a specific row, by ID
     * 
     * @param id The id of the row being requested
     * 
     * @return The data for the requested row, or null if the ID was invalid
     */
    RowData selectOne(int id) {
        RowData res = null;
        try {
            mSelectOne.setInt(1, id);
            ResultSet rs = mSelectOne.executeQuery();
            if (rs.next()) {
                res = new RowData(rs.getInt("id"), rs.getString("subject"), rs.getString("message"), 0,null,null, null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Get all data for a specific row, by ID
     * 
     * @param id The id of the row being requested
     * 
     * @return The data for the requested row, or null if the ID was invalid
     */
    RowData selectOneMsg(int id) {
        RowData res = null;
        try {
            mSelectOneMsg.setInt(1, id);
            ResultSet rs = mSelectOneMsg.executeQuery();
            if (rs.next()) {
                res = new RowData(rs.getInt("idM"), null, rs.getString("message"), rs.getInt("vote"), null,null, null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Get all data for a specific row, by ID
     * 
     * @param id The id of the row being requested
     * 
     * @return The data for the requested row, or null if the ID was invalid
     */
    RowData selectOneUserId(int id) {
        RowData res = null;
        try {
            mSelectOneUserId.setInt(1, id);
            ResultSet rs = mSelectOneUserId.executeQuery();
            if (rs.next()) {
                res = new RowData(rs.getInt("idU"), null, null, 0, rs.getString("userId"),null, null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Delete a row by ID
     * 
     * @param id The id of the row to delete
     * 
     * @return The number of rows that were deleted.  -1 indicates an error.
     */
    int deleteRow(int id) {
        int res = -1;
        try {
            mDeleteOne.setInt(1, id);
            res = mDeleteOne.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Delete a row by ID
     * 
     * @param id The id of the row to delete
     * 
     * @return The number of rows that were deleted.  -1 indicates an error.
     */
    int deleteRowMsg(int id) {
        int res = -1;
        try {
            mDeleteOneMsg.setInt(1, id);
            res = mDeleteOneMsg.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    int deleteRowUserId(int id) {
        int res = -1;
        try {
            mDeleteOneUserId.setInt(1, id);
            res = mDeleteOneUserId.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Update the message for a row in the database
     * 
     * @param id The id of the row to update
     * @param message The new message contents
     * 
     * @return The number of rows that were updated.  -1 indicates an error.
     */
    int updateOne(int id, String message) {
        int res = -1;
        try {
            mUpdateOne.setString(1, message);
            mUpdateOne.setInt(2, id);
            res = mUpdateOne.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Update the message for a row in the database
     * 
     * @param id The id of the row to update
     * @param message The new message contents
     * 
     * @return The number of rows that were updated.  -1 indicates an error.
     */
    int updateOneMsg(int id, String message, int vote) {
        int res = -1;
        try {
            mUpdateOneMsg.setString(1, message);
            mUpdateOneMsg.setInt(2, vote);
            mUpdateOneMsg.setInt(3, id);
            res = mUpdateOneMsg.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }



    /**
     * Update the message for a row in the database
     * 
     * @param id The id of the row to update
     * @param message The new message contents
     * 
     * @return The number of rows that were updated.  -1 indicates an error.
     */
    int updateOneUserId(int id, String message) {
        int res = -1;
        try {
            mUpdateOneUserId.setString(1, message);
            mUpdateOneUserId.setInt(2, id);
            res = mUpdateOneUserId.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Create tblData.  If it already exists, this will print an error
     */
    void createTable() {
        try {
            mCreateTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * create message table.
     */
    void createMessageTable() {
        try {
            mMessageTable.execute();
        } catch (SQLException e) {
            System.out.println("Couldn'create messagetable");
            e.printStackTrace();
        }
    }

    /**
     * create message table.
     */
    void createUserIdTable() {
        try {
            mUserIdTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * create message table.
     */
    void createCommentTable() {
        try {
            mCommentTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * create message table.
     */
    void createUpVoteTable() {
        try {
            mUpVoteTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * create message table.
     */
    void createDownVoteTable() {
        try {
            mDownVoteTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove tblData from the database.  If it does not exist, this will print
     * an error.
     */
    void dropTable() {
        try {
            mDropTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    /**
     * Remove tblData from the database.  If it does not exist, this will print
     * an error.
     */
    void dropUserIdTable() {
        try {
            mDropUserIdTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove tblData from the database.  If it does not exist, this will print
     * an error.
     */
    void dropMessageTable() {
        try {
            mDropMessageTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove tblData from the database.  If it does not exist, this will print
     * an error.
     */
    void dropCommentTable() {
        try {
            mDropCommentTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * Remove tblData from the database.  If it does not exist, this will print
     * an error.
     */
    void dropUpVoteTable() {
        try {
            mDropUpVoteTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * Remove tblData from the database.  If it does not exist, this will print
     * an error.
     */
    void dropDownVoteTable() {
        try {
            mDropDownVoteTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}