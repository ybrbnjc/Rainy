package one.sable.android.rainy;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    public final static String EXTRA_3HOURSFORECAST = "3hfEXTRA";
    private final static String LOG_TAG = DetailFragment.class.getSimpleName();
    private final static String appHashTag = " #RainyApp";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_detail, new DetailFragment())
                    .commit();
        }
    }

    public static class DetailFragment extends Fragment {
        private ShareActionProvider mShareActionProvider;
        private TextView mFragmentTextview;


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_activity_main, menu);
            inflater.inflate(R.menu.menu_fragment_detail,menu);

            MenuItem menuItem = menu.findItem(R.id.action_share);
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            } else {
                Log.e(LOG_TAG,"ShareActionProvider is null:(");
            }

        }

        public Intent createShareForecastIntent () {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mFragmentTextview.getText() + appHashTag);
            return shareIntent;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_settings) {
                Intent intent = new Intent(getActivity().getApplicationContext(),
                        SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            Intent intent = getActivity().getIntent();
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            mFragmentTextview = (TextView)rootView.findViewById(R.id.fragment_detail_textview);
            mFragmentTextview.setText(intent.getStringExtra(EXTRA_3HOURSFORECAST));
            return rootView;
        }
    }
}
