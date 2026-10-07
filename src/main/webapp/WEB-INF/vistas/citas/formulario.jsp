<%-- Vista: programar y editar citas. Se envía por POST a CitaServlet. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="titulo" value="${cita.nueva ? 'Programar cita' : 'Editar cita'}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>${titulo} | Gestión de Colposcopia</title>
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
                <h1>${titulo}</h1>
                <p class="migas">
                    <a href="${ctx}/index.jsp">Inicio</a> /
                    <a href="${ctx}/citas">Citas</a> /
                    ${cita.nueva ? 'Nueva' : 'Editar'}
                </p>
            </div>
            <a class="btn btn-contorno" href="${ctx}/citas">
                <i class="fa-solid fa-arrow-left"></i> Volver a la agenda</a>
        </header>

        <%@include file="/WEB-INF/vistas/comun/errores.jspf" %>

        <c:if test="${empty medicos}">
            <div class="alerta alerta-error" role="alert">
                <i class="fa-solid fa-circle-exclamation"></i>
                No hay médicos registrados. Registre al menos un médico en la base de datos antes de programar citas.
            </div>
        </c:if>

        <form class="panel formulario" action="${ctx}/citas" method="post">
            <input type="hidden" name="accion" value="guardar">
            <input type="hidden" name="idCita" value="${cita.idCita}">

            <h2 class="titulo-seccion">Datos de la cita</h2>
            <div class="rejilla">
                <div class="campo">
                    <label for="idPaciente">Paciente *</label>
                    <select id="idPaciente" name="idPaciente" required>
                        <option value="">Seleccione…</option>
                        <c:forEach var="p" items="${pacientes}">
                            <option value="${p.idPaciente}" ${p.idPaciente eq cita.idPaciente ? 'selected' : ''}>
                                <c:out value="${p.apellidos} ${p.nombres} · ${p.tipoDocumento} ${p.numeroDocumento}"/>
                            </option>
                        </c:forEach>
                    </select>
                    <small class="ayuda">¿No aparece? <a href="${ctx}/pacientes?accion=nuevo">Registre la paciente</a> primero.</small>
                </div>
                <div class="campo">
                    <label for="idMedico">Médico *</label>
                    <select id="idMedico" name="idMedico" required>
                        <option value="">Seleccione…</option>
                        <c:forEach var="m" items="${medicos}">
                            <option value="${m.idMedico}" ${m.idMedico eq cita.idMedico ? 'selected' : ''}>
                                <c:out value="${m.nombreCompleto} (${m.especialidad})"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="campo">
                    <label for="fecha">Fecha *</label>
                    <%-- Una cita nueva no puede quedar en el pasado; el servlet lo vuelve a validar --%>
                    <input id="fecha" name="fecha" type="date" required value="${cita.fecha}"
                           min="${cita.nueva ? hoy : ''}">
                </div>
                <div class="campo">
                    <label for="hora">Hora *</label>
                    <input id="hora" name="hora" type="time" required step="900"
                           min="${horaMin}" max="${horaMax}" value="${cita.horaCampo}">
                    <small class="ayuda">Atención de 7:00 a. m. a 5:30 p. m., citas cada 30 minutos por médico.</small>
                </div>
                <c:if test="${not cita.nueva}">
                    <div class="campo">
                        <label for="estado">Estado *</label>
                        <select id="estado" name="estado" required>
                            <c:forEach var="e" items="${estados}">
                                <option value="${e}" ${e eq cita.estado ? 'selected' : ''}>${e eq 'No asistio' ? 'No asistió' : e}</option>
                            </c:forEach>
                        </select>
                    </div>
                </c:if>
            </div>

            <div class="acciones-formulario">
                <a class="btn btn-contorno" href="${ctx}/citas">Cancelar</a>
                <button type="submit" class="btn btn-primario" ${empty medicos ? 'disabled' : ''}>
                    <i class="fa-solid fa-floppy-disk"></i>
                    ${cita.nueva ? 'Programar cita' : 'Guardar cambios'}
                </button>
            </div>
        </form>
    </main>
</div>
</body>
</html>
