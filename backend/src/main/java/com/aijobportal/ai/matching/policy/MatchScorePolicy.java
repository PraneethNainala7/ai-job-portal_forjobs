package com.aijobportal.ai.matching.policy;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.RoundingMode;

@ConfigurationProperties(prefix = "app.matching")
public class MatchScorePolicy {

    private Weights weights = new Weights();
    private SkillCredits skillCredits = new SkillCredits();
    private Caps caps = new Caps();
    private RoundingMode rounding = RoundingMode.HALF_UP;

    public Weights getWeights() {
        return weights;
    }

    public void setWeights(Weights weights) {
        this.weights = weights;
    }

    public SkillCredits getSkillCredits() {
        return skillCredits;
    }

    public void setSkillCredits(SkillCredits skillCredits) {
        this.skillCredits = skillCredits;
    }

    public Caps getCaps() {
        return caps;
    }

    public void setCaps(Caps caps) {
        this.caps = caps;
    }

    public RoundingMode getRounding() {
        return rounding;
    }

    public void setRounding(RoundingMode rounding) {
        this.rounding = rounding;
    }

    public static class Weights {
        private int requiredSkills = 50;
        private int preferredSkills = 15;
        private int experience = 20;
        private int roleRelevance = 10;
        private int education = 5;

        public int getRequiredSkills() {
            return requiredSkills;
        }

        public void setRequiredSkills(int requiredSkills) {
            this.requiredSkills = requiredSkills;
        }

        public int getPreferredSkills() {
            return preferredSkills;
        }

        public void setPreferredSkills(int preferredSkills) {
            this.preferredSkills = preferredSkills;
        }

        public int getExperience() {
            return experience;
        }

        public void setExperience(int experience) {
            this.experience = experience;
        }

        public int getRoleRelevance() {
            return roleRelevance;
        }

        public void setRoleRelevance(int roleRelevance) {
            this.roleRelevance = roleRelevance;
        }

        public int getEducation() {
            return education;
        }

        public void setEducation(int education) {
            this.education = education;
        }
    }

    public static class SkillCredits {
        private double exact = 1.0;
        private double related = 0.5;
        private double transferable = 0.2;

        public double getExact() {
            return exact;
        }

        public void setExact(double exact) {
            this.exact = exact;
        }

        public double getRelated() {
            return related;
        }

        public void setRelated(double related) {
            this.related = related;
        }

        public double getTransferable() {
            return transferable;
        }

        public void setTransferable(double transferable) {
            this.transferable = transferable;
        }
    }

    public static class Caps {
        private int zeroCriticalMaxScore = 30;
        private double lowRequiredRatioThreshold = 0.30;
        private int lowRequiredMaxScore = 35;

        public int getZeroCriticalMaxScore() {
            return zeroCriticalMaxScore;
        }

        public void setZeroCriticalMaxScore(int zeroCriticalMaxScore) {
            this.zeroCriticalMaxScore = zeroCriticalMaxScore;
        }

        public double getLowRequiredRatioThreshold() {
            return lowRequiredRatioThreshold;
        }

        public void setLowRequiredRatioThreshold(double lowRequiredRatioThreshold) {
            this.lowRequiredRatioThreshold = lowRequiredRatioThreshold;
        }

        public int getLowRequiredMaxScore() {
            return lowRequiredMaxScore;
        }

        public void setLowRequiredMaxScore(int lowRequiredMaxScore) {
            this.lowRequiredMaxScore = lowRequiredMaxScore;
        }
    }
}
