package com.kelsos.mbrc.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.kelsos.mbrc.R;
import com.kelsos.mbrc.data.dbdata.Album;

public class AlbumCursorAdapter extends CursorAdapter {
    public AlbumCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.ui_list_dual, parent, false);
    }

    @Override public void bindView(View view, Context context, Cursor cursor) {
        final Album album = new Album(cursor);
        ((TextView) view.findViewById(R.id.line_one)).setText(album.getAlbumName());
        ((TextView) view.findViewById(R.id.line_two)).setText(album.getArtist());
    }
}
