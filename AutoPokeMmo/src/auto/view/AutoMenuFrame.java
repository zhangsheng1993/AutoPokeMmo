package auto.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class AutoMenuFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	// JRadioButton fishBox;
	// JRadioButton hunterShinyPMbox;
	JTextField timesValueField;
	JRadioButton translateBtn;

	public AutoMenuFrame() {
		initUI();
	}

	private void initUI() {

		JLabel functionLab = new JLabel("功能项 :");

		translateBtn = new JRadioButton("翻译");
		ButtonGroup radioBtnGroup = new ButtonGroup();
		radioBtnGroup.add(translateBtn);

		JPanel buttonJP = new JPanel();
		buttonJP.setSize(300, 100);
		buttonJP.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
		buttonJP.add(functionLab);
		buttonJP.add(translateBtn);

		JButton OKBtn = new JButton("确认");
		OKBtn.addActionListener(e -> doAutoTask());

		JPanel buttonJP2 = new JPanel();
		buttonJP2.setSize(300, 50);
		buttonJP2.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonJP2.add(OKBtn);

		this.setSize(new Dimension(300, 150));
		this.setTitle("pokemmo翻译小助手         made by : xiongLaoShi");
		this.getContentPane().setLayout(new BorderLayout());
		this.add(buttonJP, BorderLayout.CENTER);
		this.add(buttonJP2, BorderLayout.SOUTH);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	public boolean needTranslate() {
		return translateBtn.isSelected();
	}

	private void doAutoTask() {
		this.setVisible(false);
		ScreenRectCatchFrame.getInstance().positioning(AutoMenuFrame.this);
		// TODO Auto-generated method stub
	}

}
