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
        canvas: withOpacity('--bg-base'),
        card: withOpacity('--bg-card'),
        surface: withOpacity('--bg-surface'),
        primary: withOpacity('--color-primary'),
        accent: withOpacity('--color-accent'),
        border: withOpacity('--border-color'),
        stellar: withOpacity('--text-primary'),
        nebula: withOpacity('--text-muted'),
      },
      borderRadius: {
        'none': '0',
        'sm': '2px',
        'DEFAULT': '4px',
        'md': '4px',
        'lg': '6px',
        'xl': '8px',
        '2xl': '10px',
        '3xl': '12px',
        'full': '9999px',
      },
    },
  },
  plugins: [],
};
