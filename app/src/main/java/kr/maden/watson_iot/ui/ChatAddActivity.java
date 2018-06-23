package kr.maden.watson_iot.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kr.maden.watson_iot.R;
import kr.maden.watson_iot.object.worker.Worker;
import kr.maden.watson_iot.utils.trans.BusanChatApiClient;
import retrofit2.Call;
import retrofit2.Response;

public class ChatAddActivity extends AppCompatActivity {

    private ListView listView;
    private List<Worker> workerList;
    private List<Worker> realWorkerList;
    private ListViewAdapter adapter;
    private EditText editText;
    private Button button;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_add);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        workerList = new ArrayList<>();
        realWorkerList = new ArrayList<>();
        listView = (ListView) findViewById(R.id.chat_add_listview);
        adapter = new ListViewAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("worker", workerList.get(position));
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.chat_add_loading);
        editText = (EditText) findViewById(R.id.chat_add_edittext);
        button = (Button) findViewById(R.id.chat_add_cancel);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                realWorkerList.clear();
                if (s.toString().isEmpty()) {
                    realWorkerList.addAll(workerList);
                    adapter.notifyDataSetChanged();
                    return;
                }
                for (int i = 0; i < workerList.size(); i++) {
                    Worker worker = workerList.get(i);
                    if (worker.getName().contains(s)) {
                        realWorkerList.add(worker);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
//        workerList.add(new Worker("s", "안전 관리자", "d", "", null, false));
//        workerList.add(new Worker("s", "안전 관리자", "d", "", null, false));
        ChatAddTask chatAddTask = new ChatAddTask();
        chatAddTask.execute();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public class ListViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return realWorkerList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.chat_add_item, parent, false);
            }
            Worker worker = realWorkerList.get(position);
            CircleImageView imageView = (CircleImageView) convertView.findViewById(R.id.chat_list_profile);
            TextView username = (TextView) convertView.findViewById(R.id.chat_list_username);
            username.setText(worker.getName());
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
            return realWorkerList.get(position);
        }


    }

    class ChatAddTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            ProgressDialog.show(ChatAddActivity.this,"")
        }

        @Override
        protected Void doInBackground(Void... voids) {
            BusanChatApiClient busan_api = BusanChatApiClient.retrofit.create(BusanChatApiClient.class);
            Call<List<Worker>> call = busan_api.getWorkerActive();

            try {
                Response<List<Worker>> response = call.execute();
                List<Worker> workers = response.body();
                for (Worker w : workers) {
                    if (w.getActive()) {
                        workerList.add(w);
                    }
                }
//                workerList.addAll(workers);
                realWorkerList.addAll(workerList);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter.notifyDataSetChanged();
            if (progressBar.getVisibility() != View.GONE) {
                progressBar.setVisibility(View.GONE);
            }
            super.onPostExecute(aVoid);
        }
    }
}
