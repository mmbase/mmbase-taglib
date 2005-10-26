/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.bridge.jsp.taglib.typehandler;

import java.util.*;

import java.text.SimpleDateFormat;
import javax.servlet.jsp.JspTagException;

import org.mmbase.bridge.*;
import org.mmbase.datatypes.*;
import org.mmbase.bridge.util.Queries;
import org.mmbase.bridge.jsp.taglib.FieldInfoTag;
import org.mmbase.bridge.jsp.taglib.ParamHandler;
import org.mmbase.bridge.jsp.taglib.util.ContextContainer;
import org.mmbase.storage.search.*;
import org.mmbase.util.Casting;

import org.mmbase.util.logging.Logging;
import org.mmbase.util.logging.Logger;

/**
 * @author Michiel Meeuwissen
 * @author Vincent vd Locht
 * @since  MMBase-1.6
 * @version $Id: DateHandler.java,v 1.28 2005-10-26 11:46:52 michiel Exp $
 */
public class DateHandler extends AbstractTypeHandler {

    private static final Logger log = Logging.getLoggerInstance(DateHandler.class);

    private static boolean EXIST_YEAR_0 = false;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static final Date NODATE = new Date(-1);

    /**
     * @param tag
     */
    public DateHandler(FieldInfoTag tag) {
        super(tag);
    }

    private void yearFieldValue(Calendar cal, StringBuffer buffer) {
        if (EXIST_YEAR_0) {
            // the year '0' does not really exist in gregorian, So 4 BC == -3, 1 BC == 0
            if(cal.get(Calendar.ERA) == java.util.GregorianCalendar.BC) {
                buffer.append("-");
                buffer.append(cal.get(Calendar.YEAR) - 1);
            } else {
                buffer.append(cal.get(Calendar.YEAR));
            }
        } else {
            // perhaps this is simpler..
            if(cal.get(Calendar.ERA) == java.util.GregorianCalendar.BC) {
                buffer.append("-");
            }
            buffer.append(cal.get(Calendar.YEAR));
        }
    }


    /**
     * @see TypeHandler#htmlInput(Node, Field, boolean)
     */
    public String htmlInput(Node node, Field field, boolean search) throws JspTagException {

        StringBuffer buffer = new StringBuffer();

        Calendar cal = Calendar.getInstance();
        boolean required = field.getDataType().isRequired();
        if (node != null) {
            Object value = node.getValue(field.getName());
            if (value != null) {
                cal.setTime(node.getDateValue(field.getName()));
            } else {
                cal = null;
            }
        } else {
            Object def = field.getDataType().getDefaultValue();
            if (def != null) {
                cal.setTime(Casting.toDate(def));
            } else {
                if (! required) {
                    cal = null;
                }
            }
        }
        buffer.append("<span class=\"mm_datetime\">");
        buffer.append("<input type=\"hidden\" name=\"");
        buffer.append(prefix(field.getName()));
        buffer.append("\" value=\"");
        if (cal != null) {
            buffer.append(Casting.toLong(cal.getTime()));
        }
        buffer.append("\" />");
        // give also present value, this makes it possible to see if user changed this field.

        ContextContainer container = tag.getContextProvider().getContextContainer();
        if (search) {
            String name = prefix(field.getName() + "_search");
            String searchi =  (String) container.find(tag.getPageContext(), name);
            if (searchi == null) searchi = "no";
            buffer.append("<select name=\"" + name + "\" class=\"mm_search\">\n");
            buffer.append("  <option value=\"no\" ");
            if (searchi.equals("no")) buffer.append(" selected=\"selected\" ");
            buffer.append("> </option>");
            buffer.append("  <option value=\"less\" ");
            if (searchi.equals("less")) buffer.append(" selected=\"selected\" ");
            buffer.append(">&lt;</option>");
            buffer.append("  <option value=\"greater\" ");
            if (searchi.equals("greater")) buffer.append(" selected=\"selected\" ");
            buffer.append(">&gt;</option>");
            buffer.append("  <option value=\"equal\" ");
            if (searchi.equals("equal")) buffer.append(" selected=\"selected\" ");
            buffer.append(">=</option>");
            buffer.append("</select>");
        }

        DataType dt = field.getDataType();
        if (log.isDebugEnabled()) {
            log.debug("Using " + dt);
        }
        Calendar minDate = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();
        DateTimePattern dateTimePattern;
        if (! (dt instanceof DateTimeDataType)) {
            // backwards compatibility
            String options = tag.getOptions();
            boolean doDate = true;
            boolean doTime = true;
            if (options != null) {
                boolean time = false;
                if (options.indexOf("time") > -1) {
                    doTime = true;
                    time = true;
                    doDate = false;

                }
                if (options.indexOf("date") > -1) {
                    doDate = true;
                    doTime = time;
                }
            }
            StringBuffer buf = new StringBuffer();
            if (doDate) {
                buf.append("yyyy-MM-dd");
            }
            if (doTime) {
                if (buf.length() > 0) buf.append(" ");
                buf.append("HH'h':mm'm':ss's'");
            }
            dateTimePattern = new DateTimePattern(buf.toString());
            minDate.setTime(new Date(Long.MIN_VALUE));
            maxDate.setTime(new Date(Long.MAX_VALUE));
        } else {
            dateTimePattern = ((DateTimeDataType) dt).getPattern();
            Date min = ((DateTimeDataType) dt).getMin();
            minDate.setTime(min);
            Date max = ((DateTimeDataType) dt).getMax();
            maxDate.setTime(max);
        }

        double interval = (double) maxDate.getTimeInMillis() - (double) minDate.getTimeInMillis();
        int startYear = minDate.get(Calendar.YEAR);
        int   endYear = maxDate.get(Calendar.YEAR);

        Locale locale = tag.getLocale();

        boolean didYear    = false;
        boolean didMonth   = false;
        boolean didDay     = false;
        boolean didHours   = false;
        boolean didMinutes = false;
        boolean didSeconds = false;
        List parsed = dateTimePattern.getList(locale);

        Iterator parsedPattern = parsed.iterator();
        boolean first = true;
        while(parsedPattern.hasNext()) {
            String pattern = (String) parsedPattern.next();
            if (pattern.length() < 1) continue;
            char firstChar = pattern.charAt(0);
            switch(firstChar) {
            case '\'': buffer.append(pattern.substring(1)); break;
            case 'y':  {
                String yearName = prefix(field.getName() + "_year");
                String searchYear =  (String) container.find(tag.getPageContext(), yearName);
                if (interval > 1000.0 * 60 * 60 * 24 * 356 * 200) {
                    buffer.append("<input class=\"mm_year\" type =\"text\" size=\"" + (pattern.length() + 1) + "\" name=\"");
                    buffer.append(yearName);
                    buffer.append("\" ");
                    addExtraAttributes(buffer);
                    buffer.append("value=\"");
                    if (searchYear == null) {
                        if (cal != null) {
                            yearFieldValue(cal, buffer);
                        } else {
                            buffer.append("1970"); // the default default year
                        }
                    } else {
                        buffer.append(searchYear);
                    }
                    buffer.append("\" />");
                } else {
                    if (startYear == endYear) {
                        buffer.append("" + startYear);
                    } else {
                        buffer.append("<select class=\"mm_year\" name=\"");
                        buffer.append(yearName);
                        buffer.append("\"");
                        addExtraAttributes(buffer);
                        buffer.append(">");
                        int checkYear;
                        if (searchYear != null) {
                            checkYear = Integer.parseInt(searchYear);
                        } else {
                            if (cal != null) {
                                checkYear = cal.get(Calendar.YEAR);
                            } else {
                                checkYear = Integer.MAX_VALUE;
                            }
                        }
                        if (! required && first) {
                            buffer.append("<option value=\"" + Integer.MAX_VALUE + "\"");
                            if (checkYear == Integer.MAX_VALUE) {
                                buffer.append(" selected=\"selected\"");
                            }
                            buffer.append(">--</option>");
                        }
                        for (int i = startYear; i <= endYear ; i++) {
                            if (! EXIST_YEAR_0 && i == 0) continue; 
                            if (checkYear == i) {
                                buffer.append("  <option selected=\"selected\" value=\"" + i + "\">" + i + "</option>\n");
                            } else {
                                buffer.append("  <option value=\"" + i + "\">" + i + "</option>\n");
                            }
                        }
                        buffer.append("</select>");
                    }
                }
                didYear = true;
                break;
            }
            case 'M': {
                String monthName = prefix(field.getName() + "_month");
                String searchMonth =  (String) container.find(tag.getPageContext(), monthName);
                int checkMonth;
                if (searchMonth == null) {
                    if (cal != null) {
                        checkMonth = cal.get(Calendar.MONTH) + 1;
                    } else {
                        checkMonth = -1;
                    }
                } else {
                    checkMonth = Integer.parseInt(searchMonth);
                }
                buffer.append("<select class=\"mm_month\" name=\"");
                buffer.append(monthName);
                buffer.append("\"");
                addExtraAttributes(buffer);
                buffer.append(">");

                int startMonth = 1;
                int endMonth = 12;
                if (startYear == endYear) {
                    startMonth = minDate.get(Calendar.MONTH) + 1;
                    endMonth = maxDate.get(Calendar.MONTH) + 1;
                }
                SimpleDateFormat format = new SimpleDateFormat(pattern, locale);
                Calendar help = Calendar.getInstance();
                if (! required && first) {
                    buffer.append("<option value=\"-1\"");
                    if (checkMonth == -1) {
                        buffer.append(" selected=\"selected\"");
                    }
                    buffer.append(">--</option>");
                }
                for (int i = startMonth; i <= endMonth; i++) {
                    help.set(Calendar.MONTH, i - 1);
                    String month = format.format(help.getTime());
                    if (checkMonth == i) {
                        buffer.append("  <option selected=\"selected\" value=\"" + i + "\">" + month + "</option>\n");
                    } else {
                        buffer.append("  <option value=\"" + i + "\">" + month + "</option>\n");
                    }
                }
                buffer.append("</select>");
                didMonth = true;
                break;
            }
            case 'd': {
                String dayName = prefix(field.getName() + "_day");
                String searchDay =  (String) container.find(tag.getPageContext(), dayName);
                int checkDay;
                if (searchDay == null) {
                    if (cal != null) {
                        checkDay = cal.get(Calendar.DAY_OF_MONTH);
                    } else {
                        checkDay = -1;
                    }
                } else {
                    checkDay = Integer.parseInt(searchDay);
                }
                buffer.append("<select class=\"mm_day\" name=\"" + dayName + "\"");
                addExtraAttributes(buffer);
                buffer.append(">");
                if (! required && first) {
                    buffer.append("<option value=\"-1\"");
                    if (checkDay == -1) {
                        buffer.append(" selected=\"selected\"");
                    }
                    buffer.append(">--</option>");
                }
                for (int i = 1; i <= 31; i++) {
                    if (checkDay == i) {
                        buffer.append("  <option selected=\"selected\" value=\"" + i + "\">");
                        buffer.append(i);
                        buffer.append("</option>\n");
                    } else {
                        buffer.append("  <option value=\"" + i + "\">");
                        buffer.append(i);
                        buffer.append("</option>\n");
                    }
                }
                buffer.append("</select>");
                didDay = true;
                break;
            }
            case 'H': {
                String hourName = prefix(field.getName() + "_hour");
                String searchHour =  (String) container.find(tag.getPageContext(), hourName);
                int checkHour;
                if (searchHour == null) {
                    if (cal != null) {
                        checkHour = cal.get(Calendar.HOUR_OF_DAY);
                    } else {
                        checkHour = -1;
                    }
                } else {
                    checkHour = Integer.parseInt(searchHour);
                }
                buffer.append("<select class=\"mm_hour\" name=\"" + hourName + "\"");
                addExtraAttributes(buffer);
                buffer.append(">");
                if (! required && first) {
                    buffer.append("<option value=\"-1\"");
                    if (checkHour == -1) {
                        buffer.append(" selected=\"selected\"");
                    }
                    buffer.append(">--</option>");
                }
                for (int i = 0; i <= 23; i++) {
                    if (checkHour == i) {
                        buffer.append("  <option selected=\"selected\" value=\"" + i + "\">");
                    } else {
                        buffer.append("  <option value=\"" + i + "\">");
                    }
                    if (i < 10 && pattern.length() > 1) buffer.append("0");
                    buffer.append(i + "</option>\n");
                }
                buffer.append("</select>");
                didHours = true;
                break;
            }
            case 'm': {

                String minuteName = prefix(field.getName() + "_minute");
                String searchMinute =  (String) container.find(tag.getPageContext(), minuteName);
                int checkMinute;
                if (searchMinute == null) {
                    if (cal != null) {
                        checkMinute = cal.get(Calendar.MINUTE);
                    } else {
                        checkMinute = -1;
                    }
                } else {
                    checkMinute = Integer.parseInt(searchMinute);
                }

                buffer.append("<select class=\"mm_minute\" name=\"" + minuteName + "\"");
                addExtraAttributes(buffer);
                buffer.append(">");
                if (! required && first) {
                    buffer.append("<option value=\"-1\"");
                    if (checkMinute == -1) {
                        buffer.append(" selected=\"selected\"");
                    }
                    buffer.append(">--</option>");
                }
                for (int i = 0; i <= 59; i++) {
                    if (checkMinute == i) {
                        buffer.append("  <option selected=\"selected\" value=\"" + i + "\">");
                    } else {
                        buffer.append("  <option value=\"" + i + "\">");
                    }
                    if (i< 10 && pattern.length() > 1) buffer.append("0");
                    buffer.append(i + "</option>\n");
                }
                buffer.append("</select>");
                didMinutes = true;
                break;
            }
            case 's': {
                String secondName = prefix(field.getName() + "_second");
                String searchSecond =  (String) container.find(tag.getPageContext(), secondName);
                int checkSecond;
                if (searchSecond == null) {
                    if (cal != null) {
                        checkSecond = cal.get(Calendar.SECOND);
                    } else {
                        checkSecond = -1;
                    }
                } else {
                    checkSecond = Integer.parseInt(searchSecond);
                }

                buffer.append("<select class=\"mm_second\" name=\"" + secondName + "\"");
                addExtraAttributes(buffer);
                buffer.append(">");
                if (! required && first) {
                    buffer.append("<option value=\"-1\"");
                    if (checkSecond == -1) {
                        buffer.append(" selected=\"selected\"");
                    }
                    buffer.append(">--</option>");
                }
                for (int i = 0; i <= 59; i++) {
                    if (checkSecond == i) {
                        buffer.append("  <option selected=\"selected\" value=\"" + i + "\">");
                    } else {
                        buffer.append("  <option value=\"" + i + "\">");
                    }
                    if (i< 10 && pattern.length() > 1) buffer.append("0");
                    buffer.append(i + "</option>\n");
                }
                buffer.append("</select>");
                didSeconds = true;
                break;
            }
            default:
                log.warn("unknown pattern '" + pattern + "'");
                continue;
            }
            first = false;
        }



        if (! didDay)   buffer.append("<input type=\"hidden\" name=\"" + prefix(field.getName() + "_day") + "\" value=\"1\" />");
        if (! didMonth) buffer.append("<input type=\"hidden\" name=\"" + prefix(field.getName() + "_month") + "\" value=\"1\" />");
        if (! didYear)  buffer.append("<input type=\"hidden\" name=\"" + prefix(field.getName() + "_year") + "\" value=\"1970\" />");


        if (! didHours)   buffer.append("<input type=\"hidden\" name=\"" + prefix(field.getName() + "_hour") + "\" value=\"0\" />");
        if (! didMinutes) buffer.append("<input type=\"hidden\" name=\"" + prefix(field.getName() + "_minute") + "\" value=\"0\" />");
        if (! didSeconds) buffer.append("<input type=\"hidden\" name=\"" + prefix(field.getName() + "_second") + "\" value=\"0\" />");



        buffer.append("</span>");
        return buffer.toString();
    }

    /**
     * Just to override. For example if the date is stored in an int and not in a long, then the year can be checked.
     *
     * @return int
     */

    protected int checkYear(int y, String fieldName) throws JspTagException {
        if (! EXIST_YEAR_0) {
            if (y == 0) throw new JspTagException("The year '0' does not exist and cannot be used for field '" + fieldName + "'");
            if (y < 0) y++; // This makes that year BC 1  =  -1 in stead of BC 1 = 0 (which seems to be the java way)
        }
        return y;
    }

    /**
     * @see TypeHandler#useHtmlInput(Node, Field)
     */
    public boolean useHtmlInput(Node node, Field field) throws JspTagException {

        String fieldName = field.getName();
        try {
            int day    = Integer.parseInt( (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName + "_day")));
            int month  = Integer.parseInt( (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName + "_month")));
            int year   = Integer.parseInt( (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName + "_year")));
            int hour   = Integer.parseInt( (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName + "_hour")));
            int minute = Integer.parseInt( (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName + "_minute")));
            int second = Integer.parseInt( (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName + "_second")));
            Calendar cal ;
            if (day == -1 || month == -1 || year == Integer.MAX_VALUE || hour == -1 || minute == -1 || second == -1) {
                cal = null;
            } else {
                cal = Calendar.getInstance();
                cal.set(checkYear(year, fieldName), month - 1, day, hour, minute, second);
                cal.set(Calendar.MILLISECOND, 0);
            }
            Object oldValue = node.getValue(field.getName());
            if (oldValue != null) oldValue = node.getDateValue(field.getName());
            Date newValue = cal == null ? null : cal.getTime();
            if (oldValue == null) {
                if (newValue != null) {
                    node.setDateValue(fieldName, newValue);
                    return true;
                }
            } else if (!oldValue.equals(newValue)) {
                node.setDateValue(fieldName, newValue);
                return true;
            }
        } catch (java.lang.NumberFormatException e) {
            throw new JspTagException("Not a valid number (" + e.toString() + ") in field " + fieldName);
        }
        return false;
    }

    protected Date getSpecifiedValue(Field field) throws JspTagException {
        String fieldName = field.getName();
        Calendar cal = Calendar.getInstance();
        String input_day    =  (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName + "_day"));
        String input_month  =  (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName + "_month"));
        String input_year   =  (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName + "_year"));
        String input_hour   =  (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName + "_hour"));
        String input_minute =  (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName + "_minute"));
        String input_second =  (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName + "_second"));
        if (input_day == null || input_hour == null) {
            return NODATE;
        }
        try {
            int day    = Integer.parseInt(input_day);
            int month  = Integer.parseInt(input_month);
            int year   = Integer.parseInt(input_year);
            int hour   = Integer.parseInt(input_hour);
            int minute = Integer.parseInt(input_minute);
            int second = Integer.parseInt(input_second);
            cal.set(checkYear(year, fieldName), month - 1, day, hour, minute, second);
        } catch (java.lang.NumberFormatException e) {
            throw new JspTagException("Not a valid number (" + e.toString() + ")");
        }
        return cal.getTime();
    }

    /**
     * @see TypeHandler#whereHtmlInput(Field)
     */
    public String whereHtmlInput(Field field) throws JspTagException {
        String fieldName = field.getName();
        String operator = (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName + "_search"));
        if (operator == null || operator.equals("no")) {
            return null;
        }

        Date timeValue = getSpecifiedValue(field);
        if (timeValue == NODATE) return null;

        String time;
        if (field.getType() == Field.TYPE_DATETIME) {
            time = dateFormat.format(timeValue);
        } else {
            time = "" + Casting.toLong(timeValue);
        }

        if (operator.equals("greater")) {
            return "( [" + fieldName + "] >" + time + ")";
        } else if (operator.equals("smaller")) {
            return "( [" + fieldName + "] <" + time + ")";
        } else if (operator.equals("equal")) {

            String options = tag.getOptions();
            if (options != null && options.indexOf("date") > -1) {
                return "( [" + fieldName + "] >= " + time + "  AND [" + fieldName + "] < " + (time + 24 * 60 * 60) + ")";
            } else {
                return "( [" + fieldName + "] = " + time + ")";
            }
        } else {
            log.warn("Found unknown operator value '" + operator + "'");
            return null;
        }
    }

    public Constraint whereHtmlInput(Field field, Query query) throws JspTagException {
        String fieldName = field.getName();
        String operator = (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName + "_search"));
        if (operator == null || operator.equals("no")) {
            return null;
        }

        Object time = getSpecifiedValue(field);
        if (field.getType() != Field.TYPE_DATETIME) {
            time = new Long(Casting.toLong(time));
        }

        // expand fieldname with nodemanager name if needed
        if (query.getSteps().size() > 1) {
            fieldName = field.getNodeManager().getName()+"."+fieldName;
        }

        Constraint con;
        if (operator.equals("greater")) {
            con = Queries.createConstraint(query, fieldName, FieldCompareConstraint.GREATER, time);
        } else if (operator.equals("less")) {
            con = Queries.createConstraint(query, fieldName, FieldCompareConstraint.LESS, time);
        } else if (operator.equals("equal")) {
            String options = tag.getOptions();
            if (options != null && options.indexOf("date") > -1) {
                Object nextTime;
                if (field.getType() == Field.TYPE_DATETIME) {
                    nextTime = new Date(((Date)time).getTime() + 24 * 60 * 60 * 1000);
                } else {
                    nextTime = new Long(((Long)time).longValue() + 24 * 60 * 60);
                }
                con = Queries.createConstraint(query, fieldName, Queries.OPERATOR_BETWEEN, time, nextTime, false);
            } else {
                con = Queries.createConstraint(query, fieldName, FieldCompareConstraint.EQUAL, time);
            }
        } else {
            log.warn("Found unknown operator value '" + operator + "'");
            return null;
        }
        Queries.addConstraint(query, con);
        return con;

    }

    public void paramHtmlInput(ParamHandler handler, Field field) throws JspTagException  {
        String fieldName = field.getName();
        String operator = (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName + "_search"));
        if (operator == null || operator.equals("no")) {
            return;
        }
        handler.addParameter(prefix(fieldName + "_search"), operator);

        String options = tag.getOptions();
        if (options == null || options.indexOf("time") > -1) {
            String hour    = (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName + "_hour"));
            String minute  = (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName + "_minute"));
            String  second = (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName + "_second"));
            handler.addParameter(prefix(fieldName + "_hour"), hour);
            handler.addParameter(prefix(fieldName + "_minute"), minute);
            handler.addParameter(prefix(fieldName + "_second"), second);
        }
        String day    =  (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName + "_day"));
        String month  =  (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName + "_month"));
        String year   =  (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName + "_year"));
        handler.addParameter(prefix(fieldName + "_day"), day);
        handler.addParameter(prefix(fieldName + "_month"), month);
        handler.addParameter(prefix(fieldName + "_year"), year);
    }
}
