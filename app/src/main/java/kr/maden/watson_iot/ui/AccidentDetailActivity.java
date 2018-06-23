package kr.maden.watson_iot.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import kr.maden.watson_iot.R;
import kr.maden.watson_iot.object.AccidentExample;

public class AccidentDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accident_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(" ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView imageView = (ImageView) findViewById(R.id.accident_detail_imageview);
        TextView textView = (TextView) findViewById(R.id.accident_detail_textview);
        TextView title = (TextView) findViewById(R.id.accident_detail_title);
        TextView date = (TextView) findViewById(R.id.accident_detail_date);


        AccidentExample accidentExample = (AccidentExample) getIntent().getSerializableExtra("accidentExample");

        if (accidentExample == null || accidentExample.getImageURL() == null || accidentExample.getContent() == null) {
            Toast.makeText(this, "사고 사례를 불러오는데 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        Glide.with(this).load(accidentExample.getImageURL()
        ).into(imageView);
        textView.setText(accidentExample.getContent());
        title.setText(accidentExample.getTitle());
        date.setText(accidentExample.getDate());
        findViewById(R.id.accident_detail_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
