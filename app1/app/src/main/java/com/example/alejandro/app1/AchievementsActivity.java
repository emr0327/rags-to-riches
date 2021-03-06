package com.example.alejandro.app1;

import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.os.StrictMode;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.alejandro.app1.adapters.AchievementAdapter;
import com.example.alejandro.app1.models.Account;
import com.example.alejandro.app1.models.Achievement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Written by: Kartik Patel
 * Tested/Debugged by: Kartik Patel, Deep Patel
 *
 * AchievementsActivity class handles all functions regarding pulling achievements
 * from the database and displaying them for the user so that they can track
 * their progress.
 */

public class AchievementsActivity extends MainMenuActivity {

    Account account;
    List<Achievement> achievements;
    String[] achievementsArray = {"Win one game", "Win five games", "Win ten games", "Earn $100", "Earn $1000", "Earn $10000"};

    private Button mBackButton = null;
    ArrayAdapter<Achievement> displayAdapter;

    /**
     * General initializer of Android Activity
     * @param savedInstanceState    saved Instance of previous activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_achievements);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final Bundle extras = getIntent().getExtras();
        account = new Account(extras.getInt("id"), extras.getString("email"), extras.getString("username"), extras.getString("password"));

        grabAchievements();

        final Context context = getApplicationContext();
        displayAdapter = new AchievementAdapter(AchievementsActivity.this, context, account, achievements);
        ListView listView = (ListView) findViewById(R.id.achievementList);
        listView.setAdapter(displayAdapter);

        mBackButton = (Button) findViewById(R.id.goMainMenuActivity);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent i = new Intent(view.getContext(), MainMenuActivity.class);
            i.putExtra("id", extras.getInt("id"));
            i.putExtra("email", extras.getString("email"));
            i.putExtra("username", extras.getString("username"));
            i.putExtra("password", extras.getString("password"));
            startActivity(i);
            }

        });
    }

    /**
     * Grab Achievements from the database for a specific player
     */
    public void grabAchievements() {
        try {
            URL url = new URL("http://parallel.gg/rags-to-riches/grab-achievements.php");
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("id","UTF-8")+"="+URLEncoder.encode("" + account.getId(),"UTF-8");
            bufferedWriter.write(post_data);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
            String result="";
            String line="";
            while((line = bufferedReader.readLine())!= null) {
                result += line;
            }
            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();

            String[] achievementResults =  result.split("/");

            int wins = Integer.parseInt(achievementResults[0]);
            int earnings = Integer.parseInt(achievementResults[1]);

            if(wins < 0) wins = 0;
            if(earnings < 0) earnings = 0;

            achievements = new ArrayList<Achievement>();
            achievements.add(new Achievement(achievementsArray[0], wins >= 1 ? "Completed" : wins + "/1"));
            achievements.add(new Achievement(achievementsArray[1], wins >= 5 ? "Completed" : wins + "/5"));
            achievements.add(new Achievement(achievementsArray[2], wins >= 10 ? "Completed" : wins + "/10"));
            achievements.add(new Achievement(achievementsArray[3], earnings >= 100 ? "Completed" : earnings + "/100"));
            achievements.add(new Achievement(achievementsArray[4], earnings >= 1000 ? "Completed" : earnings + "/1000"));
            achievements.add(new Achievement(achievementsArray[5], earnings >= 1000 ? "Completed" : earnings + "/10000"));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        return false;
    }
}
