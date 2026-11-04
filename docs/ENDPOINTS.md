# ENDPOINTS – Servicio web de registro e inicio de sesión

URL base (desarrollo): `http://localhost:8080/colposcopiaweb`
Encabezado obligatorio: `Content-Type: application/json`

| # | Método | Endpoint | Cuerpo | Respuestas |
|---|---|---|---|---|
| 1 | POST | `/api/registro` | `{"usuario": "laura.gomez", "clave": "Clave2026"}` | 201 registrado · 400 datos inválidos · 409 usuario existente · 500 error interno |
| 2 | POST | `/api/login` | `{"usuario": "auxiliar", "clave": "Auxiliar2026"}` | 200 "Autenticación satisfactoria" · 400 solicitud inválida · 401 "Error en la autenticación" · 500 error interno |

URL completas:

- `http://localhost:8080/colposcopiaweb/api/registro`
- `http://localhost:8080/colposcopiaweb/api/login`

Cualquier otro método HTTP (GET, PUT, DELETE) sobre estos endpoints responde `405 Método no permitido`.
