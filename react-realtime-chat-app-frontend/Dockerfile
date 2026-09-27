# =======================================================
# Stage 1: Build the React app with Vite
# =======================================================
FROM node:20.18.1-alpine3.20 AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

ARG VITE_API_BASE_URL=http://localhost:8088
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN npm run build

# =======================================================
# Stage 2: Serve the static build with Nginx
# =======================================================
FROM nginx:1.27.2-alpine

COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80