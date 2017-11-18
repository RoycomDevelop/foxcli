package linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Common {
	/**
	 * searchRegexString ��Ŀ���ַ����а��������Ӵ��󣬷ָ�ȡ����һ�С�
	 * @param srcStr �������ַ���
	 * @param regexStr �����ַ���
	 * @param splitString �ָ��ַ��Ӵ���һ����ʹ��" *"���ָ����ո�
	 * @param column ��ʹ���������ʽ��ѯ����ַ����ָ��ĵ�column�и�ֵ��һ��ArrayList�Ķ���
	 * @return ���ز�ѯ���ָ���column�е��ַ������顣
	 */
	public static ArrayList<String> searchRegexString(String srcStr, String regexStr, String splitString, int column){
		ArrayList<String> result = new ArrayList<String>();
		Pattern pattern = Pattern.compile(regexStr, Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(srcStr);
		while(matcher.find()){
			String tmp = matcher.group().trim();
			String[] listA = tmp.split(String.format("%s*", splitString));
			result.add(listA[column].trim());
		}
		return result;
	}
	
	/**
	 * exeShell ����shell�������ִ�н��
	 * @param cmd shell�����ַ���
	 * @return ����ִ�н��
	 * @throws IOException ���ܳ��ֵ�IO�쳣
	 * @throws InterruptedException �쳣
	 */
	public static String exeShell(String cmd) throws IOException, InterruptedException {
		ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", cmd);
		builder.redirectErrorStream(true);
		Process p = builder.start();
		p.waitFor();
		InputStream is =  p.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        StringBuilder esb = new StringBuilder();
        InputStream eis = p.getErrorStream();
        InputStreamReader eisr = new InputStreamReader(eis);
        BufferedReader ebr = new BufferedReader(eisr);
        while(true){
            String line = br.readLine();
            if(line == null){
                break;
            }
            
            if(line.trim().length()>0){
                sb.append(line);
                sb.append("\n");
            }
        }
        br.close();
        isr.close();
        is.close();
        
        while(true){
            String eline = ebr.readLine();
            if(eline==null){
                break;
            }
            if(eline.trim().length()>0){
                esb.append(eline);
                esb.append("\n");
            }
        }
        ebr.close();
        eisr.close();
        eis.close();
        
        String ssb = sb.toString().trim();
        String sesb = esb.toString().trim();
        if(0 == sesb.length()){
        	return ssb;
        }else{
        	return sesb;
        }
	}
}