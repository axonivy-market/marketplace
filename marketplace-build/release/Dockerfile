FROM nginx
# Create and set permissions at build time
RUN mkdir -p /usr/share/nginx/html/market-cache && \
    chown -R www-data:www-data /usr/share/nginx/html/market-cache && \
    chmod -R 755 /usr/share/nginx/html/market-cache && \
    chmod g+s /usr/share/nginx/html/market-cache

EXPOSE 80