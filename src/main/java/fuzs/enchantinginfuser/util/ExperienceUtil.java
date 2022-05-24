package fuzs.enchantinginfuser.util;

public class ExperienceUtil {

    public static int convertLevelsToExperience(int level) {
        int experience = 0;
        for (int i = 0; i < Math.abs(level); i++) {
            experience += getExperienceForLevelup(i);
        }
        return (int) (Math.signum(level) * experience);
    }

    public static int convertExperienceToLevel(int experience) {
        // rounds up
        final float signum = Math.signum(experience);
        experience = Math.abs(experience);
        int level = 0;
        while (experience > 0) {
            experience -= getExperienceForLevelup(level++);
        }
        level *= signum;
        if (experience != 0 && level < 0) {
            level++;
        }
        return level;
    }

    private static int getExperienceForLevelup(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        } else if (level >= 15) {
            return 37 + (level - 15) * 5;
        }
        return 7 + level * 2;
    }
}
