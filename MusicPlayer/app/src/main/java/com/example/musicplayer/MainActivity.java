package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener{
    private static final String TAG = "Main";
    ListView listView;
    RelativeLayout relativeLayout;
    TextView songtitle,songbtitle;
    ProgressBar songProgress;
    Context context;
    String[] ListElements = new String[]{};
    List<String> ListElementsArrayList = new ArrayList<>();
    ArrayAdapter<String> adapter;
    ContentResolver contentResolver;
    Cursor cursor;
    MediaPlayer mediaPlayer;
    Uri uri;
    private ArrayList<Song> songsList = new ArrayList<>();
    private double startTime = 0;
    private double finalTime = 0;
    public static int oneTimeOnly = 0;
    private Handler myHandler = new Handler();;
    private int forwardTime = 5000;
    private int backwardTime = 5000;
    String songTitle;
    int id=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.list);
        songtitle=findViewById(R.id.textView2);
        songProgress=findViewById(R.id.progressBar);
        final Button playbutton=findViewById(R.id.button3);
        final Button pausebutton=findViewById(R.id.button4);
        relativeLayout=findViewById(R.id.relative);
        playbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    playbutton.setVisibility(View.INVISIBLE);
                    pausebutton.setVisibility(View.VISIBLE);
                }
            }
        });
        pausebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    pausebutton.setVisibility(View.INVISIBLE);
                    playbutton.setVisibility(View.VISIBLE);
                }

            }
        });
        context = getApplicationContext();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this); // Important
        // Getting all songs list
        if (isReadStoragePermissionGranted())
            setList();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mediaPlayer != null) {
                    playSong(i);
                    relativeLayout.setVisibility(View.VISIBLE);
                    if (mediaPlayer.isPlaying()){
                        pausebutton.setVisibility(View.VISIBLE);
                    }else {
                        playbutton.setVisibility(View.VISIBLE);
                    }
                }

            }
        });
    }
    private void setList() {
        GetAllMediaMp3Files();
        for (Song song : songsList) {
            ListElementsArrayList.add(song.getTitle());
        }
        adapter = new ArrayAdapter<String>
                (MainActivity.this,android.R.layout.simple_list_item_1, ListElementsArrayList);
        listView.setAdapter(adapter);
    }
    public void playSong(int songIndex) {
        // Play song
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(songsList.get(songIndex).getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            // Displaying Song title
            songTitle = songsList.get(songIndex).getTitle();
            songtitle.setText(songTitle);
            finalTime = mediaPlayer.getDuration();
            startTime = mediaPlayer.getCurrentPosition();
            if (oneTimeOnly == 0) {
                songProgress.setMax((int) finalTime);
                oneTimeOnly = 1;
            }
            songProgress.setProgress((int)startTime);
            myHandler.postDelayed(UpdateSongTime,100);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = mediaPlayer.getCurrentPosition();
            songProgress.setProgress((int)startTime);
            myHandler.postDelayed(this, 100);
        }
    };
    private void GetAllMediaMp3Files() {
        contentResolver = context.getContentResolver();
        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        cursor = contentResolver.query(
                uri, // Uri
                null,
                null,
                null,
                null
        );
        if (cursor == null) {
            Toast.makeText(MainActivity.this, "Something Went Wrong.", Toast.LENGTH_LONG);
        } else if (!cursor.moveToFirst()) {
            Toast.makeText(MainActivity.this, "No Music Found on SD Card.", Toast.LENGTH_LONG);
        } else {
            int Title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int path = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            do {
                String SongTitle = cursor.getString(Title);
                String songPath = cursor.getString(path);
                Song song = new Song();
                song.setTitle(SongTitle);
                song.setPath(songPath);
                songsList.add(song);

            } while (cursor.moveToNext());
        }
    }

    public boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted1");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted1");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 3:
                Log.d(TAG, "External storage1");
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
                    //resume tasks needing this permission
                    setList();

                } else {

                }
                break;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }
}