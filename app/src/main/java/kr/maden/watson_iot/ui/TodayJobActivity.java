package kr.maden.watson_iot.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.maden.watson_iot.R;
import kr.maden.watson_iot.object.CurrentJob;
import kr.maden.watson_iot.object.TodaysJob;
import kr.maden.watson_iot.object.WorkTypeRsl;
import kr.maden.watson_iot.ui.fragment.SensorTagFragment;
import kr.maden.watson_iot.utils.trans.BusanApiClient;
import retrofit2.Call;
import retrofit2.Response;

public class TodayJobActivity extends AppCompatActivity {

    private ListView listView;
    private ListViewAdapter adapter;
    public static TodaysJob todaysJob = null;
    public static List<TodaysJob.ResultsBean.WorklistBean> worklistBeanList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_job);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" ");

        listView = (ListView) findViewById(R.id.todayjob_listview);
        worklistBeanList = new ArrayList<>();
        adapter = new ListViewAdapter();
        listView.setAdapter(adapter);
        findViewById(R.id.todayjob_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(TodayJobActivity.this, TodayisActivity.class);
                boolean allcheck = true;
                for (TodaysJob.ResultsBean.WorklistBean w : worklistBeanList) {
                    if (!w.isChecked()) allcheck = false;
                }
                if (MainActivity.DEBUGGNIG || allcheck) {
//                    Intent intent = new Intent(TodayJobActivity.this, CheckActivity.class);
                    Intent intent = new Intent(TodayJobActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(TodayJobActivity.this, "체크리스트를 확인해주세요!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        TodayJobTask todayJobTask = new TodayJobTask();
        todayJobTask.execute();


    }

    public class ListViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return worklistBeanList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

           /* if (convertView == null)*/
            {
                LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.todayjob_list_item, parent, false);
            }

            final TodaysJob.ResultsBean.WorklistBean worklist = worklistBeanList.get(position);
            RelativeLayout relativeLayout = (RelativeLayout) convertView.findViewById(R.id.todayjob_circle);
            if (position % 2 == 1)
                relativeLayout.setBackground(getDrawable(R.drawable.circle_yellow));

            TextView jobname = (TextView) convertView.findViewById(R.id.todayjob_list_jobname_textview);
            TextView jobwork = (TextView) convertView.findViewById(R.id.todayjob_list_jobname);
            TextView jobtime = (TextView) convertView.findViewById(R.id.todayjob_list_jobtime);
            TextView jobgps = (TextView) convertView.findViewById(R.id.todayjob_list_jobgps);
            CardView cardview = (CardView) convertView.findViewById(R.id.todayjob_list_cardview);
            final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.todayjob_checkbox);
            final String jname = worklist.getWork();
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TodayJobActivity.this, CheckActivity.class);
                    intent.putExtra("work", jname);
                    startActivity(intent);
//                    checkBox.toggle();
                }
            });
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    worklist.setChecked(isChecked);
                }
            });

            cardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TodayJobActivity.this, CheckActivity.class);
                    intent.putExtra("work", jname);
                    startActivity(intent);
                    checkBox.toggle();
                }
            });
            jobname.setText(worklist.getWork());
            jobwork.setText(worklist.getSub());
            jobtime.setText("hour : " + String.valueOf(worklist.getHour()));
            jobgps.setText(worklist.getDetail());
            return convertView;
        }

        // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
        @Override
        public long getItemId(int position) {
            return position;
        }

        // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
        @Override
        public Object getItem(int position) {
            return worklistBeanList.get(position);
        }


    }

    private class TodayJobTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(TodayJobActivity.this, "오늘의 작업 내용을 가져옵니다.", "잠시만 기다려주세요.");
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            BusanApiClient busanApiClient = BusanApiClient.retrofit.create(BusanApiClient.class);
            try {
//                TodaysJob todaysJob1 = busanApiClient.getTodaysJob().execute().body();
                Response<TodaysJob> c = busanApiClient.getTodaysJob().execute();

                MainActivity.todaysJob = todaysJob = c.body();
                if (todaysJob.getResults().getWorklist() != null)
                    worklistBeanList = todaysJob.getResults().getWorklist();

            } catch (IOException e) {
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            adapter.notifyDataSetChanged();

        }

    }
}
