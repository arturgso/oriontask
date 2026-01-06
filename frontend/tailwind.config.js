/** @type {import('tailwindcss').Config} */
const withOpacity = (variable) => ({ opacityValue }) => {
  if (opacityValue === undefined) {
    return `rgb(var(${variable}))`;
  }
  return `rgb(var(${variable}) / ${opacityValue})`;
};

export default {
  content: [
    './index.html',
    './src/**/*.{js,ts,jsx,tsx}',
  ],
  darkMode: ['class', '[data-theme="dark"]'],
  theme: {
    extend: {
      colors: {
        base: withOpacity('--bg-base'),
        card: withOpacity('--bg-card'),
        surface: withOpacity('--bg-surface'),
        primary: withOpacity('--color-primary'),
        accent: withOpacity('--color-accent'),
        border: withOpacity('--border-color'),
        text: {
          primary: withOpacity('--text-primary'),
          muted: withOpacity('--text-muted'),
          secondary: withOpacity('--text-muted'),
        },
      },
    },
  },
  plugins: [],
};
