package eir220.cse216.lehigh.edu.phase1;

import com.google.gson.annotations.SerializedName;

class Datum {
    /**
     * An integer index for this piece of data
     */
    @SerializedName("mIndex")
    int mIndex;

    /**
     * The string contents that comprise this piece of data
     */
    @SerializedName("mText")
    String mText;


    /**
     *The integer representing the number of likes a message has
     */
    @SerializedName("upVotes")
    int upVotes;

    /**
     * Construct a Datum by setting its votes and text
     *
     * @param upVote The number of upVotes a message has
     * @param txt The string contents for this piece of data
     * @param index the index of the message
     */
    Datum(int upVote, String txt, int index) {
        mIndex = index;
        upVotes = upVote;
        mText = txt;
    }

    Datum(String message){
        mText = message;
    }

    public int getmIndex(){
        return mIndex;
    }

    public int getUpVotes(){
        return upVotes;
    }

}
