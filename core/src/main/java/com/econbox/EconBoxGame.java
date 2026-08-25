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

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class EconBoxGame extends Game {

    /**
     * Low-resolution pixel canvas. The window is an exact 4x of this, so every world unit
     * lands on exactly 4x4 screen pixels and nothing ends up blurred or half-lit.
     */
    public static final int WORLD_WIDTH = 320;

    /** @see #WORLD_WIDTH */
    public static final int WORLD_HEIGHT = 180;

    private SpriteBatch batch;
    private Skin skin;

    @Override
    public void create() {
        batch = new SpriteBatch();
        // Skin finds the sibling ui/uiskin.atlas on its own.
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        setScreen(new FirstScreen(this));
    }

    /** Shared sprite batch. Screens draw with it and must not dispose it. */
    public SpriteBatch batch() {
        return batch;
    }

    /** Shared skin, which owns the fonts and atlas regions it hands out. */
    public Skin skin() {
        return skin;
    }

    @Override
    public void dispose() {
        // Game.dispose() only hides the active screen, and setScreen() does not dispose the
        // outgoing one either, so screens have to be disposed explicitly.
        Screen current = getScreen();
        super.dispose();
        if (current != null) {
            current.dispose();
        }
        skin.dispose();
        batch.dispose();
    }
}
