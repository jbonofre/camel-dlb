package org.apache.camel.poc.dlb.console;

public class Template {

    public static String header() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        buffer.append("<head>");
        buffer.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />");
        buffer.append("<title>DLB Console</title>");
        buffer.append("<link rel=\"stylesheet\" href=\"css/core.css\" type=\"text/css\" />");
        buffer.append("<script type=\"text/javascript\" src=\"js/jquery.js\"></script>");
        buffer.append("<script type=\"text/javascript\" src=\"js/core.js\"></script>");
        buffer.append("</head>");
        buffer.append("<body>");
        buffer.append("<div id=\"header_bc\">");
        buffer.append("<div>");
        buffer.append("<div>");
        buffer.append("<h1>DLB CONSOLE</h1>");
        buffer.append("</div>");
        buffer.append("</div>");
        buffer.append("</div>");
        return buffer.toString();
    }

    public static String footer() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("</body>\n");
        buffer.append("</html>\n");
        return buffer.toString();
    }

}
