package com.kelsos.mbrc.data;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class Artist implements BaseColumns, ArtistColumns{
    private String artistName;
    private long id;
    public static final String TABLE_NAME = "artists";
    public static final String[] FIELDS = { _ID, ARTIST_NAME };

    public static final String CREATE_TABLE =
            "create table " + TABLE_NAME + "(" + _ID + " integer primary key autoincrement,"
            + ARTIST_NAME + " text unique" + ")";
    public static final String DROP_TABLE = "drop table if exists " + TABLE_NAME;

    public static Uri URI() {
        return Uri.withAppendedPath(Uri.parse(LibraryProvider.SCHEME +
            LibraryProvider.AUTHORITY), TABLE_NAME);
    }

    public static final int BASE_URI_CODE = 0xb450ddf;
    public static final int BASE_ITEM_CODE =  0x4213467;

    public static void addMatcherUris(UriMatcher uriMatcher) {
        uriMatcher.addURI(LibraryProvider.AUTHORITY, TABLE_NAME, BASE_URI_CODE);
        uriMatcher.addURI(LibraryProvider.AUTHORITY, TABLE_NAME + "/#", BASE_ITEM_CODE);
    }

    public static final String TYPE_DIR = "vnd.android.cursor.dir/vnd.com.kelsos.mbrc.provider." + TABLE_NAME;
    public static final String TYPE_ITEM = "vnd.android.cursor.item/vnd.com.kelsos.mbrc.provider." + TABLE_NAME;

    public Artist(String artistName) {
        this.artistName = artistName;
        this.id = -1;
    }

    public Artist(final Cursor cursor) {
        this.id = cursor.getLong(cursor.getColumnIndex(_ID));
        this.artistName = cursor.getString(cursor.getColumnIndex(ARTIST_NAME));
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(ARTIST_NAME, artistName);
        return values;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}