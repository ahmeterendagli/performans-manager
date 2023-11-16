package com.globalpbx.performansmanager.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class FormatterUtil {

    public static String generateHTMLTable(String[] headers, String[][] data, String dangerousValue) {
        StringBuilder htmlTable = new StringBuilder();
        htmlTable.append("<html>\n" +
                "<head>\n" +
                "    <title>Basit HTML Tablo</title>\n" +
                "    <style>\n" +
                "        th { background-color: lightblue; }\n" +
                "   .red { background-color: #FF5252; } \n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>");
        htmlTable.append("\n<table border='1'>\n<tr>");

        for (String header : headers) {
            htmlTable.append("<th>").append(header).append("</th>");
        }
        htmlTable.append("</tr>");

        for (String[] row : data) {
            htmlTable.append("<tr>");
            for (int i = 0; i < row.length; i++) {
                String cell = row[i];
                if (headers[i].trim().equals(dangerousValue)) {
                    htmlTable.append("<td class='red'>").append(cell).append("</td>");
                } else {
                    htmlTable.append("<td>").append(cell).append("</td>");
                }
            }
            htmlTable.append("</tr>");
        }

        htmlTable.append("</table>");
        htmlTable.append("\n" +
                "</body>\n" +
                "</html>");
        return htmlTable.toString();
    }

    public static String formatFileSize(long size) {
        double kiloByteSize = (double) size / 1024;
        double megaByteSize = kiloByteSize / 1024;

        if (megaByteSize > 1) {
            return String.format("%.2f MB", megaByteSize);
        } else {
            return String.format("%.2f KB", kiloByteSize);
        }
    }

    public static String formatWithDecimal(double value) {
        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);

        String formattedCpuUsage = df.format(value);
        return formattedCpuUsage;
    }
}
