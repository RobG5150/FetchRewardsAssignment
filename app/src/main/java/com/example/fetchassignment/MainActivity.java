package com.example.fetchassignment;


import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.lang.Exception;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    //declare listviews, array adapters, and array list
    private ListView goodListView, badListView;
    private ArrayAdapter<String> goodListAdapter, badListAdapter;
    private ArrayList<String> goodItems, badItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize listviews and array adapters
        goodListView = findViewById(R.id.goodList);
        badListView = findViewById(R.id.badList);

        goodItems = new ArrayList<>();
        badItems = new ArrayList<>();

        goodListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, goodItems);
        badListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, badItems);

        goodListView.setAdapter(goodListAdapter);
        badListView.setAdapter(badListAdapter);

        new getDataFunction().execute("https://fetch-hiring.s3.amazonaws.com/hiring.json");
    }

    private class getDataFunction extends AsyncTask<String, Void, String>{

        //retrieve data from url
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder jsonResponse = new StringBuilder();
            try{
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                while((inputLine = in.readLine()) != null){
                    jsonResponse.append(inputLine);
                }
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return jsonResponse.toString();
        }

        //execute once data is received
        protected void onPostExecute(String results){
            //creating two hashmaps to group good and bad items by ListID
            HashMap<String, ArrayList<String>> goodItemsGrouped = new HashMap<>();
            HashMap<String, ArrayList<String>> badItemsGrouped = new HashMap<>();

            try{
                //getting specific data from each item
                JSONArray array = new JSONArray(results);
                for(int i = 0; i < array.length(); i++){
                    JSONObject object = array.getJSONObject(i);
                    String id = object.optString("id");
                    String listID = object.optString("listId");
                    String name = object.optString("name");

                    //if the name field in the json file is empty or null, then put it in the bad list
                    if(name == null || name.isEmpty() || name.equals("null")){
                        //create listID key in hashmap if its not there already
                        if(!badItemsGrouped.containsKey(listID)){
                            badItemsGrouped.put(listID, new ArrayList<>());
                        }
                        //place in list with proper grouping
                        badItemsGrouped.get(listID).add("ID: " + id);
                    }
                    //if the name field is not null or empty, then put it in the good list
                    else{
                        //create listID key in hashmap if its not there already
                        if(!goodItemsGrouped.containsKey(listID)){
                            goodItemsGrouped.put(listID, new ArrayList<>());
                        }
                        //place in list with proper grouping
                        goodItemsGrouped.get(listID).add("Name: " + name);                    }
                }
                //once finished, clear items from arraylists
                goodItems.clear();
                badItems.clear();

                // format output to required specifications
                formatList(goodItemsGrouped, goodItems);
                formatList(badItemsGrouped, badItems);

                //update data
                goodListAdapter.notifyDataSetChanged();
                badListAdapter.notifyDataSetChanged();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        private void formatList(HashMap<String, ArrayList<String>> groupedItems, ArrayList<String> listToUpdate){
            for(String listID : groupedItems.keySet()){
                listToUpdate.add("ListID: " + listID );

                listToUpdate.addAll(groupedItems.get(listID));
            }
        }

    }


}

