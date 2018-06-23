package kr.maden.watson_iot.ui.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import kr.maden.watson_iot.R;
import kr.maden.watson_iot.object.worker.Worker;
import kr.maden.watson_iot.ui.ChatActivity;
import kr.maden.watson_iot.ui.ChatAddActivity;
import kr.maden.watson_iot.utils.sql.ChatUserListHelper;
import kr.maden.watson_iot.utils.sql.model.ChatUserListModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ChatListFragment extends Fragment {


    public static final int ADD_NEW_WORKER = 1;

    private static ChatListFragment chatListFragment;
    private ListView listView;
    private List<ChatUser> chatUserList;
    private ListViewAdapter adapter;

    private ChatUserListHelper helper;

    private String[] projection = {
            ChatUserListModel.ChatListEntry._ID,
            ChatUserListModel.ChatListEntry.COLUMN_NAME_WORKER_ID,
            ChatUserListModel.ChatListEntry.COLUMN_NAME_USERNAME,
            ChatUserListModel.ChatListEntry.COLUMN_NAME_DATE,
            ChatUserListModel.ChatListEntry.COLUMN_NAME_MSG
    };

    public ChatListFragment() {
        // Required empty public constructor
    }


    public static ChatListFragment getInstance() {
        if (chatListFragment == null)
            chatListFragment = new ChatListFragment();
        return chatListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        chatUserList = new ArrayList<>();
        listView = (ListView) view.findViewById(R.id.chat_list_listview);
        adapter = new ListViewAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), ChatActivity.class);
                startActivity(intent);
            }
        });

        helper = new ChatUserListHelper(getContext());
        getChatUserList();


        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ChatAddActivity.class);
                getActivity().startActivityForResult(intent, ADD_NEW_WORKER);
            }
        });

        return view;
    }

    private void getChatUserList() {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sortOrder =
                ChatUserListModel.ChatListEntry.COLUMN_NAME_DATE + " DESC";

        Cursor c = db.query(
                ChatUserListModel.ChatListEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        c.moveToFirst();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            chatUserList.add(new ChatUser(c.getString(1), c.getString(2), c.getString(3), c.getString(4)));
        }
        c.close();
        db.close();
        adapter.notifyDataSetChanged();
    }

    private void addChatUserList(ChatUser chatUser) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ChatUserListModel.ChatListEntry.COLUMN_NAME_WORKER_ID, chatUser.getId());
        values.put(ChatUserListModel.ChatListEntry.COLUMN_NAME_USERNAME, chatUser.getUsername());
        values.put(ChatUserListModel.ChatListEntry.COLUMN_NAME_DATE, chatUser.getDate());
        values.put(ChatUserListModel.ChatListEntry.COLUMN_NAME_MSG, chatUser.getMsg());
        db.insert(ChatUserListModel.ChatListEntry.TABLE_NAME, null, values);
        db.close();

    }

    private boolean isChatUserExists(ChatUser chatUser) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sortOrder =
                ChatUserListModel.ChatListEntry.COLUMN_NAME_DATE + " DESC";

        Cursor c = db.query(
                ChatUserListModel.ChatListEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        c.moveToFirst();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            ChatUser user = new ChatUser(c.getString(1), c.getString(2), c.getString(3), c.getString(4));
            if (Objects.equals(user.getId(), chatUser.getId())) {
                c.close();
                db.close();

                return true;
            }
        }
        c.close();
        db.close();

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void addNewWorker(Worker worker) {
        ChatUser chatUser = new ChatUser(worker.getId(), worker.getName(), String.valueOf(Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis()), "");
        if (!isChatUserExists(chatUser)) {
            addChatUserList(chatUser);
            chatUserList.clear();
            getChatUserList();
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getContext(), "이미 추가되어있는 사용자입니다", Toast.LENGTH_SHORT).show();
        }
    }

    public class ListViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return chatUserList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.chatlist_item, parent, false);
            }
            ChatUser chatUser = chatUserList.get(position);
            CircleImageView imageView = (CircleImageView) convertView.findViewById(R.id.chat_list_profile);
            TextView username = (TextView) convertView.findViewById(R.id.chat_list_username);
            TextView date = (TextView) convertView.findViewById(R.id.chat_list_date);
            TextView textContext = (TextView) convertView.findViewById(R.id.chat_list_text_content);
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(Long.parseLong(chatUser.getDate()));
                date.setText((calendar.get(Calendar.MONTH) + 1) + "월 " + (calendar.get(Calendar.DAY_OF_MONTH) + 1) + "일");
            } catch (Exception e) {
                e.printStackTrace();
            }
            username.setText(chatUser.getUsername());
            textContext.setText(chatUser.getMsg());
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
            return chatUserList.get(position);
        }


    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatUser {
        private String id;
        private String username;
        private String date;
        private String msg;
    }

}
