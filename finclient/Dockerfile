# No build stage needed — we copy pre-built files
FROM nginx:1.25-alpine
COPY dist/finclient/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
