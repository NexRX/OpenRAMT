package Controller;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * This class provides the static methods for hooking up the events for the ResizeListener class.
 * Use this to completely setup the resizing events.
 *
 * @author Nex
 */
public class UndecoratedResizable {

    /**
     * Automatically set min and max which then calls the manual version with the values needed. This function handles
     * aadding the listeners for resizing events for the undecorated window.
     * @param stage The stage which holds the application.
     * @param lc The LauncherController needed to automatically set parameters for the manual function and add the
     *           listeners.
     */
	public static void addResizeListener(Stage stage, LauncherController lc) {
		addResizeListener(stage, lc, 0, 0, Double.MAX_VALUE, Double.MAX_VALUE);
	}

    /**
     * The manual function in which you can assign any of the parameters manually. This function handles aadding the
     * listeners for resizing events for the undecorated window.
     * @param stage The stage which holds the application.
     * @param lc The LauncherController to apply the ResizeListeners too.
     * @param minWidth Minimum width in which the program can be resized to.
     * @param minHeight Minimum height in which the program can be resized to.
     * @param maxWidth Maximum width in which the program can be resized to.
     * @param maxHeight Maximum height in which the program can be resized to.
     */
	public static void addResizeListener(Stage stage, LauncherController lc, double minWidth, double minHeight, double maxWidth, double maxHeight) {
		ResizeListener resizeListener = new ResizeListener(stage);
		lc.addEventHandler(MouseEvent.MOUSE_MOVED, resizeListener);
		lc.addEventHandler(MouseEvent.MOUSE_PRESSED, resizeListener);
		lc.addEventHandler(MouseEvent.MOUSE_DRAGGED, resizeListener);
		lc.addEventHandler(MouseEvent.MOUSE_EXITED, resizeListener);
		lc.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, resizeListener);

		resizeListener.setMinWidth(minWidth);
		resizeListener.setMinHeight(minHeight);
		resizeListener.setMaxWidth(maxWidth);
		resizeListener.setMaxHeight(maxHeight);

		// Get all the children from our controller and resizes them all at once through the loop (i.e. topBar, stackPane).
		ObservableList<Node> children = lc.getChildrenUnmodifiable();
		System.out.println(children.toString());
		for (Node child : children) {
			addListenerDeeply(child, resizeListener, false);
		}
	}

    /**
     * Intermediate function that adds the EventHandler from the given listener to the for every mouse event needed.
     * This can be repeated recursively to the children of the given node when stated.
     *
     * @param node      The child/node to add the event handler for mouse events onto.
     * @param listener  The event handler for the mouse events.
     * @param recursive True to recursively add the event handlers to all sub-children or False just for the given node.
     */
	private static void addListenerDeeply(Node node, EventHandler<MouseEvent> listener, Boolean recursive) {
		node.addEventHandler(MouseEvent.MOUSE_MOVED, listener);
		node.addEventHandler(MouseEvent.MOUSE_PRESSED, listener);
		node.addEventHandler(MouseEvent.MOUSE_DRAGGED, listener);
		node.addEventHandler(MouseEvent.MOUSE_EXITED, listener);
		node.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, listener);
		if (node instanceof Parent && recursive) {
			Parent parent = (Parent) node;
			ObservableList<Node> children = parent.getChildrenUnmodifiable();
            System.out.println(children.toString());
			for (Node child : children) {
				addListenerDeeply(child, listener, true);
			}
		}
	}

    /**
     * When constructed will provide the functionality of actually handling the resize events needed for an undecorated
     * window in the handle function.
     */
	static class ResizeListener implements EventHandler<MouseEvent> {
		private final Stage stage;
		private Cursor cursorEvent = Cursor.DEFAULT;
        private double startX = 0;
		private double startY = 0;

		// Max and min sizes for controlled stage
		private double minWidth;
		private double maxWidth;
		private double minHeight;
		private double maxHeight;

		public ResizeListener(Stage stage) {
			this.stage = stage;
		}

		public void setMinWidth(double minWidth) {
			this.minWidth = minWidth;
		}

		public void setMaxWidth(double maxWidth) {
			this.maxWidth = maxWidth;
		}

		public void setMinHeight(double minHeight) {
			this.minHeight = minHeight;
		}

		public void setMaxHeight(double maxHeight) {
			this.maxHeight = maxHeight;
		}

		@Override
        public void handle(MouseEvent mouseEvent) {
            EventType<? extends MouseEvent> mouseEventType = mouseEvent.getEventType();
            Scene scene = stage.getScene();

            double mouseEventX = mouseEvent.getSceneX(),
                    mouseEventY = mouseEvent.getSceneY(),
                    sceneWidth = scene.getWidth(),
                    sceneHeight = scene.getHeight();

            int border = 4;
            if (MouseEvent.MOUSE_MOVED.equals(mouseEventType)) {
                if (mouseEventX < border +10 && mouseEventY > sceneHeight - border -10) {
                    cursorEvent = Cursor.SW_RESIZE;
                } else if (mouseEventX > sceneWidth - border -10 && mouseEventY > sceneHeight - border -10) {
                    cursorEvent = Cursor.SE_RESIZE;
                } else if (mouseEventX < border && mouseEventY > 26) {
                    cursorEvent = Cursor.W_RESIZE;
                } else if (mouseEventX > sceneWidth - border) {
                    cursorEvent = Cursor.E_RESIZE;
                } else if (mouseEventY > sceneHeight - border) {
                    cursorEvent = Cursor.S_RESIZE;
                } else {
                    cursorEvent = Cursor.DEFAULT;
                }
                scene.setCursor(cursorEvent);
            } else if (MouseEvent.MOUSE_EXITED.equals(mouseEventType) || MouseEvent.MOUSE_EXITED_TARGET.equals(mouseEventType)) {
                scene.setCursor(Cursor.DEFAULT);
            } else if (MouseEvent.MOUSE_PRESSED.equals(mouseEventType)) {
                startX = stage.getWidth() - mouseEventX;
                startY = stage.getHeight() - mouseEventY;
            } else if (MouseEvent.MOUSE_DRAGGED.equals(mouseEventType)) {
                if (!Cursor.DEFAULT.equals(cursorEvent)) {
                    if (!Cursor.W_RESIZE.equals(cursorEvent) && !Cursor.E_RESIZE.equals(cursorEvent)) {
                        double minHeight = stage.getMinHeight() > (border * 2) ? stage.getMinHeight() : (border * 2);
                        if (Cursor.NW_RESIZE.equals(cursorEvent) || Cursor.N_RESIZE.equals(cursorEvent)
                                || Cursor.NE_RESIZE.equals(cursorEvent)) {
                            if (stage.getHeight() > minHeight || mouseEventY < 0) {
                                setStageHeight(stage.getY() - mouseEvent.getScreenY() + stage.getHeight());
                                stage.setY(mouseEvent.getScreenY());
                            }
                        } else {
                            if (stage.getHeight() > minHeight || mouseEventY + startY - stage.getHeight() > 0) {
                                setStageHeight(mouseEventY + startY);
                            }
                        }
                    }

                    if (!Cursor.N_RESIZE.equals(cursorEvent) && !Cursor.S_RESIZE.equals(cursorEvent)) {
                        double minWidth = stage.getMinWidth() > (border * 2) ? stage.getMinWidth() : (border * 2);
                        if (Cursor.NW_RESIZE.equals(cursorEvent) || Cursor.W_RESIZE.equals(cursorEvent)
                                || Cursor.SW_RESIZE.equals(cursorEvent)) {
                            if (stage.getWidth() > minWidth || mouseEventX < 0) {
                                setStageWidth(stage.getX() - mouseEvent.getScreenX() + stage.getWidth());
                                stage.setX(mouseEvent.getScreenX());
                            }
                        } else {
                            if (stage.getWidth() > minWidth || mouseEventX + startX - stage.getWidth() > 0) {
                                setStageWidth(mouseEventX + startX);
                            }
                        }
                    }
                }

            }
        }

        private void setStageWidth(double width) {
            width = Math.min(width, maxWidth);
            width = Math.max(width, minWidth);
            stage.setWidth(width);
        }

        private void setStageHeight(double height) {
            height = Math.min(height, maxHeight);
            height = Math.max(height, minHeight);
            stage.setHeight(height);
        }
	}
}
