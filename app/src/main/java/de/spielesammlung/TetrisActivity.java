package de.spielesammlung;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TetrisActivity extends Activity {
    private TetrisView game;
    private TextView score;
    private Button pause;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12), dp(14), dp(12), dp(12));
        root.setBackgroundColor(getColor(R.color.background));

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        Button back = button("‹ Spiele");
        back.setOnClickListener(view -> finish());
        score = new TextView(this);
        score.setTextColor(getColor(R.color.text));
        score.setTextSize(16);
        score.setGravity(Gravity.CENTER);
        pause = button("Pause");
        bar.addView(back, new LinearLayout.LayoutParams(0, dp(50), 1));
        bar.addView(score, new LinearLayout.LayoutParams(0, dp(50), 1.3f));
        bar.addView(pause, new LinearLayout.LayoutParams(0, dp(50), 1));
        root.addView(bar);

        game = new TetrisView(this);
        game.setGameListener(new TetrisView.GameListener() {
            @Override public void onStatsChanged(int points, int lines) {
                score.setText(points + " Punkte  ·  " + lines + " Reihen");
            }
            @Override public void onStateChanged(boolean paused, boolean gameOver) {
                pause.setText(gameOver ? "Nochmal" : paused ? "Weiter" : "Pause");
            }
        });
        root.addView(game, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER);
        Button left = button("◀");
        Button rotate = button("↻ Drehen");
        Button down = button("▼");
        Button right = button("▶");
        left.setContentDescription("Block nach links");
        rotate.setContentDescription("Block drehen");
        down.setContentDescription("Block sofort ablegen");
        right.setContentDescription("Block nach rechts");
        left.setOnClickListener(view -> game.moveLeft());
        rotate.setOnClickListener(view -> game.rotate());
        down.setOnClickListener(view -> game.drop());
        right.setOnClickListener(view -> game.moveRight());
        controls.addView(left, controlParams(1));
        controls.addView(rotate, controlParams(1.5f));
        controls.addView(down, controlParams(1));
        controls.addView(right, controlParams(1));
        root.addView(controls, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(58)));

        pause.setOnClickListener(view -> game.togglePause());
        setContentView(root);
    }

    private LinearLayout.LayoutParams controlParams(float weight) {
        return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight);
    }

    private Button button(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(getColor(R.color.blocks));
        button.setTextSize(14);
        button.setBackgroundColor(getColor(R.color.background));
        return button;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override protected void onPause() {
        super.onPause();
        if (game != null) game.pause();
    }
}
