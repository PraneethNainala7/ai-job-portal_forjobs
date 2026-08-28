"use client";

import { ListIcon, SignOutIcon, XIcon } from "@phosphor-icons/react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useState } from "react";
import { Brand } from "@/components/layout/brand";
import { getRoleNavigation } from "@/config/navigation";
import { StatusBadge } from "@/features/admin/components/status-badge";
import { logoutRequest } from "@/lib/api/auth";
import { cn } from "@/lib/utils";
import type { SessionUser } from "@/types/domain";

export function AppShell({
  children,
  user,
}: {
  children: React.ReactNode;
  user: SessionUser;
}) {
  const pathname = usePathname();
  const router = useRouter();
  const [open, setOpen] = useState(false);
  const navigation = getRoleNavigation(user.role, user.accountStatus);

  async function logout() {
    await logoutRequest();
    router.replace("/login");
    router.refresh();
  }

  return (
    <div className="min-h-[100dvh] bg-canvas lg:grid lg:grid-cols-[260px_minmax(0,1fr)]">
      <header className="sticky top-0 z-30 flex h-[72px] items-center justify-between border-b bg-surface px-4 lg:hidden">
        <Brand />
        <button
          type="button"
          onClick={() => setOpen((value) => !value)}
          className="grid size-11 place-items-center rounded-[var(--radius-control)] border bg-surface"
          aria-label={open ? "Close navigation" : "Open navigation"}
          aria-expanded={open}
        >
          {open ? <XIcon size={22} /> : <ListIcon size={22} />}
        </button>
      </header>

      {open ? (
        <button
          type="button"
          aria-label="Close navigation"
          className="fixed inset-0 z-30 bg-brand/40 lg:hidden"
          onClick={() => setOpen(false)}
        />
      ) : null}

      <aside
        className={cn(
          "fixed inset-y-0 left-0 z-40 flex w-[min(86vw,260px)] flex-col bg-brand p-4 text-white transition-transform duration-200 lg:sticky lg:top-0 lg:h-[100dvh] lg:translate-x-0",
          open ? "translate-x-0" : "-translate-x-full",
        )}
      >
        <div className="hidden h-[56px] items-center px-2 lg:flex">
          <Brand inverted />
        </div>
        <p className="mt-12 px-3 text-xs font-medium uppercase tracking-[0.14em] text-ink-muted lg:mt-6">
          {user.role.toLowerCase()}
        </p>
        <nav className="mt-3 flex flex-1 flex-col gap-1" aria-label="Workspace navigation">
          {navigation.map((item) => {
            const active = pathname === item.href || pathname.startsWith(`${item.href}/`);
            const Icon = item.icon;
            return (
              <Link
                key={item.href}
                href={item.href}
                onClick={() => setOpen(false)}
                className={cn(
                  "flex h-11 items-center gap-3 rounded-[var(--radius-control)] px-[14px] text-sm font-medium transition-colors duration-200",
                  active
                    ? "bg-primary text-white"
                    : "text-ink-muted hover:bg-white/10 hover:text-white",
                )}
                aria-current={active ? "page" : undefined}
              >
                <Icon size={18} weight={active ? "fill" : "regular"} />
                {item.label}
              </Link>
            );
          })}
        </nav>
        <div className="border-t border-white/10 pt-4">
          <p className="truncate px-3 text-sm font-semibold">{user.name}</p>
          <p className="mt-1 truncate px-3 text-xs text-ink-muted">{user.email}</p>
          <button
            type="button"
            onClick={logout}
            className="mt-3 flex h-11 w-full items-center gap-3 rounded-[var(--radius-control)] px-[14px] text-sm text-ink-muted hover:bg-white/10 hover:text-white"
          >
            <SignOutIcon size={18} />
            Logout
          </button>
        </div>
      </aside>

      <div className="min-w-0">
        <div className="hidden h-[72px] items-center justify-between border-b bg-surface px-8 lg:flex">
          <p className="text-sm text-ink-secondary">
            {user.role === "ADMIN" ? "Platform operations" : "Hiring workspace"}
          </p>
          <StatusBadge status={user.accountStatus} />
        </div>
        <main className="px-4 py-6 md:px-6 md:py-8 lg:px-8 lg:py-8">{children}</main>
      </div>
    </div>
  );
}
