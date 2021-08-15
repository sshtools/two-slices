/**
 * Copyright © 2018 SSHTOOLS Limited (support@sshtools.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sshtools.twoslices.impl;

import java.util.ArrayList;
import java.util.List;

import org.controlsfx.control.Notifications;
import org.controlsfx.control.action.Action;
import org.controlsfx.tools.Utils;

import com.sshtools.twoslices.AbstractToaster;
import com.sshtools.twoslices.ToastActionListener;
import com.sshtools.twoslices.ToastType;
import com.sshtools.twoslices.ToasterSettings;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Fall-back notifier for when a native notification system cannot be located or
 * used, and JavaFX toolkit is available. ControlsFX must be available on the
 * classpath.
 */
public class JavaFXToaster extends AbstractToaster {
	private Stage hidden;

	/**
	 * Constructor
	 * 
	 * @param configuration configuration
	 */
	public JavaFXToaster(ToasterSettings configuration) {
		super(configuration);
		try {
			Class.forName("org.controlsfx.control.Notifications");
		} catch (ClassNotFoundException cnfe) {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void toast(ToastType type, String icon, String title, String content, ToastActionListener... listeners) {
		maybeRunLater(() -> {
			Notifications n = Notifications.create();
			n.title(title);
			n.text(content);
			n.threshold(3, Notifications.create().title("Collapsed Notification"));
			List<Action> as = new ArrayList<>();
			for (ToastActionListener a : listeners) {
				Action action = new Action(a.getName(), (e) -> {
					a.action();
				});
				as.add(action);
			}
			n.action(as.toArray(new Action[0]));
			n.position(calcPos());
			n.onAction((e) -> {
			});
			if (configuration.getParent() == null) {
				if (hidden == null && Utils.getWindow(null) == null) {
					/* TODO  not entirely convinced the stage will always be hidden. 
					 * Seems to be here on Linux Mint
					 */
					hidden = new Stage(StageStyle.UTILITY);
					Text text = new Text(10, 40, " ");
					Scene scene = new Scene(new Group(text));
					hidden.setScene(scene);
					hidden.sizeToScene();
				}
				if(hidden != null)
					hidden.show();
				Platform.runLater(() -> showNotification(type, n));
			} else {
				n.owner(configuration.getParent());
				showNotification(type, n);
			}
		});
	}

	private Pos calcPos() {
		if(configuration.getPosition() == null) {
			if(System.getProperty("os.name", "").indexOf("mac") > -1) {
				return Pos.TOP_RIGHT;
			}
			else
				return Pos.BOTTOM_RIGHT;
		}
		else {
			switch(configuration.getPosition()) {
			case TL:
				return Pos.TOP_LEFT;
			case T:
				return Pos.TOP_CENTER;
			case TR:
				return Pos.TOP_RIGHT;
			case CL:
				return Pos.CENTER_LEFT;
			case C:
				return Pos.CENTER;
			case CR:
				return Pos.CENTER_RIGHT;
			case BL:
				return Pos.BOTTOM_LEFT;
			case B:
				return Pos.BOTTOM_CENTER;
			default:
				return Pos.BOTTOM_RIGHT;
			}
		}
	}

	protected void showNotification(ToastType type, Notifications n) {
		switch (type) {
		case ERROR:
			n.showError();
			break;
		case WARNING:
			n.showWarning();
			break;
		case INFO:
			n.showInformation();
			;
			break;
		default:
			n.show();
			break;
		}
	}

	void maybeRunLater(Runnable r) {
		if (Platform.isFxApplicationThread())
			r.run();
		else
			Platform.runLater(r);
	}
}
