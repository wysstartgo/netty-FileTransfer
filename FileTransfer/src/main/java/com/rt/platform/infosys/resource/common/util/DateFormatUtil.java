/**
 * 
 */
package com.rt.platform.infosys.resource.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author wuys
 */
public class DateFormatUtil {
	
	 public static final SimpleDateFormat SDFY_M_DHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	 public static final SimpleDateFormat SDFYMDHMS = new SimpleDateFormat("yyyyMMddHHmmss"); 
	 public static final SimpleDateFormat SDFYMD = new SimpleDateFormat("yyyyMMdd");
	 public static final SimpleDateFormat SDFYM = new SimpleDateFormat("yyyyMM");
	 public static final SimpleDateFormat SDFMD = new SimpleDateFormat("MMdd");
	 public static final SimpleDateFormat SDF_YM = new SimpleDateFormat("yyyy-MM");
	 public static final SimpleDateFormat SDF_YMD = new SimpleDateFormat("yyyy-MM-dd");
	 public static SimpleDateFormat sdfymdhmsSSS = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	
	public static String formatSdfymdhmsSSS(Date date){
		synchronized (sdfymdhmsSSS) {
			return sdfymdhmsSSS.format(date);
		}
	}
	
	public static Date parseSdfymdhmsSSS(String dateStr) throws ParseException{
		synchronized (sdfymdhmsSSS) {
			return sdfymdhmsSSS.parse(dateStr);
		}
	}
	
	public static String formatYm(Date date){
		synchronized (SDFYM) {
			return SDFYM.format(date);
		}
	}
	 
	public static String formatY_m_dhms(Date date) {
		synchronized (SDFY_M_DHMS) {
			return SDFY_M_DHMS.format(date);
		}
	}
	
	public static String formatYmdHms(Date date) {
		synchronized (SDFYMDHMS) {
			return SDFYMDHMS.format(date);
		}
	}
	
	
	public static Date parseY_m_dhms(String date) throws ParseException {
		synchronized (SDFY_M_DHMS) {
			return SDFY_M_DHMS.parse(date);
		}
	}
	
	public static Date parseYmdHms(String date) throws ParseException {
		synchronized (SDFYMDHMS) {
			return SDFYMDHMS.parse(date);
		}
	}
	
	public static Date parseYmd(String date) throws ParseException{
		synchronized (SDFYMD) {
			return SDFYMD.parse(date);
		}
	}
	
	public static String formatYmd(Date date){
		synchronized (SDFYMD) {
			return SDFYMD.format(date);
		}
	}
	
	public static Date parse_Ymd(String date) throws ParseException{
		synchronized (SDF_YMD) {
			return SDF_YMD.parse(date);
		}
	}
	
	public static String format_Ymd(Date date){
		synchronized (SDF_YMD) {
			return SDF_YMD.format(date);
		}
	}
	
	public static Date parseMd(String date) throws ParseException{
		synchronized (SDFMD) {
			return SDFMD.parse(date);
		}
	}
	
	public static String formatMd(Date date){
		synchronized (SDFMD) {
			return SDFMD.format(date);
		}
	}
	
	public static Date parse_Ym(String date) throws ParseException{
		synchronized (SDF_YM) {
			return SDF_YM.parse(date);
		}
	}
	
	public static String format_Ym(Date date){
		synchronized (SDF_YM) {
			return SDF_YM.format(date);
		}
	}

	/**
	 *
	 * @param year
	 * @return
	 */
	public static Date addYear(int year){
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR,year);
		return calendar.getTime();
	}

	public static Date addMonth(int month){
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH,month);
		return calendar.getTime();
	}

	public static Date addDay(int day){
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH,day);
		return calendar.getTime();
	}

}
