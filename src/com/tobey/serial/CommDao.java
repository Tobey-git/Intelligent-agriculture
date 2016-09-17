package com.tobey.serial;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TooManyListenersException;

import javax.swing.JPanel;

public class CommDao implements Runnable, SerialPortEventListener{

	private String port = "COM1";
	private JPanel contentPane;
	private String appName = "串口通讯测试";
	private int timeout = 2000;//open 端口时的等待时间
	private int threadTime = 0;
	
	private CommPortIdentifier commPort;
	private SerialPort serialPort;
	private InputStream inputStream;
	private OutputStream outputStream;
	private String textareaData;
	private String Tem="", Hum="", Light="";
	private String State="";
	
	/**
	 * @return 
	 * @方法名称 :listPort
	 * @功能描述 :列出所有可用的串口
	 * @返回值类型 :void
	 */
	@SuppressWarnings("rawtypes")
	public Enumeration listPort(){
		CommPortIdentifier cpid;
		Enumeration en = CommPortIdentifier.getPortIdentifiers();
		
		System.out.println("now to list all Port of this PC：" +en);
		
//		while(en.hasMoreElements()){
//			cpid = (CommPortIdentifier)en.nextElement();
//			if(cpid.getPortType() == CommPortIdentifier.PORT_SERIAL){
//				System.out.println(cpid.getName() + ", " + cpid.getCurrentOwner());
////				ComboBox_ListSerial.addItem(cpid.getName());
//			}
//		}
		return en;
		
	}
	
	
	/**
	 * @方法名称 :selectPort
	 * @功能描述 :选择一个串口，比如：COM1
	 * @返回值类型 :void
	 *	@param portName
	 */
	//@SuppressWarnings("rawtypes")
	public void selectPort(String portName){
		
		this.commPort = null;
		CommPortIdentifier cpid;
		Enumeration en = CommPortIdentifier.getPortIdentifiers();
		
		while(en.hasMoreElements()){
			cpid = (CommPortIdentifier)en.nextElement();
			if(cpid.getPortType() == CommPortIdentifier.PORT_SERIAL
					&& cpid.getName().equals(portName)){
				this.commPort = cpid;
				break;
			}
		}
	}
	
	/**
	 * @方法名称 :openPort
	 * @功能描述 :打开SerialPort
	 * @返回值类型 :void
	 */
	public void openPort(){
		if(commPort == null)
			log(String.format("无法找到串口！"));
		else{
			log("端口选择成功，当前端口："+commPort.getName()+",现在实例化 SerialPort:");
			
			try{
				serialPort = (SerialPort)commPort.open(appName, timeout);
				try {
					serialPort.setSerialPortParams(115200,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
					}
					catch (UnsupportedCommOperationException e){} 
				log("实例 SerialPort 成功！");
			}catch(PortInUseException e){
				throw new RuntimeException(String.format("端口'%1$s'正在使用中！", 
						commPort.getName()));
			}
		}
	}
	
	/**
	 * @方法名称 :checkPort
	 * @功能描述 :检查端口是否正确连接
	 * @返回值类型 :void
	 */
	public void checkPort(){
		if(commPort == null)
			throw new RuntimeException("没有选择端口，请使用 " +
					"selectPort(String portName) 方法选择端口");
		
		if(serialPort == null){
			throw new RuntimeException("SerialPort 对象无效！");
		}
	}
	
	/**
	 * @方法名称 :write
	 * @功能描述 :向端口发送数据，请在调用此方法前 先选择端口，并确定SerialPort正常打开！
	 * @返回值类型 :void
	 *	@param message
	 */
	public void write(byte[] message) {
		checkPort();
		
		try{
			outputStream = new BufferedOutputStream(serialPort.getOutputStream());
		}catch(IOException e){
			throw new RuntimeException("获取端口的OutputStream出错："+e.getMessage());
		}
		
		try{
			outputStream.write(message);
			log("信息发送成功！");
			
		}catch(IOException e){
			throw new RuntimeException("向端口发送信息时出错："+e.getMessage());
		}finally{
			try{
				outputStream.close();
			}catch(Exception e){
			}
		}
	}
	
	/**
	 * @方法名称 :startRead
	 * @功能描述 :开始监听从端口中接收的数据
	 * @返回值类型 :void
	 *	@param time  监听程序的存活时间，单位为秒，0 则是一直监听
	 */
	public void startRead(int time){
		checkPort();
		
		try{
			inputStream = new BufferedInputStream(serialPort.getInputStream());
		}catch(IOException e){
			throw new RuntimeException("获取端口的InputStream出错："+e.getMessage());
		}
		
		try{
			serialPort.addEventListener(this);
		}catch(TooManyListenersException e){
			throw new RuntimeException(e.getMessage());
		}
		
		serialPort.notifyOnDataAvailable(true);
		
		log(String.format("开始监听来自'%1$s'的数据--------------", commPort.getName()));
		if(time > 0){
			this.threadTime = time*1000;
			Thread t = new Thread(this);
			t.start();
			log(String.format("监听程序将在%1$d秒后关闭。。。。", threadTime/1000));
		}
	}
	
	
	/**
	 * @方法名称 :close
	 * @功能描述 :关闭 SerialPort
	 * @返回值类型 :void
	 */
	public void close(){
		serialPort.close();
		serialPort = null;
//		commPort = null;
	}
	
	
	public void log(String msg){
		System.out.println(appName+" --> "+msg);
	}

	/**
	 * 数据接收的监听处理函数
	 */
	public void serialEvent(SerialPortEvent arg0) {
		switch(arg0.getEventType()){
		case SerialPortEvent.BI:/*Break interrupt,通讯中断*/ 
        case SerialPortEvent.OE:/*Overrun error，溢位错误*/ 
        case SerialPortEvent.FE:/*Framing error，传帧错误*/
        case SerialPortEvent.PE:/*Parity error，校验错误*/
        case SerialPortEvent.CD:/*Carrier detect，载波检测*/
        case SerialPortEvent.CTS:/*Clear to send，清除发送*/ 
        case SerialPortEvent.DSR:/*Data set ready，数据设备就绪*/ 
        case SerialPortEvent.RI:/*Ring indicator，响铃指示*/
        case SerialPortEvent.OUTPUT_BUFFER_EMPTY:/*Output buffer is empty，输出缓冲区清空*/ 
            break;
        case SerialPortEvent.DATA_AVAILABLE:/*Data available at the serial port，端口有可用数据。读到缓冲数组，输出到终端*/
        	byte[] readBuffer = new byte[16];
            String readStr="";
            String s2 = "";
            
            try {
                
            	while (inputStream.available() > 0) {
                    inputStream.read(readBuffer);
                    s2 = bytesToHexString(readBuffer);
                }
                
            	readStr = s2.substring(9,11) + s2.substring(12, 14);
            	switch(readStr){
            	case "0201"://接收到温湿度消息
            		getTemHumLightMsg(readBuffer);
            		break;
            	case "0501"://接收到热红外消息
            		PIRcheck(s2);
            	default:
            		break;
            	}
            	log(s2 + "\n");
            	textareaData = s2;
            	
	            log(s2);

            } catch (IOException e) {
            }
            System.gc();//手动运行垃圾回收
		}
	}
	
	public String getTextArea(){//获取指令结果
		return textareaData;
	}
	
	public String getTem() {//获取温度
		return Tem;
	} 
	
	public String getHum() {//获取湿度
		return Hum;
	}
	
	public String getLight() {//获取光照
		return Light;
	}
	
	public String getState() {//获取棚内状况
		return State;
	}
	
	//获得温湿度及光照
	public String getTemHumLightMsg(byte[] src){
		byte[] buffer=new byte[128];
		String returnValue="";
		checkPort();
			while(true){
				buffer = src;
				if(buffer[0]==0x40){
					if((buffer[3]==(byte)0x2)&&(buffer[4]==(byte)0x1)){
						
						int tem=buffer[5]*256+buffer[6];
						int hum=buffer[7]*256+buffer[8];
						long light=(long)((buffer[9]*256+buffer[10])*3012.9/(32768*4));
						
						returnValue=tem+"_"+hum+"_"+light;
						Tem = tem + "";
						Hum = hum + "";
						Light = light + "";
//	            		textField_Temp.setText(tem + " ℃");
//	            		textField_Hum.setText(hum + " %");
//	            		textField_Light.setText(light + " LUX");
						break;
					}
					
					
				}
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			
		return returnValue;
	}
	
	public void PIRcheck(String str){//通过热红外释电判断有没有人
		if (Integer.parseInt(str.substring(15,17)) == 1){
			checkPort();
			log("有人" + str.substring(15,17));
			downHum();
			SleepTimer();
			startPWM();
			State = "棚内有人";
			SleepTimer();
		}else{
			log("没人" + str.substring(15,17));
			State = "棚内无人";
		}
			
	}
	//启动加热器
	public void startTem(){

		byte[] bytes = new byte[20] ;
		bytes[0] = 0x40 ;
		bytes[1] = 0x06 ;
		bytes[2] = 0x01 ;
		bytes[3] = 0x06 ;
		bytes[4] = 0x0a ;
		for(int k=0 ;k<5 ;k++){
			bytes[5] += bytes[k] ;
		}
		write(bytes) ;

	}
	//关闭加热器
	public void downTem(){

		byte[] bytes = new byte[20] ;
		bytes[0] = 0x40 ;
		bytes[1] = 0x06 ;
		bytes[2] = 0x01 ;
		bytes[3] = 0x06 ;
		bytes[4] = 0x0c ;
		for(int k=0 ;k<5 ;k++){
			bytes[5] += bytes[k] ;
		}
		write(bytes) ;

	}

	//启动喷水器
	public void startHum(){
		byte[] bytes = new byte[20] ;
		bytes[0] = 0x40 ;
		bytes[1] = 0x06 ;
		bytes[2] = 0x02 ;
		bytes[3] = 0x06 ;
		bytes[4] = 0x0a ;
		for(int k=0 ;k<5 ;k++){
			bytes[5] += bytes[k] ;
		}
		write(bytes) ;

	}
	//关闭喷水器
	public void downHum(){
		byte[] bytes = new byte[20] ;
		bytes[0] = 0x40 ;
		bytes[1] = 0x06 ;
		bytes[2] = 0x02 ;
		bytes[3] = 0x06 ;
		bytes[4] = 0x0c ;
		for(int k=0 ;k<5 ;k++){
			bytes[5] += bytes[k] ;
		}
		write(bytes) ;

	}
		
	//启动照明灯
	public void startPWM(){

		byte[] bytes = new byte[20] ;
		bytes[0] = 0x40 ;
		bytes[1] = 0x06 ;
		bytes[2] = 0x01 ;
		bytes[3] = 0x09 ;
		bytes[4] = 0x02 ;
		for(int k=0 ;k<5 ;k++){
			bytes[5] += bytes[k] ;
		}
		write(bytes) ;

	}
	//关闭照明灯
	public void downPWM(){

		byte[] bytes = new byte[20] ;
		bytes[0] = 0x40 ;
		bytes[1] = 0x06 ;
		bytes[2] = 0x01 ;
		bytes[3] = 0x09 ;
		bytes[4] = 0x00 ;
		for(int k=0 ;k<5 ;k++){
			bytes[5] += bytes[k] ;
		}
		write(bytes) ;

	}

	public void run() {
		try{
			Thread.sleep(threadTime);
			serialPort.close();
			log(String.format("端口''监听关闭了！", commPort.getName()));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	//将字节流转换为16进制字符串
	public String bytesToHexString(byte[] src) {
		String stmp = "";
		StringBuilder sb = new StringBuilder("");
		for ( int i=0; i<src.length; i++){
			stmp = Integer.toHexString(src[i] & 0xFF);
			sb.append((stmp.length() == 1) ?"0"+stmp:stmp);
			sb.append(" ");
		}
		return sb.toString().toUpperCase().trim();
	}
	
	//  转化十六进制编码为字符串
	public String toStringHex(String s){
		byte[] baKeyword = new byte[s.length()/2];
		for(int i = 0; i < baKeyword.length; i++){
			try{
				baKeyword[i] = (byte)(0xff & Integer.parseInt(s.substring(i*2, i*2+2),16));
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	  
		try {
			s = new String(baKeyword, "utf-8");//UTF-16le:Not
		} catch (Exception e1) {
			e1.printStackTrace();
		} 
		return s;
	} 
	
	//延时
	public void SleepTimer(){
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
