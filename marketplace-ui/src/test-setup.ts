// Inject styles.css manually since it's not being injected by Karma
const link = document.createElement('link');
link.rel = 'stylesheet';
link.href = 'base/styles.css';
document.head.appendChild(link);
