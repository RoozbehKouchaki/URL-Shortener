# URL Shortener — Frontend

Angular 19 single-page app for the URL Shortener. It has a login view and a
dashboard where a signed-in user can create short links, list their own links,
deactivate them, and view click counts.

See the [root README](../README.md) for the full-stack overview and demo accounts.

## Run

```bash
npm install
npm start
```

Runs on `http://localhost:4200`. Requests to `/api` are proxied to the backend
on port 8080 (`proxy.conf.json`), so start the backend first.

## Build

```bash
npm run build
```

Output goes to `dist/frontend`.
