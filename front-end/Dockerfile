# No multi-stage build, no nginx — this just runs the exact same dev
# server you already run by hand (`npm run dev`), inside a container
# instead of directly on your machine. Same hot-reload, same behavior,
# same everything — Docker is just what starts it now.
FROM node:22-alpine
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

EXPOSE 5173

HEALTHCHECK --interval=5s --timeout=3s --start-period=15s --retries=5 \
  CMD wget -q --spider http://localhost:5173/ || exit 1

# --host is the one thing that actually differs from local dev: Vite's
# dev server binds to localhost-only by default, which inside a
# container means "unreachable from outside the container." --host
# tells it to listen on all interfaces so the published port mapping in
# docker-compose.yml actually reaches it. No source files changed to
# make this work — package.json's own "dev" script is untouched.
CMD ["npx", "vite", "--host"]
