package com.example.yg.as;

import android.Manifest;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import com.example.yg.as.Classes.Box;
import com.example.yg.as.service.ApiUtils;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.ScannerNotClaimedException;
import com.honeywell.aidc.ScannerUnavailableException;
import com.idescout.sql.SqlScoutServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.text.TextUtils.substring;

public class MainActivity extends AppCompatActivity implements BarcodeReader.BarcodeListener, AdapterView.OnItemSelectedListener {
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    private Button btnAutomaticBarcode;
    boolean useTrigger=true;
    boolean btnPressed = false;
    UsbManager mUsbManager = null;
    UsbDevice mdevice;
    IntentFilter filterAttached_and_Detached = null;

    //public static String titleMain = "Участок: ";


    //
    private static final String ACTION_USB_PERMISSION = "com.example.yg.as.USB_PERMISSION";
    private static final String barCodePrefix = "AS.";
    private DataBaseHelper mDBHelper;
    TextView tVDBInfo, CurrentDocDetails;
    EditText editTextRQ, barCodeInput;
    Button bScan;
    Spinner opers_spinner;
    Box box;

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        return;
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner sp = (Spinner) parent;
        if (sp.getId() == R.id.opers_spinner) {
            if (position != 0) {
                String label = parent.getItemAtPosition(position).toString();
                try{
                    mDBHelper.lTempContragent = mDBHelper.getContragent(label);
                    currentDocDetailsSetText(label); //Записывем наименование участка
                    mDBHelper.defs.setContragent(mDBHelper.getContragent(mDBHelper.lTempContragent)); //Устанавливаем участок в defs
                    mDBHelper.setDefs(mDBHelper.defs);//Записывем в таблицу настроек
                }catch (Exception e){
                    currentDocDetailsSetText("");
                }
                // Showing selected spinner item
                Toast.makeText(getApplicationContext(), "Вы выбрали: " + label, Toast.LENGTH_SHORT).show();
            }
        }
    }
    //
    private final BroadcastReceiver barcodeDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //Toast.makeText(context, action, Toast.LENGTH_SHORT).show();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {
                    mdevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if(mdevice != null){
                        //
                        Log.d(mDBHelper.LOG_TAG,"USB устройство отключено-" + mdevice);
                        Toast.makeText(context, "USB устройство отключено", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            //
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                synchronized (this) {
                    mdevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                        if(mdevice != null){
                            //

                            Log.d(mDBHelper.LOG_TAG,"USB устройство подключено-" + mdevice);
                            Toast.makeText(context, "USB устройство подключено", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        PendingIntent mPermissionIntent;
                        mPermissionIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_ONE_SHOT);
                        mUsbManager.requestPermission(mdevice, mPermissionIntent);

                    }

                }
            }
//
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    mdevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                        if(mdevice != null){
                            //
                            Log.d(mDBHelper.LOG_TAG,"USB устройство разрешено-" + mdevice);
                            Toast.makeText(context, "USB устройство разрешено", Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            }

        }
    };
    private boolean extScanerDetect(){
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Log.d(mDBHelper.LOG_TAG, deviceList.size()+" USB device(s) found.");
        if (deviceList.size()==0) {
            Toast.makeText(this, "USB устройств не подключено.", Toast.LENGTH_SHORT).show();
            return false;
        }else {
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            while(deviceIterator.hasNext()) {
                mdevice = deviceIterator.next();
                Log.d(mDBHelper.LOG_TAG, "" + mdevice);
                Toast.makeText(this,"USB устройство подключено.", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    }
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mDBHelper = DataBaseHelper.getInstance(this);

        tVDBInfo = (TextView) findViewById(R.id.tVDBInfo);
        tVDBInfo.setText(mDBHelper.lastBox().getBoxDesc());

        // Spinner element
        opers_spinner = (Spinner) findViewById(R.id.opers_spinner);
        opers_spinner.setOnItemSelectedListener(this);

        // Loading spinner data from database
        loadOpers_spinnerData();

        try{
            currentDocDetailsSetText(mDBHelper.defs.getContragent().getName());
        }catch (Exception e){
            currentDocDetailsSetText("");
        }

        editTextRQ = (EditText) findViewById(R.id.editTextRQ);
        editTextRQ.setEnabled(false);
        //registerReceiver
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        //
        filterAttached_and_Detached = new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        filterAttached_and_Detached.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filterAttached_and_Detached.addAction(ACTION_USB_PERMISSION);
        //
        registerReceiver(barcodeDataReceiver, filterAttached_and_Detached);
        //scaner detect
        extScanerDetect();

        AidcManager.create(this, new AidcManager.CreatedCallback() {
            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                barcodeReader = manager.createBarcodeReader();
                try {
                    if(barcodeReader!=null) {
                        Log.d("honeywellscanner: ", "barcodereader not claimed in OnCreate()");
                        barcodeReader.claim();
                    }
                }
                catch (ScannerUnavailableException e) {
                    Toast.makeText(MainActivity.this, "Failed to claim scanner",
                            Toast.LENGTH_SHORT).show();
                    //e.printStackTrace();
                }
                // register bar code event listener
                barcodeReader.addBarcodeListener(MainActivity.this);
            }
        });
    }

    private void loadOpers_spinnerData() {
        List<String> lables = new ArrayList<>();
        // Spinner Drop down elements
        for (int i = 0; i < mDBHelper.getContragent().size(); i++)
            lables.add(mDBHelper.getContragent().get(i).getName());

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        opers_spinner.setAdapter(dataAdapter);
    }

    @Override
    public void onStop(){
        super.onStop();
        if(barcodeReader!=null)
            barcodeReader.release();
    }

    @Override
    protected void onResume() {

        super.onResume();

        try{
            currentDocDetailsSetText(mDBHelper.defs.getContragent().getName());
        }catch (Exception e){
            currentDocDetailsSetText("");
        }

        if (barcodeReader != null) {
            try {
                barcodeReader.claim();
                Log.d("noneywellscanner: ", "scanner claimed");
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                Toast.makeText(this, "Встроенный сканер не включается!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void currentDocDetailsSetText (String defContragent){
        CurrentDocDetails  = (TextView) findViewById(R.id.CurrentDocDetails);
        CurrentDocDetails.setText("Выбран участок: " +defContragent);
    }

    public void ocl_scan(View v) { //Вызов активности Сканирования
        if (mDBHelper.defs.getContragent().getId()==0) {
           Toast.makeText(this,"Нужно зайти в настройки и выбрать участок...", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this,SettingsActivity.class));  //Вызов активности Коробки
                return;
        } else
        {
            currentDocDetailsSetText(mDBHelper.defs.getContragent().getName());
        }
        /* Если сканер подключен - вызывать обработчик для него*/
        final     EditText            input = (EditText) findViewById(R.id.barCodeInput);
        Button bScan = (Button) findViewById(R.id.bScan);
        if (bScan.getText() == "OK!"){
            //input.setEnabled(false);
            ocl_bOk(v);
        }else {
            if (mdevice != null){//внешний usb сканер
                showMessage("Режим работы с внешним сканером.");
                input.setEnabled(true);
                input.requestFocus();
                //input.setInputType(InputType.TYPE_NULL);
                input.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // nothing
                    }
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String currentbarcode = input.getText().toString();
                        if( currentbarcode.indexOf("\n") > 0) {
                            currentbarcode = substring(currentbarcode,0,currentbarcode.indexOf("\n"));
                            Toast.makeText(MainActivity.this, currentbarcode, Toast.LENGTH_SHORT).show();
                            scanResultHandler(currentbarcode);
                            input.setText("");
                            input.requestFocus();
                        }
                    }
                    @Override
                    public void afterTextChanged(Editable s) {
                        // nothing
                    }
                });
            }else {//камера устройства
                input.setEnabled(false);
                if(barcodeReader!=null){
                    //showMessage("Режим работы со встроенным сканером.");
                    try {
                        barcodeReader.softwareTrigger(true);
                    } catch (ScannerNotClaimedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (ScannerUnavailableException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                else{
                    showMessage("Режим работы с камерой.");
                    IntentIntegrator integrator = new IntentIntegrator(this);
                    integrator.initiateScan();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();
        // Операции для выбранного пункта меню
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this,SettingsActivity.class));
                return true;
            case R.id.action_boxes:
                startActivity(new Intent(this,BoxesActivity.class));
                return true;
            /*case R.id.action_orders:
                startActivity(new Intent(this,OrdersActivity.class));
                return true;
            case R.id.action_prods:
                startActivity(new Intent(this,ProdsActivity.class));
                return true;
            case R.id.action_test:
                startActivity(new Intent(this,OutDocsActivity.class));
                return true;*/
            case R.id.action_get_data:
                startActivity(new Intent(this,UpdateActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public String[] splitBarcode(String storedbarcode) {
        String atmpBarcode[] = storedbarcode.split("[.]");  // по dot
        boolean b = (atmpBarcode.length == 6);
        if (!b) {
            atmpBarcode[0] = "";
        }
        return atmpBarcode;
    }

    private static String filter (String str){
        StringBuilder filtered = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char current = str.charAt(i);
            if (current >= 0x2E && current <= 0x39) {
                filtered.append(current);
            }
        }
        return filtered.toString();
    }
    public static String stripNonDigits(
            final CharSequence input /* inspired by seh's comment */){
        final StringBuilder sb = new StringBuilder(
                input.length() /* also inspired by seh's comment */);
        for(int i = 0; i < input.length(); i++){
            final char c = input.charAt(i);
            if(c > 47 && c < 58){
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public String extractQuantity(String storedbarcode) {
        String atmpBarcode[] = storedbarcode.split("[.]");  // по dot
        boolean b = (atmpBarcode.length == 5);
        if (!b) {
            atmpBarcode[4] = "";
        }
        return atmpBarcode[4];
    }
    public String extractSticker(String storedbarcode) {
        String atmpBarcode[] = storedbarcode.split("[.]");  // по dot
        boolean b = (atmpBarcode.length == 5);
        if (!b) {
            atmpBarcode[3] = "";
        }
        return atmpBarcode[3];
    }
    public String extractOrderTraceDetail(String storedbarcode) {
        String atmpBarcode[] = storedbarcode.split("[.]");  // по dot
        boolean b = (atmpBarcode.length == 5);
        if (!b) {
            atmpBarcode[2] = "";
        }
        return atmpBarcode[2];
    }

    public String extractOrderTrace(String storedbarcode) {
        String atmpBarcode[] = storedbarcode.split("[.]");  // по dot
        boolean b = (atmpBarcode.length == 5);
        if (!b) {
            atmpBarcode[1] = "";
        }
        return atmpBarcode[1];
    }

    private void scanResultHandler (String currentbarcode) {
            /*---Тут нужно данные коробки вывести и дать отредактировать количество. Есть код в currentbarcode.
            Нужно его обработать, выбрать данные новой коробки для вывода tVDBInfo и в editTextRQ
            * Если данные новой коробки не нашли в заказах - сообщить и ничего не выводить*/

        try{
        if (StringUtils.countMatches(currentbarcode, barCodePrefix)!=1) {
            showMessage("QR-код не распознан.");
            return;
        }

        if (extractOrderTrace(currentbarcode)==""){
            showMessage("QR-код не распознан.");
            return;
        }}
        catch (Exception e) {
            Log.e(mDBHelper.LOG_TAG, "QR-код не распознан.", e);
            showMessage("QR-код не распознан.!");
            return;
        }



        box = mDBHelper.searchBox(Long.parseLong(extractOrderTrace(currentbarcode)),
                Long.parseLong(extractOrderTraceDetail(currentbarcode)),
                Long.parseLong(extractSticker(currentbarcode)),
                Integer.parseInt(extractQuantity(currentbarcode))); //Передаем очищенный id строки таблицы Дитэйлз
        if (box.getBoxDesc().length()!=0) {
            tVDBInfo = (TextView) findViewById(R.id.tVDBInfo);
            tVDBInfo.setText(box.getBoxDesc());
            if (box.getId()!=0){
                showLongMessage("Коробку на этом участке уже принимали: "+box.getDateOfTrace());
            }else{
                if (box.getQuantity()!=0){
                    Button bScan = (Button) findViewById(R.id.bScan);
                    bScan.setText("OK!");
                    showMessage("Новая коробка на участке: "+mDBHelper.defs.getContragent().getName());}
                else{
                    showMessage(mDBHelper.countBox());
                    showLongMessage("Коробка с нулевым количеством на участке: "+mDBHelper.defs.getContragent().getName());
                }
            }
        } else {
            showLongMessage("Заказ для этой коробки не загружен! Нужно синхронизировать данные.");
        }
    }

    public void ocl_bOk(View v) { //Вызов активности Сканирования
        if (box.getId()==0)
            try {
                Button bScan = (Button) findViewById(R.id.bScan);
                bScan.setText("Scan!");
                    if (mDBHelper.setBox(box, mDBHelper.defs.getContragent())==0){            //---Вызов метода добавления коробки и продс
                        showLongMessage(mDBHelper.defs.getContragent().getName()+". Ошибка! Коробка не добавлена в БД!");
                    } else {
                        tVDBInfo = (TextView) findViewById(R.id.tVDBInfo);
                        tVDBInfo.setText(mDBHelper.lastBox().getBoxDesc());
                        showMessage(mDBHelper.defs.getContragent().getName()+". Принята новая коробка.");
                    }
                if (mdevice != null){//внешний usb сканер
                    final     EditText            input = (EditText) findViewById(R.id.barCodeInput);
                    input.requestFocus();
                }
            } catch (Exception e) {
                Log.e(mDBHelper.LOG_TAG, mDBHelper.defs.getContragent().getName()+". Ошибка при получении количества в коробке!", e);
                showMessage(mDBHelper.defs.getContragent().getName()+". Ошибка! Невозможно получить введенное количество!");
            }
    }
    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult.getContents() != null) {
            // handle scan result
            String currentbarcode = scanResult.getContents();
            Toast.makeText(MainActivity.this, currentbarcode, Toast.LENGTH_SHORT).show();
            scanResultHandler(currentbarcode);
        }else{
            Toast.makeText(MainActivity.this, "Ошибка сканера !", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBarcodeEvent(final BarcodeReadEvent event) {
        try {
            barcodeReader.softwareTrigger(false);
        } catch (ScannerNotClaimedException e) {
            e.printStackTrace();
        } catch (ScannerUnavailableException e) {
            e.printStackTrace();
        }
        // TODO Auto-generated method stub
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (event.getBarcodeData() != null) {
                    // handle scan result
                    String currentbarcode = event.getBarcodeData();
                    Toast.makeText(MainActivity.this, currentbarcode, Toast.LENGTH_SHORT).show();
                    scanResultHandler(currentbarcode);
                }else{
                    Toast.makeText(MainActivity.this, "Ошибка сканера !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent arg0) {
        // TODO Auto-generated method stub
        try {
            barcodeReader.softwareTrigger(false);
        } catch (ScannerNotClaimedException e) {
            e.printStackTrace();
        } catch (ScannerUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub

        super.onDestroy();
        unregisterReceiver(barcodeDataReceiver);

        if (barcodeReader != null) {
            // close BarcodeReader to clean up resources.
            barcodeReader.close();
            barcodeReader = null;
        }

        if (manager != null) {
            // close AidcManager to disconnect from the scanner service.
            // once closed, the object can no longer be used.
            manager.close();
        }

    }

    private void showMessage (String s){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, s, duration);
        toast.setGravity(Gravity.BOTTOM|Gravity.CENTER, 0, 0);
        toast.show();
    }
    private void showLongMessage (String s){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, s, duration);
        toast.setGravity(Gravity.BOTTOM|Gravity.CENTER, 0, 0);
        toast.show();
    }
    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        Button bScan = (Button) findViewById(R.id.bScan);
        if (bScan.getText()=="OK!"){
            openCancelDialog();
        }
        else {
            openQuitDialog();
        }
    }
    private void openQuitDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                MainActivity.this);
        quitDialog.setTitle("Выход: Вы уверены?");

        quitDialog.setPositiveButton("Да!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                finish();
            }
        });

        quitDialog.setNegativeButton("Нет.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        quitDialog.show();
    }
    private void openCancelDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                MainActivity.this);
        quitDialog.setTitle("Отменить: Вы уверены?");

        quitDialog.setPositiveButton("Да!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Button bScan = (Button) findViewById(R.id.bScan);
                bScan.setText("Scan!");
                editTextRQ = (EditText) findViewById(R.id.editTextRQ);
                editTextRQ.setEnabled(false);
            }
        });

        quitDialog.setNegativeButton("Нет.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                editTextRQ.requestFocus();
            }
        });
        quitDialog.show();
    }
}

