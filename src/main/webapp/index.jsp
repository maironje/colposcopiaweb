<%--
    Página de inicio. Además de JSTL, muestra elementos clásicos de JSP:
    directivas, declaración (<%! %>), expresión (<%= %>) e include dinámico.
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.time.LocalTime, java.time.LocalDate, java.time.format.DateTimeFormatter, java.util.Locale"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%!
    /** Declaración JSP: método disponible en toda la página. */
    private String saludo() {
        int hora = LocalTime.now().getHour();
        if (hora < 12) {
            return "Buenos días";
        }
        return hora < 19 ? "Buenas tardes" : "Buenas noches";
    }
%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="u" value="${sessionScope.usuarioSesion}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Inicio | Gestión de Colposcopia</title>
    <%@include file="/WEB-INF/vistas/comun/head.jspf" %>
</head>
<body>
<div class="app">
    <jsp:include page="/WEB-INF/vistas/comun/menu.jsp">
        <jsp:param name="activo" value="inicio"/>
    </jsp:include>

    <main class="contenido">
        <header class="cabecera">
            <div>
                <h1><%= saludo() %>, <c:out value="${u.usuario}"/></h1>
                <p class="subtitulo">
                    <%= LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy",
                            new Locale("es", "CO"))) %>
                </p>
            </div>
        </header>

        <section class="modulos">
            <a class="modulo" href="${ctx}/citas?fecha=<%= LocalDate.now() %>">
                <i class="fa-regular fa-calendar-check"></i>
                <h2>Agenda de hoy</h2>
                <p>Ver las citas del día y registrar si fueron atendidas.</p>
            </a>
            <a class="modulo" href="${ctx}/pacientes">
                <i class="fa-solid fa-user-group"></i>
                <h2>Pacientes</h2>
                <p>Registrar, buscar y actualizar los datos de las pacientes.</p>
            </a>
            <c:if test="${u.puedeAgendar}">
                <a class="modulo" href="${ctx}/citas?accion=nuevo">
                    <i class="fa-solid fa-calendar-plus"></i>
                    <h2>Programar cita</h2>
                    <p>Asignar paciente, médico, fecha y hora.</p>
                </a>
            </c:if>
            <c:if test="${u.administrador}">
                <a class="modulo" href="${ctx}/usuarios">
                    <i class="fa-solid fa-user-shield"></i>
                    <h2>Usuarios</h2>
                    <p>Crear cuentas, asignar roles y desactivar accesos.</p>
                </a>
            </c:if>
            <div class="modulo pendiente">
                <i class="fa-solid fa-microscope"></i>
                <h2>Colposcopias</h2>
                <p>Disponible en la siguiente entrega.</p>
            </div>
        </section>
    </main>
</div>
</body>
</html>
