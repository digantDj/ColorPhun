package com.prakharme.prakharsriv.colorphun;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public abstract class MainGameActivity extends Activity {

    protected TextView pointsTextView, levelTextView;
    protected ProgressBar timerProgress;

    protected int level, points;
    protected boolean gameStart = false;
    protected Runnable runnable;
    protected int timer;

    protected static final int POINT_INCREMENT = 2;
    protected static int TIMER_DELTA = -1;
    protected static final int TIMER_BUMP = 2;
    protected static final int START_TIMER = 200;
    protected static final int FPS = 100;
    protected static final int LEVEL = 25;

    protected Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        endGame();
    }

    @Override
    protected void onStop() {
        super.onStop();
        gameStart = false;
    }

    protected void setupGameLoop() {
        runnable = new Runnable() {
            @Override
            public void run() {
                while (timer > 0 && gameStart) {
                    synchronized (this) {
                        try {
                            wait(FPS);
                        } catch (InterruptedException e) {
                            Log.i("THREAD ERROR", e.getMessage());
                        }
                        timer = timer + TIMER_DELTA;
                        if (TIMER_DELTA > 0) {
                            TIMER_DELTA = -TIMER_DELTA / TIMER_BUMP;
                        }
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            timerProgress.setProgress(timer);
                        }
                    });
                }
                if (gameStart) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            endGame();
                        }
                    });
                }
            }
        };

    }

    protected void resetGame() {
        gameStart = false;
        level = 1;
        points = 0;

        // update view
        pointsTextView.setText(Integer.toString(points));
        levelTextView.setText(Integer.toString(level));
        timerProgress.setProgress(0);
    }

    abstract protected void setColorsOnButtons();

    protected void startGame() {
        gameStart = true;

        Toast.makeText(this, R.string.game_help, Toast.LENGTH_SHORT).show();
        setColorsOnButtons();

        // start timer
        timer = START_TIMER;
        Thread thread = new Thread(runnable);
        thread.start();
    }

    protected void endGame() {
        gameStart = false;
        int highScore = saveAndGetHighScore();
        launchGameOver(highScore);
        finish();
    }

    private int saveAndGetHighScore() {
        SharedPreferences preferences = this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        int highScore = preferences.getInt("HIGHSCORE", 0);

        if (points > highScore) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("HIGHSCORE", points);
            editor.apply();
            highScore = points;
        }
        return highScore;
    }

    private void launchGameOver(int highScore) {
        // Send data to another activity
        Intent intent = new Intent(this, GameOverActivity.class);
        intent.putExtra("points", points);
        intent.putExtra("level", level);
        intent.putExtra("best", highScore);
        intent.putExtra("newScore", highScore == points);
        startActivity(intent);
    }
}