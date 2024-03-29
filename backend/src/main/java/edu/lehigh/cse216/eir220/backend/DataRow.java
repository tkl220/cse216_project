package edu.lehigh.cse216.eir220.backend;

import java.util.Date;

/**
 * DataRow holds a row of information.  A row of information consists of
 * an identifier, strings for a "title" and "content", and a creation date.
 * 
 * Because we will ultimately be converting instances of this object into JSON
 * directly, we need to make the fields public.  That being the case, we will
 * not bother with having getters and setters... instead, we will allow code to
 * interact with the fields directly.
 */
public class DataRow {

    //Unique PK for each individual message
    public int idM;
    //String containing the message sent
    public String message;
    //Vote originally initialized to zero, from there can be incremented or decremented
    public int vote;


    DataRow(int id, String title, int vote)
    {
        idM = id;
        message = title;
        vote = 0;
    }

    DataRow(DataRow data)
    {
        idM = data.idM;
        message = data.message;
        vote = data.vote;
    }

/*    *
     * The unique identifier associated with this element.  It's final, because
     * we never want to change it.

    public final int mId;

    *
     * The title for this row of data

    public String mTitle;

    *
     * The content for this row of data

    public String mContent;

    *
     * The creation date for this row of data.  Once it is set, it cannot be 
     * changed

    public final Date mCreated;

    *
     * Create a new DataRow with the provided id and title/content, and a
     * creation date based on the system clock at the time the constructor was
     * called
     *
     * @param id The id to associate with this row.  Assumed to be unique
     *           throughout the whole program.
     *
     * @param title The title string for this row of data
     *
     * @param content The content string for this row of data

    DataRow(int id, String title, String content) {
        mId = id;
        mTitle = title;
        mContent = content;
        mCreated = new Date();
    }

    *
     * Copy constructor to create one datarow from another

    DataRow(DataRow data) {
        mId = data.mId;
        // NB: Strings and Dates are immutable, so copy-by-reference is safe
        mTitle = data.mTitle;
        mContent = data.mContent;
        mCreated = data.mCreated;
    }*/

}
