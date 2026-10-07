<%--
    Menú lateral. Se incluye de forma dinámica con <jsp:include> y recibe
    el parámetro "activo" (<jsp:param>) para resaltar la opción actual.
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<aside class="menu">
    <a class="marca" href="${ctx}/index.jsp">
        <span class="logo" aria-hidden="true"><i></i><i></i><i></i></span>
        <span class="marca-texto"><small>I.P.S.</small> ENDOFERTIL <small>S.A.S.</small></span>
    </a>
    <nav>
        <a href="${ctx}/index.jsp" class="${param.activo eq 'inicio' ? 'activo' : ''}">
            <i class="fa-solid fa-house"></i> Inicio</a>
        <a href="${ctx}/pacientes" class="${param.activo eq 'pacientes' ? 'activo' : ''}">
            <i class="fa-solid fa-user-group"></i> Pacientes</a>
        <span class="pendiente" title="Módulo en construcción"><i class="fa-regular fa-calendar"></i> Citas</span>
        <span class="pendiente" title="Módulo en construcción"><i class="fa-solid fa-microscope"></i> Colposcopias</span>
        <span class="pendiente" title="Módulo en construcción"><i class="fa-solid fa-notes-medical"></i> Diagnósticos</span>
        <span class="pendiente" title="Módulo en construcción"><i class="fa-solid fa-chart-column"></i> Reportes</span>
        <span class="pendiente" title="Módulo en construcción"><i class="fa-solid fa-user-shield"></i> Usuarios</span>
    </nav>
</aside>
