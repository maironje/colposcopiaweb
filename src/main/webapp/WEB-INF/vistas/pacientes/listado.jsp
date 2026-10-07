<%-- Vista: listado y búsqueda de pacientes (Mockup 4). Datos enviados por PacienteServlet. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Pacientes | Gestión de Colposcopia</title>
    <%@include file="/WEB-INF/vistas/comun/head.jspf" %>
</head>
<body>
<div class="app">
    <jsp:include page="/WEB-INF/vistas/comun/menu.jsp">
        <jsp:param name="activo" value="pacientes"/>
    </jsp:include>

    <main class="contenido">
        <header class="cabecera">
            <div>
                <h1>Listado de pacientes</h1>
                <p class="migas"><a href="${ctx}/index.jsp">Inicio</a> / Pacientes</p>
            </div>
            <a class="btn btn-primario" href="${ctx}/pacientes?accion=nuevo">
                <i class="fa-solid fa-plus"></i> Nueva paciente</a>
        </header>

        <%@include file="/WEB-INF/vistas/comun/mensajes.jspf" %>

        <section class="panel">
            <%-- Formulario con método GET: el filtro viaja en la URL y la búsqueda se puede compartir --%>
            <form class="buscador" action="${ctx}/pacientes" method="get">
                <input type="hidden" name="accion" value="listar">
                <label class="campo-icono">
                    <i class="fa-solid fa-magnifying-glass"></i>
                    <input type="search" name="q" value="${fn:escapeXml(filtro)}"
                           placeholder="Documento, nombres o apellidos" aria-label="Buscar paciente">
                </label>
                <button type="submit" class="btn btn-secundario">Buscar</button>
                <c:if test="${not empty filtro}">
                    <a class="btn btn-texto" href="${ctx}/pacientes">Quitar filtro</a>
                </c:if>
            </form>

            <div class="tabla-desplazable">
                <table class="tabla">
                    <thead>
                    <tr>
                        <th>Documento</th>
                        <th>Nombres</th>
                        <th>Apellidos</th>
                        <th>Edad</th>
                        <th>Teléfono</th>
                        <th>EPS</th>
                        <th class="col-acciones">Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="p" items="${pacientes}">
                        <tr>
                            <td>
                                <span class="sigla" title="${tiposDocumento[p.tipoDocumento]}">${p.tipoDocumento}</span>
                                <c:out value="${p.numeroDocumento}"/>
                            </td>
                            <td><c:out value="${p.nombres}"/></td>
                            <td><c:out value="${p.apellidos}"/></td>
                            <td>${p.edad} años</td>
                            <td><c:out value="${p.telefono}" default="—"/></td>
                            <td><c:out value="${p.eps}" default="—"/></td>
                            <td class="col-acciones">
                                <a class="icono icono-editar" title="Editar"
                                   href="${ctx}/pacientes?accion=editar&amp;id=${p.idPaciente}">
                                    <i class="fa-solid fa-pen"></i></a>
                                <%-- Eliminar modifica datos, por eso usa POST y no un enlace GET --%>
                                <form action="${ctx}/pacientes" method="post" class="en-linea"
                                      data-confirmar="¿Eliminar a ${fn:escapeXml(p.nombreCompleto)}? Esta acción no se puede deshacer."
                                      onsubmit="return confirm(this.dataset.confirmar);">
                                    <input type="hidden" name="accion" value="eliminar">
                                    <input type="hidden" name="id" value="${p.idPaciente}">
                                    <button type="submit" class="icono icono-eliminar" title="Eliminar">
                                        <i class="fa-solid fa-trash"></i></button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty pacientes}">
                        <tr>
                            <td colspan="7" class="vacio">
                                <c:choose>
                                    <c:when test="${not empty filtro}">
                                        Ninguna paciente coincide con «<c:out value="${filtro}"/>». Revise el texto o quite el filtro.
                                    </c:when>
                                    <c:otherwise>
                                        Aún no hay pacientes registradas. Use «Nueva paciente» para agregar la primera.
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
            <p class="pie-tabla">${fn:length(pacientes)} paciente(s)</p>
        </section>
    </main>
</div>
</body>
</html>
