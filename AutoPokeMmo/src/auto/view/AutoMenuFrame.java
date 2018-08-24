package auto.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.json.JSONArray;

import Utils.ScreenRectCatch;
import Utils.WordDiscernUtil;

public class AutoMenuFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	JButton OKBtn;
	JRadioButton translateBtn;
	// JRadioButton fishBox;
	// JRadioButton hunterShinyPMbox;
	JTextField timesValueField;

	public AutoMenuFrame() {
		initUI();
	}

	private void initUI() {

		JLabel functionLab = new JLabel("功能项 :");
		functionLab.setBounds(50, 10, 50, 50);

		translateBtn = new JRadioButton("翻译");
		translateBtn.setBounds(120, 10, 70, 50);
		ButtonGroup radioBtnGroup = new ButtonGroup();
		radioBtnGroup.add(translateBtn);

		OKBtn = new JButton("确认");
		OKBtn.addActionListener(e -> doAutoTask());
		OKBtn.setBounds(220, 60, 60, 40);

		this.add(functionLab);
		this.add(translateBtn);
		this.add(OKBtn);

		this.setSize(new Dimension(300, 150));

		this.setLayout(null);
		this.setTitle("pokemmo翻译小助手         made by : xiongLaoShi");
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	private void doAutoTask() {
		// TODO Auto-generated method stub
		JSONArray resultlist=	WordDiscernUtil.getInstance().basicAccurateGeneral(	ScreenRectCatch.getInstance().getTargetImageByte());
	System.out.println(resultlist);
	}

}
