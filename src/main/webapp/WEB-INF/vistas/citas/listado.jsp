<%-- Vista: agenda de citas con filtros por GET y cambios de estado por POST. Datos de CitaServlet. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="puedeAgendar" value="${sessionScope.usuarioSesion.puedeAgendar}"/>
<c:set var="hayFiltros" value="${not empty fecha or historico or not empty estadoFiltro or not empty filtro}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Citas | Gestión de Colposcopia</title>
    <%@include file="/WEB-INF/vistas/comun/head.jspf" %>
</head>
<body>
<div class="app">
    <jsp:include page="/WEB-INF/vistas/comun/menu.jsp">
        <jsp:param name="activo" value="citas"/>
    </jsp:include>

    <main class="contenido">
        <header class="cabecera">
            <div>
                <h1>Agenda de citas</h1>
                <p class="migas"><a href="${ctx}/index.jsp">Inicio</a> / Citas</p>
            </div>
            <c:if test="${puedeAgendar}">
                <a class="btn btn-primario" href="${ctx}/citas?accion=nuevo${not empty fecha ? '&amp;fecha=' += fecha : ''}">
                    <i class="fa-solid fa-plus"></i> Programar cita</a>
            </c:if>
        </header>

        <%@include file="/WEB-INF/vistas/comun/mensajes.jspf" %>

        <section class="panel">
            <%-- Filtros por GET: quedan en la URL y la agenda filtrada se puede recargar o compartir --%>
            <form class="buscador filtros" action="${ctx}/citas" method="get">
                <div class="campo">
                    <label for="fecha">Día</label>
                    <input id="fecha" type="date" name="fecha" value="${fecha}">
                </div>
                <div class="campo">
                    <label for="estado">Estado</label>
                    <select id="estado" name="estado">
                        <option value="">Todos</option>
                        <c:forEach var="e" items="${estados}">
                            <option value="${e}" ${e eq estadoFiltro ? 'selected' : ''}>${e eq 'No asistio' ? 'No asistió' : e}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="campo campo-ancho">
                    <label for="q">Paciente</label>
                    <input id="q" type="search" name="q" value="${fn:escapeXml(filtro)}" placeholder="Documento, nombres o apellidos">
                </div>
                <label class="casilla">
                    <input type="checkbox" name="historico" value="1" ${historico ? 'checked' : ''}> Incluir citas pasadas
                </label>
                <div class="filtros-botones">
                    <button type="submit" class="btn btn-secundario">Filtrar</button>
                    <a class="btn btn-texto" href="${ctx}/citas?fecha=${hoy}">Hoy</a>
                    <c:if test="${hayFiltros}">
                        <a class="btn btn-texto" href="${ctx}/citas">Quitar filtros</a>
                    </c:if>
                </div>
            </form>

            <p class="resumen-agenda">
                <c:choose>
                    <c:when test="${not empty fecha}">Citas del día seleccionado</c:when>
                    <c:when test="${historico}">Todas las citas registradas</c:when>
                    <c:otherwise>Próximas citas, desde hoy</c:otherwise>
                </c:choose>
            </p>

            <div class="tabla-desplazable">
                <table class="tabla">
                    <thead>
                    <tr>
                        <th>Fecha</th>
                        <th>Hora</th>
                        <th>Paciente</th>
                        <th>Médico</th>
                        <th>Estado</th>
                        <th class="col-acciones">Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="cita" items="${citas}">
                        <tr class="${cita.fecha eq hoy ? 'fila-hoy' : ''}">
                            <td class="sin-salto">${cita.fechaTexto}</td>
                            <td class="sin-salto"><strong>${cita.horaTexto}</strong></td>
                            <td>
                                <c:out value="${cita.nombrePaciente}"/>
                                <small class="dato-secundario"><c:out value="${cita.documentoPaciente}"/></small>
                            </td>
                            <td><c:out value="${cita.nombreMedico}"/></td>
                            <td><span class="estado estado-${cita.estadoClase}">${cita.estadoTexto}</span></td>
                            <td class="col-acciones">
                                <c:choose>
                                    <c:when test="${cita.programada}">
                                        <c:if test="${not cita.futura}">
                                            <form action="${ctx}/citas" method="post" class="en-linea">
                                                <input type="hidden" name="accion" value="cambiarEstado">
                                                <input type="hidden" name="id" value="${cita.idCita}">
                                                <input type="hidden" name="estado" value="Atendida">
                                                <input type="hidden" name="retorno" value="${fn:escapeXml(consulta)}">
                                                <button type="submit" class="icono icono-atender" title="Marcar como atendida">
                                                    <i class="fa-solid fa-check"></i></button>
                                            </form>
                                            <form action="${ctx}/citas" method="post" class="en-linea">
                                                <input type="hidden" name="accion" value="cambiarEstado">
                                                <input type="hidden" name="id" value="${cita.idCita}">
                                                <input type="hidden" name="estado" value="No asistio">
                                                <input type="hidden" name="retorno" value="${fn:escapeXml(consulta)}">
                                                <button type="submit" class="icono icono-estado" title="Marcar inasistencia">
                                                    <i class="fa-solid fa-user-xmark"></i></button>
                                            </form>
                                        </c:if>
                                        <form action="${ctx}/citas" method="post" class="en-linea"
                                              onsubmit="return confirm('¿Cancelar esta cita?');">
                                            <input type="hidden" name="accion" value="cambiarEstado">
                                            <input type="hidden" name="id" value="${cita.idCita}">
                                            <input type="hidden" name="estado" value="Cancelada">
                                            <input type="hidden" name="retorno" value="${fn:escapeXml(consulta)}">
                                            <button type="submit" class="icono icono-eliminar" title="Cancelar cita">
                                                <i class="fa-solid fa-ban"></i></button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <form action="${ctx}/citas" method="post" class="en-linea">
                                            <input type="hidden" name="accion" value="cambiarEstado">
                                            <input type="hidden" name="id" value="${cita.idCita}">
                                            <input type="hidden" name="estado" value="Programada">
                                            <input type="hidden" name="retorno" value="${fn:escapeXml(consulta)}">
                                            <button type="submit" class="icono icono-editar" title="Volver a programada">
                                                <i class="fa-solid fa-rotate-left"></i></button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>

                                <c:if test="${puedeAgendar}">
                                    <a class="icono icono-editar" title="Editar"
                                       href="${ctx}/citas?accion=editar&amp;id=${cita.idCita}">
                                        <i class="fa-solid fa-pen"></i></a>
                                    <form action="${ctx}/citas" method="post" class="en-linea"
                                          data-confirmar="¿Eliminar la cita de ${fn:escapeXml(cita.nombrePaciente)}? Si la paciente no vendrá, es mejor cancelarla."
                                          onsubmit="return confirm(this.dataset.confirmar);">
                                        <input type="hidden" name="accion" value="eliminar">
                                        <input type="hidden" name="id" value="${cita.idCita}">
                                        <input type="hidden" name="retorno" value="${fn:escapeXml(consulta)}">
                                        <button type="submit" class="icono icono-eliminar" title="Eliminar">
                                            <i class="fa-solid fa-trash"></i></button>
                                    </form>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty citas}">
                        <tr>
                            <td colspan="6" class="vacio">
                                <c:choose>
                                    <c:when test="${hayFiltros}">No hay citas con estos filtros. Cambie el día o quite los filtros.</c:when>
                                    <c:when test="${puedeAgendar}">No hay citas próximas. Use «Programar cita» para agendar la primera.</c:when>
                                    <c:otherwise>No hay citas próximas.</c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
            <p class="pie-tabla">${fn:length(citas)} cita(s)</p>
        </section>
    </main>
</div>
</body>
</html>
