# Módulos Usuarios y Citas – colposcopiaweb

Se agregan sobre el proyecto existente (módulo Pacientes). Mismo patrón MVC:
modelo → DAO → validador → servlet → JSP. Servlets con `javax.servlet` para Tomcat 9.

## Archivos

| Tipo | Archivo | Qué hace |
|---|---|---|
| Nuevo | `modelo/Usuario.java`, `Medico.java`, `Cita.java` | Entidades de las tablas `usuario`, `medico` y `cita` |
| Nuevo | `dao/UsuarioDAO.java`, `MedicoDAO.java`, `CitaDAO.java` | CRUD con PreparedStatement; la agenda usa JOIN con paciente y médico |
| Nuevo | `util/SeguridadClave.java` | Cifrado de contraseñas PBKDF2 (incluido en el JDK, sin dependencias) |
| Nuevo | `util/UsuarioValidador.java`, `CitaValidador.java` | Validaciones del servidor |
| Nuevo | `util/Sesion.java` | Usuario de la sesión y mensajes entre redirecciones |
| Nuevo | `filtro/AutenticacionFiltro.java` | Bloquea todo sin sesión; `/usuarios` solo para Administrador |
| Nuevo | `controlador/LoginServlet.java` (`/login`), `SalirServlet.java` (`/salir`) | Inicio y cierre de sesión |
| Nuevo | `controlador/UsuarioServlet.java` (`/usuarios`), `CitaServlet.java` (`/citas`) | Módulos con GET y POST |
| Nuevo | `WEB-INF/vistas/login.jsp`, `usuarios/*.jsp`, `citas/*.jsp`, `comun/errores.jspf` | Vistas |
| Reemplaza | `index.jsp`, `comun/menu.jsp`, `error.jsp`, `web.xml`, `css/estilos.css` | Menú con sesión, error 403, estilos nuevos |
| Nuevo | `test/.../SeguridadClaveTest`, `UsuarioValidadorTest`, `CitaValidadorTest` | 23 pruebas JUnit |
| Nuevo | `sql/02_usuarios_y_citas.sql` | Ajustes de la BD y datos de prueba |

No se modifica nada del módulo Pacientes (`Paciente`, `PacienteDAO`, `PacienteServlet`, sus JSP).

## Permisos por rol

| Acción | Administrador | Auxiliar | Médico |
|---|:---:|:---:|:---:|
| Pacientes | ✔ | ✔ | ✔ |
| Ver agenda de citas | ✔ | ✔ | ✔ |
| Marcar atendida / inasistencia / cancelar | ✔ | ✔ | ✔ |
| Programar, editar y eliminar citas | ✔ | ✔ | ✘ (403) |
| Gestionar usuarios | ✔ | ✘ (403) | ✘ (403) |

## Reglas de negocio

- Contraseñas cifradas con PBKDF2 y sal aleatoria. Las claves antiguas en texto plano
  (como `123456` de `mairon`) se cifran solas en el primer inicio de sesión.
- Mismo mensaje para usuario inexistente y clave errada (no revela qué cuentas existen).
- Una cuenta inactiva no puede iniciar sesión.
- Un administrador no puede inactivarse, eliminarse ni quitarse el rol; siempre queda al menos un administrador activo.
- Toda cita nueva nace "Programada" y no puede quedar en una fecha pasada.
- Horario de 7:00 a. m. a 5:30 p. m.
- Un médico (o una paciente) no puede tener dos citas no canceladas a menos de 30 minutos.
- Una cita futura no se puede marcar como atendida ni como inasistencia.
- No se elimina una cita que ya tiene colposcopia (llave foránea RESTRICT); se sugiere cancelarla.

## Pruebas funcionales

| # | Caso | Resultado esperado |
|---|---|---|
| 1 | Abrir `/pacientes` sin iniciar sesión | Redirige a `/login` |
| 2 | Ingresar con `mairon` / `123456` | Entra al inicio; en Workbench `password_hash` ya empieza por `pbkdf2$` |
| 3 | Ingresar con clave errada | "Usuario o contraseña incorrectos." |
| 4 | Crear usuario con clave `abc` | Errores de longitud y de letras y números |
| 5 | Crear usuario `laura.gomez`, rol Auxiliar | Mensaje verde; aparece en el listado |
| 6 | Crear otra vez `laura.gomez` | "Ya existe una cuenta con el usuario…" |
| 7 | Inactivar a `laura.gomez` e intentar entrar con ella | "Su cuenta está inactiva…" |
| 8 | Como `auxiliar`, abrir `/usuarios` | Página 403 "No tiene permiso…" |
| 9 | Programar cita para hoy a las 6:30 a. m. | Error de horario |
| 10 | Programar cita con el Dr. Martínez a la misma hora que otra suya | "El médico ya tiene una cita a menos de 30 minutos…" |
| 11 | Filtrar agenda por día y estado | Los filtros aparecen en la URL (GET) |
| 12 | Marcar como atendida la cita de hoy | Etiqueta verde "Atendida" |
| 13 | Como `dr.martinez`, abrir `/citas?accion=nuevo` | Página 403 |
| 14 | Cerrar sesión y presionar "Atrás" | No muestra datos; pide iniciar sesión |
| 15 | Eliminar a la paciente con citas | "No se puede eliminar: la paciente tiene citas registradas." |

Pruebas unitarias: clic derecho en el proyecto → *Test* (Alt+F6).
