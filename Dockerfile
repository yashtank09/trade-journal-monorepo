# Stage 1: Build the Angular application
FROM node:20 AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build:prod

# Stage 2: Serve the application with Nginx
FROM nginx:alpine
# Note: Ensure the path below matches your angular.json "outputPath"
COPY --from=build /app/dist/trade-journal-ui/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
