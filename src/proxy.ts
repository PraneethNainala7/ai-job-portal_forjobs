import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";
import { employerRegisterStep2Path, getHomePath } from "@/config/routes";
import { parseSessionValue, SESSION_COOKIE } from "@/lib/auth/session";
function sessionHomePath(session: NonNullable<ReturnType<typeof parseSessionValue>>) {
  return getHomePath(session.role, session.accountStatus, { cin: session.cin });
}

function nextWithPathname(request: NextRequest, pathname: string) {
  const requestHeaders = new Headers(request.headers);
  requestHeaders.set("x-pathname", pathname);
  return NextResponse.next({
    request: { headers: requestHeaders },
  });
}

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

  if (session && pathname === "/employer/onboarding") {
    return NextResponse.redirect(new URL(employerRegisterStep2Path, request.url));
  }

  if (session && (pathname === "/login" || pathname === "/register" || pathname === "/forgot-password" || pathname === "/reset-password")) {
    const needsEmployerCompanyStep = session.role === "EMPLOYER" && !session.cin;
    if (pathname === "/register" && needsEmployerCompanyStep) {
      if (request.nextUrl.searchParams.get("step") === "2") {
        return nextWithPathname(request, pathname);
      }
      return NextResponse.redirect(new URL(employerRegisterStep2Path, request.url));
    }
    return NextResponse.redirect(new URL(sessionHomePath(session), request.url));
  }

  if (session && pathname.startsWith("/admin") && session.role !== "ADMIN") {
    return NextResponse.redirect(new URL(sessionHomePath(session), request.url));
  }
  if (session && pathname.startsWith("/candidate") && session.role !== "CANDIDATE") {
    return NextResponse.redirect(new URL(sessionHomePath(session), request.url));
  }
  if (session && pathname.startsWith("/employer") && session.role !== "EMPLOYER") {
    return NextResponse.redirect(new URL(sessionHomePath(session), request.url));
  }

  return nextWithPathname(request, pathname);
}
export const config = {
  matcher: ["/admin/:path*", "/candidate/:path*", "/employer/:path*", "/login", "/register", "/forgot-password", "/reset-password"],
};
