package kr.maden.watson_iot.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.viewpagerindicator.CirclePageIndicator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kr.maden.watson_iot.R;
import kr.maden.watson_iot.object.AccidentExample;
import kr.maden.watson_iot.object.JobCheck;
import kr.maden.watson_iot.object.Safety;
import kr.maden.watson_iot.object.TodaysJob;
import kr.maden.watson_iot.utils.trans.BusanApiClient;
import retrofit2.Response;

public class CheckActivity extends AppCompatActivity {

    private Button confirmBtn;

    private ArrayList<AccidentExample> accidentExamples;
    private ArrayList<JobCheck> jobs;
    private ListView listView;
    private CheckViewpagerAdapter adapter;
    private String workName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        getSupportActionBar().setTitle("3. 안전확인");


        Intent intent = getIntent();
        workName = intent.getStringExtra("work");

        accidentExamples = new ArrayList<>();
//        accidentExamples.add(new AccidentExample("https://st2.depositphotos.com/2704315/10734/v/950/depositphotos_107349076-stock-illustration-broken-car-accident-with-car.jpg",
//                "2017-12-11", getResources().getString(R.string.accident_sample_text), false));
//        accidentExamples.add(new AccidentExample("https://st3.depositphotos.com/3291867/14481/v/1600/depositphotos_144818335-stock-illustration-fall-from-stairs-accident-at.jpg",
//                "2017-12-11", getResources().getString(R.string.accident_sample_text), false));


        jobs = new ArrayList<>();
        jobs.add(new JobCheck("1. 공장 내 안전통로는 설치되어 있으며 통로의 주요 부분에 표시가 되어 있는가?", false));
        jobs.add(new JobCheck("2. 맨홀 등 개구부는 덮개가 설치되어있고 추락방지조치가 되어 있는가?", false));
        jobs.add(new JobCheck("3. 공장 내 안전통로는 설치되어 있으며 통로의 주요 부분에 표시가 되어 있는가?", false));
        jobs.add(new JobCheck("4. 맨홀 등 개구부는 덮개가 설치되어있고 추락방지조치가 되어 있는가?", false));
        jobs.add(new JobCheck("5. 공장 내 안전통로는 설치되어 있으며 통로의 주요 부분에 표시가 되어 있는가?", false));
        jobs.add(new JobCheck("6. 맨홀 등 개구부는 덮개가 설치되어있고 추락방지조치가 되어 있는가?", false));


        final ViewPager viewPager = (ViewPager) findViewById(R.id.check_viewpager);
        adapter = new CheckViewpagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setPageMargin(1000);

        CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.check_viewpager_indicator);
        indicator.setViewPager(viewPager);
        indicator.setFillColor(Color.parseColor("#658ab5"));

        findViewById(R.id.check_image_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewPager.getCurrentItem() > 0) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
                }
            }
        });
        findViewById(R.id.check_image_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewPager.getCurrentItem() < adapter.getCount() - 1) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                }
            }
        });


        listView = (ListView) findViewById(R.id.check_listview);

        ListViewAdapter adapter1 = new ListViewAdapter();
        listView.setAdapter(adapter1);

        confirmBtn = (Button) findViewById(R.id.btn8);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*모두 체크했는지 확인*/

                boolean hasUnchecked = false;
                for (AccidentExample accidentExample : accidentExamples) {
                    if (!accidentExample.isChecked())
                        hasUnchecked = true;
                }
                for (JobCheck jobCheck : jobs) {
                    if (!jobCheck.isChecked()) {
                        hasUnchecked = true;
                    }
                }
                if (!hasUnchecked) {
//                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "체크리스트를 확인해주세요!!", Toast.LENGTH_SHORT).show();
                }

                /*
                for (int i : checkbox) {
                    if (!((CheckBox) findViewById(i)).isChecked())
                        isChecked = false;
                }

                if (isChecked) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "체크리스트를 확인해주세요!!", Toast.LENGTH_SHORT).show();
                }*/
            }
        });

        SafetyTask safetyTask = new SafetyTask();
        safetyTask.execute();
    }

    @Override
    public void onBackPressed() {
        return;
    }

    class CheckViewpagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return accidentExamples.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            final AccidentExample accidentExample = accidentExamples.get(position);
            View view = LayoutInflater.from(container.getContext()).inflate(R.layout.check_image_item, container, false);
            final CheckBox checkBox = (CheckBox) view.findViewById(R.id.check_item_checkbox);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), AccidentDetailActivity.class);
                    intent.putExtra("accidentExample", accidentExample);
                    checkBox.setChecked(true);
                    accidentExample.setChecked(true);
                    startActivity(intent);
                }
            });
            checkBox.setChecked(accidentExample.isChecked());
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    accidentExample.setChecked(isChecked);
                }
            });

            ImageView imageView = (ImageView) view.findViewById(R.id.check_item_imageview);
            Glide.with(view).load(accidentExample.getImageURL()).into(imageView);
            container.addView(view);
            return view;
        }
    }

    public class ListViewAdapter extends BaseAdapter implements View.OnClickListener {

        @Override
        public int getCount() {
            return jobs.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            final JobCheck jobCheck = (JobCheck) getItem(position);
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.check_checkbox_item, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


//            final TextView textView = (TextView) convertView.findViewById(R.id.check_item_textview);
//            final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.check_item_checkbox);
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    jobCheck.setChecked(isChecked);
                }
            });
            holder.checkBox.setChecked(jobCheck.isChecked());

            holder.textView.setText(jobCheck.getContent());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.checkBox.toggle();
                }
            });
//            convertView.setOnClickListener(this);
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
            return jobs.get(position);
        }


        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.check_item_relativelayout:

            }
        }

        public class ViewHolder {
            public CheckBox checkBox;
            public TextView textView;

            public ViewHolder(View view) {
                checkBox = (CheckBox) view.findViewById(R.id.check_item_checkbox);
                textView = (TextView) view.findViewById(R.id.check_item_textview);
            }

        }
    }

    private class SafetyTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(CheckActivity.this, "사고사례 및 안전교육 데이터를 가져옵니다.", "잠시만 기다려주세요.");
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                BusanApiClient busanApiClient = BusanApiClient.retrofit.create(BusanApiClient.class);
//                TodaysJob todaysJob1 = busanApiClient.getTodaysJob().execute().body();
                Response<Safety> c = null;
                if (workName.isEmpty() || workName.contains("강재"))
                    c = busanApiClient.getSafety().execute();
                else
                    c = busanApiClient.getDojang().execute();
                List<Safety.ResultBean.AccidentsBean> accidentBeans = c.body().getResult().getAccidents();
                for (Safety.ResultBean.AccidentsBean a : accidentBeans) {
                    List<Safety.ResultBean.AccidentsBean.ResultsBean> accidentBeans1 = a.getResults();
                    for (Safety.ResultBean.AccidentsBean.ResultsBean b : accidentBeans1) {
                        Safety.ResultBean.AccidentsBean.ResultsBean.AccidentBean accident = b.getAccident();
                        String desc = accident.getDesc();
                        String title = b.getTitle();
                        String date = accident.getDatetime().getYear() + "-" + accident.getDatetime().getMonth() + "-" + accident.getDatetime().getDate();
                        accidentExamples.add(new AccidentExample("", date, desc, false, title));
                        break;
                    }
                }
                if (accidentExamples.size() >= 2) {
                    accidentExamples.get(0).setImageURL("http://www.safetynetwork.co.kr/ns/data/file/data04/237042469_X9ZiO4ks_B3AB11.jpg");
                    accidentExamples.get(1).setImageURL("http://www.safetynews.co.kr/news/photo/201610/104145_102657_4457.jpg");
                }
                if (workName != null && !workName.contains("강재")) {
                    for (int i = 0; i < accidentExamples.size(); i++) {
                        accidentExamples.get(i).setImageURL("http://www.safetygo.com/xe/files/attach/images/933/790/029/841db3262f0136aa6d7fcbf836e0f913.jpg");
                    }
                }

            } catch (Exception e) {
                accidentExamples.add(new AccidentExample("https://st2.depositphotos.com/2704315/10734/v/950/depositphotos_107349076-stock-illustration-broken-car-accident-with-car.jpg",
                        "2017-12-11", getResources().getString(R.string.accident_sample_text), false, "탑재 작업중 추락시"));
                accidentExamples.add(new AccidentExample("https://st3.depositphotos.com/3291867/14481/v/1600/depositphotos_144818335-stock-illustration-fall-from-stairs-accident-at.jpg",
                        "2017-12-11", getResources().getString(R.string.accident_sample_text), false, "탑재 작업중 추락시"));
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
