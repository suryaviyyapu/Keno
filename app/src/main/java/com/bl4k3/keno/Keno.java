package com.bl4k3.keno;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.ads.AdRequest;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;


public class Keno extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    FirebaseAuth mAuth;
    AdView mAdView;
    AdRequest adRequest;
    private SharedPreferences pref;
    MenuItem checkable;
    private PendingIntent pendingIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keno);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        Intent alarmIntent = new Intent(Keno.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(Keno.this, 0, alarmIntent, 0);


        MobileAds.initialize(this, "ca-app-pub-8212638041206076~2499605568");

        mAdView = findViewById(R.id.adView);
        adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }
        });

        //Set Default percentage
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        setDefaultPercentage(75);

        if(pref.getBoolean("is_checked", true))
            startAlarm();


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //Default Fragment to DashBoard Fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.screen_area, new DashboardFrag()).commit();
        }
        //Check For User Registration
        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null){
            finish();
            Intent intent = new Intent(Keno.this, Welcome.class);
            Toast.makeText(getApplicationContext(),"Please login",Toast.LENGTH_LONG).show();
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
        }
    }


    //Back button Pressed
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.keno, menu);
        checkable = menu.findItem(R.id.reminder);
        checkable.setChecked(pref.getBoolean("is_checked",true));
        return true;
    }

    //Top Right menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

                //Change Required Percentage
            case R.id.changePercentage:
                changePercentageDialogBox();
                break;

                //Remainder Option
            case R.id.reminder:
                changeReminderOption();
                break;

                //Logout Option
            case R.id.Logout:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, Welcome.class));
                break;

                //About Page
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                break;

        }
        return super.onOptionsItemSelected(item);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment = null;
        int id = item.getItemId();

        if (id == R.id.Dashboard) {
            fragment = new DashboardFrag();
        } else if (id == R.id.TimeTable) {
            startActivity(new Intent(this,Timetable.class));
        } else if (id == R.id.Notes) {
            startActivity(new Intent(this, ListActivity.class));
        } else if (id == R.id.Tools){
            startActivity(new Intent(this,Tools.class));
        } else if (id == R.id.action_settings){
            startActivity(new Intent(this,Settings.class));
        } else if(id == R.id.support){
                 Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"vnsd@protonmail.com"});
                startActivity(intent);
            }
//for null pointer exception
        if(fragment != null){
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.screen_area, fragment);
            ft.commit();

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Change Percentage Method
    void changePercentageDialogBox(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(Keno.this);
        builder.setTitle("Enter Required Percentage");
        final EditText input = new EditText(Keno.this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setGravity(Gravity.CENTER);
        input.setText(String.valueOf(pref.getInt("requiredPercentage",75)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(10,10,10,10);
        input.setLayoutParams(lp);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int percent = Integer.valueOf(input.getText().toString());
                pref.edit().putInt("requiredPercentage", percent).apply();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setView(input);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    //set Default Percentage
    void setDefaultPercentage(Integer percent){
        if (pref.getBoolean("my_first_time", true)) {
            //the app is being launched for first time, do something
            Log.d("Percentage", "First time");

            // first time task
            pref.edit().putInt("requiredPercentage", percent).apply();
            pref.edit().putBoolean("is_checked", true).apply();
            // record the fact that the app has been started at least once
            pref.edit().putBoolean("my_first_time", false).apply();
        }
    }
    /*
    void deleteSubject(long id){

        long ids =id;
        ContentValues values = new ContentValues();
        values.put(SubjectContract.SubjectEntry._ID, ids);

        int rowsdeleted = getContentResolver().delete(SubjectContract.SubjectEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(ids)).build(),null,null);

        // Show a toast message depending on whether or not the insertion was successful
        if (rowsdeleted == 0) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, "File does not exists", Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            // loaddata();
            Toast.makeText(this, "Subject Deleted Successfully", Toast.LENGTH_SHORT).show();
        }
    }*/
    //Remainder's
    public void startAlarm() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 10);
        calendar.set(Calendar.SECOND, 0);

        /* Repeating on every 20 minutes interval */
        manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                1000 * 60 * 60 * 12, pendingIntent);
    }

    //cancel Remainder
    public void cancelAlarm() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
        Toast.makeText(this, "Notifications Cancelled", Toast.LENGTH_SHORT).show();
    }

    //Changing remainder State ON or OFF
    void changeReminderOption(){
        if(checkable.isChecked()){
            final AlertDialog.Builder builder = new AlertDialog.Builder(Keno.this);
            builder.setTitle("  Alert !!");
            builder.setMessage("Reminder is important to get alert for updating attendance and avoiding low attendance .");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    pref.edit().putBoolean("is_checked",false).apply();
                    cancelAlarm();
                    checkable.setChecked(pref.getBoolean("is_checked",true));
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        else{
            pref.edit().putBoolean("is_checked",true).apply();
            checkable.setChecked(pref.getBoolean("is_checked",true));
            Toast.makeText(this, "Notifications Started", Toast.LENGTH_SHORT).show();
            startAlarm();
        }
    }
    /*void deletealert(long id){
        final Long ids = id;
        AlertDialog.Builder builder = new AlertDialog.Builder(Keno.this);
        builder.setMessage(R.string.AlertMessage);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                Log.i("call", String.valueOf(ids));
                deleteSubject(ids);
            }
        });
        builder.setNegativeButton(R.string.nosorry, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }*/

}

