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
package im.bci.lwjgl.nuit.widgets;

import im.bci.lwjgl.nuit.NuitToolkit;
import im.bci.lwjgl.nuit.controls.Action;
import im.bci.lwjgl.nuit.controls.Control;
import im.bci.lwjgl.nuit.controls.ControlActivatedDetector;
import im.bci.lwjgl.nuit.controls.ControlsUtils;
import im.bci.lwjgl.nuit.controls.GamepadAxisControl;
import im.bci.lwjgl.nuit.controls.GamepadButtonControl;
import im.bci.lwjgl.nuit.controls.KeyControl;
import im.bci.lwjgl.nuit.controls.MouseButtonControl;
import im.bci.lwjgl.nuit.utils.TrueTypeFont;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class ControlsConfigurator extends Table {

    private List<ControlActivatedDetector> possibleControls;
    private final List<Action> actions;
    private final NuitToolkit toolkit;
    private List<Action> resets;
    private final List<Action> defaults;

    public ControlsConfigurator(NuitToolkit toolkit, List<Action> actions, List<Action> defaults) {
        super(toolkit);
        this.toolkit = toolkit;
        this.actions = actions;
        this.defaults = defaults;
        initResets();
        initPossibleControls();
        initUI(toolkit);
    }

	private void initResets() {
		this.resets = new ArrayList<>();
        for (Action action : actions) {
            resets.add(new Action(action));
        }
	}

    private void initUI(NuitToolkit toolkit) {
        this.defaults().expand();
        this.cell(new Label(toolkit, "nuit.controls.configurator.action"));
        this.cell(new Label(toolkit, "nuit.controls.configurator.control"));
        this.cell(new Label(toolkit, "nuit.controls.configurator.alternative"));
        this.row();
        for (final Action action : actions) {
            this.cell(new Label(toolkit, action.getName()));
            ControlConfigurator mainConfigurator = new ControlConfigurator() {
                @Override
                public Control getControl() {
                    return action.getMainControl();
                }

                @Override
                public void setControl(Control control) {
                    action.setMainControl(control);
                }
            };
            this.cell(mainConfigurator).fill();
            ControlConfigurator alternativeConfigurator = new ControlConfigurator() {
                @Override
                public Control getControl() {
                    return action.getAlternativeControl();
                }

                @Override
                public void setControl(Control control) {
                    action.setAlternativeControl(control);
                }
            };
            this.cell(alternativeConfigurator).fill();
            this.row();
        }

        this.cell(new Button(toolkit, "nuit.controls.configurator.back") {
            @Override
            public void onOK() {
                onBack();
            }
        });
        this.cell(new Button(toolkit, "nuit.controls.configurator.resets") {
            @Override
            public void onOK() {
                onReset();
            }
        });
        this.cell(new Button(toolkit, "nuit.controls.configurator.defaults") {
            @Override
            public void onOK() {
                onDefaults();
            }
        });
    }

    public abstract class ControlConfigurator extends Widget {

        private boolean suckFocus;

        public abstract Control getControl();

        public abstract void setControl(Control control);

        @Override
        public boolean isFocusWhore() {
            return true;
        }

        @Override
        public void suckFocus() {
            for (ControlActivatedDetector control : possibleControls) {
                control.reset();
            }
            suckFocus = true;
        }
        
        @Override
        public void onMouseClick(float mouseX, float mouseY) {
        	suckFocus();
        }

        @Override
        public boolean isSuckingFocus() {
            return suckFocus;
        }

        @Override
        public void update() {
            if (isSuckingFocus()) {
                for (ControlActivatedDetector control : possibleControls) {
                    control.poll();
                    if (control.isActivated()) {
                        suckFocus = false;
                        setControl(control.getControl());
                        toolkit.resetInputPoll();
                    }
                }
            }
        }

        @Override
        public void draw() {
            String text = null;
            if (suckFocus) {
                text = toolkit.getMessage("nuit.controls.configurator.press.key");
            } else if (null != getControl()) {
                text = toolkit.getMessage(getControl().getName());
            }
            
            if (null != text) {
                GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_ENABLE_BIT);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glPushMatrix();
                TrueTypeFont font = toolkit.getFont();
                GL11.glTranslatef(getX() + getWidth() / 2.0f - font.getWidth(text) / 4.0f, getY() + getHeight() / 2.0f + font.getHeight(text) / 2.0f, 0.0f);
                GL11.glScalef(1, -1, 1);
                font.drawString(text);
                GL11.glPopAttrib();
                GL11.glPopMatrix();
            }
        }

        @Override
        public float getMinWidth() {
            TrueTypeFont font = toolkit.getFont();
            return Math.max(font.getWidth(toolkit.getMessage("nuit.controls.configurator.press.key")), font.getWidth(getControl().getName()));
        }

        @Override
        public float getMinHeight() {
 TrueTypeFont font = toolkit.getFont();
            return Math.max(font.getHeight(toolkit.getMessage("nuit.controls.configurator.press.key")), font.getHeight(getControl().getName()));
        }
        
        
    }

    protected void onDefaults() {
        if (null != defaults) {
            for (int i = 0; i < defaults.size(); ++i) {
                actions.get(i).setMainControl(defaults.get(i).getMainControl());
                actions.get(i).setAlternativeControl(defaults.get(i).getAlternativeControl());
            }
        }
    }

    protected void onReset() {
        for (int i = 0; i < resets.size(); ++i) {
            actions.get(i).setMainControl(resets.get(i).getMainControl());
            actions.get(i).setAlternativeControl(resets.get(i).getAlternativeControl());
        }
    }

    public void onBack() {
    }
    
    @Override
    public void onShow() {
    	initResets();
    }

    private void initPossibleControls() {
        possibleControls = new ArrayList<>();
        for(Control control : ControlsUtils.getPossibleControls()) {
            possibleControls.add(new ControlActivatedDetector(control));            
        }
    }

}
