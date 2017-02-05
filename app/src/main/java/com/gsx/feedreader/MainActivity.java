package com.gsx.feedreader;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ListView listView;
    private String feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
    private int feedLimit = 10;
    private String cachedUrl = "INVALID";
    public static final String STATE_URL = "feedUrl";
    public static final String STATE_LIMIT = "feedLimit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.xmlListView);

        if(savedInstanceState != null) {
            feedUrl = savedInstanceState.getString(STATE_URL);
            feedLimit = savedInstanceState.getInt(STATE_LIMIT);
        }
        downloadUrl(String.format(feedUrl, feedLimit));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_menu, menu);
        if(feedLimit == 10) {
            menu.findItem(R.id.mnu10).setChecked(true);
        } else {
            menu.findItem(R.id.mnu25).setChecked(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch(id) {
            case R.id.mnuFree:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
                break;

            case R.id.mnuPaid:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml";
                break;
            case R.id.mnuSongs:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;

            case R.id.mnu10:
            case R.id.mnu25:
                if(!item.isChecked()) {
                    item.setChecked(true);
                    feedLimit = 35 - feedLimit;
                }

                break;

            case R.id.mnuRefresh:
                cachedUrl = "INVALID";
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        downloadUrl(String.format(feedUrl, feedLimit));
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_URL, feedUrl);
        outState.putInt(STATE_LIMIT, feedLimit);
        super.onSaveInstanceState(outState);
    }

    private void downloadUrl(String url) {

        if(!feedUrl.equalsIgnoreCase(cachedUrl)) {
            DownloadData downloadData = new DownloadData();
            downloadData.execute(url);
            cachedUrl = feedUrl;
        }

    }

    private class DownloadData extends AsyncTask<String, Void, String>{

        private static final String TAG = "DownloadData";
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ParseHelper data = new ParseHelper();
            data.parse(s);
            FeedAdapter feedAdapter = new FeedAdapter(MainActivity.this, R.layout.list_record, data.getArrayList());
            listView.setAdapter(feedAdapter);
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG, "doInBackground:  starts with " + params[0]);
            String rssFeed = downloadXML(params[0]);
            if(rssFeed == null) {
                Log.e(TAG, "doInBackground: Error download!");
            }
            return rssFeed;
        }

        private String downloadXML(String urlPath) {
            StringBuilder xmlResult = new StringBuilder();

            try {
                URL url = new URL(urlPath);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                int response = conn.getResponseCode();
                Log.d(TAG, "downloadXML: Response code "+response);
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                int charsRead;
                char [] inputBuffer = new char[500];
                while (true) {
                    charsRead = reader.read(inputBuffer);

                    if(charsRead < 0) {
                        break;
                    }
                    if(charsRead > 0) {
                        xmlResult.append(String.copyValueOf(inputBuffer, 0, charsRead));
                    }
                }

                reader.close();

                return xmlResult.toString();

            } catch(MalformedURLException e){
                Log.e(TAG, "downloadXML: Invalid URL " +e.getMessage());
            } catch(IOException e2) {
                Log.e(TAG, "downloadXML: IOException "+e2.getMessage());
            } catch(SecurityException e3) {
                Log.e(TAG, "downloadXML: Security Exception. Missing security permission! " +e3.getMessage());
            }

            return null;
        }
    }

}
