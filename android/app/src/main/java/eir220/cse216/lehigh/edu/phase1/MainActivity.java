package eir220.cse216.lehigh.edu.phase1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    /**
     * mData holds the data we get from Volley
     */
    ArrayList<Datum> mData = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(eir220.cse216.lehigh.edu.phase1.R.layout.activity_main);
        Toolbar toolbar = findViewById(eir220.cse216.lehigh.edu.phase1.R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.d("eir220", "Debug Message from onCreate");

// Instantiate the Singleton RequestQueue.
        final RequestQueue queue = Singleton.getInstance(this.getApplicationContext()).
                getRequestQueue();
//initially populates the android app with messages
        populateMessages();



//Listens for submit message buttons to be clicked
        final Button submitButton = (Button) findViewById(eir220.cse216.lehigh.edu.phase1.R.id.submitMessage);
        final TextView textView = (TextView) findViewById(eir220.cse216.lehigh.edu.phase1.R.id.listItemText);
        final EditText isbnInput = (EditText) findViewById(eir220.cse216.lehigh.edu.phase1.R.id.editText);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String text = isbnInput.getText().toString();
                Datum newMessage = new Datum(text);
                //url of the app
                String url = "https://whispering-reef-79322.herokuapp.com/newMessage";
//Posts a string to the provided URL.
//JSONObject params is the object being sent to the database: only expecting a message text
                JSONObject params = new JSONObject();
                try {
                    params.put("message", text);
                } catch (JSONException e) {
                    e.printStackTrace();

                }
                final JsonObjectRequest postRequest = new JsonObjectRequest(url, params,
                        new Response.Listener<JSONObject>()
                        {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("Response", response.toString());
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // error
                                Log.d("Error.Response", error.toString());
                            }
                        }
                ) {

                };
                Singleton.getInstance(getApplicationContext()).addToRequestQueue(postRequest);
                populateMessages();
                isbnInput.setText("");
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(eir220.cse216.lehigh.edu.phase1.R.menu.menu_main, menu);
        return true;
    }

//gets json response from server and populates the app
    private void populateListFromVolley(String response){
        mData = null;
        mData= new ArrayList<>();
        try {
            JSONArray json = new JSONArray(response);
            for (int i = json.length()-1; i > 0; --i) {
                int num = json.getJSONObject(i).getInt("mVote");
                String str = json.getJSONObject(i).getString("mMessage");
                int id = json.getJSONObject(i).getInt("mId");
                mData.add(new Datum(num, str, id));
            }

        } catch (final JSONException e) {
            Log.d("eir220", "Error parsing JSON file: " + e.getMessage());
            return;
        }
        Log.d("eir220", "Successfully parsed JSON file.");
        RecyclerView rv = (RecyclerView) findViewById(eir220.cse216.lehigh.edu.phase1.R.id.datum_list_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        ItemListAdapter adapter = new ItemListAdapter(this, mData);
        rv.setAdapter(adapter);

        adapter.setClickListener(new ItemListAdapter.ClickListener() {
            @Override
            public void onClick(Datum d, View view) {
                switch(view.getId()){
                    case eir220.cse216.lehigh.edu.phase1.R.id.upVote:
                        sendLike(d.getmIndex());
                        populateMessages();
                        break;
                    case eir220.cse216.lehigh.edu.phase1.R.id.downVote:
                        sendDislike(d.getmIndex());
                        populateMessages();
                        break;
                }
            }
        });
    }

    //method to send the like message to the server
    public void sendLike(int id){
        String url = "https://whispering-reef-79322.herokuapp.com/upvote/";
//Posts a string to the provided URL.
        JSONObject params = new JSONObject();
        try {
            params.put("mId", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final JsonObjectRequest postRequest = new JsonObjectRequest(url, params,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // response
                        Log.d("Response", response.toString());
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
        };
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(postRequest);
    }
    //method to send a dislike message to the server
    public void sendDislike(int id){
        String url = "https://whispering-reef-79322.herokuapp.com/downvote/";
//Posts a string to the provided URL.
        JSONObject params = new JSONObject();
        try {
            params.put("mId", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final JsonObjectRequest postRequest = new JsonObjectRequest(url, params,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // response
                        Log.d("Response", response.toString());
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
        };
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(postRequest);
    }
    //method the sends a get request to the server and passes the JSON response to next method
    public void populateMessages(){

        //write a volley method to get the messages
        String url = "https://whispering-reef-79322.herokuapp.com/messages";
        // prepare the Request
        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        populateListFromVolley(response);
                        System.out.println(response.toString());
                        Log.d("Response", response.toString());
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(getRequest);
    }
}
