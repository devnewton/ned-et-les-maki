/*
 The MIT License (MIT)

 Copyright (c) 2013 devnewton <devnewton@bci.im>

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */
package org.geekygoblin.nedetlesmaki.game.components.ui;

import com.artemis.Component;
import im.bci.jnuit.NuitToolkit;
import im.bci.jnuit.widgets.Button;
import im.bci.jnuit.widgets.Container;
import im.bci.jnuit.widgets.Label;
import im.bci.jnuit.widgets.Root;
import im.bci.jnuit.animation.IPlay;
import java.util.ArrayList;
import com.google.inject.Inject;
import im.bci.jnuit.background.TexturedBackground;
import im.bci.jnuit.NuitRenderer;
import org.geekygoblin.nedetlesmaki.game.Game;
import org.lwjgl.LWJGLException;

/**
 *
 * @author devnewton
 */
public class Dialog extends Component {

    private final Root root;
    private final Game game;
    private final ArrayList<Sentence> sentences = new ArrayList<>();
    private int currentSentenceIndex;
    private IPlay currentPlay;
    private boolean finished;
    private final Label textLabel;
    private final NuitRenderer nuitRenderer;

    private Sentence getCurrentSentence() {
        if (!sentences.isEmpty()) {
            return sentences.get(currentSentenceIndex);
        } else {
            return null;
        }
    }

    private static class Sentence {

        IPlay play;
        String text;

        public Sentence(IPlay play, String text) {
            this.play = play;
            this.text = text;
        }
    }

    @Inject
    public Dialog(NuitToolkit toolkit, NuitRenderer nuitRenderer, Game game) throws LWJGLException {
        this.game = game;
        root = new Root(toolkit);
        this.nuitRenderer = nuitRenderer;
        Container layout = new Container();
        root.add(layout);

        Button nextButton = new Button(toolkit, "dialog.button.next") {

            @Override
            public void onOK() {
                onNext();
            }

        };
        Button previousButton = new Button(toolkit, "dialog.button.previous") {
            @Override
            public void onOK() {
                onPrevious();
            }
        };

        final float buttonsWidth = Math.max(nextButton.getMinWidth(), previousButton.getMinWidth());

        this.textLabel = new Label(toolkit, "Il était une fois...");
        textLabel.setX(0);
        textLabel.setWidth(layout.getWidth() - buttonsWidth);
        textLabel.setHeight(layout.getHeight() * 0.2f);
        textLabel.setY(layout.getHeight() - textLabel.getHeight());

        nextButton.setX(textLabel.getWidth());
        nextButton.setY(textLabel.getY());
        nextButton.setWidth(buttonsWidth);
        nextButton.setHeight(textLabel.getHeight() / 2.0f);
        previousButton.setX(textLabel.getWidth());
        previousButton.setY(textLabel.getY() + nextButton.getHeight());
        previousButton.setWidth(buttonsWidth);
        previousButton.setHeight(textLabel.getHeight() / 2.0f);

        layout.add(textLabel);
        layout.add(nextButton);
        layout.add(previousButton);
    }

    public void addTirade(IPlay play, String... sentences) {
        for (String sentence : sentences) {
            this.sentences.add(new Sentence(play, sentence));
        }
        onChangeSentence();
    }

    public void update() {
        final float delta = game.getDelta();
        root.update(delta);
    }

    public void draw() {
        nuitRenderer.render(root);
    }

    protected void onNext() {
        if (currentSentenceIndex < (sentences.size() - 1)) {
            ++currentSentenceIndex;
        } else {
            onFinished();
        }
        onChangeSentence();
    }

    protected void onChangeSentence() {
        Sentence currentSentence = getCurrentSentence();
        if (null != currentSentence) {
            textLabel.setText(currentSentence.text);
            if (currentPlay != currentSentence.play) {
                currentPlay = currentSentence.play;
                currentSentence.play.restart();
                root.setBackground(new TexturedBackground(currentSentence.play));
            }
        }
    }

    protected void onPrevious() {
        if (currentSentenceIndex > 0) {
            --currentSentenceIndex;
        }
        onChangeSentence();
    }

    public boolean isFinished() {
        return finished;
    }

    protected void onFinished() {
        this.finished = true;
    }
}
