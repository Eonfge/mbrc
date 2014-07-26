package com.kelsos.mbrc.data;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Base64;
import android.util.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kelsos.mbrc.BuildConfig;
import com.kelsos.mbrc.constants.ProtocolEventType;
import com.kelsos.mbrc.data.db.LibraryDbHelper;
import com.kelsos.mbrc.data.dbdata.*;
import com.kelsos.mbrc.events.MessageEvent;
import com.kelsos.mbrc.net.Protocol;
import com.kelsos.mbrc.util.DaoSessionManager;
import com.kelsos.mbrc.util.NotificationService;
import com.squareup.otto.Bus;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class SyncHandler {

    private Context mContext;
    private NotificationService mNotification;
    private Bus bus;
    private LibraryDbHelper dbHelper;
    private DaoSessionManager mDaoSessionManager;

    @Inject public SyncHandler(Context mContext, NotificationService mNotification, Bus bus, DaoSessionManager mDaoSessionManager) {
        this.mContext = mContext;
        this.mNotification = mNotification;
        this.bus = bus;
        dbHelper = new LibraryDbHelper(mContext);
        this.mDaoSessionManager = mDaoSessionManager;

    }

    /**
     * Sends a request to get the next part of the library data.
     * @param total Represents the total number of tracks available.
     * @param offset Represents the index of the starting track.
     * @param limit Represents the number of tracks contained to the message.
     */
    public void getNextBatch(int total, int offset, int limit) {

        if ((offset + limit) < total) {

            mNotification.librarySyncNotification(total, offset);
            Map<String, Object> syncData = new HashMap<>();
            syncData.put("type", "meta");
            syncData.put("offset", offset + limit);
            syncData.put("limit", limit);
            bus.post(new MessageEvent(ProtocolEventType.USER_ACTION,
                    new UserAction(Protocol.LIBRARY, syncData)));
        } else {
            mNotification.librarySyncNotification(total, total);
            ContentResolver contentResolver = mContext.getContentResolver();
            contentResolver.notifyChange(Track.getContentUri(), null, false);
            contentResolver.notifyChange(Album.getContentUri(), null, false);
            contentResolver.notifyChange(Artist.getContentUri(), null, false);
            contentResolver.notifyChange(Genre.getContentUri(), null, false);
            requestNextBatch(0,0,5);
        }

    }

    public void setCovers(final List<Cover> list) {
        dbHelper.updateCoverHashes(list);
    }

    public void updateCover(String image, String hash) {
        if (hash == null || hash.equals("")) {
            return;
        }
        FileOutputStream outputStream;
        try {
            outputStream = mContext.openFileOutput(hash, Context.MODE_PRIVATE);
            outputStream.write(Base64.decode(image, Base64.DEFAULT));
            outputStream.close();
        }  catch (Exception ex) {
            if (BuildConfig.DEBUG) {
                Log.d(BuildConfig.PACKAGE_NAME, "saving cover", ex);
            }
        }
    }

    public void processBatch(final List<com.kelsos.mbrc.dao.Track> trackList) {
        mDaoSessionManager.getDaoSession().runInTx(new Runnable() {
            @Override
            public void run() {
                mDaoSessionManager.getDaoSession().getTrackDao().insertInTx(trackList);
            }
        });
    }

    public void requestNextBatch(int total, int offset, int limit) {
        Map<String, Object> syncData = new HashMap<>();
        syncData.put("type", "cover");
        syncData.put("offset", offset + limit);
        syncData.put("limit", limit);
        bus.post(new MessageEvent(ProtocolEventType.USER_ACTION,
                new UserAction(Protocol.LIBRARY, syncData)));

    }

    /**
     * Requests the Queue tracks from the plugin
     * @param limit Represents the number of tracks contained to the message.
     * @param offset Represents the index of the starting track.
     */
    public void getQueueTracks(int limit, int offset) {
        HashMap<String, Object> message = new HashMap<>();
        message.put("type", "list");
        message.put("offset", offset + limit);
        message.put("limit", limit);
        bus.post(new MessageEvent(ProtocolEventType.USER_ACTION,
                new UserAction(Protocol.NOW_PLAYING, message)));
    }
}
