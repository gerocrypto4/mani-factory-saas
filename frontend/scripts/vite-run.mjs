import process from "node:process";
import { build, createServer, preview } from "vite";
import react from "@vitejs/plugin-react";

const root = process.cwd();
const mode = process.argv[2] ?? "dev";

const sharedConfig = {
  configFile: false,
  root,
  plugins: [react()]
};

async function runDev() {
  const server = await createServer({
    ...sharedConfig,
    optimizeDeps: {
      noDiscovery: true
    },
    server: {
      host: "0.0.0.0",
      port: 5173,
      preTransformRequests: false,
      fs: {
        allow: [root]
      }
    }
  });

  await server.listen();
  server.printUrls();

  const shutdown = async () => {
    await server.close();
    process.exit(0);
  };

  process.once("SIGINT", shutdown);
  process.once("SIGTERM", shutdown);
}

async function runBuild() {
  await build(sharedConfig);
}

async function runPreview() {
  const server = await preview({
    ...sharedConfig,
    preview: {
      host: "0.0.0.0",
      port: 5173
    }
  });

  server.printUrls();
}

const runners = {
  dev: runDev,
  build: runBuild,
  preview: runPreview
};

const runner = runners[mode];

if (!runner) {
  console.error(`Unknown frontend command: ${mode}`);
  process.exit(1);
}

runner().catch((error) => {
  console.error(error);
  process.exit(1);
});
