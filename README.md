# Software de Gestión de Colposcopia – I.P.S. Endofertil S.A.S.

Proyecto formativo del Tecnólogo en Análisis y Desarrollo de Software (SENA).
Aplicación web en Java (servlets + JSP) sobre Apache Tomcat 9 y MySQL 8.

## Módulos

| Módulo | Ruta | Documentación |
|---|---|---|
| Pacientes | `/pacientes` | — |
| Usuarios (login y roles) | `/login`, `/usuarios` | `LEEME-usuarios-y-citas.md` |
| Citas (agenda) | `/citas` | `LEEME-usuarios-y-citas.md` |
| Servicio web de registro e inicio de sesión | `/api/registro`, `/api/login` | `docs/AA5_EV01_Diseno_servicio_web.md` |

## Puesta en marcha

1. En MySQL Workbench ejecutar `sql/bd_colposcopia.sql` (estructura y datos) y luego `sql/02_usuarios_y_citas.sql` (usuarios de prueba y citas).
2. Copiar `src/main/resources/db.properties.ejemplo` como `db.properties` y escribir la contraseña de MySQL.
3. Abrir en NetBeans, **Clean and Build** y **Run** con Apache Tomcat 9.
4. Abrir `http://localhost:8080/colposcopiaweb/`.

## Probar la API

Importar en Postman `postman/Colposcopia_API_AA5_EV01.postman_collection.json` y usar **Run collection**.
Los endpoints están en `docs/ENDPOINTS.md`.

## Pruebas unitarias

Clic derecho en el proyecto → **Test** (Alt+F6). 45 pruebas JUnit 5.
