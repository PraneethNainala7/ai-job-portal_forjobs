import { PublicFooter, PublicHeader } from "@/components/layout/public-chrome";

import { HomeActionsSection } from "@/features/home/home-actions-section";

import { HomeFeaturedJobsSection } from "@/features/home/home-featured-jobs-section";

import { HomeHeroSection } from "@/features/home/home-hero-section";



export const dynamic = "force-dynamic";



export default async function Home() {

  return (

    <div className="flex min-h-[100dvh] flex-col bg-canvas">

      <PublicHeader />

      <HomeHeroSection />

      <HomeActionsSection />

      <HomeFeaturedJobsSection />

      <PublicFooter />

    </div>

  );

}

