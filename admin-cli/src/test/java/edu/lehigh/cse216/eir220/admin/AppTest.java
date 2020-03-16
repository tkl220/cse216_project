package edu.lehigh.cse216.eir220.admin;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.sql.SQLException;

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
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }


    public void testCreateAndDropTable()
    {
        Database dbConTest = Database.getDatabase();
        dbConTest.createTable();
        assertEquals(dbConTest.insertRow("1", "2"), 1, .01);
        dbConTest.dropTable();
        dbConTest.disconnect();
    }
    public void testCreateAndDeleteRow()
    {
        Database dbConTest = Database.getDatabase();
        dbConTest.createTable();
        assertEquals(dbConTest.insertRow("1", "2"), 1, .01);
        dbConTest.dropTable();
        dbConTest.disconnect();
    }
}