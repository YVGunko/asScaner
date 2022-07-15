package com.example.yg.as;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.yg.as.Classes.Box;
import com.example.yg.as.Classes.Brand;
import com.example.yg.as.Classes.Client;
import com.example.yg.as.Classes.Contragent;
import com.example.yg.as.Classes.Model;
import com.example.yg.as.Classes.OrderTraceDetail;
import com.example.yg.as.Classes.Season;
import com.example.yg.as.Classes.Sizing;
import com.example.yg.as.Classes.Sticker;
import com.example.yg.as.service.ApiUtils;

public class UpdateActivity extends AppCompatActivity {
    private DataBaseHelper mDBHelper;
    ProgressBar pbar;
    Button buttonStart;
    ListView listView;

    String[] checkNameList = {
            "Участки",
            "Клиенты",
            "Модели",
            "Бренды",
            "Сезоны",
            "Ростовки",
            "Запуски заказов",
            "Карты коробок",
            "Остатки участков"
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        mDBHelper = DataBaseHelper.getInstance(this);
        buttonStart = (Button) findViewById(R.id.buttonStart);
        pbar = (ProgressBar) findViewById(R.id.progressBarpbar);
        listView = (ListView) findViewById(R.id.listView);

        // используем адаптер данных
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_checked, checkNameList);

        listView.setAdapter(adapter);
        listView.setEnabled(false);
    }


    @Override
    protected void onResume() {
        super.onResume();
        this.setTitle("Синхронизация данных. ");
    }

    private class SyncIncoData extends AsyncTask<String, Integer, String> {
        Integer counter;

        @Override
        protected String doInBackground(String... urls) {
            counter = 0;
            final Long lDateToSet = new Date().getTime();
            try {
                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getAllContragent().enqueue(new Callback<List<Contragent>>() {
                    @Override
                    public void onResponse(Call<List<Contragent>> call, Response<List<Contragent>> response) {

                        Log.d(mDBHelper.LOG_TAG, "Contragent. Принято строк: " + response.body().size());
                        if (response.isSuccessful()) {mDBHelper.setContragents(response.body());

                        }
                        counter = counter + 1; //1
                        publishProgress(counter);
                    }

                    @Override
                    public void onFailure(Call<List<Contragent>> call, Throwable t) {
                        Log.d(mDBHelper.LOG_TAG, "Contragent. onFailure: " + t.getMessage());
                        publishProgress(0);
                    }
                });

                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getAllClient().enqueue(new Callback<List<Client>>() {
                    @Override
                    public void onResponse(Call<List<Client>> call, Response<List<Client>> response) {
                        if (response.isSuccessful()) {
                            for (Client deps : response.body())
                                mDBHelper.setClient(deps);
                            if (response.body().size() != 0)
                                Log.d(mDBHelper.LOG_TAG, "Client. Принято строк: " + response.body().size());

                        }
                        counter = counter + 1; //2
                        publishProgress(counter);
                    }

                    @Override
                    public void onFailure(Call<List<Client>> call, Throwable t) {
                        Log.d(mDBHelper.LOG_TAG, "Client. onFailure: " + t.getMessage());
                        publishProgress(0);
                    }
                });

                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getAllModel().enqueue(new Callback<List<Model>>() {
                    @Override
                    public void onResponse(Call<List<Model>> call, Response<List<Model>> response) {

                        if (response.isSuccessful()) {
                            for (Model deps : response.body())
                                mDBHelper.setModel(deps);
                            if (response.body().size() != 0)

                                Log.d(mDBHelper.LOG_TAG, "Model. Принято строк: " + response.body().size());
                        }
                        counter = counter + 1; //3
                        publishProgress(counter);
                    }

                    @Override
                    public void onFailure(Call<List<Model>> call, Throwable t) {
                        Log.d(mDBHelper.LOG_TAG, "Model. onFailure: " + t.getMessage());
                        publishProgress(0);
                    }
                });

                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getAllBrand().enqueue(new Callback<List<Brand>>() {
                    @Override
                    public void onResponse(Call<List<Brand>> call, Response<List<Brand>> response) {
                        if (response.isSuccessful()) {
                            for (Brand deps : response.body())
                                mDBHelper.setBrand(deps);
                            if (response.body().size() != 0)
                                Log.d(mDBHelper.LOG_TAG, "Brand. Принято строк: " + response.body().size());
                        }
                        counter = counter + 1; //4
                        publishProgress(counter);
                    }

                    @Override
                    public void onFailure(Call<List<Brand>> call, Throwable t) {
                        Log.d(mDBHelper.LOG_TAG, "Brand. onFailure: " + t.getMessage());
                        publishProgress(0);
                    }
                });

                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getAllSeason().enqueue(new Callback<List<Season>>() {
                    @Override
                    public void onResponse(Call<List<Season>> call, Response<List<Season>> response) {
                        if (response.isSuccessful()) {
                            for (Season deps : response.body())
                                mDBHelper.setSeason(deps);
                            if (response.body().size() != 0) {
                                Log.d(mDBHelper.LOG_TAG, "Season. Принято строк: " + response.body().size());
                            }
                        }
                        counter = counter + 1; //5
                        publishProgress(counter);
                    }

                    @Override
                    public void onFailure(Call<List<Season>> call, Throwable t) {
                        Log.d(mDBHelper.LOG_TAG, "Season. onFailure: " + t.getMessage());
                        publishProgress(0);
                    }
                });

                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getAllSizing().enqueue(new Callback<List<Sizing>>() {
                    @Override
                    public void onResponse(Call<List<Sizing>> call, Response<List<Sizing>> response) {

                        if (response.isSuccessful()) {
                            for (Sizing deps : response.body())
                                mDBHelper.setSizing(deps);

                            if (response.body().size() != 0) {
                                Log.d(mDBHelper.LOG_TAG, "Sizing. Принято строк: " + response.body().size());
                            }
                            //Запросить синхронизацию коробок и из частей
                        }
                        counter = counter + 1; //6
                        publishProgress(counter);
                    }

                    @Override
                    public void onFailure(Call<List<Sizing>> call, Throwable t) {
                        Log.d(mDBHelper.LOG_TAG, "Sizing. onFailure: " + t.getMessage());
                        publishProgress(0);
                    }
                });

                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getAllRowsAndDoc(mDBHelper.getMaxOrderDate()).enqueue(new Callback<List<OrderTraceDetail>>() {
                    @Override
                    public void onResponse(Call<List<OrderTraceDetail>> call, Response<List<OrderTraceDetail>> response) {

                        if (response.isSuccessful()) {
                            for (OrderTraceDetail deps : response.body()) {
                                mDBHelper.setOrderTrace(deps.getOrderTrace());
                                mDBHelper.setOrderTraceDetail(deps);
                            }
                            if (response.body().size() != 0) {
                                Log.d(mDBHelper.LOG_TAG, "AllRowsAndDoc. Принято строк: " + response.body().size());
                            }
                            //Запросить синхронизацию коробок и из частей
                        }
                        counter = counter + 1; //7
                        publishProgress(counter);
                    }

                    @Override
                    public void onFailure(Call<List<OrderTraceDetail>> call, Throwable t) {
                        Log.d(mDBHelper.LOG_TAG, "AllRowsAndDoc. onFailure: " + t.getMessage());
                        publishProgress(0);
                    }
                });

                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).findStickerGreaterThan(mDBHelper.getStickerMax()).enqueue(new Callback<List<Sticker>>() {
                    @Override
                    public void onResponse(Call<List<Sticker>> call, Response<List<Sticker>> response) {

                        if (response.isSuccessful()) {
                            for (Sticker deps : response.body()) {
                                mDBHelper.setSticker(deps);
                            }
                            if (response.body().size() != 0) {
                                Log.d(mDBHelper.LOG_TAG, "findStickerGreaterThan. Принято строк: " + response.body().size());
                            }
                            //Запросить синхронизацию коробок и из частей
                        }
                        counter = counter + 1; //8
                        publishProgress(counter);
                    }

                    @Override
                    public void onFailure(Call<List<Sticker>> call, Throwable t) {
                        Log.d(mDBHelper.LOG_TAG, "findStickerGreaterThan. onFailure: " + t.getMessage());
                        publishProgress(0);
                    }
                });

                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).postBox(mDBHelper.getBoxNotSent()).enqueue(new Callback<List<Box>>() {
                    @Override
                    public void onResponse(Call<List<Box>> call, Response<List<Box>> response) {

                        if (response.isSuccessful()) {
                            if (response.body().size() != 0) {
                                Log.d(mDBHelper.LOG_TAG, "postBox. Принято строк: " + response.body().size());
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
                        Log.d(mDBHelper.LOG_TAG, "postBox. onFailure: " + t.getMessage());
                        publishProgress(0);
                    }
                });

            } catch (Exception e) {
                Log.d(mDBHelper.LOG_TAG, "Error : " + e.getMessage());
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


            switch (values[0]) {
                case -1:
                    Toast.makeText(getApplicationContext(), "Ошибка при обновлении. Проверьте подключение к серверу.", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    Toast.makeText(getApplicationContext(), "Обновление продолжается... Подождите...", Toast.LENGTH_SHORT).show();
                    listView.setItemChecked(values[0]-1, true);
                    pbar.setProgress(10 + values[0]*10);
                    break;
                case 9:
                    Toast.makeText(getApplicationContext(), "Обновление закончено.", Toast.LENGTH_SHORT).show();
                    listView.setItemChecked(values[0]-1, true);
                    pbar.setProgress(10 + values[0]*10);
                    break;
            }
        }
    }

    public void updDo(View v) { //Вызов активности Сканирования
        SyncIncoData task = new SyncIncoData();
        task.execute(new String[]{null});
    }
}
