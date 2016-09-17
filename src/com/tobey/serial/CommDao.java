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
	private String appName = "����ͨѶ����";
	private int timeout = 2000;//open �˿�ʱ�ĵȴ�ʱ��
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
	 * @�������� :listPort
	 * @�������� :�г����п��õĴ���
	 * @����ֵ���� :void
	 */
	@SuppressWarnings("rawtypes")
	public Enumeration listPort(){
		CommPortIdentifier cpid;
		Enumeration en = CommPortIdentifier.getPortIdentifiers();
		
		System.out.println("now to list all Port of this PC��" +en);
		
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
	 * @�������� :selectPort
	 * @�������� :ѡ��һ�����ڣ����磺COM1
	 * @����ֵ���� :void
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
	 * @�������� :openPort
	 * @�������� :��SerialPort
	 * @����ֵ���� :void
	 */
	public void openPort(){
		if(commPort == null)
			log(String.format("�޷��ҵ����ڣ�"));
		else{
			log("�˿�ѡ��ɹ�����ǰ�˿ڣ�"+commPort.getName()+",����ʵ���� SerialPort:");
			
			try{
				serialPort = (SerialPort)commPort.open(appName, timeout);
				try {
					serialPort.setSerialPortParams(115200,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
					}
					catch (UnsupportedCommOperationException e){} 
				log("ʵ�� SerialPort �ɹ���");
			}catch(PortInUseException e){
				throw new RuntimeException(String.format("�˿�'%1$s'����ʹ���У�", 
						commPort.getName()));
			}
		}
	}
	
	/**
	 * @�������� :checkPort
	 * @�������� :���˿��Ƿ���ȷ����
	 * @����ֵ���� :void
	 */
	public void checkPort(){
		if(commPort == null)
			throw new RuntimeException("û��ѡ��˿ڣ���ʹ�� " +
					"selectPort(String portName) ����ѡ��˿�");
		
		if(serialPort == null){
			throw new RuntimeException("SerialPort ������Ч��");
		}
	}
	
	/**
	 * @�������� :write
	 * @�������� :��˿ڷ������ݣ����ڵ��ô˷���ǰ ��ѡ��˿ڣ���ȷ��SerialPort�����򿪣�
	 * @����ֵ���� :void
	 *	@param message
	 */
	public void write(byte[] message) {
		checkPort();
		
		try{
			outputStream = new BufferedOutputStream(serialPort.getOutputStream());
		}catch(IOException e){
			throw new RuntimeException("��ȡ�˿ڵ�OutputStream����"+e.getMessage());
		}
		
		try{
			outputStream.write(message);
			log("��Ϣ���ͳɹ���");
			
		}catch(IOException e){
			throw new RuntimeException("��˿ڷ�����Ϣʱ����"+e.getMessage());
		}finally{
			try{
				outputStream.close();
			}catch(Exception e){
			}
		}
	}
	
	/**
	 * @�������� :startRead
	 * @�������� :��ʼ�����Ӷ˿��н��յ�����
	 * @����ֵ���� :void
	 *	@param time  ��������Ĵ��ʱ�䣬��λΪ�룬0 ����һֱ����
	 */
	public void startRead(int time){
		checkPort();
		
		try{
			inputStream = new BufferedInputStream(serialPort.getInputStream());
		}catch(IOException e){
			throw new RuntimeException("��ȡ�˿ڵ�InputStream����"+e.getMessage());
		}
		
		try{
			serialPort.addEventListener(this);
		}catch(TooManyListenersException e){
			throw new RuntimeException(e.getMessage());
		}
		
		serialPort.notifyOnDataAvailable(true);
		
		log(String.format("��ʼ��������'%1$s'������--------------", commPort.getName()));
		if(time > 0){
			this.threadTime = time*1000;
			Thread t = new Thread(this);
			t.start();
			log(String.format("����������%1$d���رա�������", threadTime/1000));
		}
	}
	
	
	/**
	 * @�������� :close
	 * @�������� :�ر� SerialPort
	 * @����ֵ���� :void
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
	 * ���ݽ��յļ���������
	 */
	public void serialEvent(SerialPortEvent arg0) {
		switch(arg0.getEventType()){
		case SerialPortEvent.BI:/*Break interrupt,ͨѶ�ж�*/ 
        case SerialPortEvent.OE:/*Overrun error����λ����*/ 
        case SerialPortEvent.FE:/*Framing error����֡����*/
        case SerialPortEvent.PE:/*Parity error��У�����*/
        case SerialPortEvent.CD:/*Carrier detect���ز����*/
        case SerialPortEvent.CTS:/*Clear to send���������*/ 
        case SerialPortEvent.DSR:/*Data set ready�������豸����*/ 
        case SerialPortEvent.RI:/*Ring indicator������ָʾ*/
        case SerialPortEvent.OUTPUT_BUFFER_EMPTY:/*Output buffer is empty��������������*/ 
            break;
        case SerialPortEvent.DATA_AVAILABLE:/*Data available at the serial port���˿��п������ݡ������������飬������ն�*/
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
            	case "0201"://���յ���ʪ����Ϣ
            		getTemHumLightMsg(readBuffer);
            		break;
            	case "0501"://���յ��Ⱥ�����Ϣ
            		PIRcheck(s2);
            	default:
            		break;
            	}
            	log(s2 + "\n");
            	textareaData = s2;
            	
	            log(s2);

            } catch (IOException e) {
            }
            System.gc();//�ֶ�������������
		}
	}
	
	public String getTextArea(){//��ȡָ����
		return textareaData;
	}
	
	public String getTem() {//��ȡ�¶�
		return Tem;
	} 
	
	public String getHum() {//��ȡʪ��
		return Hum;
	}
	
	public String getLight() {//��ȡ����
		return Light;
	}
	
	public String getState() {//��ȡ����״��
		return State;
	}
	
	//�����ʪ�ȼ�����
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
//	            		textField_Temp.setText(tem + " ��");
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
	
	public void PIRcheck(String str){//ͨ���Ⱥ����͵��ж���û����
		if (Integer.parseInt(str.substring(15,17)) == 1){
			checkPort();
			log("����" + str.substring(15,17));
			downHum();
			SleepTimer();
			startPWM();
			State = "��������";
			SleepTimer();
		}else{
			log("û��" + str.substring(15,17));
			State = "��������";
		}
			
	}
	//����������
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
	//�رռ�����
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

	//������ˮ��
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
	//�ر���ˮ��
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
		
	//����������
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
	//�ر�������
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
			log(String.format("�˿�''�����ر��ˣ�", commPort.getName()));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	//���ֽ���ת��Ϊ16�����ַ���
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
	
	//  ת��ʮ�����Ʊ���Ϊ�ַ���
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
	
	//��ʱ
	public void SleepTimer(){
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
