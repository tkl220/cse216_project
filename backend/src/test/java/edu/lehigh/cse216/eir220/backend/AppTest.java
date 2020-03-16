package edu.lehigh.cse216.eir220.backend;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     *
     */
    public void testApp()
    {

    }

    /**
     * This will test the selectAllMsg() function which is used to provide the server with a list of messages
     * This test will not make any modification to the database
     */
    /*public void testSelectAllMsg()
    {
        Database dbCopy = Database.getDatabase();
        int dbSize = dbCopy.getCount();
        ArrayList<MessageRow> res = dbCopy.selectAllMsg();

        assertTrue(res.size() == dbSize);
    }*/

    /**
     * Test to test the upvote() feature
     */
    /*public void testUpvote()
    {
        Database dbCopy = Database.getDatabase();
        //Gets the current vote of message 1
        int currVote = dbCopy.selectOneMsg(1).vote;
        dbCopy.upvote(1);

        assertTrue(dbCopy.selectOneMsg(1).vote == currVote + 1);

        //Return database back to original state
        dbCopy.downvote(1);
        assertTrue(dbCopy.selectOneMsg(1).vote == currVote);
    }

    public void testDownvote()
    {
        Database dbCopy = Database.getDatabase();

        int currVote = dbCopy.selectOneMsg(1).vote;
        dbCopy.downvote(1);

        assertTrue(dbCopy.selectOneMsg(1).vote == currVote - 1);

        //Return database to original state
        dbCopy.upvote(1);
        assertTrue(dbCopy.selectOneMsg(1).vote == currVote);
    }

    /*public void testConstructor()
    {
        int mId = 40;
        String subject = "h";
        String message = "Test";
        int vote = 5;
        String userId = "users";

        Database.RowData d = new Database.RowData(mId, subject, message, vote, userId);

        assertTrue(d.mId == mId);
        assertTrue(d.mSubject.equals(subject));
        assertTrue(d.mMessage.equals(message));
        assertTrue(d.mVote == vote);
        assertTrue(d.mUserId.equals(userId));

    }*/
}
