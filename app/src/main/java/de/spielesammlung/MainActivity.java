package de.spielesammlung;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View snakeCard = findViewById(R.id.snakeCard);
        snakeCard.setOnClickListener(view -> startActivity(new Intent(this, SnakeActivity.class)));
    }
}

