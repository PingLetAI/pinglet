import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

export default defineConfig(() => {
  return {
    plugins: [react(), tailwindcss()],
    build: { rollupOptions: { input: { main: "index.html", get: "get.html" } } },
    server: { host: "0.0.0.0" },
  };
});
