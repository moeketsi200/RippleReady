/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        fnb: {
          teal: '#00AA9E',
          orange: '#EA5B0C',
          dark: '#0B2228',
          card: '#102F38'
        }
      }
    },
  },
  plugins: [],
}
