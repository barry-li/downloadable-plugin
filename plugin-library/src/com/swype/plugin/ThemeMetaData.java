package com.swype.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.XmlResourceParser;
import android.util.Log;

public final class ThemeMetaData
{
    String themeStyleName;
    final String themeStyleableName = "SwypeThemeTemplate";
    boolean unlockKlingon;
    List<WordListMetadata> wordListMetadataList = new ArrayList<WordListMetadata>();

    ThemeMetaData(XmlResourceParser paramXmlResourceParser)
    {
        try {
            int eventType = paramXmlResourceParser.getEventType();
            String strTag = null;
            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                switch (eventType)
                {
                case XmlPullParser.START_TAG: 
                    strTag = paramXmlResourceParser.getName();

                    if (strTag.equals("wordlist")) {
                        WordListMetadata wordListMetadata = getWordListMetadata(paramXmlResourceParser);
                        wordListMetadataList.add(wordListMetadata);
                    }

                    break;
                case XmlPullParser.TEXT:
                    if ((strTag != null) && (!strTag.equals("id")) && 
                            (!strTag.equals("name")) && 
                            (!strTag.equals("description")) && 
                            (!strTag.equals("snapshot")) && 
                            (!strTag.equals("version")) && 
                            (!strTag.equals("date")) && 
                            (!strTag.equals("author"))) {
                        if (strTag.equals("theme")) {
                            this.themeStyleName = paramXmlResourceParser.getText();
                        } else if (strTag.equals("unlocks-klingon")) {
                            unlockKlingon = (paramXmlResourceParser.getText().compareToIgnoreCase("true") == 0);
                        }
                        else {
                            Log.w("ThemeInfo", "unknown tag: " + strTag);
                        }
                    }
                    break;
                }
                eventType = paramXmlResourceParser.next();
            }
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private WordListMetadata getWordListMetadata(XmlResourceParser parser) {
        return new WordListMetadata(parser.getAttributeValue(null, "type"),
                parser.getAttributeValue(null, "version"),
                parser.getAttributeValue(null, "file"));
    }

    public List<WordListMetadata> getWordListMetadataList() {
        return wordListMetadataList;
    }

    public static class WordListMetadata {
        public String type;
        public String version;
        public String fileName;

        public static final String TYPE_PHRASES = "phrases";
        public static final String TYPE_NAMES = "names";
        public static final String TYPE_WORDS = "words";

        public WordListMetadata(String type, String version, String file) {
            this.type =  type;
            this.version = version;
            this.fileName = file;
        }

        public boolean isTypePhrases() {
            return type.equalsIgnoreCase(TYPE_PHRASES);
        }
    }
}



