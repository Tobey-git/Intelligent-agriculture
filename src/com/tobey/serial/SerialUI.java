package com.tobey.serial;

import gnu.io.CommPortIdentifier;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
/**
 * ����ũҵС����
 * 
 * ��ʪ�����ʵʱ��ʾͨ����ʪ�ȴ�������õ���ʪ�ȼ�����ֵ
 * �¶ȡ�ʪ�ȡ����տ���������ڿ�����Ӧ�豸������/�ر�
 * 
 * ����״�����������ʾ�������Ƿ����ˣ����������ر���ˮ������������
 * ���򣬲����豸���п���
 * 
 * �趨�����ʪ�ȼ�����ֵ��壺���������ʱ��ϵͳ�����趨��ֵ��+-5��Χ�ڣ��Զ�ʵ��
 * ����Ӧ�豸�Ŀ��ƣ����������¶�Ϊ16����ʵ���¶�<11ʱ������Ƭ��������
 * ʵ���¶�>21ʱ���رռ���Ƭ��
 * 
 * ��ȡ������ʱ����������õ���ֵ�����������á�
 * 
 * @author Tobey-pc
 *
 */
class SerialUI extends JFrame{
	private static CommDao serial ;
	private String port = "COM1";
	private CommPortIdentifier cpid;
	private static boolean Set = false;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					System.gc();
					serial = new CommDao();
					SerialUI frame = new SerialUI();
					frame.setVisible(true);
					upDate();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	/**
	 * ��ʱ�����û������е�����
	 */
	public static void upDate() {  
        Timer timer = new Timer();  
        timer.schedule(new TimerTask() {  
            public void run() {  
            	Label_Result.setText(serial.getTextArea());
            	Label_Temp.setText(serial.getTem() + " ��C");
        		Label_Hum.setText(serial.getHum() + " %");
        		Label_Light.setText(serial.getLight() + " LUX");
        		Label_State.setText(serial.getState());
        		//�����������������Ӧ��ť�����޸�
        		if((!serial.getState().isEmpty()) && serial.getState().equals("��������")){
        			button_OpenHum.setText("������ˮ��");
        			button_OpenLight.setText("�ر�������");
        		}else{
        			if(Set == true){
        				judgeBestState(serial);
        			}
        				
        		}
        		SleepTimer();
        		System.gc();//�ֶ�������������
            }  
        }, new Date(), 500);// �趨ָ����ʱ��time,�˴�Ϊ200����  
    }  
	
	/**
	 * �ж������ʪ�ȡ���������
	 */
	public static void judgeBestState(CommDao serial) {

		if ((!textField_BestTem.getText().isEmpty()) && (!textField_BestHum.getText().isEmpty()) && (!textField_BestLight.getText().isEmpty())) {
			if ((!serial.getTem().isEmpty()) && (!serial.getHum().isEmpty()) && (!serial.getLight().isEmpty())) {
				System.out.println(textField_BestTem.getText() + " " + textField_BestHum.getText() + " " + textField_BestLight.getText());
				int tem = Integer.parseInt(textField_BestTem.getText());
				int hum = Integer.parseInt(textField_BestHum.getText());
				int light = Integer.parseInt(textField_BestLight.getText());
				int temReal = Integer.parseInt(serial.getTem());
				int humReal = Integer.parseInt(serial.getHum());
				int lightReal = Integer.parseInt(serial.getLight());
				
				SleepTimer();

				if (temReal < tem - 5){//�¶�
					if(button_OpenTem.getText().equals("��������Ƭ")){
						serial.startTem();
						button_OpenTem.setText("�رռ���Ƭ");
					}
				}else if(temReal > tem + 5){
					if(button_OpenTem.getText().equals("�رռ���Ƭ")){
						serial.downTem();
						button_OpenTem.setText("��������Ƭ");
					}
					
				}
				
				SleepTimer();
				
				if (humReal < hum - 5){//ʪ��
					if(button_OpenHum.getText().equals("������ˮ��")){
						serial.startHum();
						button_OpenHum.setText("�ر���ˮ��");
					}
				}else if(humReal > hum + 5){
					if(button_OpenHum.getText().equals("�ر���ˮ��")){
						serial.downHum();
						button_OpenHum.setText("������ˮ��");
					}
					
				}
				
				SleepTimer();
				
				if (lightReal < light - 5){//����
					if(button_OpenLight.getText().equals("��������")){
						serial.startPWM();
						button_OpenLight.setText("�ر�������");
					}
					
				}else if(lightReal > light + 5){
					if(button_OpenLight.getText().equals("�ر�������")){
						serial.downPWM();
						button_OpenLight.setText("��������");
					}
				}
				
				SleepTimer();
				
			}
			
		}
		
	}
	/**
	 * Create the frame.
	 */
	public SerialUI() {
		setTitle("����ũҵС����");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 530, 427);
		contentPane_Main = new JPanel();
		contentPane_Main.setForeground(Color.BLACK);
		setContentPane(contentPane_Main);
		contentPane_Main.setLayout(null);
		
		Label_Result = new JLabel();

		Label_Result.setBounds(216, 44, 277, 24);
		contentPane_Main.add(Label_Result);
		
		Label_ListResult = new JLabel("���յ�����Ϣ:");
		Label_ListResult.setBounds(217, 10, 106, 24);
		contentPane_Main.add(Label_ListResult);
		
		Label_ChooseSerial = new JLabel("ѡ�񴮿�:");
		Label_ChooseSerial.setBounds(10, 28, 77, 15);
		contentPane_Main.add(Label_ChooseSerial);
		
		ComboBox_ListSerial = new JComboBox();
		ComboBox_ListSerial.insertItemAt("��ѡ��", 0);
		ComboBox_ListSerial.setSelectedIndex(0);
		ComboBox_ListSerial.setEditable(true);
		
		Enumeration en = serial.listPort();//��Ӵ���
		while(en.hasMoreElements()){
			cpid = (CommPortIdentifier)en.nextElement();
			if(cpid.getPortType() == CommPortIdentifier.PORT_SERIAL){
				System.out.println(cpid.getName() + ", " + cpid.getCurrentOwner());
				ComboBox_ListSerial.addItem(cpid.getName());
			}
		}
		
		/**
		 * ����������Ӱ�ť
		 */
		ComboBox_ListSerial.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {

					port = port.replaceAll(port, (String) ComboBox_ListSerial.getSelectedItem());
					System.out.println("port before select: " + port);
					serial.selectPort(port);
					System.out.println("port after select: " + port);
			}
		});
		
		ComboBox_ListSerial.setBounds(97, 26, 60, 19);
		contentPane_Main.add(ComboBox_ListSerial);
		
		button_OpenSerial = new JButton("�򿪴���");
		button_OpenSerial.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				serial.openPort();
				serial.startRead(0);
				try {
					Thread.sleep(500);
				} catch (InterruptedException je) {
					je.printStackTrace();
				}
//				judgeBestState(serial);
			}
		});
		button_OpenSerial.setBounds(10, 56, 90, 23);
		contentPane_Main.add(button_OpenSerial);
		
		button_closeSerial = new JButton("�رմ���");
		button_closeSerial.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				serial.close();
			}
		});
		button_closeSerial.setBounds(107, 56, 90, 23);
		contentPane_Main.add(button_closeSerial);
		
		JPanel panel_TemHumLight = new JPanel();
		panel_TemHumLight.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u6E29\u6E7F\u5EA6\uFF1A", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_TemHumLight.setBounds(19, 113, 178, 138);
		contentPane_Main.add(panel_TemHumLight);
		panel_TemHumLight.setLayout(null);
		
		label_Temp = new JLabel("�¶ȣ�");
		label_Temp.setBounds(23, 28, 47, 21);
		panel_TemHumLight.add(label_Temp);
		
		label_Hum = new JLabel("ʪ�ȣ�");
		label_Hum.setBounds(23, 59, 47, 21);
		panel_TemHumLight.add(label_Hum);
		
		label_Light = new JLabel("���գ�");
		label_Light.setBounds(23, 89, 47, 21);
		panel_TemHumLight.add(label_Light);
		
		Label_Temp = new JLabel();
		Label_Temp.setBounds(65, 28, 66, 21);
		panel_TemHumLight.add(Label_Temp);
		
		Label_Hum = new JLabel();
		Label_Hum.setBounds(65, 59, 66, 21);
		panel_TemHumLight.add(Label_Hum);
		
		Label_Light = new JLabel();
		Label_Light.setBounds(65, 89, 66, 21);
		panel_TemHumLight.add(Label_Light);
		
		panel_CtrTem = new JPanel();
		panel_CtrTem.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u6E29\u5EA6\u63A7\u5236", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_CtrTem.setBounds(249, 88, 199, 59);
		contentPane_Main.add(panel_CtrTem);
		panel_CtrTem.setLayout(null);
		
		button_OpenTem = new JButton("��������Ƭ");
		button_OpenTem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		button_OpenTem.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				switch(button_OpenTem.getText()){
				case "��������Ƭ":
					serial.checkPort();
					serial.startTem();
					button_OpenTem.setText("�رռ���Ƭ");
					break;
				case "�رռ���Ƭ":
					serial.checkPort();
					serial.downTem();
					button_OpenTem.setText("��������Ƭ");
				default:
					break;
				}

			}
		});
		button_OpenTem.setBounds(52, 20, 110, 23);
		panel_CtrTem.add(button_OpenTem);
		
		panel_CtrHum = new JPanel();
		panel_CtrHum.setLayout(null);
		panel_CtrHum.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u6E7F\u5EA6\u63A7\u5236", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_CtrHum.setBounds(249, 157, 199, 59);
		contentPane_Main.add(panel_CtrHum);
		
		button_OpenHum = new JButton("������ˮ��");
		button_OpenHum.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				switch(button_OpenHum.getText()){
				case "������ˮ��":
					serial.checkPort();
					serial.startHum();
					button_OpenHum.setText("�ر���ˮ��");
					break;
				case "�ر���ˮ��":
					serial.checkPort();
					serial.downHum();
					button_OpenHum.setText("������ˮ��");
				default:
					break;
				}

			}
		});
		button_OpenHum.setBounds(52, 20, 110, 23);
		panel_CtrHum.add(button_OpenHum);
		
		panel_CtrLight = new JPanel();
		panel_CtrLight.setBorder(new TitledBorder(null, "\u5149\u7167\u63A7\u5236", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_CtrLight.setBounds(249, 226, 199, 59);
		contentPane_Main.add(panel_CtrLight);
		panel_CtrLight.setLayout(null);
		
		button_OpenLight = new JButton("��������");
		button_OpenLight.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				switch(button_OpenLight.getText()){
				case "��������":
					serial.checkPort();
					serial.startPWM();
					button_OpenLight.setText("�ر�������");
					break;
				case "�ر�������":
					serial.checkPort();
					serial.downPWM();
					button_OpenLight.setText("��������");
				default:
					break;
				}
				SleepTimer();

			}
		});
		button_OpenLight.setBounds(53, 26, 110, 23);
		panel_CtrLight.add(button_OpenLight);
		
		panel_State = new JPanel();
		panel_State.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u68DA\u5185\u73B0\u51B5", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_State.setBounds(19, 290, 178, 76);
		contentPane_Main.add(panel_State);
		panel_State.setLayout(null);
		
		Label_State = new JLabel();
		Label_State.setBounds(36, 25, 108, 30);
		panel_State.add(Label_State);;
		
		panel_SetTemHumLight = new JPanel();
		panel_SetTemHumLight.setBounds(223, 295, 270, 71);
		contentPane_Main.add(panel_SetTemHumLight);
		panel_SetTemHumLight.setBorder(new TitledBorder(null, "\u8BBE\u5B9A\u6700\u4F73\u6E29\u6E7F\u5EA6\u53CA\u5149\u7167\u503C", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_SetTemHumLight.setLayout(null);
		
		textField_BestTem = new JTextField();
		textField_BestTem.setBounds(10, 29, 49, 21);
		textField_BestTem.setText("");
		panel_SetTemHumLight.add(textField_BestTem);
		textField_BestTem.setColumns(10);
		
		textField_BestHum = new JTextField();
		textField_BestHum.setBounds(69, 29, 49, 21);
		textField_BestHum.setText("");
		panel_SetTemHumLight.add(textField_BestHum);
		textField_BestHum.setColumns(10);
		
		textField_BestLight = new JTextField();
		textField_BestLight.setBounds(128, 29, 49, 21);
		textField_BestLight.setText("");
		panel_SetTemHumLight.add(textField_BestLight);
		textField_BestLight.setColumns(10);
		
		button_Set = new JButton("Set");
		button_Set.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				switch(button_Set.getText()){
				case "Unset":
					Set = false;
					button_Set.setText("Set");
					break;
				case "Set":
					Set = true;
					button_Set.setText("Unset");
				default:
					break;
				}
			}
		});

		button_Set.setBounds(191, 25, 69, 29);
		panel_SetTemHumLight.add(button_Set);

	}
	
	public static void SleepTimer(){
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	private static JPanel contentPane_Main;//�����
	private static JLabel Label_Result;//������Ϣ��ǩ
	private static JLabel Label_ListResult;//��ʾ���յ�����Ϣ
	private static JLabel Label_ChooseSerial;//ѡ�񴮿ڱ�ǩ
	private static JComboBox ComboBox_ListSerial;//������
	private static JButton button_OpenSerial;//�򿪴��ڰ�ť
	private static JButton button_closeSerial;//�رմ��ڰ�ť
	private static JLabel label_Temp;//�¶ȱ�ǩ
	private static JLabel label_Hum;//ʪ�ȱ�ǩ
	private static JLabel label_Light;//���ձ�ǩ
	private static JLabel Label_Temp;//��ʾ�¶�
	private static JLabel Label_Hum;//��ʾʪ��
	private static JLabel Label_Light;//��ʾ����
	private static JPanel panel_CtrLight;//�ƹ�������
	private static JButton button_OpenLight;//�������ư�ť
	private static JPanel panel_CtrTem;//�¶ȿ������ 
	private static JButton button_OpenTem;//�򿪼���Ƭ��ť
	private static JPanel panel_CtrHum;//ʪ�ȿ������ 
	private static JButton button_OpenHum;//����ˮ����ť
	private static JPanel panel_State; //��ʾ״̬���
	private static JLabel Label_State;//��ʾ״̬
	private static JPanel panel_SetTemHumLight;//���������ʪ�ȹ���ֵ���
	private static JTextField textField_BestTem;//����¶�ֵ
	private static JTextField textField_BestHum;//���ʪ��ֵ
	private static JTextField textField_BestLight;//��ѹ���ֵ
	private static JButton button_Set;//���ð�ť
	
}
