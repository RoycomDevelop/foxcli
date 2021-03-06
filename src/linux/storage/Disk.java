package linux.storage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import linux.Common;
import linux.storage.instrument.Dev;
import linux.storage.instrument.InputException;

public class Disk implements Dev {
	private String model;
	private String fw;
	private String sn;
	private Interface interfaces;
	private String devName;
	private String fromChip;
	private String smartStr;
	private Map<String, Map<String, String>> smart_map = null;
	private String smartAll;
	
	/**
	 * Disk Disk类的构造函数
	 * @param from_chip 连接名字为dev_name的控制芯片型号
	 * @param dev_name 磁盘名称，例如sda
	 */
	public Disk(String from_chip, String dev_name){
		model = fw = sn = "";
		smartStr = "smartctl -a";
		if(!dev_name.matches("sd[a-z]*")){
			devName = "";
			throw new InputException("Disk.this(from_chip, dev_name): dev_name is invalid!");
		}else{
			devName = dev_name;
		}
		if(from_chip.matches(".*3008.*")){
			fromChip = "3008";
		}else if(from_chip.matches(".*2008.*")){
			fromChip = "2008";
		}else if(from_chip.matches(".*2308.*")){
			fromChip = "2308";
		}else{
			fromChip = "";
			throw new InputException("Disk.this(from_chip, dev_name): SAS Chip is not support!");
		}
	}
	
	/**
	 * fillAttr 填充磁盘的所有属性
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void fillAttr() throws IOException, InterruptedException{
		Pattern pattern = null;
		Matcher matcher = null;
		String smartCmd = String.format("%s /dev/%s",smartStr, devName);
		smartAll = Common.exeShell(smartCmd);
		sn = Common.searchRegexString(smartAll, ".*Serial.*", " ", 2).get(0);
		fw = Common.searchRegexString(smartAll, "(^Firmware Version.*)|(^Revision.*)", ":", 1).get(0);
		model = Common.searchRegexString(smartAll, "(^Product.*)|(.*Model.*)", ":", 1).get(0);
		boolean isSata = smartAll.matches("(\n|.)*SATA(.|\n)*");
		boolean isSas = smartAll.matches("(\n|.)*SAS(.|\n)*");
		boolean isNvme = smartAll.matches("(\n|.)*NVM(.|\n)*");
		if(isSata){
			interfaces = Interface.SATA;
			pattern = Pattern.compile(".*  0x.*-*", Pattern.MULTILINE);
			matcher = pattern.matcher(smartAll);
			while(matcher.find()){
				Map<String, String> m1 = new HashMap<String, String>();
				String line_smart = matcher.group().trim();
				String[] arr = line_smart.split(" *");
				m1.put("FLAG", arr[2]);
				m1.put("VALUE", arr[3]);
				m1.put("WORST", arr[4]);
				m1.put("THRESH", arr[5]);
				m1.put("TYPE", arr[6]);
				m1.put("UPDATED", arr[7]);
				m1.put("WHEN_FAILED", arr[8]);
				m1.put("RAW_VALUE", arr[9]);
				smart_map.put(arr[1], m1);
			}
		}
		if(isSas) {
			interfaces = Interface.SAS;
			pattern = Pattern.compile(".*(read:|write:|verify:).*", Pattern.MULTILINE);
			matcher = pattern.matcher(smartAll);
			while(matcher.find()){
				Map<String, String> m1 = new HashMap<String, String>();
				String line_smart = matcher.group().trim();
				String[] arr = line_smart.split(" *");
				m1.put("ECC_fast", arr[1]);
				m1.put("ECC_delayed", arr[2]);
				m1.put("rerw", arr[3]);
				m1.put("errors_corrected", arr[4]);
				m1.put("algorithm_invocations", arr[5]);
				m1.put("processed_10^9_bytes", arr[6]);
				m1.put("uncorrected_error", arr[7]);
				smart_map.put(arr[0], m1);
			}
		}
		if(isNvme)
			interfaces = Interface.NVMe;
	}
	
	/**
	 * smartToJson 将本磁盘的成员smart信息格式化成json格式
	 * @return 返回json格式的smart信息字符串
	 * @throws JsonProcessingException
	 */
	public String smartToJson() throws JsonProcessingException{
		String sataSmartJsonStr = null;
		ObjectMapper mapper = new ObjectMapper();
		sataSmartJsonStr = mapper.writeValueAsString(smart_map);
		return sataSmartJsonStr;
	}
	
	/**
	 * smartToString 打印输出磁盘smart信息
	 */
	public void smartToString(){
		System.out.print(smartAll);
	}
	
	/**
	 * getSmart_map 获取smart_map成员变量的Map<String, Map<String, String>>类型值
	 * @return
	 */
	public Map<String, Map<String, String>> getSmart_map() {
		return smart_map;
	}
	
	/**
	 * getSmartAll 获取磁盘smart信息的String类型值
	 * @return
	 */
	public String getSmartAll() {
		return smartAll;
	}

	/**
	 * getSn 获取磁盘SN号
	 * @return
	 */
	public String getSn() {
		return sn;
	}
	
	/**
	 * 设置磁盘SN号
	 * @param sn
	 */
	public void setSn(String sn) {
		this.sn = sn;
	}
	
	/**
	 * 获取磁盘接口协议名称，例如SAS、SATA、NVMe
	 * @return
	 */
	public Interface getInterfaces() {
		return interfaces;
	}
	
	/**
	 * 设置磁盘接口协议名称，例如SAS、SATA、NVMe
	 * @return
	 */
	public void setInterfaces(Interface interfaces) {
		this.interfaces = interfaces;
	}

	/**
	 * 获取磁盘型号全称
	 * @return
	 */
	public String getModel() {
		return model;
	}

	/**
	 * 设置磁盘型号全称
	 * @param model
	 */
	public void setModel(String model) {
		this.model = model;
	}

	/**
	 * 获取磁盘固件版本
	 * @return
	 */
	public String getFw() {
		return fw;
	}

	/**
	 * 设置磁盘固件版本
	 * @return
	 */
	public void setFw(String fw) {
		this.fw = fw;
	}

	/**
	 * 获取磁盘设备名称
	 * @return
	 */
	public String getDevName() {
		return devName;
	}

	/**
	 * 获取磁盘连接的控制器芯片型号
	 * @return
	 */
	public String getFromChip() {
		return fromChip;
	}

	/**
	 * 获取smartctl的命令字符串
	 * @return
	 */
	public String getSmartStr() {
		return smartStr;
	}
}
