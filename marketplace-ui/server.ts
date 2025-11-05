import { CommonEngine } from '@angular/ssr';
import express from 'express';
import { fileURLToPath } from 'node:url';
import { dirname, join, resolve } from 'node:path';
import bootstrap from './src/main.server';
import { APP_BASE_HREF } from '@angular/common';
import { environment } from './src/environments/environment';
import { RUNTIME_CONFIG_KEY, ENV_VAR_NAMES, RuntimeConfig } from './src/app/core/models/runtime-config';
import { API_INTERNAL_URL, API_PUBLIC_URL } from './src/app/shared/constants/api.constant';

/**
 * Load runtime configuration from process.env (SSR)
 * Falls back to environment.ts
 */
function loadRuntimeConfigFromEnv(): RuntimeConfig {
  return {
    apiUrl: process.env[ENV_VAR_NAMES.API_URL] || environment.apiUrl,
    githubOAuthAppClientId: process.env[ENV_VAR_NAMES.GITHUB_OAUTH_APP_CLIENT_ID] || environment.githubOAuthAppClientId,
    githubOAuthCallback: process.env[ENV_VAR_NAMES.GITHUB_OAUTH_CALLBACK] || environment.githubOAuthCallback,
    githubApiUrl: process.env[ENV_VAR_NAMES.GITHUB_API_URL] || environment.githubApiUrl,
    dayInMiliseconds: Number.parseInt(process.env[ENV_VAR_NAMES.DAY_IN_MILLISECONDS] || '', 10) || environment.dayInMiliseconds,
    matomoSiteId: Number.parseInt(process.env[ENV_VAR_NAMES.MATOMO_SITE_ID] || '', 10) || environment.matomoSiteId,
    matomoTrackerUrl: process.env[ENV_VAR_NAMES.MATOMO_TRACKER_URL] || environment.matomoTrackerUrl
  };
}

// The Express app is exported so that it can be used by serverless Functions.
export function app(): express.Express {
  const server = express();
  const serverDistFolder = dirname(fileURLToPath(import.meta.url));
  const browserDistFolder = resolve(serverDistFolder, '../browser');
  const indexHtml = join(serverDistFolder, 'index.server.html');
  const commonEngine = new CommonEngine();

  server.set('view engine', 'html');
  server.set('views', browserDistFolder);

  // Serve static files from /browser
  server.get('**', express.static(browserDistFolder, {
    maxAge: '1y',
    index: 'index.html',
  }));

  // All regular routes use the Angular engine
  server.get('**', (req, res, next) => {
    const { protocol, originalUrl, baseUrl, headers } = req;
    const requestProtocol = headers['x-forwarded-proto'] || protocol;
    const requestHost = headers['x-forwarded-host'] || headers.host;
    const apiPublicUrl = `${requestProtocol}://${requestHost}${environment.apiUrl}`;
    const apiInternalUrl = process.env['MARKET_API_INTERNAL_URL'] || environment.apiInternalUrl;
    const runtimeConfig = loadRuntimeConfigFromEnv();

    commonEngine
      .render({
        bootstrap,
        documentFilePath: indexHtml,
        url: `${protocol}://${headers.host}${originalUrl}`,
        publicPath: browserDistFolder,
        providers: [
          { provide: APP_BASE_HREF, useValue: baseUrl },
          { provide: RUNTIME_CONFIG_KEY, useValue: runtimeConfig },
          { provide: API_INTERNAL_URL, useValue: apiInternalUrl },
          { provide: API_PUBLIC_URL, useValue: apiPublicUrl }
        ],
      })
      .then((html) => res.send(html))
      .catch((err) => next(err));
  });

  return server;
}

function run(): void {
  const port = process.env['PORT'] || 4000;

  // Start up the Node server
  const server = app();
  server.listen(port, () => {
    console.log(`Node Express server listening on http://localhost:${port}`);
  });
}

run();
