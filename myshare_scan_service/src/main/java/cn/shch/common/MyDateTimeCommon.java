package cn.shch.common;

import java.util.Calendar;
import java.util.Date;

public class MyDateTimeCommon {

	// public static final int ONE_HOUR=60;
	// public static final int ONE_MINIUTE=60;

	/**
	 * 由于中国和美国的计时习惯是不同的，比如：每周的第一天，在中国是星期一，而在西方是星期天 此枚举类型用于定义按照中国还是西方的计时习惯
	 * 
	 * @author shangcheng
	 *
	 */
	// enum Custom {
	// CHINESE, AMERICAN
	// }

	// public static final int UNIT_OF_WEEK = 1;

	// public static final int UNIT_OF_MONTH = 2;

	// public static final int UNIT_OF_YEAR = 3;

	private MyDateTimeCommon() {
	}

	/**
	 * 根据给定日期获取当前（周，月，年）最后日期
	 */
	public static Date ObtainLastDayBySpecificDate(Date date, Unit unit) {
		Calendar cal = Calendar.getInstance();
		switch (unit) {
		case UNIT_OF_WEEK:
			cal.set(Calendar.DAY_OF_WEEK, cal.getActualMaximum(Calendar.DAY_OF_WEEK));
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			break;
		case UNIT_OF_MONTH:
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			break;
		case UNIT_OF_YEAR:
			cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR));
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			break;
		default:
			break;
		}
		Date dt = cal.getTime();
		return dt;
	}

	/**
	 * 根据给定日期获取当前（周，月，年）第一天日期
	 */
	public static Date ObtainFirstDayBySpecificDate(Date date, Unit unit) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		switch (unit) {
		case UNIT_OF_WEEK:
			cal.set(Calendar.DAY_OF_WEEK, cal.getActualMinimum(Calendar.DAY_OF_WEEK));
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			break;
		case UNIT_OF_MONTH:
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			break;
		case UNIT_OF_YEAR:
			cal.set(Calendar.DAY_OF_YEAR, cal.getActualMinimum(Calendar.DAY_OF_YEAR));
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			break;
		default:
			break;
		}
		Date dt = cal.getTime();
		return dt;
	}

	/**
	 * 根据给定时间（时，分，秒）获取当前第一（时，分，秒）日期
	 * 
	 * @param date
	 * @param unit
	 * @return
	 */
	public static Date obtainFirstTimeBySpecificDate(Date date, Unit unit) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		switch (unit) {
		case UNIT_OF_HOUR:
			cal.set(Calendar.HOUR_OF_DAY, cal.getActualMinimum(Calendar.HOUR_OF_DAY));
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			break;
		case UNIT_OF_MINUTE:
			cal.set(Calendar.MINUTE, 0);
			break;
		case UNIT_OF_SECOND:
			cal.set(Calendar.SECOND, 0);
			break;
		default:
			break;
		}
		return cal.getTime();
	}

	/**
	 * 根据给定时间（时，分，秒）获取当前第一（时，分，秒）日期
	 * 
	 * @param date
	 * @param unit
	 * @return
	 */
	public static Date obtainLastTimeBySpecificDate(Date date, Unit unit) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		switch (unit) {
		case UNIT_OF_HOUR:
			cal.set(Calendar.HOUR_OF_DAY, cal.getActualMaximum(Calendar.HOUR_OF_DAY));
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			break;
		case UNIT_OF_MINUTE:
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			break;
		case UNIT_OF_SECOND:
			cal.set(Calendar.SECOND, 59);
			break;
		default:
			break;
		}
		return cal.getTime();
	}

}
