package com.example.yg.as;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;


import com.example.yg.as.Classes.AdapterUtils;
import com.example.yg.as.Classes.Box;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BoxesActivity extends AppCompatActivity {
    //Переменная для работы с БД
    private DataBaseHelper mDBHelper;
    SimpleAdapter adapter = null;
    ListView listView = null;
    String[] from = {"Ord", "Cust"};
    int[] to = {R.id.textView, R.id.textView2};


    @Override
    protected void onResume() {
        super.onResume();
        try{
            this.setTitle(mDBHelper.defs.getContragent().getName()+" Коробки.");
        } catch (Exception e){

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boxes);
        mDBHelper = DataBaseHelper.getInstance(this);

        List<Box> boxes = new ArrayList<>();
        List<HashMap<String,String>> hmBoxes = new ArrayList<>();

        Box box = new Box(0, "", (long) 0, "", (long) 0, "",
                "", "", (long) 0, "", (long) 0, "", (long) 0,
                "", (long) 0, "", (long) 0, (long) 0, (long) 0, (long) 0, (long) 0);
        boxes.add(box);
        for(Box box1: boxes){
            HashMap hm = AdapterUtils.convertToHashMap(box);
            hmBoxes.add(hm);
        }
        adapter = new SimpleAdapter(this, hmBoxes, R.layout.adapter_item, from, to);


//Создаем адаптер
        adapter = new SimpleAdapter(this, mDBHelper.listboxes(), R.layout.adapter_item, from, to);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> var1, View var2, final int position, long id) {
                String sTmp = adapter.getItem(position).toString();
                sTmp = sTmp.substring(sTmp.indexOf("Ord=")+4,sTmp.indexOf("Ord=")+4+20)+"...\n"+
                        sTmp.substring(sTmp.indexOf("Cust=")+5,sTmp.indexOf("Cust=")+5+40)+"...";
                AlertDialog.Builder adb=new AlertDialog.Builder(BoxesActivity.this);
                adb.setTitle("Удалить запись?");
                adb.setMessage("Удаляем запись " + sTmp);
                final int positionToRemove = position;
                adb.setNegativeButton("Отменить", null);
                adb.setPositiveButton("Удалить", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //MyDataObject.remove(positionToRemove);
                        String sTmp = adapter.getItem(position).toString();
                        //("bId=",cursor.getString(10)+"/bId");
                        String sBId = sTmp.substring(sTmp.indexOf("bId=")+4,sTmp.indexOf("/bId"));
                        //readBox.put("bmId",cursor.getString(11));
                        //Проверить нет ли других операций по этой коробке. Если это расходная операция -
                        //приходная есть по умолчанию. Если это приходная операция - проверить наличие других.
                        if (mDBHelper.deleteFromTable(Box.TABLE,Box.Column_id,sBId)){

                        }
                        else {
                            Log.d("1","Ошибка при удалении коробки! Id= "+sBId );
                            Toast.makeText(getApplicationContext(), "Ошибка при удалении коробки!",
                                    Toast.LENGTH_LONG).show();
                        }
                    }});
                adb.show();
            }
        });
    }



}