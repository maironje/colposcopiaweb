<%-- Vista: registro y edición de paciente (Mockup 3). Se envía por POST a PacienteServlet. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="titulo" value="${paciente.nueva ? 'Registro de paciente' : 'Editar paciente'}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>${titulo} | Gestión de Colposcopia</title>
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
                <h1>${titulo}</h1>
                <p class="migas">
                    <a href="${ctx}/index.jsp">Inicio</a> /
                    <a href="${ctx}/pacientes">Pacientes</a> /
                    ${paciente.nueva ? 'Nueva' : 'Editar'}
                </p>
            </div>
            <a class="btn btn-contorno" href="${ctx}/pacientes">
                <i class="fa-solid fa-arrow-left"></i> Volver al listado</a>
        </header>

        <c:if test="${not empty errores}">
            <div class="alerta alerta-error" role="alert">
                <i class="fa-solid fa-circle-exclamation"></i>
                <div>
                    <strong>Revise los siguientes datos:</strong>
                    <ul>
                        <c:forEach var="error" items="${errores}">
                            <li><c:out value="${error}"/></li>
                        </c:forEach>
                    </ul>
                </div>
            </div>
        </c:if>

        <%-- Formulario HTML con método POST: los datos viajan en el cuerpo de la petición --%>
        <form class="panel formulario" action="${ctx}/pacientes" method="post">
            <input type="hidden" name="accion" value="guardar">
            <input type="hidden" name="idPaciente" value="${paciente.idPaciente}">

            <h2 class="titulo-seccion">Información personal</h2>
            <div class="rejilla">
                <div class="campo">
                    <label for="tipoDocumento">Tipo de documento *</label>
                    <select id="tipoDocumento" name="tipoDocumento" required>
                        <option value="">Seleccione…</option>
                        <c:forEach var="tipo" items="${tiposDocumento}">
                            <option value="${tipo.key}" ${tipo.key eq paciente.tipoDocumento ? 'selected' : ''}>${tipo.value}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="campo">
                    <label for="numeroDocumento">Número de documento *</label>
                    <input id="numeroDocumento" name="numeroDocumento" maxlength="20" required
                           value="${fn:escapeXml(paciente.numeroDocumento)}" placeholder="Sin puntos ni espacios">
                </div>
                <div class="campo">
                    <label for="nombres">Nombres *</label>
                    <input id="nombres" name="nombres" maxlength="60" required
                           value="${fn:escapeXml(paciente.nombres)}">
                </div>
                <div class="campo">
                    <label for="apellidos">Apellidos *</label>
                    <input id="apellidos" name="apellidos" maxlength="60" required
                           value="${fn:escapeXml(paciente.apellidos)}">
                </div>
                <div class="campo">
                    <label for="fechaNacimiento">Fecha de nacimiento *</label>
                    <%-- Expresión JSP: la fecha máxima permitida es el día de hoy --%>
                    <input id="fechaNacimiento" name="fechaNacimiento" type="date" required
                           max="<%= java.time.LocalDate.now() %>" value="${paciente.fechaNacimiento}">
                </div>
                <div class="campo">
                    <label for="telefono">Teléfono</label>
                    <input id="telefono" name="telefono" type="tel" maxlength="15" inputmode="numeric"
                           value="${fn:escapeXml(paciente.telefono)}">
                </div>
                <div class="campo">
                    <label for="correo">Correo electrónico</label>
                    <input id="correo" name="correo" type="email" maxlength="100"
                           value="${fn:escapeXml(paciente.correo)}">
                </div>
                <div class="campo">
                    <label for="direccion">Dirección</label>
                    <input id="direccion" name="direccion" maxlength="150"
                           value="${fn:escapeXml(paciente.direccion)}">
                </div>
                <div class="campo">
                    <label for="eps">EPS</label>
                    <select id="eps" name="eps">
                        <option value="">Seleccione…</option>
                        <c:forEach var="nombreEps" items="${listaEps}">
                            <option ${nombreEps eq paciente.eps ? 'selected' : ''}>${nombreEps}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="acciones-formulario">
                <a class="btn btn-contorno" href="${ctx}/pacientes">Cancelar</a>
                <button type="submit" class="btn btn-primario">
                    <i class="fa-solid fa-floppy-disk"></i>
                    ${paciente.nueva ? 'Guardar paciente' : 'Guardar cambios'}
                </button>
            </div>
        </form>
    </main>
</div>
</body>
</html>
