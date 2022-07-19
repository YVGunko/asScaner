package com.example.yg.as;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.yg.as.Classes.Box;
import com.example.yg.as.Classes.OrderTraceDetail;
import com.example.yg.as.Classes.Defs;
import com.example.yg.as.service.ApiUtils;
import com.example.yg.as.service.OrderTraceService;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.yg.as.DataBaseHelper.LOG_TAG;

public class SettingsActivity extends AppCompatActivity  {

    private OrderTraceService connectionService;
    EditText host_v, numberEdit;
    private DataBaseHelper mDBHelper;
    private Defs defs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mDBHelper = DataBaseHelper.getInstance(this);
        host_v = (EditText) findViewById(R.id.host);
        numberEdit = (EditText) findViewById(R.id.numberEdit);


        // Выборка настроек по умолчанию
        defs = mDBHelper.getDefs();
        try {
            String url = DataBaseHelper.getInstance(this).defs.getUrl();
            connectionService = ApiUtils.getOrderService(url);
            host_v.setText(DataBaseHelper.getInstance(this).defs.get_Host_IP());
            ocl_check(findViewById(R.id.check));
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();
        // Операции для выбранного пункта меню
        switch (id) {
            case R.id.action_receive_spr:
                try {
                    SettingsActivity.SyncIncoData task = new SettingsActivity.SyncIncoData();
                    task.execute(new String[]{null});

                } catch (Exception e) {
                    Log.d("1", "Ответ сервера на запрос новых заказов: " + e.getMessage());
                }
                return true;
            case R.id.action_copy_db:
                try {
                    DataBaseHelper.getInstance(this).BackUpDB(); //Copy DB to SDcard

                } catch (Exception e) {
                    Log.d("1", "Ответ сервера на запрос новых заказов: " + e.getMessage());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void ocl_check(View v) { //Вызов активности проверки подключения к серверу
        Toast.makeText(getApplicationContext(), "Проверка подключения к серверу... Подождите.", Toast.LENGTH_SHORT).show();
        checkConnection();
    }

    public void checkConnection() {
        String url = DataBaseHelper.getInstance(this).defs.getUrl();
        connectionService = ApiUtils.getOrderService(url);
        connectionService.checkConnection().enqueue(new Callback<Object>() {

            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Введенный URL недоступен! Введите верный!", Toast.LENGTH_SHORT).show();
                    host_v.requestFocus();
                } else {
                    Toast.makeText(getApplicationContext(), "Соединение установлено!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Введенный URL недоступен! Введите верный!", Toast.LENGTH_SHORT).show();
                host_v.requestFocus();
            }
        });
    }

    public void ocl_bSave(View v) {
        final String ip = host_v.getText().toString();

        defs = new Defs(ip, "4242", mDBHelper.getContragent(mDBHelper.lTempContragent));

        if (mDBHelper.setDefs(defs) != 0) {
            Toast.makeText(getApplicationContext(), "Сохранено.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Ошибка при сохранении.", Toast.LENGTH_SHORT).show();
        }
        defs = mDBHelper.getDefs();

        numberEdit = (EditText) findViewById(R.id.numberEdit);

        if (Long.valueOf(numberEdit.getText().toString()) != mDBHelper.lDaysOfSync) {
            mDBHelper.lDaysOfSync = Long.valueOf(numberEdit.getText().toString());
        }
    }

    private class SyncIncoData extends AsyncTask<String, Integer, String> {
        Integer counter;

        @Override
        protected String doInBackground(String... urls) {
            counter = 0;
            if (urls[0]=="ExportTrace" ) {
                final Long lDateToSet = new Date().getTime();
                try {
                    ApiUtils.getOrderService(mDBHelper.defs.getUrl()).exportOrderTraceDetail(mDBHelper.getAllOrderTraceDetail()).enqueue(new Callback<List<OrderTraceDetail>>() {
                        @Override
                        public void onResponse(Call<List<OrderTraceDetail>> call, Response<List<OrderTraceDetail>> response) {

                            if (response.isSuccessful()) {
                                if (response.body().size() != 0) {
                                    Log.d(LOG_TAG, "postBox. Принято строк: " + response.body().size());
                                }

                                for (OrderTraceDetail deps : response.body()) {
                                    mDBHelper.updBoxsentToMasterDate(String.valueOf(deps.getId()), lDateToSet);
                                }
                            }
                            counter = counter + 1; //9
                            publishProgress(counter);
                        }

                        @Override
                        public void onFailure(Call<List<OrderTraceDetail>> call, Throwable t) {
                            Log.d(LOG_TAG, "postBox. onFailure: " + t.getMessage());
                            publishProgress(0);
                        }
                    });

                } catch (Exception e) {
                    Log.d(LOG_TAG, "Error : " + e.getMessage());
                }
            }

            if (urls[0]=="Exp" ) {
                final Long lDateToSet = new Date().getTime();
                try {
                    ApiUtils.getOrderService(mDBHelper.defs.getUrl()).postBox(mDBHelper.getBoxSentInPeriod()).enqueue(new Callback<List<Box>>() {
                        @Override
                        public void onResponse(Call<List<Box>> call, Response<List<Box>> response) {

                            if (response.isSuccessful()) {
                                if (response.body().size() != 0) {
                                    Log.d(LOG_TAG, "postBox. Принято строк: " + response.body().size());
                                }

                                for (Box deps : response.body()) {
                                    mDBHelper.updBoxsentToMasterDate(String.valueOf(deps.getId()), lDateToSet);
                                }
                            }
                            counter = counter + 1; //9
                            publishProgress(counter);
                        }

                        @Override
                        public void onFailure(Call<List<Box>> call, Throwable t) {
                            Log.d(LOG_TAG, "postBox. onFailure: " + t.getMessage());
                            publishProgress(0);
                        }
                    });

                } catch (Exception e) {
                    Log.d(LOG_TAG, "Error : " + e.getMessage());
                }
            }
            if (urls[0]=="Save" ) {
                try {
                    ApiUtils.getOrderService(mDBHelper.defs.getUrl()).exportBox(mDBHelper.getAllBox()).enqueue(new Callback<List<Box>>() {
                        @Override
                        public void onResponse(Call<List<Box>> call, Response<List<Box>> response) {

                            if (response.isSuccessful()) {
                                if (response.body().size() != 0) {
                                    Log.d(LOG_TAG, "exportBox. Принято строк: " + response.body().size());
                                }
                            }
                            counter = response.body().size();
                            publishProgress(counter);
                        }

                        @Override
                        public void onFailure(Call<List<Box>> call, Throwable t) {
                            Log.d(LOG_TAG, "exportBox. onFailure: " + t.getMessage());
                            publishProgress(0);
                        }
                    });

                } catch (Exception e) {
                    Log.d(LOG_TAG, "Error : " + e.getMessage());
                }
            }
            if ((urls[0]=="Load" )&(mDBHelper.getAllBox().size()==0)) {
                try {
                    ApiUtils.getOrderService(mDBHelper.defs.getUrl()).importBox().enqueue(new Callback<List<Box>>() {
                        @Override
                        public void onResponse(Call<List<Box>> call, Response<List<Box>> response) {

                            if (response.isSuccessful()) {
                                for (Box deps : response.body())
                                    mDBHelper.importBox(deps);
                                if (response.body().size() != 0) {
                                    Log.d(LOG_TAG, "importBox. Принято строк: " + response.body().size());
                                }
                            }
                            counter = response.body().size();
                            publishProgress(counter);
                        }

                        @Override
                        public void onFailure(Call<List<Box>> call, Throwable t) {
                            Log.d(LOG_TAG, "importBox. onFailure: " + t.getMessage());
                            publishProgress(0);
                        }
                    });

                } catch (Exception e) {
                    Log.d(LOG_TAG, "Error : " + e.getMessage());
                }
            }else{
                counter = 0;
                publishProgress(counter);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (values[0]==0)
                    Toast.makeText(getApplicationContext(), "Ошибка при выполнении.", Toast.LENGTH_SHORT).show();
            else
                    Toast.makeText(getApplicationContext(), "Выполнено успешно.", Toast.LENGTH_SHORT).show();
        }
    }

    public void ocl_bExportTrace(View v) {
        try {
            SettingsActivity.SyncIncoData task = new SettingsActivity.SyncIncoData();
            task.execute(new String[]{"ExportTrace"});

        } catch (Exception e) {
            Log.d(LOG_TAG+". ExportTrace", "ExportTrace exception:: " + e.getMessage());
        }
    }
    public void ocl_bSaveBox(View v) {
        try {
            SettingsActivity.SyncIncoData task = new SettingsActivity.SyncIncoData();
            task.execute(new String[]{"Save"});

        } catch (Exception e) {
            Log.d(LOG_TAG+". Save", "exportBox exception: " + e.getMessage());
        }
    }

    public void ocl_bLoadBox(View v) {
        try {
            SettingsActivity.SyncIncoData task = new SettingsActivity.SyncIncoData();
            task.execute(new String[]{"Load"});

        } catch (Exception e) {
            Log.d(LOG_TAG+". Load", "importBox exception: " + e.getMessage());
        }
    }

    public void ocl_bExpBox(View v) {
        try {
            SettingsActivity.SyncIncoData task = new SettingsActivity.SyncIncoData();
            task.execute(new String[]{"Exp"});

        } catch (Exception e) {
            Log.d(LOG_TAG+". ExpBox", "Export Box exception: " + e.getMessage());
        }
    }

    public void ocl_bExpDb(View v) {
        try {
            final String backUpFile = mDBHelper.getDayTimeString(new Date())
                    .replaceAll("[^a-zA-Z0-9]", "");
            final String result =
                    mDBHelper.exportDatabase(this, mDBHelper.DB_NAME, backUpFile.concat(".db"));
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.d(LOG_TAG+". ExpDB", "Export DB exception: " + e.getMessage());
        }
    }

}
