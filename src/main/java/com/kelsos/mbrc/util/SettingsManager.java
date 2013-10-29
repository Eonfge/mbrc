package com.kelsos.mbrc.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kelsos.mbrc.BuildConfig;
import com.kelsos.mbrc.R;
import com.kelsos.mbrc.constants.UserInputEventType;
import com.kelsos.mbrc.model.ConnectionSettings;
import com.kelsos.mbrc.events.MessageEvent;
import com.kelsos.mbrc.events.general.SearchDefaultAction;
import com.kelsos.mbrc.events.ui.ChangeSettings;
import com.kelsos.mbrc.events.ui.ConnectionSettingsChanged;
import com.kelsos.mbrc.events.ui.DisplayDialog;
import com.kelsos.mbrc.events.ui.NotifyUser;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Date;

@Singleton
public class SettingsManager {
    private SharedPreferences mPreferences;
    private Context mContext;
    private MainThreadBusWrapper bus;
    private ArrayList<ConnectionSettings> mSettings;
    private ObjectMapper mMapper;
    private int defaultIndex;
    private boolean isFirstRun;

    @Inject public SettingsManager(Context context, SharedPreferences preferences, MainThreadBusWrapper bus,
                                   ObjectMapper mapper) {
        this.mPreferences = preferences;
        this.mContext = context;
        this.bus = bus;
        this.mMapper = mapper;
        bus.register(this);

        String sVal = preferences.getString(context.getString(R.string.settings_key_array), null);
        mSettings = new ArrayList<ConnectionSettings>();

        if (sVal != null && !sVal.equals("")) {
            ArrayNode node;
            try {
                node = mMapper.readValue(sVal, ArrayNode.class);
                for (int i = 0; i < node.size(); i++) {
                    JsonNode jNode = node.get(i);
                    ConnectionSettings settings = new ConnectionSettings(jNode);
                    mSettings.add(settings);
                }
            } catch (IOException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
        defaultIndex = mPreferences.getInt(mContext.getString(R.string.settings_key_default_index), 0);
        checkForFirstRunAfterUpdate();
    }

    public SocketAddress getSocketAddress() {
        String serverAddress = mPreferences.getString(mContext.getString(R.string.settings_key_hostname), null);
        int serverPort;

        try {
            serverPort = mPreferences.getInt(mContext.getString(R.string.settings_key_port), 0);
        } catch (ClassCastException castException) {
            serverPort = Integer.parseInt(mPreferences.getString(mContext.getString(R.string.settings_key_port), "0"));
        }


        if (nullOrEmpty(serverAddress) || serverPort == 0) {
            bus.post(new DisplayDialog(DisplayDialog.SETUP));
            return null;
        }

        return new InetSocketAddress(serverAddress, serverPort);
    }

    private boolean checkIfRemoteSettingsExist() {
        String serverAddress = mPreferences.getString(mContext.getString(R.string.settings_key_hostname), null);
        int serverPort;

        try {
            serverPort = mPreferences.getInt(mContext.getString(R.string.settings_key_port), 0);
        } catch (ClassCastException castException) {
            serverPort = Integer.parseInt(mPreferences.getString(mContext.getString(R.string.settings_key_port), "0"));
        }

        return !(nullOrEmpty(serverAddress) || serverPort == 0);
    }

    public boolean isVolumeReducedOnRinging() {
        return mPreferences.getBoolean(mContext.getString(R.string.settings_key_reduce_volume), false);
    }

    private static boolean nullOrEmpty(String string) {
        return string == null || string.equals("");
    }

    public boolean isNotificationControlEnabled() {
        return mPreferences.getBoolean(mContext.getString(R.string.settings_key_notification_control), true);
    }

    private void storeSettings() {
        SharedPreferences.Editor editor = mPreferences.edit();
        try {
            editor.putString(mContext.getString(R.string.settings_key_array), mMapper.writeValueAsString(mSettings));
            editor.commit();
            bus.post(new ConnectionSettingsChanged(mSettings, 0));
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                Log.d("mbrc-log", "Settings store", e);
            }
        }
    }

    @Subscribe public void handleConnectionSettings(ConnectionSettings settings) {
        if (settings.getIndex() < 0) {
            if (!mSettings.contains(settings)) {
                if (mSettings.size() == 0) {
                    updateDefault(0, settings);
                    bus.post(new MessageEvent(UserInputEventType.SettingsChanged));
                }
                mSettings.add(settings);
                storeSettings();
            } else {
                bus.post(new NotifyUser(R.string.notification_settings_stored));
            }
        } else {
            mSettings.set(settings.getIndex(), settings);
            if (settings.getIndex() == defaultIndex) {
                bus.post(new MessageEvent(UserInputEventType.SettingsChanged));
            }
            storeSettings();
        }
    }

    private void updateDefault(int index, ConnectionSettings settings) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(mContext.getString(R.string.settings_key_hostname), settings.getAddress());
        editor.putInt(mContext.getString(R.string.settings_key_port), settings.getPort());
        editor.putInt(mContext.getString(R.string.settings_key_default_index), index);
        editor.commit();
        defaultIndex = index;
    }

    public void setLastUpdated(Date lastChecked) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(mContext.getString(R.string.settings_key_last_update_check), lastChecked.getTime());
        editor.commit();
    }

    public Date getLastUpdated() {
        return new Date(mPreferences.getLong(mContext.getString(R.string.settings_key_last_update_check), 0));
    }

    @Produce public ConnectionSettingsChanged produceConnectionSettings() {
        return new ConnectionSettingsChanged(mSettings, defaultIndex);
    }

    @Subscribe public void handleSettingsChange(ChangeSettings event) {
        switch (event.getAction()) {
            case DELETE:
                mSettings.remove(event.getIndex());
                if (event.getIndex() == defaultIndex && mSettings.size() > 0) {
                    updateDefault(0, mSettings.get(0));
                    bus.post(new MessageEvent(UserInputEventType.SettingsChanged));
                } else {
                    updateDefault(0, new ConnectionSettings());
                }
                storeSettings();
                break;
            case EDIT:
                break;
            case DEFAULT:
                ConnectionSettings settings = mSettings.get(event.getIndex());
                updateDefault(event.getIndex(), settings);
                bus.post(new ConnectionSettingsChanged(mSettings, event.getIndex()));
                bus.post(new MessageEvent(UserInputEventType.SettingsChanged));
                break;
        }
    }

    @Produce public SearchDefaultAction produceAction() {
        return new SearchDefaultAction(mPreferences.getString(
                mContext.getString(R.string.settings_search_default_key),
                mContext.getString(R.string.search_click_default_value)));
    }

    @Produce public DisplayDialog produceDisplayDialog() {
        int run = DisplayDialog.NONE;
        if (isFirstRun && checkIfRemoteSettingsExist()) {
            run = DisplayDialog.UPGRADE;
        } else if (isFirstRun && !checkIfRemoteSettingsExist()) {
            run = DisplayDialog.INSTALL;
        }
        isFirstRun = false;
        return new DisplayDialog(run);
    }
    
    private void checkForFirstRunAfterUpdate() {
        try {
            long lastVersionCode = mPreferences.getLong(mContext.
                    getString(R.string.settings_key_last_version_run), 0);
            long currentVersion = RemoteUtils.getVersionCode(mContext);

            if (lastVersionCode < currentVersion) {
                isFirstRun = true;

                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putLong(mContext.getString(R.string.settings_key_last_version_run), currentVersion);
                editor.commit();

                if (BuildConfig.DEBUG) {
                    Log.d("mbrc-log", "update or fresh install");
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            if (BuildConfig.DEBUG) {
                Log.d("mbrc-log", "check for first run", e);
            }
        }
    }
}
