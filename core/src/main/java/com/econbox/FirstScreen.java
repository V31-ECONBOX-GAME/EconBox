/*
 * Copyright 2026-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.econbox;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * A 2D pixel-art sky: banded gradient, drifting clouds, and a flock of birds. The orange one
 * flies with the arrow keys or WASD.
 *
 * <p>Sprites are plain char grids and every draw is snapped to a whole world unit, which is what
 * keeps the pixels hard-edged instead of smeared across half a texel.
 */
public class FirstScreen implements Screen {

    private static final String[] BIRD_UP = {
        "#...........#",
        ".##.......##.",
        "...##...##...",
        ".....###.....",
        "......#......",
    };

    private static final String[] BIRD_MID = {
        ".............",
        "##.........##",
        "..#########..",
        ".....###.....",
        ".............",
    };

    private static final String[] BIRD_DOWN = {
        ".....###.....",
        "......#......",
        "...##...##...",
        ".##.......##.",
        "#...........#",
    };

    private static final String[][] BIRD_FRAMES = {BIRD_UP, BIRD_MID, BIRD_DOWN, BIRD_MID};

    private static final String[] CLOUD = {
        "....######....",
        "..##########..",
        ".############.",
        "##############",
    };

    /** Bottom to top. Hard edges rather than a smooth ramp, the way an 8-bit sky was drawn. */
    private static final Color[] SKY = {
        new Color(0xc4e6faff),
        new Color(0x8fc6f2ff),
        new Color(0x5b9ae0ff),
        new Color(0x3b6fc4ff),
        new Color(0x2b4c9bff),
    };

    private static final Color SUN_CORE = new Color(0xfff3b0ff);
    private static final Color SUN_RIM = new Color(0xffc94dff);
    private static final Color CLOUD_COLOR = new Color(0xf7fbffff);
    private static final Color BIRD_COLOR = new Color(0x1b2333ff);
    private static final Color PLAYER_COLOR = new Color(0xff7a3dff);

    private static final int FLOCK = 7;
    private static final int CLOUDS = 4;
    private static final int SUN_RADIUS = 13;
    private static final float PLAYER_SPEED = 78f;

    private final EconBoxGame game;
    private final Viewport viewport;
    private final BitmapFont font;
    private final TextureRegion pixel;

    private final Bird[] flock = new Bird[FLOCK];
    private final Bird player = new Bird();
    private final Vector2 input = new Vector2();

    private final float[] cloudX = new float[CLOUDS];
    private final int[] cloudY = new int[CLOUDS];
    private final int[] cloudScale = new int[CLOUDS];
    private final float[] cloudSpeed = new float[CLOUDS];

    public FirstScreen(EconBoxGame game) {
        this.game = game;
        this.viewport = new FitViewport(EconBoxGame.WORLD_WIDTH, EconBoxGame.WORLD_HEIGHT);
        this.font = game.skin().getFont("font");
        this.pixel = game.skin().getRegion("white");

        for (int i = 0; i < CLOUDS; i++) {
            cloudScale[i] = MathUtils.random(1, 2);
            cloudX[i] = MathUtils.random(0f, EconBoxGame.WORLD_WIDTH);
            cloudY[i] = MathUtils.random(96, EconBoxGame.WORLD_HEIGHT - 24);
            cloudSpeed[i] = MathUtils.random(2.5f, 6f);
        }

        for (int i = 0; i < FLOCK; i++) {
            Bird bird = new Bird();
            bird.x = MathUtils.random(0f, EconBoxGame.WORLD_WIDTH);
            bird.y = MathUtils.random(22f, EconBoxGame.WORLD_HEIGHT - 22f);
            bird.speed = MathUtils.random(9f, 26f) * (MathUtils.randomBoolean() ? 1f : -1f);
            bird.scale = MathUtils.random(1, 2);
            bird.flapSpeed = MathUtils.random(5f, 9f);
            bird.flap = MathUtils.random(4f);
            bird.bob = MathUtils.random(MathUtils.PI2);
            flock[i] = bird;
        }

        player.x = EconBoxGame.WORLD_WIDTH * 0.32f;
        player.y = EconBoxGame.WORLD_HEIGHT * 0.45f;
        player.scale = 2;
        player.flapSpeed = 8f;
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        updateClouds(delta);
        updateFlock(delta);
        updatePlayer(delta);

        viewport.apply();
        SpriteBatch batch = game.batch();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        drawSky(batch);
        drawSun(batch);
        drawClouds(batch);
        drawBirds(batch);
        drawHud(batch);
        batch.end();
    }

    private void drawSky(SpriteBatch batch) {
        int bandHeight = EconBoxGame.WORLD_HEIGHT / SKY.length;
        for (int i = 0; i < SKY.length; i++) {
            batch.setColor(SKY[i]);
            int height = (i == SKY.length - 1) ? EconBoxGame.WORLD_HEIGHT - i * bandHeight : bandHeight;
            batch.draw(pixel, 0f, i * bandHeight, EconBoxGame.WORLD_WIDTH, height);
        }
    }

    /** A filled circle drawn one horizontal span per row, so its edge steps like real pixel art. */
    private void drawSun(SpriteBatch batch) {
        int cx = EconBoxGame.WORLD_WIDTH - 46;
        int cy = EconBoxGame.WORLD_HEIGHT - 40;
        drawDisc(batch, cx, cy, SUN_RADIUS + 3, SUN_RIM);
        drawDisc(batch, cx, cy, SUN_RADIUS, SUN_CORE);
    }

    private void drawDisc(SpriteBatch batch, int cx, int cy, int radius, Color color) {
        batch.setColor(color);
        for (int dy = -radius; dy <= radius; dy++) {
            int half = (int) Math.round(Math.sqrt(radius * radius - dy * dy));
            if (half > 0) {
                batch.draw(pixel, cx - half, cy + dy, half * 2f, 1f);
            }
        }
    }

    private void drawClouds(SpriteBatch batch) {
        batch.setColor(CLOUD_COLOR);
        for (int i = 0; i < CLOUDS; i++) {
            drawSprite(batch, CLOUD, cloudX[i], cloudY[i], cloudScale[i]);
        }
    }

    private void drawBirds(SpriteBatch batch) {
        batch.setColor(BIRD_COLOR);
        for (Bird bird : flock) {
            drawSprite(batch, BIRD_FRAMES[bird.frame()], bird.x, bird.y + bird.bobOffset(), bird.scale);
        }
        batch.setColor(PLAYER_COLOR);
        drawSprite(batch, BIRD_FRAMES[player.frame()], player.x, player.y, player.scale);
    }

    /**
     * Draws a char grid, one quad per set cell. Row 0 is the top row. Coordinates are floored so
     * a sprite never straddles a world unit.
     */
    private void drawSprite(SpriteBatch batch, String[] rows, float x, float y, int scale) {
        int originX = MathUtils.floor(x);
        int originY = MathUtils.floor(y);
        for (int row = 0; row < rows.length; row++) {
            String line = rows[row];
            int cellY = originY + (rows.length - 1 - row) * scale;
            for (int column = 0; column < line.length(); column++) {
                if (line.charAt(column) != '.') {
                    batch.draw(pixel, originX + column * scale, cellY, scale, scale);
                }
            }
        }
    }

    private void drawHud(SpriteBatch batch) {
        batch.setColor(Color.WHITE);
        font.setColor(1f, 1f, 1f, 0.95f);
        font.draw(batch, "EconBox", 6f, EconBoxGame.WORLD_HEIGHT - 5f);
        font.setColor(1f, 1f, 1f, 0.75f);
        font.draw(batch, "WASD / arrows", 6f, EconBoxGame.WORLD_HEIGHT - 20f);
        font.draw(batch, Gdx.graphics.getFramesPerSecond() + " fps", 6f, 18f);
    }

    private void updateClouds(float delta) {
        for (int i = 0; i < CLOUDS; i++) {
            cloudX[i] += cloudSpeed[i] * delta;
            if (cloudX[i] > EconBoxGame.WORLD_WIDTH) {
                cloudX[i] = -CLOUD[0].length() * cloudScale[i];
                cloudY[i] = MathUtils.random(96, EconBoxGame.WORLD_HEIGHT - 24);
            }
        }
    }

    private void updateFlock(float delta) {
        for (Bird bird : flock) {
            bird.flap += bird.flapSpeed * delta;
            bird.bob += delta * 2f;
            bird.x += bird.speed * delta;

            float margin = BIRD_MID[0].length() * bird.scale;
            if (bird.x < -margin) {
                bird.x = EconBoxGame.WORLD_WIDTH + margin;
            } else if (bird.x > EconBoxGame.WORLD_WIDTH + margin) {
                bird.x = -margin;
            }
        }
    }

    private void updatePlayer(float delta) {
        input.setZero();
        if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A)) {
            input.x -= 1f;
        }
        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) {
            input.x += 1f;
        }
        if (Gdx.input.isKeyPressed(Keys.DOWN) || Gdx.input.isKeyPressed(Keys.S)) {
            input.y -= 1f;
        }
        if (Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.isKeyPressed(Keys.W)) {
            input.y += 1f;
        }

        // Beat harder while climbing, glide when the keys are released.
        player.flap += (input.isZero() ? 3f : 13f) * delta;

        if (!input.isZero()) {
            input.nor();
            player.x += input.x * PLAYER_SPEED * delta;
            player.y += input.y * PLAYER_SPEED * delta;
        }

        int width = BIRD_MID[0].length() * player.scale;
        int height = BIRD_MID.length * player.scale;
        player.x = MathUtils.clamp(player.x, 0f, EconBoxGame.WORLD_WIDTH - width);
        player.y = MathUtils.clamp(player.y, 0f, EconBoxGame.WORLD_HEIGHT - height);
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }

    private static final class Bird {
        float x;
        float y;
        float speed;
        float flap;
        float flapSpeed = 6f;
        float bob;
        int scale = 1;

        int frame() {
            return ((int) flap) & 3;
        }

        float bobOffset() {
            return MathUtils.round(MathUtils.sin(bob) * scale);
        }
    }
}
