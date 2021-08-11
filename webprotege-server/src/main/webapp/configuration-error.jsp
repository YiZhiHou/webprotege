<%@ page import="edu.stanford.bmir.protege.web.server.init.WebProtegeConfigurationException" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.StringWriter" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true"%>
<html>
<head>
    <title>Synyi知识图谱管理系统-错误页</title>
</head>
<body style="font-family: arial,sans-serif; color: #464646; font-size: 14px;">
    <h1>错误</h1>
    <p>系统发生了错误，请尝试重新打开首页
    </p>
    <h2>错误消息</h2>
    <div style="color: #af0002;">
        <%
            if(exception instanceof WebProtegeConfigurationException) {
                final String message = exception.getMessage();
                out.println(message.replace("\n", "<br>"));
            }
            else {
                StringWriter sw = new StringWriter();
                sw.append("<pre>");
                exception.printStackTrace(new PrintWriter(sw));
                sw.append("</pre>");
                out.println(sw.toString().replace("\n", "<br>"));
            }

        %>
    </div>
</body>
</html>