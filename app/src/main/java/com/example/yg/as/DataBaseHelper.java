package com.example.yg.as;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.yg.as.Classes.Box;
import com.example.yg.as.Classes.Brand;
import com.example.yg.as.Classes.Client;
import com.example.yg.as.Classes.Contragent;
import com.example.yg.as.Classes.Defs;
import com.example.yg.as.Classes.Model;
import com.example.yg.as.Classes.OrderTrace;
import com.example.yg.as.Classes.OrderTraceDetail;
import com.example.yg.as.Classes.Season;
import com.example.yg.as.Classes.Sizing;
import com.example.yg.as.Classes.Sticker;

import static android.content.ContentValues.TAG;
import static android.text.TextUtils.regionMatches;
import static android.text.TextUtils.substring;
import static com.example.yg.as.Classes.Box.COLUMN_sentToMasterDate;
import static java.lang.String.valueOf;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static String DB_PATH = "";
    private static final int DB_VERSION = 2; //1
    protected final static String DB_NAME = "as.db";
    protected final static String DB_EXP = "as_exp.db";
    private static final String dtPattern = "dd.MM.yyyy HH:mm:ss";
    private static final String dayPattern = "dd.MM.yyyy";
    private static final String dtMin = "01.01.2018 00:00:00";
    private static final long lTimeOfFirstTrace = 1514754000;
    private static final long lOneDayInMilliseconds = 86400000;
    private static final String nameOfFirstTrace = "Запуск";
    private static final String nameOfSecondTrace = "Выход цеха";
    private String sdPath;
    protected static final String LOG_TAG = "asLogs";
    private SQLiteDatabase mDataBase;
    private final Context mContext;
    private boolean mNeedUpdate = false;
    private static DataBaseHelper sInstance;

    private Contragent contragent;
    private Client client;
    private Brand brand;
    private Model model;
    private Season season;
    private Sizing sizing;
    private OrderTrace orderTrace;
    private OrderTraceDetail orderTraceDetail;
    private Box box;
    private Sticker sticker;
    public Defs defs;
    public long lDaysOfSync = 1;
    public long lDateofSyncStart = 0;
    long lTempContragent ;

    private boolean isSDPresent(Context context) {

        File[] storage = ContextCompat.getExternalFilesDirs(context, null);
        if (storage.length > 1 && storage[0] != null && storage[1] != null) {
            sdPath = storage[1].toString();
            return true;
        }
        else
            return false;
    }

    private boolean isContextValid(Context context) {
        return context instanceof Activity && !((Activity) context).isFinishing();
    }

    public String exportDatabase(Context context, String localDbName, String backupDbName) {
        if (isContextValid(context))
            try {
                if (!isSDPresent(context)) {
                    Log.e(LOG_TAG, "SD is absent!");
                    return "SD карта отсутствует!";
                }

                File sd = new File(sdPath);

                if (sd.canWrite()) {

                    File currentDB = new File("/data/data/" + context.getPackageName() +"/databases/", localDbName);
                    File backupDB = new File(sd,  backupDbName);

                    if (currentDB.exists()) {
                        FileChannel src = new FileInputStream(currentDB).getChannel();
                        FileChannel dst = new FileOutputStream(backupDB).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();
                    }
                }
                else {
                    Log.e(LOG_TAG, "SD can't write data!");
                    return "SD не может записать данные!";
                }
            } catch (Exception e) {
                Log.d(LOG_TAG, "Export DB. Data transfer exception: " + e.getMessage());
            }
        else {
            Log.e(LOG_TAG, "Export DB: Context is not valid!");
            return "Export DB: Неверный контекст!";
        }

        return "Экспорт завершен. Ваш файл должен находиться по этому пути: ".concat("/data/data/" + context.getPackageName() +"/databases/");
    }
        private static Date getStartOfDayDate(Date date) {
            return org.apache.commons.lang3.time.DateUtils.truncate(date, Calendar.DATE);
        }
        private static long getStartOfDayLong(Date date) {
            return org.apache.commons.lang3.time.DateUtils.truncate(date, Calendar.DATE).getTime();
        }
        protected static String getDayTimeString(Date date) {
            return org.apache.commons.lang3.time.DateFormatUtils.format(date, dtPattern);
        }
        private static String getStartOfDayString(Date date) {
            return org.apache.commons.lang3.time.DateFormatUtils.format(date, dayPattern);
        }
    private String getStringDayTime(Long date) {
        return org.apache.commons.lang3.time.DateFormatUtils.format(date, dtPattern);
    }
    private String getStringDate(Long date) {
        return org.apache.commons.lang3.time.DateFormatUtils.format(date, dayPattern);
    }



    public static synchronized DataBaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DataBaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private static void tryCloseCursor(Cursor c) {
        if (c != null && !c.isClosed()) {
            c.close();
        }
    }

    public static String getUUID() {
        // Creating a random UUID (Universally unique identifier).
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public void BackUpDB() {
        try {
            // проверяем доступность SD
            if (!Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                Log.d(LOG_TAG, "SD-карта не доступна: " + Environment.getExternalStorageState());
                return;
            }
            File sd = Environment.getExternalStorageDirectory();
            if (sd.canWrite()) {
                String currentDBPath = "/data/data/" + mContext.getPackageName() + "/databases/" + DB_NAME;
                String backupDBPath = "backup" + DB_NAME;
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);
                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();

                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }else Log.d(LOG_TAG, "sd.canWrite(): " + sd.canWrite());
        } catch (Exception e) {

        }
    }

    private DataBaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

        if (android.os.Build.VERSION.SDK_INT >= 17)
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        else
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        this.mContext = context;

        try {
            this.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }
        try {
            mDataBase = this.getWritableDatabase();
            //mDataBase.setForeignKeyConstraintsEnabled(true);
            //this.close();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }
        defs = getDefs();
    }

    public void updateDataBase() throws IOException {
        if (mNeedUpdate) {
            File dbFile = new File(DB_PATH + DB_NAME);
            if (dbFile.exists())
                dbFile.delete();

            copyDataBase();

            mNeedUpdate = false;
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() {
        if (!checkDataBase()) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDBFile();
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    private void copyDBFile() throws IOException {
        InputStream mInput = mContext.getAssets().open(DB_NAME);
        //InputStream mInput = mContext.getResources().openRawResource(R.raw.info);
        OutputStream mOutput = new FileOutputStream(DB_PATH + DB_NAME);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0)
            mOutput.write(mBuffer, 0, mLength);
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            if (!mDataBase.isOpen()) mDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //db.setForeignKeyConstraintsEnabled(true);

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        //db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if ((newVersion>oldVersion)&(newVersion == 2))
            try {
                db.execSQL("PRAGMA foreign_keys = 0;");
                db.beginTransaction();
                db.execSQL("Update Box set sentToMasterDate=null where ROWID>11906;");

                db.setTransactionSuccessful();
            }
            finally {
                db.endTransaction();
                db.execSQL("PRAGMA foreign_keys = 1;");
            }
    }

    public Defs getDefs(){
        try {
            if (!this.mDataBase.isOpen())
                mDataBase = this.getReadableDatabase();
            Cursor cursor = mDataBase.rawQuery("SELECT Host_IP,Port,Device_id,contragent  FROM Defs ", null);
            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();
                defs = new Defs(cursor.getString(0),cursor.getString(1),cursor.getString(2), getContragent(cursor.getLong(3)));
            }

            //if (!this.mDataBase.isOpen()) mDataBase.close();
            tryCloseCursor(cursor);

            return defs;
        } catch (SQLException e) {
            Log.d(LOG_TAG, "getDefs SQLException = " + e.getMessage());
            return new Defs("localhost","4242", "0", getContragent((long)0));
        }
    }


    public Contragent getContragent(Long id) {
        try {
            if (!mDataBase.isOpen()) mDataBase = this.getReadableDatabase();
            Cursor cursor = mDataBase.rawQuery("SELECT * FROM Contragent WHERE _id = "+id.toString(), null);
            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();
                contragent = new Contragent(cursor.getLong(0), cursor.getString(1), cursor.getInt(2));
            }
            tryCloseCursor(cursor);
            //if (!mDataBase.inTransaction()) mDataBase.close();
            return contragent;
        } catch (SQLException e) {
            Log.d(LOG_TAG, "getContragent SQLException = " + e.getMessage());
            return new Contragent((long)0, "", 0);
        }
    }

    public long getContragent(String name){
        if (!mDataBase.isOpen()) mDataBase = this.getReadableDatabase();
        long num = 0;
        Cursor cursor = mDataBase.rawQuery("SELECT _id FROM Contragent Where name='"+name+"'", null);
        if ((cursor != null) & (cursor.getCount() != 0)) {
            cursor.moveToFirst();
            num = cursor.getInt(0);
        }
        tryCloseCursor(cursor);
        //if (!mDataBase.inTransaction()) mDataBase.close();
        return num;
    }

    public Contragent getContragentBySequence(int id) {
        Cursor cursor = null;
        try {
            try {
                if (!mDataBase.isOpen()) mDataBase = this.getReadableDatabase();
                cursor = mDataBase.rawQuery("SELECT * FROM Contragent WHERE sequence = " + String.valueOf(id), null);
                if ((cursor != null) & (cursor.getCount() != 0)) {
                    cursor.moveToFirst();
                    contragent = new Contragent(cursor.getLong(0), cursor.getString(1), cursor.getInt(2));
                }
                return contragent;
            } catch (SQLException e) {
                Log.d(LOG_TAG, "getContragent SQLException = " + e.getMessage());
                return new Contragent((long) 0, "", 0);
            }
        }finally {
            tryCloseCursor(cursor);
            //if (!mDataBase.inTransaction()) mDataBase.close();
            return contragent;
        }
    }
    public List<Contragent> getContragent() {
        List<Contragent> contragent = new ArrayList<>();
        try {
            if (!mDataBase.isOpen()) mDataBase = this.getReadableDatabase();
            Cursor cursor = mDataBase.rawQuery("SELECT * FROM Contragent WHERE (sequence not in (0,1))", null);
            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    contragent.add(new Contragent(cursor.getLong(0), cursor.getString(1), cursor.getInt(2)));
                    cursor.moveToNext();
                }
            }
            tryCloseCursor(cursor);
            //mDataBase.close();
            return contragent;
        } catch (SQLException e) {
            Log.d(LOG_TAG, "getContragent SQLException = " + e.getMessage());
            contragent.add(new Contragent((long)0, "", 0));
            return contragent;
        }
    }
//Client
    public Client getClient(Long id) {
        try {
            if (!mDataBase.isOpen()) mDataBase = this.getReadableDatabase();
            Cursor cursor = mDataBase.rawQuery("SELECT * FROM Client WHERE _id = "+id.toString(), null);
            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();
                client = new Client(cursor.getLong(0), cursor.getString(1));
            }
            tryCloseCursor(cursor);
            //mDataBase.close();
            return client;
        } catch (SQLException e) {
            Log.d(LOG_TAG, "getClient SQLException = " + e.getMessage());
            return new Client((long)0, "");
        }
    }

    public long setDefs(Defs defs) {
        this.defs = defs;
        long l = 0;
        try {
            try {
                if (!mDataBase.isOpen()) mDataBase = this.getWritableDatabase();
                mDataBase.beginTransactionNonExclusive();

                ContentValues values = new ContentValues();
                values.clear();
                values.put(Defs.COLUMN_Host_IP, defs.get_Host_IP());
                values.put(Defs.COLUMN_Port, defs.get_Port());
                values.put(Defs.COLUMN_Device_id, defs.getDevice_id());
                values.put(Defs.COLUMN_contragent, defs.getContragent().getId());
                String strFilter = "_id=1" ;
                l = mDataBase.update(Defs.table_Defs, values,strFilter, null);

                mDataBase.setTransactionSuccessful();
                mDataBase.endTransaction();
            } catch (SQLException e) {
                Log.d(LOG_TAG, "setDefs SQLException = " + e.getMessage());
            }
        }finally {

            //if (!mDataBase.inTransaction()) mDataBase.close();
            return l;
        }
    }


    public long setContragents (List<Contragent> contragent){
        long l = 0;
        try {
            try {
                if (!mDataBase.isOpen()) mDataBase = this.getWritableDatabase();
                mDataBase.beginTransactionNonExclusive();
                for (Contragent deps : contragent){
                    this.contragent = deps;

                    ContentValues values = new ContentValues();
                    values.clear();
                    values.put(Contragent.Column_id, this.contragent.getId());
                    values.put(Contragent.Column_name, this.contragent.getName());
                    values.put(Contragent.Column_sequence, this.contragent.getSequence());
                    l = mDataBase.insertWithOnConflict(Contragent.TABLE, null, values, 5);

                    Log.d(LOG_TAG, "Contragent. Cтрокa: " + deps.getId().toString());
                }
                mDataBase.setTransactionSuccessful();
                mDataBase.endTransaction();

            } catch (SQLException e) {
                Log.d(LOG_TAG, "Contragent insertWithOnConflict SQLException = " + e.getMessage());
                throw e;
            }
        }finally {

            //if (!mDataBase.inTransaction()) mDataBase.close();
            return l;
        }
    }

    public long setContragent (Contragent contragent){
        this.contragent = contragent;
        long l = 0;
        try {
            try {
                if (!mDataBase.isOpen()) mDataBase = this.getWritableDatabase();
                mDataBase.beginTransactionNonExclusive();
                ContentValues values = new ContentValues();
                values.clear();
                values.put(Contragent.Column_id, this.contragent.getId());
                values.put(Contragent.Column_name, this.contragent.getName());
                values.put(Contragent.Column_sequence, this.contragent.getSequence());
                l = mDataBase.insertWithOnConflict(Contragent.TABLE, null, values, 5);
                mDataBase.setTransactionSuccessful();
                mDataBase.endTransaction();

            } catch (SQLException e) {
                Log.d(LOG_TAG, "Contragent insertWithOnConflict SQLException = " + e.getMessage());
                throw e;
            }
        }finally {

            //if (!mDataBase.inTransaction()) mDataBase.close();
            return l;
        }
    }

    public long setClient (Client client){
        this.client = client;
        long l = 0;
        try {
            try {
                if (!mDataBase.isOpen()) mDataBase = this.getWritableDatabase();
                mDataBase.beginTransactionNonExclusive();

                ContentValues values = new ContentValues();
                values.clear();
                values.put(this.client.Column_id, this.client.getId());
                values.put(this.client.Column_name, this.client.getName());
                l = mDataBase.insertWithOnConflict(this.client.TABLE, null, values, 5);

                mDataBase.setTransactionSuccessful();
                mDataBase.endTransaction();

            } catch (SQLException e) {
                Log.d(LOG_TAG, "Client insertWithOnConflict SQLException = " + e.getMessage());
                throw e;
            }
        }finally {

            //if (!mDataBase.inTransaction()) mDataBase.close();
            return l;
        }
    }
//model
    public long setModel (Model model){
        this.model = model;
        long l = 0;
        try {
            try {
                if (!mDataBase.isOpen()) mDataBase = this.getWritableDatabase();
                mDataBase.beginTransactionNonExclusive();

                ContentValues values = new ContentValues();
                values.clear();
                values.put(this.model.Column_id, this.model.getId());
                values.put(this.model.Column_name, this.model.getName());
                l = mDataBase.insertWithOnConflict(this.model.TABLE, null, values, 5);

                mDataBase.setTransactionSuccessful();
                mDataBase.endTransaction();
            } catch (SQLException e) {
                Log.d(LOG_TAG, "Model insertWithOnConflict SQLException = " + e.getMessage());
                throw e;
            }
        }finally {

            //if (!mDataBase.inTransaction()) mDataBase.close();
            return l;
        }
    }


    //Brand
    public long setBrand (Brand brand){
        this.brand = brand;
        long l = 0;
        try {
            try {
                if (!mDataBase.isOpen()) mDataBase = this.getWritableDatabase();
                mDataBase.beginTransactionNonExclusive();

                ContentValues values = new ContentValues();
                values.clear();
                values.put(this.brand.Column_id, this.brand.getId());
                values.put(this.brand.Column_name, this.brand.getName());
                l = mDataBase.insertWithOnConflict(this.brand.TABLE, null, values, 5);

                mDataBase.setTransactionSuccessful();
                mDataBase.endTransaction();

            } catch (SQLException e) {
                Log.d(LOG_TAG, "Model insertWithOnConflict SQLException = " + e.getMessage());
                throw e;
            }
        }finally {

            //if (!mDataBase.inTransaction()) mDataBase.close();
            return l;
        }
    }

    //Season
    public long setSeason (Season season){
        this.season = season;
        long l = 0;
        try {
            try {
                if (!mDataBase.isOpen()) mDataBase = this.getWritableDatabase();
                mDataBase.beginTransactionNonExclusive();

                ContentValues values = new ContentValues();
                values.clear();
                values.put(this.season.Column_id, this.season.getId());
                values.put(this.season.Column_name, this.season.getName());
                l = mDataBase.insertWithOnConflict(this.season.TABLE, null, values, 5);

                mDataBase.setTransactionSuccessful();
                mDataBase.endTransaction();

            } catch (SQLException e) {
                Log.d(LOG_TAG, "Season insertWithOnConflict SQLException = " + e.getMessage());
                throw e;
            }
        }finally {

            //if (!mDataBase.inTransaction()) mDataBase.close();
            return l;
        }
    }

//Sizing
    public long setSizing (Sizing sizing){
        this.sizing = sizing;
        long l = 0;
        try {
            try {
                if (!mDataBase.isOpen()) mDataBase = this.getWritableDatabase();
                mDataBase.beginTransactionNonExclusive();

                ContentValues values = new ContentValues();
                values.clear();
                values.put(this.sizing.Column_id, this.sizing.getId());
                values.put(this.sizing.Column_name, this.sizing.getName());
                values.put(this.sizing.Column_quantity, this.sizing.getQuantity());
                l = mDataBase.insertWithOnConflict(this.sizing.TABLE, null, values, 5);

                mDataBase.setTransactionSuccessful();
                mDataBase.endTransaction();

            } catch (SQLException e) {
                Log.d(LOG_TAG, "Sizing insertWithOnConflict SQLException = " + e.getMessage());
                throw e;
            }
        }finally {

            //if (!mDataBase.inTransaction()) mDataBase.close();
            return l;
        }
    }

    //OrderTraceDetail
    public long setOrderTraceDetail (OrderTraceDetail orderTraceDetail){
        this.orderTraceDetail = orderTraceDetail;
        long l = 0;
        try {
            try {
                if (!mDataBase.isOpen()) mDataBase = this.getWritableDatabase();
                mDataBase.beginTransactionNonExclusive();

                ContentValues values = new ContentValues();
                values.clear();
                values.put(this.orderTraceDetail.Column_id, this.orderTraceDetail.getId());
                values.put(this.orderTraceDetail.Column_number, this.orderTraceDetail.getNumberOfOrder());
                values.put(this.orderTraceDetail.Column_quantity, this.orderTraceDetail.getQuantity());
                values.put(this.orderTraceDetail.Column_sizing, this.orderTraceDetail.getSizing().getId());
                values.put(this.orderTraceDetail.Column_model, this.orderTraceDetail.getModel().getId());
                values.put(this.orderTraceDetail.Column_orderTrace, this.orderTraceDetail.getOrderTrace().getId());

                l = mDataBase.insertWithOnConflict(this.orderTraceDetail.TABLE, null, values, 5);

                mDataBase.setTransactionSuccessful();
                mDataBase.endTransaction();

            } catch (SQLException e) {
                Log.d(LOG_TAG, "orderTraceDetail insertWithOnConflict SQLException = " + e.getMessage());
                throw e;
            }
        }finally {

            //if (!mDataBase.inTransaction()) mDataBase.close();
            return l;
        }
    }


    //OrderTrace
    public long setOrderTrace (OrderTrace orderTrace){
        this.orderTrace = orderTrace;
        long l = 0;
        try {
            try {
                if (!mDataBase.isOpen()) mDataBase = this.getWritableDatabase();
                mDataBase.beginTransactionNonExclusive();

                ContentValues values = new ContentValues();
                values.clear();
                values.put(this.orderTrace.Column_id, this.orderTrace.getId());
                values.put(this.orderTrace.Column_name, this.orderTrace.getNameOfTrace());
                values.put(this.orderTrace.Column_date, this.orderTrace.getDateOfTrace());
                values.put(this.orderTrace.Column_number, this.orderTrace.getNumberOfOrder());
                values.put(this.orderTrace.Column_sender, this.orderTrace.getSender().getId());
                values.put(this.orderTrace.Column_receiver, this.orderTrace.getReceiver().getId());
                values.put(this.orderTrace.Column_client, this.orderTrace.getClient().getId());
                values.put(this.orderTrace.Column_brand, this.orderTrace.getBrand().getId());
                values.put(this.orderTrace.Column_season, this.orderTrace.getSeason().getId());

                l = mDataBase.insertWithOnConflict(this.orderTrace.TABLE, null, values, 5);

                mDataBase.setTransactionSuccessful();
                mDataBase.endTransaction();
            } catch (SQLException e) {
                Log.d(LOG_TAG, "orderTrace insertWithOnConflict SQLException = " + e.getMessage());
                throw e;
            }
        }finally {

            //if (!mDataBase.inTransaction()) mDataBase.close();
            return l;
        }
    }

    public long getMaxOrderDate(){
        Cursor cursor = null;
        long nm = lTimeOfFirstTrace*1000;
        if (lDateofSyncStart != 0) nm = lDateofSyncStart; //дату начала синхронизации из настроек возвращаем
        else {
            try {
                if (!mDataBase.isOpen()) mDataBase = this.getReadableDatabase();
                cursor = mDataBase.rawQuery("SELECT MAX(dateOfTrace) FROM OrderTrace WHERE nameOfTrace=" + "'" + nameOfFirstTrace + "'", null);
                if ((cursor != null) & (cursor.getCount() != 0)) {
                    cursor.moveToFirst();
                    if (cursor.getLong(0) != 0) {
                        nm = cursor.getLong(0) - lDaysOfSync*lOneDayInMilliseconds;
                    }
                }
            } finally {
                tryCloseCursor(cursor);
                //mDataBase.close();
            }
        }
        return nm;
    }

    public long getMaxSentToMasterDateDate(){
        Cursor cursor = null;
        long nm = lTimeOfFirstTrace*1000;
        if (lDateofSyncStart != 0) nm = lDateofSyncStart; //дату начала синхронизации из настроек возвращаем
        else {
            try {
                if (!mDataBase.isOpen()) mDataBase = this.getReadableDatabase();
                cursor = mDataBase.rawQuery("SELECT MAX(sentToMasterDate) FROM Box", null);
                if ((cursor != null) & (cursor.getCount() != 0)) {
                    cursor.moveToFirst();
                    if (cursor.getLong(0) != 0) {
                        nm = cursor.getLong(0) - lDaysOfSync*lOneDayInMilliseconds;
                    }
                }
            } finally {
                tryCloseCursor(cursor);
                //mDataBase.close();
            }
        }
        return nm;
    }

    //OrderTrace
    public long setSticker (Sticker sticker){
        this.sticker = sticker;
        long l = 0;
        try {
            try {
                if (!mDataBase.isOpen()) mDataBase = this.getWritableDatabase();
                mDataBase.beginTransactionNonExclusive();

                ContentValues values = new ContentValues();
                values.clear();
                values.put(this.sticker.Column_id, this.sticker.getIdSticker());
                values.put(this.sticker.Column_orderTrace, this.sticker.getIdDoc());
                values.put(this.sticker.Column_orderTraceDetail, this.sticker.getIdQRCode());
                values.put(this.sticker.Column_quantity, this.sticker.getQuantity());

                l = mDataBase.insertWithOnConflict(this.sticker.TABLE, null, values, 5);

                mDataBase.setTransactionSuccessful();
                mDataBase.endTransaction();
            } catch (SQLException e) {
                Log.d(LOG_TAG, "setSticker insertWithOnConflict SQLException = " + e.getMessage());
                throw e;
            }
        }finally {

            //if (!mDataBase.inTransaction()) mDataBase.close();
            return l;
        }
    }

    public long getStickerMax(){
        Cursor cursor = null;
        long nm = (long)0;
        try {
            if (!mDataBase.isOpen()) mDataBase = this.getReadableDatabase();
            try {
                cursor = mDataBase.rawQuery("SELECT MAX(_id) FROM Sticker", null);
                if ((cursor != null) & (cursor.getCount() != 0)) {
                    cursor.moveToFirst();
                    if (cursor.getLong(0) != 0) nm = cursor.getLong(0);
                }
            }catch (Exception e){
                Log.d(LOG_TAG, "getStickerMax Exception" + e);
            }
        } finally {
            tryCloseCursor(cursor);
            //if (!mDataBase.inTransaction()) mDataBase.close();
            return nm;
        }
    }

    public Box findBoxByReceiverAndSticker(Long sticker, Long receiver){ // Sticker не заполяется, это описание коробки
        /*
        *     Box(int quantity, String model, Long model_id, String sizing, Long sizing_id, String numberOfOrder,
               String dateOfTrace, String receiver, Long receiver_id, String client, Long client_id,
               String brand, Long brand_id, String season, Long season_id, String sender, Long sender_id,
               Long parent_id, Long sentToMasterDate, Long sticker, Long id)
        * */
        Box box = new Box(0, "", (long)0,"", (long)0,"",
                "", "", (long)0,"", (long)0,"",
                (long)0,"",(long)0, "",(long)0, (long)0, (long)0, (long)0, (long)0);
        Cursor cursor = null;
        try {
            try {
                if (!mDataBase.isOpen()) mDataBase = this.getReadableDatabase();
                cursor = mDataBase.rawQuery("SELECT b.quantity, m.name, b.model, g.name, " +
                        " b.sizing, b.numberOfOrder, b.dateOfTrace, r.name, b.receiver, " +
                        " c.name, b.client, br.name, b.brand, "+
                        " s.name, b.season, se.name, b.sender, b.orderTrace, "+
                        " b.sentToMasterDate, b.sticker, b.ROWID " +
                        " FROM Box b, Contragent r, Contragent se, Client c, Brand br, Model m, Season s, Sizing g " +
                        " Where b.receiver="+valueOf(receiver)+" and b.sticker="+valueOf(sticker)+" "+
                        " and b.receiver=r._id and b.sender=se._id and b.client=c._id and b.brand=br._id and b.season=s._id and b.sizing=g._id and b.model=m._id", null);
                try {
                    if ((cursor != null) & (cursor.getCount() > 0)) {
                        Log.d(LOG_TAG, "getOneBox Records count = " + cursor.getCount());
                        cursor.moveToFirst();
                        box = new Box(cursor.getInt(0), cursor.getString(1), cursor.getLong(2), cursor.getString(3),
                                cursor.getLong(4), cursor.getString(5), getStringDayTime(cursor.getLong(6)), cursor.getString(7), cursor.getLong(8),
                                cursor.getString(9), cursor.getLong(10), cursor.getString(11), cursor.getLong(12),
                                cursor.getString(13), cursor.getLong(14), cursor.getString(15), cursor.getLong(16), cursor.getLong(17),
                                cursor.getLong(18), cursor.getLong(19), cursor.getLong(20));
                    }
                } catch (CursorIndexOutOfBoundsException e){
                    Log.d(LOG_TAG, "getOneBox CursorIndexOutOfBoundsException" + cursor.getCount());
                }
                return box;
            } catch (SQLException e) {
                Log.d(LOG_TAG, "getOneBox SQLException = " + e.getMessage());
            }
        }finally {
            tryCloseCursor(cursor);
            //if (!mDataBase.inTransaction()) mDataBase.close();
            return box;
        }
    }

    public Box findBoxByParams(long idDoc, long idDocDet, long sticker, int quantity){// Вытаскиваем данные принятой ранее коробки
        /*
        *     Box(int quantity, String model, Long model_id, String sizing, Long sizing_id, String numberOfOrder,
               String dateOfTrace, String receiver, Long receiver_id, String client, Long client_id,
               String brand, Long brand_id, String season, Long season_id, String sender, Long sender_id,
               Long parent_id, Long sentToMasterDate, Long sticker, Long id)

    public class OrderTrace {

    private Long id;
    private String nameOfTrace;
    private Long dateOfTrace;
    private Contragent sender ;
    private Contragent receiver ;
    private Client client ;
    private Brand brand ;
    private Season season;
    private Long numberOfOrder;
        * */
        Box box = new Box(0, "", (long)0,"", (long)0,"",
                "", "", (long)0,"", (long)0,"",
                (long)0,"",(long)0, "",(long)0, (long)0, (long)0, (long)0, (long)0);
        Cursor cursor = null;
        try {
            try {
                if (!mDataBase.isOpen()) mDataBase = this.getReadableDatabase();
                cursor = mDataBase.rawQuery("SELECT 0 as quantity, m.name, d.model, g.name, " +
                        " d.sizing, d.numberOfOrder, b.dateOfTrace, r.name as receiver, b.receiver as receiver_id, " +
                        " c.name, b.client, br.name, b.brand, "+
                        " s.name, b.season, b.sender as sender_id, b._id " +
                        " FROM OrderTrace b, OrderTraceDetail d, Contragent r, Client c, Brand br, Model m, Season s, Sizing g " +
                        " Where b._id="+valueOf(idDoc)+" and b._id=d.orderTrace and d._id="+valueOf(idDocDet)+
                        " and b.receiver=r._id and b.client=c._id and b.brand=br._id and b.season=s._id and d.sizing=g._id and d.model=m._id", null);
                try {
                    if ((cursor != null) & (cursor.getCount() > 0)) {
                        Log.d(LOG_TAG, "getOneBox Records count = " + cursor.getCount());
                        cursor.moveToFirst();
                        box = new Box(quantity, cursor.getString(1), cursor.getLong(2), cursor.getString(3),
                                cursor.getLong(4), cursor.getString(5), getStringDayTime(cursor.getLong(6)), cursor.getString(7), cursor.getLong(8),
                                cursor.getString(9), cursor.getLong(10), cursor.getString(11), cursor.getLong(12),
                                cursor.getString(13), cursor.getLong(14), "", cursor.getLong(15), cursor.getLong(16),
                                (long)0, sticker, (long)0);
                    }
                } catch (Exception e){
                    Log.d(LOG_TAG, "getOneBox Exception = " + e);
                }
                return box;
            } catch (SQLException e) {
                Log.d(LOG_TAG, "getOneBox SQLException = " + e.getMessage());
            }
        }finally {
            tryCloseCursor(cursor);
            //if (!mDataBase.inTransaction()) mDataBase.close();
            return box;
        }
    }


    public String countBox() {
        if (!mDataBase.isOpen()) mDataBase = this.getReadableDatabase();
        String result = "";
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery("SELECT * from OrderTrace where _id=909", null);
            try {
                if ((cursor != null) & (cursor.getCount() > 0)) {
                    Log.d(LOG_TAG, "lastbox Records count = " + cursor.getCount());
                    cursor.moveToFirst();
                    result = String.valueOf(cursor.getInt(0));
                }
            } catch (CursorIndexOutOfBoundsException e){
                Log.d(LOG_TAG, "getOneBox CursorIndexOutOfBoundsException" + cursor.getCount());
            }
            cursor = mDataBase.rawQuery("SELECT * from OrderTraceDetail where _id=3483 and OrderTrace=909", null);
            try {
                if ((cursor != null) & (cursor.getCount() > 0)) {
                    Log.d(LOG_TAG, "lastbox Records count = " + cursor.getCount());
                    cursor.moveToFirst();
                    result +=", "+ String.valueOf(cursor.getInt(0));
                }
            } catch (CursorIndexOutOfBoundsException e){
                Log.d(LOG_TAG, "getOneBox CursorIndexOutOfBoundsException" + cursor.getCount());
            }

            cursor = mDataBase.rawQuery("SELECT count(ROWID) FROM Box Where receiver="+valueOf(defs.getContragent().getId())
                    , null);
            try {
                if ((cursor != null) & (cursor.getCount() > 0)) {
                    Log.d(LOG_TAG, "getOneBox Records count = " + cursor.getCount());
                    cursor.moveToFirst();
                    result +=", "+ String.valueOf(cursor.getInt(0));
                }
            } catch (CursorIndexOutOfBoundsException e){
                Log.d(LOG_TAG, "getOneBox CursorIndexOutOfBoundsException" + cursor.getCount());
            }

        }finally {
            tryCloseCursor(cursor);
            //if (!mDataBase.inTransaction()) mDataBase.close();
            return result;
        }
    }
    public Box searchBox(long orderTrace, long orderTraceDetail, long sticker, int quantity) {
        Box box = new Box(quantity, "", (long)0,"", (long)0,"",
                "", "", (long)0,"", (long)0,"", (long)0,
                "",(long)0, "",(long)0, orderTrace, (long)0, sticker, (long)0);
        if (!mDataBase.isOpen()) mDataBase = this.getReadableDatabase();
        Cursor cursor = null;
        try {

            box = findBoxByReceiverAndSticker(sticker, defs.getContragent().getId());            //Проверяем есть запись в Box и если есть сразу возвращаемся
            if (box.getId() == 0) {
                box = findBoxByParams(orderTrace, orderTraceDetail, sticker, quantity); //Тут заполняем box коробки, которая не принималась ранее
                box.setDateOfTrace(getStringDayTime(new Date().getTime()));//Текущее время устанавливаем
            }
        }finally {
            tryCloseCursor(cursor);
            //if (!mDataBase.inTransaction()) mDataBase.close();
            return box;
        }
    }

    //Box
    public long setBox (Box box, Contragent contragent){
        this.box = box;
        long l = 0;
        try {
            try {
                ContentValues values = new ContentValues();
                values.clear();

                values.put(this.box.Column_quantity, this.box.getQuantity());
                values.put(this.box.Column_sizing, this.box.getSizing_id());
                values.put(this.box.Column_model, this.box.getModel_id());
                values.put(this.box.Column_number, this.box.getNumberOfOrder());
                values.put(this.box.Column_dateOfTrace, new Date().getTime());
                values.put(this.box.Column_receiver, contragent.getId());
                values.put(this.box.Column_client, this.box.getClient_id());
                values.put(this.box.Column_brand, this.box.getBrand_id());
                values.put(this.box.Column_season, this.box.getSeason_id());
                values.put(this.box.Column_sender, this.getContragentBySequence(contragent.getSequence()-1).getId());
                values.put(this.box.Column_orderTrace, this.box.getParent_id());
                values.put(this.box.COLUMN_sticker, this.box.getSticker());
                //Не заполняем COLUMN_sentToMasterDate, потому что не отправляли на сервер
                if (!mDataBase.isOpen()) mDataBase = this.getWritableDatabase();
                mDataBase.beginTransactionNonExclusive();
                l = mDataBase.insertWithOnConflict(this.box.TABLE, null, values, 5);


                int i = 1;
                while (((contragent.getSequence()-i)>1) &&
                        (findBoxByReceiverAndSticker(this.box.getSticker(), this.getContragentBySequence(contragent.getSequence()-i).getId()).getId() == 0 ))
                {
                   //l = this.setBox(box, this.getContragentBySequence(contragent.getSequence()-1));
                    values.put(this.box.Column_receiver, this.getContragentBySequence(contragent.getSequence()-i).getId());
                    i += 1;
                    values.put(this.box.Column_sender, this.getContragentBySequence(contragent.getSequence()-i).getId());

                    //if (!mDataBase.isOpen()) mDataBase = this.getWritableDatabase();
                    //mDataBase.beginTransactionNonExclusive();

                    l = mDataBase.insertWithOnConflict(this.box.TABLE, null, values, 5);

                    //mDataBase.setTransactionSuccessful();
                    //mDataBase.endTransaction();
                }
                mDataBase.setTransactionSuccessful();
                mDataBase.endTransaction();
            } catch (Exception e) {
                Log.d(LOG_TAG, "setBox insertWithOnConflict SQLException = " + e.getMessage());
                throw e;
            }
        }finally {
            //if (!mDataBase.inTransaction())                mDataBase.close();
            return l;
        }
    }

    //Box
    public boolean updBoxsentToMasterDate (String rowId, Long lDateToSet){
        //this.box = box;
        boolean b = false;
        try {
            try {
                Log.d(LOG_TAG, "updBoxsentToMasterDate rowId " + rowId);
                if (!mDataBase.isOpen()) mDataBase = this.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.clear();

                values.put(COLUMN_sentToMasterDate, lDateToSet);

                mDataBase.beginTransactionNonExclusive();
                b = (mDataBase.update(Box.TABLE, values,Box.Column_id +"='"+rowId+"'",null) > 0) ;
                mDataBase.setTransactionSuccessful();
                mDataBase.endTransaction();

            } catch (SQLiteException e) {
                Log.d(LOG_TAG, "updBoxsentToMasterDate SQLiteException " + e);
                return false;
            }
        }finally {
            //if (!mDataBase.inTransaction()) mDataBase.close();
            return b;
        }
    }

    public boolean deleteFromTable(final String TABLE, final String COLUMN, String Value){
        boolean b = false;
        try {
            try {
                if (!mDataBase.isOpen()) mDataBase = this.getWritableDatabase();
                mDataBase.beginTransactionNonExclusive();
                b = (mDataBase.delete(TABLE, COLUMN+"='"+Value+"' and sentToMasterDate is null",null) > 0) ;
                mDataBase.setTransactionSuccessful();
                mDataBase.endTransaction();

            } catch (SQLiteException e) {
                // TODO: handle exception
                return false;
            }
        }finally {
            //if (!mDataBase.inTransaction()) mDataBase.close();
            return b;
        }
    }
    public ArrayList<Box> getBoxNotSent() {
        ArrayList<Box> boxes = new ArrayList<Box>();
        Cursor cursor = null;
        try {
            if (!mDataBase.isOpen())  mDataBase = this.getReadableDatabase();
            Log.d(LOG_TAG, "getBoxNotSent database state = "+mDataBase.isOpen());
            cursor = mDataBase.rawQuery("SELECT *, ROWID " +
                    " FROM Box where ((" + COLUMN_sentToMasterDate + " IS NULL) OR (" + COLUMN_sentToMasterDate + " = '')) " +
                    " ORDER BY dateOfTrace, sender, receiver, client", null);

            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();
                Log.d(LOG_TAG, "getBoxNotSent cursor.getCount() = " + cursor.getCount());


                while (!cursor.isAfterLast()) {
                    box = new Box(cursor.getInt(0), "", cursor.getLong(1), "",
                            cursor.getLong(2), String.valueOf(cursor.getLong(3)), getStringDayTime(cursor.getLong(4)), "",
                            cursor.getLong(5),"", cursor.getLong(6), "", cursor.getLong(7),
                            "", cursor.getLong(8), "", cursor.getLong(9), cursor.getLong(10),
                            cursor.getLong(11), cursor.getLong(12), cursor.getLong(13));

//Закидываем в список
                    boxes.add(box);
                    //Переходим к следующеq
                    cursor.moveToNext();
                }
            }
        } finally {
            tryCloseCursor(cursor);
            //if (!mDataBase.isOpen()) mDataBase.close();
            return boxes;
        }
    }
    public ArrayList<Box> getBoxSentInPeriod() {
        ArrayList<Box> boxes = new ArrayList<Box>();
        Cursor cursor = null;
        Long periodStart = getStartOfDayLong(new Date());
        Long periodEnd = getStartOfDayLong(new Date())+1*lOneDayInMilliseconds;
        try {
            if (!mDataBase.isOpen())  mDataBase = this.getReadableDatabase();
            Log.d(LOG_TAG, "getBoxNotSent database state = "+mDataBase.isOpen());
            cursor = mDataBase.rawQuery("SELECT *, ROWID " +
                    " FROM Box where ((" + COLUMN_sentToMasterDate + " > "+periodStart+") AND (" + COLUMN_sentToMasterDate + " < "+periodEnd+")) " +
                    " ORDER BY dateOfTrace, sender, receiver, client", null);

            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();
                Log.d(LOG_TAG, "getBoxNotSent cursor.getCount() = " + cursor.getCount());


                while (!cursor.isAfterLast()) {
                    box = new Box(cursor.getInt(0), "", cursor.getLong(1), "",
                            cursor.getLong(2), String.valueOf(cursor.getLong(3)), getStringDayTime(cursor.getLong(4)), "", cursor.getLong(5),
                            "", cursor.getLong(6), "", cursor.getLong(7),
                            "", cursor.getLong(8), "", cursor.getLong(9), cursor.getLong(10),
                            cursor.getLong(11), cursor.getLong(12), cursor.getLong(13));

//Закидываем в список
                    boxes.add(box);
                    //Переходим к следующеq
                    cursor.moveToNext();
                }
            }
        } finally {
            tryCloseCursor(cursor);
            //if (!mDataBase.isOpen()) mDataBase.close();
            return boxes;
        }
    }
    public ArrayList<Box> getAllBox() {
        ArrayList<Box> boxes = new ArrayList<Box>();
        Cursor cursor = null;
        try {
            if (!mDataBase.isOpen())  mDataBase = this.getReadableDatabase();
            Log.d(LOG_TAG, "getBoxNotSent database state = "+mDataBase.isOpen());
            cursor = mDataBase.rawQuery("SELECT *, ROWID " +
                    " FROM Box", null);

            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();
                Log.d(LOG_TAG, "getBoxNotSent cursor.getCount() = " + cursor.getCount());


                while (!cursor.isAfterLast()) {
                    box = new Box(cursor.getInt(0), "", cursor.getLong(1), "",
                            cursor.getLong(2), String.valueOf(cursor.getLong(3)), getStringDayTime(cursor.getLong(4)), "", cursor.getLong(5),
                            "", cursor.getLong(6), "", cursor.getLong(7),
                            "", cursor.getLong(8), "", cursor.getLong(9), cursor.getLong(10),
                            cursor.getLong(11), cursor.getLong(12), cursor.getLong(13));

//Закидываем в список
                    boxes.add(box);
                    //Переходим к следующеq
                    cursor.moveToNext();
                }
            }
        } finally {
            tryCloseCursor(cursor);
            //if (!mDataBase.isOpen()) mDataBase.close();
            return boxes;
        }
    }
    public ArrayList<OrderTraceDetail> getAllOrderTraceDetail() {
        ArrayList<OrderTraceDetail> orderTraceDetails = new ArrayList<OrderTraceDetail>();
        Cursor cursor = null;
        try {
            if (!mDataBase.isOpen())  mDataBase = this.getReadableDatabase();
            Log.d(LOG_TAG, "getAllOrderTraceDetail database state = "+mDataBase.isOpen());
            cursor = mDataBase.rawQuery("SELECT * " +
                    " FROM OrderTraceDetail", null);

            if ((cursor != null) & (cursor.getCount() != 0)) {
                cursor.moveToFirst();
                Log.d(LOG_TAG, "getAllOrderTraceDetail cursor.getCount() = " + cursor.getCount());

/*OrderTraceDetail (Long id, Long parent_id, Long numberOfOrder,
                             Long model_id,
                             Long sizing_id, int quantity)*/
                while (!cursor.isAfterLast()) {
                    orderTraceDetail = new OrderTraceDetail(cursor.getLong(0),cursor.getLong(1), cursor.getLong(2),
                            cursor.getLong(3), cursor.getLong(4), cursor.getInt(5));

//Закидываем в список
                    orderTraceDetails.add(orderTraceDetail);
                    //Переходим к следующеq
                    cursor.moveToNext();
                }
            }
        } finally {
            tryCloseCursor(cursor);
            //if (!mDataBase.isOpen()) mDataBase.close();
            return orderTraceDetails;
        }
    }
    public Box lastBox() {
        Box box = new Box(0, "", (long)0,"", (long)0,"",
                "", "", (long)0,"", (long)0,"", (long)0,
                "",(long)0, "",(long)0, (long)0, (long)0, (long)0, (long)0);
        if (!mDataBase.isOpen()) mDataBase = this.getReadableDatabase();
        box.setBoxDesc("Принятых коробок нет.");
        Cursor cursor = null;
        try {
            try {
                cursor = mDataBase.rawQuery("SELECT b.quantity, m.name, b.model, g.name, " +
                        " b.sizing, b.numberOfOrder, b.dateOfTrace, r.name, b.receiver, " +
                        " c.name, b.client, br.name, b.brand, " +
                        " s.name, b.season, b.sender, b.orderTrace, " +
                        " b.sentToMasterDate, b.sticker, b.ROWID " +
                        " FROM Box b, Contragent r, Client c, Brand br, Model m, Season s, Sizing g " +
                        " Where b.ROWID=(SELECT max(x.ROWID) FROM Box x Where x.receiver=" + valueOf(defs.getContragent().getId()) + ") " +
                        " and b.receiver=r._id and b.client=c._id and b.brand=br._id and b.season=s._id and b.sizing=g._id and b.model=m._id", null);
            }  catch (SQLException e) {
                Log.d(LOG_TAG, "lastBox SQLException = " + e.getMessage());
            }
            try {
                if ((cursor != null) & (cursor.getCount() > 0)) {
                    Log.d(LOG_TAG, "lastbox Records count = " + cursor.getCount());
                    cursor.moveToFirst();
                    box = new Box(cursor.getInt(0), cursor.getString(1), cursor.getLong(2), cursor.getString(3),
                            cursor.getLong(4), cursor.getString(5), getStringDayTime(cursor.getLong(6)), cursor.getString(7), cursor.getLong(8),
                            cursor.getString(9), cursor.getLong(10), cursor.getString(11), cursor.getLong(12),
                            cursor.getString(13), cursor.getLong(14), "", cursor.getLong(15), cursor.getLong(16),
                            cursor.getLong(17), cursor.getLong(18), cursor.getLong(19));
                }
            } catch (CursorIndexOutOfBoundsException e){
                Log.d(LOG_TAG, "getOneBox CursorIndexOutOfBoundsException" + cursor.getCount());
            }
        }finally {
            tryCloseCursor(cursor);
            //if (!mDataBase.inTransaction()) mDataBase.close();
            return box;
        }
    }

    //list all boxes
    public ArrayList<HashMap<String, String>> listboxes() {
        ArrayList<HashMap<String, String>> readBoxes = new ArrayList<HashMap<String, String>>();

        if (!mDataBase.isOpen()) mDataBase = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            try {
                cursor = mDataBase.rawQuery("SELECT b.quantity, m.name, b.model, g.name, " +
                        " b.sizing, b.numberOfOrder, b.dateOfTrace, r.name, b.receiver, " +
                        " c.name, b.client, br.name, b.brand, " +
                        " s.name, b.season, b.sender, b.orderTrace, " +
                        " b.sentToMasterDate, b.sticker, b.ROWID " +
                        " FROM Box b, Contragent r, Client c, Brand br, Model m, Season s, Sizing g " +
                        " Where b.receiver=" + valueOf(defs.getContragent().getId()) +
                        " and b.receiver=r._id and b.client=c._id and b.brand=br._id and b.season=s._id " +
                        " and b.sizing=g._id and b.model=m._id " +
                        " and (("+COLUMN_sentToMasterDate+" IS NULL) OR ("+COLUMN_sentToMasterDate+" = ''))", null);
            }  catch (SQLException e) {
                Log.d(LOG_TAG, "listBox SQLException = " + e.getMessage());
            }
            try {
                if ((cursor != null) & (cursor.getCount() > 0)) {
                    Log.d(LOG_TAG, "listbox Records count = " + cursor.getCount());
                    cursor.moveToFirst();
                    //Пробегаем по всем коробкам
                    while (!cursor.isAfterLast()) {
                        HashMap readBox = new HashMap<String, Integer>();
                        String sTmp = null;
                        /*Box(int quantity, String model, Long model_id, String sizing, Long sizing_id, String numberOfOrder,
               String dateOfTrace, String receiver, Long receiver_id, String client, Long client_id,
               String brand, Long brand_id, String season, Long season_id, String sender, Long sender_id,
               Long parent_id, Long sentToMasterDate, Long sticker, Long id)
                       boxDesc = "Участок: " + receiver +"\n";
        boxDesc += "Время приемки: " + dateOfTrace +"\n";
        boxDesc += "№Модели: " + model;
        boxDesc += ". Бренд: " + brand + "\n";
        boxDesc += "Разм/ряд: " + sizing;
        boxDesc += ". Пар/кор: " + quantity + "\n";
        boxDesc += "Клиент: " + client + "\n";
        boxDesc += "Сезон: " + season + "\n";
        boxDesc += "№Партии: " + numberOfOrder;
               */
                        readBox.put("Ord", "Время приемки: " + getStringDayTime(cursor.getLong(6)) );
                        readBox.put("Cust", "Размерный ряд: " + (cursor.getString(3))
                                + ", Пар в кор.: " + cursor.getString(0) + ". Модель: " + cursor.getString(1) + ". Заказчик: " + cursor.getString(9) );
                        readBox.put("bId",cursor.getString(19)+"/bId");

                        //Закидываем в список
                        readBoxes.add(readBox);

                        //Переходим к следующеq

                        cursor.moveToNext();
                    }

                    box = new Box(cursor.getInt(0), cursor.getString(1), cursor.getLong(2), cursor.getString(3),
                            cursor.getLong(4), cursor.getString(5), getStringDayTime(cursor.getLong(6)), cursor.getString(7), cursor.getLong(8),
                            cursor.getString(9), cursor.getLong(10), cursor.getString(11), cursor.getLong(12),
                            cursor.getString(13), cursor.getLong(14), "", cursor.getLong(15), cursor.getLong(16),
                            cursor.getLong(17), cursor.getLong(18), cursor.getLong(19));
                }
            } catch (CursorIndexOutOfBoundsException e){
                Log.d(LOG_TAG, "listbox CursorIndexOutOfBoundsException" + cursor.getCount());
            }
        }finally {
            tryCloseCursor(cursor);
            //if (!mDataBase.inTransaction()) mDataBase.close();
            return readBoxes;
        }

    }
    public long importBox (Box box){
        this.box = box;
        long l = 0;
        try {
            try {
                ContentValues values = new ContentValues();
                values.clear();

                values.put(this.box.Column_quantity, this.box.getQuantity());
                values.put(this.box.Column_sizing, this.box.getSizing_id());
                values.put(this.box.Column_model, this.box.getModel_id());
                values.put(this.box.Column_number, this.box.getNumberOfOrder());
                values.put(this.box.Column_dateOfTrace, this.box.getDateOfTrace());
                values.put(this.box.Column_receiver, this.box.getReceiver_id());
                values.put(this.box.Column_client, this.box.getClient_id());
                values.put(this.box.Column_brand, this.box.getBrand_id());
                values.put(this.box.Column_season, this.box.getSeason_id());
                values.put(this.box.Column_sender, this.box.getSender_id());
                values.put(this.box.Column_orderTrace, this.box.getParent_id());
                values.put(this.box.COLUMN_sticker, this.box.getSticker());
                values.put(this.box.COLUMN_sentToMasterDate, this.box.getSentToMasterDate());

                //Не заполняем COLUMN_sentToMasterDate, потому что не отправляли на сервер
                if (!mDataBase.isOpen()) mDataBase = this.getWritableDatabase();
                mDataBase.beginTransactionNonExclusive();
                l = mDataBase.insertWithOnConflict(this.box.TABLE, null, values, 5);

                mDataBase.setTransactionSuccessful();
                mDataBase.endTransaction();
            } catch (Exception e) {
                Log.d(LOG_TAG, "importBox insertWithOnConflict SQLException = " + e.getMessage());
                throw e;
            }
        }finally {
            //if (!mDataBase.inTransaction())                mDataBase.close();
            return l;
        }
    }
}


