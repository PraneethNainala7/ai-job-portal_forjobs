import Link from "next/link";

interface PageHeaderProps {
  crumbs: { label: string; href?: string }[];
  title: string;
  description?: string;
}

export function PageHeader({ crumbs, title, description }: PageHeaderProps) {
  return (
    <header className="mb-8">
      <nav aria-label="Breadcrumb" className="mb-3 text-[13px]">
        {crumbs.map((crumb, index) => (
          <span key={`${crumb.label}-${index}`}>
            {index > 0 && <span className="mx-2 text-ink-muted">/</span>}
            {crumb.href ? (
              <Link href={crumb.href} className="text-ink-secondary hover:text-ink">
                {crumb.label}
              </Link>
            ) : (
              <span className="text-ink">{crumb.label}</span>
            )}
          </span>
        ))}
      </nav>
      <h1 className="text-[32px] font-bold leading-tight text-ink">{title}</h1>
      {description ? (
        <p className="mt-2 max-w-[65ch] text-sm leading-6 text-ink-secondary">{description}</p>
      ) : null}
    </header>
  );
}
