//package fuzs.enchantinginfuser.compat;
//
//import fuzs.enchantinginfuser.api.world.item.enchantment.EnchantStatsProvider;
//import net.minecraft.world.item.enchantment.Enchantment;
//import shadows.apotheosis.ench.EnchModule;
//
//public class FabricApotheosisEnchantStatsProvider implements EnchantStatsProvider {
//
//    @Override
//    public String getSourceNamespace() {
//        return "apotheosis";
//    }
//
//    @Override
//    public Enchantment.Rarity getRarity(Enchantment enchantment) {
//        return enchantment.getRarity();
//    }
//
//    @Override
//    public String[] getScalingNamespaces() {
//        return new String[]{"minecraft", "apotheosis"};
//    }
//
//    @Override
//    public double getMaximumCostMultiplier() {
//        return 2.5;
//    }
//
//    @Override
//    public boolean isCompatibleWith(Enchantment enchantment, Enchantment other) {
//        return enchantment.isCompatibleWith(other);
//    }
//
//    @Override
//    public int getMinLevel(Enchantment enchantment) {
//        return enchantment.getMinLevel();
//    }
//
//    @Override
//    public int getMaxLevel(Enchantment enchantment) {
//        return EnchModule.getEnchInfo(enchantment).getMaxLevel();
//    }
//
//    @Override
//    public int getMinCost(Enchantment enchantment, int level) {
//        return EnchModule.getEnchInfo(enchantment).getMinPower(level);
//    }
//
//    @Override
//    public int getMaxCost(Enchantment enchantment, int level) {
//        return EnchModule.getEnchInfo(enchantment).getMaxPower(level);
//    }
//
//    @Override
//    public boolean isTreasureOnly(Enchantment enchantment) {
//        return EnchModule.getEnchInfo(enchantment).isTreasure();
//    }
//
//    @Override
//    public boolean isCurse(Enchantment enchantment) {
//        return enchantment.isCurse();
//    }
//
//    @Override
//    public boolean isTradeable(Enchantment enchantment) {
//        return EnchModule.getEnchInfo(enchantment).isTradeable();
//    }
//
//    @Override
//    public boolean isDiscoverable(Enchantment enchantment) {
//        return EnchModule.getEnchInfo(enchantment).isDiscoverable();
//    }
//}
