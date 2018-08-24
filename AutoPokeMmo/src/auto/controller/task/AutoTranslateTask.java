package auto.controller.task;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.json.JSONArray;

import com.sun.jna.platform.WindowUtils;

import Utils.ReceourceManage;
import Utils.WordDiscernUtil;
import auto.controller.AutoTask;
import auto.controller.AutoTaskManger;
import auto.view.ScreenRectCatchFrame;

public class AutoTranslateTask extends JFrame implements AutoTask {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea translateScreen;
	private Rectangle targetRectangle = AutoTaskManger.getInstance()
			.getTargetRectangle();

	public AutoTranslateTask() {
		int width = targetRectangle.width;
		int heigh = targetRectangle.height / 3;

		translateScreen = new JTextArea("测试");
		translateScreen.setOpaque(false);
		translateScreen.setSize(width, heigh);

		JButton OKBtn = new JButton("翻译");
		OKBtn.addActionListener(e -> doAutoTask());
		JButton closeBtn = new JButton("关闭");
		closeBtn.addActionListener(e -> dispose());
		JPanel buttonJP = new JPanel();
		buttonJP.setOpaque(true);
		buttonJP.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonJP.add(OKBtn);
		buttonJP.add(closeBtn);
		if (Boolean.valueOf(ReceourceManage.getInstance().getProperty(
				"ui.isneedtransparent"))) {
			System.setProperty("sun.java2d.noddraw", "true");
			WindowUtils.setWindowAlpha(
					this,
					Float.valueOf(ReceourceManage.getInstance().getProperty(
							"ui.transparentPercent")));
		}

		// this.setUndecorated(true);
		this.setBounds(targetRectangle.x, targetRectangle.y
				+ targetRectangle.height, width, heigh + 42);
		this.setAlwaysOnTop(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.getContentPane().setLayout(new BorderLayout());
		this.add(translateScreen, BorderLayout.CENTER);
		this.add(buttonJP, BorderLayout.SOUTH);
		this.setVisible(true);
	}

	public void dispose() {
		translateScreen = null;
		this.getContentPane().removeAll();
		super.dispose();
	}

	private void doAutoTask() {

		JSONArray result = WordDiscernUtil.getInstance().general(
				ScreenRectCatchFrame.getInstance().getTargetImageByte());
	}

	@Override
	public void executeTask(byte[] image) {
		// TODO Auto-generated method stub
		JSONArray result = WordDiscernUtil.getInstance().general(
				image);
	}

}
