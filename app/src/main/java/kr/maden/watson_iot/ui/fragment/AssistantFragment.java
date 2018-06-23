package kr.maden.watson_iot.ui.fragment;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kr.maden.watson_iot.R;
import kr.maden.watson_iot.adapter.Assistant_ListAdapter;
import kr.maden.watson_iot.adapter.SimpleItemTouchHelperCallback;
import kr.maden.watson_iot.object.Alert;
import kr.maden.watson_iot.object.AssistantsObj;
import kr.maden.watson_iot.object.Solve;
import kr.maden.watson_iot.utils.sql.AssistantListHelper;
import kr.maden.watson_iot.utils.sql.model.AssistantListModel;
import kr.maden.watson_iot.utils.trans.BusanApiClient;

public class AssistantFragment extends Fragment {
    private static AssistantFragment assistantFragment;
    private AssistantListHelper mDbHelper;
    private ItemTouchHelper mItemTouchHelper;

    private RecyclerView recyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private Assistant_ListAdapter mAdapter;
    private String[] projection = {
            AssistantListModel.AssistantListEntry._ID,
            AssistantListModel.AssistantListEntry.COLUMN_NAME_TYPE,
            AssistantListModel.AssistantListEntry.COLUMN_NAME_SHORT_MSG,
            AssistantListModel.AssistantListEntry.COLUMN_NAME_DESCRIPTION
    };
    private static final List<AssistantsObj> asskList = new ArrayList<>();
    public static final int ASSISTANT_MODE_WEATHER = 0;
    public static final int ASSISTANT_MODE_HEARTRATE = 1;
    public static final int ASSISTANT_MODE_DONE = 2;
    public static final int ASSISTANT_MODE_WARNING = 3;

    public AssistantFragment() {
    }


    public static AssistantFragment getInstance() {
        if (assistantFragment == null) {
            assistantFragment = new AssistantFragment();
        }
        return assistantFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assistant, container, false);
        mDbHelper = new AssistantListHelper(getContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

      /*  String sortOrder =
                AssistantListModel.AssistantListEntry._ID + " DESC";
        Cursor c = db.query(
                AssistantListModel.AssistantListEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );*/

      /*  if (asskList.isEmpty()) {
            asskList.add(new AssistantsObj(ASSISTANT_MODE_HEARTRATE, new Date().getTime(), 130));
            asskList.add(new AssistantsObj(ASSISTANT_MODE_WEATHER));
            asskList.add(new AssistantsObj(ASSISTANT_MODE_DONE, new Date().getTime(), "안녕하세요 작업 끝나써요 집에가세요."));
            asskList.add(new AssistantsObj(ASSISTANT_MODE_WARNING, new Date().getTime(), "안녕하세요", "저는 경고에요 집에가세요"));
        }*/
//        asskList.add(new AssistantsObj(ASSISTANT_MODE_WARNING, new Date().getTime(), "안녕하세요", "저는 경고에요 집에가세요"));


        mLinearLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerView = (RecyclerView) view.findViewById(R.id.myshelf_recycler_view);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        mAdapter = new Assistant_ListAdapter(getActivity(), asskList);
        recyclerView.setAdapter(mAdapter);


        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
        return view;
    }

    //이 함수를 통해 어시스턴트 푸시 추가
    public void addAssistantList(AssistantsObj assistantsObj) {
        if (assistantsObj != null && asskList != null) {
            for (int i = 0; i < asskList.size(); i++) {
                if (asskList.get(i).getAssistMode() == assistantsObj.getAssistMode()) {
                    asskList.remove(i);
                    i--;
                }
            }
            asskList.add(0, assistantsObj);

            sendAlert(assistantsObj, new AlertPostTask(),0);

        }
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    public static void sendSolved(AssistantsObj assistantsObj, SolvePostTask solvePostTask) {
        Solve solve = new Solve("Android", "Android", "확인 버튼을 누름");
        solvePostTask.execute(solve);
    }

    public static void sendAlert(AssistantsObj assistantsObj, AlertPostTask alertPostTask,int msgtype) {
        Alert.MetaBean metaBean = new Alert.MetaBean(33.857317, 29.22226, "application", "Android");
        Alert alert = new Alert("Android", "기타", msgtype, "worker or safetymanager or supervisor", "something", metaBean, true);
        switch (assistantsObj.getAssistMode()) {
            case ASSISTANT_MODE_DONE:
                alert.setMsg("작업 완료");
                break;
            case ASSISTANT_MODE_HEARTRATE:
                alert.setMsg("심박수 이상");
                break;
            case ASSISTANT_MODE_WARNING:
                alert.setMsg("경고");
                break;
            case ASSISTANT_MODE_WEATHER:
                alert.setMsg("날씨 이상");
                break;
            default:
                break;
        }
//        AlertPostTask alertPostTask = new AlertPostTask();
        alertPostTask.execute(alert);
    }


    public static class AlertPostTask extends AsyncTask<Alert, Void, Void> {
        @Override
        protected Void doInBackground(Alert... alerts) {
            if (alerts.length < 0) return null;
            BusanApiClient busan_api = BusanApiClient.retrofit.create(BusanApiClient.class);
            try {
                busan_api.postAlert(alerts[0]).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static class SolvePostTask extends AsyncTask<Solve, Void, Void> {
        @Override
        protected Void doInBackground(Solve... solves) {
            if (solves.length < 0) return null;
            BusanApiClient busan_api = BusanApiClient.retrofit.create(BusanApiClient.class);
            try {
                busan_api.postSolve(solves[0]).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}



