<%-- Vista: listado de cuentas de usuario (solo Administrador). Datos enviados por UsuarioServlet. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="yo" value="${sessionScope.usuarioSesion}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Usuarios | Gestión de Colposcopia</title>
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
                <h1>Usuarios del sistema</h1>
                <p class="migas"><a href="${ctx}/index.jsp">Inicio</a> / Usuarios</p>
            </div>
            <a class="btn btn-primario" href="${ctx}/usuarios?accion=nuevo">
                <i class="fa-solid fa-plus"></i> Nuevo usuario</a>
        </header>

        <%@include file="/WEB-INF/vistas/comun/mensajes.jspf" %>

        <section class="panel">
            <form class="buscador" action="${ctx}/usuarios" method="get">
                <label class="campo-icono">
                    <i class="fa-solid fa-magnifying-glass"></i>
                    <input type="search" name="q" value="${fn:escapeXml(filtro)}"
                           placeholder="Nombre de usuario" aria-label="Buscar usuario">
                </label>
                <button type="submit" class="btn btn-secundario">Buscar</button>
                <c:if test="${not empty filtro}">
                    <a class="btn btn-texto" href="${ctx}/usuarios">Quitar filtro</a>
                </c:if>
            </form>

            <div class="tabla-desplazable">
                <table class="tabla">
                    <thead>
                    <tr>
                        <th>Usuario</th>
                        <th>Rol</th>
                        <th>Estado</th>
                        <th class="col-acciones">Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="u" items="${usuarios}">
                        <c:set var="esYo" value="${u.idUsuario eq yo.idUsuario}"/>
                        <tr class="${u.activo ? '' : 'fila-inactiva'}">
                            <td>
                                <c:out value="${u.usuario}"/>
                                <c:if test="${esYo}"><span class="nota">(usted)</span></c:if>
                            </td>
                            <td><span class="sigla">${u.rolTexto}</span></td>
                            <td>
                                <span class="estado ${u.activo ? 'estado-atendida' : 'estado-cancelada'}">
                                    ${u.activo ? 'Activo' : 'Inactivo'}</span>
                            </td>
                            <td class="col-acciones">
                                <a class="icono icono-editar" title="Editar"
                                   href="${ctx}/usuarios?accion=editar&amp;id=${u.idUsuario}">
                                    <i class="fa-solid fa-pen"></i></a>
                                <c:if test="${not esYo}">
                                    <form action="${ctx}/usuarios" method="post" class="en-linea">
                                        <input type="hidden" name="accion" value="cambiarEstado">
                                        <input type="hidden" name="id" value="${u.idUsuario}">
                                        <button type="submit" class="icono icono-estado"
                                                title="${u.activo ? 'Inactivar' : 'Activar'}">
                                            <i class="fa-solid ${u.activo ? 'fa-user-lock' : 'fa-user-check'}"></i></button>
                                    </form>
                                    <form action="${ctx}/usuarios" method="post" class="en-linea"
                                          data-confirmar="¿Eliminar la cuenta ${fn:escapeXml(u.usuario)}? Si solo quiere quitarle el acceso, inactívela."
                                          onsubmit="return confirm(this.dataset.confirmar);">
                                        <input type="hidden" name="accion" value="eliminar">
                                        <input type="hidden" name="id" value="${u.idUsuario}">
                                        <button type="submit" class="icono icono-eliminar" title="Eliminar">
                                            <i class="fa-solid fa-trash"></i></button>
                                    </form>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty usuarios}">
                        <tr>
                            <td colspan="4" class="vacio">
                                Ningún usuario coincide con «<c:out value="${filtro}"/>».
                            </td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
            <p class="pie-tabla">${fn:length(usuarios)} usuario(s)</p>
        </section>
    </main>
</div>
</body>
</html>
