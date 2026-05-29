/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        brand: {
          ink: "#0B1020",
          panel: "#121A31",
          gold: "#DDB76A",
          mint: "#87E2C8",
          soft: "#94A3B8"
        }
      },
      boxShadow: {
        glow: "0 10px 40px rgba(221,183,106,0.18)"
      }
    }
  },
  plugins: []
};
