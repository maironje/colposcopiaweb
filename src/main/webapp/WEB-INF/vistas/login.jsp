<%-- Vista: inicio de sesión. Es la única página visible sin sesión (ver AutenticacionFiltro). --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Iniciar sesión | Gestión de Colposcopia</title>
    <%@include file="/WEB-INF/vistas/comun/head.jspf" %>
</head>
<body class="pagina-login">
<div class="login">
    <section class="login-marca">
        <span class="logo logo-grande" aria-hidden="true"><i></i><i></i><i></i></span>
        <p class="login-ips"><small>I.P.S.</small> ENDOFERTIL <small>S.A.S.</small></p>
        <h1>Gestión de colposcopia</h1>
        <p>Agenda, pacientes y procedimientos en un solo lugar.</p>
    </section>

    <section class="login-formulario">
        <h2>Iniciar sesión</h2>

        <c:if test="${param.salida eq '1'}">
            <div class="alerta alerta-exito" role="status">
                <i class="fa-solid fa-circle-check"></i> Sesión cerrada.
            </div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="alerta alerta-error" role="alert">
                <i class="fa-solid fa-circle-exclamation"></i> <c:out value="${error}"/>
            </div>
        </c:if>

        <form action="${ctx}/login" method="post">
            <div class="campo">
                <label for="usuario">Usuario</label>
                <input id="usuario" name="usuario" autocomplete="username" required autofocus
                       maxlength="50" value="${fn:escapeXml(usuarioDigitado)}">
            </div>
            <div class="campo">
                <label for="clave">Contraseña</label>
                <input id="clave" name="clave" type="password" autocomplete="current-password" required>
            </div>
            <button type="submit" class="btn btn-primario btn-bloque">
                <i class="fa-solid fa-right-to-bracket"></i> Ingresar
            </button>
        </form>
    </section>
</div>
</body>
</html>
