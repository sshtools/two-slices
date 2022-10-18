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
import java.net.URL;
import java.util.Arrays;

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
import com.sshtools.twoslices.Capability;
import com.sshtools.twoslices.Slice;
import com.sshtools.twoslices.ToastBuilder;
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
	 */
	public final static String OFFSET = "offset";

	/**
	 * Key for {@link ToasterSettings#getProperties()} hint for the maximum size of
	 * image content. Should be an {@link Integer}. Any size lower than this will be
	 * also be scaled up depending on {@link SWTToaster#SCALE_UP}. Use a size of
	 * zero to prevent scaling entirely.
	 */
	public final static String IMAGE_SIZE = "imageSize";

	/**
	 * Key for {@link ToasterSettings#getProperties()} hint for the maximum size of
	 * the icon. Should be an {@link Integer}. Any size lower than this will be
	 * also be scaled up depending on {@link SWTToaster#SCALE_UP}. Use a size of
	 * zero to prevent scaling entirely.
	 */
	public final static String ICON_SIZE = "iconSize";

	/**
	 * Key for {@link ToasterSettings#getProperties()} hint to use animated window
	 * positioning. This is experimental, as there are some issues.
	 */
	public final static String ANIMATED = "animated";

	/**
	 * Key for {@link ToasterSettings#getProperties()} hint to signal which monitor
	 * to use. Should be an {@link Integer}. Use -1 to indicate the primary monitor
	 * (the default)
	 */
	public final static String MONITOR = "monitor";

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
		var newSlice = new PopupWindow(display, builder, configuration);
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
		private Label title;
		private Label content;
		private Label icon;
		private ToasterSettings settings;
		private Display display;
		private long animStarted;
		private Point endPosition;
		private Point startPosition;
		private ToastBuilder builder;
		private Thread timerThread;
		private Thread swtThread;

		public PopupWindow(Display display, ToastBuilder builder, ToasterSettings settings) {
			this.settings = settings;
			this.display = display;
			this.builder = builder;
		}

		public void popup(Shell hidden) {
			swtThread = Thread.currentThread();
			var defaultListener = new MouseListener() {
				@Override
				public void mouseUp(MouseEvent e) {
					if (builder.defaultAction() != null && builder.defaultAction().listener() != null)
						builder.defaultAction().listener().action();
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


			if (builder.icon() != null) {
				icon = new Label(topRow, SWT.NONE);
				var data = new GridData();
				data.widthHint = 24;
				data.heightHint = 24;
				icon.setLayoutData(data);
				
				Image image = null;
				try {
					var u = new URL(builder.icon());
					try(var in = u.openStream()) {
						image = new Image(display, in);
					}
				}
				catch(Exception e) {
					image = new Image(display, builder.icon());
				}

				var iconSize = (Integer) settings.getProperties().getOrDefault(SWTToaster.ICON_SIZE, ICON_SIZE);
				if(iconSize > 0)
					image = proportionalImage(iconSize, image);
				var fImage = image;
				shell.addDisposeListener((e) -> fImage.dispose());
				icon.setImage(image);
				data.widthHint = image.getImageData().width;
				data.heightHint = image.getImageData().height;
				icon.pack();
			}
			else if (builder.type() != ToastType.NONE) {
				icon = new Label(topRow, SWT.NONE);
				var data = new GridData();
				data.widthHint = 24;
				data.heightHint = 24;
				icon.setLayoutData(data);
				var img = getTypeImage(builder.type());
				var iconSize = (Integer) settings.getProperties().getOrDefault(SWTToaster.ICON_SIZE, ICON_SIZE);
				if(iconSize > 0)
					img  = getScaledImage(img, iconSize);
				var fImage = img;
				shell.addDisposeListener((e) -> fImage.dispose());
				data.widthHint = img.getImageData().width;
				data.heightHint = img.getImageData().height;
				icon.setImage(img);
				icon.pack();
			}

			if (builder.title() != null) {
				title = new Label(topRow, SWT.NONE);
				var data = new GridData();
				data.grabExcessHorizontalSpace = true;
				data.grabExcessVerticalSpace = true;
				title.setLayoutData(data);
				title.setText(builder.title());
				title.setFont(createDerivedFont(title, 16));
				title.pack();
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

			if (builder.image() != null) {
				var imageLabel = new Label(contentPane, SWT.NONE);
				Image image = null;
				try {
					var u = new URL(builder.image());
					try(var in = u.openStream()) {
						image = new Image(display, in);
					}
				}
				catch(Exception e) {
					image = new Image(display, builder.image());
				}
				
				var imageSize = (Integer) settings.getProperties().getOrDefault(SWTToaster.IMAGE_SIZE, IMAGE_SIZE);
				if(imageSize > 0)
					image = proportionalImage(imageSize, image);
				imageLabel.setImage(image);
				var imageData = new RowData(image.getImageData().width, image.getImageData().height);
				imageLabel.setLayoutData(imageData);
				imageLabel.pack();
				imageSpace = image.getImageData().width;
			}

			int textWidth = builder.image() == null ? TEXT_WIDTH : TEXT_WIDTH - imageSpace;

			if (builder.content() != null) {
				content = new Label(contentPane, SWT.WRAP);
				content.setText(builder.content());
				var contentData = new RowData();
				contentData.width = textWidth;
				content.setLayoutData(contentData);
				content.pack();
				content.addListener(SWT.Modify, event -> {
					int currentHeight = content.getSize().y;
					int preferredHeight = content.computeSize(textWidth, SWT.DEFAULT).y;
					if (currentHeight != preferredHeight) {
						contentData.height = preferredHeight;
						content.pack();
						shell.pack();
						shell.setLocation(to());
					}
				});
				content.addMouseListener(defaultListener);
			}

			if (!builder.actions().isEmpty()) {
				var actions = new Composite(shell, SWT.NONE);
				var actionsRow = new RowLayout(SWT.HORIZONTAL);
				actionsRow.spacing = SPACING;
				actionsRow.wrap = false;
				actionsRow.fill = true;
				actionsRow.justify = false;
				actions.setLayout(actionsRow);
				for (var action : builder.actions()) {
					var actionButton = new Button(actions, SWT.NONE);
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

			if (builder.closed() != null) {
				shell.addDisposeListener((e) -> {
					builder.closed().action();
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
			if (builder.timeout() == 0)
				return;
			timerThread = new Thread() {
				public void run() {
					try {
						Thread.sleep((builder.timeout() == -1 ? settings.getTimeout() : builder.timeout()) * 1000);
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
			if ((boolean) settings.getProperties().getOrDefault(SWTToaster.ANIMATED, false)) {
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
			var offset = (Integer) settings.getProperties().getOrDefault(SWTToaster.OFFSET, DEFAULT_OFFSET);
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
			var idx = (Integer) settings.getProperties().getOrDefault(SWTToaster.MONITOR, -1);
			var mon = display.getPrimaryMonitor();
			if (idx != -1 && idx < display.getMonitors().length) {
				mon = display.getMonitors()[idx];
			}
			return mon.getBounds();
		}

		private Point to() {
			var bounds = getMonitorBounds();
			var offset = (Integer) settings.getProperties().getOrDefault(SWTToaster.OFFSET, DEFAULT_OFFSET);
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
