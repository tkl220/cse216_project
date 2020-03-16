package edu.lehigh.cse216.eir220.backend;

// Import the Spark package, so that we can make use of the "get" function to 
// create an HTTP GET route
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import spark.Spark;

import java.io.File;
import java.io.FileReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.Constants.REDIRECT_URI;

// Import Google's JSON library

/**
 * For now, our app creates an HTTP server that can only get and add data.
 */
public class App {

    /**
     * Get an integer environment varible if it exists, and otherwise return the
     * default value.
     *
     * @envar      The name of the environment variable to get.
     * @defaultVal The integer value to use as the default if envar isn't found
     *
     * @returns The best answer we could come up with for a value for envar
     */
    static int getIntFromEnv(String envar, int defaultVal) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get(envar) != null) {
            System.out.println("Port : " + Integer.parseInt(processBuilder.environment().get(envar)));
            return Integer.parseInt(processBuilder.environment().get(envar));
        }
        System.out.println("Default Port used");
        return defaultVal;
    }
    public static void main(String[] args) {
        // gson provides us with a way to turn JSON into objects, and objects
        // into JSON.
        //
        // NB: it must be final, so that it can be accessed from our lambdas
        //
        // NB: Gson is thread-safe.  See 
        // https://stackoverflow.com/questions/10380835/is-it-ok-to-use-gson-instance-as-a-static-field-in-a-model-bean-reuse
        final Gson gson = new Gson();

        // dataStore holds all of the data that has been provided via HTTP 
        // requests
        //
        // NB: every time we shut down the server, we will lose all data, and 
        //     every time we start the server, we'll have an empty dataStore,
        //     with IDs starting over from 0.
        final DataStore dataStore = new DataStore();
        Database db = Database.getDatabase();


        // Set up the location for serving static files.  If the STATIC_LOCATION
        // environment variable is set, we will serve from it.  Otherwise, serve
        // from "/web"
        String static_location_override = System.getenv("STATIC_LOCATION");
        if (static_location_override == null) {
        Spark.staticFileLocation("/web");
        } else {
        Spark.staticFiles.externalLocation(static_location_override);
        }

        Spark.port(getIntFromEnv("PORT",4567));

        //String cors_enabled = env.get("CORS_ENABLED");
        if (true) {
            final String acceptCrossOriginRequestsFrom = "*";
            final String acceptedCrossOriginRoutes = "GET,PUT,POST,DELETE,OPTIONS";
            final String supportedRequestHeaders = "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin";
            enableCORS(acceptCrossOriginRequestsFrom, acceptedCrossOriginRoutes, supportedRequestHeaders);
        }

        // Set up a route for serving the main page
        Spark.get("/", (req, res) -> {
            res.redirect("/index.html");
            return "This will be implemented by front end soon.";
            });

        Spark.get("/index.html", (req, res) -> {
            return "This will be implemented by front end soon.";
            });

        Spark.get("/hello", (request, response) -> "Hello World!");

        // GET route that returns all message titles and Ids.  All we do is get 
        // the data, embed it in a StructuredResponse, turn it into JSON, and 
        // return it.  If there's no data, we return "[]", so there's no need 
        // for error handling.
        Spark.get("/messages", (request, response) -> {
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");

            ArrayList<MessageRow> res = db.selectAllMsg();


            if(res == null) {
                return gson.toJson(new StructuredResponse("error", " Null Return", null));
            }
            else
            {
                JsonArray jArray;
                JsonElement element =
                        gson.toJsonTree(res, new TypeToken<ArrayList<MessageRow>>() {
                        }.getType());
                jArray = element.getAsJsonArray();
                String jsonText = jArray.toString();

                return jsonText;
            }

        });

        Spark.get("/comments", (request, response) -> {
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");

            ArrayList<CommentRow> res = db.selectAllComment();

            if(res == null) {
                return gson.toJson(new StructuredResponse("error", " Null Return", null));
            }
            else
            {
                JsonArray jArray;
                JsonElement element =
                        gson.toJsonTree(res, new TypeToken<ArrayList<CommentRow>>() {
                        }.getType());
                jArray = element.getAsJsonArray();
                String jsonText = jArray.toString();

                return jsonText;
            }

        });

        Spark.get("/profile/:id", (request, response) -> {
            // ensure status 200 OK, with a MIME type of JSON
            String idx = request.params("id");
            response.status(200);
            response.type("application/json");

            UserRow res = db.selectOneUserIdName(idx);


            //DataRow data = dataStore.readOne(idx);
            if (res == null) {
                return gson.toJson(new StructuredResponse("error", idx + " not found", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, res));
            }

        });

        Spark.post("/authCode/", (request, response) -> {
            // NB: if gson.Json fails, Spark will reply with status 500 Internal
            // Server Error
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            // NB: even on error, we return 200, but with a JSON object that
            //     describes the error.
            response.status(200);
            response.type("application/json");
            // NB: createEntry checks for null title and message

            // (Receive authCode via HTTPS POST)


            /*if (request.getHeader('X-Requested-With') == null) {
                // Without the `X-Requested-With` header, this request could be forged. Aborts.
            }*/

// Set path to the Web application client_secret_*.json file you downloaded from the
// Google API Console: https://console.developers.google.com/apis/credentials
// You can also find your Web application client ID and client secret from the
// console and specify them directly when you create the GoogleAuthorizationCodeTokenRequest
// object.
            String CLIENT_SECRET_FILE = "/path/to/client_secret.json";

// Exchange auth code for access token
            GoogleClientSecrets clientSecrets =
                    GoogleClientSecrets.load(
                            JacksonFactory.getDefaultInstance(), new FileReader(CLIENT_SECRET_FILE));
            GoogleTokenResponse tokenResponse =
                    new GoogleAuthorizationCodeTokenRequest(
                            new NetHttpTransport(),
                            JacksonFactory.getDefaultInstance(),
                            "https://www.googleapis.com/oauth2/v4/token",
                            "735140239297-9k1k1m0guat7g2k2hvgt1ffk3keq6e19.apps.googleusercontent.com",
                            "E9mC7UKz-ZmSsAhhdqCaSrBS",
                            req.token,
                            REDIRECT_URI)  // Specify the same redirect URI that you use with your web
                            // app. If you don't have a web version of your app, you can
                            // specify an empty string.
                            .execute();

            String accessToken = tokenResponse.getAccessToken();


// Get profile info from ID token
            GoogleIdToken idToken = tokenResponse.parseIdToken();
            GoogleIdToken.Payload payload = idToken.getPayload();
            String userId = payload.getSubject();  // Use this value as a key to identify a user.
            String email = payload.getEmail();
            boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String locale = (String) payload.get("locale");
            String familyName = (String) payload.get("family_name");
            String givenName = (String) payload.get("given_name");

            int res = -1;

            if(db.doesUserIdExist(userId) == 1) {
                res = db.loginUser(userId);
            } else {
                res = db.insertRowUserId(userId, givenName.concat(" ").concat(familyName), email, null, null, null);
            }

            if (res == -1) {
                return gson.toJson(new StructuredResponse("error", "error during login" , null));
            } else {
                return gson.toJson(new StructuredResponse("ok", "" + req.message, res));
            }
        });

        Spark.put("/login/", (request, response) -> {
            // ensure status 200 OK, with a MIME type of JSON
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);

            response.status(200);
            response.type("application/json");

            if(db.doesUserIdExist(req.userId) == 0)
            {
                return gson.toJson(new StructuredResponse("error", req.userId + " not found", null));
            }

            UserRow res = db.selectOneUserIdName(req.userId);
            byte[] salt = res.salt;
            String pass = req.pass;
            String hash = get_SHA_256_SecurePassword(pass, salt);

            if (res == null) {
                return gson.toJson(new StructuredResponse("error", req.userId + " not found", null));
            } else if (res.logonStatus == 1) {
                return gson.toJson(new StructuredResponse("error", "already logged on", res));
            } else if (hash.compareTo(res.password) != 0) {
                return gson.toJson(new StructuredResponse("error", "wrong password", res));
            } else {
                db.loginUser(req.userId);
                return gson.toJson(new StructuredResponse("ok", "LOGIN SUCCESS", res));
            }
        });

        Spark.put("/logout/", (request, response) -> {
            // ensure status 200 OK, with a MIME type of JSON
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            response.status(200);
            response.type("application/json");

            UserRow res = db.selectOneUserIdName(req.userId);

            //DataRow data = dataStore.readOne(idx);
            if (res == null) {
                return gson.toJson(new StructuredResponse("error", req.userId + " not found", null));
            } else if (res.logonStatus == 0) {
                return gson.toJson(new StructuredResponse("error", "not logged on", res));
            } else {
                db.logoutUser(req.userId);
                return gson.toJson(new StructuredResponse("ok", "logged off", res));
            }

        });

        // GET route that returns everything for a single row in the DataStore.
        // The ":id" suffix in the first parameter to get() becomes 
        // request.params("id"), so that we can get the requested row ID.  If 
        // ":id" isn't a number, Spark will reply with a status 500 Internal
        // Server Error.  Otherwise, we have an integer, and the only possible 
        // error is that it doesn't correspond to a row with data.
        Spark.get("/messages/:id", (request, response) -> {
            int idx = Integer.parseInt(request.params("id"));
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            MessageRow data = db.selectOneMsg(idx);
            if (data == null) {
                return gson.toJson(new StructuredResponse("error", idx + " not found", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, data));
            }
        });

        Spark.get("/comments/:id", (request, response) -> {
            int idx = Integer.parseInt(request.params("id"));
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            ArrayList<CommentRow> data = db.selectAllCommentIdM(idx);
            if (data == null) {
                return gson.toJson(new StructuredResponse("error", idx + " not found", null));
            } else {
                JsonArray jArray;
                JsonElement element =
                        gson.toJsonTree(data, new TypeToken<ArrayList<CommentRow>>() {
                        }.getType());
                jArray = element.getAsJsonArray();
                String jsonText = jArray.toString();

                return jsonText;
            }
        });

        Spark.post("/register/", (request, response) -> {
            // NB: if gson.Json fails, Spark will reply with status 500 Internal
            // Server Error
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            // NB: even on error, we return 200, but with a JSON object that
            //     describes the error.
            response.status(200);
            response.type("application/json");
            // NB: createEntry checks for null title and message


            int res = db.insertRowUserId(req.userId, req.realName,req.email, req.status, null,null);

            if (res == -1) {
                return gson.toJson(new StructuredResponse("error", "error performing insertion of:" + req.message, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", "" + req.message, res));
            }
        });

        // POST route for adding a new element to the DataStore.  This will read
        // JSON from the body of the request, turn it into a SimpleRequest 
        // object, extract the title and message, insert them, and return the 
        // ID of the newly created row.
        Spark.post("/newMessage/", (request, response) -> {
            // NB: if gson.Json fails, Spark will reply with status 500 Internal 
            // Server Error
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            // NB: even on error, we return 200, but with a JSON object that
            //     describes the error.
            response.status(200);
            response.type("application/json");
            // NB: createEntry checks for null title and message


            int res = db.insertRowMsg( req.message, 0, req.userId);

            if (res == -1) {
                return gson.toJson(new StructuredResponse("error", "error performing insertion of:" + req.message, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", "" + req.message, res));
            }
        });

        Spark.post("/newComment/", (request, response) -> {
            // NB: if gson.Json fails, Spark will reply with status 500 Internal
            // Server Error
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            // NB: even on error, we return 200, but with a JSON object that
            //     describes the error.
            response.status(200);
            response.type("application/json");
            // NB: createEntry checks for null title and message


            int res = db.insertRowComment( req.message, req.userId, req.mId);

            if (res == -1) {
                return gson.toJson(new StructuredResponse("error", "error performing insertion of:" + req.message, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", "" + req.message, res));
            }
        });

        Spark.post("/upvote/", (request, response) -> {
            // NB: if gson.Json fails, Spark will reply with status 500 Internal
            // Server Error
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            // NB: even on error, we return 200, but with a JSON object that
            //     describes the error.
            response.status(200);
            response.type("application/json");
            // NB: createEntry checks for null title and message



            int res = db.upvote(req.userId, req.mId);
            if (res == -1) {
                return gson.toJson(new StructuredResponse("error", "error performing upvote of mID:" + req.mId, null));
            } else if (res == -3) {
                return gson.toJson(new StructuredResponse("error", "upvote of mID:" + req.mId + " already made by specified user", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", "ID: " + req.mId + " upvoted", null));
            }
        });

        Spark.post("/downvote/", (request, response) -> {
            // NB: if gson.Json fails, Spark will reply with status 500 Internal
            // Server Error
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            // NB: even on error, we return 200, but with a JSON object that
            //     describes the error.
            response.status(200);
            response.type("application/json");
            // NB: createEntry checks for null title and message


            int res = db.downvote(req.userId, req.mId);
            if (res == -1) {
                return gson.toJson(new StructuredResponse("error", "error performing downvote of mID:" + req.mId , null));
            } else if (res == -3) {
                return gson.toJson(new StructuredResponse("error", "downvote of mID:" + req.mId + " already made by specified user", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", "ID: " + req.mId + " downvoted", null));
            }
        });

        Spark.put("/newPassword/", (request, response) -> {
            // NB: if gson.Json fails, Spark will reply with status 500 Internal
            // Server Error
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            // NB: even on error, we return 200, but with a JSON object that
            //     describes the error.
            response.status(200);
            response.type("application/json");
            // NB: createEntry checks for null title and message
            String userId = req.userId;
            String pass = req.pass;


            int res = db.updateOneUserIdPass(userId, pass);

            if (res == -1) {
                return gson.toJson(new StructuredResponse("error", "error performing insertion of:" + req.message, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", "pass updated", res));
            }
        });

        Spark.put("/newStatus/", (request, response) -> {
            // NB: if gson.Json fails, Spark will reply with status 500 Internal
            // Server Error
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            // NB: even on error, we return 200, but with a JSON object that
            //     describes the error.
            response.status(200);
            response.type("application/json");
            // NB: createEntry checks for null title and message
            String userId = req.userId;
            String status = req.status;


            int res = db.updateOneUserIdStatus(userId, status);

            if (res == -1) {
                return gson.toJson(new StructuredResponse("error", "error performing insertion of:" + req.message, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", "" + req.message, res));
            }
        });

        //Always remember to disconnect from the database when the program
        //exits
        //db.disconnect();
    }

    /**
     * Set up CORS headers for the OPTIONS verb, and for every response that the
     * server sends.  This only needs to be called once.
     *
     * @param origin The server that is allowed to send requests to this server
     * @param methods The allowed HTTP verbs from the above origin
     * @param headers The headers that can be sent with a request from the above
     *                origin
     */
    private static void enableCORS(String origin, String methods, String headers) {
        // Create an OPTIONS route that reports the allowed CORS headers and methods
        Spark.options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        // 'before' is a decorator, which will run before any
        // get/post/put/delete.  In our case, it will put three extra CORS
        // headers into the response
        Spark.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
            response.header("Access-Control-Allow-Headers", headers);
        });
    }

    private static String get_SHA_256_SecurePassword(String pass, byte[] salt)
    {
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] bytes = md.digest(pass.getBytes());
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
