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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.controlsfx.control.Notifications;
import org.controlsfx.control.action.Action;
import org.controlsfx.tools.Utils;

import com.sshtools.twoslices.AbstractToaster;
import com.sshtools.twoslices.BasicToastHint;
import com.sshtools.twoslices.Capability;
import com.sshtools.twoslices.Slice;
import com.sshtools.twoslices.ToastBuilder;
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
import javafx.scene.paint.Color;
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
	 * 
	 * Deprecated, use {@link BasicToastHint}.
	 */
	@Deprecated
	public final static String STYLESHEET = BasicToastHint.STYLESHEET.toLegacyKey();

	/**
	 * Key for {@link ToasterSettings#getProperties()} hint for a CSS stylesheet URI
	 * to use. Should be a {@link List} of {@link String}.
	 * 
	 * Deprecated, use {@link BasicToastHint}.
	 */
	@Deprecated
	public final static String STYLESHEETS = BasicToastHint.STYLESHEETS.toLegacyKey();

	/**
	 * Key for {@link ToasterSettings#getProperties()} hint for a an instance of a {@link Function<ToastType, Node>}. 
	 * This should generate a new node for a given toast type.
	 * 
	 * Deprecated, use {@link BasicToastHint}.
	 */
	@Deprecated
	public final static String TYPE_ICON_GENERATOR = BasicToastHint.TYPE_ICON_GENERATOR.toLegacyKey();
	/**
	 * Key for {@link ToasterSettings#getProperties()} hint for a CSS style to use.
	 * Should be a {@link String}.
	 * 
	 * Deprecated, use {@link BasicToastHint}.
	 */
	@Deprecated
	public final static String STYLE = BasicToastHint.STYLE.toLegacyKey();
	/**
	 * Key for {@link ToasterSettings#getProperties()} hinting to use dark mode.
	 * 
	 * Deprecated, use {@link BasicToastHint}.
	 */
	@Deprecated
	public final static String DARK = BasicToastHint.DARK.toLegacyKey();

	/**
	 * Key for {@link ToasterSettings#getProperties()} hinting of max number of
	 * stacked messages before they are collapsed. Should be a {@link Integer}.
	 * 
	 * Deprecated, use {@link BasicToastHint}.
	 */
	@Deprecated
	public final static String THRESHOLD = BasicToastHint.THRESHOLD.toLegacyKey();

	/**
	 * Key for {@link ToasterSettings#getProperties()} hinting of the text to use
	 * for collapsed messages. Should be a {@link String}.
	 * 
	 * Deprecated, use {@link BasicToastHint}.
	 */
	@Deprecated
	public final static String COLLAPSE_MESSAGE = BasicToastHint.COLLAPSE_MESSAGE.toLegacyKey();

	public static class Service implements ToasterService {
		@Override
		public Toaster create(ToasterSettings settings) {
			return new JavaFXToaster(settings);
		}
	}
	
	class JavaFXSlice implements Slice {
		
		private String title;
		private String content;

		JavaFXSlice(String title, String content) {
			this.title = title;
			this.content = content;
		}
		
		@Override
		public void close() throws IOException {
			findPopup(title, content).hide();
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
		capabilities.addAll(Arrays.asList(Capability.ACTIONS, Capability.CLOSE, Capability.IMAGES));
		try {
			Class.forName("org.controlsfx.control.Notifications");
		} catch (ClassNotFoundException cnfe) {
			throw new UnsupportedOperationException();
		}

		Stage.getWindows().addListener(new ListChangeListener<Window>() {
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
									String css = getHint(BasicToastHint.STYLESHEET);
									if (css != null)
										s.getStylesheets().add(css);
									List<String> csss = getHint(BasicToastHint.STYLESHEETS);
									if (csss != null)
										s.getStylesheets().addAll(csss);
									String style = getHint(BasicToastHint.STYLE);
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
	}

	@Override
	public Slice toast(ToastBuilder builder) {
		var slice = new JavaFXSlice(builder.title(), builder.content());
		maybeRunLater(() -> {
			var n = Notifications.create();
			var type = builder.type();
			n.hideAfter(builder.timeout() == 0 ? Duration.INDEFINITE
					: Duration.seconds((builder.timeout() == -1 ? configuration.getTimeout() : builder.timeout())));
			n.title(builder.title());
			n.text(builder.content());
			n.threshold((Integer) getHint(BasicToastHint.THRESHOLD, 3),
					Notifications.create().title((String) getHint(builder.hints(), BasicToastHint.COLLAPSE_MESSAGE,
							"Collapsed Notifications")));
			List<Action> as = new ArrayList<>();
			for (var a : builder.actions()) {
				var action = new Action(a.displayName(), (e) -> {
					if (a.listener() != null)
						a.listener().action();
					findPopup(builder.title(), builder.content()).hide();
					if (builder.closed() != null)
						builder.closed().action();
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
			else  {
				@SuppressWarnings("unchecked")
				var typeIconGenerator = (Function<ToastType, Node>)getHint(builder.hints(), BasicToastHint.TYPE_ICON_GENERATOR);
				if(typeIconGenerator != null) {
					n.graphic(typeIconGenerator.apply(type));
					type = ToastType.NONE;
				}
			}
			n.action(as.toArray(new Action[0]));
			if (Boolean.TRUE.equals(getHint(BasicToastHint.DARK)))
				n.darkStyle();
			n.position(calcPos());
			n.onAction((e) -> {
				if (builder.closed() != null)
					builder.closed().action();
			});
			if (configuration.getParent() == null) {
				if (hidden == null && Utils.getWindow(null) == null) {
					if(Boolean.getBoolean("twoslices.javafx.oldHiddenWindow")) {
						if (hidden == null && Utils.getWindow(null) == null) {
							/*
							 * TODO not entirely convinced the stage will always be hidden. Seems to be here
							 * on Linux Mint
							 */
							hidden = new Stage(StageStyle.UTILITY);
							var text = new Text(10, 40, " ");
							var scene = new Scene(new Group(text));
							hidden.setScene(scene);
							hidden.sizeToScene();
						}
					}
					else {
						/*
						 * TODO not entirely convinced the stage will always be hidden. Seems to be here
						 * on Linux Mint
						 */
						hidden = new Stage(StageStyle.UTILITY);
						
						var text = new Text(10, 40, " ");
						text.setStyle("-fx-background-color: transparent;");
						
						var grp = new Group(text);
						grp.setStyle("-fx-background-color: transparent;");
						
	                    var scene = new Scene(grp);
	                    scene.setFill(Color.TRANSPARENT);
	                    
						hidden.setScene(scene);
						hidden.sizeToScene();
						hidden.setOpacity(0);
					}
				}
				if (hidden != null)
					hidden.show();
				showNotification(type, n);
			} else {
				n.owner(configuration.getParent());
				showNotification(type, n);
			}
		});
		return slice;
	}
	
	private Popup findPopup(String title, String content) {
		for (var w : Stage.getWindows()) {
			if (w instanceof Popup) {
				var popup = (Popup) w;
				var s = popup.getScene();
				for (var node : s.getRoot().getChildrenUnmodifiable()) {
					if (node.getClass().getTypeName()
							.startsWith("org.controlsfx.control.Notifications$NotificationPopupHandler")) {
						return popup;
					}
				}
			}
		}
		throw new IllegalStateException("No popup windows.");
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
