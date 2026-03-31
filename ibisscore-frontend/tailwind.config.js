/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        primary:  { DEFAULT: '#6366f1', hover: '#4f46e5' },
        success:  '#22c55e',
        warning:  '#f59e0b',
        danger:   '#ef4444',
        surface:  '#1e1e2e',
        card:     '#2a2a3e',
        border:   '#3a3a5c',
        muted:    '#6b7280',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
