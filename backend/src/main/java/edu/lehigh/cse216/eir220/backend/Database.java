package edu.lehigh.cse216.eir220.backend;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Timestamp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
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
     * A prepared statement for dropping the userId in our database
     */
    private PreparedStatement mDropUserIdTable;

    /**
     * A prepared statement for getting a total count of all messages sent
     */
    private PreparedStatement mGetCount;

    /**
     * Upvote a message
     */
    private PreparedStatement mUpvote;

    /**
     * Downvote a message
     */
    private PreparedStatement mDownvote;
    private PreparedStatement mDeleteOneComment;
    private PreparedStatement mInsertOneComment;
    private PreparedStatement mSelectAllComment;
    private PreparedStatement mSelectOneComment;
    private PreparedStatement mUpdateOneComment;
    private PreparedStatement mSelectOneUserIdName;
    private PreparedStatement mUpdateOneUserIdPass;
    private PreparedStatement mUpdateOneUserIdStatus;
    private PreparedStatement mlogUser;
    private PreparedStatement mSelectAllDownVotes;
    private PreparedStatement mSelectAllUpVotes;
    private PreparedStatement mSelectAllCommentIdM;
    private PreparedStatement mUserNameExists;
    private PreparedStatement mMakeUpvote;
    private PreparedStatement mMakeDownvote;


    /**
     * RowData is like a struct in C: we use it to hold data, and we allow
     * direct access to its fields.  In the context of this Database, RowData
     * represents the data we'd see in a row.
     * <p>
     * We make RowData a static class of Database because we don't really want
     * to encourage users to think of RowData as being anything other than an
     * abstract representation of a row of the database.  RowData and the
     * Database are tightly coupled: if one changes, the other should too.
     */
    public static class RowData {
        UserRow uRow;
        CommentRow cRow;
        MessageRow mRow;

        /**
         * Construct a RowData object by providing values for its fields
         */
        public RowData(UserRow uRow, CommentRow cRow, MessageRow mRow) {
            this.uRow = uRow;
            this.cRow = cRow;
            this.mRow = mRow;
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

// Attempt to create all of our prepared statements.  If any of thesem
        // fail, the whole getDatabase() call should fail
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
                    "CREATE TABLE messageTable(idM SERIAL PRIMARY KEY, message VARCHAR(500) NOT NULL, vote INT)");
            db.mDropMessageTable = db.mConnection.prepareStatement("DROP TABLE messageTable");
            db.mUserIdTable = db.mConnection.prepareStatement(
                    "CREATE TABLE userIdTable(idU SERIAL PRIMARY KEY, userId VARCHAR(50) NOT NULL)");
            db.mDropUserIdTable = db.mConnection.prepareStatement("DROP TABLE userIdTable");
            // Standard CRUD operations
            //////////////////
            db.mDeleteOne = db.mConnection.prepareStatement("DELETE FROM tblData WHERE id = ?");
            db.mInsertOne = db.mConnection.prepareStatement("INSERT INTO tblData VALUES (default, ?, ?)");
            db.mSelectAll = db.mConnection.prepareStatement("SELECT id, subject, message, vote FROM tblData");
            db.mSelectOne = db.mConnection.prepareStatement("SELECT * from tblData WHERE id=?");
            db.mUpdateOne = db.mConnection.prepareStatement("UPDATE messageTable SET vote = ? WHERE idM = ?");
/////////////////
            db.mGetCount = db.mConnection.prepareStatement("SELECT * FROM messageTable");
            db.mDeleteOneMsg = db.mConnection.prepareStatement("DELETE FROM messageTable WHERE idM = ?");
            db.mInsertOneMsg = db.mConnection.prepareStatement("INSERT INTO messageTable VALUES (default, ?, ?, ?)");
            db.mSelectAllMsg = db.mConnection.prepareStatement("SELECT * FROM messageTable");
            db.mSelectOneMsg = db.mConnection.prepareStatement("SELECT * from messageTable WHERE idM = ?");
            db.mUpdateOneMsg = db.mConnection.prepareStatement("UPDATE messageTable SET (message = ?, vote = ?) WHERE idM = ?");
            /////////////////
            db.mSelectAllDownVotes = db.mConnection.prepareStatement("SELECT * FROM downVoteTable WHERE idM = ?");
            db.mSelectAllUpVotes = db.mConnection.prepareStatement("SELECT * FROM upVoteTable WHERE idM = ?");
            db.mUpvote = db.mConnection.prepareStatement("UPDATE messageTable SET vote = ((SELECT vote FROM messageTable WHERE idM = ?) + 1) WHERE idM = ?");
            db.mDownvote = db.mConnection.prepareStatement(" UPDATE messageTable SET vote = ((SELECT vote FROM messageTable WHERE idM = ?) - 1) WHERE idM = ?");
            db.mMakeUpvote = db.mConnection.prepareStatement("INSERT INTO upVoteTable VALUES (default, ?,?)");
            db.mMakeDownvote = db.mConnection.prepareStatement("INSERT INTO downVoteTable VALUES (default, ?,?)");
            /////////////////
            db.mDeleteOneUserId = db.mConnection.prepareStatement("DELETE FROM userIdTable WHERE idU = ?");
            db.mInsertOneUserId = db.mConnection.prepareStatement("INSERT INTO userIdTable VALUES (default, ?, ?, ?, ?, ?, ?, ?)");
            db.mSelectAllUserId = db.mConnection.prepareStatement("SELECT idU, userId FROM userIdTable");
            db.mSelectOneUserId = db.mConnection.prepareStatement("SELECT * from userIdTable WHERE idU = ?");
            db.mUserNameExists = db.mConnection.prepareStatement("SELECT COUNT(*) AS total from userIdTable WHERE userId = ?");
            db.mSelectOneUserIdName = db.mConnection.prepareStatement("SELECT * from userIdTable WHERE userId = ?");
            db.mUpdateOneUserId = db.mConnection.prepareStatement("UPDATE userIdTable SET userId = ? WHERE idU = ?");
            db.mUpdateOneUserIdPass = db.mConnection.prepareStatement("UPDATE userIdTable SET password = ? WHERE userId = ?");
            db.mUpdateOneUserIdStatus = db.mConnection.prepareStatement("UPDATE userIdTable SET status = ? WHERE userId = ?");
            db.mlogUser = db.mConnection.prepareStatement("UPDATE userIdTable SET logonStatus = ? WHERE userId = ?");
            //////////////
            db.mDeleteOneComment = db.mConnection.prepareStatement("DELETE FROM commentTable WHERE id = ?");
            db.mInsertOneComment = db.mConnection.prepareStatement("INSERT INTO commentTable VALUES (default, ?, ?, ?)");
            db.mSelectAllComment = db.mConnection.prepareStatement("SELECT  idC, comment, idM, userId FROM commentTable");
            db.mSelectAllCommentIdM = db.mConnection.prepareStatement("SELECT  idC, comment, idM, userId FROM commentTable WHERE idM = ?");
            db.mSelectOneComment = db.mConnection.prepareStatement("SELECT * from commentTable WHERE id=?");
            db.mUpdateOneComment = db.mConnection.prepareStatement("UPDATE commentTable SET vote = ? WHERE idM = ?");
            ///////////////////////////
        } catch (SQLException e) {
            System.err.println("Error creating prepared statement");
            e.printStackTrace();
            db.disconnect();
            return null;
        }
        System.out.println("Database successfully connected.");
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

    /**
     * Insert a row into the database
     *
     * @param message The message body for this new row
     *
     * @return -1 if unsuccessful, otherwise successful
     */
    int insertRowMsg(String message, int vote, String userId) {
        int count = -1;
        try {
            mInsertOneMsg.setString(1, message);
            mInsertOneMsg.setInt(2, vote);
            mInsertOneMsg.setString(3, userId);
            count += mInsertOneMsg.executeUpdate();
            System.out.println("count: " + count);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    int insertRowComment(String comment, String userId, int idM) {
        int count = -1;
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
     * @param userId The subject for this new row
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
                //res.add(new RowData(rs.getInt("id"), rs.getString("subject"), null, rs.getInt("vote"), null));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return res;
        }
    }

    /**
     * Get count of all messages in database
     *
     */
    int getCount()
    {
        int count = 0;
        try {
            ResultSet rs = mGetCount.executeQuery();
            if(rs != null)
            {
                while(rs.next())
                {
                    count++;
                }
            }
        } catch(SQLException e)
        {
            e.printStackTrace();
            return 0;
        }
        return count;
    }

    /**
     * Query the database for a list of all subjects and their IDs
     *
     * @return All rows, as an ArrayList
     */
    ArrayList<MessageRow> selectAllMsg() {
        ArrayList<MessageRow> res = new ArrayList<MessageRow>();

        try {
            ResultSet rs = mSelectAllMsg.executeQuery();
            while (rs.next()) {
                res.add(new MessageRow(rs.getInt("idM"), rs.getString("message"), rs.getInt("vote"), rs.getString("userId")));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    ArrayList<CommentRow> selectAllComment() {
        ArrayList<CommentRow> res = new ArrayList<CommentRow>();

        try {
            ResultSet rs = mSelectAllComment.executeQuery();
            while (rs.next()) {
                res.add(new CommentRow(rs.getInt("idC"), rs.getString("userId"), rs.getString("comment"), rs.getInt("idM")));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    ArrayList<CommentRow> selectAllCommentIdM(int id) {
        ArrayList<CommentRow> res = new ArrayList<CommentRow>();

        try {
            mSelectAllCommentIdM.setInt(1, id);
            ResultSet rs = mSelectAllCommentIdM.executeQuery();
            while (rs.next()) {
                res.add(new CommentRow(rs.getInt("idC"), rs.getString("userId"), rs.getString("comment"), rs.getInt("idM")));
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
    MessageRow selectOneMsg(int id) {
        MessageRow res = null;
        try {
            mSelectOneMsg.setInt(1, id);
            ResultSet rs = mSelectOneMsg.executeQuery();
            if (rs.next()) {
                res = new MessageRow(rs.getInt("idM"), rs.getString("message"), rs.getInt("vote"), rs.getString("userId"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }



    /**
     * Query the database for a list of all subjects and their IDs
     *
     * @return All rows, as an ArrayList
     */
    ArrayList<RowData> selectAllUserId() {
        ArrayList<RowData> res = new ArrayList<RowData>();
        try {
            ResultSet rs = mSelectAllUserId.executeQuery();
            while (rs.next()) {
               // res.add(new RowData(rs.getInt("idU"), null, null, 0, rs.getString("userId")));
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
               // res = new RowData(rs.getInt("id"), rs.getString("subject"), rs.getString("message"), 0,null);
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
    UserRow selectOneUserId(int id) {
        UserRow res = null;
        try {
            mSelectOneUserId.setInt(1, id);
            ResultSet rs = mSelectOneUserId.executeQuery();
            if (rs.next()) {
                res = new UserRow(rs.getInt("idU"), rs.getString("userId"), rs.getString("realName"), rs.getString("email"), rs.getBytes("salt"), rs.getString("password"), rs.getString("status"), rs.getInt("logonStatus"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    int doesUserIdExist(String userId) {
        int res = 0;
        try {
            mUserNameExists.setString(1, userId);
            ResultSet rs = mUserNameExists.executeQuery();
            if (rs.next()) {
                res = rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    UserRow selectOneUserIdName(String id) {
        UserRow res = null;
        try {
            mSelectOneUserIdName.setString(1, id);
            ResultSet rs = mSelectOneUserIdName.executeQuery();
            if (rs.next()) {
                res = new UserRow(rs.getInt("idU"), rs.getString("userId"), rs.getString("realName"), rs.getString("email"), rs.getBytes("salt"), rs.getString("password"), rs.getString("status"), rs.getInt("logonStatus"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Upvote a message by userID
     *
     *   id ID of the row to upvote
     *
     * @return The current vote count of message
     */

    int upvote(String userId, int idM)
    {
        boolean check = true;
        int res;
        ArrayList<Vote> votes = new ArrayList<Vote>();
        try {
            mSelectAllUpVotes.setInt(1, idM);
            ResultSet rs = mSelectAllUpVotes.executeQuery();
            while (rs.next()) {
                votes.add(new Vote(rs.getString("userId"), rs.getInt("idM")));
            }
            rs.close();
            for(Vote i:votes){
                if(i.userId.compareTo(userId) == 0) {
                    check =  false;
                }
            }
            if(!check){
                return -3;
            }
            mMakeUpvote.setString(1, userId);
            mMakeUpvote.setInt(2, idM);
            res = mMakeUpvote.executeUpdate();
            mUpvote.setInt(1, idM);
            mUpvote.setInt(2, idM);
            res += mUpvote.executeUpdate();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    int downvote(String userId, int idM)
    {
        boolean check = true;
        int res;
        ArrayList<Vote> votes = new ArrayList<Vote>();
        try {
            mSelectAllDownVotes.setInt(1, idM);
            ResultSet rs = mSelectAllDownVotes.executeQuery();
            while (rs.next()) {
                votes.add(new Vote(rs.getString("userId"), rs.getInt("idM")));
            }
            rs.close();
            for(Vote i:votes){
                if(i.userId.compareTo(userId) == 0) {
                    check =  false;
                }
            }
            if(!check){
                return -3;
            }
            mMakeDownvote.setString(1, userId);
            mMakeDownvote.setInt(2, idM);
            res = mMakeUpvote.executeUpdate();
            mDownvote.setInt(1, idM);
            mDownvote.setInt(2, idM);
            res += mUpvote.executeUpdate();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
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
     * param  The id of the row to update
     * param The new message contents
     *
     * return The number of rows that were updated.  -1 indicates an error.
     */
    /*int updateOneUserId(int id, String message) {
        int res = -1;
        try {
            mUpdateOneUserId.setString(1, message);
            mUpdateOneUserId.setInt(2, id);
            res = mUpdateOneUserId.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }*/


    int logoutUser(String userId) {
        int res = -1;

        try {
            mlogUser.setInt(1, 0);
            mlogUser.setString(2, userId);
            res = mlogUser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    int loginUser(String userId) {
        int res = -1;

        try {
            mlogUser.setInt(1, 1);
            mlogUser.setString(2, userId);
            res = mlogUser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    int updateOneUserIdStatus(String userId, String status) {
        int res = -1;

        try {
            mUpdateOneUserIdStatus.setString(1, status);
            mUpdateOneUserIdStatus.setString(2, userId);
            res = mUpdateOneUserIdStatus.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    int updateOneUserIdPass(String userId, String pass) {
        int res = -1;

        UserRow user = selectOneUserIdName(userId);
        byte[] salt = user.salt;
        String newHash = get_SHA_256_SecurePassword(pass, salt);

        try {
            mUpdateOneUserIdPass.setString(1, newHash);
            mUpdateOneUserIdPass.setString(2, userId);
            res = mUpdateOneUserIdPass.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
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
}