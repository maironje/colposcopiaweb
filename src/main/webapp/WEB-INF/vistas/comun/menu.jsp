<%--
    Menú lateral. Se incluye de forma dinámica con <jsp:include> y recibe
    el parámetro "activo" (<jsp:param>) para resaltar la opción actual.
    Muestra el usuario de la sesión y oculta Usuarios a quien no es administrador.
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="u" value="${sessionScope.usuarioSesion}"/>
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
        <a href="${ctx}/citas" class="${param.activo eq 'citas' ? 'activo' : ''}">
            <i class="fa-regular fa-calendar"></i> Citas</a>
        <span class="pendiente" title="Módulo en construcción"><i class="fa-solid fa-microscope"></i> Colposcopias</span>
        <span class="pendiente" title="Módulo en construcción"><i class="fa-solid fa-notes-medical"></i> Diagnósticos</span>
        <span class="pendiente" title="Módulo en construcción"><i class="fa-solid fa-chart-column"></i> Reportes</span>
        <c:if test="${u.administrador}">
            <a href="${ctx}/usuarios" class="${param.activo eq 'usuarios' ? 'activo' : ''}">
                <i class="fa-solid fa-user-shield"></i> Usuarios</a>
        </c:if>
    </nav>

    <c:if test="${not empty u}">
        <div class="sesion">
            <span class="sesion-avatar" aria-hidden="true">${fn:toUpperCase(fn:substring(u.usuario, 0, 1))}</span>
            <span class="sesion-datos">
                <strong><c:out value="${u.usuario}"/></strong>
                <small>${u.rolTexto}</small>
            </span>
            <form action="${ctx}/salir" method="post">
                <button type="submit" class="sesion-salir" title="Cerrar sesión" aria-label="Cerrar sesión">
                    <i class="fa-solid fa-arrow-right-from-bracket"></i></button>
            </form>
        </div>
    </c:if>
</aside>
