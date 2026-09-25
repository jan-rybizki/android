package de.spielesammlung;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SnakeActivity extends Activity {
    private SnakeView game;
    private TextView score;
    private Button pause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(18), dp(16), dp(16));
        root.setBackgroundColor(getColor(R.color.background));

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        Button back = button("‹ Spiele");
        back.setOnClickListener(view -> finish());
        score = new TextView(this);
        score.setTextColor(getColor(R.color.text));
        score.setTextSize(20);
        score.setGravity(Gravity.CENTER);
        pause = button("Pause");
        bar.addView(back, new LinearLayout.LayoutParams(0, dp(52), 1));
        bar.addView(score, new LinearLayout.LayoutParams(0, dp(52), 1));
        bar.addView(pause, new LinearLayout.LayoutParams(0, dp(52), 1));
        root.addView(bar);

        game = new SnakeView(this);
        game.setGameListener(new SnakeView.GameListener() {
            @Override public void onScoreChanged(int value) { score.setText("Punkte  " + value); }
            @Override public void onStateChanged(boolean paused, boolean gameOver) {
                pause.setText(gameOver ? "Nochmal" : paused ? "Weiter" : "Pause");
            }
        });
        root.addView(game, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        pause.setOnClickListener(view -> game.togglePause());
        setContentView(root);
    }

    private Button button(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(getColor(R.color.primary));
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
