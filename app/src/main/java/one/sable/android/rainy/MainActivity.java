package one.sable.android.rainy;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    @SuppressLint("ResourceType")
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.layout.menu_activity_main, menu);
        return true;
    }
    /**
     * A simple {@link Fragment} subclass.
     */
    public static class PlaceholderFragment extends Fragment {


        public PlaceholderFragment() {
            // Required empty public constructor
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            ArrayList<String> mDummyForecast = new ArrayList<String>();
            String[] mDummyArray = {"Today - Cloudy - 88/55","Tomorrow - Sunny - 90/75",
                    "Friday - Clear - 98/37","Saturday - Cloudy - 88/55","Sunay - Sunny - 90/75",
                    "Monday - Clear - 98/37"};
            for (String forecast : mDummyArray) {
                mDummyForecast.add(forecast);
            }

            return rootView;
        }

    }
}
