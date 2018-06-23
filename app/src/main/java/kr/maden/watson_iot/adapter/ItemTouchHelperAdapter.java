package kr.maden.watson_iot.adapter;


/**
 * Created by hosungkim on 2017. 11. 29..
 */

public interface ItemTouchHelperAdapter {

    void onItemMove(int fromPosition, int toPosition);

    void onItemDismiss (int position);

}


