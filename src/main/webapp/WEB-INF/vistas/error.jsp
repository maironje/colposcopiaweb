<%-- Página de error común: errores de base de datos, 404 y 500 --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Error | Gestión de Colposcopia</title>
    <%@include file="/WEB-INF/vistas/comun/head.jspf" %>
</head>
<body class="pagina-error">
<main class="panel caja-error">
    <i class="fa-solid fa-triangle-exclamation"></i>
    <h1>No se pudo completar la solicitud</h1>
    <p>
        <c:choose>
            <c:when test="${not empty detalleError}"><c:out value="${detalleError}"/></c:when>
            <c:when test="${requestScope['javax.servlet.error.status_code'] eq 404}">
                La página que busca no existe. Verifique la dirección.
            </c:when>
            <c:otherwise>Ocurrió un error inesperado. Intente de nuevo en unos minutos.</c:otherwise>
        </c:choose>
    </p>
    <a class="btn btn-primario" href="${pageContext.request.contextPath}/index.jsp">Ir al inicio</a>
</main>
</body>
</html>
