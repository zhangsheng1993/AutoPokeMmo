package auto.view;

import static com.sun.jna.platform.win32.WinUser.GW_HWNDFIRST;
import static com.sun.jna.platform.win32.WinUser.GW_HWNDNEXT;
import static com.sun.jna.platform.win32.WinUser.WS_DISABLED;
import static com.sun.jna.platform.win32.WinUser.WS_MINIMIZE;
import static com.sun.jna.platform.win32.WinUser.WS_VISIBLE;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import auto.controller.AutoTaskManger;
import auto.controller.task.AutoTranslateTask;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser.WINDOWINFO;

/**
 * 返回所选框体的起始坐标和长宽，有自动识别框体的功能
 * 
 * @author Administrator
 *
 */
public class ScreenRectCatchFrame {
	private static ScreenRectCatchFrame instance = null;
	private HashMap<Integer, Rectangle> winLayer = null;
	private Dimension screenSize = null;
	private Robot rb = null;
	private ImageIcon img = null;
	private JFrame jf;
	private Cursor myCursor;
	private myContentPane jp;
	private Rectangle targetRectangle = null;
	private Point start = new Point(0, 0), end = new Point(0, 0);
	private Point prePos, startCopy, endCopy;
	private ByteArrayOutputStream out = new ByteArrayOutputStream();
	private volatile boolean isProcess = false;// 第一次拖拽完成后，需要进行处理，这时候需要重新利用click,drag和release函数。
	private AutoTaskManger taskManger;
	private AutoMenuFrame parent;
	public static ScreenRectCatchFrame getInstance() {
		if (null == instance) {
			instance = new ScreenRectCatchFrame();
		}
		return instance;
	}

	private ScreenRectCatchFrame() {
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		taskManger=AutoTaskManger.getInstance();
		try {
			rb = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	};

	/** 屏幕窗体层次保存 **/
	private void saveLayer() {
		User32 u32 = User32.INSTANCE;
		printAllNextWin(u32.GetWindow(u32.GetForegroundWindow(),
				new WinDef.DWORD(GW_HWNDFIRST)));
	}

	/** 遍历桌面所有窗体，如果是合法的就加入到winLayer **/
	private void printAllNextWin(HWND h) {
		HashMap<Integer, Rectangle> layerInfo = new HashMap<>();// 0是最高层
		HWND hWnd = h;
		RECT r = new RECT();
		int layerPos = 0;
		Rectangle rect;
		while ((hWnd = getNextWindow(hWnd)) != null) {
			User32.INSTANCE.GetWindowRect(hWnd, r);
			rect = r.toRectangle();
			if (!CheckLegal(hWnd, rect))
				continue;
			layerInfo.put(layerPos++, rect);
		}
		winLayer = layerInfo;
	}

	private HWND getNextWindow(HWND hWnd) {
		return User32.INSTANCE.GetWindow(hWnd, new DWORD(GW_HWNDNEXT));
	}

	private boolean CheckLegal(HWND hWnd, Rectangle rt) {
		WINDOWINFO winInfo = new WINDOWINFO();
		User32.INSTANCE.GetWindowInfo(hWnd, winInfo);
		int style = winInfo.dwStyle;
		if ((style & WS_VISIBLE) == 0) {
			return false;// 去掉不可视窗口
		}
		if ((style & WS_DISABLED) != 0) {
			return false;// 去掉disable窗口
		}
		if (rt.width < 10 || rt.height < 10) {
			return false;// 去掉小不点窗口
		}
		if ((style & WS_MINIMIZE) != 0) {
			return false;// 去掉最小化状态的窗口
		}
		if ((rt.getWidth() == screenSize.getWidth())
				&& (rt.getHeight() == screenSize.getHeight())) {
			return false;// 去掉底层屏幕窗口
		}
		return true;
	}

	private void shotProcess() {
		jf = new JFrame();
		jf.setUndecorated(true);
		jf.setBounds(0, 0, screenSize.width, screenSize.height);
		jf.setAlwaysOnTop(true);
		jp = new myContentPane();
		jp.setOpaque(false);
		jp.setLayout(null);
		BufferedImage bi = rb.createScreenCapture(new Rectangle(screenSize));
		drawMouse(bi);
		img = new ImageIcon(bi);
		mouseEvent e = new mouseEvent();
		jp.addMouseListener(e);
		jp.addMouseMotionListener(e);
		jf.add(jp);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setVisible(true);
		myCursor = getNewCursor();
		jf.setCursor(myCursor);// 给系统设置彩色光标。
	}



	public byte[] getTargetImageByte() {
		try {
			ImageIO.write(rb.createScreenCapture(targetRectangle), "png", out);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] bt = out.toByteArray();
		
		return bt;
	}

	/**
	 * 将鼠标绘制成彩色
	 * 
	 * @return
	 */
	private Cursor getNewCursor() {
		return Toolkit
				.getDefaultToolkit()
				.createCustomCursor(
						new ImageIcon(ScreenRectCatchFrame.class
								.getResource("../../resource/cursor.png"))
								.getImage(),
						new Point(0, 0), "myCursor");
	}

	/** 图像添加鼠标:http://blog.csdn.net/eguid_1/article/details/52973508 **/
	private void drawMouse(BufferedImage bi) {
		Graphics2D g2d = (Graphics2D) bi.getGraphics();
		g2d.drawImage(bi, 0, 0, bi.getWidth(), bi.getHeight(), null);
		ImageIcon img = new ImageIcon(
				ScreenRectCatchFrame.class
						.getResource("../../resource/whiteCursor.png"));
		Point mp = MouseInfo.getPointerInfo().getLocation();
		g2d.drawImage(img.getImage(), mp.x, mp.y, img.getIconWidth(),
				img.getIconHeight(), null);
		g2d.dispose();
	}

	private void clean() {
		jf.dispose();
		isProcess = false;
		start = new Point(0, 0);
		end = new Point(0, 0);
	}
	
	AWTEventListener al;

	private void registerESC() {
		al = event -> {
			KeyEvent ke = (KeyEvent) event;
			if (ke.getID() == KeyEvent.KEY_PRESSED) {
				if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
					clean();
					unregisterESC();
					jp.setDrag(false);
				}
			}
		};
		Toolkit.getDefaultToolkit().addAWTEventListener(al,
				AWTEvent.KEY_EVENT_MASK);
	}

	private void unregisterESC() {
		Toolkit.getDefaultToolkit().removeAWTEventListener(al);
	}
	private class myContentPane extends JPanel {

		private static final long serialVersionUID = 1L;
		Point myStart = new Point(0, 0);
		public volatile boolean dragFlag = false;
		public volatile boolean moveFlag = false;

		public synchronized boolean isMove() {
			return moveFlag;
		}

		public synchronized void setMove(boolean f) {
			moveFlag = f;
		}

		public synchronized boolean isDrag() {
			return dragFlag;
		}

		public synchronized void setDrag(boolean f) {
			dragFlag = f;
		}

		@Override
		protected void paintComponent(Graphics g) {
			/**
			 * 设置透明度 https://wenku.baidu.com/view/d90f110d227916888486d7ee.html
			 */
			if (img != null)
				g.drawImage(img.getImage(), 0, 0, getWidth(), getHeight(), this);
			AlphaComposite ac = AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, 0.5f);
			Composite old = ((Graphics2D) g).getComposite();
			((Graphics2D) g).setComposite(ac);
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, screenSize.width, screenSize.height);
			((Graphics2D) g).setComposite(old);

			g.drawImage(img.getImage(), start.x, start.y, end.x, end.y,
					start.x, start.y, end.x, end.y, this);
			g.setColor(new Color(2, 169, 255));
			if (isMove()) {
				Stroke oldS = ((Graphics2D) g).getStroke();
				((Graphics2D) g).setStroke(new BasicStroke(4.0f));
				g.drawRect(start.x, start.y, end.x - start.x, end.y - start.y);
				((Graphics2D) g).setStroke(oldS);
				setMove(false);
			} else {
				drawBound(g);
			}
			super.paintComponent(g);
		}

		private void correctMyStart() {
			if ((start.x <= end.x) && (start.y <= end.y)) {
				myStart.x = start.x;
				myStart.y = start.y;
			} else if ((start.x <= end.x) && (start.y > end.y)) {
				// 右上角
				myStart.x = start.x;
				myStart.y = end.y;
			} else if ((start.x > end.x) && (start.y <= end.y)) {
				// 左下角
				myStart.x = end.x;
				myStart.y = start.y;
			} else {
				myStart.x = end.x;
				myStart.y = end.y;
			}
		}

		int offset = 3;
		int[][] point;

		private void drawBound(Graphics g) {
			myStart = new Point(0, 0);
			/** 纠正反方向错误 **/
			correctMyStart();
			g.drawRect(myStart.x, myStart.y, Math.abs(end.x - start.x),
					Math.abs(end.y - start.y));
			point = new int[][] { { start.x, start.y },
					{ (start.x + end.x) / 2, start.y }, { end.x, start.y },
					{ end.x, (start.y + end.y) / 2 }, { end.x, end.y },
					{ (start.x + end.x) / 2, end.y }, { start.x, end.y },
					{ start.x, (start.y + end.y) / 2 } };
			for (int i = 0; i < point.length; i++)
				g.fillRect(point[i][0] - offset, point[i][1] - offset,
						2 * offset, 2 * offset);
		}
	}

	private class mouseEvent extends MouseAdapter {
		/** 拖拽参数 **/
		private static final int BREADTH = 1;// 边界拉伸范围
		private static final int BREADTH2 = 1;// 边界拉伸范围
		private int dragType;
		private static final int DRAG_NONE = 0;
		private static final int DRAG_MOVE = 1;
		private static final int DRAG_UP = 2;
		private static final int DRAG_UPLEFT = 3;
		private static final int DRAG_UPRIGHT = 4;
		private static final int DRAG_LEFT = 5;
		private static final int DRAG_RIGHT = 6;
		private static final int DRAG_BOTTOM = 7;
		private static final int DRAG_BOTTOMLEFT = 8;
		private static final int DRAG_BOTTOMRIGHT = 9;

		private void typeSel(MouseEvent e) {
			Point p = e.getPoint();
			if (new Rectangle(start.x - BREADTH, start.y - BREADTH, end.x
					- start.x + BREADTH2, end.y - start.y + BREADTH2)
					.contains(p)) {
				/** 在区域内部 **/
			} else {
				/** 如果不在内部，就结束 **/
				isProcess = false;
				pressPro(e);
			}
		}

		public void mousePressed(MouseEvent e) {
			if (isProcess) {
				/**
				 * 在处理状态下press时，记录press的点，作为起点。
				 */
				prePos = e.getPoint();
				startCopy = new Point(start);
				endCopy = new Point(end);
				typeSel(e);
			} else {
				pressPro(e);
			}
		}

		private void pressPro(MouseEvent e) {
			isSelExist();
			start = e.getPoint();
			end = new Point(start.x, start.y);
			jp.setDrag(true);
			jp.updateUI();
		}

		public void mouseReleased(MouseEvent e) {
			if (isProcess) {
				jf.setCursor(myCursor);
				/** 修正在反方向拖动时的区域修正及更新面板 **/
				correctDir();
				reLocateSel();
				return;
			} else {
				releaseProcess(e);
			}
		}

		private void releaseProcess(MouseEvent e) {
			/**
			 * 如果只有点击没有拖拽，就进行窗体检测。
			 **/
			if (!isDrag) {
				Rectangle r;
				for (int i = 0; i < winLayer.size(); i++) {
					r = winLayer.get(i);
					if (r.contains(e.getPoint())) {
						// System.out.println("当前在矩形"+i+"中");
						start.x = r.getX() >= 0 ? (int) r.getX() : 0;
						start.y = r.getY() >= 0 ? (int) r.getY() : 0;
						end.x = start.x + (int) r.getWidth();
						end.y = start.y + (int) r.getHeight();
						jp.updateUI();// 这个必须要。
						break;
					}
				}
			}
			correctDir();
			isSelExist();
			createSel();
			isDrag = false;
			isProcess = true;
			/**
			 * 进入处理状态，如果在指定区域内再次点击，就是拖拽拉伸。
			 */
		}

		private void reLocateSel() {
			Point refP;
			// 判断按钮会不会超出屏幕
			if ((end.y + 30) >= screenSize.getHeight()) {
				refP = new Point(end.x, start.y - 30);
			} else {
				refP = new Point(end.x, end.y);
			}
			selPanel.setBounds(refP.x - 250, refP.y + 10, 250, 40);
			selPanel.updateUI();
		}

		private void createSel() {
			Point refP;
			// 判断按钮会不会超出屏幕,上下左右方位都应该判断。
			/*** 还不完善，左下角会挡住。 ***/
			if ((end.y + 30) >= screenSize.getHeight()) {
				refP = new Point(end.x, start.y - 30);
			} else {
				refP = new Point(end.x, end.y);
			}
			selPanel = new JPanel();
			selPanel.setOpaque(false);
			selPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			selPanel.setBounds(refP.x - 250, refP.y + 10, 250, 40);
			ok = new JButton("确定");
			ok.addActionListener(e -> {
				targetRectangle=new Rectangle(start.x,start.y,end.x-start.x,end.y-start.y);
				taskManger.setTargetRectangle(targetRectangle);
				jp.setDrag(false);
				if(parent.needTranslate()){
					new AutoTranslateTask();
				}
				doTaskExecute();
				clean();
			});
			cancel = new JButton("重新截图");
			cancel.addActionListener(e -> {
				shotProcess();
				jp.setDrag(false);
			});
			save = new JButton("退出");
			save.addActionListener(e -> {
				System.exit(0);
			});
			selPanel.add(ok);
			selPanel.add(cancel);
			selPanel.add(save);
			jp.add(selPanel);
			jp.updateUI();
		}

		private void isSelExist() {
			jp.removeAll();
			jp.updateUI();
		}

		JPanel selPanel;
		JButton ok = null;
		JButton cancel = null;
		JButton save = null;
		public volatile boolean isDrag = false;

		public void mouseDragged(MouseEvent e) {
			if (isProcess) {
				/**
				 * 判断拖拽拉伸类型，然后处理。
				 */
				dragPro(e);
			} else {
				/** 获取的新的拖拽点必须加工后才能送到绘图板。 **/
				end = e.getPoint();
				jp.updateUI();// 这个必须要。
				isDrag = true;
			}
		}

		private void dragPro(MouseEvent e) {
			Point curPos = e.getPoint();
			switch (dragType) {
			case DRAG_MOVE:
				start.x = startCopy.x + curPos.x - prePos.x;
				start.y = startCopy.y + curPos.y - prePos.y;
				end.x = endCopy.x + curPos.x - prePos.x;
				end.y = endCopy.y + curPos.y - prePos.y;
				break;
			case DRAG_UPLEFT:
				/** start更新为拖动点 **/
				start = new Point(curPos);
				break;
			case DRAG_UP:
				/** 只需要更新start.y **/
				start.y = curPos.y;
				break;
			case DRAG_UPRIGHT:
				/** 只需要更新start.y和end.x **/
				start.y = curPos.y;
				end.x = curPos.x;
				break;
			case DRAG_RIGHT:
				end.x = curPos.x;
				break;
			case DRAG_BOTTOMRIGHT:
				end = new Point(curPos);
				break;
			case DRAG_BOTTOM:
				end.y = curPos.y;
				break;
			case DRAG_BOTTOMLEFT:
				start.x = curPos.x;
				end.y = curPos.y;
				break;
			case DRAG_LEFT:
				start.x = curPos.x;
				break;
			default:
			}
			reLocateSel();
			jp.updateUI();
		}

		/**
		 * 纠正方向使得start始终是矩形左上角，stop始终是矩形右下角。
		 **/
		private void correctDir() {
			Point p;
			// 一共四个方向。
			// 右下角
			if ((start.x <= end.x) && (start.y <= end.y)) {
				return;
			} else if ((start.x <= end.x) && (start.y > end.y)) {
				// 右上角
				p = new Point(start);
				start.y = end.y;
				end.y = p.y;
			} else if ((start.x > end.x) && (start.y <= end.y)) {
				// 左下角
				p = new Point(end);
				end.y = start.y;
				start.y = p.y;
				swap();
			} else {
				swap();
			}
		}

		private void swap() {
			Point p = start;
			start = end;
			end = p;
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			/** 鼠标移动自动判断当前哪个矩形中 **/
			if (isProcess) {
				Point p = e.getPoint();
				if (new Rectangle(start.x - BREADTH, start.y - BREADTH,
						BREADTH2, BREADTH2).contains(p)) {
					/** stretch upper-left **/
					dragType = DRAG_UPLEFT;
					jf.setCursor(new Cursor(Cursor.NW_RESIZE_CURSOR));
				} else if (new Rectangle(start.x + BREADTH, start.y - BREADTH,
						end.x - start.x - BREADTH2, BREADTH2).contains(p)) {
					/** stretch upper **/
					dragType = DRAG_UP;
					jf.setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
				} else if (new Rectangle(end.x - BREADTH, start.y - BREADTH,
						BREADTH2, BREADTH2).contains(p)) {
					/** stretch upper-right **/
					dragType = DRAG_UPRIGHT;
					jf.setCursor(new Cursor(Cursor.NE_RESIZE_CURSOR));
				} else if (new Rectangle(end.x - BREADTH, start.y + BREADTH,
						BREADTH2, end.y - start.y - BREADTH2).contains(p)) {
					/** stretch right **/
					dragType = DRAG_RIGHT;
					jf.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
				} else if (new Rectangle(end.x - BREADTH, end.y - BREADTH,
						BREADTH2, BREADTH2).contains(p)) {
					/** stretch bottom-right **/
					dragType = DRAG_BOTTOMRIGHT;
					jf.setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
				} else if (new Rectangle(start.x + BREADTH, end.y - BREADTH,
						end.x - start.x - BREADTH2, BREADTH2).contains(p)) {
					/** stretch bottom **/
					dragType = DRAG_BOTTOM;
					jf.setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));
				} else if (new Rectangle(start.x - BREADTH, end.y - BREADTH,
						BREADTH2, BREADTH2).contains(p)) {
					/** stretch bottom-left **/
					dragType = DRAG_BOTTOMLEFT;
					jf.setCursor(new Cursor(Cursor.SW_RESIZE_CURSOR));
				} else if (new Rectangle(start.x - BREADTH, start.y + BREADTH,
						BREADTH2, end.y - start.y - BREADTH2).contains(p)) {
					/** stretch left **/
					dragType = DRAG_LEFT;
					jf.setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
				} else if (new Rectangle(start.x, start.y, end.x - start.x,
						end.y - start.y).contains(p)) {
					/** 如果在矩形内部，那么就是拖动 **/
					dragType = DRAG_MOVE;
					jf.setCursor(new Cursor(Cursor.MOVE_CURSOR));
				} else {
					dragType = DRAG_NONE;
					jf.setCursor(myCursor);
				}
				return;
			}
			if (jp.isDrag()) {
				return;
			}
			Rectangle r;
			for (int i = 0; i < winLayer.size(); i++) {
				r = winLayer.get(i);
				if (r.contains(e.getPoint())) {
					// System.out.println("当前在矩形"+i+"中");
					start.x = r.getX() >= 0 ? (int) r.getX() : 0;
					start.y = r.getY() >= 0 ? (int) r.getY() : 0;
					end.x = start.x + (int) r.getWidth();
					end.y = start.y + (int) r.getHeight();
					jp.setMove(true);
					jp.updateUI();// 这个必须要。
					return;// 无须往下搜索。
				}
			}
		}
	}

	public void positioning(AutoMenuFrame parent) {
		this.parent=parent;
		saveLayer();
		   registerESC();//注册窗体全局热键
		shotProcess();
	}

	public void doTaskExecute() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				taskManger.autoTaskExecut(getTargetImageByte());
			}
		}, 1000, 2000);
	}
		

}
