package kr.maden.watson_iot.ui;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import kr.maden.watson_iot.R;
import kr.maden.watson_iot.ui.fragment.todayis_external;
import kr.maden.watson_iot.ui.fragment.todayis_internal;

public class TodayisActivity extends AppCompatActivity {
    private static final String TAG = TodayisActivity.class.getSimpleName();

    TabLayout tabLayout;
    ViewPager viewPager;
    Pager adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todayis);
        getSupportActionBar().setTitle("2. 금일의 작업");
        tabLayout = (TabLayout) findViewById(R.id.tabLayout_todayis);
        viewPager = (ViewPager) findViewById(R.id.viewPager_todayis);

        adapter = new Pager(getSupportFragmentManager());

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

    }


    public class Pager extends FragmentPagerAdapter {

        public Pager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return todayis_internal.newInstance();
                case 1:
                    return todayis_external.newInstance();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "내부";
                case 1:
                    return "외부";
                default:
                    return null;
            }
        }
    }


}
