import type { NextConfig } from "next";

const devOrigins = [
  "localhost",
  "127.0.0.1",
  // Allow LAN access during `next dev` (Network URL from the dev server).
  "172.26.16.1",
];

const nextConfig: NextConfig = {
  poweredByHeader: false,
  allowedDevOrigins: devOrigins,
  turbopack: {
    root: process.cwd(),
  },
  async rewrites() {
    const origin = process.env.API_ORIGIN ?? "http://localhost:8080";
    return [{ source: "/api/:path*", destination: `${origin}/api/:path*` }];
  },
};

export default nextConfig;
