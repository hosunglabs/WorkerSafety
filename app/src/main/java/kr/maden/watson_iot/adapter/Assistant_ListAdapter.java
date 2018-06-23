package kr.maden.watson_iot.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import kr.maden.watson_iot.R;
import kr.maden.watson_iot.object.AssistantsObj;
import kr.maden.watson_iot.ui.fragment.AssistantFragment;

public class Assistant_ListAdapter extends RecyclerView.Adapter<Assistant_ListAdapter.RecyclerViewHolders>
        implements ItemTouchHelperAdapter {

    private Context context;
    private List<AssistantsObj> itemList;

    //생성자
    public Assistant_ListAdapter(Context context, List<AssistantsObj> itemList) {
        this.context = context;
        this.itemList = itemList;
    }


    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        int id = R.layout.assistant_integrated;
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(id, parent,
                false);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).getAssistMode();
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {
        final AssistantsObj assistantsObj = itemList.get(position);
        holder.confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AssistantFragment.sendSolved(assistantsObj, new AssistantFragment.SolvePostTask());
                onItemDismiss(holder.getAdapterPosition());
            }
        });
        holder.warningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AssistantFragment.sendAlert(assistantsObj, new AssistantFragment.AlertPostTask(), 2);
//                onItemDismiss(holder.getAdapterPosition());
            }
        });

        holder.assistDone.setVisibility(View.GONE);
        holder.assistWeather.setVisibility(View.GONE);
        holder.assistWarning.setVisibility(View.GONE);
        holder.assistHeartrate.setVisibility(View.GONE);

        switch (holder.getItemViewType()) {
            case AssistantFragment.ASSISTANT_MODE_HEARTRATE:
                holder.assistHeartrate.setVisibility(View.VISIBLE);
                holder.heartrateTextview.setText(assistantsObj.getHeartrate() + " BPM");
                break;
            case AssistantFragment.ASSISTANT_MODE_DONE:
                holder.assistDone.setVisibility(View.VISIBLE);
                holder.doneTextview.setText(assistantsObj.getDoneContent());
                break;
            case AssistantFragment.ASSISTANT_MODE_WARNING:
                holder.assistWarning.setVisibility(View.VISIBLE);
                holder.warningTitleTextview.setText(assistantsObj.getWarningTitle());
                holder.warningContentTextview.setText(assistantsObj.getWarningContent());
                break;
            case AssistantFragment.ASSISTANT_MODE_WEATHER:
                holder.assistWeather.setVisibility(View.VISIBLE);
                break;
        }

    }

    @Override
    public void onItemDismiss(int position) {
        itemList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        AssistantsObj prev = itemList.remove(fromPosition);
        itemList.add(toPosition > fromPosition ? toPosition - 1 : toPosition, prev);
        notifyItemMoved(fromPosition, toPosition);
    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }

    //뷰홀더 클래스 정의 - 별도 파일로 하거나 어답터 안에서 정의해 줄 수 있다.
    //여기에서 반복적으로 사용되는 각종 뷰들을 정의해 준다.
    public static class RecyclerViewHolders extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        public AssistantsObj assistantsObj;
        public View confirmButton, warningButton;
        public TextView heartrateTextview;
        public TextView doneTextview;
        public TextView warningTitleTextview;
        public TextView warningContentTextview;

        public ImageView weatherIcon1, weatherIcon2, weatherIcon3, weatherIcon4;
        public TextView weatherTitle2, weatherTitle3, weatherTitle4, weatherContent, weatherDegree;

        public View assistDone, assistWeather, assistWarning, assistHeartrate;

        public RecyclerViewHolders(View itemView) {
            super(itemView);
            assistDone = itemView.findViewById(R.id.assistant_done_view);
            assistHeartrate = itemView.findViewById(R.id.assistant_heartrate_view);
            assistWarning = itemView.findViewById(R.id.assistant_warning_view);
            assistWeather = itemView.findViewById(R.id.assistant_weather_view);

            confirmButton = itemView.findViewById(R.id.assistant_item_confirm);
            warningButton = itemView.findViewById(R.id.assistant_item_warning);

            heartrateTextview = (TextView) itemView.findViewById(R.id.assistant_heartrate);

            doneTextview = (TextView) itemView.findViewById(R.id.done_content);

            warningContentTextview = (TextView) itemView.findViewById(R.id.warning_content);
            warningTitleTextview = (TextView) itemView.findViewById(R.id.warning_title);

            weatherIcon1 = (ImageView) itemView.findViewById(R.id.weather_icon1);
            weatherIcon2 = (ImageView) itemView.findViewById(R.id.weather_icon2);
            weatherIcon3 = (ImageView) itemView.findViewById(R.id.weather_icon3);
            weatherIcon4 = (ImageView) itemView.findViewById(R.id.weather_icon4);
            weatherTitle2 = (TextView) itemView.findViewById(R.id.weather_text2);
            weatherTitle3 = (TextView) itemView.findViewById(R.id.weather_text3);
            weatherTitle4 = (TextView) itemView.findViewById(R.id.weather_text4);
            weatherContent = (TextView) itemView.findViewById(R.id.weather_content);
            weatherDegree = (TextView) itemView.findViewById(R.id.weather_degree);


        }


        @Override
        public void onItemSelected() {

        }

        @Override
        public void onItemClear() {

        }
    }

}
