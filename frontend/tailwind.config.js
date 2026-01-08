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
  darkMode: 'selector',
  theme: {
    extend: {
      colors: {
        base: withOpacity('--bg-base'),
        card: withOpacity('--bg-card'),
        surface: withOpacity('--bg-surface'),
        primary: withOpacity('--color-primary'),
        accent: withOpacity('--color-accent'),
        border: withOpacity('--border-color'),
        stellar: withOpacity('--text-primary'),
        nebula: withOpacity('--text-muted'),
      },
    },
  },
  plugins: [],
};
