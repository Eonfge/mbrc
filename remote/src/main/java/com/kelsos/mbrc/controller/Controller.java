package com.kelsos.mbrc.controller;

import android.content.Intent;
import android.os.IBinder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.kelsos.mbrc.BuildConfig;
import com.kelsos.mbrc.constants.UserInputEventType;
import com.kelsos.mbrc.events.MessageEvent;
import com.kelsos.mbrc.interfaces.ICommand;
import com.kelsos.mbrc.interfaces.IEvent;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import roboguice.service.RoboService;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class Controller extends RoboService {
    private static final Logger logger = LoggerManager.getLogger();
    @Inject private Injector injector;
    @Inject private Bus bus;

    private Map<String, Class<? extends ICommand>> commandMap;

    public Controller() {
        if (BuildConfig.DEBUG) {
            logger.d( "Controller initialized");
        }
    }

    @Override public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Registers an association between an event type (String) and a command
     * @param type The event type associated.
     * @param command The command associated with the event type.
     */
    public void register(String type, Class<? extends ICommand> command) {
        if (commandMap == null) {
            commandMap = new HashMap<>();
        }
        if (!commandMap.containsKey(type)) {
            commandMap.put(type, command);
        }
    }

    /**
     * Removes an association between an event type (String) and a command
     * @param type The event The event type associated.
     * @param command The command associated with the event type.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void unregister(String type, Class<? extends ICommand> command) {
        if (commandMap.containsKey(type) && commandMap.get(type).equals(command)) {
            commandMap.remove(type);
        }
    }

    /**
     * Subscriber - Handler to the MessageEvents posted through the event bus.
     * Responsible for passing the event to the executeCommand function.
     * @param event The {@link MessageEvent} passed to the controller
     */
    @Subscribe public void handleUserActionEvents(MessageEvent event) {
        executeCommand(event);
    }

    /**
     * Checks for a Command associated with the event passed.
     * Instantiates the Command and executes it.
     * @param event the event passed for execution
     */
    private void executeCommand(final IEvent event) {
        Class<? extends ICommand> commandClass = this.commandMap.get(event.getType());
        if (commandClass == null) {
            return;
        }
        try {
            final ICommand commandInstance = injector.getInstance(commandClass);
            if (commandInstance == null) {
                return;
            }

            commandInstance.execute(event);

        } catch (Exception ex) {
            if (BuildConfig.DEBUG) {
                logger.d( "executing command for type: \t" + event.getType(), ex);
                logger.d( "command data: \t" + event.getData());
            }
        }

    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        Configuration.initialize(this);
        bus.register(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override public void onDestroy() {
        executeCommand(new MessageEvent(UserInputEventType.CANCEL_NOTIFICATION));
        super.onDestroy();
    }
}
