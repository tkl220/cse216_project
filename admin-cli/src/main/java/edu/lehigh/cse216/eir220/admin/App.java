package edu.lehigh.cse216.eir220.admin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

/**
 * App is our basic admin app.  For now, it is a demonstration of the six key 
 * operations on a database: connect, insert, update, query, delete, disconnect
 */
public class App {

    /**
     * The connection to the database.  When there is no connection, it should
     * be null.  Otherwise, there is a valid open connection
     */
    private Connection mConnection;
    /**
     * Print the menu for our program
     */
    static void menu() {
        System.out.println("Main Menu");
        System.out.println("  [S] Send Email");
        System.out.println("  [T] Create tblData");
        System.out.println("  [D] Drop tblData");
        System.out.println("  [M] Create MessageTable");
        System.out.println("  [C] Create CommentTable");
        System.out.println("  [U] Create UserIdTable");
        System.out.println("  [UV] Create UpVoteTable");
        System.out.println("  [DV] Create DownVoteTable");
        System.out.println("  [DU] Drop UserIdTable");
        System.out.println("  [DUV] Drop UpVoteTable");
        System.out.println("  [DDV] Drop DownVoteTable");
        System.out.println("  [DM] Drop MessageTable");
        System.out.println("  [DC] Drop CommentTable");
        System.out.println("  [1] Query for a specific row");
        System.out.println("  [RM] Query for a specific row in MessageTable");
        System.out.println("  [RU] Query for a specific row in UserIdTable");
        System.out.println("  [UU] Query for a specific row in UserIdTable");
        System.out.println("  [*] Query for all rows");
        System.out.println("  [*M] Query for all rows MessageTable");
        System.out.println("  [*U] Query for all rows UserId");
        System.out.println("  [-] Delete a row");
        System.out.println("  [-M] Delete a row MessageTable");
        System.out.println("  [-U] Delete a row UserIdTable");
        System.out.println("  [+] Insert a new row");
        System.out.println("  [+M] Insert a new row MessageTable");
        System.out.println("  [+U] Insert a new row UserId");
        System.out.println("  [~] Update a row");
        System.out.println("  [~M] Update a row MessageTable");
        System.out.println("  [~U] Update a row UserIdTable");
        System.out.println("  [q] Quit Program");
        System.out.println("  [?] Help (this message)");
    }

    /**
     * Ask the user to enter a menu option; repeat until we get a valid option
     * 
     * @param in A BufferedReader, for reading from the keyboard
     *
     * @return The character corresponding to the chosen menu option
     */
    static String prompt(BufferedReader in) {
        // The valid actions:
        String actions = "STDMCUVDUDMDCDV1RMRDVUVDDVDUVVRUUU**M*U*V--M-V-U+M+U+V+C~~M~V~Uq?";

        // We repeat until a valid single-character option is selected        
        while (true) {
            System.out.print("[" + actions + "] :> ");
            String action;
            try {
                action = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            //if (action.length() != 1)
               // continue;
            if (actions.contains(action)) {
                return action;
            }
            System.out.println("Invalid Command");
        }
    }

    /**
     * Ask the user to enter a String message
     * 
     * @param in A BufferedReader, for reading from the keyboard
     * @param message A message to display when asking for input
     * 
     * @return The string that the user provided.  May be "".
     */
    static String getString(BufferedReader in, String message) {
        String s;
        try {
            System.out.print(message + " :> ");
            s = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return s;
    }

    /**
     * Ask the user to enter an integer
     * 
     * @param in A BufferedReader, for reading from the keyboard
     * @param message A message to display when asking for input
     * 
     * @return The integer that the user provided.  On error, it will be -1
     */
    static int getInt(BufferedReader in, String message) {
        int i = -1;
        try {
            System.out.print(message + " :> ");
            i = Integer.parseInt(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return i;
    }

    /**
     * The main routine runs a loop that gets a request from the user and
     * processes it
     * 
     * @param argv Command-line options.  Ignored by this program.
     */
    public static void main(String[] argv) throws IOException, NoSuchAlgorithmException {

        //Map<String, String> env = System.getenv();
        //String db_url = env.get("DATABASE_URL");

        Database db = Database.getDatabase();

        // Start our basic command-line interpreter:
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            // Get the user's request, and do it
            //
            // NB: for better testability, each action should be a separate
            //     function call
            String action = prompt(in);
            if (action.equals("?")) {
                menu();
            } else if (action.equals("q")) {
                break;
            } else if (action.equals("T")) {
                db.createTable();
            } else if (action.equals("DM")) {
                db.dropMessageTable();
            } else if (action.equals("U")) {
                db.createUserIdTable();
            } else if (action.equals("M")) {
                db.createMessageTable();
            } else if (action.equals("C")) {
                db.createCommentTable();
            } else if (action.equals("D")) {
                db.dropTable();
            } else if (action.equals("UV")) {
                db.createUpVoteTable();
            } else if (action.equals("DV")) {
                db.createDownVoteTable();
            } else if (action.equals("DUV")) {
                db.dropUpVoteTable();
            } else if (action.equals("DDV")) {
                db.dropDownVoteTable();
            } else if (action.equals("DU")) {
                db.dropUserIdTable();
            } else if (action.equals("DC")) {
                db.dropCommentTable();
            } else if (action.equals("1")) {
                int id = getInt(in, "Enter the row ID");
                if (id == -1)
                    continue;
                Database.RowData res = db.selectOne(id);
                if (res != null) {
                    System.out.println("  [" + res.mId + "] " + res.mSubject);
                    System.out.println("  --> " + res.mMessage);
                }
            } else if (action.equals("RM")) {
                int id = getInt(in, "Enter the row ID");
                if (id == -1)
                    continue;
                Database.RowData res = db.selectOneMsg(id);
                if (res != null) {
                    System.out.println("  [" + res.mId + "] " + res.mVote);
                    System.out.println("  --> " + res.mMessage);
                }
            } else if (action.equals("RU")) {
                int id = getInt(in, "Enter the row ID");
                if (id == -1)
                    continue;
                Database.RowData res = db.selectOneUserId(id);
                if (res != null) {
                    System.out.println("  [" + res.mId + "] " + res.mUserId);
                }
            } else if (action.equals("UU")) {
                String id = getString(in, "Enter the username");
                if (id == null)
                    continue;
                UserRow res = db.selectOneUserIdName(id);
                if (res != null) {
                    System.out.println("  [" + res.idU + "] " + res.userId);
                }
            } else if (action.equals("*")) {
                ArrayList<Database.RowData> res = db.selectAll();
                if (res == null)
                    continue;
                System.out.println("  Current Database Contents");
                System.out.println("  -------------------------");
                for (Database.RowData rd : res) {
                    System.out.println("  [" + rd.mId + "] " + rd.mSubject);
                }
            } else if (action.equals("*M")) {
                ArrayList<Database.RowData> res = db.selectAllMsg();
                if (res == null)
                    continue;
                System.out.println("  Current Database Contents");
                System.out.println("  -------------------------");
                for (Database.RowData rd : res) {
                    System.out.println("  [" + rd.mId + "] " + rd.mMessage);
            }
            } else if (action.equals("*U")) {
                ArrayList<UserRow> res = db.selectAllUserId();
                if (res == null)
                    continue;
                System.out.println("   Current Database Contents");
                System.out.println("!!!-------------------------!!!");
                for (UserRow rd : res) {
                    System.out.println("  [" + rd.idU + "] ");
                    System.out.println("userId: " + rd.userId);
                    System.out.println("realName: " + rd.realName);
                    System.out.println("email: " + rd.email);
                    System.out.println("status: " + rd.status);
                    if(rd.salt != null) {
                        System.out.println("salt: " + rd.salt);
                        System.out.println("hash: " + rd.password);
                        System.out.println("Weird hash: " + get_SHA_256_SecurePassword("Z0j", rd.salt));

                    }
                    System.out.println("____________________________________");

                }
            } else if (action.equals("-")) {
                int id = getInt(in, "Enter the row ID");
                if (id == -1)
                    continue;
                int res = db.deleteRow(id);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows deleted");

            } else if (action.equals("-M")) {
                int id = getInt(in, "Enter the row ID");
                if (id == -1)
                    continue;
                int res = db.deleteRowMsg(id);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows deleted");

            } else if (action.equals("-U")) {
                int id = getInt(in, "Enter the row ID");
                if (id == -1)
                    continue;
                int res = db.deleteRowUserId(id);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows deleted");
            } else if (action.equals("+")) {
                String subject = getString(in, "Enter the subject");
                String message = getString(in, "Enter the message");
                if (subject.equals("") || message.equals(""))
                    continue;
                int res = db.insertRow(subject, message);
                System.out.println(res + " rows added");
            } else if (action.equals("+M")) {
                int vote = getInt(in, "Enter the vote");
                String message = getString(in, "Enter the message");
                String userId = getString(in, "Enter the userId");
                if (message.equals(""))
                    continue;
                int res = db.insertRowMsg(vote, message, userId);
                System.out.println(res + " rows added");
            } else if (action.equals("+C")) {
                int idM = getInt(in, "Enter the messageId(idM)");
                String message = getString(in, "Enter the comment");
                String userId = getString(in, "Enter the userId(userName)");
                if (message.equals(""))
                    continue;
                int res = db.insertRowComment(message, userId, idM);
                System.out.println(res + " rows added");
            } else if (action.equals("S")) {
                String email = getString(in, "Enter email");
                int id = getInt(in, "Enter the id of the user");
                db.sendMail(email, id);
            } else if (action.equals("+U")) {
                String realName = getString(in, "Enter the real name");
                String userId = getString(in, "Enter the userId");
                String email = getString(in, "Enter email");
                String status = getString(in, "Enter the status");
                if (userId.equals(""))
                    continue;
                int res = db.insertRowUserId(userId, realName, email, status, null, null);
                System.out.println(res + " rows added");

            } else if (action.equals("~")) {
                int id = getInt(in, "Enter the row ID :> ");
                if (id == -1)
                    continue;
                String newMessage = getString(in, "Enter the new message");
                int res = db.updateOne(id, newMessage);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows updated");
            } else if (action.equals("~M")) {
                int id = getInt(in, "Enter the row ID :> ");
                if (id == -1)
                    continue;
                String newMessage = getString(in, "Enter the new message");
                int newVote = getInt(in, "Enter the new vote");
                int res = db.updateOneMsg(id, newMessage, newVote);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows updated");
            } else if (action.equals("~U")) {
                int id = getInt(in, "Enter the row ID :> ");
                if (id == -1)
                    continue;
                String newMessage = getString(in, "Enter the new UserId");
                int res = db.updateOneUserId(id, newMessage);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows updated");
            }
        }
        // Always remember to disconnect from the database when the program 
        // exits
        db.disconnect();
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
}