import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";
import { getHomePath } from "@/config/routes";
import { parseSessionValue, SESSION_COOKIE } from "@/lib/auth/session";

export function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const session = parseSessionValue(request.cookies.get(SESSION_COOKIE)?.value);
  const isProtected =
    pathname.startsWith("/admin") || pathname.startsWith("/candidate") || pathname.startsWith("/employer");

  if (isProtected && !session) {
    const login = new URL("/login", request.url);
    login.searchParams.set("next", pathname);
    return NextResponse.redirect(login);
  }

  if (session && (pathname === "/login" || pathname === "/register")) {
    return NextResponse.redirect(new URL(getHomePath(session.role, session.accountStatus), request.url));
  }

  if (session && pathname.startsWith("/admin") && session.role !== "ADMIN") {
    return NextResponse.redirect(new URL(getHomePath(session.role, session.accountStatus), request.url));
  }
  if (session && pathname.startsWith("/candidate") && session.role !== "CANDIDATE") {
    return NextResponse.redirect(new URL(getHomePath(session.role, session.accountStatus), request.url));
  }
  if (session && pathname.startsWith("/employer") && session.role !== "EMPLOYER") {
    return NextResponse.redirect(new URL(getHomePath(session.role, session.accountStatus), request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/admin/:path*", "/candidate/:path*", "/employer/:path*", "/login", "/register"],
};
