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
 * 智能农业小助手
 * 
 * 温湿度面板实时显示通过温湿度传感器获得的温湿度及光照值
 * 温度、湿度、光照控制面板用于控制相应设备的启动/关闭
 * 
 * 棚内状况面板用于显示大棚内是否有人，如果有人则关闭喷水器并打开照明灯
 * 否则，不对设备进行控制
 * 
 * 设定最佳温湿度及光照值面板：当点击设置时，系统根据设定的值（+-5范围内）自动实现
 * 对相应设备的控制（例如设置温度为16，当实际温度<11时，加热片启动，当
 * 实际温度>21时，关闭加热片）
 * 
 * 当取消设置时，面板中设置的数值将不再起作用。
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
	 * 定时更新用户界面中的数据
	 */
	public static void upDate() {  
        Timer timer = new Timer();  
        timer.schedule(new TimerTask() {  
            public void run() {  
            	Label_Result.setText(serial.getTextArea());
            	Label_Temp.setText(serial.getTem() + " °C");
        		Label_Hum.setText(serial.getHum() + " %");
        		Label_Light.setText(serial.getLight() + " LUX");
        		Label_State.setText(serial.getState());
        		//若大棚内有人则对相应按钮进行修改
        		if((!serial.getState().isEmpty()) && serial.getState().equals("棚内有人")){
        			button_OpenHum.setText("启动喷水器");
        			button_OpenLight.setText("关闭照明灯");
        		}else{
        			if(Set == true){
        				judgeBestState(serial);
        			}
        				
        		}
        		SleepTimer();
        		System.gc();//手动运行垃圾回收
            }  
        }, new Date(), 500);// 设定指定的时间time,此处为200毫秒  
    }  
	
	/**
	 * 判断最佳温湿度、光照条件
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

				if (temReal < tem - 5){//温度
					if(button_OpenTem.getText().equals("启动加热片")){
						serial.startTem();
						button_OpenTem.setText("关闭加热片");
					}
				}else if(temReal > tem + 5){
					if(button_OpenTem.getText().equals("关闭加热片")){
						serial.downTem();
						button_OpenTem.setText("启动加热片");
					}
					
				}
				
				SleepTimer();
				
				if (humReal < hum - 5){//湿度
					if(button_OpenHum.getText().equals("启动喷水器")){
						serial.startHum();
						button_OpenHum.setText("关闭喷水器");
					}
				}else if(humReal > hum + 5){
					if(button_OpenHum.getText().equals("关闭喷水器")){
						serial.downHum();
						button_OpenHum.setText("启动喷水器");
					}
					
				}
				
				SleepTimer();
				
				if (lightReal < light - 5){//光照
					if(button_OpenLight.getText().equals("打开照明灯")){
						serial.startPWM();
						button_OpenLight.setText("关闭照明灯");
					}
					
				}else if(lightReal > light + 5){
					if(button_OpenLight.getText().equals("关闭照明灯")){
						serial.downPWM();
						button_OpenLight.setText("打开照明灯");
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
		setTitle("智能农业小助手");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 530, 427);
		contentPane_Main = new JPanel();
		contentPane_Main.setForeground(Color.BLACK);
		setContentPane(contentPane_Main);
		contentPane_Main.setLayout(null);
		
		Label_Result = new JLabel();

		Label_Result.setBounds(216, 44, 277, 24);
		contentPane_Main.add(Label_Result);
		
		Label_ListResult = new JLabel("接收到的消息:");
		Label_ListResult.setBounds(217, 10, 106, 24);
		contentPane_Main.add(Label_ListResult);
		
		Label_ChooseSerial = new JLabel("选择串口:");
		Label_ChooseSerial.setBounds(10, 28, 77, 15);
		contentPane_Main.add(Label_ChooseSerial);
		
		ComboBox_ListSerial = new JComboBox();
		ComboBox_ListSerial.insertItemAt("请选择", 0);
		ComboBox_ListSerial.setSelectedIndex(0);
		ComboBox_ListSerial.setEditable(true);
		
		Enumeration en = serial.listPort();//添加串口
		while(en.hasMoreElements()){
			cpid = (CommPortIdentifier)en.nextElement();
			if(cpid.getPortType() == CommPortIdentifier.PORT_SERIAL){
				System.out.println(cpid.getName() + ", " + cpid.getCurrentOwner());
				ComboBox_ListSerial.addItem(cpid.getName());
			}
		}
		
		/**
		 * 向下拉栏添加按钮
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
		
		button_OpenSerial = new JButton("打开串口");
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
		
		button_closeSerial = new JButton("关闭串口");
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
		
		label_Temp = new JLabel("温度：");
		label_Temp.setBounds(23, 28, 47, 21);
		panel_TemHumLight.add(label_Temp);
		
		label_Hum = new JLabel("湿度：");
		label_Hum.setBounds(23, 59, 47, 21);
		panel_TemHumLight.add(label_Hum);
		
		label_Light = new JLabel("光照：");
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
		
		button_OpenTem = new JButton("启动加热片");
		button_OpenTem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		button_OpenTem.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				switch(button_OpenTem.getText()){
				case "启动加热片":
					serial.checkPort();
					serial.startTem();
					button_OpenTem.setText("关闭加热片");
					break;
				case "关闭加热片":
					serial.checkPort();
					serial.downTem();
					button_OpenTem.setText("启动加热片");
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
		
		button_OpenHum = new JButton("启动喷水器");
		button_OpenHum.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				switch(button_OpenHum.getText()){
				case "启动喷水器":
					serial.checkPort();
					serial.startHum();
					button_OpenHum.setText("关闭喷水器");
					break;
				case "关闭喷水器":
					serial.checkPort();
					serial.downHum();
					button_OpenHum.setText("启动喷水器");
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
		
		button_OpenLight = new JButton("打开照明灯");
		button_OpenLight.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				switch(button_OpenLight.getText()){
				case "打开照明灯":
					serial.checkPort();
					serial.startPWM();
					button_OpenLight.setText("关闭照明灯");
					break;
				case "关闭照明灯":
					serial.checkPort();
					serial.downPWM();
					button_OpenLight.setText("打开照明灯");
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
	private static JPanel contentPane_Main;//主面板
	private static JLabel Label_Result;//接收消息标签
	private static JLabel Label_ListResult;//显示接收到的消息
	private static JLabel Label_ChooseSerial;//选择串口标签
	private static JComboBox ComboBox_ListSerial;//下拉栏
	private static JButton button_OpenSerial;//打开串口按钮
	private static JButton button_closeSerial;//关闭串口按钮
	private static JLabel label_Temp;//温度标签
	private static JLabel label_Hum;//湿度标签
	private static JLabel label_Light;//光照标签
	private static JLabel Label_Temp;//显示温度
	private static JLabel Label_Hum;//显示湿度
	private static JLabel Label_Light;//显示光照
	private static JPanel panel_CtrLight;//灯光控制面板
	private static JButton button_OpenLight;//打开照明灯按钮
	private static JPanel panel_CtrTem;//温度控制面板 
	private static JButton button_OpenTem;//打开加热片按钮
	private static JPanel panel_CtrHum;//湿度控制面板 
	private static JButton button_OpenHum;//打开喷水器按钮
	private static JPanel panel_State; //显示状态面板
	private static JLabel Label_State;//显示状态
	private static JPanel panel_SetTemHumLight;//设置最佳温湿度光照值面板
	private static JTextField textField_BestTem;//最佳温度值
	private static JTextField textField_BestHum;//最佳湿度值
	private static JTextField textField_BestLight;//最佳光照值
	private static JButton button_Set;//设置按钮
	
}
