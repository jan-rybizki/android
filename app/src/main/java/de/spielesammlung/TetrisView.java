package de.spielesammlung;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

public class TetrisView extends View {
    interface GameListener {
        void onStatsChanged(int score, int lines);
        void onStateChanged(boolean paused, boolean gameOver);
    }

    private static final int COLUMNS = 10;
    private static final int ROWS = 20;
    private static final int[][][][] SHAPES = {
            {{{0, 1}, {1, 1}, {2, 1}, {3, 1}}, {{2, 0}, {2, 1}, {2, 2}, {2, 3}}},
            {{{1, 0}, {2, 0}, {1, 1}, {2, 1}}},
            {{{1, 0}, {0, 1}, {1, 1}, {2, 1}}, {{1, 0}, {1, 1}, {2, 1}, {1, 2}},
                    {{0, 1}, {1, 1}, {2, 1}, {1, 2}}, {{1, 0}, {0, 1}, {1, 1}, {1, 2}}},
            {{{1, 0}, {2, 0}, {0, 1}, {1, 1}}, {{1, 0}, {1, 1}, {2, 1}, {2, 2}}},
            {{{0, 0}, {1, 0}, {1, 1}, {2, 1}}, {{2, 0}, {1, 1}, {2, 1}, {1, 2}}},
            {{{0, 0}, {0, 1}, {1, 1}, {2, 1}}, {{1, 0}, {2, 0}, {1, 1}, {1, 2}},
                    {{0, 1}, {1, 1}, {2, 1}, {2, 2}}, {{1, 0}, {1, 1}, {0, 2}, {1, 2}}},
            {{{2, 0}, {0, 1}, {1, 1}, {2, 1}}, {{1, 0}, {1, 1}, {1, 2}, {2, 2}},
                    {{0, 1}, {1, 1}, {2, 1}, {0, 2}}, {{0, 0}, {1, 0}, {1, 1}, {1, 2}}}
    };
    private static final int[] COLORS = {0xFF45CBEF, 0xFFF4D35E, 0xFFB26DE3,
            0xFF64D687, 0xFFFF667A, 0xFF5C7CFA, 0xFFFF9F43};

    private final int[][] board = new int[ROWS][COLUMNS];
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final Runnable tick = this::step;
    private int type;
    private int rotation;
    private int pieceX;
    private int pieceY;
    private int score;
    private int lines;
    private boolean paused;
    private boolean gameOver;
    private float touchX;
    private float touchY;
    private GameListener listener;

    public TetrisView(Context context) {
        super(context);
        setFocusable(true);
        setContentDescription("Spielfeld für fallende Blöcke. Tippen dreht, Wischen bewegt.");
        reset();
    }

    void setGameListener(GameListener listener) {
        this.listener = listener;
        notifyState();
    }

    private void reset() {
        handler.removeCallbacks(tick);
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) board[row][column] = 0;
        }
        score = 0;
        lines = 0;
        paused = false;
        gameOver = false;
        spawnPiece();
        notifyState();
        scheduleTick();
        invalidate();
    }

    private void spawnPiece() {
        type = random.nextInt(SHAPES.length);
        rotation = 0;
        pieceX = 3;
        pieceY = -1;
        if (!fits(pieceX, pieceY, rotation)) gameOver = true;
    }

    private void step() {
        if (paused || gameOver || !isShown()) return;
        if (fits(pieceX, pieceY + 1, rotation)) {
            pieceY++;
        } else {
            lockPiece();
        }
        invalidate();
        notifyState();
        if (!gameOver) scheduleTick();
    }

    private void scheduleTick() {
        handler.removeCallbacks(tick);
        handler.postDelayed(tick, Math.max(150, 650 - lines * 18L));
    }

    private boolean fits(int x, int y, int turn) {
        for (int[] cell : SHAPES[type][turn]) {
            int column = x + cell[0];
            int row = y + cell[1];
            if (column < 0 || column >= COLUMNS || row >= ROWS) return false;
            if (row >= 0 && board[row][column] != 0) return false;
        }
        return true;
    }

    private void lockPiece() {
        for (int[] cell : SHAPES[type][rotation]) {
            int row = pieceY + cell[1];
            if (row < 0) {
                gameOver = true;
                return;
            }
            board[row][pieceX + cell[0]] = type + 1;
        }
        int cleared = clearLines();
        if (cleared > 0) {
            lines += cleared;
            int[] rewards = {0, 100, 300, 500, 800};
            score += rewards[cleared];
        }
        spawnPiece();
    }

    private int clearLines() {
        int cleared = 0;
        for (int row = ROWS - 1; row >= 0; row--) {
            boolean full = true;
            for (int column = 0; column < COLUMNS; column++) full &= board[row][column] != 0;
            if (!full) continue;
            cleared++;
            for (int move = row; move > 0; move--) {
                System.arraycopy(board[move - 1], 0, board[move], 0, COLUMNS);
            }
            for (int column = 0; column < COLUMNS; column++) board[0][column] = 0;
            row++;
        }
        return cleared;
    }

    void moveLeft() { move(-1); }
    void moveRight() { move(1); }

    private void move(int amount) {
        if (!paused && !gameOver && fits(pieceX + amount, pieceY, rotation)) {
            pieceX += amount;
            invalidate();
        }
    }

    void rotate() {
        if (paused || gameOver) return;
        int next = (rotation + 1) % SHAPES[type].length;
        if (fits(pieceX, pieceY, next)) rotation = next;
        else if (fits(pieceX - 1, pieceY, next)) { pieceX--; rotation = next; }
        else if (fits(pieceX + 1, pieceY, next)) { pieceX++; rotation = next; }
        invalidate();
    }

    void drop() {
        if (paused || gameOver) return;
        int distance = 0;
        while (fits(pieceX, pieceY + 1, rotation)) { pieceY++; distance++; }
        score += distance * 2;
        lockPiece();
        notifyState();
        invalidate();
        if (!gameOver) scheduleTick();
    }

    void togglePause() {
        if (gameOver) { reset(); return; }
        paused = !paused;
        handler.removeCallbacks(tick);
        if (!paused) scheduleTick();
        notifyState();
        invalidate();
    }

    void pause() {
        if (!paused && !gameOver) {
            paused = true;
            handler.removeCallbacks(tick);
            notifyState();
            invalidate();
        }
    }

    private void notifyState() {
        if (listener != null) {
            listener.onStatsChanged(score, lines);
            listener.onStateChanged(paused, gameOver);
        }
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cell = Math.min(getWidth() / (float) COLUMNS, getHeight() / (float) ROWS);
        float left = (getWidth() - cell * COLUMNS) / 2f;
        float top = (getHeight() - cell * ROWS) / 2f;
        paint.setColor(Color.rgb(19, 29, 38));
        canvas.drawRoundRect(new RectF(left, top, left + cell * COLUMNS, top + cell * ROWS), 16, 16, paint);
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                if (board[row][column] != 0) drawCell(canvas, column, row, COLORS[board[row][column] - 1], left, top, cell);
            }
        }
        if (!gameOver) {
            for (int[] block : SHAPES[type][rotation]) {
                int row = pieceY + block[1];
                if (row >= 0) drawCell(canvas, pieceX + block[0], row, COLORS[type], left, top, cell);
            }
        }
        if (paused || gameOver) {
            paint.setColor(0xD010161B);
            canvas.drawRect(left, top, left + cell * COLUMNS, top + cell * ROWS, paint);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(Color.WHITE);
            paint.setFakeBoldText(true);
            paint.setTextSize(cell * .8f);
            canvas.drawText(gameOver ? "Spiel vorbei" : "Pause", getWidth() / 2f, getHeight() / 2f, paint);
            paint.setFakeBoldText(false);
            paint.setTextSize(cell * .42f);
            paint.setColor(Color.rgb(170, 184, 190));
            canvas.drawText(gameOver ? "Nochmal startet eine neue Runde" : "Mit Weiter fortsetzen",
                    getWidth() / 2f, getHeight() / 2f + cell, paint);
        }
    }

    private void drawCell(Canvas canvas, int column, int row, int color, float left, float top, float size) {
        float gap = Math.max(1, size * .06f);
        paint.setColor(color);
        canvas.drawRoundRect(new RectF(left + column * size + gap, top + row * size + gap,
                left + (column + 1) * size - gap, top + (row + 1) * size - gap), size * .12f, size * .12f, paint);
        paint.setColor(0x35FFFFFF);
        canvas.drawRect(left + column * size + gap * 2, top + row * size + gap * 2,
                left + (column + 1) * size - gap * 2, top + row * size + size * .18f, paint);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchX = event.getX();
            touchY = event.getY();
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float dx = event.getX() - touchX;
            float dy = event.getY() - touchY;
            float threshold = 28 * getResources().getDisplayMetrics().density;
            if (Math.max(Math.abs(dx), Math.abs(dy)) < threshold) rotate();
            else if (Math.abs(dx) > Math.abs(dy)) move(dx < 0 ? -1 : 1);
            else if (dy > 0) drop();
            performClick();
            return true;
        }
        return true;
    }

    @Override public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override protected void onDetachedFromWindow() {
        handler.removeCallbacks(tick);
        super.onDetachedFromWindow();
    }
}
