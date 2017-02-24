package com.wj.rlrecyclerviewtest;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wj.rlrecyclerview.RLRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Main2Activity extends AppCompatActivity {

    private RLRecyclerView rv;

    ArrayList<MineGridBean> mData;
    private MAdapter adapter;

    private boolean isHorizontal;

    public static void actionStart(Activity activity, String mode, String layout, boolean auto,
                                   boolean haveHeader, boolean haveFooter) {

        Intent intent = new Intent(activity, Main2Activity.class);
        intent.putExtra("mode", mode);
        intent.putExtra("layout", layout);
        intent.putExtra("auto", auto);
        intent.putExtra("header", haveHeader);
        intent.putExtra("footer", haveFooter);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        rv = (RLRecyclerView) findViewById(R.id.rv);

        mData = new ArrayList<>();
        mData.addAll(getDate());
        adapter = new MAdapter();
        rv.setAdapter(adapter);

        getBundle();

        rv.setOnRLListener(new RLRecyclerView.OnRLListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 模拟耗时操作
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mData.clear();
                                mData.addAll(getDate());
                                adapter.notifyDataSetChanged();
                                rv.onComplete();
                            }
                        });
                    }
                }).start();
            }

            @Override
            public void onLoadMore() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 模拟耗时操作
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mData.addAll(getDate());
                                adapter.notifyDataSetChanged();
                                rv.onComplete();
                            }
                        });
                    }
                }).start();
            }
        });


    }

    private void getBundle() {

        Intent intent = getIntent();

        switch (intent.getStringExtra("layout")) {
            case MainActivity.LAYOUT_LINEAR:
                rv.setLayoutManager(new LinearLayoutManager(this));
                isHorizontal = false;
                break;
            case MainActivity.LAYOUT_GRID:
                rv.setLayoutManager(new GridLayoutManager(this, 4));
                isHorizontal = true;
                break;
            case MainActivity.LAYOUT_STAGGERED:
                rv.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
                isHorizontal = true;
                break;
        }

        switch (intent.getStringExtra("mode")) {
            case MainActivity.MODE_BOTH:
                rv.setMode(RLRecyclerView.REFRESH_MODE_BOTH);
                break;
            case MainActivity.MODE_REFRESH:
                rv.setMode(RLRecyclerView.REFRESH_MODE_REFRESH);
                break;
            case MainActivity.MODE_LOAD_MORE:
                rv.setMode(RLRecyclerView.REFRESH_MODE_LOADMORE);
                break;
            case MainActivity.MODE_NONE:
                rv.setMode(RLRecyclerView.REFRESH_MODE_NONE);
                break;
        }

        rv.setAutoRefresh(intent.getBooleanExtra("auto", false));

        if (intent.getBooleanExtra("header", false)) {
            final TextView header = new TextView(this);
            header.setText("Header View");
            header.setPadding(10, 10, 10, 10);
            header.setGravity(Gravity.CENTER);
            header.setBackgroundResource(R.color.colorAccent);
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            header.setLayoutParams(params);
            header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(Main2Activity.this, header.getText(), Toast.LENGTH_SHORT).show();
                }
            });
            rv.addHeaderView(header);
        }

        if (intent.getBooleanExtra("footer", false)) {
            final TextView footer = new TextView(this);
            footer.setText("Footer View");
            footer.setPadding(10, 10, 10, 10);
            footer.setGravity(Gravity.CENTER);
            footer.setBackgroundResource(R.color.colorAccent);
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            footer.setLayoutParams(params);
            footer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(Main2Activity.this, footer.getText(), Toast.LENGTH_SHORT).show();
                }
            });
            rv.addFooterView(footer);
        }
    }

    class MAdapter extends RecyclerView.Adapter<MAdapter.MViewHolder> {

        @Override
        public MViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (isHorizontal) {
                return new MViewHolder(LayoutInflater.from(Main2Activity.this).inflate(R.layout.item_horizontal, parent, false));
            } else {
                return new MViewHolder(LayoutInflater.from(Main2Activity.this).inflate(R.layout.item_vertical, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(MViewHolder holder, int position) {

            MineGridBean item = mData.get(position);
            holder.tv.setText(item.getName());
            try {
                InputStream in = getAssets().open(item.getImgsrc());
                Bitmap bmp = BitmapFactory.decodeStream(in);
                holder.iv.setImageBitmap(bmp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        class MViewHolder extends RecyclerView.ViewHolder {

            ImageView iv;
            TextView tv;

            MViewHolder(View itemView) {
                super(itemView);

                iv = (ImageView) itemView.findViewById(R.id.iv);
                tv = (TextView) itemView.findViewById(R.id.tv);
            }
        }
    }

    /**
     * 获取GridView数据
     */
    public ArrayList<MineGridBean> getDate() {

        // 获取Json数据
        String json = getJson("course/mineItem.json");
        // 解析为数据集合
        return getDataList(json);

    }

    /**
     * 根据路径获取Json字符串
     *
     * @param fileName 文件路径
     *
     * @return Json字符串
     */
    @NonNull
    private String getJson(String fileName) {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /**
     * 将Json字符串转换为数据集合
     *
     * @param json Json字符串
     *
     * @return 数据集合
     */
    private ArrayList<MineGridBean> getDataList(String json) {
        ArrayList<MineGridBean> data = new ArrayList<>();
        try {

            JSONArray array = new JSONArray(json);
            int len = array.length();
            MineGridBean item;
            for (int i = 0; i < len; i++) {
                JSONObject object = array.getJSONObject(i);
                item = new MineGridBean();
                item.setId(object.getInt("id"));
                item.setImgsrc(object.getString("imgsrc"));
                item.setName(object.getString("name"));
                data.add(item);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }
}
