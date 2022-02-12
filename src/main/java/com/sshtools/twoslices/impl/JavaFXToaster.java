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
import com.sshtools.twoslices.Slice;
import com.sshtools.twoslices.ToastBuilder;
import com.sshtools.twoslices.ToastBuilder.ToastAction;
import com.sshtools.twoslices.ToastType;
import com.sshtools.twoslices.Toaster;
import com.sshtools.twoslices.ToasterService;
import com.sshtools.twoslices.ToasterSettings;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
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
import javafx.util.Duration;

/**
 * Fall-back notifier for when a native notification system cannot be located or
 * used, and JavaFX toolkit is available. ControlsFX must be available on the
 * classpath.
 */
public class JavaFXToaster extends AbstractToaster {

	/**
	 * Key for {@link ToasterSettings#getProperties()} hint for a CSS stylesheet URI
	 * to use. Should be a {@link String}.
	 */
	public final static String STYLESHEET = "stylesheet";

	/**
	 * Key for {@link ToasterSettings#getProperties()} hint for a CSS stylesheet URI
	 * to use. Should be a {@link List} of {@link String}.
	 */
	public final static String STYLESHEETS = "stylesheets";
	/**
	 * Key for {@link ToasterSettings#getProperties()} hint for a CSS style to use.
	 * Should be a {@link String}.
	 */
	public final static String STYLE = "style";
	/**
	 * Key for {@link ToasterSettings#getProperties()} hinting to use dark mode.
	 * Should be a {@link Boolean}.
	 */
	public final static String DARK = "dark";

	/**
	 * Key for {@link ToasterSettings#getProperties()} hinting of max number of
	 * stacked messages before they are collapsed. Should be a {@link Integer}.
	 */
	public final static String THRESHOLD = "threshold";

	/**
	 * Key for {@link ToasterSettings#getProperties()} hinting of the text to use
	 * for collapsed messages. Should be a {@link String}.
	 */
	public final static String COLLAPSE_MESSAGE = "collapseMessage";

	public static class Service implements ToasterService {
		@Override
		public Toaster create(ToasterSettings settings) {
			return new JavaFXToaster(settings);
		}
	}

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
	public Slice toast(ToastBuilder builder) {
		maybeRunLater(() -> {
			Stage.getWindows().addListener(new ListChangeListener<Window>() {
				@SuppressWarnings("unchecked")
				@Override
				public void onChanged(Change<? extends Window> c) {
					while (c.next()) {
						for (Window w : c.getAddedSubList()) {
							if (w instanceof Popup) {
								Popup popup = (Popup) w;
								Scene s = popup.getScene();
								for (Node node : s.getRoot().getChildrenUnmodifiable()) {
									if (node.getClass().getTypeName().startsWith(
											"org.controlsfx.control.Notifications$NotificationPopupHandler")) {
										String css = (String) configuration.getProperties().get(STYLESHEET);
										if (css != null)
											s.getStylesheets().add(css);
										List<String> csss = (List<String>) configuration.getProperties()
												.get(STYLESHEETS);
										if (csss != null)
											s.getStylesheets().addAll(csss);
										String style = (String) configuration.getProperties().get(STYLE);
										if (style != null)
											s.getRoot().setStyle(style);
										return;
									}
								}
							}
						}
					}
				}
			});
			var n = Notifications.create();
			var type = builder.type();
			n.hideAfter(builder.timeout() == 0 ? Duration.INDEFINITE
					: Duration.seconds((builder.timeout() == -1 ? configuration.getTimeout() : builder.timeout())));
			n.title(builder.title());
			n.text(builder.content());
			n.threshold((Integer) configuration.getProperties().getOrDefault(THRESHOLD, 3),
					Notifications.create().title((String) configuration.getProperties().getOrDefault(COLLAPSE_MESSAGE,
							"Collapsed Notifications")));
			List<Action> as = new ArrayList<>();
			for (ToastAction a : builder.actions()) {
				Action action = new Action(a.displayName(), (e) -> {
					if (a.listener() != null)
						a.listener().action();
					for (Window w : Stage.getWindows()) {
						if (w instanceof Popup) {
							Popup popup = (Popup) w;
							Scene s = popup.getScene();
							for (Node node : s.getRoot().getChildrenUnmodifiable()) {
								if (node.getClass().getTypeName()
										.startsWith("org.controlsfx.control.Notifications$NotificationPopupHandler")) {
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
				/* TODO load image on different thread first? */
				var iview = new ImageView(new Image(url, false));
				iview.setPreserveRatio(true);
				var anchorPane = new AnchorPane(iview);
				anchorPane.setMaxWidth(256);
				anchorPane.setMaxHeight(256);
				n.graphic(anchorPane);
				type = ToastType.NONE;
			}
			n.action(as.toArray(new Action[0]));
			if (Boolean.TRUE.equals(configuration.getProperties().get(DARK)))
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
		return Slice.defaultSlice();
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
