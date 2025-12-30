// Force root font size to 10px for tests to match application design
document.documentElement.style.setProperty('font-size', '10px', 'important');

// Inject styles.css manually since it's not being injected by Karma
const link = document.createElement('link');
link.rel = 'stylesheet';
link.href = 'base/styles.css'; // Use base/styles.css to be safe with Karma
document.head.appendChild(link);
