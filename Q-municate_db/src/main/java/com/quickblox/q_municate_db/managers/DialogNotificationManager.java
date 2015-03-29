package com.quickblox.q_municate_db.managers;

import android.os.Handler;
import android.os.Looper;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.dao.CommonDao;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.Callable;

public class DialogNotificationManager extends Observable implements CommonDao<DialogNotification> {

    public static final String OBSERVE_DIALOG_NOTIFICATION = "observe_dialog_notification";
    private static final String TAG = DialogNotificationManager.class.getSimpleName();

    private Handler handler;
    private Dao<DialogNotification, Integer> dialogNotificationDao;
    private Dao<Dialog, Integer> dialogDao;
    private Dao<DialogOccupant, Integer> dialogOccupantDao;

    public DialogNotificationManager(Dao<DialogNotification, Integer> dialogNotificationDao,
            Dao<Dialog, Integer> dialogDao, Dao<DialogOccupant, Integer> dialogOccupantDao) {
        handler = new Handler(Looper.getMainLooper());
        this.dialogNotificationDao = dialogNotificationDao;
        this.dialogDao = dialogDao;
        this.dialogOccupantDao = dialogOccupantDao;
    }

    @Override
    public void notifyObservers(final Object data) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                setChanged();
                DialogNotificationManager.super.notifyObservers(data);
            }
        });
    }

    @Override
    public Dao.CreateOrUpdateStatus createOrUpdate(DialogNotification item) {
        Dao.CreateOrUpdateStatus createOrUpdateStatus = null;

        try {
            createOrUpdateStatus = dialogNotificationDao.createOrUpdate(item);
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "createOrUpdate() - " + e.getMessage());
        }

        notifyObservers(OBSERVE_DIALOG_NOTIFICATION);

        return createOrUpdateStatus;
    }

    @Override
    public List<DialogNotification> getAll() {
        List<DialogNotification> dialogNotificationList = null;
        try {
            dialogNotificationList = dialogNotificationDao.queryForAll();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return dialogNotificationList;
    }

    @Override
    public DialogNotification get(int id) {
        DialogNotification dialogNotification = null;
        try {
            dialogNotification = dialogNotificationDao.queryForId(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return dialogNotification;
    }

    @Override
    public void update(DialogNotification item) {
        try {
            dialogNotificationDao.update(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        notifyObservers(OBSERVE_DIALOG_NOTIFICATION);
    }

    @Override
    public void delete(DialogNotification item) {
        try {
            dialogNotificationDao.delete(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        notifyObservers(OBSERVE_DIALOG_NOTIFICATION);
    }

    public void createOrUpdate(final Collection<DialogNotification> dialogNotificationsList) {
        try {
            dialogNotificationDao.callBatchTasks(new Callable<DialogNotification>() {
                @Override
                public DialogNotification call() throws Exception {
                    for (DialogNotification message : dialogNotificationsList) {
                        dialogNotificationDao.createOrUpdate(message);
                    }

                    notifyObservers(OBSERVE_DIALOG_NOTIFICATION);

                    return null;
                }
            });
        } catch (Exception e) {
            ErrorUtils.logError(TAG, "createOrUpdate() - " + e.getMessage());
        }
    }

    public List<DialogNotification> getDialogNotificationsByDialogId(String dialogId) {
        List<DialogNotification> dialogNotificationsList = new ArrayList<>();
        try {
            QueryBuilder<DialogNotification, Integer> messageQueryBuilder = dialogNotificationDao
                    .queryBuilder();

            QueryBuilder<DialogOccupant, Integer> dialogOccupantQueryBuilder = dialogOccupantDao
                    .queryBuilder();

            QueryBuilder<Dialog, Integer> dialogQueryBuilder = dialogDao.queryBuilder();
            dialogQueryBuilder.where().eq(Dialog.Column.ID, dialogId);

            dialogOccupantQueryBuilder.join(dialogQueryBuilder);
            messageQueryBuilder.join(dialogOccupantQueryBuilder);

            PreparedQuery<DialogNotification> preparedQuery = messageQueryBuilder.prepare();
            dialogNotificationsList = dialogNotificationDao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return dialogNotificationsList;
    }
}