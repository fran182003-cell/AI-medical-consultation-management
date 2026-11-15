# Frontend (React)

Single Page Application used by doctors and administrators. It talks to the Spring Boot backend through the REST API (`/api`) and receives patient IDs from the local DNIe bridge over WebSocket (`ws://127.0.0.1:8765`).

## Main components

| File | Description |
|---|---|
| `src/components/Login.jsx` | Login form; stores the JWT returned by the backend |
| `src/components/Dashboard.jsx` | Daily calendar (FullCalendar) with drag-and-drop rescheduling, patient search and DNIe listener |
| `src/components/NuevaCita2.jsx` | Consultation form with medical-term autocompletion ("ghost layer"), similar-case search, AI second opinion and rating |
| `src/components/Adminpanel.jsx` | Administrator panel for managing doctors |
| `src/api/axiosConfig.js` | Axios instance that attaches the `Authorization: Bearer <token>` header |
| `src/api/Comprobar_rol.js` | Decodes the JWT to route users by role |
| `src/datos_medicos.json` | Medical vocabulary used for autocompletion |

## Development

Requirements: Node.js 18 or later.

```bash
npm ci
npm start
```

The development server runs at <http://localhost:3000> and forwards `/api` requests to the backend at `http://localhost:8081` (see `proxy` in `package.json`), so the backend must be running too.

## Production build

The backend serves the compiled frontend as static files. To update it:

```bash
npm run build
```

Then replace the contents of `../src/main/resources/static/` with the contents of `build/`.
