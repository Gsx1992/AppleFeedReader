package com.gsx.feedreader;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by Gsx on 05-Feb-17.
 */

public class ParseHelper {
    private static final String TAG = "ParseHelper";
    private ArrayList<FeedEntry> arrayList;

    public ParseHelper() {
        this.arrayList = new ArrayList<>();
    }

    public ArrayList<FeedEntry> getArrayList() {
        return arrayList;
    }

    public boolean parse(String data) {
        boolean status = true;
        FeedEntry currentEntry = null;
        boolean inEntry = false;
        String textValue = "";


        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(data));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                String tagName = xpp.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:

                        if (tagName.equalsIgnoreCase("entry")) {
                            inEntry = true;
                            currentEntry = new FeedEntry();
                        }
                        break;
                    case XmlPullParser.TEXT:
                        textValue = xpp.getText();
                        break;

                    case XmlPullParser.END_TAG:

                        if (inEntry) {
                            if (tagName.equalsIgnoreCase("entry")) {
                                arrayList.add(currentEntry);
                            } else if (tagName.equalsIgnoreCase("name")) {
                                currentEntry.setName(textValue);
                            } else if (tagName.equalsIgnoreCase("artist")) {
                                currentEntry.setArtist(textValue);
                            } else if (tagName.equalsIgnoreCase("releaseDate")) {
                                currentEntry.setReleaseDate(textValue);
                            } else if (tagName.equalsIgnoreCase("summary")) {
                                currentEntry.setSummary(textValue);
                            } else if (tagName.equalsIgnoreCase("image")) {
                                currentEntry.setImageURL(textValue);
                            }
                        }

                        break;

                    default:
                        //Finished
                }

                eventType = xpp.next();

            }


        } catch (Exception e) {
            status = false;
            e.printStackTrace();
        }

        return status;
    }
}
