package edu.lehigh.cse216.eir220.backend;

/**
 * SimpleRequest provides a format for clients to present title and message 
 * strings to the server.
 * 
 * NB: since this will be created from JSON, all fields must be public, and we
 *     do not need a constructor.
 */
public class SimpleRequest {


    public int mId;
    public String userId;
    public String email;
    public String realName;
    public String status;
    public String pass;
    public String message;
    public String token;
    //public byte[] salt;

    /**
     * The message being provided by the client.
     */
}