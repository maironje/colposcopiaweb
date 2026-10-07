<%-- Vista: crear y editar cuentas de usuario. Se envía por POST a UsuarioServlet. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="titulo" value="${usuario.nuevo ? 'Nuevo usuario' : 'Editar usuario'}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>${titulo} | Gestión de Colposcopia</title>
    <%@include file="/WEB-INF/vistas/comun/head.jspf" %>
</head>
<body>
<div class="app">
    <jsp:include page="/WEB-INF/vistas/comun/menu.jsp">
        <jsp:param name="activo" value="usuarios"/>
    </jsp:include>

    <main class="contenido">
        <header class="cabecera">
            <div>
                <h1>${titulo}</h1>
                <p class="migas">
                    <a href="${ctx}/index.jsp">Inicio</a> /
                    <a href="${ctx}/usuarios">Usuarios</a> /
                    ${usuario.nuevo ? 'Nuevo' : 'Editar'}
                </p>
            </div>
            <a class="btn btn-contorno" href="${ctx}/usuarios">
                <i class="fa-solid fa-arrow-left"></i> Volver al listado</a>
        </header>

        <%@include file="/WEB-INF/vistas/comun/errores.jspf" %>

        <form class="panel formulario" action="${ctx}/usuarios" method="post" autocomplete="off">
            <input type="hidden" name="accion" value="guardar">
            <input type="hidden" name="idUsuario" value="${usuario.idUsuario}">

            <h2 class="titulo-seccion">Datos de acceso</h2>
            <div class="rejilla">
                <div class="campo">
                    <label for="usuario">Nombre de usuario *</label>
                    <input id="usuario" name="usuario" maxlength="50" required pattern="[A-Za-z0-9._\-]{4,50}"
                           value="${fn:escapeXml(usuario.usuario)}" placeholder="p. ej. laura.gomez">
                    <small class="ayuda">Entre 4 y 50 caracteres, sin espacios.</small>
                </div>
                <div class="campo">
                    <label for="rol">Rol *</label>
                    <select id="rol" name="rol" required>
                        <option value="">Seleccione…</option>
                        <c:forEach var="r" items="${roles}">
                            <option value="${r}" ${r eq usuario.rol ? 'selected' : ''}>${r eq 'Medico' ? 'Médico' : r}</option>
                        </c:forEach>
                    </select>
                    <small class="ayuda">Administrador: todo el sistema. Auxiliar: pacientes y agenda. Médico: consulta la agenda y registra la atención.</small>
                </div>
                <div class="campo">
                    <label for="clave">${usuario.nuevo ? 'Contraseña *' : 'Nueva contraseña'}</label>
                    <input id="clave" name="clave" type="password" autocomplete="new-password"
                           minlength="${minClave}" maxlength="64" ${usuario.nuevo ? 'required' : ''}>
                    <small class="ayuda">
                        Mínimo ${minClave} caracteres con letras y números.
                        <c:if test="${not usuario.nuevo}">Déjela vacía para conservar la actual.</c:if>
                    </small>
                </div>
                <div class="campo">
                    <label for="confirmacion">Confirmar contraseña${usuario.nuevo ? ' *' : ''}</label>
                    <input id="confirmacion" name="confirmacion" type="password" autocomplete="new-password"
                           maxlength="64" ${usuario.nuevo ? 'required' : ''}>
                </div>
                <label class="casilla">
                    <input type="checkbox" name="activo" value="1" ${usuario.activo ? 'checked' : ''}>
                    Cuenta activa (puede iniciar sesión)
                </label>
            </div>

            <div class="acciones-formulario">
                <a class="btn btn-contorno" href="${ctx}/usuarios">Cancelar</a>
                <button type="submit" class="btn btn-primario">
                    <i class="fa-solid fa-floppy-disk"></i>
                    ${usuario.nuevo ? 'Crear usuario' : 'Guardar cambios'}
                </button>
            </div>
        </form>
    </main>
</div>
</body>
</html>
