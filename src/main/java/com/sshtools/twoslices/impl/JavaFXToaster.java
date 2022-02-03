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
import com.sshtools.twoslices.ToastBuilder;
import com.sshtools.twoslices.ToastBuilder.ToastAction;
import com.sshtools.twoslices.ToastType;
import com.sshtools.twoslices.ToasterSettings;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

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
	public void toast(ToastBuilder builder) {
		maybeRunLater(() -> {
			var n = Notifications.create();
			var type = builder.type();
			n.title(builder.title());
			n.text(builder.content());
			n.threshold(3, Notifications.create().title("Collapsed Notification"));
			List<Action> as = new ArrayList<>();
			for (ToastAction a : builder.actions()) {
				Action action = new Action(a.displayName(), (e) -> {
					if (a.listener() != null)
						a.listener().action();
					for(Window w : Stage.getWindows()) {
						if(w instanceof Popup) {
							Popup popup = (Popup)w;
							Scene s = popup.getScene();
							for(Node node : s.getRoot().getChildrenUnmodifiable()) {
								if(node.getClass().getTypeName().startsWith("org.controlsfx.control.Notifications$NotificationPopupHandler")) {
									popup.hide();
									if (builder.closed() != null)
										builder.closed().action();
									return;
								}
							}
						}
					}
				});
				as.add(action);
			}
			if (builder.image() != null) {
				var url = ensureURL(builder.image());
				/*TODO load image on different thread first? */
				var iview = new ImageView(new Image(url, false));
				iview.setPreserveRatio(true);
				var anchorPane = new AnchorPane(iview);
				anchorPane.setMaxWidth(256);
				anchorPane.setMaxHeight(256);
				n.graphic(anchorPane);
				type = ToastType.NONE;
			}
			n.action(as.toArray(new Action[0]));
			n.darkStyle();
			n.position(calcPos());
			n.onAction((e) -> {
				if (builder.closed() != null)
					builder.closed().action();
			});
			if (configuration.getParent() == null) {
				if (hidden == null && Utils.getWindow(null) == null) {
					/*
					 * TODO not entirely convinced the stage will always be hidden. Seems to be here
					 * on Linux Mint
					 */
					hidden = new Stage(StageStyle.UTILITY);
					Text text = new Text(10, 40, " ");
					Scene scene = new Scene(new Group(text));
					hidden.setScene(scene);
					hidden.sizeToScene();
				}
				if (hidden != null)
					hidden.show();
				showNotification(type, n);
			} else {
				n.owner(configuration.getParent());
				showNotification(type, n);
			}
		});
	}

	private Pos calcPos() {
		if (configuration.getPosition() == null) {
			if (System.getProperty("os.name", "").indexOf("Mac OS X") > -1) {
				return Pos.TOP_RIGHT;
			} else
				return Pos.BOTTOM_RIGHT;
		} else {
			switch (configuration.getPosition()) {
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
