#!/bin/bash
chown -R www-data:www-data /usr/share/nginx/html/market-cache
chmod -R 755 /usr/share/nginx/html/market-cache
nginx -g "daemon off;"
