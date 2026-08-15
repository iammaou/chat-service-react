import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  define: {
    // Fixes "global is not defined" error in libraries like sockjs-client
    global: "window",
  },
});
