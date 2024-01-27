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
package com.sshtools.twoslices.swt;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.sshtools.twoslices.AbstractToaster;
import com.sshtools.twoslices.BasicToastHint;
import com.sshtools.twoslices.Capability;
import com.sshtools.twoslices.Slice;
import com.sshtools.twoslices.ToastActionListener;
import com.sshtools.twoslices.ToastBuilder;
import com.sshtools.twoslices.ToastBuilder.ToastAction;
import com.sshtools.twoslices.ToastType;
import com.sshtools.twoslices.Toaster;
import com.sshtools.twoslices.ToasterService;
import com.sshtools.twoslices.ToasterSettings;
import com.sshtools.twoslices.ToasterSettings.Position;

/**
 * A specially implemented fall-back notifier for when no native notifier can be
 * found or used and the SWT toolkit is available. It creates and manages it's
 * own popup windows.
 */
public class SWTToaster extends AbstractToaster {

	/**
	 * Key for {@link ToasterSettings#getProperties()} hint for an offset between
	 * the edge of the screen (as decided by the {@Position}. Should be an
	 * {@link Integer}.
	 * 
	 * Deprecated. See {@link BasicToastHint}
	 */
	@Deprecated
	public final static String OFFSET = BasicToastHint.OFFSET.toLegacyKey();

	/**
	 * Key for {@link ToasterSettings#getProperties()} hint for the maximum size of
	 * image content. Should be an {@link Integer}. Any size lower than this will be
	 * also be scaled up depending on {@link SWTToaster#SCALE_UP}. Use a size of
	 * zero to prevent scaling entirely.
	 * 
	 * Deprecated. Use {@link BasicToastHint#IMAGE_SIZE}.
	 */
	@Deprecated
	public final static String IMAGE_SIZE = BasicToastHint.IMAGE_SIZE.toLegacyKey();

	/**
	 * Key for {@link ToasterSettings#getProperties()} hint for the maximum size of
	 * the icon. Should be an {@link Integer}. Any size lower than this will be
	 * also be scaled up depending on {@link SWTToaster#SCALE_UP}. Use a size of
	 * zero to prevent scaling entirely.
	 * 
	 * Deprecated. Use {@link BasicToastHint#ICON_SIZE}.
	 */
	@Deprecated
	public final static String ICON_SIZE = BasicToastHint.ICON_SIZE.toLegacyKey();

	/**
	 * Key for {@link ToasterSettings#getProperties()} hint to use animated window
	 * positioning. This is experimental, as there are some issues.
	 * 
	 * Deprecated. Use {@link BasicToastHint#ANIMATED}.
	 */
	@Deprecated
	public final static String ANIMATED = BasicToastHint.ANIMATED.toLegacyKey();

	/**
	 * Key for {@link ToasterSettings#getProperties()} hint to signal which monitor
	 * to use. Should be an {@link Integer}. Use -1 to indicate the primary monitor
	 * (the default)
	 * 
	 * Deprecated. Use {@link BasicToastHint#MONITOR}.
	 */
	@Deprecated
	public final static String MONITOR = BasicToastHint.MONITOR.toLegacyKey();

	public static class Service implements ToasterService {
		@Override
		public Toaster create(ToasterSettings settings) {
			return new SWTToaster(settings);
		}
	}

	private Display display;
	private Shell hidden;
	private PopupWindow slice;

	/**
	 * Constructor
	 * 
	 * @param configuration configuration
	 */
	public SWTToaster(ToasterSettings configuration) {
		super(configuration);
		try {
			display = Display.getDefault();
			capabilities.addAll(
					Arrays.asList(Capability.ACTIONS, Capability.CLOSE, Capability.DEFAULT_ACTION, Capability.IMAGES));
		} catch (Throwable cnfe) {
			throw new UnsupportedOperationException(getClass().getName() + " not supported.", cnfe);
		}
	}

	@Override
	public Slice toast(ToastBuilder builder) {
		var iconSize =  getHint(builder.hints(), BasicToastHint.ICON_SIZE, PopupWindow.ICON_SIZE);
		var imageSize =  getHint(builder.hints(), BasicToastHint.IMAGE_SIZE, PopupWindow.IMAGE_SIZE);
		var offset =  getHint(builder.hints(), BasicToastHint.OFFSET, PopupWindow.DEFAULT_OFFSET);
		var animated = (boolean) getHint(builder.hints(), BasicToastHint.ANIMATED, false);
		var idx =  getHint(builder.hints(), BasicToastHint.MONITOR, -1);
		var newSlice = new PopupWindow(display, builder, configuration, offset, iconSize, imageSize, animated, idx);
		display.asyncExec(() -> {
			if (hidden == null)
				hidden = new Shell(display);
			if (slice != null) {
				try {
					slice.close();
				} catch (IOException e) {
				}
			}
			newSlice.popup(hidden);
			slice = newSlice;
		});
		return newSlice;
	}

	public static class PopupWindow implements Slice {

		private static final int TEXT_WIDTH = 400;
		private static final int IMAGE_SIZE = 128;
		private static final int ICON_SIZE = 24;
		private static final int DEFAULT_OFFSET = 64;
		private static final int SPACING = 8;
		private static final int ANIMATION_TIME = 250;

		private Shell shell;
		private Label titleLabel;
		private Label contentLabel;
		private Label iconLabel;
		private ToasterSettings settings;
		private Display display;
		private long animStarted;
		private Point endPosition;
		private Point startPosition;
		private Thread timerThread;
		private Thread swtThread;
		private ToastAction defaultAction;
		private final String icon;
		private final ToastType type;
		private final String image;
		private final String title;
		private final String content;
		private final List<ToastAction> actions;
		private final ToastActionListener closed;
		private final int timeout;
		private final int offset;
		private final int iconSize;
		private final int imageSize;
		private final boolean animated;
		private final int monitor;

		public PopupWindow(Display display, ToastBuilder builder, ToasterSettings settings, int offset, int iconSize, int imageSize, boolean animated, int monitor) {
			this.settings = settings;
			this.display = display;
			this.offset = offset;
			this.iconSize = iconSize;
			this.imageSize = imageSize;
			this.animated = animated;
			this.monitor = monitor;
			
			defaultAction = builder.defaultAction();
			icon = builder.icon();
			type = builder.type();
			title = builder.title();
			image = builder.image();
			content = builder.content();
			actions = Collections.unmodifiableList(builder.actions());
			closed = builder.closed();
			timeout = builder.timeout();
			
		}

		public void popup(Shell hidden) {
			swtThread = Thread.currentThread();
			var defaultListener = new MouseListener() {
				@Override
				public void mouseUp(MouseEvent e) {
					if (defaultAction != null && defaultAction.listener() != null)
						defaultAction.listener().action();
				}

				@Override
				public void mouseDown(MouseEvent e) {
				}

				@Override
				public void mouseDoubleClick(MouseEvent e) {
				}
			};

			shell = new Shell(hidden, SWT.ON_TOP);

			var topRow = new Composite(shell, SWT.NONE);
			var topLayout = new GridLayout();
			topLayout.numColumns = 3;
			topRow.setLayout(topLayout);
			topRow.addMouseListener(defaultListener);


			if (icon != null) {
				iconLabel = new Label(topRow, SWT.NONE);
				var data = new GridData();
				data.widthHint = 24;
				data.heightHint = 24;
				iconLabel.setLayoutData(data);
				
				Image image = null;
				try {
					var u = new URL(icon);
					try(var in = u.openStream()) {
						image = new Image(display, in);
					}
				}
				catch(Exception e) {
					image = new Image(display, icon);
				}

				if(iconSize > 0)
					image = proportionalImage(iconSize, image);
				var fImage = image;
				shell.addDisposeListener((e) -> fImage.dispose());
				iconLabel.setImage(image);
				data.widthHint = image.getImageData().width;
				data.heightHint = image.getImageData().height;
				iconLabel.pack();
			}
			else if (type != ToastType.NONE) {
				iconLabel = new Label(topRow, SWT.NONE);
				var data = new GridData();
				data.widthHint = 24;
				data.heightHint = 24;
				iconLabel.setLayoutData(data);
				var img = getTypeImage(type);
				if(iconSize > 0)
					img  = getScaledImage(img, iconSize);
				var fImage = img;
				shell.addDisposeListener((e) -> fImage.dispose());
				data.widthHint = img.getImageData().width;
				data.heightHint = img.getImageData().height;
				iconLabel.setImage(img);
				iconLabel.pack();
			}

			if (title != null) {
				titleLabel = new Label(topRow, SWT.NONE);
				var data = new GridData();
				data.grabExcessHorizontalSpace = true;
				data.grabExcessVerticalSpace = true;
				titleLabel.setLayoutData(data);
				titleLabel.setText(title);
				titleLabel.setFont(createDerivedFont(titleLabel, 16));
				titleLabel.pack();
			}

			var close = new Button(topRow, SWT.NONE);
			var data = new GridData();
			data.widthHint = 24;
			data.heightHint = 24;
			close.setLayoutData(data);
			close.setImage(createCloseImage(display, display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND),
					display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND)));
			close.addListener(SWT.Selection, e -> {
				shell.dispose();
			});

			int imageSpace = 0;

			var contentPane = new Composite(shell, SWT.NONE);
			var contentPaneLayout = new RowLayout(SWT.HORIZONTAL);
			contentPaneLayout.spacing = SPACING;
			contentPane.setLayout(contentPaneLayout);

			if (image != null) {
				var imageLabel = new Label(contentPane, SWT.NONE);
				Image imageObj = null;
				try {
					var u = new URL(image);
					try(var in = u.openStream()) {
						imageObj = new Image(display, in);
					}
				}
				catch(Exception e) {
					imageObj = new Image(display, image);
				}
				
				if(imageSize > 0)
					imageObj = proportionalImage(imageSize, imageObj);
				imageLabel.setImage(imageObj);
				var imageData = new RowData(imageObj.getImageData().width, imageObj.getImageData().height);
				imageLabel.setLayoutData(imageData);
				imageLabel.pack();
				imageSpace = imageObj.getImageData().width;
			}

			int textWidth = image == null ? TEXT_WIDTH : TEXT_WIDTH - imageSpace;

			if (content != null) {
				contentLabel = new Label(contentPane, SWT.WRAP);
				contentLabel.setText(content);
				var contentData = new RowData();
				contentData.width = textWidth;
				contentLabel.setLayoutData(contentData);
				contentLabel.pack();
				contentLabel.addListener(SWT.Modify, event -> {
					int currentHeight = contentLabel.getSize().y;
					int preferredHeight = contentLabel.computeSize(textWidth, SWT.DEFAULT).y;
					if (currentHeight != preferredHeight) {
						contentData.height = preferredHeight;
						contentLabel.pack();
						shell.pack();
						shell.setLocation(to());
					}
				});
				contentLabel.addMouseListener(defaultListener);
			}

			if (!actions.isEmpty()) {
				var actionsWidget = new Composite(shell, SWT.NONE);
				var actionsRow = new RowLayout(SWT.HORIZONTAL);
				actionsRow.spacing = SPACING;
				actionsRow.wrap = false;
				actionsRow.fill = true;
				actionsRow.justify = false;
				actionsWidget.setLayout(actionsRow);
				for (var action : actions) {
					var actionButton = new Button(actionsWidget, SWT.NONE);
					actionButton.setText(action.displayName());
					if (action.listener() == null)
						actionButton.setGrayed(true);
					else
						actionButton.addListener(SWT.Selection, e -> {
							action.listener().action();
						});
				}
			}
			shell.addMouseListener(defaultListener);

			if (closed != null) {
				shell.addDisposeListener((e) -> {
					closed.action();
				});
			}

			var mainRow = new RowLayout(SWT.VERTICAL);
			mainRow.wrap = false;
			mainRow.fill = true;
			mainRow.justify = false;
			mainRow.spacing = SPACING;
			mainRow.marginTop = mainRow.marginBottom = mainRow.marginLeft = mainRow.marginRight = SPACING;
			shell.setLayout(mainRow);
			shell.pack();

			show();
			startTimer();
		}
		
		private Image proportionalImage(int size, Image image) {
			var imageWidth = image.getBounds().width;
			var imageHeight = image.getBounds().height;
			var imax = Math.max(imageWidth, imageHeight);
			var newImageWidth = imageWidth;
			var newImageHeight = imageHeight;
			if (imax > size) {
				var r = (float) size / (float) imax;
				newImageWidth = (int) ((float) imageWidth * r);
				newImageHeight = (int) ((float) imageHeight * r);
			}
			if (newImageWidth != imageWidth || newImageHeight != imageHeight) {
				var newImage = getScaledImage(image, newImageWidth, newImageHeight);
				image.dispose();
				image = newImage;
			}
			return image;
		}

		private void startTimer() {
			if (timeout == 0)
				return;
			timerThread = new Thread() {
				public void run() {
					try {
						Thread.sleep((timeout == -1 ? settings.getTimeout() : timeout) * 1000);
						try {
							close();
						} catch (IOException e) {
						}
					} catch (InterruptedException ie) {
					}
				}
			};
			timerThread.start();
		}

		private void show() {
			if (animated) {
				animStarted = System.currentTimeMillis();
				startPosition = from();
				shell.setLocation(startPosition);
				endPosition = to();
				animCycle();
				shell.open();
			} else {
				try {
					shell.setLocation(to());
					shell.open();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private void animCycle() {
			var now = System.currentTimeMillis();
			var time = now - animStarted;
			var progress = Math.min(1f, (double) time / (double) ANIMATION_TIME);
			var ax = startPosition.x + (int) (((float) endPosition.x - (float) startPosition.x) * progress);
			var ay = startPosition.y + (int) (((float) endPosition.y - (float) startPosition.y) * progress);
			shell.setLocation(ax, ay);
			if (progress < 1) {
				display.asyncExec(() -> animCycle());
			}
		}

		private Point from() {
			var bounds = getMonitorBounds();
			var sz = shell.getSize();
			switch (calcPos()) {
			case TR:
				return new Point(bounds.x + bounds.width - sz.x - offset, -sz.y);
			case TL:
				return new Point(bounds.x + offset, -sz.y);
			case CL:
				return new Point(-sz.x, bounds.y + ((bounds.height - (offset * 2) - sz.y) / 2));
			case CR:
				return new Point(bounds.x + bounds.width + sz.x,
						bounds.y + ((bounds.height - (offset * 2) - sz.y) / 2));
			case C:
			case T:
				return new Point(bounds.x + ((bounds.width - (offset * 2) - sz.x) / 2), -sz.y);
			case B:
				return new Point(bounds.x + ((bounds.width - (offset * 2) - sz.x) / 2),
						bounds.y + bounds.height + sz.y);
			case BL:
				return new Point(bounds.x + offset, bounds.y + bounds.height + sz.y);
			default: // BR
				return new Point(bounds.x + bounds.width - sz.x - offset, bounds.y + bounds.height + sz.y);
			}
		}

		private Rectangle getMonitorBounds() {
			var mon = display.getPrimaryMonitor();
			if (monitor != -1 && monitor < display.getMonitors().length) {
				mon = display.getMonitors()[monitor];
			}
			return mon.getBounds();
		}

		private Point to() {
			var bounds = getMonitorBounds();
			var sz = shell.getSize();
			switch (calcPos()) {
			case TR:
				return new Point(bounds.x + bounds.width - sz.x - offset, bounds.y + offset);
			case TL:
				return new Point(bounds.x + offset, bounds.y + offset);
			case CL:
				return new Point(bounds.x + offset, bounds.y + ((bounds.height - (offset * 2) - sz.y) / 2));
			case C:
				return new Point(bounds.x + ((bounds.width - (offset * 2) - sz.x) / 2),
						bounds.y + ((bounds.height - (offset * 2) - sz.y) / 2));
			case CR:
				return new Point(bounds.x + bounds.width - sz.x - offset,
						bounds.y + ((bounds.height - (offset * 2) - sz.y) / 2));
			case T:
				return new Point(bounds.x + ((bounds.width - (offset * 2) - sz.x) / 2), bounds.y + offset);
			case B:
				return new Point(bounds.x + ((bounds.width - (offset * 2) - sz.x) / 2),
						bounds.y + bounds.height - sz.y - offset);
			case BL:
				return new Point(bounds.x + offset, bounds.y + bounds.height - sz.y - offset);
			default: // BR
				return new Point(bounds.x + bounds.width - sz.x - offset, bounds.y + bounds.height - sz.y - offset);
			}
		}

		private Position calcPos() {
			var pos = settings.getPosition();
			if (pos == null) {
				if (System.getProperty("os.name", "").indexOf("Mac OS X") > -1) {
					return Position.TR;
				} else
					return Position.BR;
			}
			return pos;
		}

		private Image getTypeImage(ToastType type) {
			var d = Display.getDefault();
			switch (type) {
			case ERROR:
				return d.getSystemImage(SWT.ICON_ERROR);
			case WARNING:
				return d.getSystemImage(SWT.ICON_WARNING);
			case INFO:
				return d.getSystemImage(SWT.ICON_INFORMATION);
			default:
				return null;
			}
		}

		static Font createDerivedFont(Label base, int newSize) {
			FontData[] fontData = base.getFont().getFontData();
			for (int i = 0; i < fontData.length; ++i)
				fontData[i].setHeight(14);

			final Font newFont = new Font(base.getDisplay(), fontData);
			base.addDisposeListener((e) -> newFont.dispose());
			return newFont;
		}

		static Image getScaledImage(Image image, int sz) {
			return getScaledImage(image, sz, sz);
		}

		static Image getScaledImage(Image img, int sx, int sy) {
		    final Rectangle origBounds = img.getBounds();     
		    if (origBounds.width == sx && origBounds.height == sy) {     
		        return img;     
		    }     

		    final ImageData origData = img.getImageData();     
		    final ImageData destData = new ImageData(sx, sy, origData.depth, origData.palette);     
		    if (origData.alphaData != null) {     
		        destData.alphaData = new byte[destData.width * destData.height];     
		        for (int destRow = 0; destRow < destData.height; destRow++) {     
		            for (int destCol = 0; destCol < destData.width; destCol++) {     
		                final int origRow = destRow * origData.height / destData.height;     
		                final int origCol = destCol * origData.width / destData.width;     
		                final int o = origRow * origData.width + origCol;     
		                final int d = destRow * destData.width + destCol;     
		                destData.alphaData[d] = origData.alphaData[o];     
		            }     
		        }     
		    }     

		    final Image dest = new Image(img.getDevice(), destData);     

		    final GC gc = new GC(dest);     
		    gc.setAntialias(SWT.ON);     
		    gc.setInterpolation(SWT.HIGH);     
		    gc.drawImage(img, 0, 0, origBounds.width, origBounds.height, 0, 0, sx, sy);     
		    gc.dispose();

		    return dest;
		}

		private static final Image createCloseImage(Display display, Color bg, Color fg) {
			final int size = 11, off = 1;
			final Image image = new Image(display, size, size);
			final GC gc = new GC(image);
			gc.setBackground(bg);
			gc.fillRectangle(image.getBounds());
			gc.setForeground(fg);
			gc.drawLine(0 + off, 0 + off, size - 1 - off, size - 1 - off);
			gc.drawLine(1 + off, 0 + off, size - 1 - off, size - 2 - off);
			gc.drawLine(0 + off, 1 + off, size - 2 - off, size - 1 - off);
			gc.drawLine(size - 1 - off, 0 + off, 0 + off, size - 1 - off);
			gc.drawLine(size - 1 - off, 1 + off, 1 + off, size - 1 - off);
			gc.drawLine(size - 2 - off, 0 + off, 0 + off, size - 2 - off);
			gc.dispose();
			return image;
		}

		@Override
		public void close() throws IOException {
			if (timerThread != null && timerThread != Thread.currentThread()) {
				timerThread.interrupt();
				timerThread = null;
			}
			if (swtThread == null)
				return;
			if (Thread.currentThread() != swtThread)
				display.asyncExec(() -> doClose());
			else
				doClose();
		}

		private void doClose() {
			if (!shell.isDisposed())
				shell.dispose();
		}
	}
}
