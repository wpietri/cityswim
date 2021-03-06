package com.scissor.cityswim.app;

import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class SwimListActivity extends ActionBarActivity {

    public static final String TAG = "CitySwim";
    private SwimTableController tableController;
    private SwimDataFragment dataFragment;
    private LocationCrazinessIsolator location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting onCreate");
        setContentView(R.layout.activity_swim_list);
        Log.i(TAG, "contentView set");
        dataFragment = loadOrCreateSwimData();
        location = new LocationCrazinessIsolator(getApplicationContext());
        tableController = new SwimTableController(getApplicationContext(),
                (TableLayout) findViewById(R.id.table),
                dataFragment,
                location);
        Log.i(TAG, "tableContoller created");
        tableController.updateContents();
        startRepeatingTasks();
    }

    private void startRepeatingTasks() {
        ScheduledThreadPoolExecutor pool = (ScheduledThreadPoolExecutor)
                Executors.newScheduledThreadPool(2);
        pool.scheduleAtFixedRate(new UpdateUiTask(),30,30, TimeUnit.SECONDS );
        pool.scheduleAtFixedRate(new RefreshSwimsTask(),30,30, TimeUnit.MINUTES );

    }

    private SwimDataFragment loadOrCreateSwimData() {
        SwimDataFragment dataFragment;
        FragmentManager fm = getFragmentManager();
        dataFragment = (SwimDataFragment) fm.findFragmentByTag("swimdata");
        if (dataFragment == null) {
            dataFragment = new SwimDataFragment();
            fm.beginTransaction().add(dataFragment, "swimdata").commit();
            loadFreshSwimData(dataFragment);
        }
        return dataFragment;
    }

    private void loadFreshSwimData(SwimDataFragment dataFragment) {
        new LoadSwimsTask(dataFragment).execute();
        Log.i(TAG, "task called");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_swim_list, menu);
//        return true;
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class LoadSwimsTask extends AsyncTask<Void, Void, Void> {

        private final SwimDataFragment swimData;

        public LoadSwimsTask(SwimDataFragment saveTo) {

            this.swimData = saveTo;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.i(TAG, "doInBackground");

            try {
                swimData.loadData();
                Log.i(TAG, "loaded swims: " + swimData.getSwims().length);
            } catch (IOException e) {
                Log.e(TAG, "IO issues", e);
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void ignored) {
            Log.i(TAG, "starting OnPostExecute");
            tableController.updateContents();
            Log.i(TAG, "finished OnPostExecute");
        }

    }

    private class UpdateUiTask implements Runnable {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "starting update");
                    tableController.updateContents();
                    Log.i(TAG, "finished update");
                }
            });

        }
    }
    private class RefreshSwimsTask implements Runnable {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadFreshSwimData(dataFragment);
                }
            });

        }
    }
}
