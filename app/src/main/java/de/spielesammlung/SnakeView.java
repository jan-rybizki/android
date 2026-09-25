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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public class SnakeView extends View {
    interface GameListener {
        void onScoreChanged(int score);
        void onStateChanged(boolean paused, boolean gameOver);
    }

    private static final int COLUMNS = 18;
    private static final int ROWS = 28;
    private static final long TICK_MS = 135;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Deque<Cell> snake = new ArrayDeque<>();
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tick = this::advance;
    private Direction direction = Direction.RIGHT;
    private Direction queuedDirection = Direction.RIGHT;
    private Cell food;
    private boolean paused;
    private boolean gameOver;
    private float touchX;
    private float touchY;
    private GameListener listener;

    public SnakeView(Context context) {
        super(context);
        setFocusable(true);
        setContentDescription("Snake Spielfeld. Zum Steuern in eine Richtung wischen.");
        reset();
    }

    void setGameListener(GameListener listener) {
        this.listener = listener;
        notifyState();
    }

    private void reset() {
        handler.removeCallbacks(tick);
        snake.clear();
        snake.addFirst(new Cell(8, 14));
        snake.addLast(new Cell(7, 14));
        snake.addLast(new Cell(6, 14));
        direction = Direction.RIGHT;
        queuedDirection = direction;
        gameOver = false;
        paused = false;
        placeFood();
        notifyState();
        handler.postDelayed(tick, TICK_MS);
        invalidate();
    }

    private void advance() {
        if (paused || gameOver || !isShown()) return;
        direction = queuedDirection;
        Cell head = snake.peekFirst();
        Cell next = new Cell(head.x + direction.dx, head.y + direction.dy);
        boolean ate = next.equals(food);
        Cell tail = snake.peekLast();
        if (next.x < 0 || next.x >= COLUMNS || next.y < 0 || next.y >= ROWS
                || (snake.contains(next) && !(next.equals(tail) && !ate))) {
            gameOver = true;
            notifyState();
            invalidate();
            return;
        }
        snake.addFirst(next);
        if (ate) {
            placeFood();
            notifyState();
        } else {
            snake.removeLast();
        }
        invalidate();
        handler.postDelayed(tick, TICK_MS);
    }

    private void placeFood() {
        do {
            food = new Cell(random.nextInt(COLUMNS), random.nextInt(ROWS));
        } while (snake.contains(food));
    }

    void togglePause() {
        if (gameOver) {
            reset();
            return;
        }
        paused = !paused;
        handler.removeCallbacks(tick);
        if (!paused) handler.postDelayed(tick, TICK_MS);
        notifyState();
        invalidate();
    }

    void pause() {
        if (!gameOver && !paused) {
            paused = true;
            handler.removeCallbacks(tick);
            notifyState();
            invalidate();
        }
    }

    private void notifyState() {
        if (listener != null) {
            listener.onScoreChanged(Math.max(0, snake.size() - 3));
            listener.onStateChanged(paused, gameOver);
        }
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cell = Math.min(getWidth() / (float) COLUMNS, getHeight() / (float) ROWS);
        float left = (getWidth() - cell * COLUMNS) / 2f;
        float top = (getHeight() - cell * ROWS) / 2f;
        paint.setColor(Color.rgb(24, 34, 27));
        canvas.drawRoundRect(new RectF(left, top, left + cell * COLUMNS, top + cell * ROWS), 18, 18, paint);

        paint.setColor(Color.rgb(255, 101, 101));
        drawCell(canvas, food, left, top, cell, .25f);
        int index = 0;
        for (Cell part : snake) {
            paint.setColor(index++ == 0 ? Color.rgb(120, 224, 143) : Color.rgb(58, 169, 86));
            drawCell(canvas, part, left, top, cell, .12f);
        }

        if (paused || gameOver) {
            paint.setColor(0xCC101611);
            canvas.drawRect(left, top, left + cell * COLUMNS, top + cell * ROWS, paint);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(Color.WHITE);
            paint.setTextSize(cell * 1.45f);
            paint.setFakeBoldText(true);
            canvas.drawText(gameOver ? "Spiel vorbei" : "Pause", getWidth() / 2f, getHeight() / 2f, paint);
            paint.setTextSize(cell * .7f);
            paint.setFakeBoldText(false);
            paint.setColor(Color.rgb(170, 184, 172));
            canvas.drawText(gameOver ? "Tippen für eine neue Runde" : "Mit Weiter fortsetzen",
                    getWidth() / 2f, getHeight() / 2f + cell * 1.5f, paint);
        }
    }

    private void drawCell(Canvas canvas, Cell cell, float left, float top, float size, float inset) {
        float padding = size * inset;
        RectF rect = new RectF(left + cell.x * size + padding, top + cell.y * size + padding,
                left + (cell.x + 1) * size - padding, top + (cell.y + 1) * size - padding);
        canvas.drawRoundRect(rect, size * .25f, size * .25f, paint);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchX = event.getX();
            touchY = event.getY();
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (gameOver) {
                reset();
                performClick();
                return true;
            }
            float dx = event.getX() - touchX;
            float dy = event.getY() - touchY;
            if (Math.max(Math.abs(dx), Math.abs(dy)) < 24 * getResources().getDisplayMetrics().density) return true;
            Direction candidate = Math.abs(dx) > Math.abs(dy)
                    ? (dx > 0 ? Direction.RIGHT : Direction.LEFT)
                    : (dy > 0 ? Direction.DOWN : Direction.UP);
            if (!candidate.isOpposite(direction)) queuedDirection = candidate;
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

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!paused && !gameOver) {
            handler.removeCallbacks(tick);
            handler.postDelayed(tick, TICK_MS);
        }
    }

    private enum Direction {
        UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);
        final int dx;
        final int dy;
        Direction(int dx, int dy) { this.dx = dx; this.dy = dy; }
        boolean isOpposite(Direction other) { return dx + other.dx == 0 && dy + other.dy == 0; }
    }

    private static final class Cell {
        final int x;
        final int y;
        Cell(int x, int y) { this.x = x; this.y = y; }
        @Override public boolean equals(Object object) {
            if (!(object instanceof Cell)) return false;
            Cell other = (Cell) object;
            return x == other.x && y == other.y;
        }
        @Override public int hashCode() { return 31 * x + y; }
    }
}
