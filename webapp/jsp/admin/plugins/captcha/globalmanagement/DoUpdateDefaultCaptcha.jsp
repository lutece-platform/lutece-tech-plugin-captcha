<%@ page errorPage="../../../ErrorPage.jsp" %>
<jsp:useBean id="captchaManagement" scope="session" class="fr.paris.lutece.plugins.captcha.web.CaptchaLutecePanelJspBean" />

<%
	response.sendRedirect( captchaManagement.updateDefaultCaptcha( request ) );
%>