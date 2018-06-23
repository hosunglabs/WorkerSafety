package kr.maden.watson_iot.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kr.maden.watson_iot.R;
import kr.maden.watson_iot.object.WorkTypeRsl;
import kr.maden.watson_iot.ui.CheckActivity;
import kr.maden.watson_iot.utils.trans.BusanApiClient;

public class todayis_internal extends Fragment implements View.OnClickListener {
    private final static String TAG = todayis_internal.class.getSimpleName();

    private static todayis_internal fragment;

    private int button[] = {
            R.id.button1,
            R.id.button2,
            R.id.button3,
            R.id.button4,
            R.id.button5,
            R.id.button6,
            R.id.button7,
    };


    private List<Button> buttons;

    private int cardview[] = {
            R.id.cardview1,
            R.id.cardview2,
            R.id.cardview3,
            R.id.cardview4,
            R.id.cardview5,
            R.id.cardview6
    };

    private List<CardView> cardViews;

    public static boolean[] checked = {false, false, false, false, false, false, false}; // 버튼이 체크되면 true, 해제시 false


    public static todayis_internal newInstance() {
        if (fragment == null)
            fragment = new todayis_internal();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_todayis_internal, container, false);
        buttons = new ArrayList<>();
        for (int btn : button) {
            Button btns = (Button) view.findViewById(btn);
            buttons.add(btns);
            btns.setOnClickListener(this);

        }

        cardViews = new ArrayList<>();
        for (int card : cardview) {
            CardView cardView = (CardView) view.findViewById(card);
            cardViews.add(cardView);
            cardView.setOnClickListener(this);
        }

        WorktypeDataTrans worktypeDataTrans = new WorktypeDataTrans();
        worktypeDataTrans.execute();
        return view;

    }


    @Override
    public void onClick(View view) {
        if (view instanceof CardView) {
            CardView cardView = (CardView) view;
            for (int i = 0; i < cardview.length; i++) {
                if (cardview[i] == view.getId()) {
                    if (!checked[i]) {
                        cardView.setCardBackgroundColor(Color.parseColor("#658ab5"));
                        ((TextView) cardView.findViewById(R.id.cardview_text)).setTextColor(Color.WHITE);
                        cardView.findViewById(R.id.check).setVisibility(View.VISIBLE);
                        checked[i] = true;
                    } else {
                        cardView.setCardBackgroundColor(Color.WHITE);
                        cardView.findViewById(R.id.check).setVisibility(View.INVISIBLE);
                        ((TextView) cardView.findViewById(R.id.cardview_text)).setTextColor(Color.BLACK);
                        checked[i] = false;
                    }
                    break;
                }
            }
        }
        switch (view.getId()) {
            case R.id.button7:
                Intent intent = new Intent(getContext(), CheckActivity.class);
                startActivity(intent);
                break;
            default:

                break;
        }
    }


    private class WorktypeDataTrans extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Void... voids) {

            BusanApiClient busanApiClient = BusanApiClient.retrofit.create(BusanApiClient.class);
            try {
                WorkTypeRsl workTypeRsl = busanApiClient.getWorkType().execute().body();
                return workTypeRsl.getResult();
            } catch (IOException e) {
                Log.e(TAG, "testDataTrans", e);
            }

            return null;
        }


        @Override
        protected void onPostExecute(List<String> strings) {
            int cnt = 0;
            for (String types : strings) {
                ((TextView) cardViews.get(cnt).findViewById(R.id.cardview_text)).setText(types);
                cnt++;
                if (cnt > 5) break; //버튼 텍스트 고정을 위해 6을 5로 변환
            }
        }
    }
}
